package db.DAO;

import java.sql.*;
import java.util.*;
import java.time.DayOfWeek;
import db.RecycleDB;
import db.DTO.*;
import org.springframework.stereotype.Repository; // 스프링 연동을 위해 추가

@Repository // 스프링 @Autowired가 이 클래스를 찾을 수 있게 합니다.
public class RecycleLogDAO {

    // 테이블 이름을 하나로 통일합니다 (대문자 POINT_LOGS)
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
            System.out.println("✅ DB 테이블 초기화 완료: " + LOGS_TABLE);
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
     * [3] 단일 분리수거 로그 저장
     */
    public void insertPointLog(String userId, String itemName, int point) throws SQLException {
        String sql = "INSERT INTO " + LOGS_TABLE + " (USER_ID, TYPE, DETAIL, POINTS) VALUES (?, ?, ?, ?)";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, "적립");
            pstmt.setString(3, RECYCLE_KEY_PREFIX + itemName);
            pstmt.setInt(4, point);
            pstmt.executeUpdate();
        }
    }

    /**
     * [4] 오늘 이미 분리수거했는지 확인 (중복 방지)
     */
    public boolean isAlreadyRecycledToday(String userId, String itemName) throws SQLException {
        // DATE(TIMESTAMP)를 사용하여 날짜만 비교
        String sql = "SELECT COUNT(*) FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND DETAIL = ? " +
                     " AND DATE(TIMESTAMP) = CURDATE()";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, RECYCLE_KEY_PREFIX + itemName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * [5] 오늘 분리수거 품목 리스트 조회 (화면 출력용)
     */
    public List<String> getTodayRecycleItems(String userId) throws SQLException {
        List<String> items = new ArrayList<>();
        // TIMESTAMP에 9시간을 더해 KST 기준 오늘 날짜로 조회
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
     * [6] 주간 활동 통계
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
     * [7] 다중 품목 일괄 저장 및 포인트 합산 (트랜잭션 처리)
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
                    userDAO.addPoints(userId, totalEarned);
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
 // RecycleLogDAO.java 내부 수정 제안
 // RecycleLogDAO.java 내의 해당 메서드를 아래와 같이 수정하세요.
    public void saveRecycleLogAndPoint(String userId, String itemName, int point) throws SQLException {
        
        // 💡 중요: 같은 커넥션을 사용하여 트랜잭션 유지
        try (Connection conn = RecycleDB.connect()) {
            conn.setAutoCommit(false); // 🚨 트랜잭션 시작 (자동 커밋 비활성화)

            try {
                // 1. 포인트 로그 삽입 (RecycleLogDAO의 테이블)
                String sqlLog = "INSERT INTO POINT_LOGS (USER_ID, TYPE, DETAIL, POINTS) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmtLog = conn.prepareStatement(sqlLog)) {
                    pstmtLog.setString(1, userId);
                    pstmtLog.setString(2, "적립");
                    pstmtLog.setString(3, "분리수거:" + itemName);
                    pstmtLog.setInt(4, point);
                    pstmtLog.executeUpdate();
                }

                // 2. USERS 테이블 포인트 업데이트 (직접 UPDATE 쿼리 실행)
                String sqlUpdate = "UPDATE USERS SET BALANCE_POINTS = BALANCE_POINTS + ?, TOTAL_POINTS = TOTAL_POINTS + ? WHERE USER_ID = ?";
                try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {
                    pstmtUpdate.setInt(1, point); // BALANCE_POINTS 증가
                    pstmtUpdate.setInt(2, point); // TOTAL_POINTS 증가
                    pstmtUpdate.setString(3, userId);
                    
                    int affectedRows = pstmtUpdate.executeUpdate();
                    
                    // 업데이트된 행이 없다면 사용자 ID가 잘못된 것임
                    if (affectedRows == 0) {
                        throw new SQLException("❌ 사용자 업데이트 실패: " + userId);
                    }
                }

                conn.commit(); // ✅ 최종 커밋: 로그와 포인트 업데이트가 모두 성공했을 때만 반영
                System.out.println("✅ 포인트 적립 및 로그 저장 완료.");

            } catch (SQLException e) {
                conn.rollback(); // ❌ 오류 발생 시 전체 작업 롤백 (안전성 확보)
                System.err.println("❌ 트랜잭션 롤백: " + e.getMessage());
                throw e; // 호출한 곳으로 예외 재던짐
            } finally {
                conn.setAutoCommit(true); // 커넥션 풀 반환 전 자동 커밋 설정 원복
            }
        }
    }
    /**
     * [8] 오늘 퀴즈 참여 여부 확인
     */
    public boolean hasTakenQuizToday(String userId) throws SQLException {
        if (userId == null) return false;
        String sql = "SELECT COUNT(*) FROM " + LOGS_TABLE + 
                     " WHERE USER_ID = ? AND DETAIL LIKE ? " + 
                     " AND DATE(DATE_ADD(TIMESTAMP, INTERVAL 9 HOUR)) = CURDATE()"; 
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
     * [9] 퀴즈 보상 지급
     */
    public void insertQuizReward(String userId, int score) throws SQLException {
        if (userId == null || score <= 0) return;
        try (Connection conn = RecycleDB.connect()) {
            conn.setAutoCommit(false);
            try {
                userDAO.addPoints( userId, score);
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
     * [10] 오답 노트 관리
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