package db.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList; 
import java.util.Map;
import java.util.List;
import java.util.Set;         
import java.util.HashSet;       
import java.text.SimpleDateFormat; 
import java.util.Date;        

import db.DAO.GuideDAO;
import db.DAO.UserDAO; 
import db.DAO.PointLogDAO; 
import db.RecycleDB; 

public class RecycleLogDAO {

    private static final String LOGS_TABLE = "POINT_LOGS"; 
    
    private final UserDAO userDAO;
    private final GuideDAO guideDAO;
    private final PointLogDAO pointLogDAO; 

    // ⭐ LogItem 클래스는 더 이상 사용하지 않으므로 제거합니다.

    public RecycleLogDAO() throws Exception {
        this.userDAO = new UserDAO(); 
        this.guideDAO = new GuideDAO();
        this.pointLogDAO = new PointLogDAO(); 
    }

    public static void initializeDatabase() {
        
    }
    
    /**
     * ⭐ RecyclePanel에서 사용하기 위해 추가된 메서드.
     * 오늘 분리수거 항목이 포함된 모든 로그에서 품목 이름만 추출하여 List<String> 형태로 반환합니다.
     * (이전에 오류가 발생했던 getTodayRecycleItems(userId)를 구현합니다.)
     */
    public List<String> getTodayRecycleItems(String userId) throws SQLException {
        List<String> itemNames = new ArrayList<>();
        
        String todayStart = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " 00:00:00";
        
        // DETAIL에 '분리수거:' 문자열이 포함된 로그를 조회
        String sql = "SELECT DETAIL FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND TYPE = '적립' AND DETAIL LIKE '분리수거:%' AND TIMESTAMP >= ?";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, todayStart);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String detail = rs.getString("DETAIL");
                    
                    if (detail.startsWith("분리수거: ")) {
                        String itemsStr = detail.substring("분리수거: ".length()).trim();
                        String[] itemEntries = itemsStr.split(", ");
                        
                        for (String entry : itemEntries) {
                            int endIndex = entry.indexOf(" (");
                            String itemName = entry.trim();
                            
                            // 포인트 정보 제거 (예: '종이 (15P)' -> '종이')
                            if (endIndex != -1) {
                                itemName = entry.substring(0, endIndex).trim();
                            }
                            
                            if (!itemName.equals("적립 항목 없음")) {
                                itemNames.add(itemName);
                            }
                        }
                    }
                }
            }
        }
        return itemNames;
    }


    // ⭐ 기존의 getTodayRecycleLogs는 위의 getTodayRecycleItems로 대체됩니다.
    // 기존 코드를 유지하고 싶다면 이 메서드를 삭제하고 RecyclePanel에서 getTodayRecycleItems를 사용하세요.

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
                    String detail = rs.getString("DETAIL");
                    if (detail.startsWith("분리수거: ")) {
                        String itemsStr = detail.substring("분리수거: ".length()).trim();
                        String[] itemEntries = itemsStr.split(", ");
                        
                        for (String entry : itemEntries) {
                            int endIndex = entry.indexOf(" (");
                            // 포인트가 기록된 항목만 (XXP) 문자열이 존재함
                            if (endIndex != -1) { 
                                earnedItems.add(entry.substring(0, endIndex).trim());
                            }
                        }
                    }
                }
            }
        }
        return earnedItems;
    }

    public int insertRecycleLogsAndEarn(String userId, List<String> itemsToSave, Map<String, Integer> itemPoints) throws SQLException {
        Connection conn = null;
        int totalPointsEarned = 0;
        
        // 오늘 이미 포인트를 적립받은 품목 리스트를 불러와서 중복 적립 방지
        Set<String> todayEarnedItems = getTodayEarnedItems(userId); 
        
        try {
            conn = RecycleDB.connect();
            conn.setAutoCommit(false);

            
            StringBuilder detailBuilder = new StringBuilder();
            boolean firstItem = true;
            
            for (String item : itemsToSave) {
                int itemPoint = itemPoints.getOrDefault(item, 0);
                
                if (todayEarnedItems.contains(item)) {
                    // 이미 적립된 항목이면 포인트는 0으로 처리 (로그에는 남기되 포인트는 안 줌)
                    itemPoint = 0; 
                } else {
                    totalPointsEarned += itemPoint; 
                    if (itemPoint > 0) {
                         todayEarnedItems.add(item); // 적립된 항목으로 추가
                    }
                }
                
                // 포인트가 0이더라도 (재적립 방지로 0이 되었을지라도), 모든 시도 항목을 로그에 포함
                int logPoint = itemPoints.getOrDefault(item, 0); // 실제 품목의 기본 포인트
                
                if (!firstItem) {
                    detailBuilder.append(", ");
                }
                detailBuilder.append(item).append(" (").append(logPoint).append("P)");
                firstItem = false;
            }
            
            String detailItems = detailBuilder.toString();
            // 실제로 적립된 항목과 관계없이, 오늘 시도한 모든 항목을 로그 상세에 기록합니다.
            String logDetail = "분리수거: " + (detailItems.length() > 0 ? detailItems : "적립 항목 없음");
            if (logDetail.length() > 255) {
                logDetail = logDetail.substring(0, 252) + "...";
            }

            if (totalPointsEarned > 0) {
                 userDAO.addPointsToUser(conn, userId, totalPointsEarned); 
            }
            
            // 포인트 획득 여부와 관계없이 시도 기록을 남김 (0P라도)
            pointLogDAO.insertPointLog(conn, userId, "적립", logDetail, totalPointsEarned);
            
            conn.commit(); 
            return totalPointsEarned;
            
        } catch (SQLException e) {
            System.err.println("로그 기록 및 포인트 적립 중 DB 오류: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); 
                } catch (SQLException rollbackEx) {
                    System.err.println("롤백 오류: " + rollbackEx.getMessage());
                }
            }
            throw e; 
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); 
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Connection 닫기 오류: " + closeEx.getMessage());
                }
            }
        }
    }
    
    public void insertQuizReward(String userId, String detail, int reward) throws SQLException {
        if (reward <= 0) {
            return; 
        }
        
        Connection conn = null;
        
        try {
            conn = RecycleDB.connect();
            conn.setAutoCommit(false); 

            
            userDAO.addPointsToUser(conn, userId, reward); 
            
            
            String logDetail = "퀴즈 보상: " + detail;
            if (logDetail.length() > 255) {
                logDetail = logDetail.substring(0, 252) + "..."; 
            }
            
            pointLogDAO.insertPointLog(conn, userId, "적립", logDetail, reward);
            
            conn.commit(); 
            
        } catch (SQLException e) {
            System.err.println("퀴즈 보상 기록 및 포인트 적립 중 DB 오류: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); 
                } catch (SQLException rollbackEx) {
                    System.err.println("롤백 오류: " + rollbackEx.getMessage());
                }
            }
            throw e; 
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); 
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Connection 닫기 오류: " + closeEx.getMessage());
                }
            }
        }
    }
    public boolean hasTakenQuizToday(String userId) throws SQLException {
        String todayStart = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " 00:00:00";
        
        String sql = "SELECT COUNT(*) FROM " + LOGS_TABLE +
                     " WHERE USER_ID = ? AND TYPE = '적립' AND DETAIL LIKE '퀴즈 보상:%' AND TIMESTAMP >= ?";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, todayStart);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

}