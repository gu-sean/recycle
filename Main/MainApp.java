package Main;

import javax.swing.*;
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

    public MainApp(UserDTO user) { 
        this.currentUser = user; 
        
        setupFrame();
        
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true); 
    }
    
 
    private void setupFrame() {
        String title = "EcoCycle - " + currentUser.getNickname();
        if (currentUser.isAdmin()) {
            title += " [관리자 모드]";
        }
        setTitle(title);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800); 
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null); 
    }
    
  
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        try {
          
            RankingWindow rankingPanel = new RankingWindow(currentUser.getUserId()); 
            
            ProductWindow productPanel = new ProductWindow(currentUser);

         
            Runnable refreshCallback = () -> {
                System.out.println("[시스템] 데이터 새로고침 요청됨.");
                rankingPanel.refreshRanking();
                productPanel.loadProducts();  
            };
            
           
            tabbedPane.addTab("분리수거 및 기록", new JScrollPane(new RecyclePanel(currentUser.getUserId(), refreshCallback)));
            
            tabbedPane.addTab("분리수거 가이드", new Guide());
            
            tabbedPane.addTab("분리수거 퀴즈", new QuizPanel(currentUser, refreshCallback));
            
            tabbedPane.addTab("상품 구매/교환", productPanel);
            
            tabbedPane.addTab("포인트 랭킹", rankingPanel);

            if (currentUser.isAdmin()) {
            
                tabbedPane.addTab("⚙️ 시스템 관리", new AdminWindow(refreshCallback));
                
                int adminTabIndex = tabbedPane.getTabCount() - 1;
                tabbedPane.setForegroundAt(adminTabIndex, new Color(220, 20, 60)); 
            }

        } catch (Exception e) {
             handleInitializationError(e);
        }
        
        return tabbedPane;
    }

    private void handleInitializationError(Exception e) {
        System.err.println("메인 프레임 초기화 중 치명적 오류: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "화면을 구성하는 중 오류가 발생했습니다.\n" + e.getMessage(), 
            "초기화 오류", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

  
    public static void main(String[] args) {
   
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {
            System.err.println("OS 테마 적용 실패: " + e.getMessage());
        }

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
            System.out.println(">>> 데이터베이스 인프라 준비 완료.");
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(null, 
                "데이터베이스 연결에 실패했습니다.\n" + e.getMessage(), 
                "DB 접속 오류", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}