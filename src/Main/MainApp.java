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

/**
 * EcoCycle 메인 프레임워크
 * 테마: 다크 네온 + 화이트 텍스트 (시인성 개선 버전)
 */
public class MainApp extends JFrame {

    private final UserDTO currentUser; 
    
    // --- 디자인 컬러 정의 ---
    private static final Color BG_DARK = new Color(20, 15, 40);       
    private static final Color BG_TAB_SELECTED = new Color(40, 35, 70); 
    private static final Color POINT_CYAN = new Color(0, 255, 240);    
    private static final Color TEXT_WHITE = new Color(255, 255, 255);   

    public MainApp(UserDTO user) { 
        this.currentUser = user; 
        
        // 1. 프레임 기본 설정
        setupFrame();
        
        // 2. 탭 UI 테마 설정
        applyTabTheme();
        
        // 3. 탭 인터페이스 생성 및 배치
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
            // 1. 순서 중요: 랭킹 윈도우 먼저 생성
            RankingWindow rankingPanel = new RankingWindow(currentUser.getUserId()); 

            // 2. 공통 새로고침 콜백 정의 (포인트나 데이터 변경 시 호출)
            // ProductWindow에서 상품 구매 시 랭킹창이 바로 업데이트되도록 연결함
            Runnable refreshCallback = () -> {
                rankingPanel.refreshRanking(); 
                // 필요한 경우 currentUser의 정보를 DB에서 다시 로드하는 로직을 여기에 추가할 수 있습니다.
            };

            // 3. ProductWindow 생성 (DTO와 콜백 전달)
            // 만약 ProductWindow 생성자가 user만 받도록 되어있다면, 클래스 파일에서 인자를 추가해야 합니다.
            ProductWindow productPanel = new ProductWindow(currentUser, refreshCallback);

            // 4. 탭 추가
            tabbedPane.addTab("  분리수거 및 기록  ", createStyledScroll(new RecyclePanel(currentUser.getUserId(), refreshCallback)));
            tabbedPane.addTab("  분리수거 가이드  ", new Guide());
            tabbedPane.addTab("  분리수거 퀴즈  ", new QuizPanel(currentUser, refreshCallback));
            tabbedPane.addTab("  상품 구매/교환  ", productPanel);
            tabbedPane.addTab("  포인트 랭킹  ", rankingPanel);

            // 5. 관리자 전용 탭
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
        scroll.getVerticalScrollBar().setUnitIncrement(16); // 부드러운 스크롤
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
            // DB 초기화 로직 (필요 시)
            UserDAO.initializeDatabase();     
            RecycleLogDAO.initializeDatabase(); 
            GuideDAO.initializeDatabase();      
        } catch (Exception e) { 
            // 초기화 실패 시 경고는 띄우되, 이미 테이블이 있으면 무시하도록 DAO 설계 필요
            System.err.println("Backend Init Warning: " + e.getMessage());
        }
    }
}