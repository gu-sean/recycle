package db.DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PointLogDTO {
    
    private final int logId;
    private final String userId;
    private final String type;    // "EARN" 또는 "SPEND"
    private final String detail;
    private final int amount;     // 포인트 변동량
    private final LocalDateTime timestamp; 

    // 생성자 (기존 유지)
    public PointLogDTO(int logId, String userId, String type, String detail, int amount, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.type = type;
        this.detail = detail;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    // Getter들 (기존 유지)
    public int getLogId() { return logId; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getDetail() { return detail; }
    public int getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // --- 추가 보완 메서드 ---

    /**
     * 시간을 사람이 보기 편한 문자열로 반환 (예: 2023-12-20 14:05)
     */
    public String getFormattedTimestamp() {
        if (timestamp == null) return "-";
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * 포인트 변동량을 기호와 함께 반환 (예: +100 P, -500 P)
     */
    public String getFormattedAmount() {
        if (amount > 0) {
            return "+" + amount + " P";
        }
        return amount + " P"; // 음수일 때는 이미 -가 붙어 있음
    }

    /**
     * 구분값(type)을 한글로 반환
     */
    public String getTypeKorean() {
        if ("EARN".equalsIgnoreCase(type)) return "적립";
        if ("SPEND".equalsIgnoreCase(type)) return "사용";
        return type;
    }

    @Override
    public String toString() {
        return "PointLogDTO{" +
                "logId=" + logId +
                ", userId='" + userId + '\'' +
                ", type='" + type + '\'' +
                ", detail='" + detail + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}