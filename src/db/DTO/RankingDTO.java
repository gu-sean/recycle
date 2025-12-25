package db.DTO;

/**
 * 랭킹 정보를 담는 데이터 전송 객체 (DTO)
 * 순위 산정의 기준이 되는 누적 포인트를 관리합니다.
 */
public class RankingDTO {
    
    private final String userId;
    private final String nickname;
    private final int totalPoints; // ⭐ 기존 balancePoints에서 totalPoints로 변경

    /**
     * @param userId 사용자 아이디
     * @param nickname 사용자 닉네임
     * @param totalPoints 누적 획득 포인트 (순위 결정 기준)
     */
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

    /**
     * @return 랭킹 점수의 기준이 되는 누적 포인트
     */
    public int getTotalPoints() {
        return totalPoints;
    }
}