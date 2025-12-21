package recycle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Vector;

import db.DAO.GuideDAO;
import db.DAO.GuideDAO.ItemDetail;

public class Guide extends JPanel {

    private static final Map<String, Map<String, String>> INITIAL_GUIDE_DATA = new LinkedHashMap<>();
    static {
        Map<String, String> paperItems = new LinkedHashMap<>();
        paperItems.put("기본 배출 원칙", "<h3>종이류 배출 방법</h3><ul><li>물기에 젖지 않도록 보관하여 배출합니다.</li><li>비닐 코팅, 스프링, 테이프, 철핀 등 다른 재질은 모두 제거해야 합니다.</li><li>반듯하게 펴서 종이류끼리 묶어 배출합니다.</li></ul>");
        paperItems.put("예외 (오염/비재활용)", "<h3>주의 사항</h3><ul><li class=\"note\">* 기름 등 이물질에 심하게 오염된 종이(박스)는 종량제 봉투로 배출합니다.</li></ul>"); 
        INITIAL_GUIDE_DATA.put("종이", paperItems);
    }

    // UI 컴포넌트
    private JList<String> categoryList;
    private JList<String> itemList;
    private DefaultListModel<String> itemListModel;
    private JEditorPane editorPane;
    private JTextField searchField;
    private JButton searchButton;
    private Map<String, String> categoryMap;

    // 테마 색상 (RecyclePanel과 통일)
    private static final Color BG_DARK = new Color(20, 15, 40);
    private static final Color BG_LIGHT = new Color(40, 45, 90);
    private static final Color POINT_PURPLE = new Color(150, 100, 255);
    private static final Color POINT_CYAN = new Color(0, 255, 240);
    private static final Color LIST_BG = new Color(30, 30, 60);

    public Guide() {
        try {
            GuideDAO.initializeDatabase();
            this.categoryMap = GuideDAO.getAllCategoryNamesAndIds(); 
        } catch (Exception e) {
            displayErrorUI("가이드 정보를 불러올 수 없습니다.");
            return;
        }

        setupLayout();
        setupEvents();

        // 초기 카테고리 선택
        if (!categoryMap.isEmpty()) {
            categoryList.setSelectedIndex(0);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. 상단 패널 (제목 및 검색) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("분리수거 가이드");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titleLabel.setForeground(POINT_CYAN);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);

        searchField = new JTextField(15);
        searchField.setBackground(new Color(255, 255, 255, 20));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        searchField.setBorder(new LineBorder(POINT_PURPLE, 1));

        searchButton = createStyledButton("검색", POINT_PURPLE, Color.WHITE);
        searchButton.setPreferredSize(new Dimension(80, 30));

        searchPanel.add(new JLabel("<html><font color='white'>품목 검색: </font></html>"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- 2. 왼쪽 영역 (카테고리 & 품목 리스트) ---
        categoryList = createStyledList(new Vector<>(categoryMap.keySet()));
        JScrollPane categoryScroll = createStyledScrollPane(categoryList, "카테고리");

        itemListModel = new DefaultListModel<>();
        itemList = createStyledList(itemListModel);
        JScrollPane itemScroll = createStyledScrollPane(itemList, "품목 리스트");

        JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, categoryScroll, itemScroll);
        leftSplit.setDividerLocation(160);
        leftSplit.setOpaque(false);
        leftSplit.setBorder(null);
        leftSplit.setDividerSize(5);

        // --- 3. 오른쪽 영역 (상세 설명) ---
        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setBackground(LIST_BG);
        JScrollPane detailScroll = createStyledScrollPane(editorPane, "배출 방법 및 주의사항");

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, detailScroll);
        mainSplit.setDividerLocation(360);
        mainSplit.setOpaque(false);
        mainSplit.setBorder(null);
        mainSplit.setDividerSize(5);

        add(mainSplit, BorderLayout.CENTER);
    }

    private void setupEvents() {
        // 카테고리 선택 시 품목 로드
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = categoryList.getSelectedValue();
                if (selected != null) loadItems(categoryMap.get(selected), selected);
            }
        });

        // 품목 선택 시 상세 내용 표시
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = itemList.getSelectedValue();
                if (selected != null) {
                    try {
                        ItemDetail detail = GuideDAO.getItemDetail(selected, categoryList.getSelectedValue());
                        if (detail != null) updateDetailContent(detail);
                    } catch (SQLException ex) { ex.printStackTrace(); }
                }
            }
        });

        searchButton.addActionListener(e -> performSearch());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performSearch();
            }
        });
    }

    // --- UI 스타일 헬퍼 메서드 ---
    private <T> JList<T> createStyledList(Object data) {
        JList<T> list;
        if (data instanceof Vector) list = new JList<>((Vector<T>) data);
        else list = new JList<>((ListModel<T>) data);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        list.setBackground(LIST_BG);
        list.setForeground(Color.WHITE);
        list.setSelectionBackground(POINT_PURPLE);
        list.setSelectionForeground(Color.WHITE);
        list.setFixedCellHeight(35);
        
        // 리스트 가운데 정렬 렌더러
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(0, 10, 0, 10));
                return label;
            }
        });
        return list;
    }

    private JScrollPane createStyledScrollPane(Component view, String title) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(LIST_BG);
        
        TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(POINT_PURPLE, 1), title);
        border.setTitleColor(POINT_CYAN);
        border.setTitleFont(new Font("맑은 고딕", Font.BOLD, 13));
        scroll.setBorder(border);
        
        return scroll;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    // --- 비즈니스 로직 (원본 유지 및 일부 보완) ---
    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;

        try {
            List<ItemDetail> allItems = GuideDAO.getAllItems();
            ItemDetail foundItem = allItems.stream()
                .filter(item -> item.itemName.contains(keyword))
                .findFirst().orElse(null);

            if (foundItem != null) {
                categoryList.setSelectedValue(foundItem.categoryName, true);
                loadItems(categoryMap.get(foundItem.categoryName), foundItem.categoryName);
                itemList.setSelectedValue(foundItem.itemName, true);
                updateDetailContent(foundItem);
            } else {
                JOptionPane.showMessageDialog(this, "'" + keyword + "'에 대한 검색 결과가 없습니다.");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadItems(String categoryId, String categoryName) {
        try {
            List<String> items = GuideDAO.getItemNamesByCategory(categoryId);
            itemListModel.clear();
            for (String item : items) itemListModel.addElement(item);
            if (!items.isEmpty()) itemList.setSelectedIndex(0);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateDetailContent(ItemDetail item) {
        String css = "body { background-color: #1e1e3c; color: white; font-family: '맑은 고딕'; padding: 15px; }" +
                     "h1 { color: #00fff0; border-bottom: 2px solid #9664ff; padding-bottom: 10px; }" +
                     "h3 { color: #9664ff; }" +
                     "ul { line-height: 1.6; }" +
                     "li { margin-bottom: 8px; }";

        StringBuilder sb = new StringBuilder();
        sb.append("<h1>").append(item.itemName).append("</h1>");
        sb.append("<p style='color:#aaaaaa;'>카테고리: ").append(item.categoryName).append("</p>");

        String guide = item.disposalGuide;
        if (guide == null || guide.isEmpty() || guide.equals("공통 가이드 참조")) {
            Map<String, String> common = INITIAL_GUIDE_DATA.get(item.categoryName);
            if (common != null) common.values().forEach(sb::append);
            else sb.append("<p>상세 가이드를 준비 중입니다.</p>");
        } else {
            sb.append("<div style='font-size:14px;'>").append(guide.replace("\n", "<br>")).append("</div>");
        }

        editorPane.setText("<html><head><style>" + css + "</style></head><body>" + sb.toString() + "</body></html>");
        editorPane.setCaretPosition(0);
    }

    private void displayErrorUI(String message) {
        removeAll();
        setLayout(new GridBagLayout());
        JLabel error = new JLabel(message);
        error.setForeground(Color.RED);
        add(error);
        revalidate(); repaint();
    }
}