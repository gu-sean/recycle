package db.DTO;

/**
 * 사용자 정보를 담는 데이터 전송 객체 (DTO)
 * DB의 USERS 테이블과 매핑되며, 애플리케이션 전반에서 사용자 상태를 전달합니다.
 */
public class UserDTO {
    private String userId;
    private String password;      // 관리자가 신규 회원을 등록할 때 필요
    private String nickname;
    private int balancePoints;    // 현재 사용 가능한 잔여 포인트 (구매 시 차감되는 실제 포인트)
    private int totalPoints;      // 지금까지 획득한 전체 누적 포인트 (랭킹 산정 기준)
    private int attendanceStreak; // 연속 출석 횟수
    private boolean isAdmin;      // 관리자 권한 여부

    // [1] 기본 생성자
    public UserDTO() {}

    // [2] 최소 정보 생성자 (회원가입/테스트용)
    public UserDTO(String userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
        this.balancePoints = 0;
        this.totalPoints = 0;
        this.attendanceStreak = 0;
        this.isAdmin = false;
    }

    /**
     * [3] 관리자용 사용자 정보 수정/등록 생성자 (비밀번호, 누적포인트 포함)
     * AdminWindow에서 사용자가 수정 보류 중인 데이터를 담을 때 사용합니다.
     */
    public UserDTO(String userId, String password, String nickname, int balancePoints, int totalPoints, boolean isAdmin) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.balancePoints = balancePoints;
        this.totalPoints = totalPoints;
        this.attendanceStreak = 0;
        this.isAdmin = isAdmin;
    }
    
    /**
     * [4] DB 조회 결과 매핑용 생성자 (비밀번호 제외 전체 필드)
     * UserDAO의 mapUserDTO 메서드에서 주로 사용됩니다.
     */
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
     * 포인트 획득 시 호출 (잔액과 누적 포인트를 동시에 증가)
     */
    public void addPoints(int points) {
        if (points > 0) {
            this.balancePoints += points;
            this.totalPoints += points;
        }
    }

    /**
     * 포인트를 소비(상품 구매 등)했을 때 호출
     * 잔여 포인트(balancePoints)가 부족하면 false를 반환합니다.
     */
    public boolean spendPoints(int points) {
        if (points > 0 && this.balancePoints >= points) {
            this.balancePoints -= points;
            // 누적 포인트(totalPoints)는 소비해도 줄어들지 않음 (랭킹 유지)
            return true;
        }
        return false;
    }

    public String getRoleString() {
        return this.isAdmin ? "관리자" : "일반회원";
    }

    // --- Getter & Setter ---

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getBalancePoints() { return balancePoints; }
    public void setBalancePoints(int balancePoints) { 
        this.balancePoints = Math.max(0, balancePoints); 
    }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { 
        this.totalPoints = Math.max(0, totalPoints); 
    }
 
    public int getAttendanceStreak() { return attendanceStreak; }
    public void setAttendanceStreak(int attendanceStreak) { 
        this.attendanceStreak = Math.max(0, attendanceStreak); 
    }
    
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
    
    // --- 유틸리티 메서드 ---

    @Override
    public String toString() {
        return String.format("UserDTO[ID=%s, 닉네임=%s, 잔여=%d P, 누적=%d P, 관리자=%b]",
                userId, nickname, balancePoints, totalPoints, isAdmin);
    }

    /**
     * 현재 객체의 깊은 복사본을 생성 (수정 취소 기능 등을 구현할 때 유용)
     */
    public UserDTO copy() {
        UserDTO copy = new UserDTO(userId, nickname, balancePoints, totalPoints, attendanceStreak, isAdmin);
        copy.setPassword(this.password);
        return copy;
    }
}