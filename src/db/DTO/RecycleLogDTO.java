package db.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.util.Objects;

/**
 * 사용자의 활동 로그(분리수거 적립, 퀴즈 보상, 상품 구매) 정보를 담는 데이터 전송 객체 (DTO)
 */
public class RecycleLogDTO {

    private int logId;
    private String userId;          // 사용자 아이디
    private LocalDateTime logDate;  // 활동 시간 (DB의 TIMESTAMP)
    private String type;            // 구분: "적립" 또는 "사용"
    private String detail;          // 상세 내용
    private int points;             // 변동 포인트 금액

    // 기본 생성자
    public RecycleLogDTO() {
        this.logDate = LocalDateTime.now();
        this.type = "적립";
        this.detail = "";
    }

    // 전체 필드 생성자
    public RecycleLogDTO(int logId, String userId, LocalDateTime logDate, String type, String detail, int points) {
        this.logId = logId;
        this.userId = userId;
        this.logDate = (logDate != null) ? logDate : LocalDateTime.now();
        this.type = (type != null) ? type : "적립";
        this.detail = (detail != null) ? detail : "";
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

    // --- 핵심 로직 (Logic) ---

    /**
     * 오늘 발생한 로그인지 확인
     * (재실행 시 퀴즈 참여 여부 판단 및 로딩 최적화용)
     */
    public boolean isToday() {
        if (logDate == null) return false;
        return logDate.toLocalDate().isEqual(LocalDate.now());
    }

    /**
     * 특정 품목 포함 여부 확인 (어뷰징 방지 및 검색용)
     */
    public boolean containsItem(String itemName) {
        if (itemName == null || detail == null || !detail.startsWith("분리수거: ")) return false;
        return extractItemNames().contains(itemName.trim());
    }

    /**
     * 상세 내용(detail)에서 품목 이름들만 깔끔하게 추출
     */
    public List<String> extractItemNames() {
        List<String> items = new ArrayList<>();
        if (detail == null || !detail.startsWith("분리수거: ")) return items;

        try {
            // "분리수거: " 이후 텍스트 추출
            String content = detail.substring("분리수거: ".length()).trim();
            if (content.isEmpty()) return items;

            // 콤마로 구분된 각 품목 파싱
            String[] parts = content.split(",\\s*");
            for (String part : parts) {
                // "플라스틱 (50P)" 형태에서 이름만 추출
                int braceIndex = part.lastIndexOf(" (");
                String name = (braceIndex != -1) ? part.substring(0, braceIndex).trim() : part.trim();
                if (!name.isEmpty()) items.add(name);
            }
        } catch (Exception e) {
            System.err.println("DTO 품목 추출 중 오류: " + e.getMessage());
        }
        return items;
    }

    /**
     * UI 카테고리 분석 및 아이콘 지정
     * 포인트와 상세 내용을 기반으로 로그의 성격을 정의합니다.
     */
    public String getCategory() {
        if (detail == null) return "📝 기타";
        
        if (detail.contains("분리수거")) return "♻️ 분리수거";
        if (detail.contains("퀴즈")) {
            // 포인트가 0보다 크면 성공, 0이면 실패 또는 참여로 간주
            return (points > 0) ? "💡 퀴즈성공" : "❌ 퀴즈실패";
        }
        if (detail.contains("상품 구매") || detail.contains("교환") || "사용".equals(type)) {
            return "🛒 상점이용";
        }
        
        return "📝 기타로그";
    }

    // --- UI 가독성 지원 메서드 ---

    /**
     * 날짜 포맷팅 (초 단위 제외하여 깔끔하게 표시)
     */
    public String getFormattedDate() {
        if (logDate == null) return "-";
        return logDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * 부호가 포함된 포인트 표시 (예: +50 P, -100 P)
     */
    public String getFormattedPoints() {
        String sign = "적립".equals(this.type) ? "+" : ("사용".equals(this.type) ? "-" : "");
        return sign + Math.abs(points) + " P";
    }

    /**
     * 로그 타입에 따른 UI 컬러 반환
     */
    public Color getPointColor() {
        if ("적립".equals(this.type)) return new Color(0, 255, 240); // Cyan (밝은 느낌)
        if ("사용".equals(this.type)) return new Color(255, 80, 120); // Pink/Red (감소 느낌)
        return Color.LIGHT_GRAY;
    }

    @Override
    public String toString() {
        return String.format("LogDTO[ID=%d, User=%s, Date=%s, Type=%s, Points=%d]", 
                logId, userId, getFormattedDate(), type, points);
    }

    /**
     * 객체 깊은 복사 (UI 리스트 갱신 시 원본 보호용)
     */
    public RecycleLogDTO copy() {
        return new RecycleLogDTO(logId, userId, logDate, type, detail, points);
    }
}