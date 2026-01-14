package db.DTO;

import java.io.Serializable;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Objects;


public class UserDTO implements Serializable {
    

    private static final long serialVersionUID = 2L; 

    private String userId;
    
 
    private transient String password;      
    
    private String nickname;
    private int balancePoints;    // 현재 보유 포인트 (소비 가능)
    private int totalPoints;      // 누적 획득 포인트 (등급 산정 기준)
    private int attendanceStreak; // 연속 출석 일수
    private boolean isAdmin;      // 관리자 권한 여부

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
            return null; // 최고 등급인 경우
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

    /** 현재 사용자의 등급 반환 */
    public UserGrade getGrade() {
        return UserGrade.fromPoints(this.totalPoints);
    }

    /** 현재 등급 내에서의 경험치 진행률 (0~100%) */
    public int getGradeProgress() {
        UserGrade current = getGrade();
        UserGrade next = current.next();

        if (next == null) return 100; // 최고 등급

        int currentLevelBase = current.threshold;
        int nextLevelGoal = next.threshold;
        
        // (현재 포인트 - 현재 등급 시작점) / (다음 등급 목표치 - 현재 등급 시작점)
        double progress = (double) (totalPoints - currentLevelBase) / (nextLevelGoal - currentLevelBase) * 100;
        return (int) Math.max(0, Math.min(100, progress));
    }

    /** 다음 등급까지 남은 포인트 계산 */
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

    /** 포인트 획득 (누적치와 현재 잔액 동시 증가) */
    public void addPoints(int points) {
        if (points <= 0) return;
        this.balancePoints += points;
        this.totalPoints += points;
    }

    /** 포인트 사용 (잔액 부족 시 false 반환) */
    public boolean spendPoints(int points) {
        if (points > 0 && this.balancePoints >= points) {
            this.balancePoints -= points;
            return true;
        }
        return false;
    }

    // --- Getter & Setter (Validation 포함) ---

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
        this.balancePoints = Math.max(0, points); // 음수 방지
    }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int points) { 
        this.totalPoints = Math.max(0, points); // 음수 방지
    }

    public int getAttendanceStreak() { return attendanceStreak; }
    public void setAttendanceStreak(int streak) { 
        this.attendanceStreak = Math.max(0, streak); 
    }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { this.isAdmin = admin; }

    // --- Utility Methods ---

    /** * 객체의 깊은 복사(Deep Copy)를 지원 
     * UI 레이어에서 원본 데이터를 안전하게 수정하거나 전달할 때 사용
     */
    public UserDTO copy() {
        UserDTO copy = new UserDTO(userId, nickname, balancePoints, totalPoints, attendanceStreak, isAdmin);
        copy.setPassword(this.password);
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
        return Objects.equals(userId, that.userId); // ID가 같으면 동일한 사용자로 간주
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}