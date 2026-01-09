package recycle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import db.DAO.ProductsDAO;
import db.DTO.ProductsDTO;
import db.DAO.UserDAO;
import db.DTO.UserDTO;
import db.DAO.PointLogDAO;
import db.DTO.PointLogDTO;

public class ProductWindow extends JPanel { 
    private List<ProductsDTO> productsData; 
    private UserDTO currentUser; 

    private JLabel nameLabel;
    private JLabel pointsLabel;
    private JTextArea guideArea;
    private JButton purchaseButton;
    private JButton historyButton; 
    private ProductsDTO selectedProduct;

    private static final Color LIGHT_BLUE = new Color(204, 229, 255); 
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("", "", "!");
    }

    public ProductWindow(UserDTO user) {
        this.currentUser = user;
        setLayout(new BorderLayout());

        ProductsDAO dao = new ProductsDAO();
        productsData = dao.getAllProducts();

        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel productListPanel = new JPanel(new GridLayout(0, 1, 5, 5));

        if (productsData == null || productsData.isEmpty()) {
            productListPanel.add(new JLabel("등록된 상품이 없습니다.", JLabel.CENTER));
        } else {
            for (ProductsDTO product : productsData) {
                String buttonText = "<html><center><b>" + product.getProductName() + "</b><br>"
                                   + product.getRequiredPoints() + " P</center></html>";
                JButton productBtn = new JButton(buttonText);
                productBtn.setPreferredSize(new Dimension(180, 60));
                productBtn.setBackground(LIGHT_BLUE); 
                productBtn.addActionListener(e -> updatepurchasePanel(product));
                productListPanel.add(productBtn);
            }
        }

        JScrollPane scrollPane = new JScrollPane(productListPanel);
        scrollPane.setPreferredSize(new Dimension(220, 0));
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        historyButton = new JButton("내 포인트 내역 보기");
        historyButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        historyButton.setPreferredSize(new Dimension(0, 40));
        historyButton.addActionListener(e -> showPointHistory());
        leftPanel.add(historyButton, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        JPanel purchasePanel = new JPanel(new BorderLayout());
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        
        nameLabel = new JLabel("상품을 선택해주세요", JLabel.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        
        pointsLabel = new JLabel("", JLabel.CENTER);
        pointsLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        pointsLabel.setForeground(Color.BLUE);
        
        infoPanel.add(nameLabel);
        infoPanel.add(pointsLabel);
        purchasePanel.add(infoPanel, BorderLayout.NORTH);

        guideArea = new JTextArea();
        guideArea.setEditable(false);
        guideArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        guideArea.setMargin(new Insets(20, 20, 20, 20));
        purchasePanel.add(new JScrollPane(guideArea), BorderLayout.CENTER);

        purchaseButton = new JButton("구매하기");
        purchaseButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        purchaseButton.setPreferredSize(new Dimension(0, 60));
        purchaseButton.setEnabled(false); 
        purchaseButton.addActionListener(e -> handlePurchase());
        purchasePanel.add(purchaseButton, BorderLayout.SOUTH);

        add(purchasePanel, BorderLayout.CENTER);
    }

    private void showPointHistory() {
        if (currentUser == null) return;

        String[] columns = {"일시", "구분", "상세 내용", "포인트"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } 
        };

        try (Connection conn = getConnection()) {
            PointLogDAO logDAO = new PointLogDAO();
            List<PointLogDTO> logs = logDAO.getPointLogsByUserId(conn, currentUser.getUserId());
            for (PointLogDTO log : logs) {
                Object[] row = {
                    log.getFormattedTimestamp(), 
                    log.getTypeKorean(),       
                    log.getDetail(),
                    log.getFormattedAmount()  
                };
                model.addRow(row);
            }

            JTable table = new JTable(model);
            table.setRowHeight(25);
            JOptionPane.showMessageDialog(this, new JScrollPane(table), 
                currentUser.getUserId() + "님의 활동 내역", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "내역 로드 실패: " + e.getMessage());
        }
    }

    private void updatepurchasePanel(ProductsDTO product) {
        this.selectedProduct = product;
        nameLabel.setText(product.getProductName());
        pointsLabel.setText("필요 포인트: " + product.getRequiredPoints() + " P");

        StringBuilder sb = new StringBuilder();
        sb.append(" [ ").append(product.getProductName()).append(" ]\n\n");
        sb.append(" - 환경을 생각하는 친환경 교환 상품입니다.\n");
        sb.append(" - 결제 시 보유 포인트에서 즉시 차감됩니다.\n\n");
        sb.append(" --------------------------------------------\n");
        sb.append(" 현재 나의 보유 포인트: ").append(currentUser.getBalancePoints()).append(" P");

        guideArea.setText(sb.toString());
        purchaseButton.setEnabled(true);
    }

    private void handlePurchase() {
        if (currentUser == null || selectedProduct == null) return;

        int userPoint = currentUser.getBalancePoints(); 
        int price = selectedProduct.getRequiredPoints(); 

        if (userPoint < price) {
            JOptionPane.showMessageDialog(this, "포인트가 부족하여 구매할 수 없습니다.", "잔액 부족", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            selectedProduct.getProductName() + "을(를) 구매하시겠습니까?\n차감 포인트: " + price + " P", 
            "구매 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false); 
                try {
                    currentUser.setBalancePoints(userPoint - price);
                    UserDAO uDao = new UserDAO();
                    uDao.updateUserPoint(currentUser); 

                    PointLogDAO logDAO = new PointLogDAO();
                    logDAO.insertSpendLog(conn, currentUser.getUserId(), selectedProduct.getProductName(), price);

                    conn.commit(); 
                    
                    JOptionPane.showMessageDialog(this, "구매 완료! 남은 포인트: " + currentUser.getBalancePoints() + " P");
                    updatepurchasePanel(selectedProduct); 

                } catch (SQLException ex) {
                    conn.rollback(); 
                    throw ex;
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "처리 중 오류 발생: " + e.getMessage());
                currentUser.setBalancePoints(userPoint); 
            }
        }
    }
}