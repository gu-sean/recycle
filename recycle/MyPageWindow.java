package recycle;

import db.DAO.PointLogDAO;
import db.DAO.UserDAO;
import db.DTO.PointLogDTO;
import db.DTO.UserDTO;
import db.RecycleDB;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyPageWindow extends JDialog {
    private final String userId;
    private final PointLogDAO pointLogDAO = new PointLogDAO();
    private final UserDAO userDAO = new UserDAO();

    public MyPageWindow(Frame owner, String userId) {
        super(owner, "마이페이지 - " + userId, true);
        this.userId = userId;

        setLayout(new BorderLayout());
        setSize(480, 800); 
        setLocationRelativeTo(owner);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        try (Connection conn = RecycleDB.connect()) {
            UserDTO user = userDAO.getUserById(userId);
            List<PointLogDTO> logs = pointLogDAO.getPointLogs(conn, userId);

            mainPanel.add(createSectionPanel("👤 내 프로필", createProfileContent(user)));
            mainPanel.add(Box.createVerticalStrut(20));

            mainPanel.add(createSectionPanel("📊 환경 기여 통계", createStatsContent(logs)));
            mainPanel.add(Box.createVerticalStrut(20));

            mainPanel.add(createSectionPanel("📝 최근 포인트 내역", createLogContent(logs)));
            mainPanel.add(Box.createVerticalStrut(20));

            mainPanel.add(createActionPanel());

        } catch (Exception e) {
            e.printStackTrace();
            mainPanel.add(new JLabel("데이터 로드 실패: " + e.getMessage()));
        }

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
    }

    private JPanel createSectionPanel(String title, Component content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProfileContent(UserDTO user) {
        JPanel p = new JPanel(new GridLayout(3, 1, 2, 2));
        p.setBackground(new Color(245, 248, 250));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        int pts = user.getBalancePoints();
        String rank = pts >= 10000 ? "🌳 숲 (Forest)" : pts >= 5000 ? "🌿 나무 (Tree)" : pts >= 1000 ? "🌱 새싹 (Sprout)" : "🌑 씨앗 (Seed)";
        Color rankColor = pts >= 5000 ? new Color(34, 139, 34) : new Color(100, 100, 100);

        JLabel rankLabel = new JLabel("현재 등급: " + rank);
        rankLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        rankLabel.setForeground(rankColor);

        JLabel nick = new JLabel("닉네임: " + user.getNickname());
        JLabel point = new JLabel("보유 자산: " + String.format("%,d P", pts));
        point.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        point.setForeground(new Color(0, 102, 204));
        
        p.add(rankLabel);
        p.add(nick);
        p.add(point);
        return p;
    }

    private JPanel createStatsContent(List<PointLogDTO> logs) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(new Color(250, 255, 250));
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

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

        JLabel sLabel = new JLabel("🔥 연속 실천: " + streak + "일");
        JLabel cLabel = new JLabel(String.format("🌳 탄소 저감: %.1f kg", co2));
        JLabel vLabel = new JLabel("🌈 수거 품목: " + varietyCount + "종");
        
        container.add(sLabel);
        container.add(cLabel);
        container.add(vLabel);
        container.add(Box.createVerticalStrut(15));

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        badgePanel.setOpaque(false);
        
        if (streak >= 7) badgePanel.add(createBadgeLabel("🏅 성실 왕", "7일 연속 실천"));
        if (co2 >= 5.0) badgePanel.add(createBadgeLabel("🌍 수호자", "탄소 5kg 저감"));
        if (varietyCount >= 5) badgePanel.add(createBadgeLabel("🎓 전문가", "5종 품목 수거"));
        
        if (badgePanel.getComponentCount() == 0) {
            JLabel empty = new JLabel("아직 획득한 배지가 없습니다.");
            empty.setFont(new Font("맑은 고딕", Font.ITALIC, 11));
            badgePanel.add(empty);
        }

        container.add(new JLabel("✨ 획득한 배지"));
        container.add(badgePanel);

        return container;
    }

    private JLabel createBadgeLabel(String text, String tooltip) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(new Color(255, 235, 150));
        l.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));
        l.setFont(new Font("맑은 고딕", Font.BOLD, 11));
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

    private JScrollPane createLogContent(List<PointLogDTO> logs) {
        String[] header = {"날짜", "상세 내용", "변동"};
        DefaultTableModel model = new DefaultTableModel(header, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        logs.stream().limit(10).forEach(l -> model.addRow(new Object[]{l.getFormattedTimestamp().substring(5, 10), l.getDetail(), l.getFormattedAmount()}));
        JTable table = new JTable(model);
        table.setRowHeight(25);
        return new JScrollPane(table);
    }

    private JPanel createActionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        p.setBackground(Color.WHITE);
        JButton btnNote = new JButton("오답 노트");
        JButton btnShop = new JButton("상점 이동");
        btnShop.setBackground(new Color(0, 153, 76));
        btnShop.setForeground(Color.WHITE);
        p.add(btnNote); p.add(btnShop);
        return p;
    }
}