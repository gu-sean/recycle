package db.DTO;

import java.util.Objects;


public class RankingDTO {
    
    private final int rank;          // 화면에 표시할 순위 (1위, 2위...)
    private final String userId;
    private final String nickname;
    private final int totalPoints;   // 누적 획득 포인트 (순위 결정 기준)
    private final String gradeName;  // 등급 명칭

    /**
     * [1] 전체 필드를 포함한 생성자
     * 등급명이 null로 들어올 경우 포인트에 따라 자동 계산합니다.
     */
    public RankingDTO(int rank, String userId, String nickname, int totalPoints, String gradeName) {
        this.rank = Math.max(0, rank);
        this.userId = userId;
        this.nickname = (nickname != null) ? nickname : "알 수 없는 사용자";
        this.totalPoints = Math.max(0, totalPoints);
        
        // 등급명이 명시되지 않은 경우 포인트 기준으로 자동 할당
        if (gradeName == null || "미설정".equals(gradeName)) {
            this.gradeName = calculateGrade(this.totalPoints);
        } else {
            this.gradeName = gradeName;
        }
    }

    /**
     * [2] DAO 단순 조회 시 사용하는 생성자
     */
    public RankingDTO(String userId, String nickname, int totalPoints) {
        this(0, userId, nickname, totalPoints, null);
    }

    // --- Getter 메서드 ---
    public int getRank() { return rank; }
    public String getUserId() { return userId; }
    public String getNickname() { return nickname; }
    public int getTotalPoints() { return totalPoints; }
    public String getGradeName() { return gradeName; }

    // --- 비즈니스 로직 및 UI 도우미 ---

    /**
     * 포인트에 따른 등급 명칭을 반환하는 정적 메서드 (내부/외부 공용)
     */
    public static String calculateGrade(int points) {
        if (points >= 10000) return "전설의 숲 🌳";
        if (points >= 5000)  return "푸른 나무 🌲";
        if (points >= 1000)  return "파릇한 새싹 🌱";
        return "희망의 씨앗 🌰";
    }

    /**
     * 포인트를 천 단위 구분 기호와 함께 반환 (예: 12,500 P)
     */
    public String getFormattedPoints() {
        return String.format("%,d P", totalPoints);
    }

    /**
     * 순위를 문자열로 반환 (예: 1st, 2nd, 3rd 또는 1위)
     */
    public String getRankDisplay() {
        if (rank <= 0) return "-";
        return rank + "위";
    }

    // --- 유틸리티 메서드 ---

    @Override
    public String toString() {
        return String.format("[%s] %s(%s) - %s [%s]", 
                             getRankDisplay(), nickname, userId, getFormattedPoints(), gradeName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankingDTO that = (RankingDTO) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}