package recycle;

import db.DAO.RankingDAO; 
import db.DAO.RecycleLogDAO; 
import db.DAO.UserDAO; 
import db.DTO.UserDTO; 
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class RankingManager {
    
    public static class RankingEntry { 
        private final String userId;
        private final String nickname;
        private final int totalPoints;
        
        public RankingEntry(String userId, String nickname, int totalPoints) {
            this.userId = userId;
            this.nickname = nickname;
            this.totalPoints = totalPoints;
        }
        
        public String getUserId() { return userId; }
        public String getNickname() { return nickname; }
        public int getTotalPoints() { return totalPoints; }
    }

    private final RankingDAO rankingDAO; 
    private final UserDAO userDAO;

    public RankingManager() {
        try {
            this.rankingDAO = new RankingDAO(); 
            this.userDAO = new UserDAO(); 
        } catch (Exception e) {
             throw new RuntimeException("DAO 초기화 실패", e); 
        }
    }

    public List<RankingEntry> getSortedRankingList() throws SQLException {
        List<db.DTO.RankingDTO> dbRankingList = rankingDAO.getTopRankings();
        List<RankingEntry> rankingList = new ArrayList<>();
        
        if (dbRankingList != null) {
            for (db.DTO.RankingDTO dbDto : dbRankingList) {
                 rankingList.add(new RankingEntry(
                     dbDto.getUserId(), 
                     dbDto.getNickname(), 
                     dbDto.getTotalPoints() 
                 ));
            }
        }
        return rankingList;
    }
    
    
    public String getMyRankInfo(String userId, List<RankingEntry> rankingList) {
        int myRank = -1;
        int myPoints = 0;
        String myNickname = userId; 

        // 1. 현재 메모리에 있는 리스트에서 먼저 검색 (성능 최적화)
        if (rankingList != null) {
            for (int i = 0; i < rankingList.size(); i++) {
                RankingEntry entry = rankingList.get(i);
                if (entry.getUserId().equals(userId)) {
                    myRank = i + 1; 
                    myPoints = entry.getTotalPoints(); 
                    myNickname = entry.getNickname();
                    break;
                }
            }
        }
        
        // 2. 리스트에 없거나 정보가 부족한 경우 DB에서 전체 순위 및 정보 직접 조회
        if (myRank == -1) {
            try {
                UserDTO userDto = userDAO.getUserById(userId);
                if (userDto != null) {
                    myNickname = userDto.getNickname();
                    myPoints = userDto.getTotalPoints();
                }
            } catch (Exception e) {
                 System.err.println("❌ 실시간 조회 오류: " + e.getMessage());
            }
        }

        String rankDisplay = (myRank > 0) ? myRank + "위" : "순위권 밖";


        return String.format(
            "<html><div style='text-align: center; color: #E0E0E0;'>" +
            "닉네임: <b style='color: #00fff0;'>%s</b> | " +
            "누적: <b style='color: #00fff0;'>%,d P</b> | " +
            "순위: <b style='color: #00fff0;'>%s</b>" +
            "</div></html>", 
            myNickname, myPoints, rankDisplay);
    }
}