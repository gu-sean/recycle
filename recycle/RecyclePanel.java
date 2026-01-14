package recycle;

import db.DAO.RecycleLogDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class RecyclePanel extends JPanel {

    private final String userId;
    private final RecycleLogDAO logDAO;
    private final Map<String, Integer> itemPoints;
    private final Runnable rankUpdateCallback;

    private JComboBox<String> itemComboBox;
    private JLabel pointLabel, progressStatusLabel;
    private JButton addButton, saveButton, removeButton, uploadButton;
    private JTable currentTable;
    private DefaultTableModel tableModel;
    private JProgressBar goalProgressBar;

    private final List<String> loadedItems = new ArrayList<>();
    private final List<String> unsavedItems = new ArrayList<>();

    private boolean isAnalyzing = false;
    private boolean isLoading = false; 
    private float animAngle = 0;
    private Timer animTimer;

    private static final Color BG_DARK = new Color(10, 10, 20);
    private static final Color BG_CARD = new Color(25, 25, 45); 
    private static final Color POINT_PURPLE = new Color(140, 80, 255);
    private static final Color POINT_CYAN = new Color(0, 240, 255);
    private static final Color POINT_RED = new Color(255, 50, 100);
    private static final Color TEXT_WHITE = new Color(240, 240, 255);
    private static final Color BORDER_COLOR = new Color(50, 50, 80);

    private static final Font BOLD_FONT = new Font("맑은 고딕", Font.BOLD, 16);
    private static final Font TITLE_FONT = new Font("맑은 고딕", Font.BOLD, 22);

    public RecyclePanel(String userId, Runnable rankUpdateCallback) {
        this.userId = userId;
        this.rankUpdateCallback = rankUpdateCallback;
        this.itemPoints = initializeItemPoints();
        this.logDAO = new RecycleLogDAO();

        setBackground(BG_DARK);
        setOpaque(true);

        setupLayout();
        setupAnimation();
        
        loadLogsAndRefreshUI();
    }

    private Map<String, Integer> initializeItemPoints() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("종이", 15); map.put("비닐", 10); map.put("유리병", 25); map.put("종이팩", 20);
        map.put("캔/고철", 40); map.put("스티로폼", 10); map.put("플라스틱", 10); map.put("기타", 5);
        return map;
    }

    private void setupAnimation() {
        animTimer = new Timer(30, e -> {
            if (isAnalyzing) {
                animAngle += 0.15f;
                repaint();
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout(30, 0));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel leftPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(35, 30, 60), 0, getHeight(), BG_CARD));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                if (isAnalyzing) {
                    float x = (float) (Math.cos(animAngle) * getWidth() / 2 + getWidth() / 2);
                    float y = (float) (Math.sin(animAngle) * getHeight() / 2 + getHeight() / 2);
                    Point2D centerPoint = new Point2D.Float(x, y);
                    g2.setPaint(new RadialGradientPaint(centerPoint, 150f, new float[]{0f, 1f}, new Color[]{POINT_CYAN, new Color(0, 0, 0, 0)}));
                    g2.setStroke(new BasicStroke(5f));
                    g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 30, 30);
                }
                g2.dispose();
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(320, 0));
        leftPanel.setBorder(new EmptyBorder(40, 25, 40, 25));

        JLabel inputTitle = new JLabel("분리수거 등록");
        inputTitle.setFont(TITLE_FONT);
        inputTitle.setForeground(POINT_CYAN);
        inputTitle.setAlignmentX(CENTER_ALIGNMENT);

        itemComboBox = new JComboBox<>(itemPoints.keySet().toArray(new String[0]));
        styleComboBox(itemComboBox);
        itemComboBox.setAlignmentX(CENTER_ALIGNMENT);

        pointLabel = new JLabel("선택 포인트: 0 P");
        pointLabel.setFont(BOLD_FONT);
        pointLabel.setForeground(TEXT_WHITE);
        pointLabel.setAlignmentX(CENTER_ALIGNMENT);

        addButton = createStyledButton("목록에 추가 +", POINT_PURPLE, Color.WHITE);
        uploadButton = createStyledButton("사진 인식 (AI)", new Color(45, 140, 120), Color.WHITE);
        
        progressStatusLabel = new JLabel("목표 달성도를 확인하세요");
        progressStatusLabel.setForeground(TEXT_WHITE);
        progressStatusLabel.setAlignmentX(CENTER_ALIGNMENT);

        goalProgressBar = new JProgressBar(0, 100);
        goalProgressBar.setMaximumSize(new Dimension(250, 25));
        goalProgressBar.setBackground(new Color(40, 40, 60));
        goalProgressBar.setForeground(POINT_CYAN);
        goalProgressBar.setStringPainted(true);
        goalProgressBar.setAlignmentX(CENTER_ALIGNMENT);

        leftPanel.add(inputTitle); leftPanel.add(Box.createVerticalStrut(40));
        leftPanel.add(itemComboBox); leftPanel.add(Box.createVerticalStrut(30));
        leftPanel.add(pointLabel); leftPanel.add(Box.createVerticalStrut(50));
        leftPanel.add(addButton); leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(uploadButton); leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(progressStatusLabel); leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(goalProgressBar);

        JPanel rightPanel = new JPanel(new BorderLayout(0, 20));
        rightPanel.setOpaque(false);

        tableModel = new DefaultTableModel(new String[]{"순번", "분리수거 품목", "포인트", "상태"}, 0);
        currentTable = new JTable(tableModel);
        styleTable(currentTable);

        JScrollPane scrollPane = new JScrollPane(currentTable);
        scrollPane.getViewport().setBackground(BG_CARD);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnRow.setOpaque(false);
        removeButton = createStyledButton("선택 삭제", POINT_RED, Color.WHITE);
        saveButton = createStyledButton("적립 완료", POINT_CYAN, Color.BLACK);
        removeButton.setPreferredSize(new Dimension(140, 48));
        saveButton.setPreferredSize(new Dimension(160, 48));
        btnRow.add(removeButton); btnRow.add(saveButton);

        rightPanel.add(new JLabel("오늘의 분리수거 로그", SwingConstants.CENTER) {{
            setFont(TITLE_FONT); setForeground(Color.WHITE);
        }}, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(btnRow, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        itemComboBox.addActionListener(e -> updatePointLabel());
        addButton.addActionListener(e -> handleAddItem());
        removeButton.addActionListener(e -> handleRemove());
        saveButton.addActionListener(this::handleSaveLogs);
        uploadButton.addActionListener(e -> handleImageUpload());
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setMaximumSize(new Dimension(250, 45));
        cb.setBackground(BG_CARD); 
        cb.setForeground(Color.WHITE);
        cb.setSelectedIndex(-1);

        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setHorizontalAlignment(CENTER);
                l.setOpaque(true);
                l.setBackground(isSelected ? new Color(60, 60, 100) : BG_CARD);
                l.setForeground(Color.WHITE);
                l.setBorder(new EmptyBorder(5, 5, 5, 5));
                if (index == -1 && value == null) l.setText("품목을 선택하세요");
                return l;
            }
        });

        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = super.createArrowButton();
                btn.setBackground(BG_CARD);
                btn.setBorder(new EmptyBorder(0, 5, 0, 5));
                return btn;
            }
            @Override protected ComboPopup createPopup() {
                ComboPopup popup = super.createPopup();
                ((JComponent)popup).setBorder(new LineBorder(POINT_PURPLE, 1));
                return popup;
            }
            @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(BG_CARD);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        cb.setBorder(new LineBorder(POINT_PURPLE, 1, true));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                l.setBackground(new Color(30, 30, 50));
                l.setForeground(POINT_CYAN);
                l.setHorizontalAlignment(CENTER);
                l.setBorder(new LineBorder(BORDER_COLOR));
                return l;
            }
        });

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                l.setHorizontalAlignment(CENTER);
                l.setOpaque(true);
                
                l.setBackground(isS ? new Color(50, 50, 80) : BG_CARD);

  
                if (isLoading && r == 0) {
                    l.setForeground(POINT_CYAN);
                    return l;
                }

                String status = (String) t.getValueAt(r, 3);
                String itemName = (String) t.getValueAt(r, 1);

                if ("오늘 총 합계".equals(itemName)) {
                    l.setBackground(new Color(20, 20, 35));
                    l.setForeground(POINT_CYAN);
                    l.setFont(BOLD_FONT);
                } else if ("적립완료".equals(status)) {
                    l.setForeground(new Color(130, 130, 150));
                    l.setFont(l.getFont().deriveFont(Font.PLAIN));
                } else if ("대기중".equals(status)) {
                    l.setForeground(POINT_PURPLE);
                    l.setFont(BOLD_FONT);
                } else {
                    l.setForeground(TEXT_WHITE);
                }
                return l;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
    }

    private void setInputsEnabled(boolean enabled) {
        addButton.setEnabled(enabled);
        uploadButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        itemComboBox.setEnabled(enabled);
    }

    private void handleAddItem() {
        String selected = (String) itemComboBox.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "품목을 먼저 선택해주세요!", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (unsavedItems.contains(selected) || loadedItems.contains(selected)) {
            JOptionPane.showMessageDialog(this, "이미 목록에 등록된 품목입니다.", "중복 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        unsavedItems.add(selected);
        rebuildTable();
        itemComboBox.setSelectedIndex(-1);
    }

    private void handleRemove() {
        int viewRow = currentTable.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 항목을 리스트에서 선택해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int modelRow = currentTable.convertRowIndexToModel(viewRow);
        String status = (String) tableModel.getValueAt(modelRow, 3);
        if ("적립완료".equals(status)) {
            JOptionPane.showMessageDialog(this, "이미 적립 완료된 항목은 삭제할 수 없습니다.", "삭제 불가", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String itemName = (String) tableModel.getValueAt(modelRow, 1);
        unsavedItems.remove(itemName);
        rebuildTable();
    }

    private void handleSaveLogs(ActionEvent e) {
        if (unsavedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "새롭게 적립할 항목이 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        setInputsEnabled(false);
        new Thread(() -> {
            try {
                int earned = logDAO.insertRecycleLogsAndEarn(userId, new ArrayList<>(unsavedItems), itemPoints);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, earned + " 포인트가 성공적으로 적립되었습니다!", "적립 성공", JOptionPane.INFORMATION_MESSAGE);
                    unsavedItems.clear();
                    loadLogsAndRefreshUI(); 
                    if (rankUpdateCallback != null) rankUpdateCallback.run();
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "DB 저장 중 오류가 발생했습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                    setInputsEnabled(true);
                });
            }
        }).start();
    }

    private void handleImageUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("이미지 파일", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            isAnalyzing = true;
            setInputsEnabled(false); 
            animTimer.start();
            simulateAIAnalysis();
        }
    }

    private void simulateAIAnalysis() {
        Timer simTimer = new Timer(2000, e -> {
            isAnalyzing = false;
            animTimer.stop();
            setInputsEnabled(true);
            
            String result = "플라스틱";
            if (!unsavedItems.contains(result) && !loadedItems.contains(result)) {
                unsavedItems.add(result);
                rebuildTable();
                JOptionPane.showMessageDialog(this, "AI 분석 완료: [" + result + "] 항목이 감지되었습니다.", "인식 성공", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "인식된 [" + result + "]은 이미 목록에 있습니다.", "중복 알림", JOptionPane.WARNING_MESSAGE);
            }
            repaint();
        });
        simTimer.setRepeats(false);
        simTimer.start();
    }

    private synchronized void rebuildTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            
            if (isLoading) {
                tableModel.addRow(new Object[]{"-", "데이터 로딩 중...", "-", "-"});
                return;
            }

            int total = 0, num = 1;
            for (String s : loadedItems) {
                int p = itemPoints.getOrDefault(s, 0); total += p;
                tableModel.addRow(new Object[]{num++, s, p + " P", "적립완료"});
            }
            for (String s : unsavedItems) {
                int p = itemPoints.getOrDefault(s, 0); total += p;
                tableModel.addRow(new Object[]{num++, s, p + " P", "대기중"});
            }
            if (tableModel.getRowCount() > 0) {
                tableModel.addRow(new Object[]{"", "오늘 총 합계", total + " P", ""});
            }
            updateProgressBar(total);
        });
    }

    private void updateProgressBar(int total) {
        goalProgressBar.setValue(Math.min(total, 100));
        goalProgressBar.setString(total + " / 100 P");
        if (total >= 100) {
            progressStatusLabel.setText("🎉 오늘의 목표 달성 완료!");
            progressStatusLabel.setForeground(POINT_CYAN);
        } else {
            progressStatusLabel.setText("목표까지 " + (100 - total) + "P 남았습니다");
            progressStatusLabel.setForeground(TEXT_WHITE);
        }
    }

    private void updatePointLabel() {
        String item = (String) itemComboBox.getSelectedItem();
        pointLabel.setText("선택 포인트: " + (item == null ? 0 : itemPoints.getOrDefault(item, 0)) + " P");
    }

    public void loadLogsAndRefreshUI() {
        isLoading = true;
        setInputsEnabled(false);
        rebuildTable(); 

        new Thread(() -> {
            try {
                Thread.sleep(500); 
                List<String> items = logDAO.getTodayRecycleItems(userId);
                SwingUtilities.invokeLater(() -> {
                    loadedItems.clear();
                    if (items != null) loadedItems.addAll(items);
                    isLoading = false;
                    setInputsEnabled(true);
                    rebuildTable();
                });
            } catch (Exception e) { 
                e.printStackTrace(); 
                SwingUtilities.invokeLater(() -> {
                    isLoading = false;
                    setInputsEnabled(true);
                });
            }
        }).start();
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setFont(BOLD_FONT); btn.setMaximumSize(new Dimension(250, 50));
        btn.setFocusPainted(false); btn.setBorder(new LineBorder(new Color(255,255,255,30), 1, true));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}