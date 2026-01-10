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
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.File;
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

    private static final Color BG_DARK = new Color(20, 15, 40);
    private static final Color BG_LIGHT = new Color(40, 45, 90);
    private static final Color POINT_PURPLE = new Color(150, 100, 255);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color POINT_RED = new Color(255, 80, 120);
    private static final Color CARD_BG = new Color(35, 30, 70);

    private static final String IMG_PATH = "src/Main/webapp/images/rank/";

    public MyPageWindow(Frame owner, String userId) {
        super(owner, "내 정보 관리", true);
        this.userId = userId;

        setLayout(new BorderLayout());
        setSize(500, 820); 
        setLocationRelativeTo(owner);
        getContentPane().setBackground(BG_DARK);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 15, 20, 15)); 
        mainPanel.setBackground(BG_DARK);

        try (Connection conn = RecycleDB.connect()) {
            UserDTO user = userDAO.getUserById(userId);
            List<PointLogDTO> logs = pointLogDAO.getPointLogs(conn, userId);

            mainPanel.add(createSectionPanel("👤 내 등급 프로필", createProfileContent(user)));
            mainPanel.add(Box.createVerticalStrut(15));

            mainPanel.add(createSectionPanel("📊 환경 기여 및 명예 배지", createStatsAndBadgeContent(logs)));
            mainPanel.add(Box.createVerticalStrut(15));

            mainPanel.add(createSectionPanel("📝 최근 포인트 내역", createLogContent(logs)));
            mainPanel.add(Box.createVerticalStrut(20));

            JButton btnClose = new JButton("닫기");
            styleButton(btnClose, new Color(70, 70, 100));
            btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnClose.addActionListener(e -> dispose());
            mainPanel.add(btnClose);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("데이터 로드 실패: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            mainPanel.add(errorLabel);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(BG_DARK);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSectionPanel(String title, Component content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setForeground(POINT_CYAN);
        titleLabel.setBorder(new EmptyBorder(0, 5, 8, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProfileContent(UserDTO user) {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(POINT_PURPLE, 1), new EmptyBorder(15, 15, 15, 15)));
        
        int pts = user.getBalancePoints();
        String rankTitle, fileName;
        
        if (pts >= 10000) { rankTitle = "🌳 숲 (Forest)"; fileName = "forest.png"; }
        else if (pts >= 5000) { rankTitle = "🌿 나무 (Tree)"; fileName = "tree.png"; }
        else if (pts >= 1000) { rankTitle = "🌱 새싹 (Sprout)"; fileName = "sprout.png"; }
        else { rankTitle = "🌑 씨앗 (Seed)"; fileName = "seed.png"; }

        JLabel rankIcon = new JLabel(getScaledIcon(IMG_PATH + fileName, 85, 85));
        p.add(rankIcon, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel rankLbl = new JLabel(rankTitle);
        rankLbl.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        rankLbl.setForeground(new Color(180, 180, 255));

        JLabel nick = new JLabel(user.getNickname() + " 님");
        nick.setFont(new Font("맑은 고딕", Font.BOLD, 19));
        nick.setForeground(Color.WHITE);

        JLabel point = new JLabel(String.format("%,d P", pts));
        point.setFont(new Font("Arial", Font.BOLD, 22));
        point.setForeground(POINT_CYAN);

        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(rankLbl);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(nick);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(point);
        infoPanel.add(Box.createVerticalGlue());

        p.add(infoPanel, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatsAndBadgeContent(List<PointLogDTO> logs) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(CARD_BG);
        container.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(POINT_PURPLE, 1), new EmptyBorder(15, 5, 15, 5)));

        List<PointLogDTO> recycleLogs = logs.stream().filter(l -> l.getDetail().contains("분리수거")).collect(Collectors.toList());
        Set<String> uniqueDates = recycleLogs.stream().map(l -> l.getFormattedTimestamp().substring(0, 10)).collect(Collectors.toSet());
        int streak = calculateStreak(uniqueDates);
        double co2 = uniqueDates.size() * 0.4;
        long varietyCount = recycleLogs.stream()
                .flatMap(l -> Stream.of(l.getDetail().contains(":") ? l.getDetail().split(":")[1].split(",") : new String[]{"기타"}))
                .map(item -> item.trim().split(" \\(")[0]).distinct().count();

        JPanel statsTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        statsTextPanel.setOpaque(false);
        
        statsTextPanel.add(createStatLabel("🔥 연속", streak + "일"));
        statsTextPanel.add(new JLabel("|") {{ setForeground(Color.GRAY); }});
        statsTextPanel.add(createStatLabel("🌳 탄소", String.format("%.1fkg", co2)));
        statsTextPanel.add(new JLabel("|") {{ setForeground(Color.GRAY); }});
        statsTextPanel.add(createStatLabel("🌈 품목", varietyCount + "종"));

        container.add(statsTextPanel);
        container.add(Box.createVerticalStrut(15));

        JPanel badgeImgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        badgeImgPanel.setOpaque(false);
        
        badgeImgPanel.add(createBadgeItem("성실왕", IMG_PATH + "badge_streak.png", streak >= 7));
        badgeImgPanel.add(createBadgeItem("수호자", IMG_PATH + "badge_co2.png", co2 >= 5.0));
        badgeImgPanel.add(createBadgeItem("전문가", IMG_PATH + "badge_variety.png", varietyCount >= 5));

        container.add(badgeImgPanel);
        return container;
    }

    private JPanel createStatLabel(String title, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel tLbl = new JLabel(title + ":");
        tLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        tLbl.setForeground(Color.WHITE);
        JLabel vLbl = new JLabel(value);
        vLbl.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        vLbl.setForeground(POINT_CYAN);
        p.add(tLbl);
        p.add(vLbl);
        return p;
    }

    private JPanel createBadgeItem(String name, String path, boolean unlocked) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        
        JLabel iconLabel = new JLabel(getScaledIcon(path, 60, 60)); 
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        
        if (!unlocked) { iconLabel.setEnabled(false); }
        
        JLabel nameLabel = new JLabel(name, JLabel.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 10));
        nameLabel.setForeground(unlocked ? POINT_CYAN : Color.GRAY);
        
        p.add(iconLabel, BorderLayout.CENTER);
        p.add(nameLabel, BorderLayout.SOUTH);
        return p;
    }

    private ImageIcon getScaledIcon(String path, int w, int h) {
        try {
            File imgFile = new File(path);
            if (!imgFile.exists()) return null;
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
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
        table.setRowHeight(32);
        table.setBackground(CARD_BG);
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 90));
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        table.setSelectionBackground(BG_LIGHT);

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(55);
        columnModel.getColumn(1).setPreferredWidth(250);
        columnModel.getColumn(2).setPreferredWidth(65);

        JTableHeader head = table.getTableHeader();
        head.setBackground(BG_LIGHT);
        head.setForeground(POINT_CYAN);
        head.setFont(new Font("맑은 고딕", Font.BOLD, 11));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        columnModel.getColumn(0).setCellRenderer(centerRenderer);
        columnModel.getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setForeground(v.toString().contains("-") ? POINT_RED : POINT_CYAN);
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, 180));
        scroll.setBorder(new LineBorder(POINT_PURPLE, 1));
        scroll.getViewport().setBackground(CARD_BG);
        return scroll;
    }

    private void styleButton(JButton b, Color bg) {
        b.setPreferredSize(new Dimension(380, 45));
        b.setMaximumSize(new Dimension(380, 45));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}