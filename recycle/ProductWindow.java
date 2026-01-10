package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
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

    
    private void setProductImage(String path) {
        if (path == null || path.trim().isEmpty()) {
            imageLabel.setIcon(null);
            imageLabel.setText("이미지 없음");
            return;
        }

        File file = new File(path);
        if (file.exists()) {
            ImageIcon icon = new ImageIcon(path);
     
            Image img = icon.getImage();
            Image scaledImg = img.getScaledInstance(280, 280, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImg));
            imageLabel.setText(""); 
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("파일 찾을 수 없음");
        }
    }

    private void filterProducts() {
        if (productsData == null) return;

        final String keyword = (searchField == null) ? "" : searchField.getText().toLowerCase().trim();
        
        List<ProductsDTO> filtered = productsData.stream()
                .filter(p -> {
                    if (p == null) return false;
                    String pCat = (p.getCategory() == null) ? "" : p.getCategory();
                    boolean categoryMatch = currentCategory.equals("전체") || pCat.equals(currentCategory);
                    String pName = (p.getProductName() == null) ? "" : p.getProductName().toLowerCase();
                    boolean nameMatch = pName.contains(keyword.equals("검색어를 입력하세요") ? "" : keyword);
                    return categoryMatch && nameMatch;
                })
                .collect(Collectors.toList());

        renderFilteredList(filtered);
    }

    private void renderFilteredList(List<ProductsDTO> list) {
        if (productListPanel == null) return;
        productListPanel.removeAll();
        if (list.isEmpty()) {
            JLabel noData = new JLabel("해당 조건의 상품이 없습니다.");
            noData.setForeground(TEXT_SILVER);
            noData.setHorizontalAlignment(SwingConstants.CENTER);
            productListPanel.add(noData);
        } else {
            for (ProductsDTO product : list) {
                productListPanel.add(createProductCard(product));
            }
        }
        productListPanel.revalidate();
        productListPanel.repaint();
    }

    private void loadProducts() {
        try {
            this.productsData = new ProductsDAO().getAllProducts();
            if (productsData == null) productsData = new ArrayList<>();
            filterProducts();
        } catch (Exception e) {
            System.err.println("DB 로드 오류: " + e.getMessage());
        }
    }

    private void processPurchase() {
        if (selectedProduct == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, selectedProduct.getProductName() + "을(를) 구매하시겠습니까?", "구매 확인", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = RecycleDB.connect()) {
            if (conn == null) throw new SQLException("DB 연결 실패");
            conn.setAutoCommit(false);
            try {
                UserDAO userDAO = new UserDAO();
                ProductsDAO prodDAO = new ProductsDAO();
                PointLogDAO logDAO = new PointLogDAO();

                if (userDAO.subtractPoints(currentUser.getUserId(), selectedProduct.getRequiredPoints())) {
                    selectedProduct.setStock(selectedProduct.getStock() - 1);
                    prodDAO.updateProduct(conn, selectedProduct);
                    logDAO.insertSpendLog(conn, currentUser.getUserId(), selectedProduct.getProductName(), selectedProduct.getRequiredPoints());
                    conn.commit();

                    currentUser.setBalancePoints(currentUser.getBalancePoints() - selectedProduct.getRequiredPoints());
                    userPointLabel.setText("내 포인트: " + String.format("%,d", currentUser.getBalancePoints()) + " P");
                    JOptionPane.showMessageDialog(this, "성공적으로 구매되었습니다!");
                    if (rankUpdateCallback != null) rankUpdateCallback.run();
                    loadProducts(); 
                    updatePurchasePanel(selectedProduct); 
                } else {
                    throw new SQLException("포인트가 부족합니다.");
                }
            } catch (Exception ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "구매 실패: " + ex.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        userPointLabel = new JLabel("내 포인트: " + String.format("%,d", currentUser.getBalancePoints()) + " P");
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
        searchField.setForeground(Color.WHITE);
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
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createDetailPanel() {
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setPreferredSize(new Dimension(380, 0));
        detailPanel.setBackground(new Color(20, 20, 35));
        detailPanel.setBorder(new EmptyBorder(40, 30, 40, 30));

        imageLabel = new JLabel("상품 선택", SwingConstants.CENTER);
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
        guideArea.setBackground(new Color(30, 30, 50));
        guideArea.setForeground(Color.LIGHT_GRAY);

        purchaseButton = new JButton("구매하기");
        purchaseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        purchaseButton.setEnabled(false);
        purchaseButton.addActionListener(e -> processPurchase());

        detailPanel.add(imageLabel);
        detailPanel.add(Box.createVerticalStrut(20));
        detailPanel.add(nameLabel);
        detailPanel.add(pointsLabel);
        detailPanel.add(stockLabel);
        detailPanel.add(Box.createVerticalStrut(20));
        detailPanel.add(new JScrollPane(guideArea));
        detailPanel.add(Box.createVerticalStrut(20));
        detailPanel.add(purchaseButton);

        return detailPanel;
    }

    private JPanel createProductCard(ProductsDTO product) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(new Color(60, 55, 100)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel name = new JLabel(product.getProductName(), SwingConstants.CENTER);
        name.setForeground(Color.WHITE);
        card.add(name, BorderLayout.CENTER);
        
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { updatePurchasePanel(product); }
        });
        return card;
    }

    private void updatePurchasePanel(ProductsDTO product) {
        this.selectedProduct = product;
        nameLabel.setText(product.getProductName());
        pointsLabel.setText(String.format("%,d P", product.getRequiredPoints()));
        stockLabel.setText("남은 수량: " + product.getStock());
        guideArea.setText(product.getDescription());
        
        // [수정 포인트] 이미지 설정 메서드 호출
        setProductImage(product.getImagePath());
        
        purchaseButton.setEnabled(product.getStock() > 0 && currentUser.getBalancePoints() >= product.getRequiredPoints());
    }

    private JButton createCategoryButton(String text) {
        JButton btn = new JButton(text);
        btn.addActionListener(e -> {
            currentCategory = text;
            updateCategoryButtonStyles();
            filterProducts();
        });
        return btn;
    }

    private void updateCategoryButtonStyles() {
        for (JButton btn : categoryButtons) {
            btn.setBackground(btn.getText().equals(currentCategory) ? POINT_PURPLE : new Color(40, 40, 70));
            btn.setForeground(Color.WHITE);
        }
    }

    private static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { this.thumbColor = POINT_PURPLE; this.trackColor = BG_DARK; }
        @Override protected JButton createDecreaseButton(int orientation) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
        @Override protected JButton createIncreaseButton(int orientation) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
    }
}