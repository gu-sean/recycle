package db.DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 사용자의 활동 로그(분리수거 적립, 퀴즈 보상, 상품 구매) 정보를 담는 데이터 전송 객체 (DTO)
 */
public class RecycleLogDTO {

    private int logId;
    private String userId;          // 사용자 아이디
    private LocalDateTime logDate;  // 활동 시간 (DB의 TIMESTAMP)
    private String type;            // 구분: "적립" 또는 "사용"
    private String detail;          // 상세 내용: "분리수거: 종이 (15P)", "퀴즈 보상: 1단계", "상품 구매: 커피"
    private int points;             // 변동 포인트 금액

    // [1] 기본 생성자
    public RecycleLogDTO() {}

    /**
     * [2] 전체 필드 생성자 (DB 조회 결과 저장용)
     */
    public RecycleLogDTO(int logId, String userId, LocalDateTime logDate, String type, String detail, int points) {
        this.logId = logId;
        this.userId = userId;
        this.logDate = logDate;
        this.type = type;
        this.detail = detail;
        this.points = points;
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


    // --- 마이페이지 UI 및 통계를 위한 편의 메서드 ---

    /**
     * 날짜를 "MM/dd HH:mm" 또는 "yyyy-MM-dd HH:mm" 형식으로 변환하여 반환
     */
    public String getFormattedDate() {
        if (logDate == null) return "-";
        return logDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * 포인트를 UI용 문자열로 변환 (예: +100 P / -50 P)
     */
    public String getFormattedPoints() {
        String sign = "적립".equals(this.type) ? "+" : ("사용".equals(this.type) ? "-" : "");
        return sign + points + " P";
    }

    /**
     * 포인트 유형에 따른 텍스트 색상 결정 (Swing UI 활용 시)
     */
    public java.awt.Color getPointColor() {
        if ("적립".equals(this.type)) return new java.awt.Color(0, 102, 204); // 진한 파란색
        if ("사용".equals(this.type)) return new java.awt.Color(204, 0, 0);   // 진한 빨간색
        return java.awt.Color.BLACK;
    }

    /**
     * 상세 내용(detail)을 분석하여 활동 카테고리 아이콘/이름 반환
     * (마이페이지에서 활동별로 아이콘을 다르게 보여줄 때 유용)
     */
    public String getCategory() {
        if (detail == null) return "기타";
        if (detail.contains("분리수거")) return "♻️ 분리수거";
        if (detail.contains("퀴즈")) return "💡 퀴즈보상";
        if (detail.contains("상품 구매")) return "🛒 상점이용";
        return "📝 기타로그";
    }

    @Override
    public String toString() {
        return String.format("Log[%d] %s | %s | %s (%s)", 
                logId, getFormattedDate(), type, detail, getFormattedPoints());
    }
    
    /**
     * 객체 깊은 복사 (필요 시)
     */
    public RecycleLogDTO copy() {
        return new RecycleLogDTO(logId, userId, logDate, type, detail, points);
    }
}