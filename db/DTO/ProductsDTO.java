package db.DTO;

import java.util.Objects;


public class ProductsDTO {
    private String productId;    
    private String productName;   
    private String category;      
    private int requiredPoints;   
    private int stock;            
    private String imagePath;     
    private String description;    

    public ProductsDTO() { 
        this.category = "미분류"; 
    }

  
    public ProductsDTO(String productName, String category, int requiredPoints, int stock, String imagePath, String description) {
        this.productName = productName;
        this.category = (category != null) ? category : "미분류";
        this.requiredPoints = requiredPoints;
        this.stock = stock;
        this.imagePath = imagePath;
        this.description = description;
    }

    
    public ProductsDTO(String productId, String productName, String category, int requiredPoints, 
                       int stock, String imagePath, String description) {
        this.productId = productId;
        this.productName = productName;
        this.category = (category != null) ? category : "미분류";
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

    public String getCategory() { return (category != null) ? category : "미분류"; }
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


    @Override
    public String toString() {
        return String.format("ProductsDTO [ID=%s, 이름=%s, 카테고리=%s, 포인트=%d, 재고=%d]", 
                productId, productName, category, requiredPoints, stock);
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

  
    public ProductsDTO copy() {
        return new ProductsDTO(productId, productName, category, requiredPoints, stock, imagePath, description);
    }
}