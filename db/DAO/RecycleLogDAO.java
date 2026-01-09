package db.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList; 
import java.util.Map;
import java.util.List;
import java.util.Set;         
import java.util.HashSet;       
import java.text.SimpleDateFormat; 
import java.util.Date;        

import db.RecycleDB; 


public class RecycleLogDAO {

    private static final String LOGS_TABLE = "POINT_LOGS"; 
    
    private final UserDAO userDAO;
    private final PointLogDAO pointLogDAO; 

    public RecycleLogDAO() {
        this.userDAO = new UserDAO(); 
        this.pointLogDAO = new PointLogDAO(); 
    }

  
    public static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS " + LOGS_TABLE + " (" +
                     "LOG_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                     "USER_ID VARCHAR(50) NOT NULL, " +
                     "TYPE VARCHAR(20) NOT NULL COMMENT '적립/사용', " +
                     "DETAIL VARCHAR(255) NOT NULL, " +
                     "POINTS INT DEFAULT 0, " +
                     "TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID) ON DELETE CASCADE" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("POINT_LOGS 테이블 초기화 완료.");
        } catch (SQLException e) {
            System.err.println("POINT_LOGS 테이블 생성 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("데이터베이스 초기화 실패", e);
        }
    }

 
    public List<String> getTodayRecycleItems(String userId) throws SQLException {
        String todayStart = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " 00:00:00";
        return getRecycleItemsByPeriod(userId, todayStart);
    }

    
    public List<String> getAllRecycleItems(String userId) throws SQLException {
        return getRecycleItemsByPeriod(userId, "1970-01-01 00:00:00");
    }

   
    private List<String> getRecycleItemsByPeriod(String userId, String startDate) throws SQLException {
        List<String> itemNames = new ArrayList<>();
        String sql = "SELECT DETAIL FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND TYPE = '적립' AND DETAIL LIKE '분리수거:%' AND TIMESTAMP >= ?";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, startDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String detail = rs.getString("DETAIL");
                    parseAndAddItemNames(detail, itemNames);
                }
            }
        }
        return itemNames;
    }

    private void parseAndAddItemNames(String detail, List<String> list) {
        if (detail == null || !detail.startsWith("분리수거: ")) return;

        try {
            String itemsPart = detail.substring("분리수거: ".length()).trim();
            if (itemsPart.equals("적립 항목 없음")) return;

            String[] entries = itemsPart.split(", ");
            for (String entry : entries) {
                int endIndex = entry.lastIndexOf(" ("); 
                String name = (endIndex != -1) ? entry.substring(0, endIndex).trim() : entry.trim();
                if (!name.isEmpty()) list.add(name);
            }
        } catch (Exception e) {
            System.err.println("로그 파싱 오류: " + detail);
        }
    }

   
    public Set<String> getTodayEarnedItems(String userId) throws SQLException {
        Set<String> earnedItems = new HashSet<>();
        String todayStart = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " 00:00:00";
        
        String sql = "SELECT DETAIL FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND TYPE = '적립' AND DETAIL LIKE '분리수거:%' AND TIMESTAMP >= ?";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, todayStart);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    parseEarnedItems(rs.getString("DETAIL"), earnedItems);
                }
            }
        }
        return earnedItems;
    }

    private void parseEarnedItems(String detail, Set<String> set) {
        if (detail == null || !detail.startsWith("분리수거: ")) return;
        
        String itemsPart = detail.substring("분리수거: ".length()).trim();
        String[] entries = itemsPart.split(", ");
        
        for (String entry : entries) {
            if (entry.contains("P)") && !entry.contains("(0P)")) { 
                int endIndex = entry.lastIndexOf(" (");
                if (endIndex != -1) {
                    set.add(entry.substring(0, endIndex).trim());
                }
            }
        }
    }

    
    public int insertRecycleLogsAndEarn(String userId, List<String> itemsToSave, Map<String, Integer> itemPoints) throws SQLException {
        int totalPointsEarned = 0;
        Set<String> todayAlreadyEarned = getTodayEarnedItems(userId); 
        
        try (Connection conn = RecycleDB.connect()) {
            conn.setAutoCommit(false);
            try {
                StringBuilder detailBuilder = new StringBuilder("분리수거: ");
                
                for (int i = 0; i < itemsToSave.size(); i++) {
                    String item = itemsToSave.get(i);
                    int point = itemPoints.getOrDefault(item, 0);
                    
                    if (todayAlreadyEarned.contains(item)) {
                        point = 0; 
                    } else {
                        totalPointsEarned += point;
                        if (point > 0) todayAlreadyEarned.add(item);
                    }
                    
                    detailBuilder.append(item).append(" (").append(point).append("P)");
                    if (i < itemsToSave.size() - 1) detailBuilder.append(", ");
                }
                
                String logDetail = detailBuilder.toString();
                if (itemsToSave.isEmpty()) logDetail = "분리수거: 적립 항목 없음";
                if (logDetail.length() > 255) logDetail = logDetail.substring(0, 252) + "...";

                if (totalPointsEarned > 0) {
                     userDAO.addPointsToUser(conn, userId, totalPointsEarned); 
                }
                
                pointLogDAO.insertPointLog(conn, userId, "적립", logDetail, totalPointsEarned);
                
                conn.commit(); 
                return totalPointsEarned;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e; 
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    
   
    public void insertQuizReward(String userId, String detail, int reward) throws SQLException {
        if (reward <= 0) return; 
        
        try (Connection conn = RecycleDB.connect()) {
            conn.setAutoCommit(false); 
            try {
                userDAO.addPointsToUser(conn, userId, reward); 
                
                String logDetail = "퀴즈 보상: " + detail;
                if (logDetail.length() > 255) logDetail = logDetail.substring(0, 252) + "..."; 
                
                pointLogDAO.insertPointLog(conn, userId, "적립", logDetail, reward);
                
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