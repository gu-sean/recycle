package recycle;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

import db.DTO.UserDTO;
import db.DAO.RecycleLogDAO;
import db.DAO.UserDAO;

public class HomeDashboard extends JPanel {

    private UserDTO user;
    private final Runnable refreshMain;
    
    private JProgressBar gradeProgressBar;
    private JLabel nextGradeLabel, balanceLabel, currentGradeLabel;
    private JLabel co2ValueLabel, treeValueLabel, motivationLabel;
    private JPanel chartPanel;

    private int targetPoints = 0;
    private int displayPoints = 0;
    private String lastGrade = ""; 
    
    private double animationFactor = 0.0;
    private Timer chartTimer;

    private static final Color BG_DARK = new Color(13, 11, 25);
    private static final Color CARD_BG = new Color(22, 21, 44);
    private static final Color POINT_CYAN = new Color(0, 240, 255);
    private static final Color ECO_GREEN = new Color(0, 230, 118);
    private static final Color TEXT_WHITE = new Color(255, 255, 255);
    private static final Color TEXT_GRAY = new Color(170, 170, 185);

    public HomeDashboard(UserDTO user, Runnable refreshMain) {
        this.user = user;
        this.refreshMain = refreshMain;
        this.lastGrade = determineGrade(user.getBalancePoints());
        
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setBackground(BG_DARK);
        mainContent.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        initComponents(mainContent);
        add(mainContent, BorderLayout.CENTER);

        updateAllData();
    }

    public void updateAllData() {
    
        setLoadingState();

        SwingUtilities.invokeLater(() -> {
            try {
                UserDAO userDao = new UserDAO();
                RecycleLogDAO logDao = new RecycleLogDAO();
                
                UserDTO latestUser = userDao.getUserById(user.getUserId());
                
                if (latestUser == null) {
                    motivationLabel.setText(" 사용자 정보를 불러오지 못했습니다.");
                    return;
                }

                Map<DayOfWeek, Integer> stats = logDao.getWeeklyStats(user.getUserId());
                int logCount = logDao.getLogCountByUserId(user.getUserId());

                this.user = latestUser;
                this.targetPoints = user.getBalancePoints();
                
                balanceLabel.setText(String.format("%,d P", targetPoints)); 
                co2ValueLabel.setText(String.format("%.2f kg", logCount * 0.42));
                treeValueLabel.setText(String.format("%.2f 그루", logCount * 0.15));
                
                String currentGrade = determineGrade(latestUser.getBalancePoints());
                currentGradeLabel.setText(currentGrade);
                updateGradeUI(targetPoints);
                updateMotivationMessage(stats.getOrDefault(LocalDate.now().getDayOfWeek(), 0));

                startPointAnimation();
                updateWeeklyChart(stats);
                startChartAnimation();

                if (!this.lastGrade.isEmpty() && !this.lastGrade.equals(currentGrade)) {
                    showGradeUpPopup(currentGrade);
                }
                this.lastGrade = currentGrade;
                
                revalidate();
                repaint();
                
            } catch (Exception e) {
                e.printStackTrace();
                motivationLabel.setText(" 시스템 오류: DB 연결을 확인하세요.");
            }
        });
    }

    private void setLoadingState() {
        balanceLabel.setText("로딩 중...");
        currentGradeLabel.setText("---");
        co2ValueLabel.setText("---");
        treeValueLabel.setText("---");
        motivationLabel.setText(" 최신 데이터를 가져오는 중입니다...");
        motivationLabel.setForeground(TEXT_GRAY);
    }

    private void updateMotivationMessage(int todayCount) {
        String nick = user.getNickname();
        if (todayCount >= 5) {
            motivationLabel.setText("  대단합니다! " + nick + "님은 진정한 에코 챔피언입니다!");
            motivationLabel.setForeground(POINT_CYAN);
        } else if (todayCount > 0) {
            motivationLabel.setText("  멋져요! 오늘 벌써 " + todayCount + "번이나 실천하셨네요.");
            motivationLabel.setForeground(ECO_GREEN);
        } else {
            motivationLabel.setText("  " + nick + "님, 오늘 첫 번째 분리수거를 시작해보세요!");
            motivationLabel.setForeground(TEXT_GRAY);
        }
    }

    private void initComponents(JPanel mainContent) {
        balanceLabel = new JLabel("0 P");
        currentGradeLabel = new JLabel("-");
        co2ValueLabel = new JLabel("0.00 kg");
        treeValueLabel = new JLabel("0.00 그루");
        nextGradeLabel = new JLabel("데이터 로딩 중...");
        motivationLabel = new JLabel("데이터를 불러오는 중...");
        motivationLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        
        gradeProgressBar = new JProgressBar(0, 100);
        gradeProgressBar.setBackground(new Color(40, 40, 60));
        gradeProgressBar.setForeground(POINT_CYAN);
        gradeProgressBar.setBorderPainted(false);
        gradeProgressBar.setPreferredSize(new Dimension(0, 10));

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setOpaque(false);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel(user.getNickname() + "님의 에코 대시보드");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        titlePanel.add(title);
        titlePanel.add(motivationLabel);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(createTextBtn("새로고침 ", e -> updateAllData()));
        btnPanel.add(createTextBtn("활동 상세조회 〉", e -> openMyPage()));
        btnPanel.add(createTextBtn("리포트 저장 ", e -> exportReportImage()));
        
        topArea.add(titlePanel, BorderLayout.WEST);
        topArea.add(btnPanel, BorderLayout.EAST);
        mainContent.add(topArea, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        JPanel leftPanel = new JPanel(new GridLayout(4, 1, 0, 15));
        leftPanel.setOpaque(false);
        leftPanel.add(createStatCard("현재 보유 포인트", balanceLabel, POINT_CYAN));
        leftPanel.add(createStatCard("나의 현재 등급", currentGradeLabel, TEXT_WHITE));
        leftPanel.add(createStatCard("탄소 배출 저감량", co2ValueLabel, ECO_GREEN));
        leftPanel.add(createStatCard("소나무 식재 효과", treeValueLabel, ECO_GREEN));
        
        gbc.gridx = 0; gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 20);
        bodyPanel.add(leftPanel, gbc);

        chartPanel = new JPanel(new GridLayout(1, 7, 10, 0));
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(new Color(60, 60, 90), 1), "주간 활동 통계 (일일 최대 8회)", 
            TitledBorder.LEFT, TitledBorder.TOP, new Font("맑은 고딕", Font.BOLD, 14), TEXT_GRAY));
        
        gbc.gridx = 1; gbc.weightx = 0.65;
        gbc.insets = new Insets(0, 0, 0, 0);
        bodyPanel.add(chartPanel, gbc);

        mainContent.add(bodyPanel, BorderLayout.CENTER);

        JPanel bottomArea = new JPanel(new BorderLayout(0, 8));
        bottomArea.setOpaque(false);
        bottomArea.add(gradeProgressBar, BorderLayout.CENTER);
        bottomArea.add(nextGradeLabel, BorderLayout.SOUTH);
        mainContent.add(bottomArea, BorderLayout.SOUTH);
    }

    private void openMyPage() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof Frame) {
            MyPageWindow myPage = new MyPageWindow((Frame) parentWindow, user.getUserId());
            myPage.setVisible(true);
            updateAllData();
        }
    }

    private void startChartAnimation() {
        animationFactor = 0.0;
        if (chartTimer != null && chartTimer.isRunning()) chartTimer.stop();
        chartTimer = new Timer(20, e -> {
            animationFactor += 0.05;
            if (animationFactor >= 1.0) {
                animationFactor = 1.0;
                chartTimer.stop();
            }
            chartPanel.repaint();
        });
        chartTimer.start();
    }

    private void updateWeeklyChart(Map<DayOfWeek, Integer> stats) {
        chartPanel.removeAll();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        DayOfWeek[] weekOrder = { DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                                 DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY };

        for (DayOfWeek day : weekOrder) {
            chartPanel.add(new BarComponent(getDayName(day), stats.getOrDefault(day, 0), day == today));
        }
        chartPanel.revalidate();
    }

    private String getDayName(DayOfWeek day) {
        return switch(day) {
            case MONDAY -> "월"; case TUESDAY -> "화"; case WEDNESDAY -> "수";
            case THURSDAY -> "목"; case FRIDAY -> "금"; case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

    private class BarComponent extends JComponent {
        private String dayStr;
        private int count;
        private boolean isToday;
        private boolean isHovered = false;

        public BarComponent(String dayStr, int count, boolean isToday) { 
            this.dayStr = dayStr; this.count = count; this.isToday = isToday;
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int barWidth = Math.min(50, Math.max(15, getWidth() / 3));
            int chartStartY = 40; 
            int maxH = getHeight() - 90; 
            int barHeight = (int) (maxH * Math.min(count / 8.0, 1.0) * animationFactor);
            
            if (isHovered || isToday) {
                g2.setColor(new Color(255, 255, 255, isHovered ? 25 : 12));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 12, 12);
            }

            g2.setColor(new Color(255, 255, 255, 8));
            g2.fillRoundRect((getWidth()-barWidth)/2, chartStartY, barWidth, maxH, 8, 8);

            if (count > 0) {
                Color barColor = isToday ? ECO_GREEN : POINT_CYAN;
                if (isHovered) barColor = barColor.brighter();
                
                g2.setColor(barColor);
                g2.fillRoundRect((getWidth()-barWidth)/2, chartStartY + maxH - barHeight, barWidth, barHeight, 8, 8);
                
                if (isHovered) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect((getWidth()-barWidth)/2, chartStartY + maxH - barHeight, barWidth, barHeight, 8, 8);
                }

                if (animationFactor >= 1.0) {
                    g2.setColor(TEXT_WHITE);
                    int fontSize = Math.max(10, getWidth() / 10);
                    g2.setFont(new Font("Arial", Font.BOLD, Math.min(13, fontSize)));
                    String cStr = String.valueOf(count);
                    int textX = (getWidth() - g2.getFontMetrics().stringWidth(cStr)) / 2;
                    int textY = chartStartY + maxH - barHeight - 8;
                    g2.drawString(cStr, textX, textY);
                }
            } else if (isToday) {
                g2.setColor(new Color(ECO_GREEN.getRed(), ECO_GREEN.getGreen(), ECO_GREEN.getBlue(), 100));
                g2.fillRect((getWidth()-barWidth)/2, chartStartY + maxH + 5, barWidth, 3);
            }
            
            g2.setColor(isToday || isHovered ? ECO_GREEN : TEXT_GRAY);
            int labelSize = Math.max(11, getWidth() / 8);
            g2.setFont(new Font("맑은 고딕", isToday || isHovered ? Font.BOLD : Font.PLAIN, Math.min(14, labelSize)));
            g2.drawString(dayStr, (getWidth() - g2.getFontMetrics().stringWidth(dayStr)) / 2, getHeight() - 15);
            g2.dispose();
        }
    }

    private void startPointAnimation() {
       
        displayPoints = 0; 
        Timer timer = new Timer(25, null);
        timer.addActionListener(e -> {
            int diff = Math.abs(targetPoints - displayPoints);
            int step = Math.max(1, diff / 10); 
            
            if (displayPoints < targetPoints) displayPoints += step;
            else if (displayPoints > targetPoints) displayPoints -= step;
            
            if (Math.abs(targetPoints - displayPoints) < step) {
                displayPoints = targetPoints;
                balanceLabel.setText(String.format("%,d P", displayPoints));
                timer.stop();
            } else {
                balanceLabel.setText(String.format("%,d P", displayPoints));
            }
        });
        timer.start();
    }

    private String determineGrade(int pts) {
        if (pts >= 10000) return "울창한 숲";
        if (pts >= 5000) return "튼튼한 나무";
        if (pts >= 1000) return "파릇한 새싹";
        return "작은 씨앗";
    }

    private void updateGradeUI(int pts) {
        String title = determineGrade(pts);
        int goal = pts >= 10000 ? 20000 : pts >= 5000 ? 10000 : pts >= 1000 ? 5000 : 1000;
        currentGradeLabel.setText(title);
        gradeProgressBar.setValue((int)((double)pts/goal * 100));
        nextGradeLabel.setText("다음 등급까지 " + String.format("%,d", (goal - pts)) + "P 남음");
    }

    private void showGradeUpPopup(String newGrade) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                "<html><center><b style='color:#00F0FF; font-size:14px;'>LEVEL UP!</b><br>[" + newGrade + "] 등급을 달성했습니다!</center></html>",
                "등급 상승", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void exportReportImage() {
        try {
            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            this.paint(img.getGraphics());
            ImageIO.write(img, "png", new File("EcoCycle_Report.png"));
            JOptionPane.showMessageDialog(this, "리포트가 이미지로 저장되었습니다.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JButton createTextBtn(String txt, java.awt.event.ActionListener l) {
        JButton b = new JButton(txt);
        b.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        b.setForeground(POINT_CYAN);
        b.setContentAreaFilled(false);
        b.setBorder(null);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(l);
        return b;
    }

    private JPanel createStatCard(String t, JLabel v, Color c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(70, 70, 100), 1), new EmptyBorder(12, 18, 12, 18)));
        JLabel tl = new JLabel(t); 
        tl.setForeground(TEXT_GRAY);
        v.setFont(new Font("맑은 고딕", Font.BOLD, 22)); 
        v.setForeground(c);
        p.add(tl, BorderLayout.NORTH); 
        p.add(v, BorderLayout.CENTER);
        return p;
    }
}