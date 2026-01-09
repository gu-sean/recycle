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
        private final int balancePoints; 
        
        public RankingEntry(String userId, String nickname, int balancePoints) {
            this.userId = userId;
            this.nickname = nickname;
            this.balancePoints = balancePoints;
        }
        
        public String getUserId() { return userId; }
        public String getNickname() { return nickname; }
        public int getBalancePoints() { return balancePoints; }
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
        
        for (db.DTO.RankingDTO dbDto : dbRankingList) {
             rankingList.add(new RankingEntry(
                 dbDto.getUserId(), 
                 dbDto.getNickname(), 
                 dbDto.getBalancePoints() 
             ));
        }
        
        return rankingList;
    }
    
  
    public String getMyRankInfo(String userId, List<RankingEntry> rankingList) {
        int myRank = -1;
        int myPoints = 0;
        String myNickname = userId; 

        for (int i = 0; i < rankingList.size(); i++) {
        	RankingEntry entry = rankingList.get(i);
            
            if (entry.getUserId().equals(userId)) {
                myRank = i + 1; 
                myPoints = entry.getBalancePoints(); 
                myNickname = entry.getNickname();
                break;
            }
        }
        
        String rankStr = (myRank != -1) ? String.valueOf(myRank) : "정보 없음";

        if (myRank == -1) {
            try {
                UserDTO userDto = userDAO.getUserById(userId); 
                
                if (userDto != null) {
                    myNickname = userDto.getNickname();
                    myPoints = userDto.getBalancePoints();
                    rankStr = "목록 밖"; 
                } else {
                    myNickname = "알 수 없음";
                    rankStr = "정보 없음";
                }
            } catch (SQLException e) {
                 System.err.println("❌ UserDAO 조회 중 오류 발생: " + e.getMessage());
                 myNickname = "오류";
                 rankStr = "오류";
            }
        }

        return String.format(
            "<html><p align='center'>[내 정보] 닉네임: <strong>%s</strong> (ID: %s) | 현재 포인트: <strong>%d점</strong> | 순위: <strong>%s위</strong></p></html>", 
            myNickname, userId, myPoints, rankStr);
    }
}