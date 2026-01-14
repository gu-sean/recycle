package db.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import db.RecycleDB;
import db.DTO.RankingDTO; 


public class RankingDAO {

    /**
     * 상위 랭킹 사용자를 가져옵니다.
     */
    public List<RankingDTO> getTopRankings(int limit) {
        List<RankingDTO> rankings = new ArrayList<>();

        String sql = "SELECT USER_ID, NICKNAME, TOTAL_POINTS " + 
                     "FROM USERS " +
                     "ORDER BY TOTAL_POINTS DESC, USER_ID ASC " +
                     "LIMIT ?"; 
        
        try (Connection conn = RecycleDB.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                int currentRank = 1; 
                while (rs.next()) {
                    String userId = rs.getString("USER_ID");
                    String nickname = rs.getString("NICKNAME");
                    int points = rs.getInt("TOTAL_POINTS"); 
   
                    rankings.add(new RankingDTO(
                        currentRank++, 
                        userId, 
                        nickname, 
                        points, 
                        null 
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ RankingDAO 오류: " + e.getMessage());
        }
        
        return rankings;
    }

    /**
     * 특정 사용자의 현재 순위를 조회합니다.
     */
    public int getUserRank(String userId) {
        String sql = "SELECT COUNT(*) + 1 FROM USERS WHERE TOTAL_POINTS > " +
                     "(SELECT TOTAL_POINTS FROM USERS WHERE USER_ID = ?)";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ 순위 조회 오류: " + e.getMessage());
        }
        return 0; 
    }

    public List<RankingDTO> getTopRankings() {
        return getTopRankings(50);
    }
}