package db.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import db.DTO.PointLogDTO; 

public class PointLogDAO {
    private static final String POINT_LOGS_TABLE = "POINT_LOGS";
    public PointLogDAO() {
    }
    public void insertPointLog(Connection conn, String userId, String type, String detail, int points) throws SQLException {
        String sql = "INSERT INTO " + POINT_LOGS_TABLE + 
                     " (USER_ID, TIMESTAMP, TYPE, DETAIL, POINT) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(3, type);
            pstmt.setString(4, detail); 
            pstmt.setInt(5, points);
            
            pstmt.executeUpdate();
        }
    }

    public void insertSpendLog(Connection conn, String userId, String productName, int points) throws SQLException {
        int spendPoints = (points > 0) ? -points : points;
        insertPointLog(conn, userId, "SPEND", "상품 구매: " + productName, spendPoints);
    }

    public void insertEarnLog(Connection conn, String userId, String activity, int points) throws SQLException {
        insertPointLog(conn, userId, "EARN", activity, points);
    }

    public List<PointLogDTO> getPointLogsByUserId(Connection conn, String userId) throws SQLException {
        List<PointLogDTO> list = new ArrayList<>();

        String sql = "SELECT LOG_ID, USER_ID, TYPE, DETAIL, POINT, TIMESTAMP FROM " + POINT_LOGS_TABLE + 
                     " WHERE USER_ID = ? ORDER BY TIMESTAMP DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PointLogDTO log = new PointLogDTO(
                        rs.getInt("LOG_ID"),
                        rs.getString("USER_ID"),
                        rs.getString("TYPE"),
                        rs.getString("DETAIL"),
                        rs.getInt("POINT"),
                        rs.getTimestamp("TIMESTAMP").toLocalDateTime()
                    );
                    list.add(log);
                }
            }
        }
        return list;
    }
    
    public static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + POINT_LOGS_TABLE + " ("
                + "`LOG_ID` INT NOT NULL AUTO_INCREMENT,"
                + "`USER_ID` VARCHAR(50) NOT NULL,"
                + "`TIMESTAMP` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "`TYPE` VARCHAR(10) NOT NULL,"
                + "`DETAIL` VARCHAR(255),"
                + "`POINT` INT NOT NULL,"
                + "PRIMARY KEY (`LOG_ID`),"
                + "KEY `USER_ID_idx` (`USER_ID`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }
}