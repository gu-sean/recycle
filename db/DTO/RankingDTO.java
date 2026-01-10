package db.DTO;


public class RankingDTO {
    
    private final String userId;
    private final String nickname;
    private final int totalPoints; 

  
    public RankingDTO(String userId, String nickname, int totalPoints) {
        this.userId = userId;
        this.nickname = nickname;
        this.totalPoints = totalPoints;
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

   
    public int getTotalPoints() {
        return totalPoints;
    }
}