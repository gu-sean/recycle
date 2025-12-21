package db.DTO;

/**
 * 사용자 정보를 담는 데이터 전송 객체 (DTO)
 * DB의 USERS 테이블과 매핑되며, 애플리케이션 전반에서 사용자 상태를 전달합니다.
 */
public class UserDTO {
    private String userId;
    private String nickname;
    private int balancePoints;    // 현재 사용 가능한 잔여 포인트
    private int totalPoints;      // 지금까지 획득한 전체 누적 포인트 (랭킹 산정 기준)
    private int attendanceStreak; // 연속 출석 횟수
    private boolean isAdmin;      // 관리자 권한 여부

    // [1] 기본 생성자
    public UserDTO() {}

    // [2] 필수 필드 위주의 생성자
    public UserDTO(String userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
        this.balancePoints = 0;
        this.totalPoints = 0;
        this.attendanceStreak = 0;
        this.isAdmin = false;
    }
    
    // [3] 전체 필드를 사용하는 생성자 (DB 조회 결과 매핑용)
    public UserDTO(String userId, String nickname, int balancePoints, int totalPoints, int attendanceStreak, boolean isAdmin) {
        this.userId = userId;
        this.nickname = nickname;
        this.balancePoints = balancePoints;
        this.totalPoints = totalPoints;
        this.attendanceStreak = attendanceStreak;
        this.isAdmin = isAdmin;
    }

    // --- 비즈니스 로직 편의 메서드 ---

    /**
     * 포인트를 획득했을 때 호출 (잔액과 누적 포인트를 동시에 증가)
     */
    public void addPoints(int points) {
        if (points > 0) {
            this.balancePoints += points;
            this.totalPoints += points;
        }
    }

    /**
     * 포인트를 소비(상품 구매 등)했을 때 호출
     * @return 구매 가능 여부 (잔액이 충분하면 true)
     */
    public boolean spendPoints(int points) {
        if (points > 0 && this.balancePoints >= points) {
            this.balancePoints -= points;
            return true;
        }
        return false;
    }

    /**
     * 관리자 권한 여부를 문자열로 반환 (UI 테이블 표시용)
     */
    public String getRoleString() {
        return this.isAdmin ? "관리자" : "일반회원";
    }

    // --- Getter & Setter ---

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getBalancePoints() { return balancePoints; }
    public void setBalancePoints(int balancePoints) { this.balancePoints = balancePoints; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
 
    public int getAttendanceStreak() { return attendanceStreak; }
    public void setAttendanceStreak(int attendanceStreak) { this.attendanceStreak = attendanceStreak; }
    
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
    
    // --- 유틸리티 메서드 ---

    @Override
    public String toString() {
        return String.format("UserDTO[ID=%s, 닉네임=%s, 잔여=%d P, 누적=%d P, 관리자=%b]",
                userId, nickname, balancePoints, totalPoints, isAdmin);
    }

    /**
     * 현재 객체의 깊은 복사본을 생성합니다.
     */
    public UserDTO copy() {
        return new UserDTO(userId, nickname, balancePoints, totalPoints, attendanceStreak, isAdmin);
    }
}