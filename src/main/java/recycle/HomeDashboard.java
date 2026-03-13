package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Map;

import db.DTO.UserDTO;
import db.DAO.RecycleLogDAO;
import db.DAO.UserDAO;

public class HomeDashboard extends JPanel {

    private UserDTO user;
    private final Runnable refreshMain;
    
    // UI 요소들
    private JProgressBar gradeProgressBar;
    private JLabel nextGradeLabel, balanceLabel, currentGradeLabel;
    private JLabel co2ValueLabel, treeValueLabel;

    // 네온 테마 컬러
    private static final Color BG_DARK = new Color(13, 11, 25);
    private static final Color CARD_BG = new Color(22, 21, 44);
    private static final Color POINT_CYAN = new Color(0, 240, 255);
    private static final Color POINT_PURPLE = new Color(188, 19, 254);

    public HomeDashboard(UserDTO user, Runnable refreshMain) {
        this.user = user;
        this.refreshMain = refreshMain;
        
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // 상단 헤더
        add(createHeader(), BorderLayout.NORTH);
        
        // 중앙 메인 콘텐츠
        add(createMainContent(), BorderLayout.CENTER);
        
        // 데이터 업데이트
        updateAllData();
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        
        // getName() -> getNickname()으로 수정
        JLabel welcome = new JLabel("반가워요, " + user.getNickname() + "님! 오늘도 지구를 지켜볼까요?");
        welcome.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        welcome.setForeground(Color.WHITE);
        
        // getPoint() -> getFormattedBalance() 활용 (UserDTO에 정의된 포맷 사용)
        balanceLabel = new JLabel("현재 보유 포인트: " + user.getFormattedBalance());
        balanceLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        balanceLabel.setForeground(POINT_CYAN);
        
        p.add(welcome, BorderLayout.WEST);
        p.add(balanceLabel, BorderLayout.EAST);
        return p;
    }

    private JPanel createMainContent() {
        JPanel p = new JPanel(new GridLayout(1, 2, 30, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(30, 0, 0, 0));

        p.add(createGradeCard());
        p.add(createEcoStatsCard());
        
        return p;
    }

    private JPanel createGradeCard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(50, 50, 80), 1), 
            new EmptyBorder(25, 25, 25, 25)
        ));

        // getGrade().getName() 사용
        currentGradeLabel = new JLabel("현재 등급: " + user.getGrade().getName());
        currentGradeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        currentGradeLabel.setForeground(Color.WHITE);
        currentGradeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 진행률 바 (UserDTO의 getGradeProgress 메서드 활용)
        gradeProgressBar = new JProgressBar(0, 100);
        gradeProgressBar.setValue(user.getGradeProgress());
        gradeProgressBar.setForeground(POINT_PURPLE);
        gradeProgressBar.setBackground(new Color(40, 40, 70));
        gradeProgressBar.setMaximumSize(new Dimension(400, 20));

        // 다음 등급까지 남은 포인트 (UserDTO의 getPointsUntilNextGrade 메서드 활용)
        nextGradeLabel = new JLabel("다음 등급까지 남은 포인트: " + user.getPointsUntilNextGrade() + " P");
        nextGradeLabel.setForeground(new Color(180, 180, 200));
        nextGradeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(currentGradeLabel);
        p.add(Box.createVerticalStrut(20));
        p.add(gradeProgressBar);
        p.add(Box.createVerticalStrut(10));
        p.add(nextGradeLabel);
        
        return p;
    }

    private JPanel createEcoStatsCard() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 20));
        p.setOpaque(false);

        co2ValueLabel = new JLabel("CO2 절감량: 0.0kg");
        treeValueLabel = new JLabel("심은 나무 효과: 0.0그루");
        
        p.add(createStatItem("탄소 발자국 감소", co2ValueLabel));
        p.add(createStatItem("나무 심기 효과", treeValueLabel));

        return p;
    }

    private JPanel createStatItem(String title, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel t = new JLabel(title);
        t.setForeground(new Color(150, 150, 180));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        valueLabel.setForeground(POINT_CYAN);
        
        p.add(t, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    public void updateAllData() {
        try {
            UserDAO userDAO = new UserDAO();
            UserDTO updatedUser = userDAO.getUserById(user.getUserId());
            if (updatedUser != null) {
                this.user = updatedUser;
                
                // UI 업데이트 (UserDTO의 필드명에 맞춤)
                balanceLabel.setText("현재 보유 포인트: " + user.getFormattedBalance());
                currentGradeLabel.setText("현재 등급: " + user.getGrade().getName());
                gradeProgressBar.setValue(user.getGradeProgress());
                nextGradeLabel.setText("다음 등급까지 남은 포인트: " + user.getPointsUntilNextGrade() + " P");
                
                // 누적 포인트 기반 환경 수치 계산
                double co2 = user.getTotalPoints() * 0.12;
                co2ValueLabel.setText(String.format("CO2 절감량: %.2f kg", co2));
                treeValueLabel.setText(String.format("심은 나무 효과: %.1f 그루", co2 / 0.5));
            }
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}