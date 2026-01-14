package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import db.RecycleDB;
import db.DAO.ProductsDAO;
import db.DTO.ProductsDTO;
import db.DAO.UserDAO;
import db.DTO.UserDTO;
import db.DAO.PointLogDAO;


public class ProductWindow extends JPanel {

    private List<ProductsDTO> productsData = new ArrayList<>();
    private UserDTO currentUser;
    private final Runnable rankUpdateCallback;

    private JPanel productListPanel;
    private JLabel nameLabel, pointsLabel, stockLabel, imageLabel, userPointLabel;
    private JTextArea guideArea;
    private JButton purchaseButton;
    private ProductsDTO selectedProduct;
    private JTextField searchField;
    private String currentCategory = "전체";
    private final List<JButton> categoryButtons = new ArrayList<>();

    private static final Color BG_DARK = new Color(10, 10, 20);
    private static final Color CARD_BG = new Color(25, 25, 45);
    private static final Color POINT_PURPLE = new Color(140, 80, 255);
    private static final Color POINT_CYAN = new Color(0, 240, 255);
    private static final Color TEXT_SILVER = new Color(180, 180, 210);
    private static final Color COLOR_SOLDOUT = new Color(100, 100, 100);

    public ProductWindow(UserDTO user, Runnable rankUpdateCallback) {
        this.currentUser = (user != null) ? user : new UserDTO("Guest", "손님");
        this.rankUpdateCallback = rankUpdateCallback;

        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        setupTopPanel();
        setupCenterPanel();
        add(createDetailPanel(), BorderLayout.EAST);

        loadProducts();
    }

   
    private void filterProducts() {
        if (productsData == null) return;
        
        String text = searchField.getText();
        String keyword = (text == null || text.equals("검색어를 입력하세요")) ? "" : text.toLowerCase().trim();

        List<ProductsDTO> filtered = productsData.stream()
                .filter(p -> {
                    if (p == null) return false;
                    
                    // 1. 카테고리 매칭 (Null-Safe 비교)
                    boolean categoryMatch = "전체".equals(currentCategory) || 
                                           Objects.equals(p.getCategory(), currentCategory);
                    
                    // 2. 검색어 매칭
                    String pName = (p.getProductName() == null) ? "" : p.getProductName().toLowerCase();
                    boolean keywordMatch = pName.contains(keyword);
                    
                    return categoryMatch && keywordMatch;
                })
                .collect(Collectors.toList());

        renderFilteredList(filtered);
    }

    private void loadProducts() {
        try {
            List<ProductsDTO> data = new ProductsDAO().getAllProducts();
            this.productsData = (data != null) ? data : new ArrayList<>();
            filterProducts();
        } catch (Exception e) {
            System.err.println("상품 로드 오류: " + e.getMessage());
            this.productsData = new ArrayList<>();
            filterProducts();
        }
    }

    private void renderFilteredList(List<ProductsDTO> list) {
        if (productListPanel == null) return;
        productListPanel.removeAll();
        if (list.isEmpty()) {
            JLabel emptyMsg = new JLabel("해당 카테고리에 상품이 없습니다.");
            emptyMsg.setForeground(TEXT_SILVER);
            emptyMsg.setHorizontalAlignment(SwingConstants.CENTER);
            productListPanel.setLayout(new BorderLayout());
            productListPanel.add(emptyMsg, BorderLayout.CENTER);
        } else {
            productListPanel.setLayout(new GridLayout(0, 3, 20, 20));
            for (ProductsDTO product : list) {
                productListPanel.add(createProductCard(product));
            }
        }
        productListPanel.revalidate();
        productListPanel.repaint();
    }

    private void processPurchase() {
        if (selectedProduct == null || selectedProduct.isSoldOut()) return;
        
        if (currentUser.getBalancePoints() < selectedProduct.getRequiredPoints()) {
            JOptionPane.showMessageDialog(this, "포인트가 부족합니다.", "구매 실패", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                selectedProduct.getProductName() + "을(를) 구매하시겠습니까?", 
                "구매 확인", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = RecycleDB.connect()) {
            if (conn == null) return;
            conn.setAutoCommit(false); 

            try {
                UserDAO userDAO = new UserDAO();
                ProductsDAO prodDAO = new ProductsDAO();
                PointLogDAO logDAO = new PointLogDAO();

                boolean userOk = userDAO.subtractPoints(conn, currentUser.getUserId(), selectedProduct.getRequiredPoints());
                boolean stockOk = prodDAO.reduceStock(conn, selectedProduct.getProductId(), 1); 
                
                logDAO.insertPointLog(conn, currentUser.getUserId(), "상품구매", 
                                     selectedProduct.getProductName() + " 구매", 
                                     -selectedProduct.getRequiredPoints());
                
                if (userOk && stockOk) {
                    conn.commit(); 
                    currentUser.setBalancePoints(currentUser.getBalancePoints() - selectedProduct.getRequiredPoints());
                    refreshUserPointDisplay();
                    if (rankUpdateCallback != null) rankUpdateCallback.run();
                    
                    JOptionPane.showMessageDialog(this, "구매가 완료되었습니다!");
                    loadProducts(); 
                    updatePurchasePanel(selectedProduct);
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "처리 중 오류가 발생했습니다.");
                }
            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }


    private File findImageFile(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        File file = new File(path);
        if (file.exists()) return file;
        file = new File(System.getProperty("user.dir") + "/" + path);
        return file.exists() ? file : null;
    }

    private void setProductImage(String path) {
        File file = findImageFile(path);
        if (file != null) {
            ImageIcon icon = new ImageIcon(new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH));
            imageLabel.setIcon(icon);
            imageLabel.setText("");
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("이미지 준비중");
        }
    }

    private JPanel createProductCard(ProductsDTO product) {
        boolean isSoldOut = product.isSoldOut();
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(isSoldOut ? new Color(35, 35, 50) : CARD_BG);
        card.setBorder(new LineBorder(isSoldOut ? Color.DARK_GRAY : new Color(60, 55, 100), 1, true));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createCompoundBorder(card.getBorder(), new EmptyBorder(15,15,15,15)));

        JLabel thumb = new JLabel();
        thumb.setHorizontalAlignment(SwingConstants.CENTER);
        File f = findImageFile(product.getImagePath());
        if(f != null) {
            thumb.setIcon(new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH)));
        } else {
            thumb.setText("No Image");
            thumb.setForeground(COLOR_SOLDOUT);
        }
        
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 5));
        info.setOpaque(false);
        JLabel name = new JLabel(product.getProductName() + (isSoldOut ? " (품절)" : ""), SwingConstants.CENTER);
        name.setForeground(isSoldOut ? COLOR_SOLDOUT : Color.WHITE);
        name.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        JLabel price = new JLabel(product.getFormattedPoints(), SwingConstants.CENTER);
        price.setForeground(isSoldOut ? COLOR_SOLDOUT : POINT_CYAN);
        info.add(name); info.add(price);

        card.add(thumb, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { updatePurchasePanel(product); }
            @Override public void mouseEntered(MouseEvent e) { if (!isSoldOut) card.setBorder(new LineBorder(POINT_CYAN, 1, true)); }
            @Override public void mouseExited(MouseEvent e) { card.setBorder(new LineBorder(isSoldOut ? Color.DARK_GRAY : new Color(60, 55, 100), 1, true)); }
        });
        return card;
    }

    private void setupTopPanel() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(30, 40, 10, 40));

        JLabel titleLabel = new JLabel("Eco-Shop 상점");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);

        userPointLabel = new JLabel();
        refreshUserPointDisplay();
        userPointLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        userPointLabel.setForeground(POINT_CYAN);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userPointLabel, BorderLayout.EAST);

        JPanel filterBar = new JPanel(new BorderLayout(20, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(new EmptyBorder(10, 40, 15, 40));

        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        categoryPanel.setOpaque(false);
        String[] cats = {"전체", "생활용품", "식음료", "기프티콘"};
        for (String cat : cats) {
            JButton catBtn = createCategoryButton(cat);
            categoryButtons.add(catBtn);
            categoryPanel.add(catBtn);
        }
        updateCategoryButtonStyles();

        searchField = new JTextField("검색어를 입력하세요", 15);
        searchField.setBackground(new Color(30, 30, 50));
        searchField.setForeground(Color.GRAY);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(60,60,90)), new EmptyBorder(5,10,5,10)));
        
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("검색어를 입력하세요")) {
                    searchField.setText("");
                    searchField.setForeground(Color.WHITE);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("검색어를 입력하세요");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterProducts(); }
            public void removeUpdate(DocumentEvent e) { filterProducts(); }
            public void changedUpdate(DocumentEvent e) { filterProducts(); }
        });

        filterBar.add(categoryPanel, BorderLayout.WEST);
        filterBar.add(searchField, BorderLayout.EAST);
        topContainer.add(headerPanel);
        topContainer.add(filterBar);
        add(topContainer, BorderLayout.NORTH);
    }

    private void setupCenterPanel() {
        productListPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        productListPanel.setBackground(BG_DARK);
        productListPanel.setBorder(new EmptyBorder(10, 40, 40, 20));

        JScrollPane scrollPane = new JScrollPane(productListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createDetailPanel() {
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setPreferredSize(new Dimension(380, 0));
        detailPanel.setBackground(new Color(20, 20, 35));
        detailPanel.setBorder(new EmptyBorder(40, 30, 40, 30));

        imageLabel = new JLabel("상품을 선택하세요");
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(280, 280));
        imageLabel.setMaximumSize(new Dimension(280, 280));
        imageLabel.setBorder(new LineBorder(new Color(50, 50, 80), 2, true));
        imageLabel.setForeground(TEXT_SILVER);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameLabel = new JLabel("-");
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        pointsLabel = new JLabel("0 P");
        pointsLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        pointsLabel.setForeground(POINT_CYAN);
        pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        stockLabel = new JLabel("수량: -");
        stockLabel.setForeground(TEXT_SILVER);
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        guideArea = new JTextArea();
        guideArea.setEditable(false);
        guideArea.setLineWrap(true);
        guideArea.setWrapStyleWord(true);
        guideArea.setBackground(new Color(30, 30, 50));
        guideArea.setForeground(Color.LIGHT_GRAY);
        guideArea.setBorder(new EmptyBorder(10,10,10,10));

        purchaseButton = new JButton("구매하기");
        purchaseButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        purchaseButton.setBackground(POINT_PURPLE);
        purchaseButton.setForeground(Color.WHITE);
        purchaseButton.setPreferredSize(new Dimension(280, 50));
        purchaseButton.setMaximumSize(new Dimension(280, 50));
        purchaseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        purchaseButton.setEnabled(false);
        purchaseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        purchaseButton.addActionListener(e -> processPurchase());

        detailPanel.add(imageLabel);
        detailPanel.add(Box.createVerticalStrut(20));
        detailPanel.add(nameLabel);
        detailPanel.add(pointsLabel);
        detailPanel.add(stockLabel);
        detailPanel.add(Box.createVerticalStrut(20));
        JScrollPane areaScroll = new JScrollPane(guideArea);
        areaScroll.setBorder(null);
        detailPanel.add(areaScroll);
        detailPanel.add(Box.createVerticalStrut(20));
        detailPanel.add(purchaseButton);

        return detailPanel;
    }

    private void updatePurchasePanel(ProductsDTO product) {
        if (product == null) return;
        this.selectedProduct = product;
        nameLabel.setText(product.getProductName());
        pointsLabel.setText(product.getFormattedPoints());
        stockLabel.setText("재고: " + product.getStockStatusString());
        guideArea.setText(product.getDescription());
        setProductImage(product.getImagePath());
        
        boolean canAfford = currentUser.getBalancePoints() >= product.getRequiredPoints();
        boolean hasStock = !product.isSoldOut();
        
        if (!hasStock) {
            purchaseButton.setEnabled(false);
            purchaseButton.setText("품절");
            purchaseButton.setBackground(COLOR_SOLDOUT);
        } else if (!canAfford) {
            purchaseButton.setEnabled(false);
            purchaseButton.setText("포인트 부족");
            purchaseButton.setBackground(new Color(80, 40, 40));
        } else {
            purchaseButton.setEnabled(true);
            purchaseButton.setText("구매하기");
            purchaseButton.setBackground(POINT_PURPLE);
        }
    }

    private JButton createCategoryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            currentCategory = text;
            updateCategoryButtonStyles();
            filterProducts();
        });
        return btn;
    }

    private void updateCategoryButtonStyles() {
        for (JButton btn : categoryButtons) {
            boolean isSelected = btn.getText().equals(currentCategory);
            btn.setBackground(isSelected ? POINT_PURPLE : new Color(40, 40, 70));
            btn.setForeground(isSelected ? Color.WHITE : TEXT_SILVER);
        }
    }

    private void refreshUserPointDisplay() {
        if (userPointLabel != null) {
            userPointLabel.setText("보유 포인트: " + String.format("%,d", currentUser.getBalancePoints()) + " P");
        }
    }

    private static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { 
            this.thumbColor = POINT_PURPLE; 
            this.trackColor = BG_DARK; 
        }
        @Override protected JButton createDecreaseButton(int orientation) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
        @Override protected JButton createIncreaseButton(int orientation) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
    }
}