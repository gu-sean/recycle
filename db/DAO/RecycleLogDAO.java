package db.DAO;

import java.sql.*;
import java.util.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import db.RecycleDB;
import db.DTO.RecycleLogDTO;
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

  
    public static void initializeDatabase() {
        String sqlLogs = "CREATE TABLE IF NOT EXISTS " + LOGS_TABLE + " (" +
                "LOG_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                "USER_ID VARCHAR(50) NOT NULL, " +
                "TYPE VARCHAR(20) NOT NULL, " +
                "DETAIL VARCHAR(255) NOT NULL, " +
                "POINTS INT DEFAULT 0, " +
                "TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
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
            e.printStackTrace();
        }
    }

  
    public Map<DayOfWeek, Integer> getWeeklyStats(String userId) throws SQLException {
        Map<DayOfWeek, Integer> stats = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) stats.put(day, 0);

       
        String sql = "SELECT DATE(DATE_ADD(TIMESTAMP, INTERVAL 9 HOUR)) as log_date, COUNT(*) as cnt " +
                     "FROM POINT_LOGS " + 
                     "WHERE USER_ID = ? AND DETAIL LIKE '분리수거:%' " +
                     "AND TIMESTAMP >= DATE_SUB(NOW(), INTERVAL 7 DAY) " + 
                     "GROUP BY log_date";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("log_date");
                    if (sqlDate != null) {
                 
                        LocalDate localDate = sqlDate.toLocalDate();
                        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
                        stats.put(dayOfWeek, rs.getInt("cnt"));
                    }
                }
            }
        }
        return stats;
    }

    
    public int getLogCountByUserId(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + LOGS_TABLE + " WHERE USER_ID = ? AND DETAIL LIKE '" + RECYCLE_KEY_PREFIX + "%'";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

 
    public List<String> getTodayRecycleItems(String userId) throws SQLException {
        List<String> items = new ArrayList<>();
        String sql = "SELECT DETAIL FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND DETAIL LIKE ? AND DATE(TIMESTAMP) = CURDATE()";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, RECYCLE_KEY_PREFIX + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String detail = rs.getString("DETAIL");
                    if (detail.contains(":")) {
                        items.add(detail.split(":")[1].trim());
                    }
                }
            }
        }
        return items;
    }

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
                userDAO.addPointsToUser(conn, userId, totalEarned);
                conn.commit();
                return totalEarned;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

   
    public boolean hasTakenQuizToday(String userId) throws SQLException {
        if (userId == null) return false;
        String sql = "SELECT COUNT(*) FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND DETAIL LIKE ? AND DATE(TIMESTAMP) = CURDATE()";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, QUIZ_REWARD_KEY + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void insertQuizReward(String userId, int score) throws SQLException {
        if (userId == null) return;
        try (Connection conn = RecycleDB.connect()) {
            conn.setAutoCommit(false);
            try {
                userDAO.addPointsToUser(conn, userId, score);
                String logDetail = QUIZ_REWARD_KEY + " 완료 (" + score + "P)";
                pointLogDAO.insertPointLog(conn, userId, "적립", logDetail, score);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void insertWrongAnswer(String userId, String question, String selected, String correct) throws SQLException {
        if (userId == null) return;
        String sql = "INSERT INTO " + WRONG_NOTE_TABLE + 
                     " (USER_ID, QUESTION_TEXT, SELECTED_ANSWER, CORRECT_ANSWER, CREATED_AT) VALUES (?, ?, ?, ?, CURDATE())";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, (question != null) ? question : "내용 없음");
            pstmt.setString(3, (selected != null) ? selected : "미선택");
            pstmt.setString(4, (correct != null) ? correct : "정답 정보 없음");
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
                    list.add(new WrongData(rs.getString("QUESTION_TEXT"), rs.getString("SELECTED_ANSWER"), rs.getString("CORRECT_ANSWER")));
                }
            }
        }
        return list;
    }
}