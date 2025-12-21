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
import recycle.RankingWindow; // Window에서 Panel로 명칭/역할 변경 권장
import recycle.ProductWindow; // Window에서 Panel로 명칭/역할 변경 권장
import recycle.AdminWindow;   // Window에서 Panel로 명칭/역할 변경 권장

/**
 * 애플리케이션의 메인 프레임워크
 * 사용자의 권한(일반/관리자)에 따라 탭 구성을 다르게 설정합니다.
 */
public class MainApp extends JFrame {

    private final UserDTO currentUser; 

    public MainApp(UserDTO user) { 
        this.currentUser = user; 
        
        // 1. 프레임 기본 설정
        setupFrame();
        
        // 2. 탭 인터페이스 생성 및 배치
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // 3. 화면 표시
        setVisible(true); 
    }
    
    /**
     * 프레임의 제목, 크기, 위치 등 기본 설정
     */
    private void setupFrame() {
        String title = "EcoCycle - " + currentUser.getNickname();
        if (currentUser.isAdmin()) {
            title += " [관리자 모드]";
        }
        setTitle(title);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 800); // 넉넉한 공간 확보
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null); // 화면 중앙 배치
    }
    
    /**
     * 각 기능별 패널을 탭으로 구성
     */
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        try {
            // [데이터 동기화를 위한 패널 인스턴스 미리 생성]
            
            // 랭킹 패널
            RankingWindow rankingPanel = new RankingWindow(currentUser.getUserId()); 
            
            // 상점/상품 패널
            ProductWindow productPanel = new ProductWindow(currentUser);

            /**
             * ⭐ 새로고침 콜백(Callback)
             * 분리수거 완료, 퀴즈 보상 획득, 상품 구매 등 포인트 변동 시
             * 다른 탭의 데이터(랭킹, 상점 잔액 등)를 즉시 갱신합니다.
             */
            Runnable refreshCallback = () -> {
                System.out.println("[시스템] 데이터 새로고침 요청됨.");
                rankingPanel.refreshRanking(); // 랭킹 리스트 갱신
                productPanel.loadProducts();   // 상품 목록 및 내 포인트 표시 갱신
            };
            
            // --- 탭 구성 시작 ---
            
            // 1. 분리수거 및 활동 기록 (스크롤 적용)
            tabbedPane.addTab("분리수거 및 기록", new JScrollPane(new RecyclePanel(currentUser.getUserId(), refreshCallback)));
            
            // 2. 분리수거 가이드
            tabbedPane.addTab("분리수거 가이드", new Guide());
            
            // 3. 분리수거 퀴즈
            tabbedPane.addTab("분리수거 퀴즈", new QuizPanel(currentUser, refreshCallback));
            
            // 4. 상품 구매 및 상점
            tabbedPane.addTab("상품 구매/교환", productPanel);
            
            // 5. 실시간 랭킹
            tabbedPane.addTab("포인트 랭킹", rankingPanel);

            // ⭐ 관리자 전용 탭 (권한이 있는 경우만 노출)
            if (currentUser.isAdmin()) {
                // 관리자가 상품을 추가/수정하면 상점 탭에도 반영되도록 콜백 전달
                tabbedPane.addTab("⚙️ 시스템 관리", new AdminWindow(refreshCallback));
                
                int adminTabIndex = tabbedPane.getTabCount() - 1;
                tabbedPane.setForegroundAt(adminTabIndex, new Color(220, 20, 60)); // 크림슨 색상으로 강조
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

    /**
     * 메인 진입점
     */
    public static void main(String[] args) {
        // 1. Look and Feel 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
        } catch (Exception e) {
            System.err.println("OS 테마 적용 실패: " + e.getMessage());
        }

        // 2. DB 및 테이블 초기화
        initializeBackend();
        
        // 3. 로그인 창 시작
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