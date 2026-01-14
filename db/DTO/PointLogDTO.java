package db.DTO;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class PointLogDTO {
    
    private final int logId;
    private final String userId;
    private final String type;    
    private final String detail; 
    private final int points;    
    private final LocalDateTime timestamp; 

    // 네온 테마 UI 컬러 상수
    private static final Color COLOR_EARN = new Color(0, 240, 255);  
    private static final Color COLOR_SPEND = new Color(255, 50, 100);
    private static final Color COLOR_TEXT = new Color(200, 200, 200); 

    /**
     * 전체 생성자
     */
    public PointLogDTO(int logId, String userId, String type, String detail, int points, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.type = (type != null) ? type : "EARN";
        this.detail = (detail != null) ? detail : "상세 내역 없음";
        this.points = points;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
    }

    // --- Getter 메서드 ---
    public int getLogId() { return logId; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getDetail() { return detail; }
    public int getPoints() { return points; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // --- UI 출력 및 비즈니스 로직 편의 메서드 ---

    /**
     * 날짜 포맷팅 (yyyy-MM-dd HH:mm)
     */
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * 날짜만 반환 (MM/dd) 
     */
    public String getShortDate() {
        return timestamp.format(DateTimeFormatter.ofPattern("MM/dd"));
    }

    /**
     * 포인트 변동량 포맷팅 
     */
    public String getFormattedAmount() {
        if (points > 0) {
            return String.format("+%,d P", points);
        } else if (points < 0) {
            return String.format("%,d P", points);
        }
        return "0 P";
    }

    /**
     * 영문 구분을 한글로 변환
     */
    public String getTypeKorean() {
        if ("EARN".equalsIgnoreCase(type) || "적립".equals(type)) return "적립";
        if ("SPEND".equalsIgnoreCase(type) || "사용".equals(type)) return "사용";
        return type;
    }

    /**
     * 포인트 성격에 따른 UI 색상 반환 
     */
    public Color getPointColor() {
        if (points > 0) return COLOR_EARN;
        if (points < 0) return COLOR_SPEND;
        return COLOR_TEXT;
    }

    /**
     * 리스트 출력용 요약 문자열
     */
    public String getSummary() {
        return String.format("[%s] %s | %s", getTypeKorean(), detail, getFormattedAmount());
    }

    @Override
    public String toString() {
        return String.format("%s %s", getFormattedTimestamp(), getSummary());
    }
}