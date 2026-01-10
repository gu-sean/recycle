package recycle;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import db.DAO.GuideDAO;
import db.DAO.GuideDAO.ItemDetail;

public class Guide extends JPanel {

    private static final Color BG_DARK = new Color(15, 12, 30);      
    private static final Color BG_PANEL = new Color(25, 25, 50);     
    private static final Color POINT_PURPLE = new Color(130, 90, 255); 
    private static final Color POINT_CYAN = new Color(0, 255, 240);   
    private static final Color INPUT_BG = new Color(40, 40, 75);     

    private JTabbedPane mainTabbedPane;
    private JList<String> categoryList, itemList;
    private DefaultListModel<String> categoryListModel, itemListModel;
    private JEditorPane editorPane;
    private JScrollPane detailScrollPane; 
    private JPanel centerCardPanel;       
    private CardLayout cardLayout;
    private JSplitPane leftSplit, mainSplit; 
    
    private JTextField searchField;
    private JButton searchButton;
    private Map<String, String> allCategoryMap; 

    public Guide() {
        try {
            GuideDAO.initializeDatabase();
            this.allCategoryMap = GuideDAO.getAllCategoryNamesAndIds(); 
        } catch (Exception e) {
            displayErrorUI("데이터베이스 연결 실패: " + e.getMessage());
            return;
        }
        setupLayout();
        setupEvents();
        loadInitialData();
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 0)); 
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(10, 20, 10, 20)); 

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 5, 2, 5)); 

        JLabel titleLabel = new JLabel("♻️ 분리수거 백과사전");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(POINT_CYAN);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField(15);
        searchField.setBackground(INPUT_BG);
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(new CompoundBorder(new LineBorder(POINT_PURPLE, 1), new EmptyBorder(5, 10, 5, 10)));
        searchButton = createStyledButton("검색", POINT_PURPLE, Color.WHITE);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setUI(new CustomTabbedPaneUI());
        mainTabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        mainTabbedPane.setPreferredSize(new Dimension(Short.MAX_VALUE, 32));

        String[] tabs = {"재활용폐기물", "음식물류폐기물", "일반종량제폐기물", "불연성종량제폐기물", "대형폐기물", "공사장 생활폐기물", "생활계 유해폐기물", "기타"};
        for (String tab : tabs) mainTabbedPane.addTab(tab, null);

        topWrapper.add(headerPanel, BorderLayout.NORTH);
        topWrapper.add(mainTabbedPane, BorderLayout.SOUTH);
        add(topWrapper, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        centerCardPanel = new JPanel(cardLayout);
        centerCardPanel.setOpaque(false);

        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setBackground(BG_PANEL);
        detailScrollPane = createStyledScrollPane(editorPane, "분리배출 상세 가이드");

        JPanel recycleView = new JPanel(new BorderLayout());
        recycleView.setOpaque(false);
        categoryListModel = new DefaultListModel<>();
        categoryList = createStyledList(categoryListModel);
        itemListModel = new DefaultListModel<>();
        itemList = createStyledList(itemListModel);
        
        leftSplit = createCleanSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                               createStyledScrollPane(categoryList, "분류"), 
                               createStyledScrollPane(itemList, "품목 목록"));
        leftSplit.setDividerLocation(120); 
        leftSplit.setResizeWeight(0.2); 

        mainSplit = createCleanSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, detailScrollPane);
        mainSplit.setDividerLocation(300);
        mainSplit.setResizeWeight(0.1); 
        
        recycleView.add(mainSplit, BorderLayout.CENTER);

        JPanel fullGuideView = new JPanel(new BorderLayout());
        fullGuideView.setOpaque(false);

        centerCardPanel.add(recycleView, "RECYCLE_MODE");
        centerCardPanel.add(fullGuideView, "FULL_GUIDE_MODE");
        
        add(centerCardPanel, BorderLayout.CENTER);
    }

    private void filterCategoriesByTab(String tabName) {
        JPanel fullGuideView = (JPanel) centerCardPanel.getComponent(1);
        
        if ("재활용폐기물".equals(tabName)) {
            mainSplit.setRightComponent(detailScrollPane);
            cardLayout.show(centerCardPanel, "RECYCLE_MODE");
            
            SwingUtilities.invokeLater(() -> {
                leftSplit.setDividerLocation(120);
                mainSplit.setDividerLocation(300);
            });

            if (categoryListModel.isEmpty()) {
                allCategoryMap.keySet().forEach(categoryListModel::addElement);
                categoryList.setSelectedIndex(0);
            }
            refreshDetailView();
        } else {
            cardLayout.show(centerCardPanel, "FULL_GUIDE_MODE");
            fullGuideView.removeAll();
            fullGuideView.add(detailScrollPane, BorderLayout.CENTER); 
            fullGuideView.revalidate();
            fullGuideView.repaint();

            if ("일반종량제폐기물".equals(tabName)) {
                editorPane.setText(GuideDAO.getGeneralWasteGuideHtml());
            } else if ("음식물류폐기물".equals(tabName)) {
                editorPane.setText(GuideDAO.getFoodWasteGuideHtml());
            } else if ("불연성종량제폐기물".equals(tabName)) {
                editorPane.setText(GuideDAO.getNonFlammableWasteGuideHtml());
            } else if ("대형폐기물".equals(tabName)) {
                editorPane.setText(GuideDAO.getBulkyWasteGuideHtml());
            } else if ("공사장 생활폐기물".equals(tabName)) {
                editorPane.setText(GuideDAO.getConstructionWasteGuideHtml());
            } else if ("생활계 유해폐기물".equals(tabName)) {
                editorPane.setText(GuideDAO.getHazardousWasteGuideHtml());
            } else if ("기타".equals(tabName)) {
                editorPane.setText(GuideDAO.getOtherWasteGuideHtml());
            } else {
                editorPane.setText("<html><body style='color:white; font-family:맑은 고딕; padding:20px;'>" +
                        "<h2>" + tabName + "</h2>준비 중인 가이드입니다.</body></html>");
            }
        }
        editorPane.setCaretPosition(0);
    }

    private void refreshDetailView() {
        String selItem = itemList.getSelectedValue();
        String selCat = categoryList.getSelectedValue();
        if (selItem != null && selCat != null) {
            try {
                ItemDetail detail = GuideDAO.getItemDetail(selItem, selCat);
                if (detail != null) {
                    updateDetailWithImages(detail);
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void updateDetailWithImages(ItemDetail item) {
        String projectPath = System.getProperty("user.dir").replace("\\", "/");
        String baseUrl = "file:///" + projectPath + "/src/main/webapp/";
        
        String html = "<html><head><base href='" + baseUrl + "'><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 15px; line-height: 1.6; }" +
                ".header { color: #00fff0; font-size: 22px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin-bottom: 15px; }" +
                ".content { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; }" +
                "img { border-radius: 5px; margin-top: 15px; border: 1px solid #825aff; max-width: 90%; }" +
                "</style></head><body>" +
                "<div class='header'>" + item.itemName + "</div>" +
                "<div class='content'>" + item.disposalGuide + "</div>" +
                "</body></html>";
        
        editorPane.setText(html);
        editorPane.setCaretPosition(0);
    }

    private void setupEvents() {
        mainTabbedPane.addChangeListener(e -> {
            int sel = mainTabbedPane.getSelectedIndex();
            if (sel != -1) filterCategoriesByTab(mainTabbedPane.getTitleAt(sel));
        });
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = categoryList.getSelectedValue();
                if (selected != null) loadItems(allCategoryMap.get(selected));
            }
        });
        itemList.addListSelectionListener(e -> { 
            if (!e.getValueIsAdjusting()) refreshDetailView(); 
        });
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
    }

    private void loadInitialData() {
        SwingUtilities.invokeLater(() -> {
            mainTabbedPane.setSelectedIndex(0);
            filterCategoriesByTab("재활용폐기물");
        });
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;
        try {
            List<ItemDetail> allItems = GuideDAO.getAllItems();
            ItemDetail found = allItems.stream().filter(i -> i.itemName.contains(keyword)).findFirst().orElse(null);
            if (found != null) {
                mainTabbedPane.setSelectedIndex(0);
                categoryList.setSelectedValue(found.categoryName, true);
                itemList.setSelectedValue(found.itemName, true);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadItems(String categoryId) {
        try {
            List<String> items = GuideDAO.getItemNamesByCategory(categoryId);
            itemListModel.clear();
            items.forEach(itemListModel::addElement);
            if (!items.isEmpty()) itemList.setSelectedIndex(0);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private class CustomTabbedPaneUI extends BasicTabbedPaneUI {
        @Override protected void installDefaults() { super.installDefaults(); contentBorderInsets = new Insets(0, 0, 0, 0); }
        @Override protected void paintTabBackground(Graphics g, int tp, int ti, int x, int y, int w, int h, boolean isSel) {
            g.setColor(isSel ? BG_PANEL : BG_DARK); g.fillRect(x, y, w, h);
            if (isSel) { g.setColor(POINT_CYAN); g.fillRect(x, y + h - 3, w, 3); }
        }
        @Override protected void paintContentBorder(Graphics g, int tp, int si) {}
    }

    private JScrollPane createStyledScrollPane(Component view, String title) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setBorder(new TitledBorder(new LineBorder(POINT_PURPLE, 1), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("맑은 고딕", Font.BOLD, 12), POINT_CYAN));
        scroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
        return scroll;
    }

    private <T> JList<T> createStyledList(ListModel<T> model) {
        JList<T> list = new JList<>(model);
        list.setBackground(BG_PANEL); list.setForeground(Color.WHITE);
        list.setSelectionBackground(POINT_PURPLE); list.setFixedCellHeight(35);
        list.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        return list;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text); btn.setBackground(bg); btn.setForeground(fg);
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JSplitPane createCleanSplitPane(int orientation, Component left, Component right) {
        JSplitPane split = new JSplitPane(orientation, left, right);
        split.setOpaque(false); split.setBorder(null); split.setDividerSize(4);
        split.setUI(new BasicSplitPaneUI() {
            @Override public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) { @Override public void paint(Graphics g) { g.setColor(BG_DARK); g.fillRect(0, 0, getWidth(), getHeight()); } };
            }
        });
        return split;
    }

    private static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { this.thumbColor = POINT_PURPLE; this.trackColor = BG_DARK; }
        @Override protected JButton createDecreaseButton(int i) { return new JButton() { @Override public Dimension getPreferredSize() { return new Dimension(0,0); } }; }
        @Override protected JButton createIncreaseButton(int i) { return new JButton() { @Override public Dimension getPreferredSize() { return new Dimension(0,0); } }; }
    }

    private void displayErrorUI(String message) {
        removeAll(); add(new JLabel(message) {{ setForeground(Color.RED); }}); revalidate(); repaint();
    }
}