package db.DAO; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

import db.RecycleDB; 
import db.DTO.UserDTO;
import db.DTO.RankingDTO;

public class UserDAO {
    
    private static final String USERS_TABLE = "USERS";

    /**
     * USERS 테이블 생성 SQL
     */
    public static String getUsersCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + USERS_TABLE + " (" +
               "USER_ID VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '사용자 ID'," +
               "PASSWORD VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '비밀번호'," +
               "NICKNAME VARCHAR(50) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE COMMENT '사용자 닉네임'," + 
               "BALANCE_POINTS INT DEFAULT 0 COMMENT '현재 잔여 포인트'," +
               "TOTAL_POINTS INT DEFAULT 0 COMMENT '총 누적 포인트'," +
               "ATTENDANCE_STREAK INT DEFAULT 0 COMMENT '연속 출석 횟수'," + 
               "IS_ADMIN BOOLEAN DEFAULT FALSE COMMENT '관리자 여부'," +       
               "PRIMARY KEY (USER_ID)" +
               ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

    /**
     * DB 초기화
     */
    public static void initializeDatabase() {
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(getUsersCreateTableSql());
        } catch (SQLException e) {
            System.err.println("USERS 테이블 초기화 오류: " + e.getMessage());
            throw new RuntimeException("USERS 테이블 초기화 실패", e);
        }
    }

    // [1] 로그인
    public UserDTO loginUser(String id, String password) throws SQLException {
        String sql = "SELECT * FROM " + USERS_TABLE + " WHERE USER_ID = ? AND PASSWORD = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapUserDTO(rs);
            }
        }
        return null; 
    }

    // [2] 아이디 중복 체크 
    public boolean isIdDuplicate(String id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // [3] 닉네임 중복 체크 
    public boolean isNicknameDuplicate(String nickname) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE NICKNAME = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nickname);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // [4] 회원가입
    public boolean registerUser(String id, String password, String nickname) throws SQLException {
        String sql = "INSERT INTO " + USERS_TABLE + 
                     " (USER_ID, PASSWORD, NICKNAME, BALANCE_POINTS, TOTAL_POINTS, ATTENDANCE_STREAK, IS_ADMIN) " +
                     " VALUES (?, ?, ?, 0, 0, 0, ?)";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            pstmt.setString(3, nickname);
            pstmt.setBoolean(4, "admin".equalsIgnoreCase(id));
            return pstmt.executeUpdate() > 0;
        }
    }

    // [5] ID로 회원 정보 조회 
    public UserDTO getUserById(String userID) throws SQLException {
        String sql = "SELECT * FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapUserDTO(rs);
            }
        }
        return null; 
    }

    // [6] 관리자용: 전체 사용자 목록 조회
    public List<UserDTO> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM " + USERS_TABLE + " ORDER BY USER_ID ASC";
        List<UserDTO> userList = new ArrayList<>();
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                userList.add(mapUserDTO(rs));
            }
        }
        return userList;
    }

    // [7] 포인트 업데이트 (일반 호출용)
    public void updateUserPoint(UserDTO user) throws SQLException {
        try (Connection conn = RecycleDB.connect()) {
            updateUserPoint(conn, user);
        }
    }

    // [7-1] 포인트 업데이트 (트랜잭션용)
    public void updateUserPoint(Connection conn, UserDTO user) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + " SET BALANCE_POINTS = ?, TOTAL_POINTS = ? WHERE USER_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getBalancePoints());
            pstmt.setInt(2, user.getTotalPoints());
            pstmt.setString(3, user.getUserId());
            pstmt.executeUpdate();
        }
    }

    /**
     * [8] 관리자용: 사용자 정보 통합 수정
     */
    public boolean updateUserByAdmin(UserDTO user) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + 
                     " SET NICKNAME = ?, BALANCE_POINTS = ?, IS_ADMIN = ? " + 
                     " WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getNickname());
            pstmt.setInt(2, user.getBalancePoints());
            pstmt.setBoolean(3, user.isAdmin());
            pstmt.setString(4, user.getUserId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * [9] 관리자 대시보드용: 총 회원 수 조회
     */
    public int getTotalUserCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + USERS_TABLE;
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * [10] 관리자 대시보드용: 시스템 내 유통 중인 총 포인트 합계 조회
     */
    public int getTotalSystemPoints() throws SQLException {
        String sql = "SELECT SUM(BALANCE_POINTS) FROM " + USERS_TABLE;
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // [11] 포인트 추가 (트랜잭션용)
    public void addPointsToUser(Connection conn, String userID, int points) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + 
                     " SET BALANCE_POINTS = BALANCE_POINTS + ?, TOTAL_POINTS = TOTAL_POINTS + ? WHERE USER_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, points);
            pstmt.setInt(2, points);
            pstmt.setString(3, userID);
            pstmt.executeUpdate();
        }
    }

    // [12] 전체 랭킹 조회 
    public List<RankingDTO> getAllUserRankings() throws SQLException {
        String sql = "SELECT USER_ID, NICKNAME, TOTAL_POINTS FROM " + USERS_TABLE + 
                     " ORDER BY TOTAL_POINTS DESC, USER_ID ASC";
        List<RankingDTO> rankingList = new ArrayList<>();
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                rankingList.add(new RankingDTO(
                    rs.getString("USER_ID"),
                    rs.getString("NICKNAME"),
                    rs.getInt("TOTAL_POINTS")
                ));
            }
        }
        return rankingList;
    }

    // [13] 회원 탈퇴
    public boolean deleteUser(String userId) throws SQLException {
        String sql = "DELETE FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * ResultSet 데이터를 UserDTO로 변환하는 헬퍼 메서드
     */
    private UserDTO mapUserDTO(ResultSet rs) throws SQLException {
        return new UserDTO(
            rs.getString("USER_ID"),
            rs.getString("NICKNAME"),
            rs.getInt("BALANCE_POINTS"),
            rs.getInt("TOTAL_POINTS"),
            rs.getInt("ATTENDANCE_STREAK"),
            rs.getBoolean("IS_ADMIN")
        );
    }
}