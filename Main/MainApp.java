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

public class MainApp extends JFrame {

    private final UserDTO currentUser; 

    public MainApp(UserDTO user) { 
        this.currentUser = user; 
        
        setTitle("분리수거 포인트 서비스 - [사용자: " + user.getNickname() + " (" + user.getUserId() + ")]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); 
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true); 
    }
    
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        try {
   
            RankingWindow rankingPanel = new RankingWindow(currentUser.getUserId()); 
            
            Runnable rankUpdateCallback = () -> {
                System.out.println("랭킹 업데이트 콜백 실행...");
                rankingPanel.refreshRanking();
            };
            
            RecyclePanel recyclePanel = new RecyclePanel(currentUser.getUserId(), rankUpdateCallback); 
            tabbedPane.addTab("분리수거 및 기록", new JScrollPane(recyclePanel));
            
            Guide guidePanel = new Guide();
            tabbedPane.addTab("분리수거 가이드", guidePanel);
            
            QuizPanel quizPanel = new QuizPanel(currentUser, rankUpdateCallback); 
            tabbedPane.addTab("분리수거 퀴즈", quizPanel);
            
            ProductWindow productPanel = new ProductWindow(currentUser);
            tabbedPane.addTab("상품 구매/교환", new JScrollPane(productPanel));
            
            tabbedPane.addTab("포인트 랭킹", rankingPanel);

        } catch (Exception e) {
             System.err.println("메인 프레임 패널 초기화 오류: " + e.getMessage());
             e.printStackTrace(); 
             JOptionPane.showMessageDialog(this, 
                 "애플리케이션 초기화 중 오류가 발생했습니다: " + e.getMessage(), 
                 "오류", JOptionPane.ERROR_MESSAGE);
             System.exit(1); 
        }
        
        return tabbedPane;
    }

    public static void main(String[] args) {
     
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {
            System.err.println("Look and Feel 설정 실패: " + e.getMessage());
        }

        try {
            UserDAO.initializeDatabase();     
            RecycleLogDAO.initializeDatabase(); 
            GuideDAO.initializeDatabase();      
            System.out.println("DB 테이블 및 초기 데이터 설정 완료.");
        } catch (Exception e) { 
            System.err.println("심각한 DB 초기화 오류 발생: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "프로그램 시작 전 DB 초기화에 실패했습니다. 프로그램을 종료합니다.\n" + e.getMessage(), 
                "심각한 오류", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginPanel(); 
        });
    }
}