package db.DAO; 

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Repository;

import db.RecycleDB; 
import db.DTO.UserDTO;

@Repository
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
                     "  REG_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP," + 
                     "  LAST_LOGIN TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                     "  PRIMARY KEY (USER_ID)," +
                     "  INDEX idx_total_points (TOTAL_POINTS DESC)" + 
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ [Database] USERS 테이블 초기화 완료.");
        } catch (SQLException e) {
            handleException("테이블 초기화", e);
        }
    }

    /**
     * [2] 로그인 및 비밀번호 검증
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
                        updateLastLogin(id); 
                        return mapUserDTO(rs);
                    }
                }
            }
        }
        return null; 
    }

    /** 정보 수정 전 비밀번호 확인용 */
    public boolean checkPassword(String userId, String password) {
        String sql = "SELECT PASSWORD FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("PASSWORD").equals(hashPassword(password));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * [3] 닉네임 중복 체크
     */
    public boolean isNicknameAvailable(String nickname) throws SQLException {
        if (nickname == null || nickname.trim().isEmpty()) return false;
        
        String sql = "SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE NICKNAME = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nickname.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // 0이면 사용 가능(true)
                }
            }
        }
        return false;
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
        }
    }

    /**
     * [5] 사용자 정보 수정 (사용자용)
     */
    public boolean updateUser(String userId, String nickname, String password) {
        boolean hasPassword = (password != null && !password.trim().isEmpty());
        String sql = hasPassword 
            ? "UPDATE " + USERS_TABLE + " SET NICKNAME = ?, PASSWORD = ? WHERE USER_ID = ?"
            : "UPDATE " + USERS_TABLE + " SET NICKNAME = ? WHERE USER_ID = ?";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (hasPassword) {
                pstmt.setString(1, nickname);
                pstmt.setString(2, hashPassword(password));
                pstmt.setString(3, userId);
            } else {
                pstmt.setString(1, nickname);
                pstmt.setString(2, userId);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("사용자 정보 수정", e);
            return false;
        }
    }
    
    /**
     * 유저의 포인트(잔액 및 누적)를 적립합니다.
     * @param conn 서비스 레이어에서 관리하는 트랜잭션용 커넥션
     */
 // UserDAO.java
    public void addPoints(String userId, int totalEarned) throws SQLException {                
        String sql = "UPDATE USERS SET " +
                     "BALANCE_POINTS = BALANCE_POINTS + ?, " +
                     "TOTAL_POINTS = TOTAL_POINTS + ? " +
                     "WHERE USER_ID = ?";

        // 💡 내부에서 새로운 커넥션을 맺고 자동으로 닫도록 수정
        try (Connection conn = RecycleDB.connect();                
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, totalEarned); 
            pstmt.setInt(2, totalEarned); 
            pstmt.setString(3, userId);   
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("❌ 사용자 업데이트 실패: " + userId);
            }
        }
    }
    
    private void checkCurrentPoints(Connection conn, String userId) {
        String sql = "SELECT BALANCE_POINTS, TOTAL_POINTS FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("DEBUG >>> [Points] 현재 DB 상태 - ID: " + userId + 
                                       ", Balance: " + rs.getInt("BALANCE_POINTS") + 
                                       ", Total: " + rs.getInt("TOTAL_POINTS"));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ 디버깅 쿼리 실패");
        }
    }
    
    /**
     * [6] 환경 기여도 조회
     */
    public int getEnvironmentContribution(String userId) {
        String sql = "SELECT COUNT(*) FROM point_logs WHERE user_id = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ 환경 기여도 조회 오류: " + e.getMessage());
        }
        return 0;
    }

    /**
     * [포인트 내역 조회]
     */
    public List<Map<String, Object>> getRecentPointLogs(String userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT TIMESTAMP, DETAIL, POINTS FROM point_logs WHERE user_id = ? ORDER BY TIMESTAMP DESC LIMIT 5";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    Timestamp ts = rs.getTimestamp("TIMESTAMP");
                    row.put("date", ts != null ? ts.toString().substring(0, 10) : "-");
                    row.put("description", rs.getString("DETAIL"));
                    row.put("points", rs.getInt("POINTS")); 
                    
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * [기타 유틸리티 메서드]
     */
    private void updateLastLogin(String userId) {
        String sql = "UPDATE " + USERS_TABLE + " SET LAST_LOGIN = CURRENT_TIMESTAMP WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
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
            throw new RuntimeException("SHA-256 알고리즘 없음");
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
        Timestamp regDate = rs.getTimestamp("REG_DATE");
        Timestamp lastLogin = rs.getTimestamp("LAST_LOGIN");
        if (regDate != null) user.setRegDate(regDate.toString().substring(0, 16));
        if (lastLogin != null) user.setLastLogin(lastLogin.toString().substring(0, 16));
        return user;
    }

    public boolean deleteUser(String userId) throws SQLException {
        String sql = "DELETE FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    private static void handleException(String task, SQLException e) {
        System.err.println(String.format("❌ [UserDAO Error] %s 실패: %s (코드: %d)", 
                           task, e.getMessage(), e.getErrorCode()));
    }

    /**
     * [사용자 조회]
     */
    public UserDTO getUserById(String userId) {
        String sql = "SELECT * FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        
        try (Connection conn = RecycleDB.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUserDTO(rs);
                }
            }
        } catch (SQLException e) {
            handleException("사용자 상세 조회(ID: " + userId + ")", e);
        }
        
        return null;
    }
    
    /**
     * [관리자 전용] 특정 회원의 정보를 강제로 수정합니다.
     */
    public boolean updateUserByAdmin(UserDTO user) {
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
        } catch (SQLException e) {
            handleException("관리자용 회원 정보 수정(ID: " + user.getUserId() + ")", e);
            return false;
        }
    }
    
    /**
     * [관리자 전용] 전체 사용자 목록을 페이징하여 조회합니다.
     */
    public List<UserDTO> getAllUsersPaged(int limit, int offset) {
        List<UserDTO> userList = new ArrayList<>();
        String sql = "SELECT * FROM " + USERS_TABLE + " ORDER BY REG_DATE DESC LIMIT ? OFFSET ?";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userList.add(mapUserDTO(rs));
                }
            }
        } catch (SQLException e) {
            handleException("관리자용 전체 유저 조회", e);
        }
        return userList;
    }
}