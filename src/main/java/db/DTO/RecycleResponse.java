package db.DTO;

public class RecycleResponse {
    private String itemName;
    private int point;
    private String status;

    // 1. 기본 생성자 추가 (Jackson 변환용)
    public RecycleResponse() {}

    // 2. 기존 생성자
    public RecycleResponse(String itemName, int point) {
        this.itemName = itemName;
        this.point = point;
        this.status = "적립완료";
    }

    // Getter들 (이건 잘 작성하셨습니다!)
    public String getItemName() { return itemName; }
    public int getPoint() { return point; }
    public String getStatus() { return status; }
    
    // Setter도 추가하는 것이 안전합니다.
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setPoint(int point) { this.point = point; }
    public void setStatus(String status) { this.status = status; }
}