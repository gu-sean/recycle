package db.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList; 
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.text.SimpleDateFormat; 
import java.util.Date;        

import db.RecycleDB; 
import recycle.QuizPanel.WrongData; 


public class RecycleLogDAO {

    private static final String LOGS_TABLE = "POINT_LOGS"; 
    private static final String WRONG_NOTE_TABLE = "QUIZ_WRONG_NOTE"; 
    
    private final UserDAO userDAO;
    private final PointLogDAO pointLogDAO; 

    public RecycleLogDAO() {
        this.userDAO = new UserDAO(); 
        this.pointLogDAO = new PointLogDAO(); 
    }

   
    public static void initializeDatabase() {
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            
            String sqlLogs = "CREATE TABLE IF NOT EXISTS " + LOGS_TABLE + " (" +
                         "LOG_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                         "USER_ID VARCHAR(50) NOT NULL, " +
                         "TYPE VARCHAR(20) NOT NULL, " +
                         "DETAIL VARCHAR(255) NOT NULL, " +
                         "POINTS INT DEFAULT 0, " +
                         "TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID) ON DELETE CASCADE" +
                         ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(sqlLogs);

            String sqlWrongNote = "CREATE TABLE IF NOT EXISTS " + WRONG_NOTE_TABLE + " (" +
                                "NOTE_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                                "USER_ID VARCHAR(50) NOT NULL, " +
                                "QUESTION_TEXT TEXT NOT NULL, " +
                                "SELECTED_ANSWER VARCHAR(255), " +
                                "CORRECT_ANSWER VARCHAR(255), " +
                                "CREATED_AT DATE NOT NULL, " + 
                                "FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID) ON DELETE CASCADE" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            stmt.execute(sqlWrongNote);

            System.out.println("퀴즈 및 포인트 관련 테이블 초기화 완료.");
        } catch (SQLException e) {
            System.err.println("데이터베이스 초기화 중 오류 발생: " + e.getMessage());
        }
    }

 
    public List<String> getTodayRecycleItems(String userId) throws SQLException {
        String todayStart = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " 00:00:00";
        Set<String> itemSet = new HashSet<>();
        String sql = "SELECT DETAIL FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND TYPE = '적립' AND DETAIL LIKE '분리수거:%' AND TIMESTAMP >= ?";
                     
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, todayStart);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    extractNamesFromDetail(rs.getString("DETAIL"), itemSet);
                }
            }
        }
        return new ArrayList<>(itemSet);
    }

    private void extractNamesFromDetail(String detail, Set<String> itemSet) {
        if (detail == null || !detail.startsWith("분리수거: ")) return;
        try {
            String content = detail.substring("분리수거: ".length()).trim();
            if (content.isEmpty()) return;
            String[] parts = content.split(", ");
            for (String part : parts) {
                int braceIndex = part.lastIndexOf(" (");
                String name = (braceIndex != -1) ? part.substring(0, braceIndex).trim() : part.trim();
                if (!name.isEmpty()) itemSet.add(name);
            }
        } catch (Exception e) { System.err.println("파싱 오류: " + e.getMessage()); }
    }

   
    public int insertRecycleLogsAndEarn(String userId, List<String> itemsToSave, Map<String, Integer> itemPoints) throws SQLException {
        List<String> alreadySaved = getTodayRecycleItems(userId);
        int totalPointsToEarn = 0;
        List<String> validNewItems = new ArrayList<>();

        for (String item : itemsToSave) {
            if (alreadySaved.contains(item)) continue;
            int point = itemPoints.getOrDefault(item, 0);
            totalPointsToEarn += point;
            validNewItems.add(item + " (" + point + "P)");
        }

        if (validNewItems.isEmpty()) return 0;

        try (Connection conn = RecycleDB.connect()) {
            conn.setAutoCommit(false); 
            try {
          
                userDAO.addPointsToUser(conn, userId, totalPointsToEarn);
                
                String logDetail = "분리수거: " + String.join(", ", validNewItems);
                if (logDetail.length() > 255) logDetail = logDetail.substring(0, 252) + "...";
                
                pointLogDAO.insertPointLog(conn, userId, "적립", logDetail, totalPointsToEarn);
                
                conn.commit(); 
                return totalPointsToEarn;
            } catch (SQLException e) { 
                conn.rollback(); 
                throw e; 
            } finally { 
                conn.setAutoCommit(true); 
            }
        }
    }

    public void insertWrongAnswer(String userId, String question, String selected, String correct) throws SQLException {
        String sql = "INSERT INTO " + WRONG_NOTE_TABLE + " (USER_ID, QUESTION_TEXT, SELECTED_ANSWER, CORRECT_ANSWER, CREATED_AT) VALUES (?, ?, ?, ?, CURDATE())";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, question);
            pstmt.setString(3, selected);
            pstmt.setString(4, correct);
            pstmt.executeUpdate();
        }
    }

 
    public List<WrongData> getWrongAnswersToday(String userId) throws SQLException {
        List<WrongData> list = new ArrayList<>();
        String sql = "SELECT QUESTION_TEXT, SELECTED_ANSWER, CORRECT_ANSWER FROM " + WRONG_NOTE_TABLE + 
                     " WHERE USER_ID = ? AND CREATED_AT = CURDATE()";
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

   
    public void insertQuizReward(String userId, String detail, int score) throws SQLException {
        try (Connection conn = RecycleDB.connect()) {
            conn.setAutoCommit(false);
            try {
                userDAO.addPointsToUser(conn, userId, score);
                pointLogDAO.insertPointLog(conn, userId, "적립", "퀴즈 보상: " + detail, score);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

   
    public boolean hasTakenQuizToday(String userId) throws SQLException {
        String todayStart = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " 00:00:00";
        String sql = "SELECT COUNT(*) FROM " + LOGS_TABLE +
                     " WHERE USER_ID = ? AND (DETAIL LIKE '퀴즈 보상:%' OR DETAIL LIKE '퀴즈 실패%') AND TIMESTAMP >= ?";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, todayStart);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}