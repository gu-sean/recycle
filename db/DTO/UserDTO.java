package db.DTO;

import java.io.Serializable;
import java.awt.Color;
import java.util.Objects;


public class UserDTO implements Serializable {
    private static final long serialVersionUID = 2L; 

    private String userId;
    private String password;      
    private String nickname;
    private int balancePoints;   
    private int totalPoints;    
    private int attendanceStreak; 
    private boolean isAdmin;      

    public static final String GRADE_1 = "새싹 요정 🌱";
    public static final String GRADE_2 = "지구 친구 🌍";
    public static final String GRADE_3 = "환경 수호자 🌿";
    public static final String GRADE_4 = "에코 마스터 💎";

    public static final int THRESHOLD_G2 = 200;
    public static final int THRESHOLD_G3 = 500;
    public static final int THRESHOLD_G4 = 1000;

    public UserDTO() {}

    public UserDTO(String userId, String nickname) {
        this.userId = Objects.requireNonNull(userId, "ID는 필수입니다.");
        this.nickname = Objects.requireNonNull(nickname, "닉네임은 필수입니다.");
        this.balancePoints = 0;
        this.totalPoints = 0;
        this.attendanceStreak = 0;
        this.isAdmin = false;
    }

    public UserDTO(String userId, String nickname, int balancePoints, int totalPoints, int attendanceStreak, boolean isAdmin) {
        this.userId = userId;
        this.nickname = nickname;
        this.balancePoints = Math.max(0, balancePoints);
        this.totalPoints = Math.max(0, totalPoints);
        this.attendanceStreak = Math.max(0, attendanceStreak);
        this.isAdmin = isAdmin;
    }

   
    public String getGradeName() {
        if (totalPoints >= THRESHOLD_G4) return GRADE_4;
        if (totalPoints >= THRESHOLD_G3) return GRADE_3;
        if (totalPoints >= THRESHOLD_G2) return GRADE_2;
        return GRADE_1;
    }

   
    public int getGradeProgress() {
        if (totalPoints >= THRESHOLD_G4) return 100;

        int currentBase = 0;
        int nextTarget = THRESHOLD_G2;

        if (totalPoints >= THRESHOLD_G3) {
            currentBase = THRESHOLD_G3;
            nextTarget = THRESHOLD_G4;
        } else if (totalPoints >= THRESHOLD_G2) {
            currentBase = THRESHOLD_G2;
            nextTarget = THRESHOLD_G3;
        }

        double progress = (double) (totalPoints - currentBase) / (nextTarget - currentBase) * 100;
        return (int) Math.min(100, Math.max(0, progress));
    }

   
    public int getPointsUntilNextGrade() {
        if (totalPoints >= THRESHOLD_G4) return 0;
        if (totalPoints >= THRESHOLD_G3) return THRESHOLD_G4 - totalPoints;
        if (totalPoints >= THRESHOLD_G2) return THRESHOLD_G3 - totalPoints;
        return THRESHOLD_G2 - totalPoints;
    }

    
    public Color getGradeColor() {
        if (totalPoints >= THRESHOLD_G4) return new Color(0, 255, 240);
        if (totalPoints >= THRESHOLD_G3) return new Color(46, 204, 113); 
        if (totalPoints >= THRESHOLD_G2) return new Color(241, 196, 15); 
        return new Color(189, 195, 199); 
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


    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getBalancePoints() { return balancePoints; }
    public void setBalancePoints(int balancePoints) { this.balancePoints = Math.max(0, balancePoints); }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = Math.max(0, totalPoints); }

    public int getAttendanceStreak() { return attendanceStreak; }
    public void setAttendanceStreak(int attendanceStreak) { this.attendanceStreak = Math.max(0, attendanceStreak); }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }


    public String getRoleDisplayName() {
        return this.isAdmin ? "관리자" : "일반회원";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (포인트: %d, 누적: %d)", getGradeName(), nickname, balancePoints, totalPoints);
    }

    
    public UserDTO copy() {
        UserDTO copy = new UserDTO(userId, nickname, balancePoints, totalPoints, attendanceStreak, isAdmin);
        copy.setPassword(this.password);
        return copy;
    }
}