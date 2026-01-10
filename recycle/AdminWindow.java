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
import java.io.*;
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
    private JComboBox<String> categoryCombo, productSearchOption, userSearchOption;
    private JTextArea descArea;
    private JLabel imagePreviewLabel;
    private JCheckBox adminCheck;
    
    private String currentSelectedProductId = null;
    private String currentSelectedUserId = null;

    public AdminWindow(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        setupDashboard();
        setupTabs();
        refreshAllData();
    }

    private void setupDashboard() {
        JPanel dashboard = new JPanel(new GridLayout(1, 3, 20, 0));
        dashboard.setOpaque(false);
        dashboard.setPreferredSize(new Dimension(0, 80));
        userStat = new StatCard("총 활성 회원", "0 명", POINT_PURPLE);
        pointStat = new StatCard("시스템 유통 포인트", "0 P", POINT_CYAN);
        stockStat = new StatCard("재고 부족 상품", "0 건", POINT_RED);
        dashboard.add(userStat); dashboard.add(pointStat); dashboard.add(stockStat);
        add(dashboard, BorderLayout.NORTH);
    }

    private void setupTabs() {
        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.setBackground(BG_DARK);
        adminTabs.setForeground(Color.WHITE);
        adminTabs.setBorder(BorderFactory.createEmptyBorder());
        adminTabs.addTab("  상품 인벤토리 관리  ", createProductManagementPanel());
        adminTabs.addTab("  회원 보안/권한 관리  ", createUserManagementPanel());
        add(adminTabs, BorderLayout.CENTER);
    }

    private static class NonEditableModel extends DefaultTableModel {
        public NonEditableModel(Object[] columnNames, int rowCount) { super(columnNames, rowCount); }
        @Override public boolean isCellEditable(int row, int column) { return false; }
    }

    private JPanel createProductManagementPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_DARK);

        JPanel inputSection = new JPanel(new BorderLayout(15, 0));
        inputSection.setOpaque(false);
        inputSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_CARD);
        formPanel.setBorder(new CompoundBorder(new LineBorder(POINT_PURPLE, 1), new EmptyBorder(15, 15, 15, 15)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 10, 4, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = createStyledTextField(false); pointField = createStyledTextField(true);
        stockField = createStyledTextField(true); imagePathField = createStyledTextField(false);
        categoryCombo = new JComboBox<>(new String[]{"생활용품", "식음료", "기프티콘"});
        styleComboBox(categoryCombo);
        descArea = new JTextArea(3, 20); descArea.setBackground(BG_DARK); descArea.setForeground(Color.WHITE); descArea.setLineWrap(true);
        JScrollPane descScroll = createStyledScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(0, 60));

        addFormRow(formPanel, "상품 이름", nameField, gbc, 0);
        addFormRow(formPanel, "카테고리", categoryCombo, gbc, 1);
        addFormRow(formPanel, "필요 포인트", pointField, gbc, 2);
        addFormRow(formPanel, "재고 수량", stockField, gbc, 3);
        addFormRow(formPanel, "이미지 경로", imagePathField, gbc, 4);
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
        controlPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        controlPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnGroup.setOpaque(false);
        JButton addBtn = createStyledButton("상품 생성", new Color(40, 167, 69));
        JButton updateBtn = createStyledButton("상품 수정", new Color(0, 123, 255));
        JButton deleteBtn = createStyledButton("상품 삭제", POINT_RED);
        JButton exportBtn = createStyledButton("CSV 저장", new Color(108, 117, 125));
        btnGroup.add(addBtn); btnGroup.add(updateBtn); btnGroup.add(deleteBtn); btnGroup.add(exportBtn);

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchGroup.setOpaque(false);
        productSearchOption = new JComboBox<>(new String[]{"전체 검색", "ID", "카테고리", "상품명", "포인트", "재고", "이미지경로", "설명"});
        styleComboBox(productSearchOption);
        productSearchField = createStyledTextField(false); 
        productSearchField.setPreferredSize(new Dimension(100, 28));
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
        exportBtn.addActionListener(e -> exportToCSV(productTable, "product_list.csv"));
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
        topSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        topSection.setBorder(new CompoundBorder(new LineBorder(POINT_CYAN, 1), new EmptyBorder(15, 20, 15, 20)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        userIdField = createStyledTextField(false); userPwField = createStyledTextField(false);
        nicknameField = createStyledTextField(false); userPointField = createStyledTextField(true);
        adminCheck = new JCheckBox("관리자 권한 부여");
        adminCheck.setOpaque(false); adminCheck.setForeground(POINT_CYAN);

        addFormRow(topSection, "아이디", userIdField, gbc, 0);
        addFormRow(topSection, "비밀번호", userPwField, gbc, 1);
        addFormRow(topSection, "닉네임", nicknameField, gbc, 2);
        addFormRow(topSection, "보유 포인트", userPointField, gbc, 3);
        gbc.gridx = 1; gbc.gridy = 4; topSection.add(adminCheck, gbc);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setOpaque(false);
        controlPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        controlPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnGroup.setOpaque(false);
        JButton uAdd = createStyledButton("회원 등록", new Color(40, 167, 69));
        JButton uEdit = createStyledButton("정보 수정", new Color(0, 123, 255));
        JButton uDel = createStyledButton("회원 삭제", POINT_RED);
        JButton uExport = createStyledButton("CSV 저장", new Color(108, 117, 125));
        btnGroup.add(uAdd); btnGroup.add(uEdit); btnGroup.add(uDel); btnGroup.add(uExport);

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchGroup.setOpaque(false);
        userSearchOption = new JComboBox<>(new String[]{"전체 검색", "아이디", "닉네임", "현재 포인트", "누적 포인트", "계정 등급"});
        styleComboBox(userSearchOption);
        userSearchField = createStyledTextField(false); 
        userSearchField.setPreferredSize(new Dimension(100, 28));
        searchGroup.add(userSearchOption); searchGroup.add(userSearchField);
        controlPanel.add(btnGroup, BorderLayout.CENTER); controlPanel.add(searchGroup, BorderLayout.EAST);

        userTableModel = new NonEditableModel(new String[]{"아이디", "닉네임", "현재 포인트", "누적 포인트", "계정 등급"}, 0);
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
                String grade = (String) userTableModel.getValueAt(mRow, 4);
                adminCheck.setSelected(grade.equals("관리자"));
                userPwField.setText(""); 
            }
        });

        uAdd.addActionListener(e -> handleUserAction("INSERT"));
        uEdit.addActionListener(e -> {
            if (currentSelectedUserId == null) {
                JOptionPane.showMessageDialog(this, "수정할 회원을 목록에서 선택하세요.");
                return;
            }
            handleUserAction("UPDATE");
        });
        uDel.addActionListener(e -> handleUserAction("DELETE"));
        uExport.addActionListener(e -> exportToCSV(userTable, "user_list.csv"));
        userSearchField.addCaretListener(e -> applyUserFilter());

        mainPanel.add(topSection); mainPanel.add(controlPanel);
        mainPanel.add(createStyledScrollPane(userTable));
        return mainPanel;
    }


    private void addFormRow(JPanel p, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0.1;
        JLabel lbl = new JLabel(label); lbl.setForeground(Color.LIGHT_GRAY);
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.9;
        p.add(field, gbc);
    }

    private JTextField createStyledTextField(boolean numericOnly) {
        JTextField tf = new JTextField();
        tf.setBackground(BG_DARK); tf.setForeground(Color.WHITE); tf.setCaretColor(POINT_CYAN);
        tf.setBorder(new CompoundBorder(new LineBorder(new Color(60, 60, 80)), new EmptyBorder(5, 8, 5, 8)));
        if (numericOnly) {
            ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if (text.matches("\\d*")) super.replace(fb, offset, length, text, attrs);
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
            {
                setBackground(bg); setForeground(Color.WHITE); setFocusPainted(false);
                setBorderPainted(false); setFont(new Font("맑은 고딕", Font.BOLD, 12));
                setCursor(new Cursor(Cursor.HAND_CURSOR)); setPreferredSize(new Dimension(90, 32));
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_CARD); table.setForeground(TEXT_WHITE);
        table.setRowHeight(30); table.setShowGrid(false);
        table.setSelectionBackground(POINT_PURPLE);
        table.getTableHeader().setBackground(BG_DARK); table.getTableHeader().setForeground(POINT_CYAN);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(BG_DARK));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                comp.setForeground(isS ? Color.WHITE : TEXT_WHITE);
                setHorizontalAlignment(JLabel.CENTER);
                return comp;
            }
        };
        for(int i=0; i<table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        return table;
    }

    private void applyStockRenderer(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                try {
                    int stock = Integer.parseInt(t.getValueAt(r, 4).toString());
                    if (isS) comp.setForeground(Color.WHITE);
                    else if (stock < 5) comp.setForeground(POINT_RED);
                    else comp.setForeground(TEXT_WHITE);
                } catch(Exception e) {}
                setHorizontalAlignment(JLabel.CENTER);
                return comp;
            }
        });
    }

    private JScrollPane createStyledScrollPane(Component comp) {
        JScrollPane scroll = new JScrollPane(comp);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(new LineBorder(new Color(50, 50, 70)));
        scroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        return scroll;
    }

    static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { this.thumbColor = new Color(70, 70, 100); }
        @Override protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 4, thumbBounds.y + 2, thumbBounds.width - 8, thumbBounds.height - 4, 10, 10);
            g2.dispose();
        }
    }

    // --- 비즈니스 로직 ---

    private void handleProductAction(String type) {
        try {
            if (type.equals("DELETE")) {
                if (currentSelectedProductId != null) productsDAO.deleteProduct(currentSelectedProductId);
            } else {
                String cat = (String) categoryCombo.getSelectedItem();
                ProductsDTO p = new ProductsDTO(currentSelectedProductId, nameField.getText(), 
                        Integer.parseInt(pointField.getText()), Integer.parseInt(stockField.getText()), 
                        imagePathField.getText(), "[" + cat + "] " + descArea.getText());
                if (type.equals("INSERT")) productsDAO.insertProduct(p);
                else productsDAO.updateProduct(p);
            }
            refreshAllData(); if (refreshCallback != null) refreshCallback.run();
            clearProductFields();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "상품 처리 오류: " + ex.getMessage()); }
    }

    private void handleUserAction(String type) {
        try {
            if (type.equals("DELETE")) {
                if (currentSelectedUserId != null) userDAO.deleteUser(currentSelectedUserId);
            } else {
                UserDTO u = new UserDTO();
                u.setUserId(userIdField.getText());
                u.setNickname(nicknameField.getText());
                u.setBalancePoints(Integer.parseInt(userPointField.getText()));
                u.setAdmin(adminCheck.isSelected());
                
                if (type.equals("INSERT")) {
                    u.setPassword(userPwField.getText());
                    userDAO.registerUser(u.getUserId(), u.getPassword(), u.getNickname());
                } else {
                    u.setUserId(currentSelectedUserId);
                    userDAO.updateUserByAdmin(u);
                }
            }
            refreshAllData(); if (refreshCallback != null) refreshCallback.run();
            clearUserFields();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "회원 처리 오류: " + ex.getMessage()); }
    }

    private void clearProductFields() {
        currentSelectedProductId = null; nameField.setText(""); pointField.setText("");
        stockField.setText(""); imagePathField.setText(""); descArea.setText("");
        imagePreviewLabel.setIcon(null); imagePreviewLabel.setText("NO IMAGE");
    }

    private void clearUserFields() {
        currentSelectedUserId = null; userIdField.setText(""); userIdField.setEditable(true);
        userPwField.setText(""); nicknameField.setText(""); userPointField.setText("");
        adminCheck.setSelected(false);
    }

    private void refreshAllData() { loadProductList(); loadUserList(); updateStats(); }

    private void updateStats() {
        try {
            List<UserDTO> users = userDAO.getAllUsers();
            List<ProductsDTO> products = productsDAO.getAllProducts();
            userStat.setValue(users.size() + " 명");
            pointStat.setValue(String.format("%,d P", users.stream().mapToInt(UserDTO::getBalancePoints).sum()));
            stockStat.setValue(products.stream().filter(p -> p.getStock() < 5).count() + " 건");
        } catch (Exception e) {}
    }

    private void loadProductList() {
        productTableModel.setRowCount(0);
        for (ProductsDTO p : productsDAO.getAllProducts()) {
            String desc = p.getDescription();
            String cat = desc.startsWith("[") && desc.contains("]") ? desc.substring(1, desc.indexOf("]")) : "기타";
            productTableModel.addRow(new Object[]{p.getProductId(), cat, p.getProductName(), p.getRequiredPoints(), p.getStock(), p.getImagePath(), desc});
        }
    }

    private void loadUserList() {
        userTableModel.setRowCount(0);
        try {
            for (UserDTO u : userDAO.getAllUsers()) {
                userTableModel.addRow(new Object[]{
                    u.getUserId(), 
                    u.getNickname(), 
                    u.getBalancePoints(), 
                    u.getTotalPoints(), 
                    u.isAdmin() ? "관리자" : u.getGradeName()
                });
            }
        } catch (SQLException e) {}
    }

    private void exportToCSV(JTable table, String defaultFilename) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(defaultFilename));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileChooser.getSelectedFile()), "MS949"))) {
                for (int i = 0; i < table.getColumnCount(); i++) bw.write(table.getColumnName(i) + ",");
                bw.newLine();
                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < table.getColumnCount(); j++) bw.write(table.getValueAt(i, j).toString().replace(",", " ") + ",");
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this, "CSV 저장이 완료되었습니다.");
            } catch (IOException e) { JOptionPane.showMessageDialog(this, "저장 실패: " + e.getMessage()); }
        }
    }

    private void updateImagePreview(String path) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (path == null || path.isEmpty()) { imagePreviewLabel.setIcon(null); imagePreviewLabel.setText("NO IMAGE"); return; }
                ImageIcon icon = path.startsWith("http") ? new ImageIcon(new URL(path)) : new ImageIcon(path);
                imagePreviewLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
                imagePreviewLabel.setText("");
            } catch (Exception e) { imagePreviewLabel.setIcon(null); imagePreviewLabel.setText("N/A"); }
        });
    }

    private void applyProductFilter() {
        String text = productSearchField.getText();
        int option = productSearchOption.getSelectedIndex();
        if (text.isEmpty()) { productSorter.setRowFilter(null); return; }
        if (option == 0) productSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        else productSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, option - 1));
    }

    private void applyUserFilter() {
        String text = userSearchField.getText();
        int option = userSearchOption.getSelectedIndex();
        if (text.isEmpty()) { userSorter.setRowFilter(null); return; }
        if (option == 0) userSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        else userSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, option - 1));
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
}