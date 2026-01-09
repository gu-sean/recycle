package recycle;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;
import java.io.File;

import db.RecycleDB;
import db.DAO.ProductsDAO;
import db.DTO.ProductsDTO;
import db.DAO.UserDAO;
import db.DTO.UserDTO;
import db.DAO.PointLogDAO;

public class ProductWindow extends JPanel { 

    private List<ProductsDTO> productsData; 
    private UserDTO currentUser; 

    private JPanel productListPanel;
    private JLabel nameLabel;
    private JLabel pointsLabel;
    private JLabel stockLabel;   
    private JLabel imageLabel;     
    private JTextArea guideArea;
    private JButton purchaseButton;
    private ProductsDTO selectedProduct;

    private static final Color LIGHT_BLUE = new Color(204, 229, 255); 

    public ProductWindow(UserDTO user) {
        this.currentUser = user;
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        productListPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        loadProducts(); 

        JScrollPane scrollPane = new JScrollPane(productListPanel);
        scrollPane.setPreferredSize(new Dimension(280, 0));
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        JPanel purchasePanel = new JPanel(new BorderLayout());
        
        JPanel topInfoPanel = new JPanel(new GridLayout(3, 1));
        topInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        nameLabel = new JLabel("상품을 선택해주세요", JLabel.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        
        pointsLabel = new JLabel("", JLabel.CENTER);
        pointsLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        pointsLabel.setForeground(new Color(0, 102, 204));
        
        stockLabel = new JLabel("", JLabel.CENTER);
        stockLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        
        topInfoPanel.add(nameLabel);
        topInfoPanel.add(pointsLabel);
        topInfoPanel.add(stockLabel);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        imageLabel = new JLabel("이미지 없음", JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 200));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        guideArea = new JTextArea();
        guideArea.setEditable(false);
        guideArea.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        guideArea.setLineWrap(true);
        guideArea.setWrapStyleWord(true);
        
        centerPanel.add(imageLabel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(guideArea), BorderLayout.CENTER);

        purchaseButton = new JButton("구매하기");
        purchaseButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        purchaseButton.setPreferredSize(new Dimension(0, 70));
        purchaseButton.setBackground(new Color(255, 235, 59)); 
        purchaseButton.setEnabled(false); 
        purchaseButton.addActionListener(e -> handlePurchase());

        purchasePanel.add(topInfoPanel, BorderLayout.NORTH);
        purchasePanel.add(centerPanel, BorderLayout.CENTER);
        purchasePanel.add(purchaseButton, BorderLayout.SOUTH);

        add(purchasePanel, BorderLayout.CENTER);
    }

    public void loadProducts() {
        ProductsDAO dao = new ProductsDAO();
        productsData = dao.getAllProducts();

        productListPanel.removeAll();
        if (productsData == null || productsData.isEmpty()) {
            productListPanel.add(new JLabel("등록된 상품이 없습니다.", JLabel.CENTER));
        } else {
            for (ProductsDTO product : productsData) {
                String status = (product.getStock() <= 0) ? " [품절]" : "";
                String buttonText = "<html><center><b>" + product.getProductName() + "</b>" + status + "<br>"
                                   + product.getRequiredPoints() + " P</center></html>";
                JButton productBtn = new JButton(buttonText);
                productBtn.setPreferredSize(new Dimension(180, 80));
                productBtn.setBackground(product.getStock() <= 0 ? Color.LIGHT_GRAY : LIGHT_BLUE);
                productBtn.addActionListener(e -> updatePurchasePanel(product));
                productListPanel.add(productBtn);
            }
        }
        productListPanel.revalidate();
        productListPanel.repaint();
    }

    private void updatePurchasePanel(ProductsDTO product) {
        this.selectedProduct = product;
        nameLabel.setText(product.getProductName());
        pointsLabel.setText("필요 포인트: " + product.getRequiredPoints() + " P");
        stockLabel.setText("남은 재고: " + product.getStock() + " 개");

        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            File imgFile = new File(product.getImagePath());
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(product.getImagePath());
                Image img = icon.getImage().getScaledInstance(250, 180, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(img));
                imageLabel.setText("");
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText("이미지를 찾을 수 없습니다.");
            }
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("이미지 미등록 상품");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" [ 상세 설명 ]\n");
        sb.append(product.getDescription() != null ? product.getDescription() : "설명이 없습니다.").append("\n\n");
        sb.append(" --------------------------------------------\n");
        sb.append(" 현재 나의 보유 포인트: ").append(currentUser.getBalancePoints()).append(" P");

        guideArea.setText(sb.toString());

        if (product.getStock() <= 0) {
            purchaseButton.setText("품절 되었습니다");
            purchaseButton.setEnabled(false);
        } else {
            purchaseButton.setText("구매하기");
            purchaseButton.setEnabled(true);
        }
    }

    private void handlePurchase() {
        if (currentUser == null || selectedProduct == null) return;

        int userPoint = currentUser.getBalancePoints(); 
        int price = selectedProduct.getRequiredPoints(); 

        if (userPoint < price) {
            JOptionPane.showMessageDialog(this, "포인트가 부족합니다.", "구매 불가", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedProduct.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, "재고가 부족합니다.", "품절", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            selectedProduct.getProductName() + "을(를) 구매하시겠습니까?", "구매 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = RecycleDB.connect()) {
                conn.setAutoCommit(false); 

                try {
                
                    UserDAO uDao = new UserDAO();
                    currentUser.setBalancePoints(userPoint - price);
                    uDao.updateUserPoint(currentUser); 

                    selectedProduct.setStock(selectedProduct.getStock() - 1);
                    ProductsDAO pDao = new ProductsDAO();
                    pDao.updateProduct(selectedProduct); 

                    PointLogDAO logDAO = new PointLogDAO();
                    logDAO.insertSpendLog(conn, currentUser.getUserId(), selectedProduct.getProductName(), price);

                    conn.commit(); 
                    JOptionPane.showMessageDialog(this, "구매 완료! 감사합니다.");
                    
                    loadProducts(); 
                    updatePurchasePanel(selectedProduct); 

                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "구매 실패: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}