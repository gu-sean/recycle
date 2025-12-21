package recycle;

import db.DAO.PointLogDAO;
import db.DAO.UserDAO;
import db.DTO.PointLogDTO;
import db.DTO.UserDTO;
import db.RecycleDB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyPageWindow extends JDialog {
    private final String userId;
    private final PointLogDAO pointLogDAO = new PointLogDAO();
    private final UserDAO userDAO = new UserDAO();

    // --- 네온 다크 퍼플 테마 색상 ---
    private static final Color BG_DARK = new Color(20, 15, 40);
    private static final Color BG_LIGHT = new Color(40, 45, 90);
    private static final Color POINT_PURPLE = new Color(150, 100, 255);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color CARD_BG = new Color(35, 30, 70);

    public MyPageWindow(Frame owner, String userId) {
        super(owner, "마이페이지 - " + userId, true);
        this.userId = userId;

        setLayout(new BorderLayout());
        setSize(520, 850); 
        setLocationRelativeTo(owner);
        getContentPane().setBackground(BG_DARK);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(BG_DARK);

        try (Connection conn = RecycleDB.connect()) {
            UserDTO user = userDAO.getUserById(userId);
            List<PointLogDTO> logs = pointLogDAO.getPointLogs(conn, userId);

            // 1. 사용자 정보 및 등급
            mainPanel.add(createSectionPanel("👤 내 프로필", createProfileContent(user)));
            mainPanel.add(Box.createVerticalStrut(25));

            // 2. 환경 기여 통계 및 배지
            mainPanel.add(createSectionPanel("📊 환경 기여 통계", createStatsContent(logs)));
            mainPanel.add(Box.createVerticalStrut(25));

            // 3. 포인트 활동 로그
            mainPanel.add(createSectionPanel("📝 최근 포인트 내역", createLogContent(logs)));
            mainPanel.add(Box.createVerticalStrut(30));

            // 4. 하단 액션 버튼
            mainPanel.add(createActionPanel());

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("데이터 로드 실패: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            mainPanel.add(errorLabel);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSectionPanel(String title, Component content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setForeground(POINT_CYAN);
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // [1] 프로필 및 등급 표시
    private JPanel createProfileContent(UserDTO user) {
        JPanel p = new JPanel(new GridLayout(3, 1, 5, 5));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(POINT_PURPLE, 1), new EmptyBorder(20, 20, 20, 20)));
        
        int pts = user.getBalancePoints();
        String rank = pts >= 10000 ? "🌳 숲 (Forest)" : pts >= 5000 ? "🌿 나무 (Tree)" : pts >= 1000 ? "🌱 새싹 (Sprout)" : "🌑 씨앗 (Seed)";
        
        JLabel rankLabel = new JLabel(rank);
        rankLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        rankLabel.setForeground(new Color(180, 180, 255));

        JLabel nick = new JLabel(user.getNickname() + " 님");
        nick.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        nick.setForeground(Color.WHITE);

        JLabel point = new JLabel(String.format("%,d P", pts));
        point.setFont(new Font("Arial", Font.BOLD, 26));
        point.setForeground(POINT_CYAN);
        
        p.add(rankLabel);
        p.add(nick);
        p.add(point);
        return p;
    }

    // [2] 통계 및 배지 시스템
    private JPanel createStatsContent(List<PointLogDTO> logs) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CARD_BG);
        container.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(POINT_PURPLE, 1), new EmptyBorder(20, 20, 20, 20)));

        List<PointLogDTO> recycleLogs = logs.stream()
                .filter(l -> l.getDetail().contains("분리수거"))
                .collect(Collectors.toList());

        Set<String> uniqueDates = recycleLogs.stream()
                .map(l -> l.getFormattedTimestamp().substring(0, 10))
                .collect(Collectors.toSet());

        int streak = calculateStreak(uniqueDates);
        double co2 = uniqueDates.size() * 0.4;
        long varietyCount = recycleLogs.stream()
                .flatMap(l -> Stream.of(l.getDetail().contains(":") ? l.getDetail().split(":")[1].split(",") : new String[]{"기타"}))
                .map(item -> item.trim().split(" \\(")[0]).distinct().count();

        String statsText = String.format("<html><body style='color:white; font-family:맑은 고딕; font-size:12px;'>"
                + "🔥 연속 실천: <font color='#00fff0'>%d일</font><br>"
                + "🌳 탄소 저감: <font color='#00fff0'>%.1f kg</font><br>"
                + "🌈 수거 품목: <font color='#00fff0'>%d종</font></body></html>", streak, co2, varietyCount);
        
        JLabel statsLabel = new JLabel(statsText);
        container.add(statsLabel);
        container.add(Box.createVerticalStrut(20));

        JLabel badgeTitle = new JLabel("✨ 획득한 명예 배지");
        badgeTitle.setForeground(new Color(200, 200, 200));
        badgeTitle.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        container.add(badgeTitle);
        container.add(Box.createVerticalStrut(10));

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        badgePanel.setOpaque(false);
        
        if (streak >= 7) badgePanel.add(createBadgeLabel("🏅 성실 왕", "7일 연속 실천"));
        if (co2 >= 5.0) badgePanel.add(createBadgeLabel("🌍 수호자", "탄소 5kg 저감"));
        if (varietyCount >= 5) badgePanel.add(createBadgeLabel("🎓 전문가", "5종 품목 수거"));
        
        if (badgePanel.getComponentCount() == 0) {
            JLabel empty = new JLabel("활동을 통해 배지를 획득하세요!");
            empty.setForeground(Color.GRAY);
            empty.setFont(new Font("맑은 고딕", Font.ITALIC, 12));
            badgePanel.add(empty);
        }
        container.add(badgePanel);

        return container;
    }

    private JLabel createBadgeLabel(String text, String tooltip) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(BG_LIGHT);
        l.setForeground(POINT_CYAN);
        l.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(POINT_CYAN, 1), new EmptyBorder(5, 10, 5, 10)));
        l.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        l.setToolTipText(tooltip);
        return l;
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

    // [3] 포인트 활동 로그 테이블 수정
    private JScrollPane createLogContent(List<PointLogDTO> logs) {
        String[] header = {"날짜", "상세 내용", "변동"};
        DefaultTableModel model = new DefaultTableModel(header, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        logs.stream().limit(10).forEach(l -> model.addRow(new Object[]{l.getFormattedTimestamp().substring(5, 10), l.getDetail(), l.getFormattedAmount()}));
        
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setBackground(CARD_BG);
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 90));
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.setSelectionBackground(BG_LIGHT);

        // 헤더 디자인
        JTableHeader head = table.getTableHeader();
        head.setBackground(BG_LIGHT);
        head.setForeground(POINT_CYAN);
        head.setFont(new Font("맑은 고딕", Font.BOLD, 13));

        // 셀 가운데 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setOpaque(false);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, 200));
        scroll.setBorder(new LineBorder(POINT_PURPLE, 1));
        scroll.getViewport().setBackground(CARD_BG);
        return scroll;
    }

    private JPanel createActionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        p.setOpaque(false);
        
        JButton btnClose = new JButton("닫기");
        styleButton(btnClose, new Color(100, 100, 100));
        btnClose.addActionListener(e -> dispose());
        
        JButton btnShop = new JButton("포인트 상점");
        styleButton(btnShop, POINT_PURPLE);
        // 상점 이동 로직은 메인 프레임에서 제어하거나 필요시 추가

        p.add(btnClose); 
        p.add(btnShop);
        return p;
    }

    private void styleButton(JButton b, Color bg) {
        b.setPreferredSize(new Dimension(160, 45));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}