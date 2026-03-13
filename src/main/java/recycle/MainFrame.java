package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import db.DTO.UserDTO;

public class MainFrame extends JFrame {
    private JPanel contentPanel;    // 실제 내용이 갈아끼워질 중앙 패널
    private CardLayout cardLayout; // 화면 전환 도구
    private UserDTO currentUser;

    public MainFrame(UserDTO user) {
        this.currentUser = user;
        
        setTitle("EcoCycle - 스마트 분리수거");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. 전체 레이아웃 (상단 네비바 + 중앙 콘텐츠)
        setLayout(new BorderLayout());

        // 2. 상단 네비게이션 바 추가
        add(createNavBar(), BorderLayout.NORTH);

        // 3. 중앙 패널 설정 (CardLayout 적용)
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(13, 11, 25));

        // [중요] HomeDashboard를 "HOME"이라는 이름으로 추가
        HomeDashboard home = new HomeDashboard(currentUser, this::refreshHome);
        contentPanel.add(home, "HOME");

        // [참고] 분리수거 기록 화면 추가 (RecyclePanel이 있다고 가정)
        // contentPanel.add(new RecyclePanel(currentUser), "RECYCLE");

        add(contentPanel, BorderLayout.CENTER);

        // 4. 시작 시 무조건 홈 화면이 보이도록 고정
        cardLayout.show(contentPanel, "HOME");
    }

    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(13, 11, 25));
        navBar.setBorder(new EmptyBorder(15, 30, 15, 30));

        // 왼쪽: EcoCycle 로고 (클릭 시 홈으로 이동)
        JLabel logo = new JLabel("EcoCycle");
        logo.setFont(new Font("Arial", Font.ITALIC | Font.BOLD, 26));
        logo.setForeground(new Color(0, 240, 255));
        logo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        logo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 클릭 시 "HOME" 화면으로 전환하고 데이터 갱신
                cardLayout.show(contentPanel, "HOME");
                refreshHome(); 
            }
        });

        // 오른쪽: 메뉴 버튼들 (탭 대신 버튼 사용)
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        menuPanel.setOpaque(false);
        
        JButton recycleBtn = createNavBtn("분리수거 기록");
        recycleBtn.addActionListener(e -> cardLayout.show(contentPanel, "RECYCLE"));
        
        menuPanel.add(recycleBtn);
        navBar.add(logo, BorderLayout.WEST);
        navBar.add(menuPanel, BorderLayout.EAST);
        
        // 하단 구분선
        navBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 40, 60)));
        
        return navBar;
    }

    private JButton createNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorder(null);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void refreshHome() {
        // HomeDashboard 패널을 찾아 데이터 업데이트 함수 호출
        for (Component comp : contentPanel.getComponents()) {
            if (comp instanceof HomeDashboard) {
                ((HomeDashboard) comp).updateAllData();
            }
        }
    }
}