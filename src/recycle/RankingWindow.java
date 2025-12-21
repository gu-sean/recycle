package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

import recycle.RankingManager.RankingEntry;

public class RankingWindow extends JPanel {

    private final String currentUserId;
    private recycle.RankingManager manager;
    private JPanel rankListPanel;
    private JLabel infoLabel;
    private int userCurrentPoints;

    // --- 네온 다크 퍼플 테마 색상 ---
    private static final Color BG_DARK = new Color(20, 15, 40);
    private static final Color POINT_PURPLE = new Color(150, 100, 255);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color CARD_BG = new Color(35, 30, 70);
    
    // 랭킹 메달 색상 (금, 은, 동, 나머지)
    private static final Color[] RANK_MEDAL_COLORS = {
        new Color(255, 215, 0),  // Gold
        new Color(192, 192, 192), // Silver
        new Color(205, 127, 50),  // Bronze
        new Color(180, 180, 210)  // Default
    };

    private static final int MAX_RANK_DISPLAY = 5; 

    public RankingWindow(String userId) {
        this.currentUserId = (userId != null && !userId.isEmpty()) ? userId : "테스트ID";

        try {
            this.manager = new recycle.RankingManager();
        } catch (RuntimeException e) {
            System.err.println("랭킹 관리자 초기화 실패: " + e.getMessage());
        }

        setLayout(new BorderLayout(0, 20));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // [상단 타이틀]
        JLabel titleLabel = new JLabel("TOP 5 랭킹 보드", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        titleLabel.setForeground(POINT_CYAN);
        add(titleLabel, BorderLayout.NORTH);

        // [중앙 랭킹 리스트]
        rankListPanel = new JPanel();
        rankListPanel.setLayout(new BoxLayout(rankListPanel, BoxLayout.Y_AXIS));
        rankListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(rankListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // [하단 내 정보 영역]
        infoLabel = new JLabel("", SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setOpaque(true);
        infoLabel.setBackground(BG_LIGHT_PURPLE()); // 커스텀 배경색
        infoLabel.setBorder(new LineBorder(POINT_PURPLE, 2, true));
        infoLabel.setPreferredSize(new Dimension(0, 80));
        infoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        infoLabel.setToolTipText("클릭하면 마이페이지가 열립니다.");
        
        infoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { openMyPage(); }
            @Override
            public void mouseEntered(MouseEvent e) { infoLabel.setBorder(new LineBorder(POINT_CYAN, 2, true)); }
            @Override
            public void mouseExited(MouseEvent e) { infoLabel.setBorder(new LineBorder(POINT_PURPLE, 2, true)); }
        });
        
        add(infoLabel, BorderLayout.SOUTH);

        loadRankingList();
    }

    private Color BG_LIGHT_PURPLE() {
        return new Color(50, 45, 90);
    }

    private void openMyPage() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof Frame) {
            MyPageWindow myPage = new MyPageWindow((Frame) parentWindow, currentUserId);
            myPage.setVisible(true);
        }
    }

    public void refreshRanking() {
        loadRankingList();
    }

    public void loadRankingList() {
        if (manager == null) {
            infoLabel.setText("<html><center>데이터베이스 연결을 확인해주세요.</center></html>");
            return;
        }

        try {
            List<RankingManager.RankingEntry> rankingList = manager.getSortedRankingList();
            List<RankingManager.RankingEntry> topRankingList = rankingList.subList(0, Math.min(rankingList.size(), MAX_RANK_DISPLAY));
            
            updateRankListUI(topRankingList);
            updateMyRank(rankingList); 

        } catch (SQLException e) {
            infoLabel.setText("<html><center>랭킹 로드 중 오류 발생</center></html>");
        }
    }

    private void updateRankListUI(List<RankingEntry> rankingList) {
        rankListPanel.removeAll();

        if (rankingList.isEmpty()) {
            JLabel noRank = new JLabel("현재 집계된 데이터가 없습니다.", SwingConstants.CENTER);
            noRank.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
            noRank.setForeground(Color.GRAY);
            rankListPanel.add(noRank);
        } else {
            for (int i = 0; i < rankingList.size(); i++) {
                rankListPanel.add(createRankItemPanel(i + 1, rankingList.get(i)));
                rankListPanel.add(Box.createVerticalStrut(10)); // 아이템 간 간격
            }
        }
        rankListPanel.revalidate();
        rankListPanel.repaint();
    }

    private JPanel createRankItemPanel(int rank, RankingEntry entry) {
        JPanel itemPanel = new JPanel(new BorderLayout(20, 0));
        itemPanel.setMaximumSize(new Dimension(800, 70));
        itemPanel.setPreferredSize(new Dimension(0, 70));
        itemPanel.setBackground(CARD_BG);
        
        // 내 순위인 경우 강조
        boolean isMe = entry.getUserId().equals(currentUserId);
        if (isMe) {
            itemPanel.setBorder(new LineBorder(POINT_CYAN, 2));
        } else {
            itemPanel.setBorder(new LineBorder(new Color(60, 55, 100), 1));
        }

        // [순위 표시]
        JLabel rankLabel = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
        rankLabel.setFont(new Font("Arial", Font.BOLD, 24));
        rankLabel.setPreferredSize(new Dimension(60, 0));
        Color medalColor = (rank <= 3) ? RANK_MEDAL_COLORS[rank-1] : RANK_MEDAL_COLORS[3];
        rankLabel.setForeground(medalColor);
        itemPanel.add(rankLabel, BorderLayout.WEST);

        // [닉네임]
        JLabel nicknameLabel = new JLabel(entry.getNickname());
        nicknameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        nicknameLabel.setForeground(isMe ? POINT_CYAN : Color.WHITE);
        itemPanel.add(nicknameLabel, BorderLayout.CENTER);

        // [포인트]
        JLabel pointsLabel = new JLabel(String.format("%,d P", entry.getBalancePoints()));
        pointsLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        pointsLabel.setForeground(POINT_PURPLE);
        pointsLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
        itemPanel.add(pointsLabel, BorderLayout.EAST);

        return itemPanel;
    }

    public void updateMyRank(List<RankingEntry> rankingList) {
        if (manager == null) return;

        String myInfoHtml = manager.getMyRankInfo(currentUserId, rankingList);
        
        // HTML 스타일을 다크 테마에 맞게 조정 (RankingManager의 반환값에 따라 필요시 가공)
        String styledInfo = myInfoHtml
            .replace("color: blue", "color: #00fff0") // Cyan
            .replace("color: red", "color: #ff5555"); // Red
        
        infoLabel.setText("<html><center>" + styledInfo + "</center></html>");

        try {
            // 포인트 추출 로직 유지
            int start = myInfoHtml.indexOf("현재 포인트: <strong>") + "현재 포인트: <strong>".length();
            int end = myInfoHtml.indexOf("점</strong>", start);
            if (start > 0 && end > start) {
                String pointStr = myInfoHtml.substring(start, end).trim().replaceAll(",", "");
                this.userCurrentPoints = Integer.parseInt(pointStr);
            }
        } catch (Exception e) {
            this.userCurrentPoints = 0;
        }
    }
}