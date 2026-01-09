package db.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import db.DTO.ProductsDTO;

public class ProductsDAO {

    private static final String DB_URL ="";
    private static final String DB_ID = "";
    private static final String DB_PASSWORD = "";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC 드라이버 로드 실패!");
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL, DB_ID, DB_PASSWORD);
    }

    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 
    public List<ProductsDTO> getAllProducts() {
        List<ProductsDTO> list = new ArrayList<>();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

 
        String sql = "SELECT PRODUCT_ID, PRODUCT_NAME, REQUIRED_POINTS FROM PRODUCTS "
                   + "WHERE PRODUCT_NAME NOT IN ('☕ 재활용 커피 쿠폰', '🌱 친환경 에코백', '📚 도서 상품권 (1만원)', '🌳 나무 심기 기부 (1,000 P)') "
                   + "ORDER BY PRODUCT_NAME ASC";

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
       
                ProductsDTO product = new ProductsDTO(
                    rs.getString("PRODUCT_ID"), 	
                    rs.getString("PRODUCT_NAME"), 	
                    rs.getInt("REQUIRED_POINTS") 		
                );

                list.add(product);
            }
        } catch (SQLException e) {
            System.err.println("상품 목록 조회 중 SQL 오류 발생:");
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }

        return list; 
    }
}