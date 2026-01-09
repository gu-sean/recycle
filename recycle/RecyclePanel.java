package recycle;

import db.DAO.RecycleLogDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.filechooser.FileNameExtensionFilter;
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
    private JButton addButton;
 
    private JTable currentTable;
    private DefaultTableModel tableModel;
    private JButton saveButton;
    private JButton removeButton;
    private JButton uploadButton;
    
    private int totalPoint = 0; 

    private final List<String> loadedItems = new ArrayList<>(); 

    private final List<String> unsavedItems = new ArrayList<>(); 
    
    private static final Color BUTTON_BACKGROUND = new Color(220, 240, 255); 
    private static final Color TOTAL_ROW_BACKGROUND = BUTTON_BACKGROUND; 
    private static final Color UPLOAD_BUTTON_COLOR = new Color(100, 180, 100); 
    
    private static final String DEFAULT_SELECTION_TEXT = "--- 품목 선택 ---";
    private static final String[] CURRENT_LOG_COLUMN_NAMES = {"순번", "품목", "포인트"};
    
    private static final Font KOREAN_FONT = new Font("맑은 고딕", Font.PLAIN, 15);
    private static final Font KOREAN_BOLD_FONT = new Font("맑은 고딕", Font.BOLD, 15);

    public RecyclePanel(String userId, Runnable rankUpdateCallback) {
        this.userId = userId;
        this.rankUpdateCallback = rankUpdateCallback; 
        this.itemPoints = initializeItemPoints(); 

        RecycleLogDAO dao = null;
        try {
            dao = new RecycleLogDAO();
        } catch (Exception e) {
            System.err.println("RecycleLogDAO 초기화 오류: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "DB 초기화 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
        this.logDAO = dao;

        if (this.logDAO != null) {
            createUILayout(); 
            loadLogsAndRefreshUI(); 
        } else {
            removeAll();
            setLayout(new GridBagLayout());
            add(new JLabel("DB 연결 오류로 패널을 사용할 수 없습니다.", SwingConstants.CENTER));
        }
    }

    public RecyclePanel(String userId) {
        this(userId, null);
    }

    private Map<String, Integer> initializeItemPoints() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("종이", 15);
        map.put("비닐", 10);
        map.put("유리병", 25);
        map.put("종이팩", 20);
        map.put("캔ㆍ고철", 40);
        map.put("스티로폼", 10);
        map.put("플라스틱", 10);
        map.put("기타", 5); 
        return map;
    }

    private void createUILayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel leftPanel = createInputPanel();
        leftPanel.setPreferredSize(new Dimension(200, 400)); 

        JPanel rightPanelContainer = new JPanel(new BorderLayout());
        rightPanelContainer.setPreferredSize(new Dimension(350, 400));
        
        JLabel titleLabel = new JLabel("분리수거 목록", SwingConstants.CENTER);
        titleLabel.setFont(KOREAN_BOLD_FONT.deriveFont(Font.BOLD, 20)); 
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel currentLogPanel = createCurrentLogPanel(); 
        currentLogPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        rightPanelContainer.add(titleLabel, BorderLayout.NORTH);
        rightPanelContainer.add(currentLogPanel, BorderLayout.CENTER);
        
        add(leftPanel, BorderLayout.WEST);
        add(rightPanelContainer, BorderLayout.CENTER);
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10)); 
        
        itemComboBox = new JComboBox<>(itemPoints.keySet().toArray(new String[0]));
        itemComboBox.insertItemAt(DEFAULT_SELECTION_TEXT, 0);
        itemComboBox.setSelectedIndex(0);
        itemComboBox.setFont(KOREAN_FONT);
        itemComboBox.setMaximumSize(new Dimension(200, 30)); 
        itemComboBox.setRenderer(new CenterAlignedRenderer());

        pointLabel = new JLabel("선택 포인트: 0 P", SwingConstants.CENTER);
        pointLabel.setFont(KOREAN_FONT);
        pointLabel.setAlignmentX(Component.CENTER_ALIGNMENT); 

        addButton = new JButton("목록에 추가");
        addButton.setFont(KOREAN_BOLD_FONT);
        addButton.addActionListener(e -> addRecycleItemToTable((String)itemComboBox.getSelectedItem()));
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setMaximumSize(new Dimension(200, 40));
        addButton.setBackground(BUTTON_BACKGROUND);

        panel.add(Box.createVerticalGlue()); 
        panel.add(itemComboBox);
        panel.add(Box.createVerticalStrut(25)); 
        panel.add(pointLabel);
        panel.add(Box.createVerticalStrut(25)); 
        panel.add(addButton);
        panel.add(Box.createVerticalGlue()); 
        
        itemComboBox.addActionListener(e -> {
            String selectedItem = (String) itemComboBox.getSelectedItem();
            int point = (selectedItem == null || selectedItem.equals(DEFAULT_SELECTION_TEXT)) 
                        ? 0 
                        : itemPoints.getOrDefault(selectedItem, 0);
            pointLabel.setText("선택 포인트: " + point + " P");
        });
        
        return panel;
    }
    
    private JPanel createCurrentLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        tableModel = new DefaultTableModel(CURRENT_LOG_COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        currentTable = new JTable(tableModel);
        currentTable.setFont(KOREAN_FONT);
        currentTable.setRowHeight(25);
        currentTable.getTableHeader().setFont(KOREAN_BOLD_FONT);
        currentTable.setDefaultRenderer(Object.class, new TotalRowRenderer());
        
        JScrollPane scrollPane = new JScrollPane(currentTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        uploadButton = new JButton("사진 업로드");
        uploadButton.setBackground(UPLOAD_BUTTON_COLOR);
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFont(KOREAN_BOLD_FONT);
        uploadButton.addActionListener(e -> openImageUploadDialog());

        removeButton = new JButton("선택 항목 제거");
        saveButton = new JButton("포인트 얻기 (저장)");
        
        removeButton.setBackground(BUTTON_BACKGROUND);
        saveButton.setBackground(BUTTON_BACKGROUND);
        
        saveButton.addActionListener(this::handleSaveLogs); 

        removeButton.addActionListener(e -> {
            int selectedRow = currentTable.getSelectedRow();
            if (selectedRow == -1) {
                 JOptionPane.showMessageDialog(this, "제거할 항목을 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                 return;
            } 
            String itemName = (String) tableModel.getValueAt(selectedRow, 1);
            if (itemName.equals("합계")) return;
            if (loadedItems.contains(itemName)) {
                 JOptionPane.showMessageDialog(this, "이미 저장된 항목은 제거할 수 없습니다.", "경고", JOptionPane.WARNING_MESSAGE);
                 return;
            }
            unsavedItems.remove(itemName);
            rebuildTableFromInternalLists();
        });

        buttonPanel.add(uploadButton); 
        buttonPanel.add(removeButton); 
        buttonPanel.add(saveButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void openImageUploadDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("분리수거 물품 사진 선택");
        fileChooser.setFileFilter(new FileNameExtensionFilter("이미지 파일", "jpg", "png", "jpeg", "gif"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            showImageAnalysisDialog(selectedFile);
        }
    }

    private void showImageAnalysisDialog(File file) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "사진 분석 결과", true);
        dialog.setLayout(new BorderLayout(15, 15));
        
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        Image scaledImg = icon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        String suggested = (String) itemComboBox.getSelectedItem();
        if (suggested.equals(DEFAULT_SELECTION_TEXT)) suggested = "플라스틱";

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel msgLabel = new JLabel("<html><center>사진 분석 완료!<br>분석된 카테고리: <b>[" + suggested + "]</b></center></html>", SwingConstants.CENTER);
        msgLabel.setFont(KOREAN_FONT);
        infoPanel.add(msgLabel);
        
        JButton confirmBtn = new JButton(suggested + " 항목으로 리스트에 추가");
        confirmBtn.setFont(KOREAN_BOLD_FONT);
        confirmBtn.setBackground(UPLOAD_BUTTON_COLOR);
        confirmBtn.setForeground(Color.WHITE);
        
        final String finalSuggested = suggested;
        confirmBtn.addActionListener(e -> {
            addRecycleItemToTable(finalSuggested);
            dialog.dispose();
        });

        dialog.add(imageLabel, BorderLayout.CENTER);
        dialog.add(infoPanel, BorderLayout.NORTH);
        dialog.add(confirmBtn, BorderLayout.SOUTH);
        
        ((JPanel)dialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addRecycleItemToTable(String itemName) {
        if (itemName == null || itemName.equals(DEFAULT_SELECTION_TEXT)) {
            JOptionPane.showMessageDialog(this, "품목을 선택해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (loadedItems.contains(itemName) || unsavedItems.contains(itemName)) {
            JOptionPane.showMessageDialog(this, "이미 오늘 목록에 추가된 품목입니다: " + itemName, "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
        unsavedItems.add(itemName);
        rebuildTableFromInternalLists();
        itemComboBox.setSelectedIndex(0); 
    }

    private void calculateTotalPoints() {
        if (tableModel.getRowCount() > 0 && tableModel.getValueAt(tableModel.getRowCount() - 1, 1).equals("합계")) {
            tableModel.removeRow(tableModel.getRowCount() - 1);
        }
        totalPoint = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String pointStr = (String) tableModel.getValueAt(i, 2);
            totalPoint += Integer.parseInt(pointStr.replace("P", "").trim());
        }
        tableModel.addRow(new Vector<>(List.of("", "합계", totalPoint + "P")));
    }

    public void loadLogsAndRefreshUI() {
        tableModel.setRowCount(0);
        loadedItems.clear(); 
        unsavedItems.clear(); 
        if (logDAO == null) { calculateTotalPoints(); return; }
        try {
            List<String> itemsFromDB = logDAO.getTodayRecycleItems(userId); 
            loadedItems.addAll(itemsFromDB);
            rebuildTableFromInternalLists();
        } catch (SQLException e) {
            System.err.println("로그 로드 중 오류: " + e.getMessage());
            rebuildTableFromInternalLists(); 
        }
    }
    
    private void rebuildTableFromInternalLists() {
        tableModel.setRowCount(0);
        List<String> combinedItems = new ArrayList<>(loadedItems);
        combinedItems.addAll(unsavedItems);
        for (String itemName : combinedItems) {
            int point = itemPoints.getOrDefault(itemName, 0);
            tableModel.addRow(new Vector<>(List.of(tableModel.getRowCount() + 1, itemName, point + "P")));
        }
        calculateTotalPoints();
    }

    private void handleSaveLogs(ActionEvent e) {
        if (logDAO == null) return;
        if (unsavedItems.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "새로 저장할 품목이 없습니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int earnedPoints = logDAO.insertRecycleLogsAndEarn(userId, new ArrayList<>(unsavedItems), itemPoints);
            JOptionPane.showMessageDialog(this, earnedPoints + "포인트가 적립되었습니다.");
            if (rankUpdateCallback != null) rankUpdateCallback.run(); 
            loadLogsAndRefreshUI();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 실패: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private class TotalRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment(SwingConstants.CENTER);
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (row == table.getRowCount() - 1) {
                c.setBackground(TOTAL_ROW_BACKGROUND);
                c.setFont(KOREAN_BOLD_FONT);
            } else {
                c.setBackground(table.getBackground());
                c.setFont(KOREAN_FONT);
            }
            return c;
        }
    }

    private class CenterAlignedRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            renderer.setHorizontalAlignment(CENTER);
            return renderer;
        }
    }
}