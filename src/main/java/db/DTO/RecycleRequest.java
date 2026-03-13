package db.DTO;

public class RecycleRequest {
    private String userId;
    private String item;

    // 1. Jackson 라이브러리가 객체를 생성하기 위해 필수적으로 필요한 기본 생성자
    public RecycleRequest() {
    }

    // 2. 데이터를 담기 위한 생성자 (선택 사항)
    public RecycleRequest(String userId, String item) {
        this.userId = userId;
        this.item = item;
    }

    // Getter, Setter (필수 - 데이터 바인딩에 사용됨)
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
}