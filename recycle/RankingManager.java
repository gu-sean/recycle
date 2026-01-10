package recycle;

import db.DAO.RankingDAO; 
import db.DAO.RecycleLogDAO; 
import db.DAO.UserDAO; 
import db.DTO.UserDTO; 
import db.DTO.RankingDTO; 

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
    private final RecycleLogDAO logDAO; 
    private final UserDAO userDAO;

    public RankingManager() {
        try {
            this.rankingDAO = new RankingDAO(); 
            this.logDAO = new RecycleLogDAO(); 
            this.userDAO = new UserDAO(); 
        } catch (Exception e) {
             System.err.println("❌ DAO 초기화 실패: " + e.getMessage());
             throw new RuntimeException("DB 연결 또는 DAO 초기화 실패", e); 
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
        
        String rankStr = (myRank != -1) ? String.valueOf(myRank) : "순위권 밖";

        if (myRank == -1) {
            try {

                UserDTO userDto = userDAO.getUserById(userId); 
                
                if (userDto != null) {
                    myNickname = userDto.getNickname();
                    myPoints = userDto.getTotalPoints(); 
                    rankStr = "순위권 밖"; 
                } else {
                    myNickname = "알 수 없음";
                    rankStr = "정보 없음";
                }
            } catch (Exception e) {
                 System.err.println("❌ 실시간 정보 조회 오류: " + e.getMessage());
                 myNickname = "오류";
                 rankStr = "오류";
            }
        }

        return String.format(
            "<html><div style='text-align: center; color: white;'>" +
            "[내 정보] 닉네임: <b style='color: #00fff0;'>%s</b> | " +
            "누적 포인트: <b style='color: #00fff0;'>%,d P</b> | " +
            "현재 순위: <b style='color: #00fff0;'>%s위</b>" +
            "</div></html>", 
            myNickname, myPoints, rankStr);
    }
}