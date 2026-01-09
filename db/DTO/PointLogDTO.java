package db.DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PointLogDTO {
    
    private final int logId;
    private final String userId;
    private final String type;    
    private final String detail;
    private final int amount;    
    private final LocalDateTime timestamp; 

    public PointLogDTO(int logId, String userId, String type, String detail, int amount, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.type = type;
        this.detail = detail;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public int getLogId() { return logId; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getDetail() { return detail; }
    public int getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }

  
    public String getFormattedTimestamp() {
        if (timestamp == null) return "-";
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

 
    public String getFormattedAmount() {
        if (amount > 0) {
            return "+" + amount + " P";
        }
        return amount + " P"; 
    }

   
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