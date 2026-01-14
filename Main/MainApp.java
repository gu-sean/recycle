package Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import db.DAO.RecycleLogDAO; 
import db.DAO.GuideDAO; 
import db.DAO.UserDAO; 
import db.DTO.UserDTO; 

import recycle.LoginPanel;
import recycle.HomeDashboard; 
import recycle.RecyclePanel;
import recycle.Guide;
import recycle.QuizPanel;
import recycle.RankingWindow; 
import recycle.ProductWindow; 
import recycle.AdminWindow;  


public class MainApp extends JFrame {

    private final UserDTO currentUser; 
    private HomeDashboard homeDashboard; 
    
    // --- 디자인 시스템 컬러 ---
    private static final Color BG_DARK = new Color(15, 12, 30);       
    private static final Color BG_TAB_AREA = new Color(20, 18, 45);   
    private static final Color POINT_CYAN = new Color(0, 255, 240);    
    private static final Color TEXT_WHITE = new Color(240, 240, 250);  
    private static final Color TEXT_DIM = new Color(150, 150, 180);    

    public MainApp(UserDTO user) { 
        this.currentUser = user; 
        
        applyGlobalTheme();
        setupFrame();
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        setVisible(true); 
    }
    
    private void setupFrame() {
        setTitle("에코사이클(EcoCycle) - " + currentUser.getNickname());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmExit(); }
        });
        
        setSize(1280, 850); 
        setMinimumSize(new Dimension(1100, 750));
        setLocationRelativeTo(null); 
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());
    }

    private void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setPreferredSize(new Dimension(getWidth(), 70));
        header.setBorder(new EmptyBorder(10, 25, 0, 25));

        JLabel logoLabel = new JLabel("ECO CYCLE");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLabel.setForeground(POINT_CYAN);
        header.add(logoLabel, BorderLayout.WEST);

        JPanel userArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        userArea.setOpaque(false);

        JLabel welcomeLabel = new JLabel(currentUser.getNickname() + "님 접속 중");
        welcomeLabel.setForeground(TEXT_WHITE);
        welcomeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));

        JButton logoutBtn = new JButton("로그아웃");
        logoutBtn.setBackground(new Color(220, 50, 50));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());

        userArea.add(welcomeLabel);
        userArea.add(logoutBtn);
        header.add(userArea, BorderLayout.EAST);

        return header;
    }
    
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new CustomTabbedUI());
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        
  
        tabbedPane.setForeground(Color.WHITE); 
        
        tabbedPane.setBorder(new EmptyBorder(10, 15, 10, 15));

        try {
            RankingWindow rankingPanel = new RankingWindow(currentUser.getUserId()); 
            
            Runnable refreshCallback = () -> {
                rankingPanel.refreshRanking(); 
                if (homeDashboard != null) {
                    homeDashboard.updateAllData(); 
                }
            };

            homeDashboard = new HomeDashboard(currentUser, refreshCallback);

            tabbedPane.addTab("  홈 대시보드  ", homeDashboard);
            tabbedPane.addTab("  분리수거 기록  ", createStyledScroll(new RecyclePanel(currentUser.getUserId(), refreshCallback)));
            tabbedPane.addTab("  수거 백과사전  ", new Guide());
            tabbedPane.addTab("  에코 퀴즈  ", new QuizPanel(currentUser, refreshCallback));
            tabbedPane.addTab("  에코 상점  ", new ProductWindow(currentUser, refreshCallback));
            tabbedPane.addTab("  전체 랭킹  ", rankingPanel);

            if (currentUser.isAdmin()) {
                tabbedPane.addTab(" 시스템 관리 ", new AdminWindow(refreshCallback));
            }

            tabbedPane.addChangeListener(e -> {
                if (tabbedPane.getSelectedIndex() == 0 && homeDashboard != null) {
                    homeDashboard.updateAllData();
                }
            });

        } catch (Exception e) {
             handleInitializationError(e);
        }
        
        return tabbedPane;
    }

    /**
     * 커스텀 탭 디자인 UI
     */
    private class CustomTabbedUI extends BasicTabbedPaneUI {
        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected) {
                g2.setColor(BG_TAB_AREA); g2.fillRect(x, y, w, h);
                g2.setColor(POINT_CYAN); g2.fillRect(x, y + h - 3, w, 3);
            } else {
                g2.setColor(BG_DARK); g2.fillRect(x, y, w, h);
            }
        }

  
        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            g.setFont(font);
            if (isSelected) {
                g.setColor(POINT_CYAN); // 선택된 탭은 청록색
            } else {
                g.setColor(Color.WHITE); // 선택되지 않은 탭은 흰색
            }
            int x = textRect.x;
            int y = textRect.y + metrics.getAscent();
            g.drawString(title, x, y);
        }

        @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}
        @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {}
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(10, 8, 25));
        statusBar.setPreferredSize(new Dimension(getWidth(), 30));
        statusBar.setBorder(new EmptyBorder(5, 20, 5, 20));
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        JLabel infoLabel = new JLabel("상태: 서버 연결됨 | 실시간 동기화 활성화 | " + time);
        infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        infoLabel.setForeground(TEXT_DIM);
        statusBar.add(infoLabel, BorderLayout.WEST);
        return statusBar;
    }

    private JScrollPane createStyledScroll(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        scroll.getViewport().setBackground(BG_DARK);
        return scroll;
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dispose();
            new LoginPanel();
        }
    }

    private void confirmExit() {
        if (JOptionPane.showConfirmDialog(this, "종료하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
    }

    private void handleInitializationError(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "초기화 오류: " + e.getMessage());
        System.exit(1);
    }

    public static void main(String[] args) {
        initializeBackend();
        SwingUtilities.invokeLater(() -> {
            new LoginPanel();
        });
    }

    private static void initializeBackend() {
        try {
            UserDAO.initializeDatabase();
            RecycleLogDAO.initializeDatabase();
            GuideDAO.initializeDatabase();
        } catch (Exception e) {
            System.err.println("DB 연결 오류: " + e.getMessage());
        }
    }
}