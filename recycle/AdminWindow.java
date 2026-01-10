package recycle;

import db.DAO.UserDAO;
import db.DAO.ProductsDAO;
import db.DTO.UserDTO;
import db.DTO.ProductsDTO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.net.URL;

public class AdminWindow extends JPanel {
    private final Runnable refreshCallback;
    private final UserDAO userDAO = new UserDAO();
    private final ProductsDAO productsDAO = new ProductsDAO();

    private static final Color BG_DARK = new Color(20, 15, 40);      
    private static final Color BG_CARD = new Color(30, 25, 60);      
    private static final Color POINT_PURPLE = new Color(150, 100, 255); 
    private static final Color POINT_CYAN = new Color(0, 255, 240);    
    private static final Color POINT_RED = new Color(255, 80, 120); 
    private static final Color TEXT_WHITE = new Color(240, 240, 240);

    private JLabel statsLabel;

    private JTable productTable;
    private DefaultTableModel productTableModel;
    private TableRowSorter<DefaultTableModel> productSorter;
    private JTextField nameField, pointField, stockField, imagePathField, productSearchField;
    private JTextArea descArea;
    private JLabel imagePreviewLabel;
    private String currentSelectedProductId = null;

    private JTable userTable;
    private DefaultTableModel userTableModel;
    private TableRowSorter<DefaultTableModel> userSorter;
    private JTextField userIdField, userPwField, nicknameField, userPointField, userSearchField;
    private JCheckBox adminCheck;
    private String currentSelectedUserId = null;

    public AdminWindow(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
        setLayout(new BorderLayout(0, 10));
        setBackground(BG_DARK); 
        setBorder(new EmptyBorder(20, 20, 20, 20));

        setupDashboard();
        setupTabs();
        
        refreshAllData();
    }

    private void setupDashboard() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(BG_CARD);
        dashboardPanel.setBorder(new LineBorder(POINT_PURPLE, 1));
        
        statsLabel = new JLabel("시스템 상태 로드 중...", JLabel.CENTER);
        statsLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        statsLabel.setForeground(POINT_CYAN);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        dashboardPanel.add(statsLabel, BorderLayout.CENTER);
        add(dashboardPanel, BorderLayout.NORTH);
    }

    private void setupTabs() {
        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        adminTabs.addTab("📦 상품 관리", createProductManagementPanel());
        adminTabs.addTab("👥 사용자 관리", createUserManagementPanel());
        add(adminTabs, BorderLayout.CENTER);
    }

    private void refreshAllData() {
        loadProductList();
        loadUserList();
        updateStats();
    }

    private void updateStats() {
        try {
            List<UserDTO> users = userDAO.getAllUsers();
            long lowStock = productsDAO.getAllProducts().stream().filter(p -> p.getStock() < 5).count();
            int totalPoints = users.stream().mapToInt(UserDTO::getBalancePoints).sum();
            
            statsLabel.setText(String.format("📊 실시간 대시보드  |  총 회원: %d명  |  유통 포인트: %,d P  |  재고 부족: %d건", 
                               users.size(), totalPoints, lowStock));
        } catch (Exception e) { 
            statsLabel.setText("데이터 연결 오류"); 
        }
    }

    private JPanel createProductManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_DARK);

        JPanel topWrapper = new JPanel(new BorderLayout(10, 0));
        topWrapper.setOpaque(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_CARD);
        formPanel.setBorder(createCustomTitledBorder("상품 상세 설정"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 15, 4, 15); gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = createStyledTextField(20); 
        pointField = createStyledTextField(20);
        stockField = createStyledTextField(20); 
        imagePathField = createStyledTextField(15);
        descArea = new JTextArea(3, 20); 
        descArea.setBackground(BG_DARK); descArea.setForeground(Color.WHITE);
        descArea.setCaretColor(Color.WHITE); descArea.setLineWrap(true);

        String[] labels = {"상품명", "필요 포인트", "재고 수량", "이미지 경로", "상품 설명"};
        JComponent[] fields = {nameField, pointField, stockField, imagePathField, new JScrollPane(descArea)};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel lbl = new JLabel(labels[i]); lbl.setForeground(TEXT_WHITE);
            formPanel.add(lbl, gbc);
            gbc.gridx = 1; formPanel.add(fields[i], gbc);
        }

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(BG_CARD);
        previewPanel.setPreferredSize(new Dimension(180, 0));
        previewPanel.setBorder(createCustomTitledBorder("미리보기"));
        imagePreviewLabel = new JLabel("이미지 없음", JLabel.CENTER);
        imagePreviewLabel.setForeground(Color.GRAY);
        previewPanel.add(imagePreviewLabel, BorderLayout.CENTER);
        imagePathField.addCaretListener(e -> updateImagePreview(imagePathField.getText()));

        topWrapper.add(formPanel, BorderLayout.CENTER);
        topWrapper.add(previewPanel, BorderLayout.EAST);

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JButton addBtn = createStyledButton("신규 등록", new Color(40, 167, 69));
        JButton updateBtn = createStyledButton("정보 수정", new Color(0, 123, 255));
        JButton deleteBtn = createStyledButton("상품 삭제", new Color(220, 53, 69));
        JButton clearBtn = createStyledButton("초기화", Color.GRAY);
        
        productSearchField = createStyledTextField(15);
        productSearchField.setMaximumSize(new Dimension(180, 30));
        JLabel searchIcon = new JLabel(" 🔍 검색: ");
        searchIcon.setForeground(POINT_CYAN);
        productSearchField.addCaretListener(e -> {
            String text = productSearchField.getText();
            productSorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
        });

        btnPanel.add(Box.createHorizontalGlue()); 
        btnPanel.add(addBtn); btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(updateBtn); btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(deleteBtn); btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(clearBtn); 
        btnPanel.add(Box.createHorizontalStrut(30));
        btnPanel.add(searchIcon); btnPanel.add(productSearchField);

        productTableModel = new DefaultTableModel(new String[]{"ID", "상품명", "포인트", "재고", "이미지경로", "설명"}, 0);
        productTable = createStyledTable(productTableModel);
        applyStockRenderer(productTable); 
        productSorter = new TableRowSorter<>(productTableModel);
        productTable.setRowSorter(productSorter);

        productTable.getSelectionModel().addListSelectionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row != -1) {
                int mRow = productTable.convertRowIndexToModel(row);
                currentSelectedProductId = String.valueOf(productTableModel.getValueAt(mRow, 0));
                nameField.setText((String) productTableModel.getValueAt(mRow, 1));
                pointField.setText(String.valueOf(productTableModel.getValueAt(mRow, 2)));
                stockField.setText(String.valueOf(productTableModel.getValueAt(mRow, 3)));
                imagePathField.setText((String) productTableModel.getValueAt(mRow, 4));
                descArea.setText((String) productTableModel.getValueAt(mRow, 5));
            }
        });

        addBtn.addActionListener(e -> handleProductAction("INSERT"));
        updateBtn.addActionListener(e -> handleProductAction("UPDATE"));
        deleteBtn.addActionListener(e -> handleProductAction("DELETE"));
        clearBtn.addActionListener(e -> clearProductFields());

        mainPanel.add(topWrapper, BorderLayout.NORTH);
        mainPanel.add(btnPanel, BorderLayout.CENTER);
        mainPanel.add(createStyledScrollPane(productTable), BorderLayout.SOUTH);
        mainPanel.getComponent(2).setPreferredSize(new Dimension(0, 300));

        return mainPanel;
    }

    private JPanel createUserManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_DARK);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBackground(BG_CARD);
        editPanel.setBorder(createCustomTitledBorder("회원 관리 (등록/수정)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 15, 5, 15); gbc.fill = GridBagConstraints.HORIZONTAL;

        userIdField = createStyledTextField(15);
        userPwField = createStyledTextField(15);
        nicknameField = createStyledTextField(15);
        userPointField = createStyledTextField(15);
        adminCheck = new JCheckBox("관리자 권한"); 
        adminCheck.setBackground(BG_CARD); adminCheck.setForeground(POINT_CYAN);

        String[] labels = {"아이디", "비밀번호", "닉네임", "보유 포인트"};
        JTextField[] fields = {userIdField, userPwField, nicknameField, userPointField};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel lbl = new JLabel(labels[i]); lbl.setForeground(TEXT_WHITE);
            editPanel.add(lbl, gbc);
            gbc.gridx = 1; editPanel.add(fields[i], gbc);
        }
        gbc.gridy = 4; editPanel.add(adminCheck, gbc);

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JButton userAddBtn = createStyledButton("회원 등록", new Color(40, 167, 69));
        JButton userUpdateBtn = createStyledButton("정보 수정", new Color(0, 123, 255));
        JButton userDeleteBtn = createStyledButton("회원 삭제", new Color(220, 53, 69));
        JButton clearBtn = createStyledButton("초기화", Color.GRAY);
        
        userSearchField = createStyledTextField(15);
        userSearchField.setMaximumSize(new Dimension(180, 30));
        JLabel searchIcon = new JLabel(" 🔍 검색: ");
        searchIcon.setForeground(POINT_CYAN);
        userSearchField.addCaretListener(e -> {
            String text = userSearchField.getText();
            userSorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
        });

        btnPanel.add(Box.createHorizontalGlue()); 
        btnPanel.add(userAddBtn); btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(userUpdateBtn); btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(userDeleteBtn); btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(clearBtn); 
        btnPanel.add(Box.createHorizontalStrut(30));
        btnPanel.add(searchIcon); btnPanel.add(userSearchField);

        userTableModel = new DefaultTableModel(new String[]{"아이디", "닉네임", "보유 포인트", "누적 포인트", "권한"}, 0);
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
                userPwField.setText("********");
                nicknameField.setText((String) userTableModel.getValueAt(mRow, 1));
                userPointField.setText(userTableModel.getValueAt(mRow, 2).toString());
                adminCheck.setSelected(userTableModel.getValueAt(mRow, 4).toString().contains("관리자"));
            }
        });

        userAddBtn.addActionListener(e -> handleUserAction("INSERT"));
        userUpdateBtn.addActionListener(e -> handleUserAction("UPDATE"));
        userDeleteBtn.addActionListener(e -> handleUserAction("DELETE"));
        clearBtn.addActionListener(e -> clearUserFields());

        topPanel.add(editPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(createStyledScrollPane(userTable), BorderLayout.CENTER);

        return mainPanel;
    }

    
    private void handleProductAction(String type) {
        try {
            if (type.equals("DELETE")) {
                if (currentSelectedProductId == null) throw new Exception("삭제할 상품을 테이블에서 선택해주세요.");
                if (JOptionPane.showConfirmDialog(this, "상품을 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    productsDAO.deleteProduct(currentSelectedProductId);
                } else return;
            } else {
       
                String name = nameField.getText().trim();
                if (name.isEmpty()) throw new Exception("상품명을 입력해주세요.");
                
                int points, stock;
                try {
                    points = Integer.parseInt(pointField.getText().trim());
                    stock = Integer.parseInt(stockField.getText().trim());
                    if (points < 0 || stock < 0) throw new Exception("포인트와 재고는 0 이상이어야 합니다.");
                } catch (NumberFormatException e) {
                    throw new Exception("포인트와 재고는 숫자만 입력 가능합니다.");
                }

                ProductsDTO p = new ProductsDTO(currentSelectedProductId, name, points, stock, 
                                                imagePathField.getText().trim(), descArea.getText().trim());
                
                if (type.equals("INSERT")) productsDAO.insertProduct(p);
                else productsDAO.updateProduct(p);
            }

            refreshAllData(); 
            clearProductFields(); 
            if (refreshCallback != null) refreshCallback.run(); 
            JOptionPane.showMessageDialog(this, "성공적으로 처리되었습니다.");
            
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "실패", JOptionPane.ERROR_MESSAGE); 
        }
    }

    private void handleUserAction(String type) {
        try {
            if (type.equals("DELETE")) {
                if (currentSelectedUserId == null) throw new Exception("삭제할 회원을 선택해주세요.");
                if (JOptionPane.showConfirmDialog(this, "회원을 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    userDAO.deleteUser(currentSelectedUserId);
                } else return;
            } else {
      
                String id = userIdField.getText().trim();
                String nick = nicknameField.getText().trim();
                if (id.isEmpty() || nick.isEmpty()) throw new Exception("아이디와 닉네임은 필수입니다.");
                
                int points;
                try {
                    points = Integer.parseInt(userPointField.getText().trim());
                } catch (NumberFormatException e) {
                    throw new Exception("포인트는 숫자만 입력 가능합니다.");
                }

                if (type.equals("INSERT")) {
                    String pw = userPwField.getText().trim();
                    if (pw.isEmpty() || pw.equals("********")) throw new Exception("신규 등록 시 비밀번호는 필수입니다.");
                    userDAO.registerUser(new UserDTO(id, pw, nick, points, points, adminCheck.isSelected()));
                } else {
                    UserDTO u = new UserDTO(); 
                    u.setUserId(currentSelectedUserId); 
                    u.setNickname(nick);
                    u.setBalancePoints(points); 
                    u.setAdmin(adminCheck.isSelected());
                    userDAO.updateUserByAdmin(u);
                }
            }

            refreshAllData(); 
            clearUserFields();
            if (refreshCallback != null) refreshCallback.run();
            JOptionPane.showMessageDialog(this, "회원 정보가 반영되었습니다.");
            
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "실패", JOptionPane.ERROR_MESSAGE); 
        }
    }


    private void updateImagePreview(String path) {
        if (path == null || path.trim().isEmpty()) {
            imagePreviewLabel.setIcon(null); imagePreviewLabel.setText("이미지 없음");
            return;
        }
        try {
            ImageIcon icon = path.startsWith("http") ? new ImageIcon(new URL(path)) : new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            imagePreviewLabel.setIcon(new ImageIcon(img)); imagePreviewLabel.setText("");
        } catch (Exception e) {
            imagePreviewLabel.setIcon(null); imagePreviewLabel.setText("로드 실패");
        }
    }

    private void applyStockRenderer(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                try {
                    int stock = Integer.parseInt(table.getModel().getValueAt(table.convertRowIndexToModel(row), 3).toString());
                    c.setForeground(stock < 5 ? POINT_RED : (isSelected ? Color.WHITE : TEXT_WHITE));
                } catch (Exception e) { c.setForeground(TEXT_WHITE); }
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });
    }

    private void clearProductFields() {
        currentSelectedProductId = null; nameField.setText(""); pointField.setText(""); stockField.setText("");
        imagePathField.setText(""); descArea.setText(""); productSearchField.setText("");
        imagePreviewLabel.setIcon(null); imagePreviewLabel.setText("이미지 없음"); productTable.clearSelection();
    }

    private void clearUserFields() {
        currentSelectedUserId = null; userIdField.setText(""); userIdField.setEditable(true);
        userPwField.setText(""); nicknameField.setText(""); userPointField.setText(""); userSearchField.setText(""); 
        adminCheck.setSelected(false); userTable.clearSelection();
    }

    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(BG_DARK); scroll.setBorder(new LineBorder(POINT_PURPLE, 1));
        return scroll;
    }

    private JTextField createStyledTextField(int size) {
        JTextField tf = new JTextField(size); tf.setBackground(BG_DARK); tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE); tf.setBorder(new CompoundBorder(new LineBorder(new Color(80, 80, 100)), new EmptyBorder(2,5,2,5)));
        return tf;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text); btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model); table.setBackground(BG_CARD); table.setForeground(TEXT_WHITE);
        table.setGridColor(new Color(60, 60, 90)); table.setRowHeight(35); table.setSelectionBackground(POINT_PURPLE);
        JTableHeader header = table.getTableHeader(); header.setBackground(new Color(50, 50, 80)); header.setForeground(POINT_CYAN);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
        
        return table;
    }

    private TitledBorder createCustomTitledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(new LineBorder(POINT_PURPLE), title);
        tb.setTitleColor(POINT_CYAN); tb.setTitleFont(new Font("맑은 고딕", Font.BOLD, 13)); return tb;
    }

    private void loadProductList() {
        productTableModel.setRowCount(0);
        for (ProductsDTO p : productsDAO.getAllProducts()) 
            productTableModel.addRow(new Object[]{p.getProductId(), p.getProductName(), p.getRequiredPoints(), p.getStock(), p.getImagePath(), p.getDescription()});
    }

    private void loadUserList() {
        userTableModel.setRowCount(0);
        try {
            for (UserDTO u : userDAO.getAllUsers())
                userTableModel.addRow(new Object[]{u.getUserId(), u.getNickname(), u.getBalancePoints(), u.getTotalPoints(), u.isAdmin() ? "✅ 관리자" : "일반회원"});
        } catch (SQLException e) { e.printStackTrace(); }
    }
}