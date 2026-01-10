package db.DTO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.awt.Color;
import java.sql.Timestamp;


public class RecycleLogDTO {

    private int logId;
    private String userId;          
    private LocalDateTime logDate;  
    private String type;         
    private String detail;       
    private int points;             

    private static final Color COLOR_EARN = new Color(0, 240, 255);   
    private static final Color COLOR_SPEND = new Color(255, 50, 100);  
    private static final Color COLOR_ZERO = new Color(150, 150, 180);  

    public RecycleLogDTO() {

        this.logDate = LocalDateTime.now(ZoneId.systemDefault());
        this.type = "적립";
    }

  
    public RecycleLogDTO(String type, String detail, int points, Timestamp timestamp) {
        this.type = (type != null) ? type : "적립";
        this.detail = (detail != null) ? detail : "";
        this.points = points;
        
        if (timestamp != null) {
        
            this.logDate = timestamp.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime();
        } else {
            this.logDate = LocalDateTime.now();
        }
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

    public DayOfWeek getDayOfWeek() {
        if (logDate == null) return DayOfWeek.MONDAY;
        return logDate.toLocalDate().getDayOfWeek();
    }

   
    public String getCategoryName() {
        if (detail == null || detail.isEmpty()) return "기타";
        if (detail.startsWith("분리수거")) return "분리수거";
        if (detail.contains("퀴즈")) return "퀴즈";
        if (detail.contains("구매") || "사용".equals(type)) return "상점";
        return "포인트";
    }

  
    public String getItemName() {
        if (detail == null || detail.isEmpty()) return "내역 없음";
        try {
            String result = detail;
            if (detail.contains(":")) {
                result = detail.split(":", 2)[1].trim();
            }
            if (result.contains("(")) {
                result = result.substring(0, result.lastIndexOf("(")).trim();
            }
            return (result.length() > 12) ? result.substring(0, 10) + "..." : result;
        } catch (Exception e) {
            return detail;
        }
    }

   
    public String getFormattedPoints() {
        String sign = ("사용".equals(type) || "차감".equals(type)) ? "-" : "+";
        return sign + String.format("%,d", Math.abs(points)) + " P";
    }

   
    public String getFormattedTime() {
        if (logDate == null) return "-";
        return logDate.format(DateTimeFormatter.ofPattern("a hh:mm"));
    }

  
    public double getCO2Reduction() {
      
        if ("분리수거".equals(getCategoryName())) {
            return 0.42; 
        }
        return 0.0;
    }

    
    public double getTreeEffect() {
    
        return getCO2Reduction() * 0.15; 
    }

   
    public Color getPointColor() {
        if ("사용".equals(type) || "차감".equals(type)) return COLOR_SPEND;
        return (points > 0) ? COLOR_EARN : COLOR_ZERO;
    }
}