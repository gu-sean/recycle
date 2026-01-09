package recycle;

import db.DAO.UserDAO;
import db.DAO.ProductsDAO;
import db.DTO.UserDTO;
import db.DTO.ProductsDTO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class AdminWindow extends JPanel {
    private final Runnable refreshCallback;
    private final UserDAO userDAO = new UserDAO();
    private final ProductsDAO productsDAO = new ProductsDAO();

    private JTable productTable;
    private DefaultTableModel productTableModel;
    private JTextField nameField, pointField, stockField, imagePathField;
    private JTextArea descArea;
    private String currentSelectedProductId = null;

    private JTable userTable;
    private DefaultTableModel userTableModel;
    private JTextField userIdField, nicknameField, userPointField;
    private JCheckBox adminCheck;
    private String currentSelectedUserId = null;

    public AdminWindow(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
        setLayout(new BorderLayout());

        JTabbedPane adminTabs = new JTabbedPane();
        adminTabs.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        
        adminTabs.addTab("📦 상품 관리", createProductManagementPanel());
        adminTabs.addTab("👥 사용자 관리", createUserManagementPanel());

        add(adminTabs, BorderLayout.CENTER);
    }

    private JPanel createProductManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("상품 정보 관리"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(20);
        pointField = new JTextField(20);
        stockField = new JTextField(20);      
        imagePathField = new JTextField(15);  
        JButton imageSearchBtn = new JButton("찾기");   
        descArea = new JTextArea(3, 20); descArea.setLineWrap(true);

        String[] labels = {"상품명:", "필요 포인트:", "재고 수량:", "이미지 경로:", "상품 설명:"};
        JComponent[] fields = {nameField, pointField, stockField, null, new JScrollPane(descArea)};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            formPanel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            if (i == 3) {
                JPanel imgP = new JPanel(new BorderLayout(5,0)); imgP.setOpaque(false);
                imgP.add(imagePathField, BorderLayout.CENTER); imgP.add(imageSearchBtn, BorderLayout.EAST);
                formPanel.add(imgP, gbc);
            } else formPanel.add(fields[i], gbc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addBtn = new JButton("신규 등록");
        JButton updateBtn = new JButton("정보 수정");
        JButton deleteBtn = new JButton("상품 삭제");
        JButton clearBtn = new JButton("입력 초기화");

        addBtn.setBackground(new Color(40, 167, 69)); addBtn.setForeground(Color.WHITE);
        updateBtn.setBackground(new Color(0, 123, 255)); updateBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(new Color(220, 53, 69)); deleteBtn.setForeground(Color.WHITE);

        btnPanel.add(addBtn); btnPanel.add(updateBtn); btnPanel.add(deleteBtn); btnPanel.add(clearBtn);

        String[] colNames = {"ID", "상품명", "포인트", "재고", "이미지경로", "설명"};
        productTableModel = new DefaultTableModel(colNames, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        productTable = new JTable(productTableModel);
        loadProductList();

        productTable.getSelectionModel().addListSelectionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row != -1) {
                currentSelectedProductId = (String) productTableModel.getValueAt(row, 0);
                nameField.setText((String) productTableModel.getValueAt(row, 1));
                pointField.setText(String.valueOf(productTableModel.getValueAt(row, 2)));
                stockField.setText(String.valueOf(productTableModel.getValueAt(row, 3)));
                imagePathField.setText((String) productTableModel.getValueAt(row, 4));
                descArea.setText((String) productTableModel.getValueAt(row, 5));
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

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createUserManagementPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBackground(Color.WHITE);
        editPanel.setBorder(BorderFactory.createTitledBorder("선택된 사용자 상세 정보"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        userIdField = new JTextField(15); userIdField.setEditable(false);
        nicknameField = new JTextField(15);
        userPointField = new JTextField(15);
        adminCheck = new JCheckBox("관리자 권한 부여"); adminCheck.setBackground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0; editPanel.add(new JLabel("아이디:"), gbc);
        gbc.gridx = 1; editPanel.add(userIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; editPanel.add(new JLabel("닉네임:"), gbc);
        gbc.gridx = 1; editPanel.add(nicknameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; editPanel.add(new JLabel("보유 포인트:"), gbc);
        gbc.gridx = 1; editPanel.add(userPointField, gbc);
        gbc.gridx = 1; gbc.gridy = 3; editPanel.add(adminCheck, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton userUpdateBtn = new JButton("사용자 정보 수정");
        JButton userDeleteBtn = new JButton("사용자 강제 탈퇴");
        JButton refreshBtn = new JButton("새로고침");

        userUpdateBtn.setBackground(new Color(0, 123, 255)); userUpdateBtn.setForeground(Color.WHITE);
        userDeleteBtn.setBackground(new Color(220, 53, 69)); userDeleteBtn.setForeground(Color.WHITE);

        btnPanel.add(userUpdateBtn); btnPanel.add(userDeleteBtn); btnPanel.add(refreshBtn);

        String[] columnNames = {"아이디", "닉네임", "보유 포인트", "누적 포인트", "권한"};
        userTableModel = new DefaultTableModel(columnNames, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        userTable = new JTable(userTableModel);
        loadUserList();

        userTable.getSelectionModel().addListSelectionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row != -1) {
                currentSelectedUserId = (String) userTableModel.getValueAt(row, 0);
                userIdField.setText(currentSelectedUserId);
                nicknameField.setText((String) userTableModel.getValueAt(row, 1));
                userPointField.setText(String.valueOf(userTableModel.getValueAt(row, 2)));
                adminCheck.setSelected(userTableModel.getValueAt(row, 4).toString().contains("관리자"));
            }
        });

        userUpdateBtn.addActionListener(e -> handleUserAction("UPDATE"));
        userDeleteBtn.addActionListener(e -> handleUserAction("DELETE"));
        refreshBtn.addActionListener(e -> loadUserList());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(editPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        return mainPanel;
    }

    
    private void handleProductAction(String type) {
        try {
            if (type.equals("DELETE")) {
                if (currentSelectedProductId == null) throw new Exception("삭제할 상품을 선택하세요.");
                if (JOptionPane.showConfirmDialog(this, "정말 삭제할까요?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    productsDAO.deleteProduct(currentSelectedProductId);
            } else {
                ProductsDTO p = new ProductsDTO(currentSelectedProductId, nameField.getText().trim(), 
                    Integer.parseInt(pointField.getText()), Integer.parseInt(stockField.getText()), 
                    imagePathField.getText(), descArea.getText());
                if (type.equals("INSERT")) productsDAO.insertProduct(p);
                else productsDAO.updateProduct(p);
            }
            loadProductList(); clearProductFields(); if (refreshCallback != null) refreshCallback.run();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage()); }
    }

    private void handleUserAction(String type) {
        try {
            if (currentSelectedUserId == null) throw new Exception("사용자를 테이블에서 선택하세요.");
            if (type.equals("DELETE")) {
                if (JOptionPane.showConfirmDialog(this, "탈퇴시키겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    userDAO.deleteUser(currentSelectedUserId);
            } else {
                UserDTO u = new UserDTO(); u.setUserId(currentSelectedUserId); 
                u.setNickname(nicknameField.getText().trim()); u.setBalancePoints(Integer.parseInt(userPointField.getText()));
                u.setAdmin(adminCheck.isSelected());
                userDAO.updateUserByAdmin(u);
            }
            loadUserList(); JOptionPane.showMessageDialog(this, "처리 완료");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage()); }
    }

    private void clearProductFields() {
        currentSelectedProductId = null; nameField.setText(""); pointField.setText("");
        stockField.setText(""); imagePathField.setText(""); descArea.setText("");
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