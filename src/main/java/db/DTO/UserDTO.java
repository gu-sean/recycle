package db.DTO;

import java.io.Serializable;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * 사용자 정보 및 등급 시스템을 관리하는 Data Transfer Object (DTO)
 */
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 2L; 

    private String userId;
    private transient String password;      
    private String nickname;
    private int balancePoints;    // 현재 보유 포인트 (소비 가능)
    private int totalPoints;      // 누적 획득 포인트 (등급 산정 기준)
    private int attendanceStreak; // 연속 출석 일수
    private boolean isAdmin;      // 관리자 권한 여부
    
    // [추가] 가입일 및 최근 접속일 필드
    private String regDate;       
    private String lastLogin;

    /**
     * [Enum] UserGrade 시스템
     * 등급별 기준치와 UI 테마 컬러를 캡슐화합니다.
     */
    public enum UserGrade {
        SPROUT("새싹 요정 🌱", 0, new Color(189, 195, 199)),
        FRIEND("지구 친구 🌍", 200, new Color(241, 196, 15)),
        GUARDIAN("환경 수호자 🌿", 500, new Color(46, 204, 113)),
        MASTER("에코 마스터 💎", 1000, new Color(0, 255, 240));

        private final String name;
        private final int threshold;
        private final Color color;

        UserGrade(String name, int threshold, Color color) {
            this.name = name;
            this.threshold = threshold;
            this.color = color;
        }

        public String getName() { return name; }
        public int getThreshold() { return threshold; }
        public Color getColor() { return color; }

        public static UserGrade fromPoints(int points) {
            if (points >= MASTER.threshold) return MASTER;
            if (points >= GUARDIAN.threshold) return GUARDIAN;
            if (points >= FRIEND.threshold) return FRIEND;
            return SPROUT;
        }

        public UserGrade next() {
            int ordinal = this.ordinal();
            if (ordinal < UserGrade.values().length - 1) {
                return UserGrade.values()[ordinal + 1];
            }
            return null; 
        }
    }

    // --- 생성자 (Constructors) ---

    /** [1] 기본 생성자 */
    public UserDTO() {
        this.balancePoints = 0;
        this.totalPoints = 0;
        this.attendanceStreak = 0;
        this.isAdmin = false;
    }

    /** [2] 신규 가입용 간이 생성자 */
    public UserDTO(String userId, String nickname) {
        this(userId, nickname, 0, 0, 0, false);
    }

    /** [3] DB 조회 및 전체 필드 생성자 */
    public UserDTO(String userId, String nickname, int balancePoints, int totalPoints, int attendanceStreak, boolean isAdmin) {
        setUserId(userId);
        setNickname(nickname);
        setBalancePoints(balancePoints);
        setTotalPoints(totalPoints);
        setAttendanceStreak(attendanceStreak);
        this.isAdmin = isAdmin;
    }

    // --- [Business Logic] 등급 및 경험치 관리 ---

    public UserGrade getGrade() {
        return UserGrade.fromPoints(this.totalPoints);
    }

    public int getGradeProgress() {
        UserGrade current = getGrade();
        UserGrade next = current.next();
        if (next == null) return 100;

        int currentLevelBase = current.threshold;
        int nextLevelGoal = next.threshold;
        
        double progress = (double) (totalPoints - currentLevelBase) / (nextLevelGoal - currentLevelBase) * 100;
        return (int) Math.max(0, Math.min(100, progress));
    }

    public int getPointsUntilNextGrade() {
        UserGrade next = getGrade().next();
        if (next == null) return 0;
        return Math.max(0, next.threshold - totalPoints);
    }

    // --- [UI Helper] 데이터 포맷팅 ---

    public String getFormattedBalance() {
        return new DecimalFormat("#,###").format(balancePoints) + " P";
    }

    public String getFormattedTotal() {
        return new DecimalFormat("#,###").format(totalPoints) + " P";
    }

    public String getRoleDisplayName() {
        return this.isAdmin ? "시스템 관리자" : "지구 수호자";
    }

    // --- [Service Logic] 포인트 데이터 조작 ---

    public void addPoints(int points) {
        if (points <= 0) return;
        this.balancePoints += points;
        this.totalPoints += points;
    }

    public boolean spendPoints(int points) {
        if (points > 0 && this.balancePoints >= points) {
            this.balancePoints -= points;
            return true;
        }
        return false;
    }

    // --- Getter & Setter ---

    public String getRegDate() { return regDate; }
    public void setRegDate(String regDate) { this.regDate = regDate; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { 
        this.userId = Objects.requireNonNull(userId, "사용자 ID는 Null일 수 없습니다."); 
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { 
        this.nickname = Objects.requireNonNull(nickname, "닉네임은 필수 항목입니다."); 
    }

    public int getBalancePoints() { return balancePoints; }
    public void setBalancePoints(int points) { 
        this.balancePoints = Math.max(0, points); 
    }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int points) { 
        this.totalPoints = Math.max(0, points); 
    }

    public int getAttendanceStreak() { return attendanceStreak; }
    public void setAttendanceStreak(int streak) { 
        this.attendanceStreak = Math.max(0, streak); 
    }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { this.isAdmin = admin; }

    // --- Utility Methods ---

    /** * 객체의 깊은 복사(Deep Copy)를 지원 
     * 가입일(regDate)과 최근접속일(lastLogin)도 복사 대상에 포함했습니다.
     */
    public UserDTO copy() {
        UserDTO copy = new UserDTO(userId, nickname, balancePoints, totalPoints, attendanceStreak, isAdmin);
        copy.setPassword(this.password);
        copy.setRegDate(this.regDate);   // [추가됨]
        copy.setLastLogin(this.lastLogin); // [추가됨]
        return copy;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s(%s) | 잔액: %d P | 누적: %d P", 
                getGrade().getName(), nickname, userId, balancePoints, totalPoints);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDTO)) return false;
        UserDTO that = (UserDTO) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}