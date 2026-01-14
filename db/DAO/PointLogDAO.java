package db.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import db.DTO.PointLogDTO;
import db.RecycleDB;


public class PointLogDAO {
    
    private static final String POINT_LOGS_TABLE = "POINT_LOGS";

    public PointLogDAO() {
    }

    /**
     * 포인트 로그 삽입 
     */
    public void insertPointLog(Connection conn, String userId, String type, String detail, int points) throws SQLException {
        String sql = "INSERT INTO " + POINT_LOGS_TABLE + 
                     " (USER_ID, TYPE, DETAIL, POINTS, TIMESTAMP) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, type);
            pstmt.setString(3, detail); 
            pstmt.setInt(4, points);
            // KST 시간 보장을 위해 명시적으로 현재 시간 전달
            pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            
            pstmt.executeUpdate();
        }
    }

    /**
     * 상품 구매 로그 삽입 
     */
    public void insertSpendLog(Connection conn, String userId, String productName, int spentPoints) throws SQLException {
        String sql = "INSERT INTO POINT_LOGS (USER_ID, TYPE, DETAIL, POINTS, TIMESTAMP) " +
                     "VALUES (?, '상품구매', ?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, productName + " 구매");
            pstmt.setInt(3, -spentPoints); 
            pstmt.executeUpdate();
        }
    }

    /**
     * 적립 로그 전용 (퀴즈, 분리수거 등)
     */
    public void insertEarnLog(Connection conn, String userId, String activity, int points) throws SQLException {
        insertPointLog(conn, userId, "적립", activity, points);
    }

    /**
     * 특정 사용자의 포인트 내역 조회 (최신순)
     */
    public List<PointLogDTO> getPointLogs(Connection conn, String userId) throws SQLException {
        List<PointLogDTO> list = new ArrayList<>();
        
    
        String sql = "SELECT LOG_ID, USER_ID, TYPE, DETAIL, POINTS, TIMESTAMP FROM " + POINT_LOGS_TABLE + 
                     " WHERE USER_ID = ? ORDER BY TIMESTAMP DESC, LOG_ID DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                 
                    PointLogDTO log = new PointLogDTO(
                        rs.getInt("LOG_ID"),
                        rs.getString("USER_ID"),
                        rs.getString("TYPE"),
                        rs.getString("DETAIL"),
                        rs.getInt("POINTS"),
                        rs.getTimestamp("TIMESTAMP").toLocalDateTime()
                    );
                    list.add(log);
                }
            }
        }
        return list;
    }
    
    /**
     * 테이블 초기화 SQL (앱 실행 시 최초 1회 호출)
     */
    public static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + POINT_LOGS_TABLE + " ("
                + "LOG_ID INT NOT NULL AUTO_INCREMENT, "
                + "USER_ID VARCHAR(50) NOT NULL, "
                + "TIMESTAMP TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "TYPE VARCHAR(20) NOT NULL, " 
                + "DETAIL VARCHAR(255), "
                + "POINTS INT NOT NULL, " 
                + "PRIMARY KEY (LOG_ID), "
                + "INDEX idx_user_id (USER_ID)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("[PointLogDAO] 테이블 초기화 오류: " + e.getMessage());
        }
    }
}