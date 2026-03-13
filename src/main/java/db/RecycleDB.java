package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RecycleDB {

    // DB 접속 정보
    private static final String DB_URL ="";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    /**
     * DB 연결을 시도하고 Connection 객체를 반환합니다.
     */
    public static Connection connect() throws SQLException {
        try {
            // MySQL 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL 드라이버 로드 실패: " + e.getMessage());
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
    }

    /**
     * GuideDAO 등에서 호출하기 편하도록 만든 연결 메서드입니다.
     */
    public static Connection setup() {
        try {
            return connect();
        } catch (SQLException e) {
            System.err.println("❌ DB 연결 실패 (ID/PW 또는 URL 확인): " + e.getMessage());
            return null;
        }
    }
}
