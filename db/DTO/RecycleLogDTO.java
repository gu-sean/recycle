package db.DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class RecycleLogDTO {

    private int logId;
    private String userId;         
    private LocalDateTime logDate;  
    private String type;            
    private String detail;          
    private int points;             

    public RecycleLogDTO() {}

   
    public RecycleLogDTO(int logId, String userId, LocalDateTime logDate, String type, String detail, int points) {
        this.logId = logId;
        this.userId = userId;
        this.logDate = logDate;
        this.type = type;
        this.detail = detail;
        this.points = points;
    }


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


  
    public String getFormattedDate() {
        if (logDate == null) return "-";
        return logDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

   
    public String getFormattedPoints() {
        String sign = "적립".equals(this.type) ? "+" : ("사용".equals(this.type) ? "-" : "");
        return sign + points + " P";
    }

 
    public java.awt.Color getPointColor() {
        if ("적립".equals(this.type)) return new java.awt.Color(0, 102, 204); 
        if ("사용".equals(this.type)) return new java.awt.Color(204, 0, 0);   
        return java.awt.Color.BLACK;
    }

   
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
    
  
    public RecycleLogDTO copy() {
        return new RecycleLogDTO(logId, userId, logDate, type, detail, points);
    }
}