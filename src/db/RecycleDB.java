package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;


public class RecycleDB {

    private static final String DB_URL ="jdbc:mysql://localhost:3306/recycle?serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "fjf0301!";

  
    public static Connection connect() throws SQLException {
        try {
  
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
 
            JOptionPane.showMessageDialog(null, "MySQL 드라이버를 찾을 수 없습니다. (JAR 파일 확인 필요)", "DB 연결 오류", JOptionPane.ERROR_MESSAGE);
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}