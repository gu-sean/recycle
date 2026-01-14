package db.DAO;

import java.sql.*;
import java.util.*;
import java.time.DayOfWeek;
import db.RecycleDB;
import recycle.QuizPanel.WrongData;


public class RecycleLogDAO {

    private static final String LOGS_TABLE = "POINT_LOGS";
    private static final String WRONG_NOTE_TABLE = "QUIZ_WRONG_NOTE";
    
    public static final String QUIZ_REWARD_KEY = "데일리 퀴즈 보상";
    public static final String RECYCLE_KEY_PREFIX = "분리수거:";

    private final UserDAO userDAO;
    private final PointLogDAO pointLogDAO;

    public RecycleLogDAO() {
        this.userDAO = new UserDAO();
        this.pointLogDAO = new PointLogDAO();
    }

    /**
     * [1] 테이블 초기화 및 인덱스 설정
     */
    public static void initializeDatabase() {
        String sqlLogs = "CREATE TABLE IF NOT EXISTS " + LOGS_TABLE + " (" +
                "LOG_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "USER_ID VARCHAR(50) NOT NULL, " +
                "TYPE VARCHAR(20) NOT NULL, " +
                "DETAIL VARCHAR(255) NOT NULL, " +
                "POINTS INT DEFAULT 0, " +
                "TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_user_activity (USER_ID, DETAIL(20), TIMESTAMP), " + 
                "FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        String sqlWrongNote = "CREATE TABLE IF NOT EXISTS " + WRONG_NOTE_TABLE + " (" +
                "LOG_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "USER_ID VARCHAR(50) NOT NULL, " +
                "QUESTION_TEXT TEXT NOT NULL, " +
                "SELECTED_ANSWER VARCHAR(255), " +
                "CORRECT_ANSWER VARCHAR(255), " +
                "CREATED_AT DATE NOT NULL, " +
                "FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlLogs);
            stmt.execute(sqlWrongNote);
        } catch (SQLException e) {
            System.err.println("❌ [Database Error] 초기화 실패: " + e.getMessage());
        }
    }

    /**
     * [2] 유저의 총 분리수거 횟수 조회
     */
    public int getLogCountByUserId(String userId) throws SQLException {
        if (userId == null) return 0;
        String sql = "SELECT COUNT(*) FROM " + LOGS_TABLE + " WHERE USER_ID = ? AND DETAIL LIKE ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, RECYCLE_KEY_PREFIX + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * [3] 주간 활동 통계 (KST 기준)
     */
    public Map<DayOfWeek, Integer> getWeeklyStats(String userId) throws SQLException {
        Map<DayOfWeek, Integer> stats = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) stats.put(day, 0);

        String sql = "SELECT DAYOFWEEK(DATE_ADD(TIMESTAMP, INTERVAL 9 HOUR)) as dow, COUNT(*) as cnt " +
                     "FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND DETAIL LIKE ? " +
                     " AND TIMESTAMP >= DATE_SUB(NOW(), INTERVAL 7 DAY) " + 
                     " GROUP BY dow";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, RECYCLE_KEY_PREFIX + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int mysqlDow = rs.getInt("dow");
                    int javaDowValue = (mysqlDow == 1) ? 7 : mysqlDow - 1; 
                    stats.put(DayOfWeek.of(javaDowValue), rs.getInt("cnt"));
                }
            }
        }
        return stats;
    }

    /**
     * [4] 오늘 분리수거 품목 리스트 조회
     */
    public List<String> getTodayRecycleItems(String userId) throws SQLException {
        List<String> items = new ArrayList<>();
        String sql = "SELECT DETAIL FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND DETAIL LIKE ? " +
                     " AND DATE(DATE_ADD(TIMESTAMP, INTERVAL 9 HOUR)) = CURDATE()";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, RECYCLE_KEY_PREFIX + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String detail = rs.getString("DETAIL");
                    if (detail != null && detail.contains(":")) {
                        items.add(detail.split(":", 2)[1].trim());
                    }
                }
            }
        }
        return items;
    }

    /**
     * [5] 분리수거 수행 및 포인트 적립 
     */
    public int insertRecycleLogsAndEarn(String userId, List<String> items, Map<String, Integer> itemPointsMap) throws SQLException {
        if (userId == null || items == null || items.isEmpty()) return 0;

        int totalEarned = 0;
        try (Connection conn = RecycleDB.connect()) {
            conn.setAutoCommit(false); 
            try {
                for (String itemName : items) {
                    int point = itemPointsMap.getOrDefault(itemName, 0);
                    totalEarned += point;
                    String detail = RECYCLE_KEY_PREFIX + itemName;
                    
      
                    pointLogDAO.insertPointLog(conn, userId, "적립", detail, point);
                }
        
                if (totalEarned > 0) {
                    userDAO.addPoints(conn, userId, totalEarned);
                }
                
                conn.commit(); 
                return totalEarned;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * [6] 오늘 퀴즈 참여 여부 확인
     */
    public boolean hasTakenQuizToday(String userId) throws SQLException {
        if (userId == null) return false;

        String sql = "SELECT COUNT(*) FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND DETAIL LIKE ? " + 
                     " AND TIMESTAMP >= CURRENT_DATE - INTERVAL 9 HOUR"; 
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, QUIZ_REWARD_KEY + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * [7] 퀴즈 보상 지급 
     */
    public void insertQuizReward(String userId, int score) throws SQLException {
        if (userId == null || score <= 0) return;
        
        try (Connection conn = RecycleDB.connect()) {
            conn.setAutoCommit(false);
            try {
      
                userDAO.addPoints(conn, userId, score);
                
        
                String logDetail = String.format("%s 완료 (%dP)", QUIZ_REWARD_KEY, score);
                pointLogDAO.insertPointLog(conn, userId, "적립", logDetail, score);
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * [8] 오답 노트 관리
     */
    public void insertWrongAnswer(String userId, String question, String selected, String correct) throws SQLException {
        if (userId == null) return;
        String sql = "INSERT INTO " + WRONG_NOTE_TABLE + 
                     " (USER_ID, QUESTION_TEXT, SELECTED_ANSWER, CORRECT_ANSWER, CREATED_AT) VALUES (?, ?, ?, ?, CURDATE())";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, Objects.requireNonNullElse(question, "내용 없음"));
            pstmt.setString(3, Objects.requireNonNullElse(selected, "미선택"));
            pstmt.setString(4, Objects.requireNonNullElse(correct, "정보 없음"));
            pstmt.executeUpdate();
        }
    }

    public List<WrongData> getWrongAnswersToday(String userId) throws SQLException {
        List<WrongData> list = new ArrayList<>();
        String sql = "SELECT QUESTION_TEXT, SELECTED_ANSWER, CORRECT_ANSWER FROM " + WRONG_NOTE_TABLE +
                     " WHERE USER_ID = ? AND CREATED_AT = CURDATE() ORDER BY LOG_ID ASC";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new WrongData(
                        rs.getString("QUESTION_TEXT"), 
                        rs.getString("SELECTED_ANSWER"), 
                        rs.getString("CORRECT_ANSWER")
                    ));
                }
            }
        }
        return list;
    }
}