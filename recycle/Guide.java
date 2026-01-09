package recycle;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
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

    private JList<String> categoryList;
    private JList<String> itemList;
    private DefaultListModel<String> itemListModel;
    private JEditorPane editorPane;
    private Map<String, String> categoryMap; 
    
    private JTextField searchField;
    private JButton searchButton;

    public Guide() {
        try {
            GuideDAO.initializeDatabase();
            this.categoryMap = GuideDAO.getAllCategoryNamesAndIds(); 
        } catch (Exception e) {
            displayErrorUI("가이드 정보를 불러올 수 없습니다.");
            return;
        }

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        searchField = new JTextField(20);
        searchButton = new JButton("검색");
        
        searchPanel.add(new JLabel("품목 검색: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        JLabel titleLabel = new JLabel("  분리수거 가이드");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        add(topPanel, BorderLayout.NORTH);

        categoryList = new JList<>(new Vector<>(categoryMap.keySet()));
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        
        JScrollPane categoryScroll = new JScrollPane(categoryList);
        categoryScroll.setBorder(BorderFactory.createTitledBorder("카테고리"));

        itemListModel = new DefaultListModel<>();
        itemList = new JList<>(itemListModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JScrollPane itemScroll = new JScrollPane(itemList);
        itemScroll.setBorder(BorderFactory.createTitledBorder("품목 리스트"));

        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        JScrollPane detailScroll = new JScrollPane(editorPane);
        detailScroll.setBorder(BorderFactory.createTitledBorder("배출 방법 및 주의사항"));

        JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, categoryScroll, itemScroll);
        leftSplit.setDividerLocation(150);
        
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, detailScroll);
        mainSplit.setDividerLocation(350);
        
        add(mainSplit, BorderLayout.CENTER);

     
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedCategory = categoryList.getSelectedValue();
                if (selectedCategory != null) {
                    loadItems(categoryMap.get(selectedCategory), selectedCategory);
                }
            }
        });

        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedItemName = itemList.getSelectedValue();
                if (selectedItemName != null) {
                    try {
                        String selectedCategory = categoryList.getSelectedValue();
                        ItemDetail itemDetail = GuideDAO.getItemDetail(selectedItemName, selectedCategory);
                        if (itemDetail != null) {
                            updateDetailContent(itemDetail);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
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

        if (!categoryMap.isEmpty()) {
            categoryList.setSelectedIndex(0);
        }
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "검색어를 입력해주세요.");
            return;
        }

        try {
            List<ItemDetail> allItems = GuideDAO.getAllItems();
            ItemDetail foundItem = null;

            for (ItemDetail item : allItems) {
                if (item.itemName.contains(keyword)) {
                    foundItem = item;
                    break;
                }
            }

            if (foundItem != null) {
           
                categoryList.setSelectedValue(foundItem.categoryName, true);
                
                loadItems(categoryMap.get(foundItem.categoryName), foundItem.categoryName);
                
                itemList.setSelectedValue(foundItem.itemName, true);
                
                updateDetailContent(foundItem);
            } else {
                JOptionPane.showMessageDialog(this, "'" + keyword + "'에 대한 검색 결과가 없습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "검색 중 오류가 발생했습니다.");
        }
    }

    private void loadItems(String categoryId, String categoryName) {
        try {
            List<String> items = GuideDAO.getItemNamesByCategory(categoryId);
            itemListModel.clear();
            for (String item : items) {
                itemListModel.addElement(item);
            }
            if (!items.isEmpty()) {
                itemList.setSelectedIndex(0);
            } else {
                editorPane.setText("<html><body>품목 데이터가 없습니다.</body></html>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateDetailContent(ItemDetail item) {
        String cssStyles = GuideDAO.getCssStyles();
        StringBuilder contentBuilder = new StringBuilder();
        
        contentBuilder.append("<h1>").append(item.itemName).append("</h1>");
        contentBuilder.append("<p><b>카테고리:</b> ").append(item.categoryName).append("</p>");
        contentBuilder.append("<hr>");

        String itemGuide = item.disposalGuide;
        if (itemGuide == null || itemGuide.trim().isEmpty() || itemGuide.equals("공통 가이드 참조")) {
            Map<String, String> commonGuideMap = INITIAL_GUIDE_DATA.get(item.categoryName);
            if (commonGuideMap != null) {
                for (String val : commonGuideMap.values()) {
                    contentBuilder.append(val);
                }
            } else {
                contentBuilder.append("<p>상세 배출 가이드가 준비 중입니다.</p>");
            }
        } else {
            contentBuilder.append("<div class='guide-text'>").append(itemGuide.replace("\n", "<br>")).append("</div>");
        }

        String styledHtml = String.format("<html><head>%s</head><body>%s</body></html>", cssStyles, contentBuilder.toString());
        editorPane.setText(styledHtml);
        editorPane.setCaretPosition(0);
    }

    private void displayErrorUI(String message) {
        removeAll();
        setLayout(new GridBagLayout()); 
        JLabel errorLabel = new JLabel(message, SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        add(errorLabel);
        revalidate();
        repaint();
    }
}