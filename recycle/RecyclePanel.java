package recycle;

import db.DAO.RecycleLogDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    
    private final List<String> loadedItems = new ArrayList<>();  
    private final List<String> unsavedItems = new ArrayList<>(); 

    private static final Color BG_DARK = new Color(15, 12, 30);   
    private static final Color BG_LIGHT = new Color(40, 45, 90);     
    private static final Color POINT_PURPLE = new Color(130, 90, 255); 
    private static final Color POINT_CYAN = new Color(0, 255, 240);     
    private static final Color COMP_BG = new Color(25, 25, 50); 

    private static final String DEFAULT_SELECTION_TEXT = "--- 품목 선택 ---";
    private static final String[] COLUMN_NAMES = {"순번", "분리수거 품목", "포인트", "상태"};

    public RecyclePanel(String userId, Runnable rankUpdateCallback) {
        this.userId = userId;
        this.rankUpdateCallback = rankUpdateCallback; 
        this.itemPoints = initializeItemPoints(); 
        this.logDAO = new RecycleLogDAO();

        setupLayout();
        loadLogsAndRefreshUI(); 
    }

    private Map<String, Integer> initializeItemPoints() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("종이", 15); map.put("비닐", 10); map.put("유리병", 25); map.put("종이팩", 20);
        map.put("캔ㆍ고철", 40); map.put("스티로폼", 10); map.put("플라스틱", 10); map.put("기타", 5); 
        return map;
    }

    public void loadLogsAndRefreshUI() {
        try {
            List<String> itemsFromDB = logDAO.getTodayRecycleItems(userId);
            SwingUtilities.invokeLater(() -> {
                loadedItems.clear(); 
                if (itemsFromDB != null) {
                    for (String s : itemsFromDB) loadedItems.add(s.trim());
                }
                rebuildTable();
            });
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    private synchronized void rebuildTable() {
        tableModel.setRowCount(0); 
        int currentTotal = 0;
        int rowNum = 1;

        for (String name : loadedItems) {
            int p = itemPoints.getOrDefault(name, 0);
            currentTotal += p;
            tableModel.addRow(new Object[]{rowNum++, name, p + " P", "적립완료"});
        }

        for (String name : unsavedItems) {
            int p = itemPoints.getOrDefault(name, 0);
            currentTotal += p;
            tableModel.addRow(new Object[]{rowNum++, name, p + " P", "대기중"});
        }

        if (tableModel.getRowCount() > 0) {
            tableModel.addRow(new Object[]{"", "오늘 총 합계", currentTotal + " P", ""});
        }
    }

    private void handleSaveLogs(ActionEvent e) {
        if (unsavedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "적립할 새로운 항목이 없습니다.");
            return;
        }
        
        try {
            int earned = logDAO.insertRecycleLogsAndEarn(userId, new ArrayList<>(unsavedItems), itemPoints);
            if (earned >= 0) {
                JOptionPane.showMessageDialog(this, earned + " 포인트 적립 완료!");
                loadedItems.addAll(new ArrayList<>(unsavedItems)); 
                unsavedItems.clear();       
                rebuildTable(); 
                if (rankUpdateCallback != null) rankUpdateCallback.run(); 
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 중 오류: " + ex.getMessage());
        }
    }

    private void addRecycleItemToTable(String itemName) {
        if (itemName == null || itemName.equals(DEFAULT_SELECTION_TEXT)) return;
        if (loadedItems.contains(itemName) || unsavedItems.contains(itemName)) {
            JOptionPane.showMessageDialog(this, "[" + itemName + "] 항목은 이미 목록에 있습니다.");
            return;
        }
        unsavedItems.add(itemName);
        rebuildTable();
    }

    private void handleRemove() {
        int row = currentTable.getSelectedRow();
        if (row == -1) return;
        String status = (String) tableModel.getValueAt(row, 3);
        if ("적립완료".equals(status)) {
            JOptionPane.showMessageDialog(this, "이미 적립 완료된 내역은 삭제할 수 없습니다.");
            return;
        }
        String name = (String) tableModel.getValueAt(row, 1);
        unsavedItems.remove(name);
        rebuildTable();
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setBackground(COMP_BG);
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 110));
        
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                label.setHorizontalAlignment(CENTER);
                if (r < t.getRowCount() && "오늘 총 합계".equals(t.getValueAt(r, 1))) {
                    label.setForeground(POINT_CYAN);
                } else {
                    Object status = t.getValueAt(r, 3);
                    if ("적립완료".equals(status)) {
                        label.setForeground(new Color(130, 130, 130)); 
                    } else {
                        label.setForeground(POINT_CYAN); 
                    }
                }
                return label;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(renderer);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(15, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel leftPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
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

        itemComboBox = new JComboBox<>(itemPoints.keySet().toArray(new String[0]));
        itemComboBox.insertItemAt(DEFAULT_SELECTION_TEXT, 0);
        itemComboBox.setSelectedIndex(0);
        styleComboBox(itemComboBox); // 콤보박스 스타일 및 가운데 정렬 적용

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
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());

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

  
    private void styleComboBox(JComboBox<String> cb) {
        cb.setMaximumSize(new Dimension(210, 45));
        cb.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        cb.setBackground(Color.WHITE);
        
        DefaultListCellRenderer centerRenderer = new DefaultListCellRenderer();
        centerRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
        cb.setRenderer(centerRenderer);

        cb.setUI(new BasicComboBoxUI() {
            @Override protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox) {
                    @Override protected JScrollPane createScroller() {
                        JScrollPane scroller = super.createScroller();
                        scroller.getVerticalScrollBar().setUI(new CustomScrollBarUI());
                        return scroller;
                    }
                };
                popup.setBorder(new LineBorder(POINT_PURPLE, 1));
                return popup;
            }
        });
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

    private void openImageUploadDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("이미지 파일", "jpg", "png", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String[] keys = itemPoints.keySet().toArray(new String[0]);
            String detected = keys[(int)(Math.random() * keys.length)];
            JOptionPane.showMessageDialog(this, "AI 분석 결과: [" + detected + "] 감지!");
            addRecycleItemToTable(detected);
        }
    }

    private static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { this.thumbColor = POINT_PURPLE; this.trackColor = BG_DARK; }
        @Override protected JButton createDecreaseButton(int orientation) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
        @Override protected JButton createIncreaseButton(int orientation) { return new JButton() {{ setPreferredSize(new Dimension(0,0)); }}; }
    }
}