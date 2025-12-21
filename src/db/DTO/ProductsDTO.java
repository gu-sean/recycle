package db.DTO;

import java.util.Objects;

/**
 * 상품 정보를 관리하는 데이터 전송 객체 (DTO)
 * 기존 필드: ID, 이름, 포인트
 * 추가 필드: 재고, 이미지 경로, 상세 설명
 */
public class ProductsDTO {
    private String productId;      // 상품 고유 번호 (8자리 UUID 등)
    private String productName;    // 상품 이름
    private int requiredPoints;    // 구매에 필요한 포인트
    private int stock;             // 상품 재고 수량
    private String imagePath;      // 상품 이미지 파일 경로
    private String description;    // 상품 상세 설명

    // [1] 기본 생성자
    public ProductsDTO() { }

    /**
     * [2] 신규 등록용 생성자 (ID 미포함)
     * 관리자가 새로운 상품을 등록할 때 사용합니다.
     */
    public ProductsDTO(String productName, int requiredPoints, int stock, String imagePath, String description) {
        this.productName = productName;
        this.requiredPoints = requiredPoints;
        this.stock = stock;
        this.imagePath = imagePath;
        this.description = description;
    }

    /**
     * [3] 전체 정보 생성자 (DB 조회 및 수정용)
     */
    public ProductsDTO(String productId, String productName, int requiredPoints, 
                       int stock, String imagePath, String description) {
        this.productId = productId;
        this.productName = productName;
        this.requiredPoints = requiredPoints;
        this.stock = stock;
        this.imagePath = imagePath;
        this.description = description;
    }

    // --- 비즈니스 로직 편의 메서드 ---

    /**
     * 현재 상품이 품절 상태인지 확인
     */
    public boolean isSoldOut() {
        return this.stock <= 0;
    }

    /**
     * 재고가 설정한 임계값보다 낮은지 확인 (관리자 대시보드 알림용)
     * @param threshold 기준 수량 (예: 5)
     */
    public boolean isLowStock(int threshold) {
        return this.stock > 0 && this.stock < threshold;
    }

    /**
     * 재고 수량을 안전하게 변경 (0 미만 방지)
     */
    public void changeStock(int amount) {
        this.stock = Math.max(0, this.stock + amount);
    }

    /**
     * 재고 상태를 문자열로 반환 (UI 표시용)
     */
    public String getStockStatusString() {
        if (isSoldOut()) return "❌ 품절";
        if (isLowStock(5)) return "⚠️ 품절임박 (" + stock + ")";
        return "✅ 재고여유 (" + stock + ")";
    }

    // --- Getter & Setter ---

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getRequiredPoints() { return requiredPoints; }
    public void setRequiredPoints(int requiredPoints) { 
        this.requiredPoints = Math.max(0, requiredPoints); 
    }

    public int getStock() { return stock; }
    public void setStock(int stock) { 
        this.stock = Math.max(0, stock); 
    }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // --- 유틸리티 메서드 ---

    @Override
    public String toString() {
        return String.format("ProductsDTO [ID=%s, 이름=%s, 포인트=%d, 재고=%d, 설명=%s]", 
                productId, productName, requiredPoints, stock, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductsDTO that = (ProductsDTO) o;
        return Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}