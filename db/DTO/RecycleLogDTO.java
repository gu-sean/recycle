package db.DTO;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.awt.Color;
import java.sql.Timestamp;
import java.util.Objects;


public class RecycleLogDTO {

    private int logId;
    private String userId;          
    private LocalDateTime logDate;  
    private String type;            // "적립", "사용", "차감"
    private String detail;          // 예: "분리수거:종이", "데일리 퀴즈 보상 완료 (20P)"
    private int points;             

    // --- 환경 보호 상수 (환경부 및 산림청 2024 가이드라인 반영) ---
    private static final double CO2_PER_ITEM = 0.42;     // 품목당 CO2 저감량 (kg)
    private static final double TREE_COEFFICIENT = 0.15;  // CO2 1kg당 소나무 식재 효과 (그루)

    // --- UI 테마 컬러 (네온 다크 모드 최적화) ---
    private static final Color COLOR_EARN = new Color(0, 240, 255);    // 사이언 (적립)
    private static final Color COLOR_SPEND = new Color(255, 50, 100);  // 네온 레드 (사용/차감)
    private static final Color COLOR_ZERO = new Color(140, 140, 160);  // 뮤트 그레이 (내역 없음)

    /** [1] 기본 생성자 */
    public RecycleLogDTO() {
        this.logDate = LocalDateTime.now();
        this.type = "적립";
        this.detail = "";
        this.points = 0;
    }

    /** [2] 전체 필드 생성자 (로그 생성용) */
    public RecycleLogDTO(int logId, String userId, String type, String detail, int points, LocalDateTime logDate) {
        this.logId = logId;
        this.userId = userId;
        this.type = type;
        this.detail = detail;
        this.points = points;
        this.logDate = (logDate != null) ? logDate : LocalDateTime.now();
    }

    /** * [3] DB 조회 전용 생성자 
     * 타임존 오차 없이 시스템 로컬 시간으로 변환합니다.
     */
    public RecycleLogDTO(String type, String detail, int points, Timestamp timestamp) {
        this.type = Objects.requireNonNullElse(type, "적립");
        this.detail = Objects.requireNonNullElse(detail, "");
        this.points = points;
        
        if (timestamp != null) {

            this.logDate = timestamp.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime();
        } else {
            this.logDate = LocalDateTime.now();
        }
    }

    // --- Getter & Setter ---
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDateTime getLogDate() { return logDate; }
    public void setLogDate(LocalDateTime logDate) { this.logDate = logDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    // --- [Business Logic] 데이터 분석 및 변환 ---

    /** 요일 데이터 반환 (통계 차트용) */
    public DayOfWeek getDayOfWeek() {
        return (logDate == null) ? DayOfWeek.MONDAY : logDate.getDayOfWeek();
    }

    /** 내역 카테고리 자동 분류 */
    public String getCategoryName() {
        if (detail == null || detail.isEmpty()) return "기타";
        if (detail.startsWith("분리수거")) return "분리수거";
        if (detail.contains("퀴즈")) return "에코 퀴즈";
        if (detail.contains("구매") || "사용".equals(type)) return "상점 이용";
        return "포인트 변동";
    }

    /** UI용 품목명 정제 */
    public String getItemName() {
        if (detail == null || detail.isEmpty()) return "내역 없음";
        try {
            String result = detail;

            if (detail.contains(":")) {
                String[] parts = detail.split(":", 2);
                result = parts[parts.length - 1].trim();
            }

            if (result.contains("(")) {
                result = result.substring(0, result.indexOf("(")).trim();
            }
            return (result.length() > 10) ? result.substring(0, 9) + "..." : result;
        } catch (Exception e) {
            return detail;
        }
    }

    // --- [UI Helper] 포맷팅 및 시각화 ---

    public String getFormattedPoints() {
        boolean isMinus = "사용".equals(type) || "차감".equals(type) || points < 0;
        return String.format("%s%,d P", isMinus ? "-" : "+", Math.abs(points));
    }

    public String getFormattedDate() {
        return (logDate == null) ? "--/--" : logDate.format(DateTimeFormatter.ofPattern("MM/dd"));
    }

    public String getFormattedTime() {
        return (logDate == null) ? "-:-" : logDate.format(DateTimeFormatter.ofPattern("a hh:mm"));
    }

    /** 탄소 발자국 저감 효과 계산 (단위: kg) */
    public double getCO2Reduction() {
        return getCategoryName().equals("분리수거") ? CO2_PER_ITEM : 0.0;
    }

    /** 소나무 식재 효과 (그루 수로 환산) */
    public double getTreeEffect() {
        // CO2 1kg 저감 = 소나무 약 0.15그루 식재 효과
        return getCO2Reduction() * TREE_COEFFICIENT;
    }

    /** 포인트 성격에 따른 UI 강조색 반환 */
    public Color getPointColor() {
        if ("사용".equals(type) || "차감".equals(type)) return COLOR_SPEND;
        return (points > 0) ? COLOR_EARN : COLOR_ZERO;
    }

    public String getSummary() {
        return String.format("[%s] %s (%s)", getCategoryName(), getItemName(), getFormattedPoints());
    }

    @Override
    public String toString() {
        return getSummary();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecycleLogDTO)) return false;
        RecycleLogDTO that = (RecycleLogDTO) o;
        return logId == that.logId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId);
    }
}