package db.DAO; 

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import db.RecycleDB; 
import db.DTO.UserDTO;
import db.DTO.RankingDTO;


public class UserDAO {
    
    private static final String USERS_TABLE = "USERS";

    public static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS " + USERS_TABLE + " (" +
                     "USER_ID VARCHAR(50) NOT NULL," +
                     "PASSWORD VARCHAR(255) NOT NULL," +
                     "NICKNAME VARCHAR(50) NOT NULL UNIQUE," + 
                     "BALANCE_POINTS INT DEFAULT 0," +
                     "TOTAL_POINTS INT DEFAULT 0," +
                     "ATTENDANCE_STREAK INT DEFAULT 0," + 
                     "IS_ADMIN TINYINT DEFAULT 0," +       
                     "PRIMARY KEY (USER_ID)" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[Database] USERS 테이블 준비 완료.");
        } catch (SQLException e) {
            System.err.println("[Database Error] USERS 테이블 초기화 실패: " + e.getMessage());
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

    public boolean isExists(String column, String value) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE " + column + " = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean registerUser(String id, String password, String nickname) throws SQLException {
        String sql = "INSERT INTO " + USERS_TABLE + 
                     " (USER_ID, PASSWORD, NICKNAME, IS_ADMIN) VALUES (?, ?, ?, ?)";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            pstmt.setString(3, nickname);
            pstmt.setBoolean(4, id.toLowerCase().contains("admin")); 
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
        String sql = "SELECT * FROM " + USERS_TABLE + " ORDER BY TOTAL_POINTS DESC";
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

    public int getUserRanking(String userID) throws SQLException {
        String sql = "SELECT rank_val FROM (" +
                     "  SELECT USER_ID, RANK() OVER (ORDER BY TOTAL_POINTS DESC) as rank_val " +
                     "  FROM " + USERS_TABLE +
                     ") t WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("rank_val");
            }
        }
        return 0;
    }

    public void addPointsToUser(String userID, int points) throws SQLException {
        try (Connection conn = RecycleDB.connect()) {
            addPointsToUser(conn, userID, points);
        }
    }

   
    public void addPointsToUser(Connection conn, String userID, int points) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + 
                     " SET BALANCE_POINTS = BALANCE_POINTS + ?, TOTAL_POINTS = TOTAL_POINTS + ? " +
                     " WHERE USER_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, points);
            pstmt.setInt(2, points);
            pstmt.setString(3, userID);
            pstmt.executeUpdate();
        }
    }

    public boolean subtractPoints(String userID, int amount) throws SQLException {
   
        String sql = "UPDATE " + USERS_TABLE + 
                     " SET BALANCE_POINTS = BALANCE_POINTS - ? " +
                     " WHERE USER_ID = ? AND BALANCE_POINTS >= ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, userID);
            pstmt.setInt(3, amount);
            return pstmt.executeUpdate() > 0;
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

    public boolean updatePassword(String userId, String newPassword) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + " SET PASSWORD = ? WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<RankingDTO> getTopRankings(int limit) throws SQLException {
        String sql = "SELECT USER_ID, NICKNAME, TOTAL_POINTS FROM " + USERS_TABLE + 
                     " ORDER BY TOTAL_POINTS DESC, USER_ID ASC LIMIT ?";
        List<RankingDTO> rankingList = new ArrayList<>();
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rankingList.add(new RankingDTO(
                        rs.getString("USER_ID"),
                        rs.getString("NICKNAME"),
                        rs.getInt("TOTAL_POINTS")
                    ));
                }
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
        UserDTO user = new UserDTO(
            rs.getString("USER_ID"),
            rs.getString("NICKNAME"),
            rs.getInt("BALANCE_POINTS"),
            rs.getInt("TOTAL_POINTS"),
            rs.getInt("ATTENDANCE_STREAK"),
            rs.getBoolean("IS_ADMIN")
        );
        user.setPassword(rs.getString("PASSWORD"));
        return user;
    }
}