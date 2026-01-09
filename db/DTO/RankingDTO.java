package db.DTO;


public class RankingDTO {
    
    private final String userId;
    private final String nickname;
    private final int balancePoints; 

    public RankingDTO(String userId, String nickname, int balancePoints) {
        this.userId = userId;
        this.nickname = nickname;
        this.balancePoints = balancePoints;
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public int getBalancePoints() {
        return balancePoints;
    }
}