package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

import db.RecycleDB;
import db.DAO.ProductsDAO;
import db.DTO.ProductsDTO;
import db.DAO.UserDAO;
import db.DTO.UserDTO;
import db.DAO.PointLogDAO;

public class ProductWindow extends JPanel { 

    private List<ProductsDTO> productsData; 
    private UserDTO currentUser; 
    private final Runnable rankUpdateCallback; 

    private JPanel productListPanel;
    private JLabel nameLabel;
    private JLabel pointsLabel;
    private JLabel stockLabel;      
    private JLabel imageLabel;      
    private JTextArea guideArea;
    private JButton purchaseButton;
    private ProductsDTO selectedProduct;

    private static final Color BG_DARK = new Color(15, 12, 30);
    private static final Color POINT_PURPLE = new Color(130, 90, 255);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color CARD_BG = new Color(25, 25, 50);

    public ProductWindow(UserDTO user, Runnable rankUpdateCallback) {
        this.currentUser = user;
        this.rankUpdateCallback = rankUpdateCallback;

        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        JLabel titleLabel = new JLabel("Eco-Shop: 포인트 상점", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(POINT_CYAN);
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        productListPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        productListPanel.setBackground(BG_DARK);
        productListPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JScrollPane scrollPane = new JScrollPane(productListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        add(scrollPane, BorderLayout.CENTER);

        add(createDetailPanel(), BorderLayout.EAST);
        loadProducts();
    }

    private JPanel createDetailPanel() {
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setPreferredSize(new Dimension(320, 0));
        detailPanel.setBackground(CARD_BG);
        detailPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        imageLabel = new JLabel("상품을 선택하세요", SwingConstants.CENTER);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(220, 220));
        imageLabel.setMaximumSize(new Dimension(220, 220));
        imageLabel.setBorder(new LineBorder(POINT_PURPLE, 1));
        imageLabel.setForeground(Color.GRAY);

        nameLabel = new JLabel("-");
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        pointsLabel = new JLabel("가격: - P");
        pointsLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        pointsLabel.setForeground(POINT_CYAN);
        pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        stockLabel = new JLabel("재고: -");
        stockLabel.setForeground(Color.LIGHT_GRAY);
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        guideArea = new JTextArea("상세 설명");
        guideArea.setEditable(false);
        guideArea.setLineWrap(true);
        guideArea.setWrapStyleWord(true);
        guideArea.setBackground(CARD_BG);
        guideArea.setForeground(Color.WHITE);
        guideArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        purchaseButton = new JButton("구매하기");
        purchaseButton.setBackground(POINT_PURPLE);
        purchaseButton.setForeground(Color.WHITE);
        purchaseButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        purchaseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        purchaseButton.setEnabled(false);
        purchaseButton.addActionListener(e -> processPurchase());

        detailPanel.add(imageLabel);
        detailPanel.add(Box.createVerticalStrut(20));
        detailPanel.add(nameLabel);
        detailPanel.add(pointsLabel);
        detailPanel.add(stockLabel);
        detailPanel.add(Box.createVerticalStrut(15));
        detailPanel.add(new JScrollPane(guideArea) {{ setBorder(null); setOpaque(false); getViewport().setOpaque(false); }});
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.add(purchaseButton);

        return detailPanel;
    }

    private void loadProducts() {
        ProductsDAO dao = new ProductsDAO();
        productsData = dao.getAllProducts();
        renderProductList();
    }

    private void renderProductList() {
        productListPanel.removeAll();
        for (ProductsDTO product : productsData) {
            productListPanel.add(createProductCard(product));
        }
        productListPanel.revalidate();
        productListPanel.repaint();
    }

    private JPanel createProductCard(ProductsDTO product) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(new Color(50, 50, 80), 1));

        JLabel img = new JLabel(loadScaledImage(product.getImagePath(), 120, 120));
        img.setHorizontalAlignment(JLabel.CENTER);
        card.add(img, BorderLayout.CENTER);

        JLabel name = new JLabel(product.getProductName(), SwingConstants.CENTER);
        name.setForeground(Color.WHITE);
        name.setBorder(new EmptyBorder(5,0,5,0));
        card.add(name, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                updatePurchasePanel(product);
            }
        });
        return card;
    }

    private ImageIcon loadScaledImage(String path, int w, int h) {
        if (path == null || path.isEmpty()) return null;
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    private void updatePurchasePanel(ProductsDTO product) {
        if (product == null) return;
        this.selectedProduct = product;
        
        nameLabel.setText(product.getProductName());
        pointsLabel.setText(String.format("가격: %,d P", product.getRequiredPoints()));
        stockLabel.setText("재고: " + product.getStock() + "개");
        guideArea.setText(product.getDescription());

        refreshUserBalance();
        pointsLabel.setText(pointsLabel.getText() + String.format(" (내 포인트: %,d P)", currentUser.getBalancePoints()));

        ImageIcon icon = loadScaledImage(product.getImagePath(), 200, 200);
        if (icon != null) {
            imageLabel.setIcon(icon);
            imageLabel.setText("");
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("이미지 없음");
        }

        purchaseButton.setEnabled(product.getStock() > 0);
    }

    private void refreshUserBalance() {
        UserDAO uDao = new UserDAO();
        UserDTO updated = uDao.getUserById(currentUser.getUserId());
        if (updated != null) this.currentUser = updated;
    }

    private void processPurchase() {
        if (selectedProduct == null) return;

        refreshUserBalance();
        int price = selectedProduct.getRequiredPoints();
        if (currentUser.getBalancePoints() < price) {
            JOptionPane.showMessageDialog(this, "포인트가 부족합니다!");
            return;
        }

        int res = JOptionPane.showConfirmDialog(this, 
            selectedProduct.getProductName() + "을(를) 구매하시겠습니까?", "구매 확인", JOptionPane.YES_NO_OPTION);
        
        if (res == JOptionPane.YES_OPTION) {
            try (Connection conn = RecycleDB.connect()) {
                conn.setAutoCommit(false);
                try {
            
                    currentUser.setBalancePoints(currentUser.getBalancePoints() - price);
                    new UserDAO().updateUserPoint(conn, currentUser);

                    selectedProduct.setStock(selectedProduct.getStock() - 1);
                    new ProductsDAO().updateProduct(conn, selectedProduct);

                    new PointLogDAO().insertSpendLog(conn, currentUser.getUserId(), selectedProduct.getProductName(), price);

                    conn.commit();
                    
                    if (rankUpdateCallback != null) rankUpdateCallback.run();
                    JOptionPane.showMessageDialog(this, "정상적으로 구매되었습니다!");
                    
                    loadProducts(); 
                    
                    for(ProductsDTO p : productsData) {
                        if(p.getProductId() == selectedProduct.getProductId()) {
                            updatePurchasePanel(p);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "오류 발생: " + e.getMessage());
            }
        }
    }

    private static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { this.thumbColor = POINT_PURPLE; this.trackColor = BG_DARK; }
        @Override protected JButton createDecreaseButton(int orientation) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
        @Override protected JButton createIncreaseButton(int orientation) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
    }
}