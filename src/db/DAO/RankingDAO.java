package db.DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import db.RecycleDB;
import db.DTO.RankingDTO; 

public class RankingDAO {

    /**
     * 전체 사용자의 순위를 누적 포인트 순으로 가져옵니다.
     * 수정 사항: BALANCE_POINTS 대신 TOTAL_POINTS를 사용하여 랭킹을 산정합니다.
     */
    public List<RankingDTO> getTopRankings() throws SQLException {
        List<RankingDTO> rankings = new ArrayList<>();

        // ⭐ 핵심: TOTAL_POINTS(누적 포인트) 내림차순 정렬
        // 만약 누적 포인트가 같다면 USER_ID 오름차순으로 2차 정렬합니다.
        String sql = "SELECT USER_ID, NICKNAME, TOTAL_POINTS " + 
                     "FROM USERS " +
                     "ORDER BY TOTAL_POINTS DESC, USER_ID ASC"; 
        
        try (Connection conn = RecycleDB.connect(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String userId = rs.getString("USER_ID");
                String nickname = rs.getString("NICKNAME");
                // ⭐ RankingDTO 생성 시 누적 포인트를 전달합니다.
                int points = rs.getInt("TOTAL_POINTS"); 
                
                // RankingDTO 클래스에 (String, String, int) 생성자가 있는지 확인 필요
                RankingDTO dto = new RankingDTO(userId, nickname, points);
                
                rankings.add(dto);
            }
        } catch (SQLException e) {
            System.err.println("❌ DB에서 랭킹을 가져오는 중 오류 발생: " + e.getMessage());
            throw e;
        }
        
        return rankings;
    }
}