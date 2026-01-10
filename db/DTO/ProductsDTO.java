package db.DTO;

import java.util.Objects;


public class ProductsDTO {
    private String productId;     
    private String productName;   
    private int requiredPoints; 
    private int stock;            
    private String imagePath;    
    private String description;    

    public ProductsDTO() { }

    
    public ProductsDTO(String productName, int requiredPoints, int stock, String imagePath, String description) {
        this.productName = productName;
        this.requiredPoints = requiredPoints;
        this.stock = stock;
        this.imagePath = imagePath;
        this.description = description;
    }

   
    public ProductsDTO(String productId, String productName, int requiredPoints, 
                       int stock, String imagePath, String description) {
        this.productId = productId;
        this.productName = productName;
        this.requiredPoints = requiredPoints;
        this.stock = stock;
        this.imagePath = imagePath;
        this.description = description;
    }

   
    public boolean isSoldOut() {
        return this.stock <= 0;
    }

    
    public boolean isLowStock(int threshold) {
        return this.stock > 0 && this.stock < threshold;
    }

  
    public void changeStock(int amount) {
        this.stock = Math.max(0, this.stock + amount);
    }

   
    public String getStockStatusString() {
        if (isSoldOut()) return "❌ 품절";
        if (isLowStock(5)) return "⚠️ 품절임박 (" + stock + ")";
        return "✅ 재고여유 (" + stock + ")";
    }


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


    @Override
    public String toString() {
        return String.format("ProductsDTO [ID=%s, 이름=%s, 포인트=%d, 재고=%d, 경로=%s]", 
                productId, productName, requiredPoints, stock, imagePath);
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