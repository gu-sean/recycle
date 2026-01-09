package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import recycle.RankingManager.RankingEntry;

public class RankingWindow extends JPanel {

    private final String currentUserId;

    private recycle.RankingManager manager;
    private JPanel rankListPanel;
    private JLabel infoLabel;
    private int userCurrentPoints;

    private static final Font TITLE_FONT = new Font("맑은 고딕", Font.BOLD, 30);
    private static final Font LABEL_FONT = new Font("맑은 고딕", Font.PLAIN, 16);
    private static final Color[] RANK_COLORS = {
        new Color(255, 223, 0),
        new Color(192, 192, 192),
        new Color(205, 127, 50),
        Color.WHITE,
        Color.WHITE
    };
    
    private static final int MAX_RANK_DISPLAY = 5; 

    public RankingWindow(String userId) {
        this.currentUserId = (userId != null && !userId.isEmpty()) ? userId : "테스트ID";

        try {
            this.manager = new recycle.RankingManager();
        } catch (RuntimeException e) {
            System.err.println("랭킹 관리자 초기화 실패: " + e.getMessage());
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("분리수거 포인트 랭킹", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        add(titleLabel, BorderLayout.NORTH);

        rankListPanel = new JPanel();
        rankListPanel.setLayout(new BoxLayout(rankListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(rankListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        infoLabel = new JLabel("<html><p align='center'>[내 정보] 랭킹 정보를 로드 중입니다...</p></html>", SwingConstants.CENTER);
        infoLabel.setFont(LABEL_FONT);
        infoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        infoLabel.setBackground(new Color(240, 248, 255));
        infoLabel.setOpaque(true);
        add(infoLabel, BorderLayout.SOUTH);

        loadRankingList();
    }

    public void refreshRanking() {
        loadRankingList();
    }

    public void loadRankingList() {
        if (manager == null) {
            System.err.println("RankingManager가 초기화되지 않았습니다. 랭킹 로드 불가.");
            infoLabel.setText("<html><p align='center'>[내 정보] DB 연결 오류</p></html>");
            return;
        }
        try {
            List<RankingManager.RankingEntry> rankingList = manager.getSortedRankingList();

            List<RankingManager.RankingEntry> topRankingList = rankingList.subList(0, Math.min(rankingList.size(), MAX_RANK_DISPLAY));
            
            updateRankListUI(topRankingList);

            updateMyRank(rankingList); 

        } catch (SQLException e) {
            System.err.println("랭킹 정보 로드 중 DB 오류: " + e.getMessage());
            infoLabel.setText("<html><p align='center'>[내 정보] 랭킹 로드 오류</p></html>");
        }
    }

    private void updateRankListUI(List<RankingEntry> rankingList) {
        rankListPanel.removeAll();

        if (rankingList.isEmpty()) {
            JLabel noRank = new JLabel("랭킹 정보가 없습니다.", SwingConstants.CENTER);
            noRank.setFont(LABEL_FONT);
            rankListPanel.add(noRank);
        } else {
            for (int i = 0; i < rankingList.size(); i++) {
                RankingEntry entry = rankingList.get(i);
                int rank = i + 1; 

                rankListPanel.add(createRankItemPanel(rank, entry));

                if (i < rankingList.size() - 1) { 
                    rankListPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
                }
            }
        }
        rankListPanel.revalidate();
        rankListPanel.repaint();
    }

    private JPanel createRankItemPanel(int rank, RankingEntry entry) {
        JPanel itemPanel = new JPanel(new BorderLayout(15, 5));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Color bgColor = Color.WHITE;
        if (rank <= RANK_COLORS.length) { 
            bgColor = RANK_COLORS[rank - 1];
        }
        itemPanel.setBackground(bgColor);

        JLabel rankLabel = new JLabel(String.valueOf(rank));
        rankLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        rankLabel.setPreferredSize(new Dimension(30, 20));
        rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
        itemPanel.add(rankLabel, BorderLayout.WEST);

        JLabel nicknameLabel = new JLabel(entry.getNickname());
        nicknameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        itemPanel.add(nicknameLabel, BorderLayout.CENTER);

        JLabel pointsLabel = new JLabel(String.format("%,d P", entry.getBalancePoints()));
        pointsLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        pointsLabel.setForeground(new Color(0, 100, 200));
        itemPanel.add(pointsLabel, BorderLayout.EAST);

        if (entry.getUserId().equals(currentUserId)) {
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 150, 0), 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
        }
        return itemPanel;
    }

    public void updateMyRank(List<RankingEntry> rankingList) {
        if (manager == null) {
            infoLabel.setText("<html><p align='center'>[내 정보] DB 연결 오류</p></html>");
            this.userCurrentPoints = 0;
            return;
        }
        String myInfoHtml = manager.getMyRankInfo(currentUserId, rankingList);
        infoLabel.setText(myInfoHtml);

        try {
            int start = myInfoHtml.indexOf("현재 포인트: <strong>") + "현재 포인트: <strong>".length();
            int end = myInfoHtml.indexOf("점</strong>", start);
            if (start > 0 && end > start) {
                String pointStr = myInfoHtml.substring(start, end).trim().replaceAll(",", "");
                this.userCurrentPoints = Integer.parseInt(pointStr);
            } else {
                this.userCurrentPoints = 0;
            }
        } catch (Exception e) {
             System.err.println("MyRank 포인트 파싱 오류: " + e.getMessage());
             this.userCurrentPoints = 0;
        }
    }
}