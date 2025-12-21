package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.net.URL;
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

    private JPanel productListPanel;
    private JLabel nameLabel;
    private JLabel pointsLabel;
    private JLabel stockLabel;      
    private JLabel imageLabel;      
    private JTextArea guideArea;
    private JButton purchaseButton;
    private ProductsDTO selectedProduct;

    // --- 네온 다크 퍼플 테마 색상 ---
    private static final Color BG_DARK = new Color(20, 15, 40);
    private static final Color BG_LIGHT = new Color(40, 45, 90);
    private static final Color POINT_PURPLE = new Color(150, 100, 255);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color CARD_BG = new Color(35, 30, 70);

    public ProductWindow(UserDTO user) {
        this.currentUser = user;
        setLayout(new BorderLayout(20, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 좌측 패널 (상품 목록) ---
        JPanel leftContainer = new JPanel(new BorderLayout(10, 10));
        leftContainer.setOpaque(false);
        leftContainer.setPreferredSize(new Dimension(300, 0));

        JLabel listTitle = new JLabel("포인트 상점", JLabel.LEFT);
        listTitle.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        listTitle.setForeground(POINT_CYAN);
        leftContainer.add(listTitle, BorderLayout.NORTH);

        productListPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        productListPanel.setBackground(BG_DARK);
        
        loadProducts(); // 데이터 로드

        JScrollPane scrollPane = new JScrollPane(productListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(BG_DARK);
        leftContainer.add(scrollPane, BorderLayout.CENTER);
        
        add(leftContainer, BorderLayout.WEST);

        // --- 우측 패널 (상세 정보 및 구매) ---
        JPanel detailPanel = new JPanel(new BorderLayout(15, 15));
        detailPanel.setBackground(CARD_BG);
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(POINT_PURPLE, 1), new EmptyBorder(20, 20, 20, 20)));
        
        // 1. 상단 정보 (이름, 포인트)
        JPanel topInfoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        topInfoPanel.setOpaque(false);
        
        nameLabel = new JLabel("상품을 선택해주세요", JLabel.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        nameLabel.setForeground(Color.WHITE);
        
        pointsLabel = new JLabel("", JLabel.CENTER);
        pointsLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        pointsLabel.setForeground(POINT_CYAN);
        
        stockLabel = new JLabel("", JLabel.CENTER);
        stockLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        stockLabel.setForeground(Color.LIGHT_GRAY);
        
        topInfoPanel.add(nameLabel);
        topInfoPanel.add(pointsLabel);
        topInfoPanel.add(stockLabel);
        
        // 2. 중앙 영역 (이미지 + 설명)
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);
        
        imageLabel = new JLabel("상세 이미지가 표시됩니다", JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(350, 220));
        imageLabel.setBorder(new LineBorder(new Color(80, 80, 130), 1));
        imageLabel.setForeground(Color.GRAY);
        
        guideArea = new JTextArea();
        guideArea.setEditable(false);
        guideArea.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        guideArea.setBackground(new Color(25, 20, 50));
        guideArea.setForeground(Color.WHITE);
        guideArea.setLineWrap(true);
        guideArea.setWrapStyleWord(true);
        guideArea.setMargin(new Insets(15, 15, 15, 15));
        
        JScrollPane guideScroll = new JScrollPane(guideArea);
        guideScroll.setBorder(new LineBorder(new Color(80, 80, 130), 1));

        centerPanel.add(imageLabel, BorderLayout.NORTH);
        centerPanel.add(guideScroll, BorderLayout.CENTER);

        // 3. 하단 구매 버튼
        purchaseButton = new JButton("구매하기");
        purchaseButton.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        purchaseButton.setPreferredSize(new Dimension(0, 70));
        purchaseButton.setBackground(POINT_PURPLE);
        purchaseButton.setForeground(Color.WHITE);
        purchaseButton.setFocusPainted(false);
        purchaseButton.setBorderPainted(false);
        purchaseButton.setEnabled(false); 
        purchaseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        purchaseButton.addActionListener(e -> handlePurchase());

        detailPanel.add(topInfoPanel, BorderLayout.NORTH);
        detailPanel.add(centerPanel, BorderLayout.CENTER);
        detailPanel.add(purchaseButton, BorderLayout.SOUTH);

        add(detailPanel, BorderLayout.CENTER);
    }

    public void loadProducts() {
        ProductsDAO dao = new ProductsDAO();
        productsData = dao.getAllProducts();

        productListPanel.removeAll();
        if (productsData == null || productsData.isEmpty()) {
            JLabel emptyLabel = new JLabel("등록된 상품이 없습니다.", JLabel.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            productListPanel.add(emptyLabel);
        } else {
            for (ProductsDTO product : productsData) {
                productListPanel.add(createProductButton(product));
            }
        }
        productListPanel.revalidate();
        productListPanel.repaint();
    }

    private JButton createProductButton(ProductsDTO product) {
        boolean isOutOfStock = product.getStock() <= 0;
        String statusText = isOutOfStock ? " <font color='red'>[품절]</font>" : "";
        String text = "<html><div style='text-align: left; padding-left: 10px;'>"
                    + "<b style='font-size: 14px; color: white;'>" + product.getProductName() + "</b>" + statusText + "<br>"
                    + "<span style='color: #00fff0;'>" + product.getRequiredPoints() + " P</span>"
                    + "</div></html>";

        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(250, 80));
        btn.setBackground(CARD_BG);
        btn.setForeground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(80, 80, 130), 1));
        
        if (isOutOfStock) {
            btn.setBackground(new Color(45, 45, 60));
        }

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!isOutOfStock) btn.setBorder(new LineBorder(POINT_CYAN, 2));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBorder(new LineBorder(new Color(80, 80, 130), 1));
            }
        });

        btn.addActionListener(e -> updatePurchasePanel(product));
        return btn;
    }

    private void updatePurchasePanel(ProductsDTO product) {
        this.selectedProduct = product;
        nameLabel.setText(product.getProductName());
        pointsLabel.setText(product.getRequiredPoints() + " P");
        stockLabel.setText("재고: " + product.getStock() + " 개");

        // 이미지 로드 (파일 경로 기반)
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            File imgFile = new File(product.getImagePath());
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(product.getImagePath());
                Image img = icon.getImage().getScaledInstance(320, 200, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(img));
                imageLabel.setText("");
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText("이미지 파일을 찾을 수 없습니다.");
            }
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("이미지 미등록 상품");
        }

        // 설명창 업데이트
        StringBuilder sb = new StringBuilder();
        sb.append(" [ 상세 설명 ]\n\n");
        sb.append(product.getDescription() != null ? product.getDescription() : "설명이 등록되지 않았습니다.").append("\n\n");
        sb.append(" --------------------------------------------\n");
        sb.append(" 현재 보유 포인트: ").append(currentUser.getBalancePoints()).append(" P");

        guideArea.setText(sb.toString());

        // 버튼 상태 업데이트
        if (product.getStock() <= 0) {
            purchaseButton.setText("품절 되었습니다");
            purchaseButton.setBackground(new Color(80, 80, 80));
            purchaseButton.setEnabled(false);
        } else {
            purchaseButton.setText("구매하기");
            purchaseButton.setBackground(POINT_PURPLE);
            purchaseButton.setEnabled(true);
        }
    }

    private void handlePurchase() {
        if (currentUser == null || selectedProduct == null) return;

        int userPoint = currentUser.getBalancePoints(); 
        int price = selectedProduct.getRequiredPoints(); 

        if (userPoint < price) {
            JOptionPane.showMessageDialog(this, "포인트가 부족하여 구매할 수 없습니다.", "포인트 부족", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            selectedProduct.getProductName() + "을(를) 구매하시겠습니까?\n차감 포인트: " + price + " P", 
            "구매 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = RecycleDB.connect()) {
                conn.setAutoCommit(false); 

                try {
                    // 1. 유저 포인트 차감
                    UserDAO uDao = new UserDAO();
                    currentUser.setBalancePoints(userPoint - price);
                    uDao.updateUserPoint(currentUser); 

                    // 2. 상품 재고 차감
                    selectedProduct.setStock(selectedProduct.getStock() - 1);
                    ProductsDAO pDao = new ProductsDAO();
                    pDao.updateProduct(selectedProduct); 

                    // 3. 로그 기록
                    PointLogDAO logDAO = new PointLogDAO();
                    logDAO.insertSpendLog(conn, currentUser.getUserId(), selectedProduct.getProductName(), price);

                    conn.commit(); 
                    JOptionPane.showMessageDialog(this, "정상적으로 구매되었습니다!", "구매 완료", JOptionPane.INFORMATION_MESSAGE);
                    
                    loadProducts(); // 목록 갱신
                    updatePurchasePanel(selectedProduct); // 현재 화면 갱신

                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "구매 처리 중 오류가 발생했습니다: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}