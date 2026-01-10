package Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import db.DAO.RecycleLogDAO; 
import db.DAO.GuideDAO; 
import db.DAO.UserDAO; 
import db.DTO.UserDTO; 

import recycle.LoginPanel;
import recycle.RecyclePanel;
import recycle.Guide;
import recycle.QuizPanel;
import recycle.RankingWindow; 
import recycle.ProductWindow; 
import recycle.AdminWindow;  


public class MainApp extends JFrame {

    private final UserDTO currentUser; 
    
    private static final Color BG_DARK = new Color(20, 15, 40);       
    private static final Color BG_TAB_SELECTED = new Color(40, 35, 70); 
    private static final Color POINT_CYAN = new Color(0, 255, 240);    
    private static final Color TEXT_WHITE = new Color(255, 255, 255);   

    public MainApp(UserDTO user) { 
        this.currentUser = user; 
        
        setupFrame();
        
        applyTabTheme();
        
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true); 
    }
    
    private void setupFrame() {
        String title = "EcoCycle - " + currentUser.getNickname();
        if (currentUser.isAdmin()) title += " [ADMIN MODE]";
        setTitle(title);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 850); 
        setMinimumSize(new Dimension(1000, 750));
        setLocationRelativeTo(null); 
        
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());
    }

    private void applyTabTheme() {
        UIManager.put("TabbedPane.foreground", TEXT_WHITE);         
        UIManager.put("TabbedPane.background", BG_DARK);           
        UIManager.put("TabbedPane.selectedForeground", POINT_CYAN); 
        UIManager.put("TabbedPane.selected", BG_TAB_SELECTED);     
        
        UIManager.put("TabbedPane.contentAreaColor", BG_DARK);
        UIManager.put("TabbedPane.borderHighlightColor", BG_DARK);
        UIManager.put("TabbedPane.darkShadow", BG_DARK);
        UIManager.put("TabbedPane.shadow", BG_DARK);
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0)); 
    }
    
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        tabbedPane.setOpaque(true);
        tabbedPane.setBackground(BG_DARK);

        try {
   
            RankingWindow rankingPanel = new RankingWindow(currentUser.getUserId()); 

       
            Runnable refreshCallback = () -> {
                rankingPanel.refreshRanking(); 
              
            };

            ProductWindow productPanel = new ProductWindow(currentUser, refreshCallback);

            tabbedPane.addTab("  분리수거 및 기록  ", createStyledScroll(new RecyclePanel(currentUser.getUserId(), refreshCallback)));
            tabbedPane.addTab("  분리수거 가이드  ", new Guide());
            tabbedPane.addTab("  분리수거 퀴즈  ", new QuizPanel(currentUser, refreshCallback));
            tabbedPane.addTab("  상품 구매/교환  ", productPanel);
            tabbedPane.addTab("  포인트 랭킹  ", rankingPanel);

            if (currentUser.isAdmin()) {
                tabbedPane.addTab(" ⚙️ 시스템 관리 ", new AdminWindow(refreshCallback));
                int adminIdx = tabbedPane.getTabCount() - 1;
                tabbedPane.setForegroundAt(adminIdx, POINT_CYAN); 
            }

        } catch (Exception e) {
             handleInitializationError(e);
        }
        
        return tabbedPane;
    }

    private JScrollPane createStyledScroll(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void handleInitializationError(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "화면 구성 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeBackend();
        SwingUtilities.invokeLater(() -> new LoginPanel());
    }

    private static void initializeBackend() {
        try {
       
            UserDAO.initializeDatabase();     
            RecycleLogDAO.initializeDatabase(); 
            GuideDAO.initializeDatabase();      
        } catch (Exception e) { 
            System.err.println("Backend Init Warning: " + e.getMessage());
        }
    }
}