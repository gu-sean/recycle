package db.DAO;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import db.RecycleDB; 
import db.DTO.ProductsDTO;

/**
 * 상품 관리 및 상점 기능을 담당하는 데이터 접근 객체 (DAO)
 */
public class ProductsDAO {

    /**
     * [0] 테이블 초기화
     */
    public static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS PRODUCTS (" +
                     "  PRODUCT_ID VARCHAR(50) NOT NULL," +
                     "  PRODUCT_NAME VARCHAR(100) NOT NULL," +
                     "  REQUIRED_POINTS INT NOT NULL," +
                     "  STOCK INT DEFAULT 0," +                
                     "  IMAGE_PATH VARCHAR(255)," +            
                     "  DESCRIPTION TEXT," +                    
                     "  PRIMARY KEY (PRODUCT_ID)" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ PRODUCTS 테이블 초기화 완료.");
        } catch (SQLException e) {
            System.err.println("❌ PRODUCTS 테이블 생성 오류: " + e.getMessage());
        }
    }

    /**
     * [1] 전체 상품 목록 조회
     */
    public List<ProductsDTO> getAllProducts() {
        List<ProductsDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCTS ORDER BY PRODUCT_NAME ASC";

        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToDTO(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ 상품 목록 조회 오류: " + e.getMessage());
        }
        return list; 
    }

    /**
     * [2] 특정 상품 상세 조회
     */
    public ProductsDTO getProductById(String productId) throws SQLException {
        String sql = "SELECT * FROM PRODUCTS WHERE PRODUCT_ID = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDTO(rs);
                }
            }
        }
        return null;
    }

    /**
     * [3] 관리자 기능: 새 상품 등록
     */
    public boolean insertProduct(ProductsDTO product) throws SQLException {
        String sql = "INSERT INTO PRODUCTS (PRODUCT_ID, PRODUCT_NAME, REQUIRED_POINTS, STOCK, IMAGE_PATH, DESCRIPTION) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String uniqueID = (product.getProductId() == null || product.getProductId().isEmpty()) 
                              ? UUID.randomUUID().toString().substring(0, 8) 
                              : product.getProductId();
            
            pstmt.setString(1, uniqueID);
            pstmt.setString(2, product.getProductName());
            pstmt.setInt(3, product.getRequiredPoints());
            pstmt.setInt(4, product.getStock());
            pstmt.setString(5, product.getImagePath());
            pstmt.setString(6, product.getDescription());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * [4-1] ⭐ 상품 수정 (트랜잭션 지원용)
     * ProductWindow의 구매 로직 등에서 외부 Connection을 사용할 때 호출합니다.
     */
    public void updateProduct(Connection conn, ProductsDTO product) throws SQLException {
        String sql = "UPDATE PRODUCTS SET PRODUCT_NAME = ?, REQUIRED_POINTS = ?, STOCK = ?, " +
                     "IMAGE_PATH = ?, DESCRIPTION = ? WHERE PRODUCT_ID = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getProductName());
            pstmt.setInt(2, product.getRequiredPoints());
            pstmt.setInt(3, product.getStock());
            pstmt.setString(4, product.getImagePath());
            pstmt.setString(5, product.getDescription());
            pstmt.setString(6, product.getProductId());
            
            pstmt.executeUpdate();
        }
    }

    /**
     * [4-2] 상품 수정 (기존 단일 작업용)
     * 관리자 창 등에서 직접 수정할 때 사용합니다.
     */
    public boolean updateProduct(ProductsDTO product) throws SQLException {
        try (Connection conn = RecycleDB.connect()) {
            updateProduct(conn, product);
            return true;
        } catch (SQLException e) {
            System.err.println("❌ 상품 수정 오류: " + e.getMessage());
            throw e;
        }
    }

    /**
     * [5] 관리자 기능: 상품 삭제
     */
    public boolean deleteProduct(String productId) throws SQLException {
        String sql = "DELETE FROM PRODUCTS WHERE PRODUCT_ID = ?";
        
        try (Connection conn = RecycleDB.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * [Helper] ResultSet 데이터를 DTO로 변환하는 핵심 로직
     */
    private ProductsDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        ProductsDTO product = new ProductsDTO();
        product.setProductId(rs.getString("PRODUCT_ID"));
        product.setProductName(rs.getString("PRODUCT_NAME"));
        product.setRequiredPoints(rs.getInt("REQUIRED_POINTS"));
        product.setStock(rs.getInt("STOCK"));
        product.setDescription(rs.getString("DESCRIPTION"));

        // 🖼️ 이미지 경로 처리 로직
        String rawPath = rs.getString("IMAGE_PATH"); 
        
        if (rawPath != null && !rawPath.isEmpty()) {
            // 이미 절대 경로(D:\... 또는 C:\...)인 경우에는 그대로 사용
            if (new File(rawPath).isAbsolute()) {
                product.setImagePath(rawPath);
            } else {
                // 상대 경로인 경우 프로젝트 루트 결합
                String absolutePath = System.getProperty("user.dir") + File.separator + "src" + 
                                      File.separator + "main" + File.separator + "webapp" + rawPath;
                product.setImagePath(absolutePath);
            }
        }

        return product;
    }
}