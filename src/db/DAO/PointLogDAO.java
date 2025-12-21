package db.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import db.DTO.PointLogDTO;
import db.RecycleDB;

public class PointLogDAO {
    
    private static final String POINT_LOGS_TABLE = "POINT_LOGS";

    public PointLogDAO() {
    }

    /**
     * 포인트 로그 삽입 (공통 메서드)
     */
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

    /**
     * 차감 로그 전용 (상품 구매 등)
     */
    public void insertSpendLog(Connection conn, String userId, String productName, int points) throws SQLException {
        // 포인트가 양수로 들어오더라도 DB에는 음수로 저장되도록 처리
        int spendPoints = (points > 0) ? -points : points;
        insertPointLog(conn, userId, "SPEND", "상품 구매: " + productName, spendPoints);
    }

    /**
     * 적립 로그 전용 (퀴즈, 분리수거 등)
     */
    public void insertEarnLog(Connection conn, String userId, String activity, int points) throws SQLException {
        insertPointLog(conn, userId, "EARN", activity, points);
    }

    /**
     * [수정] MyPageWindow에서 호출하는 메서드 이름으로 통일
     * 특정 사용자의 포인트 내역을 최신순으로 가져옵니다.
     */
    public List<PointLogDTO> getPointLogs(Connection conn, String userId) throws SQLException {
        List<PointLogDTO> list = new ArrayList<>();
        
        // LOG_ID를 포함하여 모든 컬럼을 명확하게 조회
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
    
    /**
     * 테이블 초기화 SQL (앱 실행 시 최초 1회 호출 권장)
     */
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
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(createTableSQL)) {
            pstmt.execute();
        } catch (SQLException e) {
            System.err.println("테이블 초기화 중 오류: " + e.getMessage());
        }
    }
}