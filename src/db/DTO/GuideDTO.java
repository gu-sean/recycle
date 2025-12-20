package db.DTO;

/**
 * 분리수거 가이드 데이터를 담는 전송 객체 (Data Transfer Object)
 */
public class GuideDTO {
    
    private final String category;   // 카테고리 명 (예: 플라스틱)
    private final String itemName;   // 세부 품목 명 (예: 페트병)
    private final String content;    // 배출 방법 상세 설명

    // 생성자 (필드 추가)
    public GuideDTO(String category, String itemName, String content) {
        this.category = category;
        this.itemName = itemName;
        this.content = content;
    }

    // 카테고리 이름 반환
    public String getCategory() {
        return category;
    }

    // 품목 이름 반환 (추가됨)
    public String getItemName() {
        return itemName;
    }

    // 배출 방법 상세 내용 반환
    public String getContent() {
        return content;
    }

    /**
     * 리스트 등에 표시될 때 형식을 지정하기 위한 toString 오버라이드 (선택 사항)
     */
    @Override
    public String toString() {
        return "[" + category + "] " + itemName;
    }
}