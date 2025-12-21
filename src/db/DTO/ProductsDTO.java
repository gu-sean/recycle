package db.DTO;

import java.util.Objects;

/**
 * 상품 정보를 관리하는 데이터 전송 객체 (DTO)
 * 기존 필드: ID, 이름, 포인트
 * 추가 필드: 재고, 이미지 경로, 상세 설명
 */
public class ProductsDTO {
    private String productId;      // 상품 고유 번호 (UUID 등)
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
     * ID는 DAO에서 UUID로 자동 생성되므로 포함하지 않습니다.
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
     * DB에서 데이터를 불러오거나 기존 정보를 수정할 때 사용합니다.
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

    // --- Getter & Setter ---

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getRequiredPoints() { return requiredPoints; }
    public void setRequiredPoints(int requiredPoints) { this.requiredPoints = requiredPoints; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // --- 유틸리티 메서드 ---

    /**
     * 객체의 정보를 문자열로 반환 (디버깅용)
     */
    @Override
    public String toString() {
        return String.format("ProductsDTO [ID=%s, 이름=%s, 포인트=%d, 재고=%d, 설명=%s]", 
                productId, productName, requiredPoints, stock, description);
    }

    /**
     * 상품 ID를 기준으로 동일 객체 여부를 판단
     */
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