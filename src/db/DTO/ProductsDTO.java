package db.DTO;

public class ProductsDTO {
    private String productId;
    private String productName;
    private int requiredPoints;

 
    public ProductsDTO() { }


    public ProductsDTO(String productId, String productName, int requiredPoints) {
        this.productId = productId;
        this.productName = productName;
        this.requiredPoints = requiredPoints;
    }



    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getRequiredPoints() {
        return requiredPoints;
    }

    public void setRequiredPoints(int requiredPoints) {
        this.requiredPoints = requiredPoints;
    }


    @Override
    public String toString() {
        return "ProductsDTO [상품번호=" + productId + ", 상품명=" + productName + ", 필요포인트=" + requiredPoints + "]";
    }
}