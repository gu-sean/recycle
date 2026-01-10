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

    
    public static void initializeDatabase() {
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(getUsersCreateTableSql());
        } catch (SQLException e) {
            System.err.println("USERS 테이블 초기화 오류: " + e.getMessage());
            throw new RuntimeException("USERS 테이블 초기화 실패", e);
        }
    }

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

    public boolean registerUser(UserDTO user) throws SQLException {
        String sql = "INSERT INTO " + USERS_TABLE + 
                     " (USER_ID, PASSWORD, NICKNAME, BALANCE_POINTS, TOTAL_POINTS, ATTENDANCE_STREAK, IS_ADMIN) " +
                     " VALUES (?, ?, ?, ?, ?, 0, ?)";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword() != null ? user.getPassword() : "1234");
            pstmt.setString(3, user.getNickname());
            pstmt.setInt(4, user.getBalancePoints());
            pstmt.setInt(5, user.getTotalPoints());
            pstmt.setBoolean(6, user.isAdmin());
            return pstmt.executeUpdate() > 0;
        }
    }

    public UserDTO getUserById(String userID) {
        String sql = "SELECT * FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapUserDTO(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; 
    }

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

    public void updateUserPoint(UserDTO user) throws SQLException {
        try (Connection conn = RecycleDB.connect()) {
            updateUserPoint(conn, user);
        }
    }

    public void updateUserPoint(Connection conn, UserDTO user) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + " SET BALANCE_POINTS = ?, TOTAL_POINTS = ? WHERE USER_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getBalancePoints());
            pstmt.setInt(2, user.getTotalPoints());
            pstmt.setString(3, user.getUserId());
            pstmt.executeUpdate();
        }
    }

    public boolean updateUserByAdmin(UserDTO user) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + 
                     " SET NICKNAME = ?, BALANCE_POINTS = ?, TOTAL_POINTS = ?, IS_ADMIN = ? " + 
                     " WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getNickname());
            pstmt.setInt(2, user.getBalancePoints());
            pstmt.setInt(3, user.getTotalPoints()); 
            pstmt.setBoolean(4, user.isAdmin());
            pstmt.setString(5, user.getUserId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public int getTotalUserCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + USERS_TABLE;
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getTotalSystemPoints() throws SQLException {
        String sql = "SELECT SUM(BALANCE_POINTS) FROM " + USERS_TABLE;
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

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

    public boolean deleteUser(String userId) throws SQLException {
        String sql = "DELETE FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

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