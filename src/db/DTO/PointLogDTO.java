package db.DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 포인트 적립 및 사용 내역을 담는 데이터 전송 객체 (DTO)
 */
public class PointLogDTO {
    
    private final int logId;
    private final String userId;
    private final String type;    // "EARN"(적립) 또는 "SPEND"(사용)
    private final String detail;  // 세부 내용 (예: 분리수거 품목, 상품명 등)
    private final int points;     // 포인트 변동량 (+ 또는 -)
    private final LocalDateTime timestamp; 

    // 생성자
    public PointLogDTO(int logId, String userId, String type, String detail, int points, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.type = type;
        this.detail = detail;
        this.points = points;
        this.timestamp = timestamp;
    }

    // 기본 Getter 메서드
    public int getLogId() { return logId; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getDetail() { return detail; }
    public int getPoints() { return points; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // --- 마이페이지 UI 출력을 위한 편의 메서드 ---

    /**
     * 날짜와 시간을 "yyyy-MM-dd HH:mm" 형식의 문자열로 반환
     */
    public String getFormattedTimestamp() {
        if (timestamp == null) return "-";
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * 포인트 변동량을 기호와 단위(P)를 포함하여 반환
     * 예: +50 P, -1,200 P
     */
    public String getFormattedAmount() {
        if (points > 0) {
            return String.format("+%,d P", points);
        } else if (points < 0) {
            return String.format("%,d P", points); // 음수는 이미 기호가 포함됨
        }
        return "0 P";
    }

    /**
     * 영문 구분값(Type)을 한글로 변환하여 반환
     */
    public String getTypeKorean() {
        if ("EARN".equalsIgnoreCase(type) || "적립".equals(type)) return "적립";
        if ("SPEND".equalsIgnoreCase(type) || "사용".equals(type)) return "사용";
        return type;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s (%s)", 
            getFormattedTimestamp(), getTypeKorean(), detail, getFormattedAmount());
    }
}