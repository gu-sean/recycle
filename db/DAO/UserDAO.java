package db.DAO; 

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import db.RecycleDB; 
import db.DTO.UserDTO;
import db.DTO.RankingDTO;


public class UserDAO {
    
    private static final String USERS_TABLE = "USERS";

    /**
     * [1] 테이블 초기화 및 스키마 최적화
     */
    public static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS " + USERS_TABLE + " (" +
                     "  USER_ID VARCHAR(50) NOT NULL," +
                     "  PASSWORD VARCHAR(255) NOT NULL," + 
                     "  NICKNAME VARCHAR(50) NOT NULL UNIQUE," + 
                     "  BALANCE_POINTS INT DEFAULT 0 CHECK (BALANCE_POINTS >= 0)," +
                     "  TOTAL_POINTS INT DEFAULT 0," +
                     "  ATTENDANCE_STREAK INT DEFAULT 0," + 
                     "  IS_ADMIN TINYINT DEFAULT 0," +       
                     "  PRIMARY KEY (USER_ID)," +
                     "  INDEX idx_total_points (TOTAL_POINTS DESC)" + 
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ [Database] USERS 테이블 스키마 초기화 및 인덱싱 완료.");
        } catch (SQLException e) {
            handleException("테이블 초기화", e);
        }
    }

    /**
     * [2] 로그인 
     */
    public UserDTO loginUser(String id, String password) throws SQLException {
        String sql = "SELECT * FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPw = rs.getString("PASSWORD");
                    if (storedHashedPw.equals(hashPassword(password))) {
                        return mapUserDTO(rs);
                    }
                }
            }
        }
        return null; 
    }
    
    private String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("❌ 보안 알고리즘 오류: SHA-256을 찾을 수 없습니다.");
        }
    }
    /**
     * [3] 중복 체크 
     */
    public boolean isExists(String column, String value) throws SQLException {
 
        String upperCol = column.toUpperCase();
        if (!upperCol.equals("USER_ID") && !upperCol.equals("NICKNAME")) {
            throw new IllegalArgumentException("❌ 보안 경고: 허용되지 않은 컬럼 접근입니다.");
        }

        String sql = "SELECT 1 FROM " + USERS_TABLE + " WHERE " + upperCol + " = ? LIMIT 1";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * [4] 회원가입 
     */
    public boolean registerUser(String id, String password, String nickname) throws SQLException {
        String sql = "INSERT INTO " + USERS_TABLE + 
                     " (USER_ID, PASSWORD, NICKNAME, IS_ADMIN) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, hashPassword(password)); 
            pstmt.setString(3, nickname);
            pstmt.setBoolean(4, id.toLowerCase().contains("admin")); 
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) throw new SQLException("이미 등록된 아이디나 닉네임입니다.");
            throw e;
        }
    }

    /**
     * [5] 사용자 조회
     */
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

    /**
     * [6] 관리자용: 전체 사용자 목록 
     */
    public List<UserDTO> getAllUsersPaged(int limit, int offset) throws SQLException {
        String sql = "SELECT * FROM " + USERS_TABLE + " ORDER BY TOTAL_POINTS DESC LIMIT ? OFFSET ?";
        List<UserDTO> userList = new ArrayList<>();
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userList.add(mapUserDTO(rs));
                }
            }
        }
        return userList;
    }

    /**
     * [7] 포인트 지급 
     */
    public void addPoints(Connection conn, String userID, int points) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + 
                     " SET BALANCE_POINTS = BALANCE_POINTS + ?, TOTAL_POINTS = TOTAL_POINTS + ? " +
                     " WHERE USER_ID = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, points);
            pstmt.setInt(2, points);
            pstmt.setString(3, userID);
            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("❌ 포인트 지급 실패: 존재하지 않는 사용자 ID(" + userID + ")입니다.");
            }
        }
    }

    /**
     * [8] 포인트 차감 
     */
    public boolean subtractPoints(Connection conn, String userID, int amount) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + 
                     " SET BALANCE_POINTS = BALANCE_POINTS - ? " +
                     " WHERE USER_ID = ? AND BALANCE_POINTS >= ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, userID);
            pstmt.setInt(3, amount);
            
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
    
                System.err.println("[Points] 차감 실패 - ID: " + userID + ", 잔액 부족 혹은 유저 없음");
                return false;
            }
            return true;
        }
    }

    /**
     * [9] 실시간 순위 조회 
     */
    public int getUserRanking(String userID) throws SQLException {
        String sql = "SELECT rank_val FROM (" +
                     "  SELECT USER_ID, RANK() OVER (ORDER BY TOTAL_POINTS DESC) as rank_val " +
                     "  FROM " + USERS_TABLE +
                     ") t WHERE USER_ID = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userID);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("rank_val") : 0;
            }
        }
    }

    /**
     * [10] 상위 랭킹 리스트 조회
     */
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

    /**
     * [11] 출석  갱신 
     */
    public void updateAttendanceStreak(String userId, boolean reset) throws SQLException {
        String sql = reset ? 
            "UPDATE " + USERS_TABLE + " SET ATTENDANCE_STREAK = 1 WHERE USER_ID = ?" :
            "UPDATE " + USERS_TABLE + " SET ATTENDANCE_STREAK = ATTENDANCE_STREAK + 1 WHERE USER_ID = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * [12] 비밀번호 변경
     */
    public boolean updatePassword(String userId, String newPassword) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + " SET PASSWORD = ? WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setString(1, hashPassword(newPassword)); 
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * [13] 회원 탈퇴
     */
    public boolean deleteUser(String userId) throws SQLException {
        String sql = "DELETE FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * [Helper] DTO 매핑 유틸리티
     */
    private UserDTO mapUserDTO(ResultSet rs) throws SQLException {
        UserDTO user = new UserDTO(
            rs.getString("USER_ID"),
            rs.getString("NICKNAME"),
            rs.getInt("BALANCE_POINTS"),
            rs.getInt("TOTAL_POINTS"),
            rs.getInt("ATTENDANCE_STREAK"),
            rs.getBoolean("IS_ADMIN")
        );

        return user;
    }
    /**
     * 관리자 전용 회원 정보 수정
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
     * [Helper] 공통 예외 처리 로거
     */
    private static void handleException(String task, SQLException e) {
        System.err.println(String.format("❌ [UserDAO Error] %s 실패: %s (코드: %d)", 
                           task, e.getMessage(), e.getErrorCode()));
    }
}