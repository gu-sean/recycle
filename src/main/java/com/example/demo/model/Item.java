package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products") // SQL 테이블명에 맞춤
public class Item {

    @Id
    @Column(name = "PRODUCT_ID") // SQL: PRODUCT_ID
    private String itemId; // Java에서는 기존처럼 itemId로 유지 (ID가 String임에 주의)

    @Column(name = "PRODUCT_NAME", nullable = false) // SQL: PRODUCT_NAME
    private String itemName;

    @Column(name = "REQUIRED_POINTS", nullable = false) // SQL: REQUIRED_POINTS
    private Integer itemPrice;

    @Column(name = "STOCK") // SQL: STOCK
    private Integer stock;

    @Column(name = "IMAGE_PATH") // SQL: IMAGE_PATH (이모지 대신 경로 사용 시)
    private String itemImagePath;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT") // SQL: DESCRIPTION
    private String itemDesc;

    @Column(name = "CATEGORY") // SQL: CATEGORY
    private String category;

    // --- Getter & Setter ---
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getItemPrice() { return itemPrice; }
    public void setItemPrice(Integer itemPrice) { this.itemPrice = itemPrice; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getItemImagePath() { return itemImagePath; }
    public void setItemImagePath(String itemImagePath) { this.itemImagePath = itemImagePath; }

    public String getItemDesc() { return itemDesc; }
    public void setItemDesc(String itemDesc) { this.itemDesc = itemDesc; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}