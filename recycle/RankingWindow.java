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

    private static final Color BG_DARK = new Color(20, 15, 40);
    private static final Color POINT_PURPLE = new Color(150, 100, 255);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color CARD_BG = new Color(35, 30, 70);
    
    private static final Color[] RANK_MEDAL_COLORS = {
        new Color(255, 215, 0),  
        new Color(192, 192, 192),
        new Color(205, 127, 50), 
        new Color(180, 180, 210)  
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

        JLabel titleLabel = new JLabel("TOP 5 랭킹 보드", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        titleLabel.setForeground(POINT_CYAN);
        add(titleLabel, BorderLayout.NORTH);

        rankListPanel = new JPanel();
        rankListPanel.setLayout(new BoxLayout(rankListPanel, BoxLayout.Y_AXIS));
        rankListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(rankListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        infoLabel = new JLabel("", SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setOpaque(true);
        infoLabel.setBackground(new Color(50, 45, 90));
        infoLabel.setBorder(new LineBorder(POINT_PURPLE, 2, true));
        infoLabel.setPreferredSize(new Dimension(0, 80));
        infoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        infoLabel.setToolTipText("클릭하면 마이페이지가 열립니다.");
        
        infoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { 
                openMyPage(); 
            }
            @Override
            public void mouseEntered(MouseEvent e) { 
                infoLabel.setBorder(new LineBorder(POINT_CYAN, 2, true)); 
                infoLabel.setBackground(new Color(70, 65, 110)); 
            }
            @Override
            public void mouseExited(MouseEvent e) { 
                infoLabel.setBorder(new LineBorder(POINT_PURPLE, 2, true)); 
                infoLabel.setBackground(new Color(50, 45, 90));
            }
        });
        
        add(infoLabel, BorderLayout.SOUTH);

        loadRankingList();
    }

    
    private void openMyPage() {

        Window ancestor = SwingUtilities.getWindowAncestor(this);
        
        if (ancestor instanceof Frame) {
   
            MyPageWindow myPage = new MyPageWindow((Frame) ancestor, currentUserId);
            myPage.setLocationRelativeTo(ancestor); 
            myPage.setVisible(true);
            
            refreshRanking();
        } else {
            System.err.println("부모 프레임을 찾을 수 없어 마이페이지를 열 수 없습니다.");
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
            int limit = Math.min(rankingList.size(), MAX_RANK_DISPLAY);
            List<RankingManager.RankingEntry> topRankingList = rankingList.subList(0, limit);
            
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
                rankListPanel.add(Box.createVerticalStrut(10));
            }
        }
        rankListPanel.revalidate();
        rankListPanel.repaint();
    }

    private JPanel createRankItemPanel(int rank, RankingEntry entry) {
        JPanel itemPanel = new JPanel(new BorderLayout(20, 0));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        itemPanel.setPreferredSize(new Dimension(0, 70));
        itemPanel.setBackground(CARD_BG);
        
        boolean isMe = entry.getUserId().equals(currentUserId);
        if (isMe) {
            itemPanel.setBorder(new LineBorder(POINT_CYAN, 2));
        } else {
            itemPanel.setBorder(new LineBorder(new Color(60, 55, 100), 1));
        }

        JLabel rankLabel = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
        rankLabel.setFont(new Font("Arial", Font.BOLD, 24));
        rankLabel.setPreferredSize(new Dimension(80, 0));
        Color medalColor = (rank <= 3) ? RANK_MEDAL_COLORS[rank-1] : RANK_MEDAL_COLORS[3];
        rankLabel.setForeground(medalColor);
        itemPanel.add(rankLabel, BorderLayout.WEST);

        JLabel nicknameLabel = new JLabel(entry.getNickname());
        nicknameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        nicknameLabel.setForeground(isMe ? POINT_CYAN : Color.WHITE);
        itemPanel.add(nicknameLabel, BorderLayout.CENTER);

        JLabel pointsLabel = new JLabel(String.format("%,d P", entry.getTotalPoints()));
        pointsLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        pointsLabel.setForeground(POINT_PURPLE);
        pointsLabel.setBorder(new EmptyBorder(0, 0, 0, 30));
        itemPanel.add(pointsLabel, BorderLayout.EAST);

        return itemPanel;
    }

    public void updateMyRank(List<RankingEntry> rankingList) {
        if (manager == null) return;

        String myInfoHtml = manager.getMyRankInfo(currentUserId, rankingList);
        infoLabel.setText(myInfoHtml);

        if (rankingList != null) {
            for (RankingEntry e : rankingList) {
                if (e.getUserId().equals(currentUserId)) {
                    this.userCurrentPoints = e.getTotalPoints();
                    break;
                }
            }
        }
    }
}