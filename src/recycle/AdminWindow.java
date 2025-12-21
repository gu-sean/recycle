package recycle;

import db.DAO.UserDAO;
import db.DAO.ProductsDAO;
import db.DTO.UserDTO;
import db.DTO.ProductsDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminWindow extends JPanel {
    private final Runnable refreshCallback;
    private final UserDAO userDAO = new UserDAO();
    private final ProductsDAO productsDAO = new ProductsDAO();

    // --- 네온 다크 퍼플 테마 색상 ---
    private static final Color BG_DARK = new Color(20, 15, 40);
    private static final Color BG_CARD = new Color(35, 30, 70);
    private static final Color POINT_PURPLE = new Color(150, 100, 255);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color TEXT_WHITE = new Color(240, 240, 240);

    // --- 통계 대시보드 ---
    private JLabel statsLabel;

    // --- 상품 관리 컴포넌트 ---
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private TableRowSorter<DefaultTableModel> productSorter;
    private JTextField nameField, pointField, stockField, imagePathField, productSearchField;
    private JTextArea descArea;
    private String currentSelectedProductId = null;

    // --- 사용자 관리 컴포넌트 ---
    private JTable userTable;
    private DefaultTableModel userTableModel;
    private TableRowSorter<DefaultTableModel> userSorter;
    private JTextField userIdField, nicknameField, userPointField, userSearchField;
    private JCheckBox adminCheck;
    private String currentSelectedUserId = null;

    public AdminWindow(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
        setLayout(new BorderLayout(0, 10));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. 상단 대시보드 영역 (네온 스타일)
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(BG_CARD);
        dashboardPanel.setBorder(new LineBorder(POINT_PURPLE, 1));
        
        statsLabel = new JLabel("시스템 상태 로드 중...", JLabel.CENTER);
        statsLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        statsLabel.setForeground(POINT_CYAN);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        dashboardPanel.add(statsLabel, BorderLayout.CENTER);
        add(dashboardPanel, BorderLayout.NORTH);

        // 2. 탭 설정 (다크 테마 커스텀)
        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        
        adminTabs.addTab("📦 상품 관리", createProductManagementPanel());
        adminTabs.addTab("👥 사용자 관리", createUserManagementPanel());
        adminTabs.addTab("📜 활동 로그", createLogPanel());

        add(adminTabs, BorderLayout.CENTER);
        
        refreshAllData();
    }

    private void refreshAllData() {
        loadProductList();
        loadUserList();
        updateStats();
    }

    private void updateStats() {
        try {
            List<UserDTO> users = userDAO.getAllUsers();
            int userCount = users.size();
            int totalPoints = users.stream().mapToInt(UserDTO::getBalancePoints).sum();
            long lowStock = productsDAO.getAllProducts().stream().filter(p -> p.getStock() < 5).count();
            
            statsLabel.setText(String.format("📊 실시간 대시보드  |  총 회원: %d명  |  유통 포인트: %,d P  |  재고 부족: %d건", 
                               userCount, totalPoints, lowStock));
        } catch (Exception e) {
            statsLabel.setText("통계 정보를 불러오지 못했습니다.");
        }
    }

    // ============================================================
    // 1. 상품 관리 패널 (다크 디자인 적용)
    // ============================================================
    private JPanel createProductManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_DARK);

        // --- 상단: 검색 및 폼 ---
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setOpaque(false);

        // 검색 바
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍 상품 검색: ");
        searchIcon.setForeground(TEXT_WHITE);
        productSearchField = createStyledTextField(20);
        productSearchField.addCaretListener(e -> {
            String text = productSearchField.getText();
            if (text.trim().length() == 0) productSorter.setRowFilter(null);
            else productSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        });
        searchBar.add(searchIcon); searchBar.add(productSearchField);

        // 입력 폼
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_CARD);
        formPanel.setBorder(createCustomTitledBorder("상품 상세 설정"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 15, 5, 15); gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = createStyledTextField(20); pointField = createStyledTextField(20);
        stockField = createStyledTextField(20); imagePathField = createStyledTextField(15);
        JButton imageSearchBtn = createStyledButton("찾기", BG_DARK);
        descArea = new JTextArea(3, 20); 
        descArea.setBackground(BG_DARK); descArea.setForeground(Color.WHITE);
        descArea.setCaretColor(Color.WHITE); descArea.setLineWrap(true);

        String[] labels = {"상품명", "필요 포인트", "재고 수량", "이미지 경로", "상품 설명"};
        JComponent[] fields = {nameField, pointField, stockField, null, new JScrollPane(descArea)};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel lbl = new JLabel(labels[i]); lbl.setForeground(TEXT_WHITE);
            formPanel.add(lbl, gbc);
            gbc.gridx = 1;
            if (i == 3) {
                JPanel imgP = new JPanel(new BorderLayout(5,0)); imgP.setOpaque(false);
                imgP.add(imagePathField, BorderLayout.CENTER); imgP.add(imageSearchBtn, BorderLayout.EAST);
                formPanel.add(imgP, gbc);
            } else formPanel.add(fields[i], gbc);
        }

        // 버튼 영역
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);
        JButton addBtn = createStyledButton("신규 등록", new Color(40, 167, 69));
        JButton updateBtn = createStyledButton("정보 수정", new Color(0, 123, 255));
        JButton deleteBtn = createStyledButton("상품 삭제", new Color(220, 53, 69));
        JButton clearBtn = createStyledButton("초기화", Color.GRAY);

        btnPanel.add(addBtn); btnPanel.add(updateBtn); btnPanel.add(deleteBtn); btnPanel.add(clearBtn);

        topPanel.add(searchBar, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        // --- 하단: 테이블 ---
        String[] colNames = {"ID", "상품명", "포인트", "재고", "이미지경로", "설명"};
        productTableModel = new DefaultTableModel(colNames, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        productTable = createStyledTable(productTableModel);
        productSorter = new TableRowSorter<>(productTableModel);
        productTable.setRowSorter(productSorter);

        productTable.getSelectionModel().addListSelectionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row != -1) {
                int modelRow = productTable.convertRowIndexToModel(row);
                currentSelectedProductId = String.valueOf(productTableModel.getValueAt(modelRow, 0));
                nameField.setText((String) productTableModel.getValueAt(modelRow, 1));
                pointField.setText(String.valueOf(productTableModel.getValueAt(modelRow, 2)));
                stockField.setText(String.valueOf(productTableModel.getValueAt(modelRow, 3)));
                imagePathField.setText((String) productTableModel.getValueAt(modelRow, 4));
                descArea.setText((String) productTableModel.getValueAt(modelRow, 5));
            }
        });

        imageSearchBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) imagePathField.setText(chooser.getSelectedFile().getAbsolutePath());
        });

        clearBtn.addActionListener(e -> clearProductFields());
        addBtn.addActionListener(e -> handleProductAction("INSERT"));
        updateBtn.addActionListener(e -> handleProductAction("UPDATE"));
        deleteBtn.addActionListener(e -> handleProductAction("DELETE"));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        return mainPanel;
    }

    // ============================================================
    // 2. 사용자 관리 패널
    // ============================================================
    private JPanel createUserManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_DARK);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍 사용자 검색: ");
        searchIcon.setForeground(TEXT_WHITE);
        userSearchField = createStyledTextField(20);
        userSearchField.addCaretListener(e -> {
            String text = userSearchField.getText();
            if (text.trim().length() == 0) userSorter.setRowFilter(null);
            else userSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        });
        searchBar.add(searchIcon); searchBar.add(userSearchField);

        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBackground(BG_CARD);
        editPanel.setBorder(createCustomTitledBorder("회원 정보 수정"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20); gbc.fill = GridBagConstraints.HORIZONTAL;

        userIdField = createStyledTextField(15); userIdField.setEditable(false);
        nicknameField = createStyledTextField(15); userPointField = createStyledTextField(15);
        adminCheck = new JCheckBox("관리자 권한 부여"); 
        adminCheck.setBackground(BG_CARD); adminCheck.setForeground(POINT_CYAN);

        gbc.gridx = 0; gbc.gridy = 0; 
        JLabel lbl1 = new JLabel("아이디:"); lbl1.setForeground(TEXT_WHITE); editPanel.add(lbl1, gbc);
        gbc.gridx = 1; editPanel.add(userIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; 
        JLabel lbl2 = new JLabel("닉네임:"); lbl2.setForeground(TEXT_WHITE); editPanel.add(lbl2, gbc);
        gbc.gridx = 1; editPanel.add(nicknameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; 
        JLabel lbl3 = new JLabel("보유 포인트:"); lbl3.setForeground(TEXT_WHITE); editPanel.add(lbl3, gbc);
        gbc.gridx = 1; editPanel.add(userPointField, gbc);
        gbc.gridx = 1; gbc.gridy = 3; editPanel.add(adminCheck, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);
        JButton userUpdateBtn = createStyledButton("정보 수정", new Color(0, 123, 255));
        JButton userDeleteBtn = createStyledButton("강제 탈퇴", new Color(220, 53, 69));
        JButton refreshBtn = createStyledButton("새로고침", Color.GRAY);

        btnPanel.add(userUpdateBtn); btnPanel.add(userDeleteBtn); btnPanel.add(refreshBtn);

        topPanel.add(searchBar, BorderLayout.NORTH);
        topPanel.add(editPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        String[] columnNames = {"아이디", "닉네임", "보유 포인트", "누적 포인트", "권한"};
        userTableModel = new DefaultTableModel(columnNames, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        userTable = createStyledTable(userTableModel);
        userSorter = new TableRowSorter<>(userTableModel);
        userTable.setRowSorter(userSorter);

        userTable.getSelectionModel().addListSelectionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row != -1) {
                int modelRow = userTable.convertRowIndexToModel(row);
                currentSelectedUserId = (String) userTableModel.getValueAt(modelRow, 0);
                userIdField.setText(currentSelectedUserId);
                nicknameField.setText((String) userTableModel.getValueAt(modelRow, 1));
                userPointField.setText(String.valueOf(userTableModel.getValueAt(modelRow, 2)));
                adminCheck.setSelected(userTableModel.getValueAt(modelRow, 4).toString().contains("관리자"));
            }
        });

        userUpdateBtn.addActionListener(e -> handleUserAction("UPDATE"));
        userDeleteBtn.addActionListener(e -> handleUserAction("DELETE"));
        refreshBtn.addActionListener(e -> refreshAllData());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20,20,20,20));
        
        JLabel logTitle = new JLabel("📜 시스템 활동 로그 (최신순)", JLabel.CENTER);
        logTitle.setForeground(POINT_CYAN);
        logTitle.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        
        String[] columns = {"시간", "ID", "활동", "포인트 변동", "상세내역"};
        DefaultTableModel logModel = new DefaultTableModel(columns, 0);
        JTable logTable = createStyledTable(logModel);
        
        panel.add(logTitle, BorderLayout.NORTH);
        panel.add(new JScrollPane(logTable), BorderLayout.CENTER);
        
        JButton refreshLog = createStyledButton("로그 새로고침", POINT_PURPLE);
        panel.add(refreshLog, BorderLayout.SOUTH);
        
        return panel;
    }

    // --- 유틸리티 UI 생성 메서드 ---

    private JTextField createStyledTextField(int size) {
        JTextField tf = new JTextField(size);
        tf.setBackground(BG_DARK);
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(new LineBorder(new Color(80, 80, 100)));
        return tf;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        return btn;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_CARD);
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 90));
        table.setRowHeight(30);
        table.setSelectionBackground(POINT_PURPLE);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(50, 50, 80));
        header.setForeground(POINT_CYAN);
        header.setFont(new Font("맑은 고딕", Font.BOLD, 13));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        return table;
    }

    private TitledBorder createCustomTitledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(new LineBorder(POINT_PURPLE), title);
        tb.setTitleColor(POINT_CYAN);
        tb.setTitleFont(new Font("맑은 고딕", Font.BOLD, 13));
        return tb;
    }

    // --- 비즈니스 로직 ---

    private void handleProductAction(String type) {
        try {
            if (type.equals("DELETE")) {
                if (currentSelectedProductId == null) throw new Exception("삭제할 상품을 선택하세요.");
                if (JOptionPane.showConfirmDialog(this, "상품을 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    productsDAO.deleteProduct(currentSelectedProductId);
            } else {
                ProductsDTO p = new ProductsDTO(currentSelectedProductId, nameField.getText().trim(), 
                    Integer.parseInt(pointField.getText()), Integer.parseInt(stockField.getText()), 
                    imagePathField.getText(), descArea.getText());
                if (type.equals("INSERT")) productsDAO.insertProduct(p);
                else productsDAO.updateProduct(p);
            }
            refreshAllData(); clearProductFields(); if (refreshCallback != null) refreshCallback.run();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage()); }
    }

    private void handleUserAction(String type) {
        try {
            if (currentSelectedUserId == null) throw new Exception("사용자를 선택하세요.");
            if (type.equals("DELETE")) {
                if (JOptionPane.showConfirmDialog(this, "사용자를 탈퇴시키겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    userDAO.deleteUser(currentSelectedUserId);
            } else {
                UserDTO u = new UserDTO(); u.setUserId(currentSelectedUserId); 
                u.setNickname(nicknameField.getText().trim()); u.setBalancePoints(Integer.parseInt(userPointField.getText()));
                u.setAdmin(adminCheck.isSelected());
                userDAO.updateUserByAdmin(u);
            }
            refreshAllData(); JOptionPane.showMessageDialog(this, "처리 완료");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage()); }
    }

    private void clearProductFields() {
        currentSelectedProductId = null; nameField.setText(""); pointField.setText("");
        stockField.setText(""); imagePathField.setText(""); descArea.setText("");
        productTable.clearSelection();
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