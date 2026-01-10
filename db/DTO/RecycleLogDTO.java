package db.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.util.Objects;


public class RecycleLogDTO {

    private int logId;
    private String userId;          
    private LocalDateTime logDate;  
    private String type;            
    private String detail;        
    private int points;            

    public RecycleLogDTO() {
        this.logDate = LocalDateTime.now();
        this.type = "적립";
        this.detail = "";
    }

    public RecycleLogDTO(int logId, String userId, LocalDateTime logDate, String type, String detail, int points) {
        this.logId = logId;
        this.userId = userId;
        this.logDate = (logDate != null) ? logDate : LocalDateTime.now();
        this.type = (type != null) ? type : "적립";
        this.detail = (detail != null) ? detail : "";
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

   
    public boolean isToday() {
        if (logDate == null) return false;
        return logDate.toLocalDate().isEqual(LocalDate.now());
    }

    
    public boolean containsItem(String itemName) {
        if (itemName == null || detail == null || !detail.startsWith("분리수거: ")) return false;
        return extractItemNames().contains(itemName.trim());
    }

    
    public List<String> extractItemNames() {
        List<String> items = new ArrayList<>();
        if (detail == null || !detail.startsWith("분리수거: ")) return items;

        try {
       
            String content = detail.substring("분리수거: ".length()).trim();
            if (content.isEmpty()) return items;

            String[] parts = content.split(",\\s*");
            for (String part : parts) {
            
                int braceIndex = part.lastIndexOf(" (");
                String name = (braceIndex != -1) ? part.substring(0, braceIndex).trim() : part.trim();
                if (!name.isEmpty()) items.add(name);
            }
        } catch (Exception e) {
            System.err.println("DTO 품목 추출 중 오류: " + e.getMessage());
        }
        return items;
    }

   
    public String getCategory() {
        if (detail == null) return "📝 기타";
        
        if (detail.contains("분리수거")) return "♻️ 분리수거";
        if (detail.contains("퀴즈")) {
  
            return (points > 0) ? "💡 퀴즈성공" : "❌ 퀴즈실패";
        }
        if (detail.contains("상품 구매") || detail.contains("교환") || "사용".equals(type)) {
            return "🛒 상점이용";
        }
        
        return "📝 기타로그";
    }

    public String getFormattedDate() {
        if (logDate == null) return "-";
        return logDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

   
    public String getFormattedPoints() {
        String sign = "적립".equals(this.type) ? "+" : ("사용".equals(this.type) ? "-" : "");
        return sign + Math.abs(points) + " P";
    }

   
    public Color getPointColor() {
        if ("적립".equals(this.type)) return new Color(0, 255, 240); 
        if ("사용".equals(this.type)) return new Color(255, 80, 120); 
        return Color.LIGHT_GRAY;
    }

    @Override
    public String toString() {
        return String.format("LogDTO[ID=%d, User=%s, Date=%s, Type=%s, Points=%d]", 
                logId, userId, getFormattedDate(), type, points);
    }

    public RecycleLogDTO copy() {
        return new RecycleLogDTO(logId, userId, logDate, type, detail, points);
    }
}