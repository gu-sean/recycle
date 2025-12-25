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

/**
 * 분리수거 활동 로그 기록, 포인트 적립 및 퀴즈 오답 노트를 관리하는 DAO
 */
public class RecycleLogDAO {

    private static final String LOGS_TABLE = "POINT_LOGS"; 
    private static final String WRONG_NOTE_TABLE = "QUIZ_WRONG_NOTE"; 
    
    private final UserDAO userDAO;
    private final PointLogDAO pointLogDAO; 

    public RecycleLogDAO() {
        this.userDAO = new UserDAO(); 
        this.pointLogDAO = new PointLogDAO(); 
    }

    /**
     * DB 초기화: 테이블 생성 (연결 안정성 강화)
     */
    public static void initializeDatabase() {
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            
            // 1. 포인트 로그 테이블 (TIMESTAMP 보정)
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

            // 2. 오답 노트 테이블 추가 (재실행 시 오답 유지용)
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

    /**
     * 오늘 이미 등록된 분리수거 품목 리스트 조회 (중복 적립 방지용)
     */
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

    /**
     * 분리수거 로그 저장 및 포인트 일괄 지급 (트랜잭션 처리)
     */
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
            conn.setAutoCommit(false); // 트랜잭션 시작
            try {
                // 1. 사용자 포인트 업데이트
                userDAO.addPointsToUser(conn, userId, totalPointsToEarn);
                
                // 2. 포인트 로그 상세 정보 생성
                String logDetail = "분리수거: " + String.join(", ", validNewItems);
                if (logDetail.length() > 255) logDetail = logDetail.substring(0, 252) + "...";
                
                // 3. 로그 인서트
                pointLogDAO.insertPointLog(conn, userId, "적립", logDetail, totalPointsToEarn);
                
                conn.commit(); // 모든 작업 성공 시 커밋
                return totalPointsToEarn;
            } catch (SQLException e) { 
                conn.rollback(); // 하나라도 실패 시 되돌림
                throw e; 
            } finally { 
                conn.setAutoCommit(true); 
            }
        }
    }

    /**
     * 개별 오답 데이터를 DB에 저장 (오늘 날짜 기준)
     */
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

    /**
     * 오늘 틀린 오답 목록을 DB에서 불러와 WrongData 리스트로 변환
     */
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

    /**
     * 퀴즈 참여 보상 기록 (트랜잭션 처리)
     */
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

    /**
     * 오늘 퀴즈 참여 여부 확인
     */
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