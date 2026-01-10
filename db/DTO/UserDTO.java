package db.DTO;


public class UserDTO {
    private String userId;
    private String password;   
    private String nickname;
    private int balancePoints;   
    private int totalPoints;    
    private int attendanceStreak; 
    private boolean isAdmin;      

    public UserDTO() {}

    public UserDTO(String userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
        this.balancePoints = 0;
        this.totalPoints = 0;
        this.attendanceStreak = 0;
        this.isAdmin = false;
    }

  
    public UserDTO(String userId, String password, String nickname, int balancePoints, int totalPoints, boolean isAdmin) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.balancePoints = balancePoints;
        this.totalPoints = totalPoints;
        this.attendanceStreak = 0;
        this.isAdmin = isAdmin;
    }
    
   
    public UserDTO(String userId, String nickname, int balancePoints, int totalPoints, int attendanceStreak, boolean isAdmin) {
        this.userId = userId;
        this.nickname = nickname;
        this.balancePoints = balancePoints;
        this.totalPoints = totalPoints;
        this.attendanceStreak = attendanceStreak;
        this.isAdmin = isAdmin;
    }

 
    public void addPoints(int points) {
        if (points > 0) {
            this.balancePoints += points;
            this.totalPoints += points;
        }
    }

   
    public boolean spendPoints(int points) {
        if (points > 0 && this.balancePoints >= points) {
            this.balancePoints -= points;
            return true;
        }
        return false;
    }

    public String getRoleString() {
        return this.isAdmin ? "관리자" : "일반회원";
    }


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
    

    @Override
    public String toString() {
        return String.format("UserDTO[ID=%s, 닉네임=%s, 잔여=%d P, 누적=%d P, 관리자=%b]",
                userId, nickname, balancePoints, totalPoints, isAdmin);
    }

    public UserDTO copy() {
        UserDTO copy = new UserDTO(userId, nickname, balancePoints, totalPoints, attendanceStreak, isAdmin);
        copy.setPassword(this.password);
        return copy;
    }
}