package db.DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class PointLogDTO {
    
    private final int logId;
    private final String userId;
    private final String type;  
    private final String detail; 
    private final int points;     
    private final LocalDateTime timestamp; 

    public PointLogDTO(int logId, String userId, String type, String detail, int points, LocalDateTime timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.type = type;
        this.detail = detail;
        this.points = points;
        this.timestamp = timestamp;
    }

    public int getLogId() { return logId; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getDetail() { return detail; }
    public int getPoints() { return points; }
    public LocalDateTime getTimestamp() { return timestamp; }

  
    public String getFormattedTimestamp() {
        if (timestamp == null) return "-";
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

   
    public String getFormattedAmount() {
        if (points > 0) {
            return String.format("+%,d P", points);
        } else if (points < 0) {
            return String.format("%,d P", points);
        }
        return "0 P";
    }

   
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