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
        
        // 제목창에 사용자 정보 표시
        setTitle("분리수거 포인트 서비스 - [사용자: " + user.getNickname() + " (" + user.getUserId() + ")]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // 탭 내용이 많으므로 높이를 조금 더 키웠습니다.
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        
        // 탭 인터페이스 생성 및 추가
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true); 
    }
    
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        try {
            // 1. 포인트 랭킹 패널 먼저 생성 (새로고침 기능을 다른 패널에 전달하기 위함)
            RankingWindow rankingPanel = new RankingWindow(currentUser.getUserId()); 
            
            // ⭐ 실시간 랭킹 업데이트를 위한 콜백(Runnable) 정의
            Runnable rankUpdateCallback = () -> {
                System.out.println("랭킹 업데이트 콜백 실행...");
                rankingPanel.refreshRanking();
            };
            
            // 2. 분리수거 및 기록 탭 (분리수거 성공 시 랭킹 갱신)
            RecyclePanel recyclePanel = new RecyclePanel(currentUser.getUserId(), rankUpdateCallback); 
            tabbedPane.addTab("분리수거 및 기록", new JScrollPane(recyclePanel));
            
            // 3. 분리수거 가이드 탭
            Guide guidePanel = new Guide();
            tabbedPane.addTab("분리수거 가이드", guidePanel);
            
            // 4. 분리수거 퀴즈 탭 (⭐ 수정: 퀴즈 정답 시 랭킹 실시간 반영을 위해 콜백 전달)
            QuizPanel quizPanel = new QuizPanel(currentUser, rankUpdateCallback); 
            tabbedPane.addTab("분리수거 퀴즈", quizPanel);
            
            // 5. 상품 구매/포인트 교환 탭 
            ProductWindow productPanel = new ProductWindow(currentUser);
            tabbedPane.addTab("상품 구매/교환", new JScrollPane(productPanel));
            
            // 6. 포인트 랭킹 탭 추가
            tabbedPane.addTab("포인트 랭킹", rankingPanel);

        } catch (Exception e) {
             System.err.println("메인 프레임 패널 초기화 오류: " + e.getMessage());
             e.printStackTrace(); // 상세 오류 추적을 위해 추가
             JOptionPane.showMessageDialog(this, 
                 "애플리케이션 초기화 중 오류가 발생했습니다: " + e.getMessage(), 
                 "오류", JOptionPane.ERROR_MESSAGE);
             System.exit(1); 
        }
        
        return tabbedPane;
    }

    public static void main(String[] args) {
        // UI 디자인 설정 (시스템 테마 적용)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {
            System.err.println("Look and Feel 설정 실패: " + e.getMessage());
        }

        // DB 초기화 작업
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
        
        // 로그인 화면 실행
        SwingUtilities.invokeLater(() -> {
            new LoginPanel(); 
        });
    }
}