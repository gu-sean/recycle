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

   
    public List<RankingDTO> getTopRankings() throws SQLException {
        List<RankingDTO> rankings = new ArrayList<>();

        String sql = "SELECT USER_ID, NICKNAME, TOTAL_POINTS " + 
                     "FROM USERS " +
                     "ORDER BY TOTAL_POINTS DESC, USER_ID ASC"; 
        
        try (Connection conn = RecycleDB.connect(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String userId = rs.getString("USER_ID");
                String nickname = rs.getString("NICKNAME");
           
                int points = rs.getInt("TOTAL_POINTS"); 
                
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