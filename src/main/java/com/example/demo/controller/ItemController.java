package com.example.demo.controller;

import com.example.demo.model.Item;
import com.example.demo.model.User; // 유저 모델 임포트
import com.example.demo.model.PointLog;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;

import db.DTO.GuideDTO;

import com.example.demo.repository.PointLogRepository;// 👈 이 줄이 없어서 에러가 난 것입니다!
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PointLogRepository pointLogRepository;

    @GetMapping("/items")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

 // ItemController.java의 purchaseItem 메서드를 아래와 같이 교체하세요.

    
    @PostMapping("/items/{id}/purchase")
    public ResponseEntity<?> purchaseItem(@PathVariable String id, @RequestBody Map<String, String> requestData) {
        String userId = requestData.get("userId");
        
        try {
            Item item = itemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
            // 💡 userId가 String이면 Long.parseLong 대신 String으로 조회해야 할 수도 있습니다. 
            // UserRepository의 findById 인자 타입에 맞춰주세요.
            User user = userRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // ✅ [수정] getPoints() -> getBalancePoints()
            if (user.getBalancePoints() < item.getItemPrice()) {
                return ResponseEntity.status(400).body("잔여 포인트가 부족합니다.");
            }

            if (item.getStock() <= 0) {
                return ResponseEntity.status(400).body("상품 재고가 없습니다.");
            }

            // ✅ [수정] getPoints(), setPoints() -> getBalancePoints(), setBalancePoints()
            int usedPoints = item.getItemPrice();
            user.setBalancePoints(user.getBalancePoints() - usedPoints);
            userRepository.save(user);
            
            item.setStock(item.getStock() - 1);
            itemRepository.save(item);

            try {
                PointLog purchaseLog = new PointLog(
                    userId, 
                    "차감", 
                    item.getItemName() + " 구매", 
                    -usedPoints
                );
                
                pointLogRepository.save(purchaseLog);
                System.out.println("로그 저장 성공: " + item.getItemName());
            } catch (Exception logError) {
                System.err.println("로그 저장 실패: " + logError.getMessage());
            }

            return ResponseEntity.ok(user);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    
 // ItemController.java 내의 getExtraGuides를 아래 코드로 교체하세요.

    @GetMapping("/guide/static") // 👈 프론트엔드의 호출 경로와 정확히 일치시킴
    public ResponseEntity<String> getStaticGuide(@RequestParam("type") String type) {
        // 프론트엔드에서 보낸 type(예: FOOD, NORMAL 등)에 따라 해당 HTML을 반환
        String htmlContent = "";
        
        switch (type) {
            case "FOOD":
                htmlContent = getFoodWasteGuideHtml();
                break;
            case "NORMAL":
                htmlContent = getGeneralWasteGuideHtml();
                break;
            case "NON_BURN":
                htmlContent = getNonFlammableWasteGuideHtml();
                break;
            case "LARGE":
                htmlContent = getBulkyWasteGuideHtml();
                break;
            case "CONSTRUCTION":
                htmlContent = getConstructionWasteGuideHtml();
                break;
            case "HAZARDOUS":
                htmlContent = getHazardousWasteGuideHtml();
                break;
            case "OTHER":
                htmlContent = getOtherWasteGuideHtml();
                break;
            default:
                htmlContent = "<div style='padding:20px; color:white;'>해당 카테고리의 상세 가이드를 준비 중입니다.</div>";
                break;
        }
        
        // CSS 스타일을 HTML 내용 앞에 결합하여 반환
        return ResponseEntity.ok(getCommonStyle() + htmlContent);
    }

    private Map<String, String> createGuideData(String title, String content) {
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        return data;
    }


    
    // 공통 CSS (내용이 잘 보이도록 배경과 폰트색 고정)
    private String getCommonStyle() {
        return "<style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 10px; }" +
                ".section-title { color: #00fff0; font-size: 18px; font-weight: bold; border-bottom: 2px solid #825aff; margin: 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; }" +
                ".img-group { display: flex; gap: 10px; justify-content: center; margin-top: 10px; }" +
                ".guide-img { width: 100px; height: 100px; border-radius: 5px; object-fit: cover; border: 1px solid #555; }" +
                "</style>";
    }

    public String getConstructionWasteGuideHtml() {
        // 웹 서버 상대 경로 설정 (src/main/resources/static/images/...)
        String imgPathUrl = "/images/construction site household waste/construction site household waste";

        return "<style>" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; line-height: 1.6; color: #ffffff; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" +
                ".highlight-place { color: #00fff0; font-weight: bold; }" +
                ".step-title { font-weight: bold; color: #ffffff; margin-bottom: 2px; }" +
                ".step-desc { margin-bottom: 15px; color: #e0e0e0; }" +
                ".img-group { margin-top: 20px; text-align: center; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }" +
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; width: 110px; height: 110px; object-fit: cover; }" +
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" +
                "</style>" +
                
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🏗️ 공사장 생활폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목 및 기준</div>" +
                "<div class='content-box'>" +
                "인테리어 공사 등으로 발생하는 <span class='highlight-point'>5톤 미만</span>의 폐기물을 의미합니다.<br><br>" +
                "• <b>불연성 건설폐재류:</b> 폐벽돌, 폐타일, 폐콘크리트, 전선관, 흙 등<br>" +
                "• <b>가연성 폐기물:</b> 폐목재, 폐벽지, 장판, 폐합성수지(스티로폼 등)<br>" +
                "• <b>시설물/기타:</b> 싱크대, 세면대, 변기, 욕조, 문틀, 고철류" +
                "<div class='img-group'>" +
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>" +
                "</div></div>" +

                "<div class='section-title'>🚛 성상별 배출방법</div>" +
                "<div class='content-box'>" +
                "<b>1. 불연성 폐기물 <span class='highlight-point'>(타지 않는 것)</span></b><br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight-place'>특수규격봉투(PP마대)</span>에 담아 지정 장소에 배출하세요.<br>" +
                "&nbsp;&nbsp;&nbsp;- 벽돌이나 타일은 봉투가 터지지 않게 적정량만 담아야 합니다.<br><br>" +
                "<b>2. 가연성 및 대형 폐기물</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 욕조, 변기 등은 <span class='highlight-place'>대형폐기물 스티커</span>를 부착 후 배출하세요.<br>" +
                "&nbsp;&nbsp;&nbsp;- 고철이나 플라스틱은 재활용으로 분류 배출이 가능합니다.<br><br>" +
                "<b>3. 배출 신고</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 대량 배출 시 지자체 지정 공공선별장 혹은 대행업체를 이용하세요." +
                "</div>" +

                "<div class='section-title'>⚠️ 주의사항</div>" +
                "<div class='content-box'>" +
                "• <b>혼합 금지:</b> 마대에 가연성과 불연성을 섞으면 수거되지 않을 수 있습니다.<br>" +
                "• <b>5톤 이상:</b> 발생량이 5톤 이상이면 별도의 신고 절차가 필요합니다." +
                "<p class='sub-text-red'>※ 특수규격마대는 인근 편의점이나 마트에서 구매해야 합니다.</p>" +
                "</div>" +

                "<div class='section-title'>♻️ 처리 과정</div>" +
                "<div class='content-box'>" +
                "    <div class='step-title'>STEP 01. 성상별 정밀 선별</div>" +
                "    <div class='step-desc'>수거된 폐기물은 가연성, 불연성, 재활용으로 분류됩니다.</div>" +
                "    <div class='step-title'>STEP 02. 시설 운반 및 처리</div>" +
                "    <div class='step-desc'>폐목재와 폐콘크리트는 전용 파쇄 시설로 이동합니다.</div>" +
                "    <div class='step-title'>STEP 03. 친환경 자원화</div>" +
                "    <div class='step-desc'>파쇄된 폐재류는 도로 공사 등에 쓰이는 <span class='highlight-point'>순환골재</span>로 재활용됩니다.</div>" +
                "</div>";
    }

    public String getHazardousWasteGuideHtml() {
        // 웹 서버 상대 경로 설정 (src/main/resources/static/images/...)
        String imgPathUrl = "/images/household hazardous waste/household hazardous waste";

        return "<style>" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; line-height: 1.6; color: #ffffff; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" + 
                ".highlight-place { color: #00fff0; font-weight: bold; }" + 
                ".step-title { font-weight: bold; color: #ffffff; margin-bottom: 2px; }" + 
                ".step-desc { margin-bottom: 15px; color: #e0e0e0; }" + 
                ".img-group { margin-top: 20px; text-align: center; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }" +
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; width: 110px; height: 110px; object-fit: cover; }" +
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" + 
                ".sub-text-cyan { font-size: 13px; color: #00fff0; margin-top: 8px; }" +
                "</style>" +
                
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🧪 생활계 유해폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목 및 유해성</div>" +
                "<div class='content-box'>" +
                "인체에 치명적이거나 생태계 파괴 위험이 있어 <span class='highlight-point'>특별 관리</span>가 필요한 폐기물입니다.<br><br>" +
                "• <b>폐의약품:</b> 유통기한 경과 또는 미복용 알약, 가루약, 물약, 연고 등<br>" +
                "• <b>수은 제품:</b> 수은 체온계, 혈압계, 수은 온도계 등<br>" +
                "• <b>생활 화학제품:</b> 폐농약, 폐페인트, 살충제, 소독제, 강력 접착제 등<br>" +
                "• <b>기타 유해물:</b> 라돈 침대 등 천연방사성 생활폐기물" +
                "<div class='img-group'>" +
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>" +
                "</div></div>" +

                "<div class='section-title'>🚛 품목별 상세 배출방법</div>" +
                "<div class='content-box'>" +
                "<b>1. 폐의약품</b><br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight-place'>약국, 보건소, 주민센터</span> 전용 수거함에 상시 배출하세요.<br>" +
                "&nbsp;&nbsp;&nbsp;- 알약은 <span class='highlight-point'>내용물만 봉투에 모아서</span> 배출하는 것이 원칙입니다.<br><br>" +
                "<b>2. 수은 함유 제품</b><br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight-place'>주민센터 전용 수거함</span>에 파손되지 않도록 완충재로 감싸 배출하세요.<br>" +
                "&nbsp;&nbsp;&nbsp;- 깨진 경우 증기가 발생하므로 <span class='highlight-point'>비닐로 다중 밀봉</span>하여 신속히 배출하세요.<br><br>" +
                "<b>3. 폐농약 및 폐페인트</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 유출 방지를 위해 반드시 <span class='highlight-point'>마개를 꽉 닫아</span> 용기째로 배출하세요.<br>" +
                "&nbsp;&nbsp;&nbsp;- 지자체별 지정된 <span class='highlight-place'>유해폐기물 거점 수거함</span>을 이용하세요." +
                "</div>" +

                "<div class='section-title'>⚠️ 주의사항 및 금지행위</div>" +
                "<div class='content-box'>" +
                "• <b>무단 투기 금지:</b> 약품을 <span class='highlight-point'>변기나 싱크대</span>에 버리면 수질을 심각하게 오염시킵니다.<br>" +
                "• <b>혼합 금지:</b> 성분이 다른 화학제품을 섞으면 예기치 못한 화학 반응이 일어날 수 있습니다.<br>" +
                "• <b>종량제 배출 금지:</b> 화재나 토양 오염의 주원인이 되므로 절대 봉투에 담지 마세요." +
                "<p class='sub-text-red'>※ 유해폐기물은 반드시 전용 수거함을 이용해 안전하게 처리해야 합니다.</p>" +
                "</div>" +

                "<div class='section-title'>♻️ 안전 처리 과정</div>" +
                "<div class='content-box'>" +
                "    <div class='step-title'>STEP 01. 거점 수거함 분리 수집</div>" +
                "    <div class='step-desc'>공공기관 및 약국에 설치된 <span class='highlight-place'>전용 수거함</span>을 통해 안전하게 수집합니다.</div>" +
                "    <div class='step-title'>STEP 02. 전문 수거 및 밀폐 운반</div>" +
                "    <div class='step-desc'>유출 방지 설비를 갖춘 <span class='highlight-place'>특수 수거 차량</span>이 밀폐된 상태로 운반합니다.</div>" +
                "    <div class='step-title'>STEP 03. 전문 시설 고온 소각</div>" +
                "    <div class='step-desc'>오염 방지 설비가 완비된 전문 시설에서 <span class='highlight-point'>고온 소각</span>하여 유해성을 완전히 제거합니다.</div>" +
                "    <p class='sub-text-cyan'>※ 일부 지역에서는 우체통을 통한 폐의약품 회수 서비스도 시행하고 있습니다.</p>" +
                "</div>";
    }

    public String getOtherWasteGuideHtml() {
        // 웹 서버 상대 경로 설정 (static/images/Other Waste/...)
        String imgPathUrl = "/images/Other Waste/Other Waste";

        return "<style>" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; line-height: 1.6; color: #ffffff; }" +
                ".highlight-cyan { color: #00fff0; font-weight: bold; }" +
                ".highlight-orange { color: #ff9d00; font-weight: bold; }" +
                ".highlight-red { color: #ff5555; font-weight: bold; }" +
                ".img-group { margin-top: 20px; text-align: center; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }" +
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; width: 110px; height: 110px; object-fit: cover; }" +
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" + 
                ".sub-text-cyan { font-size: 13px; color: #00fff0; margin-top: 8px; }" +
                "</style>" +
                
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🌾 영농폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목 및 분류</div>" +
                "<div class='content-box'>" +
                "<span class='highlight-cyan'>[영농폐비닐]</span><br>" +
                "• 하우스 비닐(흰색), 로덴 비닐(검정색), 멀칭 비닐 등<br><br>" +
                "<span class='highlight-cyan'>[폐농약용기류]</span><br>" +
                "• 농약 유리병, 플라스틱 병(살충제, 살균제, 제초제 등)<br>" +
                "• 농약 봉지류(수화제, 입제 등)<br>" +
                "<p class='sub-text-red'>※ 주의: 일반 생활폐기물(페트병, 캔 등)과 섞이지 않도록 주의하세요.</p>" +
                "<div class='img-group'>" +
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>" +
                "</div></div>" +

                "<div class='section-title'>🚛 올바른 배출 방법</div>" +
                "<div class='content-box'>" +
                "<b>1. 영농폐비닐</b><br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight-red'>흙, 자갈, 잡초 등 이물질을 제거</span>한 후 배출하세요.<br>" +
                "&nbsp;&nbsp;&nbsp;- 하우스용과 멀칭용을 <span class='highlight-orange'>색상별로 분리</span>하여 묶어주세요.<br><br>" +
                "<b>2. 폐농약용기</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 병 내부의 <span class='highlight-red'>잔액을 비우고 세척</span>하여 안전하게 배출하세요.<br>" +
                "&nbsp;&nbsp;&nbsp;- 재질별(유리, 플라스틱, 봉지)로 구분하여 <span class='highlight-orange'>전용 그물망</span>에 담으세요.<br><br>" +
                "<b>3. 배출 장소</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 마을별로 지정된 <span class='highlight-cyan'>마을 공동집하장</span>에 상시 배출 가능합니다." +
                "</div>" +

                "<div class='section-title'>💰 영농폐기물 수거 보상금 제도</div>" +
                "<div class='content-box'>" +
                "농민이 분리 배출한 영농폐기물에 대해 국가와 지자체가 보상금을 지급합니다.<br><br>" +
                "• <span class='highlight-orange'>폐비닐:</span> 이물질 정도에 따라 <span class='highlight-cyan'>등급별(A, B, C) 차등 지급</span><br>" +
                "• <span class='highlight-orange'>폐농약용기:</span> 용기 종류 및 무게에 따라 개별 수거 보상금 지급<br>" +
                "<p class='sub-text-cyan'>※ 보상금은 한국환경공단의 실적 확인 후 해당 지자체에서 지급합니다.</p>" +
                "</div>" +

                "<div class='section-title'>♻️ 처리 및 재활용 과정</div>" +
                "<div class='content-box'>" +
                "<b>STEP 01. 마을 공동집하장 수집</b><br>" +
                "주민들이 배출한 폐기물을 마을 공동집하장에 품목별로 적재합니다.<br><br>" +
                "<b>STEP 02. 한국환경공단 운반</b><br>" +
                "환경공단 전용 수거 차량이 정기적으로 방문하여 집하된 폐기물을 운반합니다.<br><br>" +
                "<b>STEP 03. 선별 및 자원 재생</b><br>" +
                "폐비닐은 정밀 세척 후 <span class='highlight-orange'>플라스틱 재생 원료</span>로 가공되어 새로운 제품으로 탄생합니다." +
                "</div>";
    }

    // 나머지 FOOD, NORMAL, NON_BURN, LARGE도 위와 같은 형식으로 구현...
    public String getFoodWasteGuideHtml() {
        // 웹 서버 환경에 맞춘 이미지 상대 경로 (src/main/resources/static/images/...)
        String imgPathUrl = "/images/food waste/food waste";

        return "<style>" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".section-title-red { color: #ff5555; font-size: 20px; font-weight: bold; border-bottom: 2px solid #ff5555; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; line-height: 1.6; color: #ffffff; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" +
                ".highlight-place { color: #00fff0; font-weight: bold; }" +
                ".step-title { font-weight: bold; color: #ffffff; margin-bottom: 2px; }" +
                ".step-desc { margin-bottom: 15px; color: #e0e0e0; }" +
                ".img-group { margin-top: 15px; text-align: center; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }" + 
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; width: 100px; height: 100px; object-fit: cover; }" +
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" + 
                ".sub-text-cyan { font-size: 13px; color: #00fff0; margin-top: 8px; }" +
                "</style>" +
                
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0; margin-bottom: 10px;'>🍎 음식물류 폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "가공 후 <span class='highlight-point'>동물의 사료나 퇴비</span>로 재활용이 가능한 유기물 폐기물입니다.<br><br>" +
                "• <b>곡류/채소:</b> 쌀밥, 면류, 과일 껍질(바나나, 사과 등), 배추, 무 등<br>" +
                "• <b>조리 음식:</b> 남겨진 반찬류, 국건더기(국물 제외), 상한 음식물" +
                "<div class='img-group'>" +
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "4.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "5.png'>" +
                "</div></div>" +

                "<div class='section-title-red'>⚠️ 음식물류가 아닌 품목 (일반 배출)</div>" +
                "<div class='content-box'>" +
                "동물이 먹을 수 없는 것들은 반드시 <span class='highlight-point'>일반 종량제 봉투</span>에 버려야 합니다.<br><br>" +
                "• <b>단단한 껍데기/뼈:</b> 조개·게·소라 껍데기, 소·돼지·닭의 뼈다귀<br>" +
                "• <b>딱딱한 씨앗/뿌리:</b> 복숭아·살구·감의 씨앗, 쪽파·대파의 뿌리, 양파 껍질<br>" +
                "• <b>기타:</b> 계란 껍질, 티백, 한약재 찌꺼기 등" +
                "<div class='img-group'>" +
                "<img class='guide-img' src='" + imgPathUrl + "6.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "7.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "8.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "9.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "10.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "11.png'>" +
                "</div></div>" +

                "<div class='section-title'>🚛 올바른 배출 방법</div>" +
                "<div class='content-box'>" +
                "<b>1. 수분 및 이물질 제거</b><br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight-point'>물기를 최대한 꽉 짜서</span> 부피를 줄이고 이물질을 제거하세요.<br><br>" +
                "<b>2. 배출 수단 확인</b><br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight-place'>전용 종량제 봉투, 전용 수거함, RFID 장비</span>를 이용하세요.<br><br>" +
                "<b>3. 소금기 제거</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 소금기가 많은 음식은 <span class='highlight-point'>물에 헹궈서</span> 배출하면 더 좋은 자원이 됩니다." +
                "<div class='img-group'>" +
                "<img class='guide-img' src='" + imgPathUrl + "12.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "13.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "14.png'>" +
                "</div></div>" +

                "<div class='section-title'>♻️ 재활용 및 자원화 과정</div>" +
                "<div class='content-box'>" +
                "    <div class='step-title'>STEP 01. 수거 및 이물질 선별</div>" +
                "    <div class='step-desc'>처리 시설에서 파쇄 및 자력 선별을 통해 금속 등 이물질을 제거합니다.</div>" +
                "    <div class='step-title'>STEP 02. 가열 멸균 및 발효</div>" +
                "    <div class='step-desc'>고온 가열로 유해균을 멸균하고 건조/발효 과정을 거칩니다.</div>" +
                "    <div class='step-title'>STEP 03. 자원 재생 (사료/에너지)</div>" +
                "    <div class='step-desc'>가축의 사료나 퇴비로 재탄생하거나 <span class='highlight-point'>바이오 에너지</span>로 회수됩니다.</div>" +
                "</div>";
    }
    public String getGeneralWasteGuideHtml() {
        // 웹 서버 상대 경로 설정
        String imgPathUrl = "/images/general standard waste/general standard waste";

        return "<style>" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; line-height: 1.6; color: #ffffff; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" +
                ".highlight-place { color: #00fff0; font-weight: bold; }" +
                ".step-title { font-weight: bold; color: #ffffff; margin-bottom: 2px; }" +
                ".step-desc { margin-bottom: 15px; color: #e0e0e0; }" +
                ".img-group { margin-top: 20px; text-align: center; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }" +
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; width: 100px; height: 100px; object-fit: cover; }" +
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" +
                "</style>" +
                
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🗑️ 일반종량제 폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "재활용이 불가능하며 불에 잘 타는 <span class='highlight-point'>가연성 폐기물</span>이 해당됩니다.<br><br>" +
                "• <b>오염된 종이류:</b> 사용한 휴지, 기저귀, 음식물이 묻은 종이컵 등<br>" +
                "• <b>복합재질/기타:</b> 볼펜, 칫솔, 노끈, 고무장갑 등<br>" +
                "• <b>나무/가죽:</b> 나무젓가락, 가죽 지갑, 신발류 등<br>" +
                "• <b>비닐류:</b> 보온보냉 팩(은박 코팅), 소량의 비닐 조각" +
                "<div class='img-group'>" +
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "4.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "5.png'>" +
                "</div></div>" +

                "<div class='section-title'>🚛 올바른 배출 방법</div>" +
                "<div class='content-box'>" +
                "<b>1. 가연성 종량제 봉투 사용</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 거주 지역의 전용 <span class='highlight-place'>일반 종량제 봉투</span>에 담아 배출하세요.<br>" +
                "&nbsp;&nbsp;&nbsp;- 봉투 입구를 완전히 묶어서 배출해야 합니다.<br><br>" +
                "<b>2. 분리배출 우선 확인</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 플라스틱, 캔 등 재활용 가능 품목을 최대한 선별 후 배출하세요.<br><br>" +
                "<b>3. 대형 규격 폐기물</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 봉투에 들어가지 않는 크기는 <span class='highlight-place'>대형폐기물</span> 스티커를 부착하세요." +
                "</div>" +

                "<div class='section-title'>⚠️ 주의사항</div>" +
                "<div class='content-box'>" +
                "• <b>음식물 혼합 금지:</b> 젖은 음식물은 반드시 분리 배출하세요.<br>" +
                "• <b>폭발 위험물 주의:</b> <span class='highlight-point'>부탄가스, 라이터</span> 등은 반드시 비우고 별도 배출하세요." +
                "<p class='sub-text-red'>※ 재활용 마크가 있어도 오염이 심하면 종량제 봉투로 배출해야 합니다.</p>" +
                "</div>" +

                "<div class='section-title'>♻️ 처리 및 에너지화 과정</div>" +
                "<div class='content-box'>" +
                "    <div class='step-title'>STEP 01. 소각 시설 반입</div>" +
                "    <div class='step-desc'>수거된 폐기물은 자원회수시설로 이동하여 고르게 혼합됩니다.</div>" +
                "    <div class='step-title'>STEP 02. 고온 소각</div>" +
                "    <div class='step-desc'><span class='highlight-place'>850℃ 이상의 고온</span>에서 소각하여 부피를 90% 이상 줄입니다.</div>" +
                "    <div class='step-title'>STEP 03. 자원 회수</div>" +
                "    <div class='step-desc'>소각 시 발생하는 열로 <span class='highlight-point'>지역난방 온수 및 전기</span>를 생산하여 공급합니다.</div>" +
                "</div>";
    }
    public String getNonFlammableWasteGuideHtml() {
        // 웹 서버 상대 경로 설정 (static/images/...)
        String imgPathUrl = "/images/incombustible waste/incombustible waste";

        return "<style>" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; line-height: 1.6; color: #ffffff; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" +
                ".highlight-place { color: #00fff0; font-weight: bold; }" +
                ".step-title { font-weight: bold; color: #ffffff; margin-bottom: 2px; }" +
                ".step-desc { margin-bottom: 15px; color: #e0e0e0; }" +
                ".img-group { margin-top: 20px; text-align: center; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }" +
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; width: 100px; height: 100px; object-fit: cover; }" +
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" +
                "</style>" +
                
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🧱 불연성종량제 폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "재활용이 불가능하고 불길에 타지 않는 <span class='highlight-point'>불연성 자재</span>들이 해당됩니다.<br><br>" +
                "• <b>자기류/유리:</b> 사기그릇, 화분, 깨진 유리, 거울, 내열식기(뚝배기 등)<br>" +
                "• <b>소량 건설폐기물:</b> 집수리 시 발생하는 벽돌, 타일, 시멘트 블록 파편<br>" +
                "• <b>기타:</b> 조개껍데기, 연탄재, 장식용 수석, 고양이 배변 모래 등" +
                "<div class='img-group'>" +
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "4.png'>" +
                "</div></div>" +

                "<div class='section-title'>🚛 올바른 배출 방법</div>" +
                "<div class='content-box'>" +
                "<b>1. 전용 마대 사용 <span class='highlight-point'>(필수)</span></b><br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight-place'>불연성 특수규격마대(PP마대)</span>를 사용하여 배출하세요.<br>" +
                "&nbsp;&nbsp;&nbsp;- 일반 비닐 종량제 봉투는 수거되지 않으며 파손 위험이 큽니다.<br><br>" +
                "<b>2. 안전 배출 요령</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 깨진 유리는 환경미화원 부상 방지를 위해 <span class='highlight-point'>신문지로 감싸서</span> 담아주세요.<br>" +
                "&nbsp;&nbsp;&nbsp;- 마대가 너무 무겁지 않도록 묶음 선까지만 담아 배출하세요.<br><br>" +
                "<b>3. 연탄재 배출</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 완전히 식힌 후 투명 비닐봉지에 담아 지자체 지정 장소에 배출하세요." +
                "</div>" +

                "<div class='section-title'>⚠️ 주의사항</div>" +
                "<div class='content-box'>" +
                "• <b>혼합 금지:</b> 음식물이나 가연성 쓰레기를 절대 섞지 마세요.<br>" +
                "• <b>다량 배출 시:</b> 리모델링 등으로 인한 대량 발생은 <span class='highlight-place'>대형폐기물</span>로 별도 신고가 필요합니다." +
                "<p class='sub-text-red'>※ 깨진 유리는 수거 사고를 유발하므로 반드시 다중 포장해 주세요.</p>" +
                "</div>" +

                "<div class='section-title'>♻️ 안전 처리 과정</div>" +
                "<div class='content-box'>" +
                "    <div class='step-title'>STEP 01. 선별장 반입</div>" +
                "    <div class='step-desc'>수거된 마대는 선별시설로 운반되어 철저한 검수 과정을 거칩니다.</div>" +
                "    <div class='step-title'>STEP 02. 물리적 선별</div>" +
                "    <div class='step-desc'>자석 및 기계적 파쇄를 통해 재활용 가능한 건설폐재류를 골라냅니다.</div>" +
                "    <div class='step-title'>STEP 03. 자원화 및 최종 매립</div>" +
                "    <div class='step-desc'>선별된 폐재류는 <span class='highlight-point'>순환골재</span>로 재탄생하며, 나머지는 매립지에 위생적으로 매립됩니다.</div>" +
                "</div>";
    }
    public String getBulkyWasteGuideHtml() {
        // 웹 서버 상대 경로 설정 (src/main/resources/static/images/...)
        String imgPathUrl = "/images/large-scale waste/large-scale waste";

        return "<style>" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; line-height: 1.6; color: #ffffff; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" +
                ".highlight-place { color: #00fff0; font-weight: bold; }" +
                ".step-title { font-weight: bold; color: #ffffff; margin-bottom: 2px; }" +
                ".step-desc { margin-bottom: 15px; color: #e0e0e0; }" +
                ".img-group { margin-top: 20px; text-align: center; display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }" +
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; width: 110px; height: 110px; object-fit: cover; }" +
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" +
                "</style>" +
                
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🛋️ 대형폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "종량제 봉투에 담기 어렵거나 생활 가구/가전 등 <span class='highlight-point'>개별 수거</span>가 필요한 물품입니다.<br><br>" +
                "• <b>가구류:</b> 장롱, 침대, 책상, 소파, 식탁 등<br>" +
                "• <b>가전류:</b> 냉장고, 세탁기, 에어컨, TV, 전자레인지 등<br>" +
                "• <b>기타:</b> 자전거, 유모차, 피아노, 거울, 전기장판 등" +
                "<div class='img-group'>" +
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>" +
                "<img class='guide-img' src='" + imgPathUrl + "4.png'>" +
                "</div></div>" +

                "<div class='section-title'>🚛 올바른 배출 방법</div>" +
                "<div class='content-box'>" +
                "<b>1. 지자체 신고 배출 <span class='highlight-point'>(유료)</span></b><br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight-place'>주민센터 방문</span> 또는 <span class='highlight-place'>온라인/앱</span>을 통해 배출 예약<br>" +
                "&nbsp;&nbsp;&nbsp;- 수수료 결제 후 <span class='highlight-point'>납부필증(스티커)</span>을 부착하여 지정 장소 배출<br><br>" +
                "<b>2. 폐가전 제품 배출 <span class='highlight-point'>(무상 수거)</span></b><br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight-place'>폐가전 무상배출예약시스템</span>(☎ 1599-0903)을 통해 방문 수거 요청<br>" +
                "&nbsp;&nbsp;&nbsp;- 원형이 훼손되지 않은 가전 제품은 무상으로 방문하여 수거해 갑니다.<br><br>" +
                "<b>3. 재사용 물품 기부</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 상태가 양호한 물품은 지역 <span class='highlight-place'>재활용 센터</span>에 기증하여 자원 순환에 동참하세요." +
                "</div>" +

                "<div class='section-title'>⚠️ 주의사항</div>" +
                "<div class='content-box'>" +
                "• <b>신고 필수:</b> 신고 없이 무단 배출 시 과태료가 부과될 수 있습니다.<br>" +
                "• <b>배출 장소:</b> 차량 진입이 가능한 지정 장소 또는 내 집 앞(지자체 기준 준수)에 배출하세요.<br>" +
                "• <b>세트 품목:</b> 침대 프레임과 매트리스처럼 분리되는 품목은 각각 신고해야 합니다." +
                "<p class='sub-text-red'>※ 스티커가 비에 젖거나 떨어지지 않도록 투명 테이프로 견고하게 고정해 주세요.</p>" +
                "</div>" +

                "<div class='section-title'>♻️ 처리 과정</div>" +
                "<div class='content-box'>" +
                "    <div class='step-title'>STEP 01. 수거 및 중간 집하</div>" +
                "    <div class='step-desc'>신고된 대형 폐기물은 지자체 전용 차량이 순회하며 수거하여 집하장으로 운반합니다.</div>" +
                "    <div class='step-title'>STEP 02. 파쇄 및 재질 선별</div>" +
                "    <div class='step-desc'>대형 분쇄기로 파쇄 후 <span class='highlight-place'>고철, 합성수지, 폐목재</span> 등을 종류별로 정밀하게 골라냅니다.</div>" +
                "    <div class='step-title'>STEP 03. 에너지화 및 자원 순환</div>" +
                "    <div class='step-desc'>선별된 자원은 원료로 재사용되며, 나머지 잔재물은 안전하게 <span class='highlight-point'>소각 또는 매립</span>됩니다.</div>" +
                "</div>";
    }
}