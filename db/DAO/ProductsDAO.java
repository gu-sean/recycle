package db.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import db.RecycleDB; 
import db.DTO.ProductsDTO;


public class ProductsDAO {

    /**
     * [1] 테이블 초기화
     */
    public static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS PRODUCTS (" +
                     "  PRODUCT_ID VARCHAR(50) NOT NULL," +
                     "  CATEGORY VARCHAR(50) DEFAULT '미분류'," +
                     "  PRODUCT_NAME VARCHAR(100) NOT NULL," +
                     "  REQUIRED_POINTS INT NOT NULL DEFAULT 0," +
                     "  STOCK INT DEFAULT 0 CHECK (STOCK >= 0)," +                 
                     "  IMAGE_PATH VARCHAR(255)," +            
                     "  DESCRIPTION TEXT," +                    
                     "  PRIMARY KEY (PRODUCT_ID)" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("❌ PRODUCTS 테이블 초기화 오류: " + e.getMessage());
        }
    }

    /**
     * [2] 전체 상품 목록 조회
     */
    public List<ProductsDTO> getAllProducts() {
        List<ProductsDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTS ORDER BY CATEGORY ASC, PRODUCT_NAME ASC";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDTO(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ 상품 조회 오류: " + e.getMessage());
        }
        return list; 
    }

    /**
     * [3] 신규 상품 등록
     */
    public boolean insertProduct(ProductsDTO product) throws SQLException {
        String sql = "INSERT INTO PRODUCTS (PRODUCT_ID, CATEGORY, PRODUCT_NAME, REQUIRED_POINTS, STOCK, IMAGE_PATH, DESCRIPTION) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (product.getProductId() == null || product.getProductId().isEmpty()) {
                product.setProductId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }
            
            pstmt.setString(1, product.getProductId());
            pstmt.setString(2, product.getCategory());
            pstmt.setString(3, product.getProductName());
            pstmt.setInt(4, product.getRequiredPoints());
            pstmt.setInt(5, product.getStock());
            pstmt.setString(6, product.getImagePath());
            pstmt.setString(7, product.getDescription());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * [4] 상품 정보 수정 (관리자용)
     */
    public boolean updateProduct(ProductsDTO product) throws SQLException {
        String sql = "UPDATE PRODUCTS SET CATEGORY=?, PRODUCT_NAME=?, REQUIRED_POINTS=?, STOCK=?, " +
                     "IMAGE_PATH=?, DESCRIPTION=? WHERE PRODUCT_ID=?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setProductParams(pstmt, product);
            pstmt.setString(7, product.getProductId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * [5] 구매 시 재고 차감 
     */
    public boolean reduceStock(Connection conn, String productId, int quantity) throws SQLException {
        String sql = "UPDATE PRODUCTS SET STOCK = STOCK - ? WHERE PRODUCT_ID = ? AND STOCK >= ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, productId);
            pstmt.setInt(3, quantity);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * [6] 특정 상품 상세 조회
     */
    public ProductsDTO getProductById(String productId) {
        String sql = "SELECT * FROM PRODUCTS WHERE PRODUCT_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToDTO(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [7] 상품 삭제
     */
    public boolean deleteProduct(String productId) throws SQLException {
        String sql = "DELETE FROM PRODUCTS WHERE PRODUCT_ID = ?";
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            return pstmt.executeUpdate() > 0;
        }
    }



    private void setProductParams(PreparedStatement pstmt, ProductsDTO product) throws SQLException {
        pstmt.setString(1, product.getCategory());
        pstmt.setString(2, product.getProductName());
        pstmt.setInt(3, product.getRequiredPoints());
        pstmt.setInt(4, product.getStock());
        pstmt.setString(5, product.getImagePath());
        pstmt.setString(6, product.getDescription());
    }

    private ProductsDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        ProductsDTO product = new ProductsDTO();
        product.setProductId(rs.getString("PRODUCT_ID"));
        product.setCategory(rs.getString("CATEGORY"));
        product.setProductName(rs.getString("PRODUCT_NAME"));
        product.setRequiredPoints(rs.getInt("REQUIRED_POINTS"));
        product.setStock(rs.getInt("STOCK"));
        product.setImagePath(rs.getString("IMAGE_PATH"));
        product.setDescription(rs.getString("DESCRIPTION"));
        return product;
    }
}