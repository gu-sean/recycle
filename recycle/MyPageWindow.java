package recycle;

import db.DAO.PointLogDAO;
import db.DAO.UserDAO;
import db.DTO.PointLogDTO;
import db.DTO.UserDTO;
import db.RecycleDB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyPageWindow extends JDialog {
    private final String userId;
    private final PointLogDAO pointLogDAO = new PointLogDAO();
    private final UserDAO userDAO = new UserDAO();

    private static final Color BG_DARK = new Color(10, 10, 20);      
    private static final Color BG_CARD = new Color(25, 25, 45);      
    private static final Color POINT_PURPLE = new Color(140, 80, 255); 
    private static final Color POINT_CYAN = new Color(0, 240, 255);     
    private static final Color POINT_RED = new Color(255, 50, 100);     
    private static final Color TEXT_SILVER = new Color(180, 180, 210);
    private static final Color TABLE_HEADER_BG = new Color(35, 35, 60);

    private static final String KOREAN_FONT = "Malgun Gothic";
    private static final Font FONT_TITLE = new Font(KOREAN_FONT, Font.BOLD, 17);
    private static final Font FONT_NORMAL = new Font(KOREAN_FONT, Font.PLAIN, 13);
    private static final String IMG_PATH = "src/Main/webapp/images/rank/";

    public MyPageWindow(Frame owner, String userId) {
        super(owner, "마이페이지", true);
        this.userId = userId;

        setLayout(new BorderLayout());
        setSize(540, 820); 
        setLocationRelativeTo(owner);
        getContentPane().setBackground(BG_DARK);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25)); 
        mainPanel.setBackground(BG_DARK);

        loadAndBuildUI(mainPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null); 
        scrollPane.setViewportBorder(null);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); 

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadAndBuildUI(JPanel mainPanel) {
        try (Connection conn = RecycleDB.connect()) {
            UserDTO user = userDAO.getUserById(userId);
            List<PointLogDTO> logs = pointLogDAO.getPointLogs(conn, userId);
            if (logs == null) logs = new ArrayList<>();

            if (user != null) {
                mainPanel.add(createSectionPanel("프로필", "PROFILE", createProfileContent(user)));
                mainPanel.add(Box.createVerticalStrut(20));
                mainPanel.add(createSectionPanel("통계", "STATS", createStatsAndBadgeContent(logs)));
                mainPanel.add(Box.createVerticalStrut(20));
                mainPanel.add(createSectionPanel("활동 내역", "LOGS", createLogContent(logs)));
            }

            mainPanel.add(Box.createVerticalStrut(25));
            JButton btnClose = new JButton("돌아가기");
            styleButton(btnClose, BG_CARD);
            btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnClose.addActionListener(e -> dispose());
            mainPanel.add(btnClose);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createSectionPanel(String kor, String eng, Component content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setOpaque(false);
        JLabel mainTitle = new JLabel(kor);
        mainTitle.setFont(FONT_TITLE);
        mainTitle.setForeground(Color.WHITE);
        JLabel subTitle = new JLabel(eng);
        subTitle.setFont(new Font("Consolas", Font.PLAIN, 11));
        subTitle.setForeground(POINT_CYAN);
        titlePanel.add(mainTitle);
        titlePanel.add(subTitle);
        titlePanel.setBorder(new EmptyBorder(0, 0, 8, 0));
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProfileContent(UserDTO user) {
        JPanel p = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(40, 40, 70), 0, getHeight(), BG_CARD));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        int pts = user.getBalancePoints();
        String rankTitle, fileName;
        int nextGoal;
        if (pts >= 10000) { rankTitle = "울창한 숲 등급"; fileName = "forest.png"; nextGoal = 20000; }
        else if (pts >= 5000) { rankTitle = "튼튼한 나무 등급"; fileName = "tree.png"; nextGoal = 10000; }
        else if (pts >= 1000) { rankTitle = "파릇한 새싹 등급"; fileName = "sprout.png"; nextGoal = 5000; }
        else { rankTitle = "작은 씨앗 등급"; fileName = "seed.png"; nextGoal = 1000; }

        p.add(new JLabel(getScaledIcon(IMG_PATH + fileName, 85, 85)), BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nick = new JLabel(user.getNickname() + " 님");
        nick.setFont(new Font(KOREAN_FONT, Font.BOLD, 22));
        nick.setForeground(Color.WHITE);

        JLabel point = new JLabel(String.format("%,d P", pts));
        point.setFont(new Font("Consolas", Font.BOLD, 26));
        point.setForeground(POINT_CYAN);

        double progressPercent = Math.min((double) pts / nextGoal, 1.0);
        JPanel progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 60, 80)); 
                g2.fillRoundRect(0, 0, 180, 8, 4, 4);
                g2.setColor(POINT_PURPLE); 
                g2.fillRoundRect(0, 0, (int)(180 * progressPercent), 8, 4, 4);
                g2.dispose();
            }
        };
        progressBar.setOpaque(false);
        progressBar.setMaximumSize(new Dimension(180, 8));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nextGoalLbl = new JLabel("다음 등급까지 " + (nextGoal - pts) + "P 남음");
        nextGoalLbl.setFont(new Font(KOREAN_FONT, Font.PLAIN, 11));
        nextGoalLbl.setForeground(TEXT_SILVER);

        infoPanel.add(nick);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(point);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(progressBar);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(nextGoalLbl);

        p.add(infoPanel, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatsAndBadgeContent(List<PointLogDTO> logs) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        JPanel statsCard = new JPanel(new GridLayout(1, 3, 15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        statsCard.setOpaque(false);
        statsCard.setBorder(new EmptyBorder(20, 15, 20, 15));

        List<PointLogDTO> rLogs = logs.stream().filter(l -> l.getDetail() != null && l.getDetail().contains("분리수거")).collect(Collectors.toList());
        Set<String> uniqueDates = rLogs.stream().map(l -> l.getFormattedTimestamp().substring(0, 10)).collect(Collectors.toSet());
        int streak = calculateStreak(uniqueDates);
        double co2 = uniqueDates.size() * 0.42; 
        long varietyCount = rLogs.stream()
                .flatMap(l -> Stream.of(l.getDetail().contains(":") ? l.getDetail().split(":")[1].split(",") : new String[]{"기타"}))
                .map(item -> item.trim().split(" \\(")[0]).distinct().count();

        statsCard.add(createStatItem("연속 참여", streak + "일"));
        statsCard.add(createStatItem("CO2 절감", String.format("%.1fkg", co2)));
        statsCard.add(createStatItem("품목 다양성", varietyCount + "종"));

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        badgePanel.setOpaque(false);
        badgePanel.add(createBadgeItem("성실왕", "badge_streak.png", streak >= 7));
        badgePanel.add(createBadgeItem("지구 지킴이", "badge_co2.png", co2 >= 5.0));
        badgePanel.add(createBadgeItem("분리수거 박사", "badge_variety.png", varietyCount >= 5));

        container.add(statsCard);
        container.add(Box.createVerticalStrut(15));
        container.add(badgePanel);
        return container;
    }

    private JPanel createStatItem(String title, String value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel t = new JLabel(title, JLabel.CENTER);
        t.setFont(new Font(KOREAN_FONT, Font.PLAIN, 12));
        t.setForeground(TEXT_SILVER);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel v = new JLabel(value, JLabel.CENTER);
        v.setFont(new Font(KOREAN_FONT, Font.BOLD, 20)); 
        v.setForeground(POINT_CYAN);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(t); p.add(Box.createVerticalStrut(5)); p.add(v);
        return p;
    }

    private JPanel createBadgeItem(String name, String fileName, boolean unlocked) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel icon = new JLabel(getScaledIcon(IMG_PATH + fileName, 60, 60));
        icon.setHorizontalAlignment(JLabel.CENTER);
        if (!unlocked) icon.setEnabled(false);
        JLabel n = new JLabel(name, JLabel.CENTER);
        n.setFont(new Font(KOREAN_FONT, Font.BOLD, 11));
        n.setForeground(unlocked ? POINT_CYAN : new Color(80, 80, 100));
        p.add(icon, BorderLayout.CENTER);
        p.add(n, BorderLayout.SOUTH);
        return p;
    }

    private JComponent createLogContent(List<PointLogDTO> logs) {
        if (logs == null || logs.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(BG_CARD);
            emptyPanel.setPreferredSize(new Dimension(0, 240));
            JLabel emptyMsg = new JLabel("<html><center>아직 활동 내역이 없습니다.<br>첫 분리수거를 시작해보세요! ♻️</center></html>");
            emptyMsg.setFont(FONT_NORMAL);
            emptyMsg.setForeground(TEXT_SILVER);
            emptyPanel.add(emptyMsg);
            return emptyPanel;
        }

        String[] header = {"날짜", "상세 내용", "포인트"};
        DefaultTableModel model = new DefaultTableModel(header, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        for (PointLogDTO l : logs) {
            String amountStr = l.getFormattedAmount();
         
            if (!amountStr.startsWith("+") && !amountStr.startsWith("-") && !amountStr.equals("0")) {
                amountStr = "+" + amountStr;
            }
            
            model.addRow(new Object[]{
                l.getFormattedTimestamp().substring(5, 10), l.getDetail(), amountStr
            });
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(38);
        table.setBackground(BG_CARD);
        table.setForeground(Color.WHITE);
        table.setFont(FONT_NORMAL);
        table.setShowGrid(false);           
        table.setIntercellSpacing(new Dimension(0, 0)); 
        table.setFillsViewportHeight(true);

        JTableHeader head = table.getTableHeader();
        head.setBackground(TABLE_HEADER_BG);
        head.setPreferredSize(new Dimension(0, 35));
        head.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                lbl.setBackground(TABLE_HEADER_BG);
                lbl.setForeground(POINT_PURPLE);
                lbl.setFont(new Font(KOREAN_FONT, Font.BOLD, 12));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setBorder(BorderFactory.createEmptyBorder()); 
                return lbl;
            }
        });

        TableColumnModel cm = table.getColumnModel();
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(BG_CARD);
        cm.getColumn(0).setPreferredWidth(60);
        cm.getColumn(0).setCellRenderer(centerRenderer);

        cm.getColumn(1).setPreferredWidth(280);
        cm.getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0)); // 여백 추가
                lbl.setBackground(BG_CARD);
                lbl.setForeground(Color.WHITE);
                return lbl;
            }
        });

        cm.getColumn(2).setPreferredWidth(80);
        cm.getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Consolas", Font.BOLD, 13));
                lbl.setBackground(BG_CARD);
                String val = (v != null) ? v.toString() : "";
                lbl.setForeground(val.contains("-") ? POINT_RED : POINT_CYAN);
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, 240));
        scroll.setBorder(BorderFactory.createEmptyBorder()); 
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(BG_CARD);
        
        JPanel corner = new JPanel();
        corner.setBackground(TABLE_HEADER_BG);
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);
        
        scroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0)); 
        scroll.getVerticalScrollBar().setBackground(BG_CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(15); 

        return scroll;
    }

    static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = POINT_PURPLE;
            this.trackColor = BG_CARD;
        }
        @Override
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() {
            JButton jb = new JButton();
            jb.setPreferredSize(new Dimension(0, 0));
            return jb;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
            g2.dispose();
        }
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    private void styleButton(JButton b, Color bg) {
        b.setPreferredSize(new Dimension(440, 48));
        b.setMaximumSize(new Dimension(440, 48));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font(KOREAN_FONT, Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setBorder(null); 
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(POINT_PURPLE); b.setForeground(Color.BLACK); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(bg); b.setForeground(Color.WHITE); }
        });
    }

    private ImageIcon getScaledIcon(String path, int w, int h) {
        try {
            File f = new File(path);
            if (!f.exists()) return null;
            return new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) { return null; }
    }

    private int calculateStreak(Set<String> dates) {
        int streak = 0;
        LocalDate date = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (dates.contains(date.format(fmt))) {
            streak++;
            date = date.minusDays(1);
        }
        return streak;
    }
}