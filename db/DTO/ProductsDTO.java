package db.DTO;

import java.util.Objects;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;
import java.io.Serializable;


public class ProductsDTO implements Serializable {
    private static final long serialVersionUID = 3L;

    private String productId;      // 상품 고유 번호
    private String productName;    // 상품 이름
    private String category;       // 상품 카테고리
    private int requiredPoints;    // 구매 필요 포인트
    private int stock;             // 현재 재고
    private String imagePath;      // 이미지 경로
    private String description;    // 상세 설명

    // 기본 이미지 경로 (이미지가 없을 경우 대체용)
    private static final String DEFAULT_IMAGE = "images/products/no_image.png";

    /** [1] 기본 생성자 */
    public ProductsDTO() { 
        this.category = "미분류";
        this.description = "상품 설명이 없습니다.";
    }

    /** [2] 신규 등록용 생성자 (ID 미포함) */
    public ProductsDTO(String productName, String category, int requiredPoints, int stock, String imagePath, String description) {
        this.productName = Objects.requireNonNullElse(productName, "이름 없는 상품");
        this.category = Objects.requireNonNullElse(category, "미분류");
        this.requiredPoints = Math.max(0, requiredPoints);
        this.stock = Math.max(0, stock);
        this.imagePath = imagePath;
        this.description = Objects.requireNonNullElse(description, "상품 설명이 없습니다.");
    }

    /** [3] 전체 정보 생성자 (DB 조회 및 수정용) */
    public ProductsDTO(String productId, String productName, String category, int requiredPoints, 
                       int stock, String imagePath, String description) {
        this(productName, category, requiredPoints, stock, imagePath, description);
        this.productId = productId;
    }

    // --- UI 및 비즈니스 로직 편의 메서드 ---

    /** 포인트 콤마 포맷팅 (예: "1,200 P") */
    public String getFormattedPoints() {
        return String.format("%,d P", requiredPoints);
    }

 
    public ImageIcon getImageIcon(int width, int height) {
        String path = (imagePath == null || imagePath.isEmpty()) ? DEFAULT_IMAGE : imagePath;
        File file = new File(path);
        
 
        if (!file.exists()) {
            return null; 
        }

        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    /** 재고 유무 확인 */
    public boolean isSoldOut() {
        return this.stock <= 0;
    }

    /** 재고 상태 텍스트 (UI Label 출력용) */
    public String getStockStatusString() {
        if (isSoldOut()) return "<html><font color='red'><b>품절 (SOLD OUT)</b></font></html>";
        return String.format("재고 %d개 남음", stock);
    }

    /** 구매 가능 여부 체크 (사용자 포인트와 비교) */
    public boolean canPurchase(int userBalance) {
        return !isSoldOut() && userBalance >= requiredPoints;
    }

    // --- Getter & Setter (안정성 강화) ---

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

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
        return String.format("[%s] %s | %s", category, productName, getFormattedPoints());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductsDTO)) return false;
        ProductsDTO that = (ProductsDTO) o;
        return Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

 
    public ProductsDTO copy() {
        return new ProductsDTO(productId, productName, category, requiredPoints, stock, imagePath, description);
    }
}