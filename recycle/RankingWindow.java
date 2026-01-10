package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import recycle.RankingManager.RankingEntry;


public class RankingWindow extends JPanel {

    private final String currentUserId;
    private recycle.RankingManager manager;
    private JPanel rankListPanel;
    private JLabel infoLabel;

    private static final Color BG_DARK = new Color(10, 10, 20);      
    private static final Color CARD_BG = new Color(25, 25, 45);      
    private static final Color HOVER_BG = new Color(35, 35, 70);     
    private static final Color POINT_PURPLE = new Color(140, 80, 255); 
    private static final Color POINT_CYAN = new Color(0, 240, 255);     
    private static final Color TEXT_SILVER = new Color(180, 180, 210);
    
    private static final Color COLOR_GOLD = new Color(255, 215, 0);
    private static final Color COLOR_SILVER = new Color(192, 192, 192);
    private static final Color COLOR_BRONZE = new Color(205, 127, 50);

    private static final int MAX_RANK_DISPLAY = 5;

    public RankingWindow(String userId) {
        this.currentUserId = (userId != null && !userId.isEmpty()) ? userId : "방문자";

        try {
            this.manager = new recycle.RankingManager();
        } catch (Exception e) {
            System.err.println("랭킹 관리자 연동 실패: " + e.getMessage());
        }

        setLayout(new BorderLayout(0, 25));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setOpaque(false);
        
        JLabel title = new JLabel("TOP 5 명예의 전당");
        title.setFont(new Font("Malgun Gothic", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        
        JLabel desc = new JLabel("실시간 분리수거 포인트 기여도 순위 리스트");
        desc.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));
        desc.setForeground(POINT_CYAN);
        
        header.add(title);
        header.add(desc);
        add(header, BorderLayout.NORTH);

        rankListPanel = new JPanel();
        rankListPanel.setLayout(new BoxLayout(rankListPanel, BoxLayout.Y_AXIS));
        rankListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(rankListPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); 
        add(scroll, BorderLayout.CENTER);

        infoLabel = new JLabel("", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 17));
        infoLabel.setForeground(Color.WHITE);
        
        JPanel footerCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, POINT_PURPLE, getWidth(), 0, new Color(100, 50, 200));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
            }
        };
        footerCard.setOpaque(false);
        footerCard.setPreferredSize(new Dimension(0, 75));
        footerCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        footerCard.add(infoLabel);
        
        footerCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { openMyPage(); }
            @Override
            public void mouseEntered(MouseEvent e) { infoLabel.setForeground(POINT_CYAN); }
            @Override
            public void mouseExited(MouseEvent e) { infoLabel.setForeground(Color.WHITE); }
        });
        
        add(footerCard, BorderLayout.SOUTH);

        loadRankingList();
    }

    private void openMyPage() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        if (ancestor instanceof Frame) {
            MyPageWindow myPage = new MyPageWindow((Frame) ancestor, currentUserId);
            myPage.setVisible(true);
            refreshRanking();
        }
    }

    public void refreshRanking() {
        loadRankingList();
    }

    public void loadRankingList() {
        if (manager == null) return;
        try {
            List<RankingEntry> fullList = manager.getSortedRankingList();
            if (fullList == null) fullList = new ArrayList<>();
            
            int limit = Math.min(fullList.size(), MAX_RANK_DISPLAY);
            updateRankListUI(fullList.subList(0, limit));
            updateMyRank(fullList); 
        } catch (SQLException e) {
            infoLabel.setText("랭킹 정보를 불러올 수 없습니다.");
        }
    }

    private void updateRankListUI(List<RankingEntry> rankingList) {
        rankListPanel.removeAll();
        if (rankingList.isEmpty()) {
            JLabel empty = new JLabel("현재 표시할 랭킹 데이터가 없습니다.", SwingConstants.CENTER);
            empty.setForeground(TEXT_SILVER);
            rankListPanel.add(empty);
        } else {
            for (int i = 0; i < rankingList.size(); i++) {
                rankListPanel.add(createRankCard(i + 1, rankingList.get(i)));
                rankListPanel.add(Box.createVerticalStrut(12));
            }
        }
        rankListPanel.revalidate();
        rankListPanel.repaint();
    }

    private JPanel createRankCard(int rank, RankingEntry entry) {
        boolean isMe = entry.getUserId().equals(currentUserId);
        
        JPanel card = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(isMe ? new Color(45, 40, 90) : CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                if (isMe) {
                    g2.setColor(POINT_CYAN);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setPreferredSize(new Dimension(0, 70));
        card.setBorder(new EmptyBorder(0, 25, 0, 30));

        JLabel rankLbl = new JLabel(rank + "위");
        rankLbl.setFont(new Font("Malgun Gothic", Font.BOLD, 22));
        
        if (rank == 1) rankLbl.setForeground(COLOR_GOLD);
        else if (rank == 2) rankLbl.setForeground(COLOR_SILVER);
        else if (rank == 3) rankLbl.setForeground(COLOR_BRONZE);
        else rankLbl.setForeground(TEXT_SILVER);
        
        rankLbl.setPreferredSize(new Dimension(80, 0));

        String displayName = entry.getNickname();
        if (isMe) displayName += " (나)";
        
        JLabel nameLbl = new JLabel(displayName);
        nameLbl.setFont(new Font("Malgun Gothic", isMe ? Font.BOLD : Font.PLAIN, 18));
        nameLbl.setForeground(isMe ? POINT_CYAN : Color.WHITE);

        JLabel ptsLbl = new JLabel(String.format("%,d P", entry.getTotalPoints()));
        ptsLbl.setFont(new Font("Consolas", Font.BOLD, 22));
        ptsLbl.setForeground(isMe ? POINT_CYAN : POINT_PURPLE);

        card.add(rankLbl, BorderLayout.WEST);
        card.add(nameLbl, BorderLayout.CENTER);
        card.add(ptsLbl, BorderLayout.EAST);

        return card;
    }

    public void updateMyRank(List<RankingEntry> rankingList) {
        if (manager == null || rankingList == null) return;
        String myInfoHtml = manager.getMyRankInfo(currentUserId, rankingList);
        
        infoLabel.setText(myInfoHtml);
    }
}