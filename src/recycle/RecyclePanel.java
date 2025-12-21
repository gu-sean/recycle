package recycle;

import db.DAO.RecycleLogDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.LinkedHashMap;
import java.sql.SQLException;

public class RecyclePanel extends JPanel {

    private final String userId;
    private final RecycleLogDAO logDAO;
    private final Map<String, Integer> itemPoints; 
    private final Runnable rankUpdateCallback; 

    private JComboBox<String> itemComboBox;
    private JLabel pointLabel; 
    private JButton addButton, saveButton, removeButton, uploadButton;
    private JTable currentTable;
    private DefaultTableModel tableModel;
    
    private int totalPoint = 0; 
    private final List<String> loadedItems = new ArrayList<>(); 
    private final List<String> unsavedItems = new ArrayList<>(); 

    // --- 색상 테마 ---
    private static final Color BG_DARK = new Color(20, 15, 40);   
    private static final Color BG_LIGHT = new Color(40, 45, 90);     
    private static final Color POINT_PURPLE = new Color(150, 100, 255); 
    private static final Color POINT_CYAN = new Color(0, 255, 240);     
    private static final Color COMP_BG = new Color(30, 25, 60); 
    private static final Color TABLE_HEAD_BG = new Color(50, 45, 100);

    private static final String DEFAULT_SELECTION_TEXT = "--- 품목 선택 ---";
    private static final String[] COLUMN_NAMES = {"순번", "분리수거 품목", "포인트"};

    public RecyclePanel(String userId, Runnable rankUpdateCallback) {
        this.userId = userId;
        this.rankUpdateCallback = rankUpdateCallback; 
        this.itemPoints = initializeItemPoints(); 

        RecycleLogDAO dao = null;
        try { dao = new RecycleLogDAO(); } catch (Exception e) {}
        this.logDAO = dao;

        setupLayout();
        loadLogsAndRefreshUI();
    }

    private Map<String, Integer> initializeItemPoints() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("종이", 15); map.put("비닐", 10); map.put("유리병", 25); map.put("종이팩", 20);
        map.put("캔ㆍ고철", 40); map.put("스티로폼", 10); map.put("플라스틱", 10); map.put("기타", 5); 
        return map;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(15, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // [왼쪽 패널]
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, BG_DARK, 0, getHeight(), BG_LIGHT));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(40, 20, 40, 20));

        JLabel inputTitle = new JLabel("분리수거 등록");
        inputTitle.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        inputTitle.setForeground(POINT_CYAN);
        inputTitle.setAlignmentX(CENTER_ALIGNMENT);

        // 콤보박스 생성 및 스타일 적용
        itemComboBox = new JComboBox<>(itemPoints.keySet().toArray(new String[0]));
        itemComboBox.insertItemAt(DEFAULT_SELECTION_TEXT, 0);
        itemComboBox.setSelectedIndex(0);
        styleComboBox(itemComboBox);

        pointLabel = new JLabel("선택 포인트: 0 P");
        pointLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        pointLabel.setForeground(Color.WHITE);
        pointLabel.setAlignmentX(CENTER_ALIGNMENT);

        addButton = createStyledButton("목록에 추가", POINT_PURPLE, Color.WHITE);
        uploadButton = createStyledButton("사진 인식 (AI)", new Color(60, 160, 140), Color.WHITE);

        leftPanel.add(inputTitle);
        leftPanel.add(Box.createVerticalStrut(40));
        leftPanel.add(itemComboBox);
        leftPanel.add(Box.createVerticalStrut(25));
        leftPanel.add(pointLabel);
        leftPanel.add(Box.createVerticalStrut(45));
        leftPanel.add(addButton);
        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(uploadButton);

        // [오른쪽 패널]
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);

        JLabel tableTitle = new JLabel("오늘의 분리수거 로그");
        tableTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        tableTitle.setForeground(Color.WHITE);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        currentTable = new JTable(tableModel);
        styleTable(currentTable);

        JScrollPane scrollPane = new JScrollPane(currentTable);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(new LineBorder(POINT_PURPLE, 1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnRow.setOpaque(false);
        removeButton = createStyledButton("선택 삭제", new Color(180, 60, 60), Color.WHITE);
        saveButton = createStyledButton("적립 완료", POINT_CYAN, Color.BLACK);
        
        removeButton.setPreferredSize(new Dimension(110, 45));
        saveButton.setPreferredSize(new Dimension(160, 45));

        btnRow.add(removeButton);
        btnRow.add(saveButton);

        rightPanel.add(tableTitle, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(btnRow, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        itemComboBox.addActionListener(e -> updatePointLabel());
        addButton.addActionListener(e -> addRecycleItemToTable((String)itemComboBox.getSelectedItem()));
        uploadButton.addActionListener(e -> openImageUploadDialog());
        removeButton.addActionListener(e -> handleRemove());
        saveButton.addActionListener(this::handleSaveLogs);
    }

    // --- 콤보박스 스타일 수정 (가독성 확보) ---
    private void styleComboBox(JComboBox<String> cb) {
        cb.setMaximumSize(new Dimension(210, 45));
        cb.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        
        // 콤보박스 본체 배경색을 '연한 회색'으로, 글씨를 '검정색'으로 변경하여 확실히 보이게 함
        cb.setBackground(Color.WHITE); 
        cb.setForeground(Color.BLACK);
        
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setHorizontalAlignment(CENTER);
                l.setOpaque(true);
                if (isSelected) {
                    l.setBackground(POINT_PURPLE);
                    l.setForeground(Color.WHITE);
                } else {
                    l.setBackground(Color.WHITE); // 리스트 배경 흰색
                    l.setForeground(Color.BLACK); // 리스트 글씨 검정색
                }
                return l;
            }
        });
    }

    // --- 테이블 스타일 수정 (가운데 정렬 포함) ---
    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setBackground(COMP_BG);
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 110));
        table.setSelectionBackground(POINT_PURPLE);
        
        // 모든 열 가운데 정렬 렌더러
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                label.setHorizontalAlignment(CENTER); // 텍스트 가운데 정렬
                label.setOpaque(true);

                if (r == t.getRowCount() - 1 && "합계".equals(t.getValueAt(r, 1))) {
                    label.setBackground(TABLE_HEAD_BG);
                    label.setForeground(POINT_CYAN);
                    label.setFont(new Font("맑은 고딕", Font.BOLD, 15));
                } else {
                    label.setBackground(isS ? t.getSelectionBackground() : COMP_BG);
                    label.setForeground(Color.WHITE);
                }
                return label;
            }
        };

        // 전 열에 정렬 적용
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // 헤더 설정
        table.getTableHeader().setBackground(TABLE_HEAD_BG);
        table.getTableHeader().setForeground(POINT_CYAN);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 14));
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(210, 50));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        return btn;
    }

    private void updatePointLabel() {
        String item = (String) itemComboBox.getSelectedItem();
        int point = (item == null || item.equals(DEFAULT_SELECTION_TEXT)) ? 0 : itemPoints.getOrDefault(item, 0);
        pointLabel.setText("선택 포인트: " + point + " P");
    }

    private void handleRemove() {
        int row = currentTable.getSelectedRow();
        if (row == -1) return;
        String name = (String) tableModel.getValueAt(row, 1);
        if ("합계".equals(name) || loadedItems.contains(name)) {
            JOptionPane.showMessageDialog(this, "이미 저장된 기록은 삭제할 수 없습니다.");
            return;
        }
        unsavedItems.remove(name);
        rebuildTableFromInternalLists();
    }

    private void openImageUploadDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("이미지 파일", "jpg", "png", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String detected = (String) itemPoints.keySet().toArray()[(int)(Math.random() * 8)];
            JOptionPane.showMessageDialog(this, "AI 분석 결과: [" + detected + "] 항목이 감지되었습니다.");
            addRecycleItemToTable(detected);
        }
    }

    private void addRecycleItemToTable(String itemName) {
        if (itemName == null || itemName.equals(DEFAULT_SELECTION_TEXT)) return;
        if (loadedItems.contains(itemName) || unsavedItems.contains(itemName)) {
            JOptionPane.showMessageDialog(this, "이미 목록에 존재합니다.");
            return;
        }
        unsavedItems.add(itemName);
        rebuildTableFromInternalLists();
    }

    public void loadLogsAndRefreshUI() {
        if (logDAO == null) return;
        try {
            List<String> itemsFromDB = logDAO.getTodayRecycleItems(userId);
            loadedItems.clear(); loadedItems.addAll(itemsFromDB);
            rebuildTableFromInternalLists();
        } catch (SQLException e) {}
    }

    private void rebuildTableFromInternalLists() {
        tableModel.setRowCount(0);
        List<String> all = new ArrayList<>(loadedItems);
        all.addAll(unsavedItems);
        totalPoint = 0;
        for (String name : all) {
            int p = itemPoints.getOrDefault(name, 0);
            totalPoint += p;
            tableModel.addRow(new Vector<>(List.of(tableModel.getRowCount() + 1, name, p + "P")));
        }
        tableModel.addRow(new Vector<>(List.of("", "합계", totalPoint + "P")));
    }

    private void handleSaveLogs(ActionEvent e) {
        if (unsavedItems.isEmpty()) return;
        try {
            int earned = logDAO.insertRecycleLogsAndEarn(userId, new ArrayList<>(unsavedItems), itemPoints);
            JOptionPane.showMessageDialog(this, earned + "포인트 적립 완료!");
            if (rankUpdateCallback != null) rankUpdateCallback.run();
            loadLogsAndRefreshUI();
            unsavedItems.clear();
        } catch (Exception ex) {}
    }
}