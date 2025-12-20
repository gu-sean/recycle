package db.DAO; 

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
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
               "PASSWORD VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '비밀번호 (해시 처리 권장)'," +
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


    // 1. 로그인
    public UserDTO loginUser(String id, String password) throws SQLException {
        String sql = "SELECT USER_ID, NICKNAME, BALANCE_POINTS, TOTAL_POINTS, ATTENDANCE_STREAK, IS_ADMIN FROM " + USERS_TABLE + 
                     " WHERE USER_ID = ? AND PASSWORD = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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
        }
        return null; 
    }


    // 2. 아이디 중복 체크 
    public boolean isIdDuplicate(String id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }


    // 3. 닉네임 중복 체크 
    public boolean isNicknameDuplicate(String nickname) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE NICKNAME = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nickname);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

   
    // 4. 회원가입 
    public boolean registerUser(String id, String password, String nickname) throws SQLException {
        String sql = "INSERT INTO " + USERS_TABLE + " (USER_ID, PASSWORD, NICKNAME, BALANCE_POINTS, TOTAL_POINTS, ATTENDANCE_STREAK, IS_ADMIN) VALUES (?, ?, ?, 0, 0, 0, FALSE)";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            pstmt.setString(2, password);
            pstmt.setString(3, nickname);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { 
                System.err.println("회원가입 실패: 아이디 또는 닉네임 중복");
            }
            throw e; 
        }
    }
    
    // 5. ID로 회원 정보 조회 
    public UserDTO getUserById(String userID) throws SQLException {
        String sql = "SELECT USER_ID, NICKNAME, BALANCE_POINTS, TOTAL_POINTS, ATTENDANCE_STREAK, IS_ADMIN FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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
        }
        return null; 
    }
    
    public UserDTO getUserFullData(String userID) throws SQLException {
        return getUserById(userID);
    }
    

    // 6. 회원 잔여 포인트 조회 
    public int getUserPoints(String userID) throws SQLException {
        String sql = "SELECT BALANCE_POINTS FROM " + USERS_TABLE + " WHERE USER_ID = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("BALANCE_POINTS");
                }
            }
        }
        return 0; 
    }

    // 7. 포인트 추가 
    public void addPointsToUser(String userID, int points) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + " SET BALANCE_POINTS = BALANCE_POINTS + ?, TOTAL_POINTS = TOTAL_POINTS + ? WHERE USER_ID = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, points);
            pstmt.setInt(2, points);
            pstmt.setString(3, userID);
            
            pstmt.executeUpdate();
        }
    }
    
    public void addPointsToUser(Connection conn, String userId, int points) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + " SET BALANCE_POINTS = BALANCE_POINTS + ?, TOTAL_POINTS = TOTAL_POINTS + ? WHERE USER_ID = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, points);
            pstmt.setInt(2, points);
            pstmt.setString(3, userId);
            
            pstmt.executeUpdate();
        }
    }


    // 8. 잔여 포인트 업데이트
 
    public void updateUserPoint(UserDTO user) throws SQLException {
        String sql = "UPDATE " + USERS_TABLE + " SET BALANCE_POINTS = ? WHERE USER_ID = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, user.getBalancePoints());
            pstmt.setString(2, user.getUserId());
            
            pstmt.executeUpdate();
            System.out.println("✅ 사용자 포인트(잔액) 업데이트 완료: " + user.getUserId());

        } catch (SQLException e) {
            System.err.println("❌ 사용자 포인트 업데이트 실패: " + e.getMessage());
            throw e;
        }
    }


    // 9. 퀴즈 결과 및 포인트 업데이트 
    public void updateQuizResult(UserDTO user) throws SQLException {
         String sql = "UPDATE " + USERS_TABLE + " SET TOTAL_POINTS = ?, BALANCE_POINTS = ?, ATTENDANCE_STREAK = ? WHERE USER_ID = ?";
         
         try (Connection conn = RecycleDB.connect();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
             pstmt.setInt(1, user.getTotalPoints());
             pstmt.setInt(2, user.getBalancePoints());
             pstmt.setInt(3, user.getAttendanceStreak());
             pstmt.setString(4, user.getUserId());
             
             pstmt.executeUpdate();
             System.out.println("✅ 퀴즈 결과 및 출석 정보 저장 완료: " + user.getUserId());

         } catch (SQLException e) {
             System.err.println("❌ 퀴즈 결과 저장 실패: " + e.getMessage());
             throw e;
         }
    }

  
    // 10. 전체 랭킹 조회 
    public List<RankingDTO> getAllUserRankings() throws SQLException {
 
        String sql = "SELECT USER_ID, NICKNAME, BALANCE_POINTS FROM " + USERS_TABLE + 
                     " ORDER BY BALANCE_POINTS DESC, USER_ID ASC";
        
        List<RankingDTO> rankingList = new ArrayList<>();
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                rankingList.add(new RankingDTO(
                    rs.getString("USER_ID"),
                    rs.getString("NICKNAME"),
                    rs.getInt("BALANCE_POINTS")
                ));
            }
        }
        return rankingList;
    }

 
    // 11. 상위 랭킹 5개 조회 
    public List<UserDTO> getTop5Rankings() throws SQLException {
        List<UserDTO> rankList = new ArrayList<>();
        
        // 기존 getRankings의 SQL: TOTAL_POINTS 기준 상위 5개
        String sql = "SELECT USER_ID, NICKNAME, TOTAL_POINTS, BALANCE_POINTS, ATTENDANCE_STREAK, IS_ADMIN FROM " + USERS_TABLE + 
                     " ORDER BY TOTAL_POINTS DESC LIMIT 5";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {

                rankList.add(new UserDTO(
                    rs.getString("USER_ID"),
                    rs.getString("NICKNAME"),
                    rs.getInt("BALANCE_POINTS"),
                    rs.getInt("TOTAL_POINTS"),
                    rs.getInt("ATTENDANCE_STREAK"),
                    rs.getBoolean("IS_ADMIN")
                ));
            }
        }
        return rankList;
    }
    
 
    // 12. 회원 탈퇴
    public void deleteUser(String userId) throws SQLException {
        String sql = "DELETE FROM " + USERS_TABLE + " WHERE USER_ID = ?";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);

            int result = pstmt.executeUpdate();
            if (result > 0) System.out.println("✅ 회원 탈퇴 완료: " + userId);

        } catch (SQLException e) {
            System.err.println("❌ 회원 탈퇴 실패: " + e.getMessage());
            throw e;
        }
    }
}