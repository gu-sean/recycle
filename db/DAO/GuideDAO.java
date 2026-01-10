package db.DAO;

import java.sql.Connection;
import db.RecycleDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class GuideDAO {
    private static final String CATEGORIES_TABLE = "CATEGORIES";
    private static final String ITEMS_TABLE = "ITEMS"; 

    private static final Map<String, String[]> CATEGORY_RESOURCES = new LinkedHashMap<>();
    static {
        CATEGORY_RESOURCES.put("C01", new String[]{ "1", "4", "images/paper/mark.png", "images/paper/paper1.png", "images/paper/paper2.png", "images/paper/paper3.png", "images/paper/paper4.png" });
        CATEGORY_RESOURCES.put("C02", new String[]{ "6", "3", "images/Vinyl/mark1.png", "images/Vinyl/mark2.png", "images/Vinyl/mark3.png", "images/Vinyl/mark4.png", "images/Vinyl/mark5.png", "images/Vinyl/mark6.png", "images/Vinyl/vinyl1.png", "images/Vinyl/vinyl2.png", "images/Vinyl/vinyl3.png" });
        CATEGORY_RESOURCES.put("C03", new String[]{ "1", "4", "images/glass_bottle/mark1.png", "images/glass_bottle/glass1.png", "images/glass_bottle/glass2.png", "images/glass_bottle/glass3.png", "images/glass_bottle/glass4.png" });
        CATEGORY_RESOURCES.put("C04", new String[]{ "2", "2", "images/paper_pack/mark1.png", "images/paper_pack/mark2.png", "images/paper_pack/pack1.png", "images/paper_pack/pack2.png" });
        CATEGORY_RESOURCES.put("C05", new String[]{ "2", "3", "images/Can/mark1.png", "images/Can/mark2.png", "images/Can/can1.png", "images/Can/can2.png", "images/Can/can3.png" });
        CATEGORY_RESOURCES.put("C06", new String[]{ "0", "3", "images/Strofoam/styrofoam1.png", "images/Strofoam/styrofoam2.png", "images/Strofoam/styrofoam3.png" });
        CATEGORY_RESOURCES.put("C07", new String[]{ "7", "6", "images/plastic/mark1.png", "images/plastic/mark2.png", "images/plastic/mark3.png", "images/plastic/mark4.png", "images/plastic/mark5.png", "images/plastic/mark6.png", "images/plastic/mark7.png", "images/plastic/plastic1.png", "images/plastic/plastic2.png", "images/plastic/plastic3.png", "images/plastic/plastic4.png", "images/plastic/plastic5.png", "images/plastic/plastic6.png" });
        CATEGORY_RESOURCES.put("C08", new String[]{ "0", "3", "images/Clothing/clothing1.png", "images/Clothing/clothing2.png", "images/Clothing/clothing3.png" });
    }
    
    
    private static final Map<String, String> CATEGORY_ID_MAP = new LinkedHashMap<>();
    static {
        CATEGORY_ID_MAP.put("종이", "C01");
        CATEGORY_ID_MAP.put("비닐", "C02");
        CATEGORY_ID_MAP.put("유리병", "C03");
        CATEGORY_ID_MAP.put("종이팩", "C04");
        CATEGORY_ID_MAP.put("캔ㆍ고철", "C05");
        CATEGORY_ID_MAP.put("스티로폼", "C06");
        CATEGORY_ID_MAP.put("플라스틱", "C07");
        CATEGORY_ID_MAP.put("기타", "C08");
    }



    private static final Map<String, String> COMMON_GUIDES = new LinkedHashMap<>();
    static {
        COMMON_GUIDES.put("C01", 
            "<div class='common-box theme-paper' style='margin-top: 40px;'>" +
            "   <b>📄 종이류 올바른 배출 방법</b><br>" +
            "   • <b>비우고 제거하기:</b> 테이프, 택배송장, 철핀 등 이물질 제거<br>" +
            "   • <b>펴고 쌓기:</b> 상자는 펼쳐서, 신문은 쌓아서 묶기<br>" +
            "   • <b>코팅지 주의:</b> 비닐 코팅된 종이는 <span class='point-text'>종량제 봉투</span>로 배출" +
            "</div>");
        
        COMMON_GUIDES.put("C04", 
            "<div class='common-box theme-pack' style='margin-top: 40px;'>" +
            "   <b>🧃 종이팩(살균팩/멸균팩) 배출 핵심 가이드</b><br>" +
            "   • <b>일반 종이와 혼합 금지:</b> 고급 펄프 재질이라 일반 종이와 섞이면 재활용이 안 됩니다.<br>" +
            "   • <b>4단계 원칙:</b> 비우기 → 헹구기 → 펼치기 → 말리기<br>" +
            "   • <b>전용함 배출:</b> 종이팩 전용 수거함이나 주민센터 '종이팩-화장지 교환'을 이용하세요." +
            "</div>");
        
        COMMON_GUIDES.put("C08", 
            "<div class='common-box theme-clothing' style='margin-top: 40px;'>" +
            "   <b>⚠️ 기타 품목 배출 핵심 가이드 (카테고리별 확인)</b><br>" +
            "   1. <b>불연성 마대:</b> 도자기, 사기, 깨진 유리는 전용 마대에 넣어 배출하세요.<br>" +
            "   2. <b>폐가전:</b> 5개 이상은 <span class='point-text'>무상방문수거(1599-0903)</span>, 소형 1개는 전용함에 배출하세요.<br>" +
            "   3. <b>헌옷수거함:</b> 의류, 신발, 가방(상태 양호 시)은 이물질 제거 후 배출하세요.<br>" +
            "   4. <b>음식물:</b> 동물이 먹을 수 있는 것만 음식물로, 딱딱한 뼈나 껍질은 종량제로 배출하세요." +
            "</div>");
        
        COMMON_GUIDES.put("C07", 
            "<div class='common-box theme-plastic' style='margin-top: 40px;'>" +
            "   <b>♻️ 플라스틱 배출 4대 원칙 (비/행/섞/라)</b><br>" +
            "   • <b>비우기:</b> 용기 안의 내용물을 완전히 비우세요.<br>" +
            "   • <b>헹구기:</b> 이물질과 기름기를 물로 깨끗이 닦으세요.<br>" +
            "   • <b>섞지않기:</b> 고무, 실리콘, 금속 스프링은 반드시 분리하세요.<br>" +
            "   • <b>라벨제거:</b> 비닐 라벨과 스티커는 떼어서 <span class='point-text'>비닐류</span>로 따로 배출하세요." +
            "</div>");
        
        COMMON_GUIDES.put("C06", 
            "<div class='common-box theme-styrofoam' style='margin-top: 40px;'>" +
            "   <b>⚪ 스티로폼 배출 핵심 가이드</b><br>" +
            "   • <b>색상 확인:</b> 순수 <span class='point-text'>흰색 스티로폼</span>만 재활용이 가능합니다.<br>" +
            "   • <b>이물질 제거:</b> 테이프, 택배 송장, 아이스팩을 완전히 제거하세요.<br>" +
            "   • <b>오염 주의:</b> 음식물이 묻은 경우 깨끗이 씻어야 하며, 지워지지 않으면 종량제로 배출하세요.<br>" +
            "   • <b>날림 방지:</b> 바람에 날리지 않도록 묶거나 투명 비닐에 담아 배출하세요." +
            "</div>");
        
        COMMON_GUIDES.put("C05", 
            "<div class='common-box theme-can' style='margin-top: 40px;'>" +
            "   <b>🥫 캔/고철류 배출 4대 안전 수칙</b><br>" +
            "   • <b>비우기와 헹구기:</b> 내용물을 비우고 이물질은 물로 헹궈 배출하세요.<br>" +
            "   • <b>가스 제거:</b> 살충제, 부탄가스는 통풍이 잘 되는 곳에서 반드시 <span class='point-text'>잔류 가스를 제거</span>하세요.<br>" +
            "   • <b>이물질 분리:</b> 플라스틱 뚜껑, 손잡이, 고무 패킹 등 타 재질은 최대한 제거하세요.<br>" +
            "   • <b>안전 배출:</b> 못이나 톱날 등 날카로운 물건은 캔에 넣거나 종이에 싸서 안전하게 배출하세요." +
            "</div>");

        COMMON_GUIDES.put("C02", 
            "<div class='common-box theme-vinyl' style='margin-top: 40px;'>" +
            "   <b>♻️ 비닐류 배출 핵심 가이드 (비/행/모)</b><br>" +
            "   • <b>비우기:</b> 비닐 내부의 이물질을 완전히 제거하세요.<br>" +
            "   • <b>헹구기:</b> 음식물이 묻은 비닐은 물로 헹구어 <span class='point-text'>말린 뒤</span> 배출하세요.<br>" +
            "   • <b>모으기:</b> 흩날리지 않도록 큰 비닐 봉투에 차곡차곡 모아 묶어서 배출하세요.<br>" +
            "   • <b>주의:</b> 스티커, 테이프 등 타 재질은 반드시 제거해야 합니다." +
            "</div>");
        
        COMMON_GUIDES.put("C03", 
            "<div class='common-box theme-glass' style='margin-top: 40px;'>" +
            "   <b>🍶 유리병 배출 핵심 가이드</b><br>" +
            "   • <b>보증금 환급:</b> <span class='point-text'>소주, 맥주병</span>은 마트나 편의점에 반납하여 보증금을 돌려받으세요.<br>" +
            "   • <b>이물질 제거:</b> 병 안의 담배꽁초나 이물질은 깨끗이 비우고 헹구어 배출하세요.<br>" +
            "   • <b>뚜껑 분리:</b> 금속이나 플라스틱 뚜껑은 반드시 제거하여 해당 재질로 따로 배출하세요.<br>" +
            "   • <b>주의:</b> 깨진 유리, 거울, 도자기는 유리병이 아니므로 <span class='point-text'>불연성 마대</span>에 버려야 합니다." +
            "</div>");
    }

    private static final Map<String, Integer> CATEGORY_REWARDS = new LinkedHashMap<>();
    static {
        CATEGORY_REWARDS.put("종이", 15);
        CATEGORY_REWARDS.put("비닐", 10);
        CATEGORY_REWARDS.put("유리병", 25);
        CATEGORY_REWARDS.put("종이팩", 20);
        CATEGORY_REWARDS.put("캔ㆍ고철", 40);
        CATEGORY_REWARDS.put("스티로폼", 10);
        CATEGORY_REWARDS.put("플라스틱", 10);
        CATEGORY_REWARDS.put("기타", 5);
    }

    private static final String[][] ITEMS_FULL_DATA = {
    		// [종이류 - C01] (I001 ~ I022) 보강 버전
    	    {"I001", "가격표", "C01", 
    	        "<span class='action-tag'>접착제 제거</span> 테이프나 스티커 성분을 칼이나 가위로 완전히 제거한 후 종이류로 배출하세요.<br/>" +
    	        "하지만 <span class='point-text'>전체가 접착제인 라벨지나 비닐 코팅된 의류 택</span>은 재활용 공정을 방해하므로 종량제 봉투로 버려야 합니다.", "tag-paper"},

    	    {"I002", "골판지 상자", "C01", 
    	        "<span class='action-tag'>박스 해체</span> 택배 송장 스티커와 투명 테이프를 완전히 떼어낸 뒤, 박스를 칼로 잘라 납작하게 접어서 배출하세요.<br/>" +
    	        "박스 내부에 <span class='point-text'>스티로폼이나 비닐 완충재</span>가 남지 않도록 주의해야 하며, 비에 젖은 상자는 재활용이 어렵습니다.", "tag-paper"},

    	    {"I003", "광고전단지", "C01", 
    	        "<span class='action-tag'>코팅 재질 확인</span> 손으로 찢었을 때 매끄럽게 찢기지 않고 비닐이 늘어나는 전단지는 재활용이 불가능한 '혼합 재질'입니다.<br/>" +
    	        "순수한 <span class='point-text'>종이 재질 전단지만</span> 모아서 배출하고, 비닐 코팅된 전단지는 반드시 일반 쓰레기로 분류하세요.", "tag-paper"},

    	    {"I004", "랩의 심", "C01", 
    	        "<span class='action-tag'>부속품 분리</span> 주방용 랩이나 은박지를 다 쓰고 남은 두꺼운 종이 심은 압착하여 부피를 줄인 뒤 배출하세요.<br/>" +
    	        "심 양옆에 붙은 <span class='point-text'>플라스틱 마개나 금속 절단용 톱날</span>은 반드시 도구를 이용해 제거한 후 재질별로 분리해야 합니다.", "tag-paper"},

    	    {"I005", "명함", "C01", 
    	        "<span class='action-tag'>선별 배출</span> 일반적인 종이 명함은 재활용이 가능하지만, 물에 젖지 않는 플라스틱 합성지 명함은 불가합니다.<br/>" +
    	        "<span class='point-text'>금박·은박 가공이 들어간 명함</span>이나 사진 인화지 형태의 명함은 일반 쓰레기로 버리는 것이 원칙입니다.", "tag-paper"},

    	    {"I006", "백과사전", "C01", 
    	        "<span class='action-tag'>표지 분리</span> 두껍고 딱딱한 양장본 표지는 대부분 비닐 코팅과 접착제가 강하게 결합되어 재활용이 안 됩니다.<br/>" +
    	        "칼을 사용하여 <span class='point-text'>딱딱한 표지를 완전히 잘라내어 일반 쓰레기</span>로 버리고, 내부의 순수 종이 속지만 묶어서 배출하세요.", "tag-paper"},

    	    {"I007", "사전", "C01", 
    	        "<span class='action-tag'>가죽 커버 제거</span> 가죽, 인조 가죽, 또는 플라스틱 소재의 사전 커버는 종이 재활용 시스템에 혼란을 줍니다.<br/>" +
    	        "커버를 완전히 벗겨내고 <span class='point-text'>안쪽의 종이 뭉치만</span> 노끈으로 묶어 종이류 수거함에 내놓으시기 바랍니다.", "tag-paper"},

    	    {"I008", "쌀포대", "C01", 
    	        "<span class='action-tag'>내부 확인</span> 겉면은 종이지만 내부에 습기 방지용 비닐이 코팅되거나 붙어 있는 쌀포대는 재활용 가치가 낮습니다.<br/>" +
    	        "가급적 <span class='point-text'>종량제 봉투(일반 쓰레기)</span>로 배출하시고, 순수 종이 재질인 경우에만 오염 물질을 털어낸 후 배출하세요.", "tag-paper"},

    	    {"I009", "서류봉투", "C01", 
    	        "<span class='action-tag'>이물질 추출</span> 봉투 주소창에 붙은 투명 비닐 필름이나 속이 보이는 비닐 창은 반드시 손으로 뜯어 비닐류로 따로 버려야 합니다.<br/>" +
    	        "봉투 입구에 붙은 <span class='point-text'>풀 성분이나 양면테이프</span> 부분도 가급적 가위로 잘라내고 배출하는 것이 좋습니다.", "tag-paper"},

    	    {"I010", "수첩", "C01", 
    	        "<span class='action-tag'>복합재질 분해</span> 스프링 수첩은 그대로 버리면 재활용이 전혀 되지 않습니다. <br/>" +
    	        "1. 금속/플라스틱 스프링 제거 2. 비닐 표지 제거 3. <span class='point-text'>내부의 종이 속지만</span> 따로 모으기 등 3단계를 꼭 지켜주세요.", "tag-paper"},

    	    {"I011", "신문지", "C01", 
    	        "<span class='action-tag'>오염 예방</span> 신문지는 습기에 매우 취약하므로 비 오는 날 배출을 피하고 물기에 젖지 않게 관리해야 합니다.<br/>" +
    	        "한 장씩 반듯하게 펴서 쌓은 후, 바람에 흩날리지 않도록 <span class='point-text'>노끈으로 십자 형태로 묶어서</span> 배출하는 것이 권장됩니다.", "tag-paper"},

    	    {"I012", "잡지", "C01", 
    	        "<span class='action-tag'>비닐 제거</span> 잡지의 표지는 대부분 화려한 광고를 위해 비닐 코팅이 되어 있어 재활용이 되지 않습니다.<br/>" +
    	        "코팅된 표지와 <span class='point-text'>내부의 광고 전단지, 부록(샘플 등)</span>을 모두 제거한 후 순수한 내지만 종이류로 배출하세요.", "tag-paper"},

    	    {"I013", "전단지", "C01", 
    	        "<span class='action-tag'>코팅 여부</span> 겉면이 번쩍거리는 코팅 종이는 종이 재생 과정에서 녹지 않아 치명적인 불량의 원인이 됩니다.<br/>" +
    	        "찢었을 때 단면에서 <span class='point-text'>비닐 껍질이 보인다면 반드시 종량제 봉투</span>에 버려주시고, 일반 종이 전단지만 배출하세요.", "tag-paper"},

    	    {"I014", "전화번호부", "C01", 
    	        "<span class='action-tag'>제본 부위 제거</span> 두꺼운 책자는 페이지를 고정하는 강력한 접착제(본드)가 세로면에 발라져 있습니다.<br/>" +
    	        "이 <span class='point-text'>접착제 부위를 칼로 잘라내고</span> 종이 부분만 배출하면 재활용 품질이 훨씬 높아집니다.", "tag-paper"},

    	    {"I015", "종이상자", "C01", 
    	        "<span class='action-tag'>위험물 제거</span> 과자나 소형 가전 박스를 고정할 때 사용된 스테이플러 철핀이나 구리핀은 선별 작업자에게 위험할 수 있습니다.<br/>" +
    	        "핀을 완전히 제거한 후 배출하세요. <span class='point-text'>은박지나 비닐이 부착된 상자</span>는 일반 쓰레기로 분류됩니다.", "tag-paper"},

    	    {"I016", "종이심", "C01", 
    	        "<span class='action-tag'>상태 확인</span> 휴지심이나 키친타월 심은 이물질이 묻지 않은 상태라면 그대로 종량제 봉투가 아닌 종이류로 배출 가능합니다.<br/>" +
    	        "다만, 화장실에서 사용 중 <span class='point-text'>오염되거나 젖은 종이심</span>은 재활용이 되지 않으므로 일반 쓰레기로 버리세요.", "tag-paper"},

    	    {"I017", "종이조각(파쇄지)", "C01", 
    	        "<span class='action-tag'>선별 불가 항목</span> 문서 세단기로 파쇄된 종이 조각은 크기가 너무 작아 재활용 선별장의 자동 선별 기계에서 걸러지지 못합니다.<br/>" +
    	        "이들은 바닥으로 떨어져 쓰레기가 되므로, <span class='point-text'>반드시 종량제 봉투(일반 쓰레기)</span>로 배출해야 합니다.", "tag-paper"},

    	    {"I018", "책", "C01", 
    	        "<span class='action-tag'>표지/부록 분리</span> 책의 딱딱한 겉표지는 제거하여 일반 쓰레기로 버리고, 내부의 종이 부분만 끈으로 묶어서 배출하세요.<br/>" +
    	        "<span class='point-text'>CD, DVD 부록이나 비닐 커버</span>가 포함된 경우 이를 반드시 분리한 후 배출하는 것이 핵심입니다.", "tag-paper"},

    	    {"I019", "치킨박스", "C01", 
    	        "<span class='action-tag'>기름기 차단</span> 치킨 상자 바닥에 배어든 기름과 양념은 종이 재생 공정을 방해하는 주범입니다.<br/>" +
    	        "<span class='point-text'>오염된 바닥 부분은 잘라내어 일반 쓰레기</span>로 버리고, 오염되지 않은 깨끗한 윗부분만 종이류로 분리 배출하세요.", "tag-paper"},

    	    {"I020", "탁상달력", "C01", 
    	        "<span class='action-tag'>전체 분해</span> 달력을 지탱하는 스프링(금속)과 받침대(두꺼운 종이/비닐)를 모두 분해해야 합니다.<br/>" +
    	        "스프링은 고철로, 받침대는 코팅 확인 후 재질별로, <span class='point-text'>날짜가 적힌 종이만</span> 종이류로 분리하여 배출하세요.", "tag-paper"},

    	    {"I021", "포스터/포장지", "C01", 
    	        "<span class='action-tag'>재질 선별</span> 선물 포장지 중 금박, 은박, 홀로그램 가공이 된 제품은 재활용이 되지 않는 폐기물입니다.<br/>" +
    	        "코팅되지 않은 <span class='point-text'>순수 종이 포장지</span>만 모아서 배출하시고, 반짝이는 소재는 종량제 봉투에 버리세요.", "tag-paper"},

    	    {"I022", "피자박스", "C01", 
    	        "<span class='action-tag'>세척 불가 오염</span> 피자에서 흘러나온 기름과 소스가 묻은 종이는 재활용 원료를 오염시킵니다.<br/>" +
    	        "오염이 심한 <span class='point-text'>바닥면과 고정용 플라스틱 핀</span>은 일반 쓰레기로 버리고, 깨끗한 뚜껑 부위만 종이류로 배출하세요.", "tag-paper"},

    	     // [비닐류 - C02] (I023 ~ I032) 보강 버전
    	        {"I023", "과자/라면 봉지", "C02", 
    	            "<span class='action-tag'>펼쳐서 배출</span> 봉지 내부의 과자 부스러기나 스프 가루를 완전히 털어내고 배출하세요.<br/>" +
    	            "과거에는 딱지로 접어 버리기도 했으나, <span class='point-text'>딱지 형태는 선별장의 공기 선별기가 무거워 들어 올리지 못합니다.</span><br/>" +
    	            "가급적 가위로 잘라 펼치거나 납작하게 접어서 비닐류로 배출해 주세요.", "tag-vinyl"},

    	        {"I024", "양파망", "C02", 
    	            "<span class='action-tag'>이물질 제거</span> 합성수지 재질의 양파망이나 과일망은 비닐류로 재활용이 가능한 자원입니다.<br/>" +
    	            "다만, 입구를 조이고 있는 <span class='point-text'>플라스틱 고정 장치나 종이 라벨</span>은 반드시 칼로 잘라 제거한 뒤 배출하세요.<br/>" +
    	            "망 안에 다른 쓰레기를 넣어 배출하는 행위는 절대 금지입니다.", "tag-vinyl"},

    	        {"I025", "일회용 비닐장갑", "C02", 
    	            "<span class='action-tag'>오염 확인</span> 요리 중 사용한 비닐장갑은 묻어있는 기름기와 음식물 찌꺼기가 재활용을 방해합니다.<br/>" +
    	            "깨끗한 상태라면 비닐류로 배출하되, <span class='point-text'>고기 기름이나 빨간 양념이 묻어 세척이 어려운 경우</span>는<br/>" +
    	            "재활용이 되지 않으므로 반드시 종량제 봉투(일반 쓰레기)에 버려주세요.", "tag-vinyl"},

    	        {"I026", "라면 스프 봉지", "C02", 
    	            "<span class='action-tag'>내용물 비우기</span> 스프 가루는 염분이 많아 비닐 재생 원료의 품질을 크게 떨어뜨립니다.<br/>" +
    	            "가루를 완전히 비우고 물로 내부를 가볍게 헹구어 배출하세요. <span class='point-text'>내부가 빨갛게 오염된 상태</span>가<br/>" +
    	            "지워지지 않는다면 비닐류가 아닌 일반 쓰레기로 처리하는 것이 올바른 배출법입니다.", "tag-vinyl"},

    	        {"I027", "필름류(빵 포장지)", "C02", 
    	            "<span class='action-tag'>부착물 제거</span> 투명한 빵 포장지나 과채류를 감싸는 얇은 필름도 모두 비닐류에 해당합니다.<br/>" +
    	            "포장지에 직접 붙어 있는 <span class='point-text'>가격표 스티커나 투명 테이프</span>는 재활용 공정에서 녹지 않으므로<br/>" +
    	            "해당 부위를 가위로 도려낸 후 순수 비닐 부분만 모아서 배출해 주세요.", "tag-vinyl"},

    	        {"I028", "택배용 비닐 봉투", "C02", 
    	            "<span class='action-tag'>운송장 제거</span> 의류나 소형 가전을 담아오는 택배 비닐(폴리백)은 비닐류로 재활용됩니다.<br/>" +
    	            "봉투에 붙은 <span class='point-text'>개인정보 운송장 스티커와 입구의 강력 테이프</span>는 반드시 제거해야 합니다.<br/>" +
    	            "스티커가 잘 떨어지지 않는다면 그 부분만 가위로 오려낸 후 배출하시기 바랍니다.", "tag-vinyl"},

    	        {"I029", "한약/즙 파우치", "C02", 
    	            "<span class='action-tag'>내부 세척</span> 한약이나 과일즙이 담겼던 파우치는 내용물이 조금이라도 남으면 곰팡이가 생겨 수거함을 오염시킵니다.<br/>" +
    	            "가위로 입구를 크게 잘라 <span class='point-text'>물로 내부를 깨끗이 헹군 뒤</span> 말려서 배출하세요.<br/>" +
    	            "알루미늄 성분이 섞인 복합질이라도 '비닐류' 표시가 있다면 재활용이 가능합니다.", "tag-vinyl"},

    	        {"I030", "뽁뽁이(에어캡)", "C02", 
    	            "<span class='action-tag'>이물질 관리</span> 에어캡은 LDPE 재질의 훌륭한 재활용 자원입니다. 바람을 일일이 터뜨릴 필요는 없으나 부피를 줄여주면 좋습니다.<br/>" +
    	            "다만, <span class='point-text'>택배 박스에서 묻어나온 종이 조각이나 테이프</span>가 붙어 있다면 반드시 제거해야 합니다.<br/>" +
    	            "이물질이 너무 많이 붙어 제거가 힘들다면 종량제 봉투로 배출하세요.", "tag-vinyl"},

    	        {"I031", "아이스팩 비닐", "C02", 
    	            "<span class='action-tag'>성분별 처리</span> 최근 사용되는 '물 100%' 아이스팩은 물을 따라 버린 후 비닐만 따로 비닐류로 배출할 수 있습니다.<br/>" +
    	            "하지만 <span class='point-text'>고분자화합물(젤 형태)이 담긴 아이스팩</span>은 내용물과 봉투 모두 재활용이 되지 않으므로<br/>" +
    	            "통째로 종량제 봉투에 담아 버리거나, 전용 수거함에 반납해야 합니다.", "tag-vinyl"},

    	        {"I032", "비닐 봉지(검정/투명)", "C02", 
    	            "<span class='action-tag'>오염 차단</span> 가정에서 흔히 사용하는 검정 비닐봉투나 투명 위생봉투도 모두 비닐류 수거함으로 배출합니다.<br/>" +
    	            "단, <span class='point-text'>음식물 쓰레기를 담았던 비닐이나 흙이 많이 묻은 비닐</span>은 세척이 어렵다면 일반 쓰레기로 버려주세요.<br/>" +
    	            "바람에 날리지 않도록 큰 비닐 봉투 안에 작은 비닐들을 모아 묶어서 배출하는 것이 좋습니다.", "tag-vinyl"},

    	     // [유리병 - C03] (I033 ~ I042) 보강 버전
    	        {"I033", "맥주/소주 빈병", "C03", 
    	            "<span class='action-tag'>보증금 환급</span> 빈용기 보증금 마크가 있는 소주, 맥주, 청량음료 병은 재사용되는 소중한 자원입니다.<br/>" +
    	            "내용물을 깨끗이 헹구고 담배꽁초 등 이물질을 넣지 마세요. <span class='point-text'>대형마트나 편의점에 반납</span>하면<br/>" +
    	            "규격에 따라 70원~150원 이상의 보증금을 돌려받을 수 있습니다.", "tag-glass"},

    	        {"I034", "화장품 유리병", "C03", 
    	            "<span class='action-tag'>부속품 분리</span> 내용물을 완전히 비우고 물로 내부를 헹궈 배출하세요.<br/>" +
    	            "유리 몸체 외에 <span class='point-text'>플라스틱 마개, 고무 펌프, 금속 스프링 노즐</span> 등 타 재질은 최대한 분리해야 합니다.<br/>" +
    	            "분리가 불가능한 복합 재질 펌프는 일반 쓰레기로 버리고 몸체만 유리로 배출하세요.", "tag-glass"},

    	        {"I035", "조미료/잼 유리병", "C03", 
    	            "<span class='action-tag'>세척 후 분리</span> 끈적한 설탕물이나 잼 성분이 남지 않도록 따뜻한 물로 깨끗이 세척하세요.<br/>" +
    	            "병 입구의 테두리나 <span class='point-text'>철제 뚜껑은 반드시 제거하여 캔류</span>로 따로 분리 배출해야 합니다.<br/>" +
    	            "병에 붙은 종이 라벨은 물에 불려 제거하면 재활용 품질이 더욱 높아집니다.", "tag-glass"},

    	        {"I036", "유리잔/드링크병", "C03", 
    	            "<span class='action-tag'>재질 확인</span> 자양강장제 등 작은 드링크병도 유리병으로 배출 가능합니다.<br/>" +
    	            "주의할 점은 <span class='point-text'>강화유리(락앤락 등), 크리스탈, 사기그릇, 도자기</span>는 일반 유리와 녹는점이 달라 재활용이 안 됩니다.<br/>" +
    	            "이들은 반드시 불연성 마대(타지 않는 쓰레기)에 담아 배출하세요.", "tag-glass"},

    	        {"I037", "식용유 유리병", "C03", 
    	            "<span class='action-tag'>기름기 완전 제거</span> 유리병 재활용 공정에서 기름기는 매우 치명적인 오염원입니다.<br/>" +
    	            "세제를 사용하여 내부 유분을 완벽히 씻어내어 배출하세요. <span class='point-text'>오염이 심해 세척이 어려운 경우</span>는<br/>" +
    	            "유리병으로 배출하지 말고 그대로 일반 쓰레기(종량제 봉투)로 버려야 합니다.", "tag-glass"},

    	        {"I038", "소스병(케첩/머스타드)", "C03", 
    	            "<span class='action-tag'>입구 마개 분리</span> 입구 부분의 플라스틱 캡이나 비닐 실링을 도구를 이용해 최대한 분리하세요.<br/>" +
    	            "내부 양념을 깨끗이 헹군 뒤 <span class='point-text'>투명/청색/갈색별로 구분된 수거함</span>이 있다면 색상에 맞춰 배출하세요.<br/>" +
    	            "색상별 선별은 유리병 재활용의 핵심 단계입니다.", "tag-glass"},

    	        {"I039", "와인병", "C03", 
    	            "<span class='action-tag'>이물질 제거</span> 와인병 입구를 감싸고 있는 알루미늄 캡 실과 코르크 마개를 완전히 제거하세요.<br/>" +
    	            "와인병은 색상에 따라 녹여져 다시 유리병 원료로 사용됩니다. <span class='point-text'>병 내부의 와인 찌꺼기</span>가<br/>" +
    	            "말라붙지 않도록 다 마신 즉시 헹구어 배출하는 것이 좋습니다.", "tag-glass"},

    	        {"I040", "참기름 유리병", "C03", 
    	            "<span class='action-tag'>열탕 세척 권장</span> 참기름 특유의 강한 냄새와 유분은 일반적인 헹굼으로는 제거되지 않습니다.<br/>" +
    	            "뜨거운 물과 베이킹소다 등을 활용해 <span class='point-text'>미끈거림이 없을 때까지</span> 씻어 유리 수거함에 배출하세요.<br/>" +
    	            "라벨이 비닐 재질이라면 떼어내어 비닐류로 따로 버리시기 바랍니다.", "tag-glass"},

    	        {"I041", "커피 유리병", "C03", 
    	            "<span class='action-tag'>실링지 제거</span> 유리병 몸체는 유리로, 뚜껑은 보통 플라스틱으로 재질에 맞게 분리합니다.<br/>" +
    	            "특히 병 입구에 붙어 있는 <span class='point-text'>종이 또는 알루미늄 실링지</span> 조각을 깔끔하게 떼어내야 합니다.<br/>" +
    	            "병 속의 커피 가루가 남지 않도록 물로 가볍게 흔들어 씻어주세요.", "tag-glass"},

    	        {"I042", "향수병", "C03", 
    	            "<span class='action-tag'>노즐 분리 주의</span> 남아있는 향수 원액은 비우고 유리로 배출하세요.<br/>" +
    	            "향수병 입구의 <span class='point-text'>금속 스프레이 노즐과 펌프</span>는 유리와 결합된 경우가 많아 분리가 어렵습니다.<br/>" +
    	            "도구로 분리가 가능하다면 분리하고, 불가능하다면 해당 부분만 깨뜨리지 않게 주의하며 배출하세요.", "tag-glass"},
    	        
    	     // [종이팩 - C04] (I043 ~ I052) 보강 버전
    	        {"I043", "우유팩(살균팩)", "C04", 
    	            "<span class='action-tag'>비우고·헹구고·펼치고</span> 다 마신 우유팩은 내용물을 비우고 물로 깨끗이 헹군 뒤 펼쳐서 건조하세요.<br/>" +
    	            "일반 종이와 섞이면 재활용이 안 되므로 <span class='point-text'>반드시 종이팩 전용 수거함</span>에 배출해야 합니다.<br/>" +
    	            "종이팩은 일반 폐지보다 높은 단가의 고급 화장지 원료로 재활용되는 소중한 자원입니다.", "tag-pack"},

    	        {"I044", "두유팩(멸균팩)", "C04", 
    	            "<span class='action-tag'>은박 코팅 구분</span> 내부가 은색 알루미늄으로 코팅된 멸균팩은 일반 살균팩(우유팩)과 재활용 공정이 다릅니다.<br/>" +
    	            "가급적 지자체별 <span class='point-text'>멸균팩 전용 수거함</span>에 배출하시고, 없다면 종이팩함에 구분하여 넣어주세요.<br/>" +
    	            "멸균팩은 페이퍼 타월이나 건축 자재 등으로 재탄생하며, 상온 보관용 음료팩이 주로 해당합니다.", "tag-pack"},

    	        {"I045", "일회용 종이컵", "C04", 
    	            "<span class='action-tag'>이물질 제거</span> 컵 내부에 커피나 음료 찌꺼기가 남지 않도록 가볍게 물로 헹구어 배출하세요.<br/>" +
    	            "종이컵은 내부의 폴리에틸렌(PE) 코팅 때문에 일반 종이와 함께 섞이면 재활용 공정에서 녹지 않아 불량의 원인이 됩니다.<br/>" +
    	            "<span class='point-text'>가급적 종이팩 전용 수거함</span>이나 전용 회수함에 모아서 배출해 주세요.", "tag-pack"},

    	        {"I046", "주스팩(테트라팩)", "C04", 
    	            "<span class='action-tag'>부속품 분리</span> 상단의 플라스틱 캡(뚜껑)과 입구 부분은 손이나 도구로 떼어내어 플라스틱으로 배출하세요.<br/>" +
    	            "본체는 멸균팩 공정으로 분류되므로 <span class='point-text'>완전히 펼쳐서 바닥까지 세척</span>한 뒤 납작하게 눌러 배출합니다.<br/>" +
    	            "빨대와 빨대 비닐은 각각 재질에 맞게 따로 분리해야 합니다.", "tag-pack"},

    	        {"I047", "액상커피 종이팩", "C04", 
    	            "<span class='action-tag'>절단 후 세척</span> 대용량 액상커피 팩은 대부분 내부가 코팅된 멸균팩 구조입니다.<br/>" +
    	            "가위로 상단을 완전히 잘라 내부의 <span class='point-text'>커피 잔여물과 향을 제거</span>한 뒤 햇볕에 말려 배출하세요.<br/>" +
    	            "세척하지 않은 팩은 수거 과정에서 부패하여 다른 자원까지 오염시킬 수 있습니다.", "tag-pack"},

    	        {"I048", "생수 종이팩", "C04", 
    	            "<span class='action-tag'>프리미엄 자원</span> 최근 유행하는 종이팩 생수는 플라스틱 사용을 줄이기 위한 멸균팩 제품입니다.<br/>" +
    	            "플라스틱 뚜껑은 따로 돌려서 분리 배출하고, 본체는 <span class='point-text'>멸균팩 수거함</span>으로 보냅니다.<br/>" +
    	            "이는 최고급 펄프 원료가 포함되어 있어 화장지 생산에 매우 중요한 자원이 됩니다.", "tag-pack"},

    	        {"I049", "우유 음료팩(가공유)", "C04", 
    	            "<span class='action-tag'>당분 제거 필수</span> 딸기, 초코, 바나나우유 등이 담겼던 팩은 당분 때문에 세척이 더 중요합니다.<br/>" +
    	            "끈적임이 남으면 벌레가 꼬이거나 곰팡이가 생겨 재활용 품질을 떨어뜨립니다.<br/>" +
    	            "<span class='point-text'>냉장 보관용 가공유 팩은 살균팩</span>으로 분류하여 일반 종이팩함에 배출하세요.", "tag-pack"},

    	        {"I050", "와인 종이팩", "C04", 
    	            "<span class='action-tag'>부피 축소</span> 대용량 와인이나 수입 음료에 쓰이는 큰 종이팩도 알루미늄 코팅이 된 멸균팩입니다.<br/>" +
    	            "부피가 크므로 반드시 상하단을 가위로 잘라 <span class='point-text'>완전히 평평하게 펼쳐서</span> 배출해 주세요.<br/>" +
    	            "입구의 플라스틱 나사산 부분도 최대한 제거하는 것이 권장됩니다.", "tag-pack"},

    	        {"I051", "생크림 종이팩", "C04", 
    	            "<span class='action-tag'>유지방 제거</span> 생크림이나 조리용 크림 팩은 유분(기름기)이 매우 강해 세제 세척이 필수입니다.<br/>" +
    	            "세제를 푼 따뜻한 물로 내부를 흔들어 씻어 <span class='point-text'>미끈거림을 제거한 후</span> 배출하세요.<br/>" +
    	            "기름기가 남은 종이팩은 재활용 과정에서 섬유 결합을 방해하여 폐기 처분됩니다.", "tag-pack"},

    	        {"I052", "소형 요구르트팩", "C04", 
    	            "<span class='action-tag'>마크 확인</span> 크기가 아주 작더라도 '종이팩' 마크가 있다면 재활용 가치가 높은 품목입니다.<br/>" +
    	            "작은 크기 때문에 잃어버리기 쉬우므로 <span class='point-text'>다른 종이팩 안에 끼워 넣거나</span> 따로 묶어서 배출하세요.<br/>" +
    	            "물론 내부 요구르트 잔여물은 깨끗이 헹궈내야 합니다.", "tag-pack"},

    	     // [캔/고철 - C05] (I053 ~ I074) 보강 버전
    	        {"I053", "스프레이/살충제", "C05", 
    	            "<span class='action-tag'>잔류 가스 방출</span> 가스 성분이 남은 채로 압착되면 화재나 폭발 사고의 원인이 됩니다.<br/>" +
    	            "노즐을 끝까지 눌러 내용물을 완전히 비우고, 통풍이 잘되는 실외에서 구멍을 뚫어 배출하세요.<br/>" +
    	            "상단의 <span class='point-text'>플라스틱 캡과 분사 노즐</span>은 재질에 맞게 따로 분리해야 합니다.", "tag-can"},

    	        {"I054", "부탄가스", "C05", 
    	            "<span class='action-tag'>폭발 사고 주의</span> 화기가 없는 탁 트인 실외에서 노즐을 바닥에 눌러 '칙' 소리가 나지 않을 때까지 비우세요.<br/>" +
    	            "가스 전용 펀치나 송곳을 이용해 <span class='point-text'>측면에 2~3개의 구멍</span>을 내어 잔여 가스를 완전히 뺀 뒤 캔류로 배출하세요.<br/>" +
    	            "내용물이 남은 가스통은 수거 차량 화재의 주범이 되므로 절대 그대로 버리지 마세요.", "tag-can"},

    	        {"I055", "애완동물 음식캔", "C05", 
    	            "<span class='action-tag'>악취 및 오염 제거</span> 습식 사료가 담겼던 캔은 내용물이 부패하기 쉬우므로 물로 깨끗이 헹구어 배출하세요.<br/>" +
    	            "캔 뚜껑이 완전히 분리되는 형태(원터치 캔)라면 <span class='point-text'>뚜껑을 캔 안쪽으로 밀어 넣어</span> 배출하세요.<br/>" +
    	            "날카로운 절단면에 수거 작업자가 다치지 않도록 주의가 필요합니다.", "tag-can"},

    	        {"I056", "공구류(망치/드라이버)", "C05", 
    	            "<span class='action-tag'>금속 비중 확인</span> 망치의 머리나 드라이버의 날처럼 순수 금속 부분은 고철로 재활용 가치가 높습니다.<br/>" +
    	            "다만 손잡이가 고무나 플라스틱 일체형이라 분리가 전혀 안 된다면 <span class='point-text'>불연성 마대</span>에 담아 버려야 합니다.<br/>" +
    	            "분리가 가능하다면 금속 부분만 모아 고철 수거함에 넣어주세요.", "tag-can"},

    	        {"I057", "국자(금속)", "C05", 
    	            "<span class='action-tag'>복합재질 분리</span> 스테인리스나 알루미늄 재질의 국자는 훌륭한 고철 자원입니다.<br/>" +
    	            "플라스틱이나 나무로 된 손잡이가 나사로 연결되어 있다면 <span class='point-text'>드라이버로 나사를 풀어</span> 각각 분리 배출하세요.<br/>" +
    	            "일체형이라 분리가 어렵다면 고철로 배출하되 가급적 금속 위주로 배출합니다.", "tag-can"},

    	        {"I058", "그릇(금속)", "C05", 
    	            "<span class='action-tag'>재질 구분</span> 스테인리스 밥그릇이나 냉면 그릇 등은 음식물을 씻어낸 후 고철로 배출합니다.<br/>" +
    	            "단, <span class='point-text'>사기그릇, 뚝배기, 놋그릇(유기)</span>은 일반적인 고철 공정에서 녹지 않으므로 절대로 고철함에 넣으면 안 됩니다.<br/>" +
    	            "이런 제품은 불연성 쓰레기 봉투(마대)를 이용해 배출하세요.", "tag-can"},

    	        {"I059", "나사/못", "C05", 
    	            "<span class='action-tag'>안전 봉합 배출</span> 작은 나사나 못은 낱개로 버리면 수거 차량의 타이어를 펑크 내거나 작업자를 다치게 합니다.<br/>" +
    	            "빈 캔 속에 나사들을 넣은 뒤 <span class='point-text'>캔 입구를 발로 밟아 구부려서</span> 쏟아지지 않게 배출하는 것이 정석입니다.<br/>" +
    	            "봉지에 담아 버리는 것은 수거 과정에서 봉지가 터질 위험이 있어 권장하지 않습니다.", "tag-can"},

    	        {"I060", "낫/톱", "C05", 
    	            "<span class='action-tag'>사고 예방 포장</span> 날카로운 금속 날 부위는 수거 시 매우 위험합니다.<br/>" +
    	            "두꺼운 골판지나 종이로 날을 여러 번 감싸고 <span class='point-text'>테이프로 단단히 고정한 뒤</span> '칼' 혹은 '날카로움'이라고 표시해 고철로 배출하세요.<br/>" +
    	            "나무 손잡이는 가급적 분리하여 일반 쓰레기로 버려주세요.", "tag-can"},

    	        {"I061", "도끼", "C05", 
    	            "<span class='action-tag'>대형 폐기물 검토</span> 무거운 금속 날은 고철로 재활용되지만 부피와 무게가 상당하므로 주의가 필요합니다.<br/>" +
    	            "나무 자루를 분리할 수 있다면 날만 고철로 배출하시고, 분리가 불가능하거나 크기가 매우 크다면<br/>" +
    	            "<span class='point-text'>대형 폐기물 스티커</span>를 부착하여 정해진 장소에 내놓아야 합니다.", "tag-can"},

    	        {"I062", "병뚜껑(금속)", "C05", 
    	            "<span class='action-tag'>모아서 배출</span> 맥주병이나 소주병의 작은 금속 뚜껑들은 너무 작아 선별 기계에서 유실되기 쉽습니다.<br/>" +
    	            "빈 캔(커피캔 등) 안에 이들 뚜껑을 가득 채워 <span class='point-text'>입구를 압착하여 배출</span>하면 재활용률을 획기적으로 높일 수 있습니다.<br/>" +
    	            "병뚜껑 안쪽의 고무 패킹은 무시하고 금속류로 배출해도 무방합니다.", "tag-can"},

    	        {"I063", "분유 깡통", "C05", 
    	            "<span class='action-tag'>부속품 철저 분리</span> 분유 통은 크기가 커서 고철 자원으로 가치가 큽니다.<br/>" +
    	            "상단의 플라스틱 뚜껑과 통 안에 든 <span class='point-text'>플라스틱 스푼은 반드시 분리</span>하여 플라스틱류로 배출하세요.<br/>" +
    	            "통 속에 남은 가루가 없도록 털어내거나 가볍게 헹구어 배출하는 것이 좋습니다.", "tag-can"},

    	        {"I064", "쓰레기받기(금속)", "C05", 
    	            "<span class='action-tag'>고무날 제거</span> 전체가 철제인 쓰레받기는 고철 수거함에 배출하면 됩니다.<br/>" +
    	            "바닥과 맞닿는 부분에 부착된 <span class='point-text'>고무날이나 손잡이의 플라스틱 마개</span>는 도구를 이용해 제거해 주세요.<br/>" +
    	            "코팅된 제품이라도 주재질이 철이라면 고철로 재활용이 가능합니다.", "tag-can"},

    	        {"I065", "아령/역기", "C05", 
    	            "<span class='action-tag'>재질 코팅 주의</span> 겉면이 우레탄이나 두꺼운 고무로 감싸진 아령은 금속 분리가 어려워 재활용이 거부될 수 있습니다.<br/>" +
    	            "순수 쇠로 된 아령만 고철로 배출하시고, <span class='point-text'>고무 코팅 제품은 대형 폐기물</span>이나 일반 쓰레기로 처리하세요.<br/>" +
    	            "모래나 물이 들어 있는 플라스틱 아령은 고철이 아니니 주의해야 합니다.", "tag-can"},

    	        {"I066", "압력솥/냄비", "C05", 
    	            "<span class='action-tag'>패킹 및 손잡이 분리</span> 뚜껑에 달린 고무 패킹과 플라스틱 손잡이는 고철 재활용을 방해하는 불순물입니다.<br/>" +
    	            "최대한 드라이버로 나사를 풀어 제거한 뒤 <span class='point-text'>금속 본체만 고철로 배출</span>하세요.<br/>" +
    	            "음식물이 타서 눌러붙은 자국은 철수세미로 최대한 제거해야 고품질 고철로 재탄생합니다.", "tag-can"},

    	        {"I067", "옷걸이(철사)", "C05", 
    	            "<span class='action-tag'>이탈 방지 결속</span> 세탁소용 흰색 코팅 옷걸이는 낱개로 버리면 수거 시 부피만 차지하고 다른 자원과 엉키기 쉽습니다.<br/>" +
    	            "여러 개를 모아 <span class='point-text'>노끈이나 테이프로 단단히 묶어서</span> 한 덩어리로 배출해 주세요.<br/>" +
    	            "전체가 플라스틱인 옷걸이는 고철이 아닌 플라스틱류로 배출해야 합니다.", "tag-can"},

    	        {"I068", "의류건조대", "C05", 
    	            "<span class='action-tag'>부품 최소화</span> 살대가 금속인 건조대는 고철로 배출합니다. 부피가 크므로 최대한 접어서 내놓으세요.<br/>" +
    	            "살대 끝부분이나 연결 부위의 <span class='point-text'>플라스틱 부속품</span>들은 가급적 손이나 펜치로 떼어내는 것이 좋습니다.<br/>" +
    	            "분리가 너무 힘든 대형 제품은 지자체 대형 폐기물로 신고 배출을 권장합니다.", "tag-can"},

    	        {"I069", "재떨이(금속)", "C05", 
    	            "<span class='action-tag'>완전 세척</span> 담배꽁초와 재는 고철 용광로에서 연기를 발생시키고 품질을 떨어뜨립니다.<br/>" +
    	            "꽁초를 비운 뒤 물과 세제로 내부를 깨끗이 닦아 배출하세요.<br/>" +
    	            "<span class='point-text'>유리 재질이나 도자기 재질 재떨이</span>는 고철이 아니므로 일반 쓰레기로 버려야 합니다.", "tag-can"},

    	        {"I070", "주전자", "C05", 
    	            "<span class='action-tag'>손잡이 확인</span> 스테인리스나 알루미늄 주전자는 고철로 배출하세요.<br/>" +
    	            "뚜껑의 손잡이나 주전자 핸들의 <span class='point-text'>플라스틱 부분</span>은 돌려서 뺄 수 있다면 분리하는 것이 정석입니다.<br/>" +
    	            "전기 주전자(무선 포트)는 가전제품이므로 고철이 아닌 소형 가전 수거함으로 가야 합니다.", "tag-can"},

    	        {"I071", "철사/노끈(금속)", "C05", 
    	            "<span class='action-tag'>부피 형성</span> 가느다란 철사는 선별장 바닥으로 떨어져 유실되기 쉽습니다.<br/>" +
    	            "야구공 크기 정도로 <span class='point-text'>동그랗게 뭉쳐서 부피를 만든 뒤</span> 배출해야 기계식 선별이 가능해집니다.<br/>" +
    	            "비닐 노끈이나 마끈은 고철이 아니므로 일반 쓰레기로 버려주세요.", "tag-can"},

    	        {"I072", "철판/불판", "C05", 
    	            "<span class='action-tag'>유지방 제거</span> 고기 불판에 묻은 기름기와 탄 찌꺼기는 강력한 세제로 제거 후 배출해야 합니다.<br/>" +
    	            "이물질이 많이 남은 불판은 고철로 재활용되지 못하고 폐기됩니다.<br/>" +
    	            "<span class='point-text'>불소 코팅된 불판</span>이라도 본체가 금속이므로 고철로 배출 가능합니다.", "tag-can"},

    	        {"I073", "컵(텀블러/금속)", "C05", 
    	            "<span class='action-tag'>내부 확인</span> 스테인리스 진공 텀블러는 내부 실링지와 빨대, 플라스틱 뚜껑을 모두 제거한 후 몸체만 배출하세요.<br/>" +
    	            "가죽이나 고무 홀더가 씌워진 제품은 <span class='point-text'>홀더를 벗겨내야 합니다.</span><br/>" +
    	            "도자기 내벽이 있는 텀블러는 복합 재질이므로 일반 쓰레기로 버리는 것이 맞습니다.", "tag-can"},

    	        {"I074", "후라이팬", "C05", 
    	            "<span class='action-tag'>코팅 무관 배출</span> 테플론이나 세라믹 코팅이 되어 있어도 90% 이상이 금속이므로 고철로 배출합니다.<br/>" +
    	            "손잡이가 나사로 연결된 경우 <span class='point-text'>나사를 풀어 플라스틱 손잡이를 제거</span>하면 가장 완벽한 배출법입니다.<br/>" +
    	            "기름기는 키친타월로 닦아낸 뒤 세척하여 배출해 주세요.", "tag-can"},

    	     // [스티로폼 - C06] (I075 ~ I084) 보강 버전
    	        {"I075", "스티로폼 완충재", "C06", 
    	            "<span class='action-tag'>이물질 철저 제거</span> 가전제품 상자 안의 큰 완충재는 부피가 크지만 훌륭한 자원입니다.<br/>" +
    	            "박스에서 떼어낼 때 붙어 있던 <span class='point-text'>박스 조각, 투명 테이프, 코팅 스티커</span>를 완전히 제거해야 합니다.<br/>" +
    	            "부피가 너무 크다면 쪼개서 비닐 봉투에 담거나 끈으로 묶어서 배출해 주세요.", "tag-styrofoam"},

    	        {"I076", "과일 포장재", "C06", 
    	            "<span class='action-tag'>색상 확인 필수</span> 사과나 배를 감싸는 그물 모양의 발포 수지는 흰색일 경우에만 스티로폼으로 배출합니다.<br/>" +
    	            "최근 들어 들어오는 <span class='point-text'>유색(분홍, 노랑 등) 과일망</span>은 재활용 가치가 없어 일반 쓰레기로 분류됩니다.<br/>" +
    	            "또한 이물질이 묻지 않은 깨끗한 상태인지를 반드시 확인 후 배출하세요.", "tag-styrofoam"},

    	        {"I077", "신선식품 아이스박스", "C06", 
    	            "<span class='action-tag'>부착물 완전 박리</span> 택배로 받은 아이스박스의 운송장 스티커와 테이프는 반드시 칼로 긁어서라도 다 떼어내야 합니다.<br/>" +
    	            "테이프 자국이 남으면 재활용 공정에서 불순물로 취급됩니다. <span class='point-text'>내용물이 터져 오염된 박스</span>는<br/>" +
    	            "깨끗이 씻기지 않는다면 쪼개서 종량제 봉투(일반 쓰레기)에 담아 배출해 주세요.", "tag-styrofoam"},

    	        {"I078", "컵라면 용기", "C06", 
    	            "<span class='action-tag'>햇빛 소독 권장</span> 라면 국물이 밴 스티로폼 용기는 물로 헹궈도 빨간 자국이 남는 경우가 많습니다.<br/>" +
    	            "물로 헹군 뒤 <span class='point-text'>햇빛에 하루 정도 말리면</span> 색소가 어느 정도 휘발되어 재활용이 가능해집니다.<br/>" +
    	            "그럼에도 오염이 심하거나 은박지 뚜껑이 붙어 있다면 반드시 일반 쓰레기로 버려야 합니다.", "tag-styrofoam"},

    	        {"I079", "육류/생선 트레이", "C06", 
    	            "<span class='action-tag'>잔여물 세척</span> 정육점에서 흔히 쓰는 흰색 트레이는 핏물과 기름기를 세제로 완전히 씻어내야 합니다.<br/>" +
    	            "트레이 바닥에 깔린 <span class='point-text'>흡수 패드와 상단의 랩 필름</span>은 절대로 스티로폼이 아닙니다.<br/>" +
    	            "이들은 반드시 분리하여 종량제 봉투에 버리고, 깨끗해진 트레이만 스티로폼으로 배출하세요.", "tag-styrofoam"},

    	        {"I080", "배달 용기(흰색)", "C06", 
    	            "<span class='action-tag'>순수 흰색만 배출</span> 음식물이 담겼던 스티로폼 용기는 양념을 완벽히 제거한 뒤 배출하는 것이 원칙입니다.<br/>" +
    	            "주의할 점은 <span class='point-text'>유색 스티로폼(검정색 등)</span>은 흰색 스티로폼과 섞이면 재활용 품질을 낮추므로<br/>" +
    	            "대부분 지자체에서 일반 쓰레기로 분류하길 권장합니다. 흰색 용기만 선별해 주세요.", "tag-styrofoam"},

    	        {"I081", "전자제품 보호 패드", "C06", 
    	            "<span class='action-tag'>재질 재확인</span> 가전제품 사이사이를 보호하는 흰색 발포 스티렌 조각들도 스티로폼으로 배출합니다.<br/>" +
    	            "단, 겉보기엔 비슷하지만 스펀지처럼 말랑한 <span class='point-text'>폴리에틸렌(PE) 폼</span>은 스티로폼이 아닙니다.<br/>" +
    	            "손으로 눌렀을 때 뽀득 소리가 나며 부서지는 딱딱한 흰색 패드만 모아서 배출하세요.", "tag-styrofoam"},

    	        {"I082", "소형 택배 스티로폼", "C06", 
    	            "<span class='action-tag'>흩어짐 방지</span> 반찬이나 소형 냉장 식품이 담긴 작은 스티로폼 상자도 송장을 제거하고 배출합니다.<br/>" +
    	            "스티로폼은 가벼워 바람에 날리기 쉬우므로 <span class='point-text'>투명 비닐 봉투에 담거나 투명 테이프가 아닌 노끈</span>으로 묶어서<br/>" +
    	            "정해진 수거 장소에 안전하게 내놓으시기 바랍니다.", "tag-styrofoam"},

    	        {"I083", "건축용 스티로폼 조각", "C06", 
    	            "<span class='action-tag'>오염물질 확인</span> 인테리어 공사 후 남은 건축 단열재 스티로폼 조각은 오염 여부가 중요합니다.<br/>" +
    	            "표면에 <span class='point-text'>페인트, 시멘트, 실리콘, 흙</span> 등이 묻어 있다면 재활용이 전혀 되지 않습니다.<br/>" +
    	            "이런 경우 지자체 전용 폐기물 봉투(마대)에 담아 배출해야 합니다.", "tag-styrofoam"},

    	        {"I084", "단열 벽지(스티로폼 재질)", "C06", 
    	            "<span class='action-tag'>접착면 제거</span> 벽면에 붙이는 얇은 폼 블록이나 단열재는 뒷면의 접착 성분이 문제입니다.<br/>" +
    	            "스티커처럼 떼어낼 수 있는 <span class='point-text'>이형지나 강력 접착제</span>가 남아 있다면 재활용 공정에서 화재를 유발할 수 있습니다.<br/>" +
    	            "순수한 스티로폼 부분만 남기기 어렵다면 종량제 봉투에 담아 버리시는 것이 안전합니다.", "tag-styrofoam"},

    	     // [플라스틱 - C07] (I085 ~ I102) 보강 버전
    	        {"I085", "국자(플라스틱)", "C07", 
    	            "<span class='action-tag'>재질 구성 확인</span> 전체가 순수 플라스틱(PP, PE 등)인 경우에만 플라스틱류로 배출이 가능합니다.<br/>" +
    	            "손잡이가 <span class='point-text'>나무, 실리콘, 또는 스테인리스</span>로 된 혼합 재질은 재활용 공정에서 분리가 어려워 불량의 원인이 됩니다.<br/>" +
    	            "분리가 안 되는 혼합 재질 국자는 반드시 종량제 봉투(일반 쓰레기)에 버려주세요.", "tag-plastic"},

    	        {"I086", "그릇(플라스틱)", "C07", 
    	            "<span class='action-tag'>세척 및 건조</span> 남은 음식물과 기름기를 주방세제로 깨끗이 씻어낸 뒤 건조하여 배출하세요.<br/>" +
    	            "하지만 <span class='point-text'>고추장 등 양념 배임이 심해 붉게 변했거나 기름기가 빠지지 않는 그릇</span>은 재활용 가치가 없습니다.<br/>" +
    	            "오염된 상태라면 깨끗한 자원을 오염시키지 않도록 일반 쓰레기로 배출하는 것이 올바른 방법입니다.", "tag-plastic"},

    	        {"I087", "도마(플라스틱)", "C07", 
    	            "<span class='action-tag'>순수 플라스틱 판별</span> 칼자국 사이사이에 낀 음식물을 솔로 닦아내고 배출해야 합니다.<br/>" +
    	            "겉은 플라스틱처럼 보이지만 내부가 나무인 경우나, <span class='point-text'>고무·실리콘 재질의 도마</span>는 재활용 대상이 아닙니다.<br/>" +
    	            "재질 분류가 불명확하거나 실리콘 소재라면 반드시 종량제 봉투에 담아 버려주세요.", "tag-plastic"},

    	        {"I088", "리코더", "C07", 
    	            "<span class='action-tag'>부속품 선별</span> 교육용 리코더는 순수 플라스틱 재질이므로 본체는 플라스틱류로 배출 가능합니다.<br/>" +
    	            "다만 내부의 <span class='point-text'>청소용 솔(금속/섬유)과 천 케이스</span>는 재활용되지 않는 소모품이므로 따로 분리하세요.<br/>" +
    	            "리코더 마디마디를 분해하여 부피를 줄인 뒤 배출하면 더욱 좋습니다.", "tag-plastic"},

    	        {"I089", "마요네즈/케찹 용기", "C07", 
    	            "<span class='action-tag'>유분 완전 제거</span> 마요네즈와 같은 기름진 소스는 용기 내벽에 강하게 밀착되어 재활용을 방해합니다.<br/>" +
    	            "따뜻한 물과 세제를 넣고 여러 번 흔들어 <span class='point-text'>미끈거림이 전혀 없게 세척</span>한 뒤 배출하세요.<br/>" +
    	            "입구의 비닐 덮개와 뚜껑에 묻은 잔여물도 꼼꼼히 닦아내야 합니다.", "tag-plastic"},

    	        {"I090", "메가폰", "C07", 
    	            "<span class='action-tag'>전자 부품 분리</span> 메가폰 내부에는 전선, 스피커, 건전지 등 다양한 금속 부품이 들어있습니다.<br/>" +
    	            "드라이버로 나사를 풀어 <span class='point-text'>내부 부품을 모두 적출한 뒤</span> 순수 플라스틱 껍데기만 배출하는 것이 원칙입니다.<br/>" +
    	            "분해할 도구나 능력이 안 된다면 대형 폐기물이나 소형 가전으로 신고 배출하세요.", "tag-plastic"},

    	        {"I091", "빨대", "C07", 
    	            "<span class='action-tag'>크기 선별 한계</span> 빨대는 플라스틱 재질이지만, 크기가 너무 작고 가벼워 선별 기계의 틈새로 빠져나갑니다.<br/>" +
    	            "결과적으로 선별장에서 쓰레기로 버려지는 경우가 많으므로 <span class='point-text'>종량제 봉투 배출</span>을 강력히 권장합니다.<br/>" +
    	            "수백 개 이상을 한곳에 꽉 묶어서 내놓지 않는 이상 일반 쓰레기로 처리해 주세요.", "tag-plastic"},

    	        {"I092", "볼풀공", "C07", 
    	            "<span class='action-tag'>청결 유지</span> 키즈카페나 가정에서 쓰는 볼풀공은 PE 재질의 플라스틱으로 재활용이 잘 되는 품목입니다.<br/>" +
    	            "찌그러진 것은 상관없으나 <span class='point-text'>침이나 먼지 등 이물질</span>이 많이 묻었다면 가볍게 닦아 배출하세요.<br/>" +
    	            "그물망에 담겨 있다면 그물망(비닐류)은 따로 분리해서 배출해야 합니다.", "tag-plastic"},

    	        {"I093", "분무기", "C07", 
    	            "<span class='action-tag'>노즐부 폐기</span> 분무기 헤드 내부에는 액체를 끌어올리기 위한 <span class='point-text'>금속 스프링</span>이 숨어있어 재활용이 불가합니다.<br/>" +
    	            "헤드 부분은 통째로 잘라 일반 쓰레기로 버리고, 통 내부의 액체를 완전히 비운 <span class='point-text'>몸체(페트/플라스틱)만</span><br/>" +
    	            "라벨을 제거한 뒤 플라스틱류로 배출해 주세요.", "tag-plastic"},

    	        {"I094", "비디오테이프", "C07", 
    	            "<span class='action-tag'>완전 분해의 정석</span> 테이프는 복합 재질의 결정체로 그대로 버리면 100% 폐기됩니다.<br/>" +
    	            "1. 나사를 풀어 케이스를 연다. 2. 내부의 필름과 소형 금속 부품을 일반 쓰레기로 버린다. <br/>" +
    	            "3. <span class='point-text'>외부 투명/검정 플라스틱 케이스</span>만 플라스틱으로 배출한다. 이 단계가 불가능하면 종량제 봉투에 버리세요.", "tag-plastic"},

    	        {"I095", "샴푸 용기", "C07", 
    	            "<span class='action-tag'>펌프 재질 주의</span> 샴푸나 린스 용기의 펌프 노즐 안에는 아주 작은 쇠 스프링이 들어있습니다.<br/>" +
    	            "<span class='point-text'>펌프는 반드시 일반 쓰레기</span>로 버리고, 용기 본체만 내부 거품이 나오지 않을 때까지<br/>" +
    	            "물로 대여섯 번 헹구어 배출하세요. 펌프를 꽂은 채 버리면 용기 전체가 폐기됩니다.", "tag-plastic"},

    	        {"I096", "쓰레받기", "C07", 
    	            "<span class='action-tag'>부착물 확인</span> 플라스틱 쓰레받기 끝부분에 부착된 연질의 <span class='point-text'>고무날이나 고정용 철핀</span>은 제거해야 합니다.<br/>" +
    	            "먼지와 흙을 깨끗이 털어내고 순수한 플라스틱 본체만 배출하세요.<br/>" +
    	            "손잡이 끝에 달린 끈(마끈 등)도 제거 대상입니다.", "tag-plastic"},

    	        {"I097", "식용유 용기", "C07", 
    	            "<span class='action-tag'>기름기 제로</span> 식용유 페트병은 재활용 품질을 크게 좌우합니다. 주방세제를 두세 방울 넣어 유분을 완전히 닦으세요.<br/>" +
    	            "용기 겉면에 붙은 <span class='point-text'>비닐 라벨을 제거</span>하는 것도 잊지 마세요.<br/>" +
    	            "기름기가 남아있는 상태로 배출되면 선별장에서 주변의 다른 깨끗한 자원까지 오염시킵니다.", "tag-plastic"},

    	        {"I098", "젖병", "C07", 
    	            "<span class='action-tag'>부속품별 분리</span> 아이가 쓰는 젖병은 고품질 플라스틱(PPSU, PP 등)이지만 부속품은 재질이 다릅니다.<br/>" +
    	            "입에 닿는 <span class='point-text'>실리콘 젖꼭지는 무조건 종량제 봉투</span>로 가야 합니다.<br/>" +
    	            "젖병 캡과 본체만 플라스틱으로 배출하되, 내부 우유 잔여물이 없도록 살균 세척 후 배출하세요.", "tag-plastic"},

    	        {"I099", "치약 용기", "C07", 
    	            "<span class='action-tag'>선별적 배출</span> 치약 튜브는 은박지와 플라스틱이 겹겹이 층을 이룬 복합 재질이라 본체는 재활용이 안 됩니다.<br/>" +
    	            "내용물을 다 썼더라도 <span class='point-text'>본체는 일반 쓰레기</span>에 버려주시고, <span class='point-text'>플라스틱 뚜껑만</span> 따로 모아 플라스틱으로 배출하세요.<br/>" +
    	            "최근 나오는 플라스틱 단일 재질 치약은 세척 후 배출 가능합니다.", "tag-plastic"},

    	        {"I100", "플라스틱 뚜껑/캡", "C07", 
    	            "<span class='action-tag'>자원 순환 극대화</span> 페트병이나 유리병에서 분리한 작은 플라스틱 뚜껑들은 선별장에서 분실되기 쉽습니다.<br/>" +
    	            "이들을 따로 모으는 <span class='point-text'>병뚜껑 전용 수거함</span>에 배출하거나, 큰 투명 페트병 안에 가득 모아서 배출하면<br/>" +
    	            "작은 플라스틱들도 훌륭한 재생 원료(치약 짜개, 업사이클링 굿즈 등)로 재탄생할 수 있습니다.", "tag-plastic"},

    	        {"I101", "플라스틱 컵", "C07", 
    	            "<span class='action-tag'>순수 투명 유지</span> 일회용 커피 컵은 내용물을 헹구고 빨대, 종이 홀더를 제거한 뒤 배출해야 합니다.<br/>" +
    	            "특히 <span class='point-text'>로고가 크게 인쇄된 컵이나 색깔이 들어간 일회용 컵</span>은 투명 페트와 섞이면 가치가 떨어집니다.<br/>" +
    	            "지자체에 따라 일회용 컵 보증금제 대상인 경우 반납하여 보증금을 돌려받으세요.", "tag-plastic"},

    	        {"I102", "페트병(투명)", "C07", 
    	            "<span class='action-tag'>고품질 배출 4단계</span> 투명 페트병은 고품질 섬유로 재활용되는 아주 귀한 자원입니다.<br/>" +
    	            "1. 내용물 비우기 2. <span class='point-text'>라벨 떼기</span> 3. 찌그러뜨리기 4. 뚜껑 닫기.<br/>" +
    	            "반드시 불투명 플라스틱과 섞이지 않도록 <b>투명 페트병 전용 수거함</b>에 별도로 배출해 주세요.", "tag-plastic"},
    	        
    	     // [기타 - C08] (I103 ~ I138) 보강 버전
    	        {"I103", "농약용기", "C08", 
    	            "<span class='action-tag'>특수 폐기물</span> 농약병은 토양과 수질에 치명적인 독성을 가집니다. 일반 분리배출함에 절대 넣지 마세요.<br/>" +
    	            "내용물을 완전히 비운 뒤 <span class='point-text'>마을 단위나 지자체 지정 농약 전용 수거함</span>에 안전하게 배출해야 합니다.", "tag-clothing"},

    	        {"I104", "구두", "C08", 
    	            "<span class='action-tag'>상태별 분류</span> 상태가 좋아 재사용이 가능한 구두는 헌옷 수거함에 짝을 맞춰 배출하세요.<br/>" +
    	            "창이 떨어지거나 <span class='point-text'>심하게 낡은 구두, 장화, 털장화</span> 등은 재사용이 안 되므로 종량제 봉투에 담아 버려야 합니다.", "tag-clothing"},

    	        {"I105", "샌들", "C08", 
    	            "<span class='action-tag'>복합재질 주의</span> 가죽, 고무, 금속 장식이 섞인 샌들은 깨끗하다면 헌옷 수거함에 배출 가능합니다.<br/>" +
    	            "다만, 최근의 <span class='point-text'>크록스(EVA 재질)나 슬리퍼</span> 형태는 헌옷 수거 품목이 아닌 경우가 많으니 지역별 지침을 확인하세요.", "tag-clothing"},

    	        {"I106", "슬리퍼", "C08", 
    	            "<span class='action-tag'>종량제 봉투</span> 슬리퍼는 고무, 스펀지, 천 등 여러 재질이 강력하게 접착되어 있어 재활용이 불가능합니다.<br/>" +
    	            "재질에 상관없이 <span class='point-text'>일반 쓰레기(종량제 봉투)</span>로 배출하는 것이 올바른 방법입니다.", "tag-clothing"},

    	        {"I107", "머플러/목도리", "C08", 
    	            "<span class='action-tag'>헌옷 수거함</span> 오염되지 않은 면, 실크, 울 재질의 목도리는 헌옷 수거함 배출이 가능합니다.<br/>" +
    	            "비에 젖으면 곰팡이가 생겨 다른 의류까지 망치므로 <span class='point-text'>반드시 비닐봉지에 담아</span> 젖지 않게 배출하세요.", "tag-clothing"},

    	        {"I108", "모자", "C08", 
    	            "<span class='action-tag'>형태 보존</span> 캡이 살아있는 깨끗한 야구모자 등은 헌옷 수거함으로 갑니다.<br/>" +
    	            "하지만 <span class='point-text'>앞챙이 부러졌거나 땀으로 변색이 심한 모자</span>는 재사용이 불가능하므로 일반 쓰레기로 버려주세요.", "tag-clothing"},

    	        {"I109", "의류", "C08", 
    	            "<span class='action-tag'>헌옷 수거함</span> 티셔츠, 바지 등 의류는 헌옷 수거함에 넣으세요. 단추나 지퍼는 떼지 않아도 됩니다.<br/>" +
    	            "단, <span class='point-text'>속옷, 양말, 솜이불, 걸레, 심하게 오염된 옷</span>은 수거 대상이 아니므로 종량제 봉투에 버려야 합니다.", "tag-clothing"},

    	        {"I110", "식용유", "C08", 
    	            "<span class='action-tag'>전용 폐유 수거함</span> 유통기한이 지났거나 사용한 식용유를 하수구에 버리면 관로가 막히고 수질 오염이 발생합니다.<br/>" +
    	            "아파트 단지 내 <span class='point-text'>폐유 수거함</span>에 모으거나, 소량일 경우 종이나 기저귀 등에 흡수시켜 종량제로 버리세요.", "tag-clothing"},

    	        {"I111", "기계/엔진오일", "C08", 
    	            "<span class='action-tag'>전문 업체 처리</span> 자동차나 기계에서 나온 오일은 폐기물 관리법에 따라 엄격히 처리되어야 합니다.<br/>" +
    	            "가까운 <span class='point-text'>정비소나 카센터</span>에 방문하여 처리를 부탁하거나 전문 수거 업체에 문의해야 합니다.", "tag-clothing"},

    	        {"I112", "자동차 부품/타이어", "C08", 
    	            "<span class='action-tag'>반납 또는 신고</span> 타이어는 새 타이어 교체 시 판매점에 반납하는 것이 가장 좋습니다.<br/>" +
    	            "개별 배출 시 지자체 <span class='point-text'>대형 폐기물 스티커</span>를 부착해야 하며, 배터리 등은 카센터에 문의하세요.", "tag-clothing"},

    	        {"I113", "빗/헤어브러시", "C08", 
    	            "<span class='action-tag'>종량제 봉투</span> 머리카락을 완전히 제거한 뒤 배출하세요.<br/>" +
    	            "손잡이는 플라스틱이라도 빗살 부분이 나무, 고무, 짐승 털(돈모) 등 복합 재질이라 <span class='point-text'>재활용이 안 됩니다.</span>", "tag-clothing"},

    	        {"I114", "애완동물집/케이스", "C08", 
    	            "<span class='action-tag'>대형 폐기물</span> 플라스틱 이동장이나 대형 집은 지자체에 신고 후 대형 폐기물로 배출하세요.<br/>" +
    	            "금속 문을 분해할 수 있다면 문은 고철로, <span class='point-text'>나머지 플라스틱 몸체</span>는 플라스틱으로 배출 가능합니다.", "tag-clothing"},

    	        {"I115", "야구배트", "C08", 
    	            "<span class='action-tag'>재질별 구분</span> 알루미늄 배트는 고철류로 배출하면 재활용이 됩니다.<br/>" +
    	            "반면 <span class='point-text'>나무(우드) 배트나 카본 소재</span>는 재활용이 안 되므로 종량제 봉투나 대형 폐기물로 처리하세요.", "tag-clothing"},

    	        {"I116", "와이퍼", "C08", 
    	            "<span class='action-tag'>종량제 봉투</span> 와이퍼의 금속 프레임과 고무 날은 강력하게 압착되어 분리가 매우 어렵습니다.<br/>" +
    	            "분리하지 않은 와이퍼는 고철로 재활용되지 못하므로 <span class='point-text'>일반 쓰레기</span>로 버리는 것이 일반적입니다.", "tag-clothing"},

    	        {"I117", "줄자", "C08", 
    	            "<span class='action-tag'>종량제 봉투</span> 외부는 플라스틱, 내부는 금속 태엽 스프링으로 구성된 복합 제품입니다.<br/>" +
    	            "분해가 거의 불가능하므로 <span class='point-text'>일반 쓰레기(종량제 봉투)</span>에 넣어 배출하시기 바랍니다.", "tag-clothing"},

    	        {"I118", "바나나/오렌지껍질", "C08", 
    	            "<span class='action-tag'>음식물 쓰레기</span> 가공 공정을 거쳐 가축의 사료로 쓰일 수 있는 부드러운 껍질은 음식물 쓰레기입니다.<br/>" +
    	            "단, <span class='point-text'>바나나 꼭지의 딱딱한 부분</span>은 동물이 먹기 어려우므로 일반 쓰레기로 잘라 버려주세요.", "tag-clothing"},

    	        {"I119", "상한 음식", "C08", 
    	            "<span class='action-tag'>음식물 쓰레기</span> 곰팡이가 피거나 상한 음식도 음식물 쓰레기로 배출 가능합니다.<br/>" +
    	            "이때 <span class='point-text'>비닐, 랩, 이쑤시개 등 이물질</span>을 반드시 제거하고 물기를 최대한 짠 뒤 배출하세요.", "tag-clothing"},

    	        {"I120", "생선 뼈/가시", "C08", 
    	            "<span class='action-tag'>일반 쓰레기 주의</span> 생선 가시나 소, 돼지, 닭의 뼈는 사료화 과정에서 분쇄되지 않아 가축의 내장을 손상시킵니다.<br/>" +
    	            "<span class='point-text'>반드시 종량제 봉투</span>에 담아 배출하시고 살코기가 많이 붙어 있지 않도록 주의하세요.", "tag-clothing"},

    	        {"I121", "깨진 유리/거울", "C08", 
    	            "<span class='action-tag'>불연성 마대</span> 깨진 유리는 유리병 재활용 공정에 들어갈 수 없습니다.<br/>" +
    	            "두꺼운 종이나 신문지에 감싸 <span class='point-text'>지자체 지정 불연성 마대(타지 않는 쓰레기)</span>에 담아 안전하게 배출하세요.", "tag-clothing"},

    	        {"I122", "도자기/사기그릇", "C08", 
    	            "<span class='action-tag'>타지 않는 쓰레기</span> 사기그릇, 도자기, 뚝배기 등은 소각장에서 타지 않고 남습니다.<br/>" +
    	            "일반 종량제 봉투가 아닌 <span class='point-text'>불연성 전용 봉투(마대)</span>를 구매하여 배출해야 합니다.", "tag-clothing"},

    	        {"I123", "내열식기/강화유리", "C08", 
    	            "<span class='action-tag'>혼입 금지</span> 내열유리(글라스락 등)는 일반 유리병보다 훨씬 높은 온도에서 녹아 유리 재활용을 망칩니다.<br/>" +
    	            "절대 유리병 수거함에 넣지 마시고 <span class='point-text'>불연성 마대</span>에 담아 배출하시기 바랍니다.", "tag-clothing"},

    	        {"I124", "백열전구", "C08", 
    	            "<span class='action-tag'>종량제/불연성</span> 형광등과 달리 수은이 들어있지 않아 형광등 수거함 배출 대상이 아닙니다.<br/>" +
    	            "신문지에 잘 싸서 <span class='point-text'>종량제 봉투</span>에 넣거나, 양이 많으면 불연성 마대를 이용하세요.", "tag-clothing"},

    	        {"I125", "벽돌", "C08", 
    	            "<span class='action-tag'>폐기물 봉투</span> 집수리 후 나온 소량의 벽돌은 주민센터 등에서 판매하는 특수 규격 마대(불연성)에 담으세요.<br/>" +
    	            "<span class='point-text'>양이 많을 경우</span> 건설 폐기물 처리 업체를 통해 신고 후 배출해야 합니다.", "tag-clothing"},

    	        {"I126", "화분/화병", "C08", 
    	            "<span class='action-tag'>재질 선별</span> 플라스틱 화분은 플라스틱으로, 고철 화분은 고철로 배출하세요.<br/>" +
    	            "단, <span class='point-text'>도자기나 토기 화분</span>은 불연성 마대에 담아 버려야 하며, 흙은 화단에 뿌리거나 별도 처리하세요.", "tag-clothing"},

    	        {"I127", "가습기/공기청정기", "C08", 
    	            "<span class='action-tag'>소형 가전</span> 높이 1m 미만의 가습기는 소형 가전 전용 수거함에 무상 배출 가능합니다.<br/>" +
    	            "단, <span class='point-text'>공기청정기 내부의 필터</span>는 재활용이 안 되므로 반드시 빼서 일반 쓰레기로 버려야 합니다.", "tag-clothing"},

    	        {"I128", "냉장고/세탁기/에어컨", "C08", 
    	            "<span class='action-tag'>무상 방문수거</span> 대형 가전은 스티커를 사지 마세요. <span class='point-text'>1599-0903</span>이나 웹사이트(15990903.or.kr)를 통해<br/>" +
    	            "무상 방문 수거를 예약하면 전문 기사가 직접 방문하여 수거해 갑니다.", "tag-clothing"},

    	        {"I129", "노트북/컴퓨터", "C08", 
    	            "<span class='action-tag'>정보 보안 주의</span> 노트북은 소형 가전으로 배출 가능합니다.<br/>" +
    	            "배출 전 <span class='point-text'>하드디스크(SSD/HDD)를 파쇄</span>하거나 전용 소프트웨어로 데이터를 영구 삭제하세요.", "tag-clothing"},

    	        {"I130", "소형가전(다리미/카메라)", "C08", 
    	            "<span class='action-tag'>무상 배출</span> 다리미, 헤어드라이어, 카메라 등 소형 가전은 보통 5개 이상일 때 무상 수거를 신청하거나<br/>" +
    	            "단지 내 <span class='point-text'>소형 가전 전용 수거함</span>에 상시 배출할 수 있습니다.", "tag-clothing"},

    	        {"I131", "런닝머신", "C08", 
    	            "<span class='action-tag'>대형 폐기물</span> 가전 수거 대상에서 제외되는 운동기구류는 지자체에 대형 폐기물로 신고해야 합니다.<br/>" +
    	            "<span class='point-text'>스티커를 구매하여 부착</span>한 후 지정된 날짜와 장소에 내놓으세요.", "tag-clothing"},

    	        {"I132", "선풍기", "C08", 
    	            "<span class='action-tag'>소형 가전</span> 선풍기는 날개나 망을 분해할 필요 없이 본체 그대로 소형 가전함에 넣으시면 됩니다.<br/>" +
    	            "<span class='point-text'>리모컨</span>이 있다면 함께 가전 수거함에 담아 배출하세요.", "tag-clothing"},

    	        {"I133", "전기밥솥", "C08", 
    	            "<span class='action-tag'>가전 배출</span> 전기밥솥은 소형 가전 수거함에 배출합니다.<br/>" +
    	            "내부에 코팅된 <span class='point-text'>금속 내솥</span>은 깨끗이 씻어 그대로 넣거나 고철로 따로 배출해도 됩니다.", "tag-clothing"},

    	        {"I134", "전자레인지", "C08", 
    	            "<span class='action-tag'>분리 배출</span> 본체는 소형 가전 수거함에 무상 배출 가능합니다.<br/>" +
    	            "내부의 <span class='point-text'>회전 유리 받침대</span>는 재활용이 안 되는 내열유리인 경우가 많으니 불연성 마대에 버려주세요.", "tag-clothing"},

    	        {"I135", "청소기", "C08", 
    	            "<span class='action-tag'>가전 수거</span> 유선/무선 청소기 모두 소형 가전 수거함에 배출하세요.<br/>" +
    	            "<span class='point-text'>먼지통 내부의 쓰레기</span>는 깨끗이 비우고 필터는 일반 쓰레기로 버린 후 배출하는 것이 매너입니다.", "tag-clothing"},

    	        {"I136", "키보드/마우스", "C08", 
    	            "<span class='action-tag'>폐가전 분류</span> PC 주변기기인 키보드와 마우스도 가전제품으로 분류되어 소형 가전 수거함에 배출 가능합니다.<br/>" +
    	            "<span class='point-text'>무선 마우스의 건전지</span>는 반드시 빼서 건전지 수거함에 따로 버려주세요.", "tag-plastic"},

    	        {"I137", "프린터", "C08", 
    	            "<span class='action-tag'>카트리지 처리</span> 프린터 본체는 소형 가전 수거함에 배출하세요.<br/>" +
    	            "가급적 <span class='point-text'>토너나 잉크 카트리지</span>는 별도로 분리하여 전용 수거함에 배출하는 것이 환경 보호에 도움이 됩니다.", "tag-clothing"},

    	        {"I138", "휴대전화", "C08", 
    	            "<span class='action-tag'>희귀금속의 보고</span> 스마트폰에는 금, 은, 팔라듐 등 귀한 금속이 많이 포함되어 있습니다.<br/>" +
    	            "<span class='point-text'>주민센터 전용 수거함이나 나눔폰(수거 서비스)</span>을 이용하면 환경도 보호하고 기부도 가능합니다.", "tag-clothing"}
    	    };
    
    
    public static String getFoodWasteGuideHtml() {
        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".sub-title { color: #825aff; font-size: 17px; font-weight: bold; margin-top: 15px; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".not-food { color: #ff5555; font-weight: bold; }" +
                "ul { padding-left: 20px; } li { margin-bottom: 5px; }" +
                ".step-container { display: flex; justify-content: space-around; text-align: center; margin-top: 10px; }" +
                "</style></head><body>" +
                "<div style='font-size: 24px; font-weight: bold; color: #00fff0;'>🍎 음식물류폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>식재료 및 식품의 생산·유통·조리 등의 과정에서 발생하는 쓰레기와 남겨서 버려지는 음식물" +
                "<br><br><b>종류:</b> 곡식, 채소, 과일껍질, 상한음식, 남은 음식물 등</div>" +

                "<div class='section-title' style='color:#ff5555; border-bottom: 2px solid #ff5555;'>⚠️ 음식물류가 아닌 품목 (일반 종량제 배출)</div>" +
                "<div class='content-box'>" +
                "<ul>" +
                "<li><b>육류:</b> 소·돼지·닭 등의 뼈, 털, 깃털</li>" +
                "<li><b>패류:</b> 조개, 전복, 굴 등껍데기, 게·가재 껍데기</li>" +
                "<li><b>과일/견과:</b> 복숭아·살구·감 등 핵과류의 씨, 호두·밤·땅콩·코코넛 껍질</li>" +
                "<li><b>알껍데기:</b> 달걀, 오리알, 메추리알 껍질</li>" +
                "<li><b>채소류:</b> 양파·마늘·생강·옥수수 껍질, 파·미나리 뿌리, 고추대, 옥수수대</li>" +
                "<li><b>기타:</b> 독성이 있는 복어내장, 티백, 육수팩, 한약·커피·차 찌꺼기</li>" +
                "</ul></div>" +

                "<div class='section-title'>🚛 배출방법</div>" +
                "<div class='content-box'>수분을 최대한 제거하고 이물질을 없앤 후 전용 방식을 이용해 배출하세요." +
                "<br><b>방식:</b> 종량제 봉투, 전용용기(스티커/칩), RFID 방식 등</div>" +

                "<div class='section-title'>♻️ 재활용 과정</div>" +
                "<div class='content-box'>" +
                "<b>STEP 01:</b> 처리시설 반입<br>" +
                "<b>STEP 02:</b> 파쇄 및 이물질 선별<br>" +
                "<b>STEP 03:</b> 멸균 / 건조·발효과정<br>" +
                "<b>결과물:</b> 습식/건식 사료, 퇴비, 바이오가스" +
                "</div>" +
                "</body></html>";
    }
    
    public static String getGeneralWasteGuideHtml() {
        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".step-container { display: flex; text-align: center; margin-top: 10px; }" +
                ".step-box { background: #3d3d70; padding: 10px; border-radius: 8px; margin: 5px; font-size: 13px; }" +
                "</style></head><body>" +
                "<div style='font-size: 24px; font-weight: bold; color: #00fff0;'>🗑️ 일반종량제폐기물</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "휴지, 나무제품, 플라스틱 조각, 고무제품 등 재활용이 어렵고 불에 타는 폐기물<br><br>" +
                "• <b>사용한 휴지:</b> 오염된 종이류<br>" +
                "• <b>나무도마/제품:</b> 가공된 나무류<br>" +
                "• <b>플라스틱 조각:</b> 소량의 파손된 플라스틱<br>" +
                "• <b>필기도구:</b> 볼펜, 샤프 등 복합재질<br>" +
                "• <b>고무장갑:</b> 천연/합성 고무제품" +
                "</div>" +

                "<div class='section-title'>🚛 배출방법</div>" +
                "<div class='content-box'>" +
                "1. 재활용 가능한 것을 최대한 분리한 후 <b>일반가연성종량제봉투</b>에 배출<br>" +
                "2. 봉투에 담기 어려운 폐기물은 <b>대형폐기물</b>로 신고 배출" +
                "</div>" +

                "<div class='section-title'>♻️ 처리과정</div>" +
                "<div class='content-box'>" +
                "<b>STEP 01. 소각시설(반입)</b><br>수거된 폐기물을 소각장으로 운반<br><br>" +
                "<b>STEP 02. 연소(850℃ 이상)</b><br>고온의 연소실에서 위생적으로 소각 처리<br><br>" +
                "<b>STEP 03. 자원회수 및 매립</b><br>발생한 고온의 열을 스팀으로 회수하여 지역난방이나 전기에너지로 이용하며, 남은 재는 매립 또는 고철 재활용" +
                "</div>" +
                "</body></html>";
    }
    
    public static String getNonFlammableWasteGuideHtml() {
        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight { color: #ff9d00; font-weight: bold; }" +
                "</style></head><body>" +
                "<div style='font-size: 24px; font-weight: bold; color: #00fff0;'>🧱 불연성종량제폐기물</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "유리, 벽돌, 내열식기류, 도자기, 연탄재 등 재활용이 어렵고 <span class='highlight'>불에 타지 않는</span> 폐기물<br><br>" +
                "• <b>유리제품:</b> 깨진 유리, 거울 등<br>" +
                "• <b>자기류:</b> 사기그릇, 화분, 도자기 파편<br>" +
                "• <b>건축자재:</b> 소량의 벽돌, 타일, 시멘트 블록<br>" +
                "• <b>기타:</b> 내열식기류, 수석, 연탄재" +
                "</div>" +

                "<div class='section-title'>🚛 배출방법</div>" +
                "<div class='content-box'>" +
                "1. <span class='highlight'>불연성 특수규격마대</span>(지자체 지정 판매소 구매)에 담아 배출<br>" +
                "2. 마대에 담기 어려운 다량의 폐기물은 <b>대형폐기물</b>로 신고 배출<br>" +
                "3. 연탄재는 지자체 조례 및 정해진 배출방법에 따라 무상 또는 유상 배출" +
                "</div>" +

                "<div class='section-title'>♻️ 처리과정</div>" +
                "<div class='content-box'>" +
                "<b>STEP 01. 선별시설 반입</b><br>공사장생활폐기물 선별시설 등으로 운반 및 반입<br><br>" +
                "<b>STEP 02. 선별 및 파쇄</b><br>자석 및 수선별을 통해 재활용 가능한 건설폐재류 분리<br><br>" +
                "<b>STEP 03. 자원화 및 매립</b><br>건설폐재류는 파쇄 후 <span class='highlight'>순환골재</span>로 재활용하며, 재활용이 불가능한 나머지는 매립시설에서 매립 처분" +
                "</div>" +
                "</body></html>";
    }
    
    public static String getBulkyWasteGuideHtml() {
        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight { color: #ff9d00; font-weight: bold; }" +
                ".info-list { margin-left: -20px; }" +
                "</style></head><body>" +
                "<div style='font-size: 24px; font-weight: bold; color: #00fff0;'>🛋️ 대형폐기물</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "종량제봉투에 담기 어려운 크기의 폐기물<br><br>" +
                "• <b>가구류:</b> 침대, 장롱, 책상, 의자, 소파 등<br>" +
                "• <b>침구류:</b> 매트리스, 카페트, 이불 등<br>" +
                "• <b>가전제품:</b> 냉장고, 세탁기, 에어컨, TV 등<br>" +
                "• <b>생활용품:</b> 거울, 자전거, 유모차, 시계 등" +
                "</div>" +

                "<div class='section-title'>🚛 배출방법</div>" +
                "<div class='content-box'>" +
                "<b>1. <span class='highlight'>대형폐기물 스티커</span> 부착 배출</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 주민센터 방문 또는 인터넷/모바일로 스티커 구매 후 부착<br><br>" +
                "<b>2. 지역 재활용센터 판매</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 재사용이 가능한 상태의 깨끗한 제품은 센터에 연락<br><br>" +
                "<b>3. 가전제품 <span class='highlight'>무상수거 서비스</span> 이용</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 폐가전 무상배출예약시스템 이용 (☎ 1599-0903)" +
                "</div>" +

                "<div class='section-title'>♻️ 처리과정</div>" +
                "<div class='content-box'>" +
                "<b>STEP 01. 수거 및 운반</b><br>신고된 장소에서 지자체 또는 대행업체가 폐기물 수거<br><br>" +
                "<b>STEP 02. 중간 처리 (파쇄/선별)</b><br>재활용 가능한 부품(고철, 플라스틱 등)을 선별하고 나머지는 파쇄<br><br>" +
                "<b>STEP 03. 최종 처리</b><br>재활용 불가능한 잔재물은 소각하거나 매립지로 운반하여 처리" +
                "</div>" +
                "</body></html>";
    }
    
    public static String getConstructionWasteGuideHtml() {
        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight { color: #ff9d00; font-weight: bold; }" +
                ".item-tag { background: #3d3d70; padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-right: 5px; }" +
                "</style></head><body>" +
                "<div style='font-size: 24px; font-weight: bold; color: #00fff0;'>🏗️ 공사장 생활폐기물</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "인테리어·리모델링 공사 시 발생하는 <span class='highlight'>5톤 미만</span>의 폐기물<br><br>" +
                "• <b>건설폐재류:</b> 폐벽돌, 폐타일, 폐콘크리트 등<br>" +
                "• <b>가연성폐기물:</b> 폐목재, 폐벽지, 장판, 폐합성수지<br>" +
                "• <b>대형/기타:</b> 싱크대, 세면대, 변기, 고철류" +
                "</div>" +

                "<div class='section-title'>🚛 성상별 배출방법</div>" +
                "<div class='content-box'>" +
                "<b>1. <span class='highlight'>대형폐기물</span> 배출</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 싱크대, 변기, 폐목재 등은 스티커 구매 후 부착 배출<br><br>" +
                "<b>2. <span class='highlight'>특수규격봉투(마대)</span> 배출</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 폐벽돌, 폐타일 등 불에 타지 않는 폐기물을 전용 마대에 담아 배출<br><br>" +
                "<b>3. 재활용 및 일반 배출</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 고철·플라스틱은 재활용으로, 폐벽지는 일반 종량제 봉투에 배출<br><br>" +
                "<b>4. 전문처리업체 위탁</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 다량 배출 시 지자체 지정 공공선별장이나 전문업체에 신청" +
                "</div>" +

                "<div class='section-title'>♻️ 처리과정</div>" +
                "<div class='content-box'>" +
                "<b>STEP 01. 성상별 선별</b><br>공사장 발생 폐기물을 가연성, 불연성, 재활용으로 분류<br><br>" +
                "<b>STEP 02. 시설 운반 및 처리</b><br>가연성은 소각, 불연성은 매립, 건설폐재류는 파쇄 시설로 운반<br><br>" +
                "<b>STEP 03. 자원화 (순환골재)</b><br>선별된 건설폐재류는 파쇄 과정을 거쳐 <span class='highlight'>순환골재</span>로 재탄생하여 건설 현장에서 재활용" +
                "</div>" +
                "</body></html>";
    }
  
    public static String getHazardousWasteGuideHtml() {
        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight { color: #ff9d00; font-weight: bold; }" +
                "</style></head><body>" +
                "<div style='font-size: 24px; font-weight: bold; color: #00fff0;'>🧪 생활계 유해폐기물</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "인체와 환경에 유해한 성분을 포함하고 있어 <span class='highlight'>별도 관리</span>가 필요한 폐기물<br><br>" +
                "• <b>화학제품:</b> 폐농약, 폐페인트, 락카, 광택제, 접착제<br>" +
                "• <b>보건/의료:</b> 폐의약품(알약, 가루약 등), 수은 온도계/혈압계<br>" +
                "• <b>기타:</b> 천연방사성제품 생활폐기물 등" +
                "</div>" +

                "<div class='section-title'>🚛 품목별 배출방법</div>" +
                "<div class='content-box'>" +
                "<b>1. <span class='highlight'>전용 수거함</span> 배출 (원칙)</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 지자체별 비치된 유해폐기물 전용 수거함에 안전하게 배출<br><br>" +
                "<b>2. 폐의약품</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 약국, 보건소, 주민센터 전용 수거함 또는 우체통 이용<br>" +
                "&nbsp;&nbsp;&nbsp;- <small>(※ 물약은 우체통 배출 불가, 전용 수거함 이용)</small><br><br>" +
                "<b>3. 수은/화학제품</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 유출되지 않도록 밀봉 또는 포장 후 전용 수거함 배출<br><br>" +
                "<b>4. 방사성 제품</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 생활주변방사선 정보서비스 문의 후 측정 결과에 따라 배출" +
                "</div>" +

                "<div class='section-title'>⚠️ 주의사항</div>" +
                "<div class='content-box'>" +
                "• 파손으로 인한 내용물 유출이 없도록 <span class='highlight'>완전 밀봉</span> 필수<br>" +
                "• 전용 수거함이 없는 경우, 밀봉하여 일반 종량제 봉투에 배출(지자체 확인 필요)" +
                "</div>" +
                "</body></html>";
    }
    
    public static String getOtherWasteGuideHtml() {
        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight { color: #ff9d00; font-weight: bold; }" +
                "</style></head><body>" +
                "<div style='font-size: 24px; font-weight: bold; color: #00fff0;'>🌾 기타 (영농폐기물)</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "농업 활동 과정에서 발생하는 폐기물<br><br>" +
                "• <b>영농폐비닐:</b> 하우스 비닐, 로덴 비닐 등<br>" +
                "• <b>폐농약용기:</b> 농약 유리병, 플라스틱 병, 봉지류" +
                "</div>" +

                "<div class='section-title'>🚛 배출 및 수거방법</div>" +
                "<div class='content-box'>" +
                "<b>1. 영농폐비닐</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 이물질(흙, 자갈, 잡초)을 제거하고 재질/색상별로 분류<br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight'>마을 공동집하장</span>에 배출<br><br>" +
                "<b>2. 폐농약용기</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 내용물을 완전히 비운 후 재질별로 구분하여 그물망에 수집<br>" +
                "&nbsp;&nbsp;&nbsp;- <span class='highlight'>마을 공동집하장</span>에 배출<br><br>" +
                "<b>3. 전문기관 수거</b><br>" +
                "&nbsp;&nbsp;&nbsp;- 한국환경공단에서 수거하여 재활용 또는 적정 처리" +
                "</div>" +

                "<div class='section-title'>♻️ 처리과정</div>" +
                "<div class='content-box'>" +
                "<b>STEP 01. 수집</b><br>마을 공동집하장에 모아진 폐기물을 환경공단 운반 차량이 수거<br><br>" +
                "<b>STEP 02. 처리</b><br>폐비닐은 재생 원료로 자원화하며, 폐농약용기는 파쇄 후 재활용" +
                "</div>" +
                "</body></html>";
    }
    public static class ItemDetail {
        public String itemId; 
        public String itemName;
        public String categoryName;
        public String disposalGuide; 

        public ItemDetail(String itemId, String itemName, String categoryName, String disposalGuide) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.categoryName = categoryName;
            this.disposalGuide = disposalGuide;
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("DROP TABLE IF EXISTS " + ITEMS_TABLE); 
            stmt.execute("DROP TABLE IF EXISTS " + CATEGORIES_TABLE); 
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            String createCategoriesSQL = 
                    "CREATE TABLE " + CATEGORIES_TABLE + " (" +
                            "CATEGORY_ID VARCHAR(10) PRIMARY KEY," +
                            "CATEGORY_NAME VARCHAR(50) NOT NULL UNIQUE," +
                            "REWARD_POINTS INT NOT NULL DEFAULT 0)";

            String createItemsSQL = 
                    "CREATE TABLE " + ITEMS_TABLE + " (" +
                            "ITEM_ID VARCHAR(10) PRIMARY KEY," + 
                            "ITEM_NAME VARCHAR(100) NOT NULL UNIQUE," +
                            "CATEGORY_ID VARCHAR(10) NOT NULL," + 
                            "DISPOSAL_GUIDE LONGTEXT NOT NULL," +
                            "FOREIGN KEY (CATEGORY_ID) REFERENCES " + CATEGORIES_TABLE + "(CATEGORY_ID))";

            stmt.execute(createCategoriesSQL);
            stmt.execute(createItemsSQL);
            insertInitialData(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertInitialData(Connection conn) throws SQLException {
   
        String catSql = "INSERT INTO " + CATEGORIES_TABLE + " (CATEGORY_ID, CATEGORY_NAME, REWARD_POINTS) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(catSql)) {
            Object[][] cats = {{"C01", "종이류", 10}, {"C02", "비닐류", 10}, {"C03", "유리병", 20}, {"C04", "종이팩", 15}, {"C05", "캔ㆍ고철", 20}, {"C06", "스티로폼", 10}, {"C07", "플라스틱", 15}, {"C08", "기타", 5}};
            for (Object[] cat : cats) {
                pstmt.setString(1, (String)cat[0]); pstmt.setString(2, (String)cat[1]); pstmt.setInt(3, (Integer)cat[2]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }

        String itemSql = "INSERT INTO " + ITEMS_TABLE + " (ITEM_ID, ITEM_NAME, CATEGORY_ID, DISPOSAL_GUIDE) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
            for (String[] item : ITEMS_FULL_DATA) {
        
                if (item.length < 5) {
                    System.out.println("데이터 형식 오류(항목 부족): " + item[1]);
                    continue; 
                }

                String catId = item[2];
                String[] res = CATEGORY_RESOURCES.get(catId);
                
                if (res == null || res.length < 2) continue;

                try {
                    int markCnt = Integer.parseInt(res[0]);
                    int imgCnt = Integer.parseInt(res[1]);
                    String common = COMMON_GUIDES.getOrDefault(catId, "");
                    String tagClass = item[4]; 
                    String pathPrefix = "";

                    
                 
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("<div class='guide-container'>");
        
                    sb.append("<div class='header-container' style='margin-bottom:30px;'>");

                    if (markCnt > 0) {
            
                        sb.append("<div class='mark-section' style='display:inline-block; vertical-align:middle; margin-right:50px;'>");
                        for(int i = 0; i < markCnt; i++) {
                            if (2 + i < res.length) {
                          
                                sb.append("<img src='").append(pathPrefix).append(res[2 + i])
                                  .append("' width='65' height='65' hspace='1' style='margin-right:15px; border:1px solid #ccc;'>");
                            }
                        }
                        sb.append("</div>");
                    }
                    sb.append("<span style='margin-right:50px;'>&nbsp;</span>"); 
                    sb.append("<div class='image-section' style='display:inline-block; vertical-align:middle;'>");
                    for(int i = 0; i < imgCnt; i++) {
                        if (2 + markCnt + i < res.length) {
                 
                            sb.append("<img src='").append(pathPrefix).append(res[2 + markCnt + i])
                                  .append("' width='160' height='100' hspace='2' style='margin-right:20px; border-radius:8px;'>");
                        }
                    }
                    sb.append("</div></div>");

                    sb.append("<div style='margin-bottom: 40px;'>").append(common).append("</div>");

                    sb.append("<h2 class='guide-title'>🔍 ").append(item[1]).append(" 상세 배출 가이드</h2>");
                    sb.append("<div class='detail-box'>");
                    sb.append("<span class='action-tag ").append(tagClass).append("'>배출방법</span>");
                    sb.append(item[3]);
                    sb.append("</div>");
                    
                    sb.append("</div>"); 

                    pstmt.setString(1, item[0]);
                    pstmt.setString(2, item[1]);
                    pstmt.setString(3, catId);
                    pstmt.setString(4, sb.toString());
                    pstmt.addBatch();
                    
                } catch (Exception e) {
                    System.out.println("아이템 처리 중 오류 발생: " + item[1] + " - " + e.getMessage());
                }
            }
            pstmt.executeBatch();
        }
    }

    public static Map<String, String> getAllCategoryNamesAndIds() throws SQLException {
        Map<String, String> categories = new LinkedHashMap<>();
        String sql = "SELECT CATEGORY_NAME, CATEGORY_ID FROM " + CATEGORIES_TABLE + " ORDER BY CATEGORY_ID ASC";
        try (Connection conn = RecycleDB.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) categories.put(rs.getString("CATEGORY_NAME"), rs.getString("CATEGORY_ID"));
        }
        return categories;
    }

    public static List<String> getItemNamesByCategory(String categoryId) throws SQLException {
        List<String> items = new ArrayList<>();
        String sql = "SELECT ITEM_NAME FROM " + ITEMS_TABLE + " WHERE CATEGORY_ID = ? ORDER BY ITEM_NAME ASC";
        try (Connection conn = RecycleDB.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) items.add(rs.getString("ITEM_NAME"));
            }
        }
        return items;
    }

    public static List<ItemDetail> getAllItems() throws SQLException {
        List<ItemDetail> list = new ArrayList<>();
        String sql = "SELECT i.ITEM_ID, i.ITEM_NAME, c.CATEGORY_NAME, i.DISPOSAL_GUIDE FROM " + ITEMS_TABLE + " i " +
                     "JOIN " + CATEGORIES_TABLE + " c ON i.CATEGORY_ID = c.CATEGORY_ID";
        try (Connection conn = RecycleDB.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ItemDetail(rs.getString("ITEM_ID"), rs.getString("ITEM_NAME"), rs.getString("CATEGORY_NAME"), rs.getString("DISPOSAL_GUIDE")));
            }
        }
        return list;
    }

    public static ItemDetail getItemDetail(String itemName, String categoryName) throws SQLException {
        String sql = "SELECT i.ITEM_ID, i.ITEM_NAME, c.CATEGORY_NAME, i.DISPOSAL_GUIDE FROM " + ITEMS_TABLE + " i " +
                     "JOIN " + CATEGORIES_TABLE + " c ON i.CATEGORY_ID = c.CATEGORY_ID " +
                     "WHERE i.ITEM_NAME = ? AND c.CATEGORY_NAME = ?";
        try (Connection conn = RecycleDB.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemName);
            pstmt.setString(2, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ItemDetail(rs.getString("ITEM_ID"), rs.getString("ITEM_NAME"), rs.getString("CATEGORY_NAME"), rs.getString("DISPOSAL_GUIDE"));
                }
            }
        }
        return null;
    }
    public static String[] getCategoryResources(String itemId) {
        return CATEGORY_RESOURCES.get(itemId);
    }

}