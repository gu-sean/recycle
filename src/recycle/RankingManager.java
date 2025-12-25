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
    
    /**
     * 랭킹 표시를 위한 내부 클래스 (UI 전달용)
     */
    public static class RankingEntry { 
        private final String userId;
        private final String nickname;
        private final int totalPoints; // ⭐ 순위 기준은 '잔액'이 아닌 '누적 포인트'
        
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

    /**
     * DB에서 누적 포인트 순으로 정렬된 랭킹 목록을 가져옵니다.
     */
    public List<RankingEntry> getSortedRankingList() throws SQLException {
        // RankingDAO에서 누적 포인트(total_points) 내림차순으로 가져온 데이터를 받음
        List<db.DTO.RankingDTO> dbRankingList = rankingDAO.getTopRankings();
        
        List<RankingEntry> rankingList = new ArrayList<>();
        
        if (dbRankingList != null) {
            for (db.DTO.RankingDTO dbDto : dbRankingList) {
                 // DTO에서 TotalPoints를 가져와 RankingEntry로 변환
                 rankingList.add(new RankingEntry(
                     dbDto.getUserId(), 
                     dbDto.getNickname(), 
                     dbDto.getTotalPoints() 
                 ));
            }
        }
        
        return rankingList;
    }
    
    /**
     * 특정 사용자의 랭킹 정보를 HTML 형식의 문자열로 반환합니다.
     * 하단 정보 패널에 표시될 때 사용됩니다.
     */
    public String getMyRankInfo(String userId, List<RankingEntry> rankingList) {
        int myRank = -1;
        int myPoints = 0;
        String myNickname = userId; 

        // 1. 현재 불러온 리스트에서 유저의 순위 탐색
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

        // 2. 리스트에 없는 경우(신규 유저 등), UserDAO를 통해 DB에서 직접 최신 정보 조회
        if (myRank == -1) {
            try {
                // 수정된 UserDAO를 사용하여 실시간 포인트(관리자 수정분) 확인
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

        // ⭐ UI 디자인: 네온 테마와 일치하도록 HTML 컬러 적용 및 천 단위 콤마(,) 추가
        return String.format(
            "<html><div style='text-align: center; color: white;'>" +
            "[내 정보] 닉네임: <b style='color: #00fff0;'>%s</b> | " +
            "누적 포인트: <b style='color: #00fff0;'>%,d P</b> | " +
            "현재 순위: <b style='color: #00fff0;'>%s위</b>" +
            "</div></html>", 
            myNickname, myPoints, rankStr);
    }
}