package recycle;

import db.DAO.UserDAO;
import db.DAO.ProductsDAO;
import db.DTO.UserDTO;
import db.DTO.ProductsDTO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.List;
import java.net.URL;

public class AdminWindow extends JPanel {
    private final Runnable refreshCallback;
    private final UserDAO userDAO = new UserDAO();
    private final ProductsDAO productsDAO = new ProductsDAO();

    private static final Color BG_DARK = new Color(15, 15, 30);
    private static final Color BG_CARD = new Color(25, 25, 50);
    private static final Color POINT_PURPLE = new Color(138, 43, 226);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color POINT_RED = new Color(255, 46, 99);
    private static final Color TEXT_WHITE = new Color(230, 230, 250);

    private StatCard userStat, pointStat, stockStat;
    private Timer imageLoadTimer;

    private JTable productTable, userTable;
    private DefaultTableModel productTableModel, userTableModel;
    private TableRowSorter<DefaultTableModel> productSorter, userSorter;
    
    private JTextField nameField, pointField, stockField, imagePathField, productSearchField;
    private JTextField userIdField, userPwField, nicknameField, userPointField, userSearchField;
    private JComboBox<String> categoryCombo, productSearchOption, userSearchOption, gradeCombo;
    private JTextArea descArea;
    private JLabel imagePreviewLabel;
    
    private String currentSelectedProductId = null;
    private String currentSelectedUserId = null;

    public AdminWindow(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        setTabUI();
        setupDashboard();
        setupTabs();
        refreshAllData();
    }

    private void setTabUI() {
        UIManager.put("TabbedPane.background", BG_CARD);
        UIManager.put("TabbedPane.foreground", Color.LIGHT_GRAY);
        UIManager.put("TabbedPane.selected", POINT_PURPLE);
        UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
        UIManager.put("TabbedPane.contentOpaque", false);
    }

    private void setupDashboard() {
        JPanel dashboard = new JPanel(new GridLayout(1, 3, 20, 0));
        dashboard.setOpaque(false);
        dashboard.setPreferredSize(new Dimension(0, 80));
        userStat = new StatCard("총 활성 회원", "0 명", POINT_PURPLE);
        pointStat = new StatCard("시스템 유통 포인트", "0 P", POINT_CYAN);
        stockStat = new StatCard("품절 상품(재고 0)", "0 건", POINT_RED);
        dashboard.add(userStat); dashboard.add(pointStat); dashboard.add(stockStat);
        add(dashboard, BorderLayout.NORTH);
    }

    private void setupTabs() {
        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.addTab("상품 인벤토리 관리", createProductManagementPanel());
        adminTabs.addTab("회원 보안/권한 관리", createUserManagementPanel());
        add(adminTabs, BorderLayout.CENTER);
    }

    private JPanel createProductManagementPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_DARK);

        JPanel inputSection = new JPanel(new BorderLayout(15, 0));
        inputSection.setOpaque(false);
        inputSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_CARD);
        formPanel.setBorder(new CompoundBorder(new LineBorder(POINT_PURPLE, 1), new EmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 10, 4, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = createStyledTextField(false); 
        pointField = createStyledTextField(true);
        stockField = createStyledTextField(true); 
        imagePathField = createStyledTextField(false);
        
        JPanel imagePathWrapper = new JPanel(new BorderLayout(5, 0));
        imagePathWrapper.setOpaque(false);
        imagePathWrapper.add(imagePathField, BorderLayout.CENTER);
        JButton browseBtn = createStyledButton("찾기", new Color(60, 60, 80));
        browseBtn.addActionListener(e -> {
            FileDialog fd = new FileDialog((Frame) SwingUtilities.getWindowAncestor(this), "이미지 선택", FileDialog.LOAD);
            fd.setVisible(true);
            if (fd.getFile() != null) imagePathField.setText(fd.getDirectory() + fd.getFile());
        });
        imagePathWrapper.add(browseBtn, BorderLayout.EAST);

        categoryCombo = new JComboBox<>(new String[]{"생활용품", "식음료", "기프티콘", "미분류"});
        styleComboBox(categoryCombo);
        descArea = new JTextArea(3, 20); descArea.setBackground(BG_DARK); descArea.setForeground(Color.WHITE); 
        descArea.setLineWrap(true); descArea.setCaretColor(POINT_CYAN);
        JScrollPane descScroll = createStyledScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(0, 60));

        addFormRow(formPanel, "상품 이름", nameField, gbc, 0);
        addFormRow(formPanel, "카테고리", categoryCombo, gbc, 1);
        addFormRow(formPanel, "필요 포인트", pointField, gbc, 2);
        addFormRow(formPanel, "재고 수량", stockField, gbc, 3);
        addFormRow(formPanel, "이미지 경로", imagePathWrapper, gbc, 4);
        addFormRow(formPanel, "상품 설명", descScroll, gbc, 5);

        JPanel previewBox = new JPanel(new BorderLayout());
        previewBox.setBackground(BG_CARD); previewBox.setPreferredSize(new Dimension(180, 0));
        previewBox.setBorder(new TitledBorder(new LineBorder(POINT_PURPLE), "PREVIEW", TitledBorder.CENTER, TitledBorder.TOP, null, POINT_CYAN));
        imagePreviewLabel = new JLabel("NO IMAGE", JLabel.CENTER);
        imagePreviewLabel.setForeground(Color.GRAY);
        previewBox.add(imagePreviewLabel, BorderLayout.CENTER);
        
        imagePathField.addCaretListener(e -> {
            if (imageLoadTimer != null) imageLoadTimer.stop();
            imageLoadTimer = new Timer(500, evt -> updateImagePreview(imagePathField.getText()));
            imageLoadTimer.setRepeats(false);
            imageLoadTimer.start();
        });

        inputSection.add(formPanel, BorderLayout.CENTER);
        inputSection.add(previewBox, BorderLayout.EAST);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnGroup.setOpaque(false);
        JButton addBtn = createStyledButton("상품 생성", new Color(40, 167, 69));
        JButton updateBtn = createStyledButton("상품 수정", new Color(0, 123, 255));
        JButton deleteBtn = createStyledButton("상품 삭제", POINT_RED);
        JButton clearBtn = createStyledButton("초기화", new Color(108, 117, 125));
        btnGroup.add(addBtn); btnGroup.add(updateBtn); btnGroup.add(deleteBtn); btnGroup.add(clearBtn);

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchGroup.setOpaque(false);
   
        productSearchOption = new JComboBox<>(new String[]{"전체 검색", "ID", "카테고리", "상품명", "포인트", "재고", "이미지경로", "설명"});
        styleComboBox(productSearchOption);
        productSearchField = createStyledTextField(false); 
        productSearchField.setPreferredSize(new Dimension(120, 28));
        searchGroup.add(productSearchOption); searchGroup.add(productSearchField);
        controlPanel.add(btnGroup, BorderLayout.CENTER); controlPanel.add(searchGroup, BorderLayout.EAST);

        productTableModel = new NonEditableModel(new String[]{"ID", "카테고리", "상품명", "포인트", "재고", "이미지경로", "설명"}, 0);
        productTable = createStyledTable(productTableModel);
        applyStockRenderer(productTable);
        productSorter = new TableRowSorter<>(productTableModel);
        productTable.setRowSorter(productSorter);

        productTable.getSelectionModel().addListSelectionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row != -1) {
                int mRow = productTable.convertRowIndexToModel(row);
                currentSelectedProductId = String.valueOf(productTableModel.getValueAt(mRow, 0));
                categoryCombo.setSelectedItem(productTableModel.getValueAt(mRow, 1));
                nameField.setText((String) productTableModel.getValueAt(mRow, 2));
                pointField.setText(String.valueOf(productTableModel.getValueAt(mRow, 3)));
                stockField.setText(String.valueOf(productTableModel.getValueAt(mRow, 4)));
                imagePathField.setText((String) productTableModel.getValueAt(mRow, 5));
                descArea.setText((String) productTableModel.getValueAt(mRow, 6));
            }
        });

        addBtn.addActionListener(e -> handleProductAction("INSERT"));
        updateBtn.addActionListener(e -> handleProductAction("UPDATE"));
        deleteBtn.addActionListener(e -> handleProductAction("DELETE"));
        clearBtn.addActionListener(e -> clearProductFields());
        productSearchField.addCaretListener(e -> applyProductFilter());

        mainPanel.add(inputSection);
        mainPanel.add(controlPanel);
        mainPanel.add(createStyledScrollPane(productTable));
        return mainPanel;
    }

    private JPanel createUserManagementPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_DARK);

        JPanel topSection = new JPanel(new GridBagLayout());
        topSection.setBackground(BG_CARD);
        topSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        topSection.setBorder(new CompoundBorder(new LineBorder(POINT_CYAN, 1), new EmptyBorder(15, 20, 15, 20)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        userIdField = createStyledTextField(false); 
        userPwField = createStyledTextField(false);
        nicknameField = createStyledTextField(false); 
        userPointField = createStyledTextField(true);
        
        gradeCombo = new JComboBox<>(new String[]{"일반 사용자", "관리자"});
        styleComboBox(gradeCombo);

        addFormRow(topSection, "아이디", userIdField, gbc, 0);
        addFormRow(topSection, "비밀번호", userPwField, gbc, 1);
        addFormRow(topSection, "닉네임", nicknameField, gbc, 2);
        addFormRow(topSection, "보유 포인트", userPointField, gbc, 3);
        addFormRow(topSection, "계정 권한", gradeCombo, gbc, 4);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnGroup.setOpaque(false);
       
        JButton uAdd = createStyledButton("회원 등록", new Color(40, 167, 69));
        JButton uEdit = createStyledButton("정보 수정", new Color(0, 123, 255));
        JButton uDel = createStyledButton("회원 탈퇴", POINT_RED);
        JButton uClear = createStyledButton("초기화", new Color(108, 117, 125));
        btnGroup.add(uAdd); btnGroup.add(uEdit); btnGroup.add(uDel); btnGroup.add(uClear);

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchGroup.setOpaque(false);
       
        userSearchOption = new JComboBox<>(new String[]{"전체 검색", "아이디", "닉네임", "현재 포인트", "누적 포인트", "권한"});
        styleComboBox(userSearchOption);
        userSearchField = createStyledTextField(false); 
        userSearchField.setPreferredSize(new Dimension(120, 28));
        searchGroup.add(userSearchOption); searchGroup.add(userSearchField);
        controlPanel.add(btnGroup, BorderLayout.CENTER); controlPanel.add(searchGroup, BorderLayout.EAST);

        userTableModel = new NonEditableModel(new String[]{"아이디", "닉네임", "현재 포인트", "누적 포인트", "권한"}, 0);
        userTable = createStyledTable(userTableModel);
        userSorter = new TableRowSorter<>(userTableModel);
        userTable.setRowSorter(userSorter);

        userTable.getSelectionModel().addListSelectionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row != -1) {
                int mRow = userTable.convertRowIndexToModel(row);
                currentSelectedUserId = (String) userTableModel.getValueAt(mRow, 0);
                userIdField.setText(currentSelectedUserId);
                userIdField.setEditable(false); 
                nicknameField.setText((String) userTableModel.getValueAt(mRow, 1));
                userPointField.setText(userTableModel.getValueAt(mRow, 2).toString());
                gradeCombo.setSelectedItem(userTableModel.getValueAt(mRow, 4));
            }
        });

        uAdd.addActionListener(e -> handleUserAction("INSERT"));
        uEdit.addActionListener(e -> handleUserAction("UPDATE"));
        uDel.addActionListener(e -> handleUserAction("DELETE"));
        uClear.addActionListener(e -> clearUserFields());
        userSearchField.addCaretListener(e -> applyUserFilter());

        mainPanel.add(topSection);
        mainPanel.add(controlPanel);
        mainPanel.add(createStyledScrollPane(userTable));
        return mainPanel;
    }

    private void handleProductAction(String type) {
        try {
            if (type.equals("DELETE")) {
                if (currentSelectedProductId == null) return;
                int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) productsDAO.deleteProduct(currentSelectedProductId);
            } else {
                ProductsDTO p = new ProductsDTO();
                p.setProductId(currentSelectedProductId);
                p.setProductName(nameField.getText().trim());
                p.setCategory((String) categoryCombo.getSelectedItem());
                p.setRequiredPoints(Integer.parseInt(pointField.getText().isEmpty() ? "0" : pointField.getText()));
                p.setStock(Integer.parseInt(stockField.getText().isEmpty() ? "0" : stockField.getText()));
                p.setImagePath(imagePathField.getText().trim());
                p.setDescription(descArea.getText().trim());

                if (type.equals("INSERT")) productsDAO.insertProduct(p);
                else productsDAO.updateProduct(p);
            }
            refreshAllData(); if (refreshCallback != null) refreshCallback.run();
            clearProductFields();
            JOptionPane.showMessageDialog(this, "완료되었습니다.");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage()); }
    }

    private void handleUserAction(String type) {
        try {
            if (type.equals("DELETE")) {
                if (currentSelectedUserId == null) return;
                int confirm = JOptionPane.showConfirmDialog(this, "탈퇴시키겠습니까?", "삭제", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) userDAO.deleteUser(currentSelectedUserId);
            } else if (type.equals("INSERT")) {
                userDAO.registerUser(userIdField.getText().trim(), userPwField.getText(), nicknameField.getText().trim());
                UserDTO u = new UserDTO();
                u.setUserId(userIdField.getText().trim());
                u.setBalancePoints(Integer.parseInt(userPointField.getText().trim()));
                u.setAdmin("관리자".equals(gradeCombo.getSelectedItem()));
                userDAO.updateUserByAdmin(u);
            } else {
                UserDTO u = new UserDTO();
                u.setUserId(currentSelectedUserId);
                u.setNickname(nicknameField.getText().trim());
                u.setBalancePoints(Integer.parseInt(userPointField.getText().trim()));
                u.setAdmin("관리자".equals(gradeCombo.getSelectedItem()));
                userDAO.updateUserByAdmin(u);
            }
            refreshAllData(); clearUserFields();
            JOptionPane.showMessageDialog(this, "반영되었습니다.");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage()); }
    }

    private void applyProductFilter() {
        String text = productSearchField.getText();
        int opt = productSearchOption.getSelectedIndex();
        if (text.isEmpty()) productSorter.setRowFilter(null);
        else if (opt == 0) productSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        else productSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, opt - 1));
    }

    private void applyUserFilter() {
        String text = userSearchField.getText();
        int opt = userSearchOption.getSelectedIndex();
        if (text.isEmpty()) userSorter.setRowFilter(null);
        else if (opt == 0) userSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        else userSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, opt - 1));
    }

    private void addFormRow(JPanel p, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0.1;
        JLabel lbl = new JLabel(label); lbl.setForeground(Color.LIGHT_GRAY);
        p.add(lbl, gbc); gbc.gridx = 1; gbc.weightx = 0.9; p.add(field, gbc);
    }

    private JTextField createStyledTextField(boolean numericOnly) {
        JTextField tf = new JTextField();
        tf.setBackground(BG_DARK); tf.setForeground(Color.WHITE); tf.setCaretColor(POINT_CYAN);
        tf.setBorder(new CompoundBorder(new LineBorder(new Color(60, 60, 80)), new EmptyBorder(5, 8, 5, 8)));
        if (numericOnly) {
            ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override public void replace(FilterBypass fb, int o, int l, String t, AttributeSet a) throws BadLocationException {
                    if (t.matches("\\d*")) super.replace(fb, o, l, t, a);
                }
            });
        }
        return tf;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(BG_DARK); combo.setForeground(POINT_CYAN);
        combo.setBorder(new LineBorder(new Color(60, 60, 80)));
    }

    private JButton createStyledButton(String text, Color bg) {
        return new JButton(text) {
            { setBackground(bg); setForeground(Color.WHITE); setFocusPainted(false); setBorderPainted(false);
              setCursor(new Cursor(Cursor.HAND_CURSOR)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(getForeground()); FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_CARD); table.setForeground(TEXT_WHITE); table.setRowHeight(30); table.setShowGrid(false);
        table.setSelectionBackground(POINT_PURPLE); table.getTableHeader().setBackground(BG_DARK);
        table.getTableHeader().setForeground(POINT_CYAN);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        return table;
    }

    private void applyStockRenderer(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                try {
                    int stock = Integer.parseInt(t.getValueAt(r, 4).toString());
                    if (!isS && stock <= 0) comp.setForeground(POINT_RED);
                    else if (!isS) comp.setForeground(TEXT_WHITE);
                } catch(Exception e) {}
                setHorizontalAlignment(JLabel.CENTER); return comp;
            }
        });
    }

    private JScrollPane createStyledScrollPane(Component comp) {
        JScrollPane scroll = new JScrollPane(comp);
        scroll.getViewport().setBackground(BG_DARK); scroll.setBorder(new LineBorder(new Color(50, 50, 70)));
        scroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        return scroll;
    }

    private void clearProductFields() {
        currentSelectedProductId = null; nameField.setText(""); pointField.setText("");
        stockField.setText(""); imagePathField.setText(""); descArea.setText("");
        imagePreviewLabel.setIcon(null); imagePreviewLabel.setText("NO IMAGE"); productTable.clearSelection();
    }

    private void clearUserFields() {
        currentSelectedUserId = null; userIdField.setText(""); userIdField.setEditable(true);
        userPwField.setText(""); nicknameField.setText(""); userPointField.setText("");
        gradeCombo.setSelectedIndex(0); userTable.clearSelection();
    }

    private void refreshAllData() { loadProductList(); loadUserList(); updateStats(); }

    private void updateStats() {
        try {
            List<UserDTO> users = userDAO.getAllUsersPaged(1000, 0);
            List<ProductsDTO> products = productsDAO.getAllProducts();
            userStat.setValue(users.size() + " 명");
            int totalPoints = users.stream().mapToInt(UserDTO::getBalancePoints).sum();
            pointStat.setValue(String.format("%,d P", totalPoints));
            long count = products.stream().filter(p -> p.getStock() <= 0).count();
            stockStat.setValue(count + " 건");
        } catch (Exception e) {}
    }

    private void loadProductList() {
        productTableModel.setRowCount(0);
        List<ProductsDTO> products = productsDAO.getAllProducts();
        for (ProductsDTO p : products) {
            productTableModel.addRow(new Object[]{p.getProductId(), p.getCategory(), p.getProductName(), p.getRequiredPoints(), p.getStock(), p.getImagePath(), p.getDescription()});
        }
    }

    private void loadUserList() {
        userTableModel.setRowCount(0);
        try {
            List<UserDTO> users = userDAO.getAllUsersPaged(1000, 0);
            for (UserDTO u : users) {
                userTableModel.addRow(new Object[]{u.getUserId(), u.getNickname(), u.getBalancePoints(), u.getTotalPoints(), u.isAdmin() ? "관리자" : "일반 사용자"});
            }
        } catch (SQLException e) {}
    }

    private void updateImagePreview(String path) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (path == null || path.isEmpty()) { imagePreviewLabel.setIcon(null); imagePreviewLabel.setText("NO IMAGE"); return; }
                ImageIcon icon = path.startsWith("http") ? new ImageIcon(new URL(path)) : new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                imagePreviewLabel.setIcon(new ImageIcon(img)); imagePreviewLabel.setText("");
            } catch (Exception e) { imagePreviewLabel.setIcon(null); imagePreviewLabel.setText("N/A"); }
        });
    }

    private static class NonEditableModel extends DefaultTableModel {
        public NonEditableModel(Object[] cn, int rc) { super(cn, rc); }
        @Override public boolean isCellEditable(int r, int c) { return false; }
    }

    private static class StatCard extends JPanel {
        private final JLabel valueLabel;
        public StatCard(String title, String value, Color accent) {
            setLayout(new BorderLayout()); setBackground(BG_CARD);
            setBorder(new CompoundBorder(new LineBorder(accent, 2), new EmptyBorder(8, 15, 8, 15)));
            JLabel t = new JLabel(title); t.setForeground(Color.GRAY);
            valueLabel = new JLabel(value); valueLabel.setForeground(accent);
            valueLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
            add(t, BorderLayout.NORTH); add(valueLabel, BorderLayout.CENTER);
        }
        public void setValue(String val) { valueLabel.setText(val); }
    }

    static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { this.thumbColor = new Color(70, 70, 100); }
        @Override protected JButton createDecreaseButton(int o) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int o) { return createZeroButton(); }
        private JButton createZeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
    }
}