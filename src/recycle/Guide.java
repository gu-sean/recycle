package recycle;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap; 
import java.util.Vector; 

import db.DAO.GuideDAO; 
import db.DAO.GuideDAO.ItemDetail; 


public class Guide extends JPanel {

    private static final Map<String, Map<String, String>> INITIAL_GUIDE_DATA = new LinkedHashMap<>();
    static {
        // 1. 종이
        Map<String, String> paperItems = new LinkedHashMap<>();
        paperItems.put("기본 배출 원칙", "<h3>종이류 배출 방법</h3><ul><li>물기에 젖지 않도록 보관하여 배출합니다.</li><li>비닐 코팅, 스프링, 테이프, 철핀 등 다른 재질은 모두 제거해야 합니다.</li><li>반듯하게 펴서 종이류끼리 끈으로 묶어 배출합니다.</li></ul>");
        paperItems.put("예외 (오염/비재활용)", "<h3>주의 사항</h3><ul><li class=\"note\">* 기름 등 이물질에 심하게 오염된 종이(박스)는 종량제 봉투로 배출합니다.</li></ul>"); 
        INITIAL_GUIDE_DATA.put("종이", paperItems);

        // 2. 비닐
        Map<String, String> vinylItems = new LinkedHashMap<>();
        vinylItems.put("기본 배출 원칙", "<h2>합성수지류</h2><h3>비닐류 배출 방법</h3><ul><li>내용물을 완전히 비우고 물로 헹구는 등 이물질을 제거하여 깨끗하게 만듭니다.</li><li>흩날리지 않도록 투명 봉투에 담아 배출합니다.</li></ul>");
        vinylItems.put("예외 (오염/복합재질)", "<h3>주의 사항</h3><ul><li class=\"note\">* 이물질 제거가 어려운 랩 필름이나 복합 재질 비닐은 종량제 봉투로 배출합니다.</li></ul>"); 
        INITIAL_GUIDE_DATA.put("비닐", vinylItems);

        // 3. 유리병
        Map<String, String> glassItems = new LinkedHashMap<>();
        glassItems.put("기본 배출 원칙", "<h2>유리병 배출 방법</h2><h3>음료수병/주류병 등</h3><ul><li>내용물을 비우고 헹군 후 배출합니다.</li><li>금속 뚜껑이나 라벨은 제거하여 재질별로 분리 배출합니다.</li></ul>");
        glassItems.put("예외 (비재활용 유리)", "<h3>주의 사항</h3><ul><li class=\"note\"> 깨진 유리, 거울, 도자기류, 내열 식기는 재활용이 안 되므로 신문지 등에 싸서 종량제 봉투나 불연성 폐기물(마대)로 배출합니다.</li></ul>"); 
        INITIAL_GUIDE_DATA.put("유리병", glassItems);
        
        // 4. 종이팩
        Map<String, String> packItems = new LinkedHashMap<>();
        packItems.put("기본 배출 원칙", "<h2>종이팩 배출 방법</h2><h3>우유팩/멸균팩/종이컵</h3><ul><li>내용물을 비우고 물로 헹군 후 펼쳐서 건조합니다.</li><li>빨대, 비닐 등 다른 재질은 제거합니다.</li><li>일반 종이와 섞이지 않게 전용 수거함에 배출합니다.</li></ul>");
        packItems.put("예외", "<h3>주의 사항</h3><ul><li class=\"note\"> 오염이 심한 종이팩은 종량제 봉투로 배출합니다.</li></ul>"); 
        INITIAL_GUIDE_DATA.put("종이팩", packItems);

        // 5. 캔ㆍ고철
        Map<String, String> metalItems = new LinkedHashMap<>();
        metalItems.put("기본 배출 원칙", "<h2>캔ㆍ고철 배출 방법</h2><h3>금속캔, 고철류</h3><ul><li>내용물을 비우고 헹군 후 배출합니다.</li><li>플라스틱 뚜껑 등 다른 재질은 제거하여 분리 배출합니다.</li><li>고철류는 이물질이 섞이지 않도록 모아서 배출합니다.</li></ul>");
        metalItems.put("특수 캔 (가스)", "<h2>부탄가스/스프레이</h2><ul><li>내용물을 완전히 제거(가스 배출)하고, 구멍을 뚫어 배출합니다.</li></ul>");
        INITIAL_GUIDE_DATA.put("캔ㆍ고철", metalItems);
        
        // 6. 스티로폼
        Map<String, String> styrofoamItems = new LinkedHashMap<>();
        styrofoamItems.put("기본 배출 원칙", "<h2>합성수지류</h2><h3>스티로폼 배출 방법</h3><ul><li>이물질(테이프, 부착 상표)을 제거한 후 흩날리지 않게 묶어 배출합니다.</li><li>유색 스티로폼은 재활용이 안 됩니다.</li></ul>");
        styrofoamItems.put("예외 (오염)", "<h3>주의 사항</h3><ul><li class=\"note\"> 음식물 등 이물질에 오염된 스티로폼은 종량제 봉투로 배출합니다.</li></ul>"); 
        INITIAL_GUIDE_DATA.put("스티로폼", styrofoamItems);

        // 7. 플라스틱
        Map<String, String> plasticItems = new LinkedHashMap<>();
        plasticItems.put("기본 배출 원칙", "<h2>합성수지류</h2><h3>플라스틱 배출 방법</h3><ul><li>내용물을 완전히 비우고 깨끗하게 헹군 후 배출합니다.</li><li>부착 상표, 뚜껑 등 다른 재질은 제거하여 재질별로 분리 배출합니다.</li></ul>");
        plasticItems.put("예외 (복합재질/오염)", "<h3>주의 사항</h3><ul><li class=\"note\"> 플라스틱 이외의 재질이 섞여 분리가 어렵거나(칫솔, 완구), 오염이 심한 품목은 종량제 봉투로 배출합니다.</li></ul>"); 
        INITIAL_GUIDE_DATA.put("플라스틱", plasticItems);
        
        // 8. 기타
        Map<String, String> etcItems = new LinkedHashMap<>();
        etcItems.put("의류/원단류", "<h2>의류 및 원단류</h2><h3>배출 방법</h3><ul><li>깨끗한 의류, 신발, 원단 등은 폐의류 전용수거함에 배출합니다.</li></ul>");
        etcItems.put("일반 쓰레기", "<h3>기타 (재활용 불가)</h3><ul><li class=\"note\"> 음식물쓰레기, 폐건전지 등은 각 품목의 지정된 규격에 따라 전용 수거함 또는 종량제 봉투에 분리 배출합니다.</li></ul>"); 
        INITIAL_GUIDE_DATA.put("기타", etcItems);
    }
    

    private Map<String, String> categoryMap; 
    

    private JList<String> categoryList;
    
 
    private JList<String> itemList;
    private DefaultListModel<String> itemListModel;
    
  
    private JEditorPane editorPane;
    

    private Map<String, ItemDetail> currentItemDetails; 

    public Guide() {
        try {
            GuideDAO.initializeDatabase();
        } catch (Exception e) {
            System.err.println("DB 초기화 중 오류 발생: " + e.getMessage());
            displayErrorUI("DB 연결 및 초기화에 실패했습니다. " + e.getMessage());
            return;
        }


        try {
            this.categoryMap = GuideDAO.getAllCategoryNamesAndIds(); 
        } catch (Exception e) {
            System.err.println("카테고리 데이터 로드 중 오류 발생: " + e.getMessage());
            displayErrorUI("가이드 정보를 불러올 수 없습니다. DB 오류: " + e.getMessage());
            return;
        }
        

        setLayout(new BorderLayout(5, 5)); 
        

        // 1. 왼쪽: 카테고리 목록 

        categoryList = new JList<>(categoryMap.keySet().toArray(new String[0]));
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        categoryList.setFixedCellHeight(30);
        

        // 2. 중앙: 세부 품목 목록 

        itemListModel = new DefaultListModel<>();
        itemList = new JList<>(itemListModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        itemList.setFixedCellHeight(25);
        

        // 3. 오른쪽: 상세 내용 표시 패널 

        JPanel detailPanel = createDetailViewPanel();
        

        // 4. 리스너 설정

        
        // (A) 카테고리 선택 리스너: 세부 품목 목록 갱신 및 첫 번째 세부 품목 자동 선택
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedCategory = categoryList.getSelectedValue();
                if (selectedCategory != null) {
                    String categoryId = categoryMap.get(selectedCategory);
                    loadItems(categoryId, selectedCategory);
                }
            }
        });

        // (B) 세부 품목 선택 리스너: 상세 내용 갱신
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedItemName = itemList.getSelectedValue();
                if (selectedItemName != null && currentItemDetails != null) {
                    ItemDetail detail = currentItemDetails.get(selectedItemName);
                    if (detail != null) {
                        updateDetailContent(detail);
                    }
                }
            }
        });

        // 5. Split Pane 구성 (중앙과 오른쪽을 먼저 합침)

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplitPane.setDividerLocation(150); 
        rightSplitPane.setResizeWeight(0.0); 
        
        rightSplitPane.setLeftComponent(new JScrollPane(itemList));
        rightSplitPane.setRightComponent(detailPanel);

        // 전체 Split Pane 구성 (왼쪽과 오른쪽 영역 합침)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(150); 
        mainSplitPane.setResizeWeight(0.0); 
        
        mainSplitPane.setLeftComponent(new JScrollPane(categoryList));
        mainSplitPane.setRightComponent(rightSplitPane);
        
        add(mainSplitPane, BorderLayout.CENTER);
    
        // 초기 로딩 시 첫 번째 카테고리 자동 선택
        if (!categoryMap.isEmpty()) {
            categoryList.setSelectedIndex(0);
        }
    }
    
  
    private void loadItems(String categoryId, String categoryName) {
        itemListModel.clear();
        currentItemDetails = new LinkedHashMap<>(); 
        editorPane.setText("<html><body><p style='color: gray; text-align: center; font-size: 11pt; margin-top: 50px;'>세부 품목을 선택해주세요.</p></body></html>");
        
        try {

            List<ItemDetail> details = GuideDAO.getItemsByCategory(categoryId);
            
            if (details.isEmpty()) {
                itemListModel.addElement("등록된 품목이 없습니다.");
            } else {
                for (ItemDetail item : details) {
                    itemListModel.addElement(item.itemName);
                    currentItemDetails.put(item.itemName, item);
                }
                

                itemList.setSelectedIndex(0); 
            }
        } catch (Exception e) {
            itemListModel.addElement("데이터 로드 오류");
            System.err.println(categoryName + " 품목 로드 오류: " + e.getMessage());
            editorPane.setText("<html><body><p style='color: red; text-align: center;'>품목 로드 중 오류 발생.</p></body></html>");
        }
    }

 
    private JPanel createDetailViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        
        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setBackground(Color.WHITE); 
        editorPane.setText("<html><body><p style='color: gray; text-align: center; font-size: 14pt; margin-top: 50px;'>왼쪽 목록에서 카테고리를 선택하세요.</p></body></html>");
        
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); 

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }


    private void updateDetailContent(ItemDetail item) {
        try {
            
            StringBuilder contentBuilder = new StringBuilder();
            String itemGuide = item.disposalGuide; 
            

            String cssStyles = "<style>"
                    + "h1 { color: #008000; margin-top: 0px; margin-bottom: 20px; font-size: 18pt; border-bottom: 3px solid #008000; padding-bottom: 8px; }"
                    + "h2 { font-size: 14pt; color: #333333; margin-top: 25px; margin-bottom: 10px; font-weight: bold; background-color: #f0fff0; padding: 5px; border-left: 5px solid #008000; }"
                    + "h3 { font-size: 12pt; color: #1a1a1a; margin-top: 15px; margin-bottom: 5px; }"
                    + "p { margin-bottom: 15px; }"
                    + ".guide-text { padding: 10px; border: 1px solid #ddd; background-color: #f9f9f9; white-space: pre-wrap; }"

                    + ".common-guide { background-color: #f0f0f0; padding: 15px; border: 1px solid #cccccc; margin-top: 20px; }"
                    + "body { font-family: 'Malgun Gothic', '맑은 고딕'; font-size: 11pt; line-height: 1.6; padding: 5px; }" 
                    + ".note { padding-left: 15px; list-style-type: none; font-style: italic; color: #ff5722; }" 
                    + "</style>";
            

            contentBuilder.append(String.format("<h1>%s 분리수거 가이드</h1>", item.itemName)); 
            
            contentBuilder.append("<h2>배출 방법</h2>");

            if (itemGuide.contains("배출방법을 준수하여 배출합니다.")) { 

                
                Map<String, String> commonGuideMap = INITIAL_GUIDE_DATA.get(item.categoryName);
                if (commonGuideMap != null) {
                    contentBuilder.append("<div class='common-guide'>");
                    
                    for (Map.Entry<String, String> entry : commonGuideMap.entrySet()) {
                         contentBuilder.append(entry.getValue());
                    }
                    contentBuilder.append("</div>");
                }
            } else {
                String guideHtml = itemGuide.replace("\n", "<br>");
                contentBuilder.append(String.format("<div class='guide-text'>%s</div>", guideHtml));
            }
            
      
            String styledHtml = String.format(
                "<html><head>%s</head><body>%s</body></html>",
                cssStyles,
                contentBuilder.toString()
            );
            
            editorPane.setText(styledHtml);
            editorPane.setCaretPosition(0); 

        } catch (Exception e) {
            editorPane.setText("<html><body><p style='color: red;'>상세 정보 표시 중 오류 발생.</p></body></html>");
        }
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