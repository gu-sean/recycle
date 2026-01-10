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
        CATEGORY_RESOURCES.put("C01", new String[]{ "1", "4", "images/paper/mark.png" });
        CATEGORY_RESOURCES.put("C02", new String[]{ "6", "3", "images/Vinyl/mark1.png", "images/Vinyl/mark2.png", "images/Vinyl/mark3.png", "images/Vinyl/mark4.png", "images/Vinyl/mark5.png", "images/Vinyl/mark6.png" });
        CATEGORY_RESOURCES.put("C03", new String[]{ "1", "4", "images/glass_bottle/mark1.png"});
        CATEGORY_RESOURCES.put("C04", new String[]{ "2", "2", "images/paper_pack/mark1.png", "images/paper_pack/mark2.png" });
        CATEGORY_RESOURCES.put("C05", new String[]{ "2", "3", "images/Can/mark1.png", "images/Can/mark2.png"});
        CATEGORY_RESOURCES.put("C06", new String[]{ "0", "3"});
        CATEGORY_RESOURCES.put("C07", new String[]{ "7", "6", "images/plastic/mark1.png", "images/plastic/mark2.png", "images/plastic/mark3.png", "images/plastic/mark4.png", "images/plastic/mark5.png", "images/plastic/mark6.png", "images/plastic/mark7.png" });
        CATEGORY_RESOURCES.put("C08", new String[]{ "0", "3" });
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
    		    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		
    		    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 종이류 </b></p>" +
    		    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		    "      · <span style='color: #ffffff; '>순수 종이 재질</span>의 가격표는 <span style='color: #ffcc00; font-weight: bold;'>종이류</span>로 배출 가능합니다.<br/>" +
    		    "      · 단, 부착된 <span style='color: #ffcc00; font-weight: bold;'>접착 테이프나 스티커</span> 성분은 칼이나 가위로 <span style='color: #ffcc00; font-weight: bold;'>완전히 오려내고</span> 배출해야 합니다." +
    		    "    </div>" +

    		    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 일반 쓰레기</b></p>" +
    		    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		    "      · <span style='color: #ffcc00; font-weight: bold;'>비닐 코팅된 택</span> : 의류 택 중 반짝이는 재질은 재활용이 안 됩니다.<br/>" +
    		    "      · <span style='color: #ffcc00;font-weight: bold;'>라벨지</span> : 뒷면 전체가 접착제인 스티커는 <span style='color: #ffcc00; font-weight: bold;'>종량제 봉투</span>에 버려주세요.<br/>" +
    		    "      · <span style='color: #ffcc00;font-weight: bold;'>플라스틱 줄</span> : 옷과 택을 연결하는 끈은 반드시 제거하여 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 버려야 합니다." +
    		    "    </div>" +

    		    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; color: #ffffff;'>" +
    		    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 손으로 찢었을 때 <span style='color: #ffcc00; font-weight: bold;'>비닐이 늘어나거나 잘 안 찢어지면</span>  코팅된 것이니 일반 쓰레기로 분류하세요." +
    		    "    </div>" +

    		    "  </div>" +
    		    "</div>", 
    		    "images/paper/price_tag.png", "images/paper/mark.png"},
    		{"I121", "꼬깔형 생수컵", "C01", 
    		        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        
    		        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 방법</b></p>" +
    		        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        "      · 사용한 꼬깔형 생수컵은 <span style='color: #ffcc00; font-weight: bold;'>종이류 수거함</span>으로 배출합니다.<br/>" +
    		        "      · 부피를 줄이기 위해 <span style='color: #ffcc00;'>최대한 압착</span>하여 봉투에 넣거나 한데 묶어서 배출해 주세요." +
    		        "    </div>" +

    		        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 품목별 특징</b></p>" +
    		        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        "      · 음수대용 종이컵은 형태에 따라 <span style='color: #ffcc00;'>봉투형, 고깔형, 원기둥형</span> 등이 있습니다.<br/>" +
    		        "      · 재질이 종이로 되어 있어 종류와 관계없이 모두 종이류로 재활용이 가능합니다." +
    		        "    </div>" +

    		        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        "      · 컵 내부에 물 외의 이물질(커피, 음료 등)이 묻어 오염이 심한 경우에는 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기(종량제 봉투)</span>로 배출해야 합니다." +
    		        "    </div>" +

    		        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        
    		        "      <span><b style='color: #00fff0;'>💡 Tip</b> : 위생 봉투컵, 음수대용 종이컵 등</span>" +
    		        "    </div>" +

    		        "  </div>" +
    		        "</div>", 
    		        "images/paper/cone_cup.png", "images/paper/mark.png"},
    		
    		{"I002", "종이상자(박스)", "C01", 
                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
                    
                    "    " +
                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 철핀/스테이플러 제거</b></p>" +
                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
                    "      · 박스를 고정하는 <span style='color: #ffcc00; font-weight: bold;'>철핀이나 구리핀</span>은 선별 작업자에게 상처를 입힐 수 있습니다.<br/>" +
                    "      · 반드시 도구를 이용해 <span style='color: #ffcc00; font-weight: bold;'>핀을 완전히 뽑아낸 후 배출</span>해 주세요." +
                    "    </div>" +

                    "    " +
                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 테이프 및 송장 제거</b></p>" +
                    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
                    "      · 박스 겉면의 <span style='color: #ffcc00; font-weight: bold;'>택배 송장과 비닐 테이프</span>를 모두 떼어내 주세요.<br/>" +
                    "      · 음식물이 묻었거나 은박지/비닐이 합지된 상자는  <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기(종량제)</span>입니다." +
                    "    </div>" +

                    "    " +
                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b;'>" +
                    "       <span style='font-weight: bold; color:ff6b6b; '>⚠️ 주의</span> : 상자는 반드시 <span style=color:ffcc00; font-weight: bold; '> 납작하게 펼쳐서</span> 묶어서 배출해 주세요." +
                    "    </div>" +

                    "  </div>" +
                    "</div>", 
                    "images/paper/box.png", "images/paper/mark.png"},
    		
    		{"I003", "떡메모지", "C01", 
    		            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		            
    		            "    " +
    		            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 종이류 배출</b></p>" +
    		            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		            "      · 이물질이 묻지 않은 순수 종이 재질의 메모지는 <span style='color: #ffcc00; font-weight: bold;'>종이류 수거함</span>으로 배출합니다.<br/>" +
    		            "      · 상단의 제본용 풀(접착제) 성분이 과도하다면 <span style='color: #ffcc00; font-weight: bold;'>해당 부분만 잘라내고 배출하는 것</span>이 좋습니다." +
    		            "    </div>" +

    		            "    " +
    		            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 시 주의사항</b></p>" +
    		            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		            "      · 크기가 작은 종이는 낱개로 버리면 선별 과정에서 유실되기 쉽습니다.<br/>" +
    		            "      · 낱개로 배출할 때는 흩날리지 않도록 <span style='color: #ffcc00; font-weight: bold;'>종이봉투</span>에 담거나 <span style='color: #ffcc00; font-weight: bold;'>신문지 사이</span>에 끼워서 배출하세요." +
    		            "    </div>" +

    		            "    " +
    		            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		            "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 메모지 전체가 접착제인 '포스트잇'은 재활용이 어려우므로 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 분류하는 것이 권장됩니다." +
    		            "    </div>" +

    		          "  </div>" +
    		          "</div>", 
    		          "images/paper/memo_pad.png", "images/paper/mark.png"},
    		
    		{"I004", "랩의 심(종이관)", "C01", 
    		        	    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        	    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        	    
    		        	    "    " +
    		        	    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부속품 제거 후 배출</b></p>" +
    		        	    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        	    "      · 양옆의 <span style='color: #ffcc00; font-weight: bold;'>플라스틱 캡</span>이나 <span style='color: #ffcc00; font-weight: bold;'>금속 톱날</span>은 반드시 분리하여 각각 재질에 맞게 배출하세요.<br/>" +
    		        	    "      · 이물질이 제거된 순수한 종이 심만 <span style='color: #ffcc00; font-weight: bold;'>종이류</span>로 분류합니다." +
    		        	    "    </div>" +

    		        	    "    " +
    		        	    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 팁</b></p>" +
    		        	    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        	    "      · 부피를 많이 차지하므로 발로 밟아 <span style='color: #ffcc00; font-weight: bold;'>납작하게 압착</span>하여 배출하면 수거 효율이 높아집니다.<br/>" +
    		        	    "      · 종이 심 자체가 젖었거나 음식물이 많이 묻었다면 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기 </span>로 버려주세요." +
    		        	    "    </div>" +

    		        	    "    " +
    		        	    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b;'>" +
    		        	    "      · <span style='font-weight: bold; color: #ff6b6b;'>⚠️ 주의</span> : 랩의 날카로운 칼날에 손을 다칠 수 있으니 도구를 사용하여 안전하게 제거하세요." +
    		        	    "    </div>" +

    		        	  "  </div>" +
    		        	  "</div>", 
    		        	  "images/paper/roll_core.png", "images/paper/mark.png"},
    		
    		{"I005", "명함", "C01", 
    		        		    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		    
    		        		    "    " +
    		        		    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재질 확인 후 배출</b></p>" +
    		        		    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		    "      · 순수하게 종이로만 된 일반 명함은 <span style='color: #ffcc00; font-weight: bold;'>종이류</span>로 배출하세요.<br/>" +
    		        		    "      · <span style='color: #ffcc00; font-weight: bold;'>비닐 코팅된 종이나 플라스틱 합성지</span> 명함은 재활용이 불가능합니다." +
    		        		    "    </div>" +

    		        		    "    " +
    		        		    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 일반 쓰레기 품목</b></p>" +
    		        		    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		    "      · <span style='color: #ffcc00; font-weight: bold;'>금박·은박·엠보싱</span> 가공이 들어간 명함은 일반 쓰레기(종량제 봉투)입니다.<br/>" +
    		        		    "      · <span style='color: #ffcc00; font-weight: bold;'>사진 인화지 형태나 비닐 재질의 명함</span>도 반드시 일반 쓰레기로 버려주세요." +
    		        		    "    </div>" +

    		        		    "    " +
    		        		    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		    "      · <span style='font-weight: bold; color: #00fff0; '>💡 Tip</span> : 명함을 찢었을 때 단면에 비닐이 늘어난다면 코팅된 종이이므로 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 분류하세요." +
    		        		    "    </div>" +

    		        		    "  </div>" +
    		        		    "</div>", 
    		        		    "images/paper/business_card.png", "images/paper/mark.png"},
    		
    	
    		{"I006", "사전", "C01", 
    		        		            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		            
    		        		            "    " +
    		        		            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 이물질 커버 제거</b></p>" +
    		        		            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		            "      · 가죽, 인조 가죽(레더), 또는 플라스틱 소재의 <span style='color: #ffcc00; font-weight: bold;'>외부 커버는 반드시 벗겨서 일반 쓰레기</span>로 버려주세요.<br/>" +
    		        		            "      · 커버를 완전히 제거한 안쪽의 <span style='color: #ffcc00; font-weight: bold;'>종이 뭉치만 종이류</span>로 배출해야 합니다." +
    		        		            "    </div>" +

    		        		            "    " +
    		        		            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 추가 분리 항목</b></p>" +
    		        		            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		            "      · 사전 옆면에 붙은 <span style='color: #ffcc00; font-weight: bold;'>플라스틱 인덱스 탭이나 스티커</span>는 가급적 제거해 주세요.<br/>" +
    		        		            "      · 제본 부분에 사용된 <span style='color: #ffcc00; font-weight: bold;'>두꺼운 본드나 실 뭉치</span>는 일반 쓰레기로 분류하는 것이 가장 깨끗합니다." +
    		        		            "    </div>" +

    		        		            "    " +
    		        		            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		            "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 종이가 너무 얇아 흩어지기 쉬우므로, 커버를 벗긴 후에는 <span style='color: #ffcc00; font-weight: bold;'>노끈으로 단단히 묶어서</span> 배출하세요." +
    		        		            "    </div>" +

    		        		            "  </div>" +
    		        		            "</div>", 
    		        		            "images/paper/dictionary.png", "images/paper/mark.png"},
    		{"I007", "쌀포대", "C01", 
    		        		                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                
    		        		                "    " +
    		        		                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재질 확인 및 오염 제거</b></p>" +
    		        		                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                "      · 내부에 <span style='color: #ffcc00; font-weight: bold;'>비닐 코팅</span>이 되어 있는 쌀포대는 재활용이 불가능하므로 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 버려주세요.<br/>" +
    		        		                "      · 순수 종이 재질인 경우, 안쪽의 잔여 쌀가루나 이물질을 <span style='color: #ffcc00; font-weight: bold;'>완전히 털어낸 후 배출</span>해야 합니다." +
    		        		                "    </div>" +

    		        		                "    " +
    		        		                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 부속품 제거</b></p>" +
    		        		                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                "      · 입구를 봉인했던 <span style='color: #ffcc00; font-weight: bold;'>실 뭉치나 플라스틱 손잡이</span>는 반드시 가위로 잘라 별도로 배출하세요.<br/>" +
    		        		                "      · 종이 포대가 젖었거나 기름기 등 오염이 심하다면 재활용이 되지 않습니다." +
    		        		                "    </div>" +

    		        		                "    " +
    		        		                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 포대를 살짝 찢었을 때 비닐막이 보인다면 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기(종량제 봉투)</span>입니다." +
    		        		                "    </div>" +

    		        		                "  </div>" +
    		        		                "</div>", 
    		        		                "images/paper/rice_sack.png", "images/paper/mark.png"},
    		{"I008", "서류봉투", "C01", 
    		        		                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                    
    		        		                    "    " +
    		        		                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 비닐 창 및 이물질 제거</b></p>" +
    		        		                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                    "      · 주소 확인용 <span style='color: #ffcc00; font-weight: bold;'>투명 비닐(창봉투)</span>은 반드시 손으로 뜯어내어 <span style='color: #ffcc00; font-weight: bold;'>비닐류</span>로 따로 버려주세요.<br/>" +
    		        		                    "      · 봉투에 붙은 우표, 스티커, 테이프 등 종이가 아닌 재질은 <span style='color: #ffcc00; font-weight: bold;'>모두 제거</span>해야 합니다." +
    		        		                    "    </div>" +

    		        		                    "    " +
    		        		                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 접착제 부분 처리</b></p>" +
    		        		                    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                    "      · 입구의 <span style='color: #ffcc00; font-weight: bold;'>양면테이프나 풀</span>이 묻은 부분은 가급적 가위로 잘라내어 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 버려주세요.<br/>" +
    		        		                    "      · <span style='color: #ffcc00; font-weight: bold;'>금속 집게(클립)나 스테이플러 심</span>이 박혀 있다면 반드시 제거 후 배출합니다." +
    		        		                    "    </div>" +

    		        		                    "    " +
    		        		                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                    "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 내부가 뽁뽁이(에어캡)로 된 서류봉투는 종이로 재활용되지 않으니 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 버려야 합니다." +
    		        		                    "    </div>" +

    		        		                  "  </div>" +
    		        		                  "</div>", 
    		        		                    "images/paper/envelope.png", "images/paper/mark.png"},
    		{"I009", "수첩", "C01", 
    		        		                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                        
    		        		                        "    " +
    		        		                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재질별 3단계 분해</b></p>" +
    		        		                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                        "      1. <span style='color: #ffcc00; font-weight: bold;'>스프링 제거</span> : 금속이나 플라스틱 스프링은 모두 빼내어 <span style='color: #ffcc00; font-weight: bold;'>재질별(고철/플라스틱)</span>로 분리하세요.<br/>" +
    		        		                        "      2. <span style='color: #ffcc00; font-weight: bold;'>비닐 표지 제거</span> : 코팅된 단단한 표지나 비닐 커버는 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 버려주세요.<br/>" +
    		        		                        "      3️. <span style='color: #ffcc00; font-weight: bold;'>종이 배출</span> : 순수한 종이 속지만 모아서 종량제 봉투가 아닌 <span style='color: #ffcc00; font-weight: bold;'>종이류</span>에 배출합니다." +
    		        		                        "    </div>" +

    		        		                        "    " +
    		        		                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 시 주의사항</b></p>" +
    		        		                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                        "      · 스프링을 제거하기 힘들다면 통째로 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기(종량제 봉투)</span>로 버려야 합니다.<br/>" +
    		        		                        "      · 수첩에 붙은 메모지(포스트잇)는 접착제 성분 때문에 일반 쓰레기로 분류하는 것이 좋습니다." +
    		        		                        "    </div>" +

    		        		                        "    " +
    		        		                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                        "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 스프링 수첩은 그대로 버리면 재활용 불가입니다. <span style='color: #ffcc00; font-weight: bold;'>반드시 분해 작업이 필요</span>합니다." +
    		        		                        "    </div>" +

    		        		                        "  </div>" +
    		        		                        "</div>", 
    		        		                        "images/paper/notebook.png", "images/paper/mark.png"},
    		{"I010", "신문지", "C01", 
    		        		                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                            
    		        		                            "    " +
    		        		                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 습기 방지 및 정리</b></p>" +
    		        		                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                            "      · 신문지는 한 장씩 반듯하게 펴서 차곡차곡 쌓아주세요.<br/>" +
    		        		                            "      · 습기에 매우 취약하므로 <span style='color: #ffcc00; font-weight: bold;'>비 오는 날 배출은 가급적 피하고</span>, <span style='color: #ffcc00; font-weight: bold;'>젖은 상태라면 말려서 배출</span>해야 합니다." +
    		        		                            "    </div>" +

    		        		                            "    " +
    		        		                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 흩날림 방지</b></p>" +
    		        		                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                            "      · 바람에 흩날리지 않도록 <span style='color: #ffcc00; font-weight: bold;'>끈(노끈)으로 십자 형태</span>로 단단히 묶어서 배출하세요.<br/>" +
    		        		                            "      · 신문지 사이에 끼어 있는 전단지(코팅된 종이), 광고 책자, 비닐 등은 <span style='color: #ffcc00; font-weight: bold;'>반드시 따로 분리</span>해야 합니다." +
    		        		                            "    </div>" +

    		        		                            "    " +
    		        		                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b;'>" +
    		        		                            "      · <span style='font-weight: bold; color:#ff6b6b;'>⚠️ 주의</span> : 비닐 코팅된 전단지나 이물질이 묻은 신문은 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 분류하세요." +
    		        		                            "    </div>" +

    		        		                            "  </div>" +
    		        		                            "</div>", 
    		        		                            "images/paper/newspaper.png", "images/paper/mark.png"},
    		{"I405", "계란판", "C01", 
    		        		                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                
    		        		                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 종이(펄프) 계란판 배출</b></p>" +
    		        		                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                "      · 종이 재질의 계란판은 <span style='color: #ffcc00; font-weight: bold;'>종이류 수거함</span>에 배출합니다.<br/>" +
    		        		                                "      · 계란 껍데기 등 이물질이 묻지 않도록 주의하고, 깨끗한 상태로 배출해 주세요." +
    		        		                                "    </div>" +

    		        		                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 플라스틱 계란판 배출</b></p>" +
    		        		                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                "      · 투명한 플라스틱 재질은 <span style='color: #ffcc00; font-weight: bold;'>플라스틱(PET) 수거함</span>에 배출합니다.<br/>" +
    		        		                                "      · 가급적 부착된 상표 스티커를 제거하여 분리배출 하시기 바랍니다." +
    		        		                                "    </div>" +

    		        		                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
    		        		                                "      <b style='color: #4ebfad;'>💡 Tip</b><br/>" +
    		        		                                "      <span style='font-size: 13px; color: #ffffff;'>" +
    		        		                                "        종이 계란판은 이미 한 번 재활용된 펄프인 경우가 많아 다시 <b style='color: #ffcc00;'>신문지나 종이 완충재</b> 등으로 재탄생하며, 플라스틱 계란판은 고품질 재생 원료가 될 수 있습니다." +
    		        		                                "      </span>" +
    		        		                                "    </div>" +

    		        		                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 계란 껍데기는 재활용이 되지 않는 <b style='color: #ffcc00;'>일반 쓰레기(종량제)</b>입니다. 계란판에 섞이지 않도록 따로 분리해 주세요.</span>" +
    		        		                                "    </div>" +

    		        		                                "  </div>" +
    		        		                                "</div>", 
    		        		                                "images/Paper/egg_tray.png", "images/paper/mark.png"},
    		{"I011", "잡지", "C01", 
    		        		                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                
    		        		                                "    " +
    		        		                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 코팅 표지 및 부록 제거</b></p>" +
    		        		                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                "      · 잡지 표지는 대부분 <span style='color: #ffcc00; font-weight: bold;'>비닐 코팅</span>이 되어 있으므로, 뜯어내어 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 버려주세요.<br/>" +
    		        		                                "      · 내부에 포함된 CD, 화장품 샘플, 비닐 전단지 등 <span style='color: #ffcc00; font-weight: bold;'>종이가 아닌 부록</span>은 반드시 제거해야 합니다." +
    		        		                                "    </div>" +

    		        		                                "    " +
    		        		                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 제본 부분 처리</b></p>" +
    		        		                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                "      · 본드로 단단히 고정된 잡지의 책등(제본 부위)은 가급적 칼로 잘라내고 <span style='color: #ffcc00; font-weight: bold;'>깨끗한 내지만 배출</span>하는 것이 가장 좋습니다.<br/>" +
    		        		                                "      · <span style='color: #ffcc00; font-weight: bold;'>스테이플러 심</span>이 박혀 있다면 모두 제거 후 종이류로 분류하세요." +
    		        		                                "    </div>" +

    		        		                                "    " +
    		        		                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 찢었을 때 비닐이 늘어나며 찢어지는 종이는 재활용이 안 되는 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>입니다." +
    		        		                                "    </div>" +

    		        		                                "  </div>" +
    		        		                                "</div>", 
    		        		                                "images/paper/magazine.png", "images/paper/mark.png"},
    		{"I012", "전단지", "C01", 
    		        		                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                    
    		        		                                    "    " +
    		        		                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 코팅 여부 확인법</b></p>" +
    		        		                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                    "      · 전단지를 살짝 찢어보세요. 단면에서 <span style='color: #ffcc00; font-weight: bold;'>비닐 막</span>이 늘어난다면 재활용이 불가능한 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>입니다.<br/>" +
    		        		                                    "      · 겉면이 지나치게 매끄럽거나 광택이 심한 종이는 대부분 코팅된 종이입니다." +
    		        		                                    "    </div>" +

    		        		                                    "    " +
    		        		                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 장소 선택</b></p>" +
    		        		                                    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                    "      · <span style='color: #ffcc00; font-weight: bold;'>일반 종이 전단지</span>: 종량제 봉투가 아닌 <span style='color: #ffcc00; font-weight: bold;'>종이류</span> 분리수거함으로 배출하세요.<br/>" +
    		        		                                    "      · <span style='color: #ffcc00; font-weight: bold;'>코팅/비닐 전단지</span>: 종이 재질을 방해하는 원인이 되므로 반드시 <span style='font-weight: bold; color: #ffcc00;'>종량제 봉투</span>에 버려야 합니다." +
    		        		                                    "    </div>" +

    		        		                                    "    " +
    		        		                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                    "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 신문 사이에 꽂혀 오는 전단지들을 한데 모아 <span style='color: #ffcc00; font-weight: bold;'>코팅된 것</span>만 골라내는 습관이 필요합니다." +
    		        		                                    "    </div>" +

    		        		                                    "  </div>" +
    		        		                                    "</div>", 
    		        		                                    "images/paper/flyer.png", "images/paper/mark.png"},
    		{"I013", "전화번호부/두꺼운 책", "C01", 
    		        		                                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                        
    		        		                                        "    " +
    		        		                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 제본 본드 제거</b></p>" +
    		        		                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                        "      · 책등(세로면)에 발라진 <span style='color: #ffcc00; font-weight: bold;'>딱딱한 접착제 부위</span>를 칼로 잘라내 주세요.<br/>" +
    		        		                                        "      · 본드는 종이 재생 과정에서 녹지 않는 이물질로 분류되어 품질을 떨어뜨립니다." +
    		        		                                        "    </div>" +

    		        		                                        "    " +
    		        		                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 표지 및 이물질 확인</b></p>" +
    		        		                                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                        "      · <span style='color: #ffcc00; font-weight: bold;'>비닐 코팅 표지</span>: 종이와 잘 분리되지 않는다면 <span style='color: #ffcc00; font-weight: bold;'>표지만 떼어 일반 쓰레기</span>로 버리세요.<br/>" +
    		        		                                        "      · <span style='color: #ffcc00; font-weight: bold;'>부착물 제거</span>: 책 중간의 인덱스 스티커나 CD 등은 <span style='color: #ffcc00; font-weight: bold;'>반드시 제거</span>해야 합니다." +
    		        		                                        "    </div>" +

    		        		                                        "    " +
    		        		                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                        "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 한 번에 자르기 힘들다면 여러 번에 나누어 칼질하여 본드 부위만 쏙 뽑아내세요!" +
    		        		                                        "    </div>" +

    		        		                                        "  </div>" +
    		        		                                        "</div>", 
    		        		                                        "images/paper/phonebook.png", "images/paper/mark.png"},
    		{"I014", "달력(탁상/벽걸이)", "C01", 
    		        		                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                            
    		        		                                            "    " +
    		        		                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 스프링 및 비닐 제거</b></p>" +
    		        		                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                            "      · 탁상달력의 <span style='color: #ffcc00; font-weight: bold;'>플라스틱/철제 스프링</span>은 종이가 아닙니다.<br/>" +
    		        		                                            "      · 스프링을 <span style='color: #ffcc00; font-weight: bold;'>완전히 분리</span>하여 고철이나 플라스틱으로 각각 배출해 주세요." +
    		        		                                            "    </div>" +

    		        		                                            "    " +
    		        		                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 종이 부분 배출 방법</b></p>" +
    		        		                                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                            "      · <span style='color: #ffcc00; font-weight: bold;'>일반 종이</span>: 스프링을 제거한 종이 부분은 <span style='color: #ffcc00; font-weight: bold;'>종이류</span>로 배출하세요.<br/>" +
    		        		                                            "      · <span style='color: #ffcc00; font-weight: bold;'>코팅된 종이</span>: 비닐 코팅이 된 화려한 페이지는 재활용이 어려우니 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 분류합니다." +
    		        		                                            "    </div>" +

    		        		                                            "    " +
    		        		                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                            "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 달력 지지대(두꺼운 판판한 종이)도 이물질이 없다면 종이로 배출 가능합니다." +
    		        		                                            "    </div>" +

    		        		                                            "  </div>" +
    		        		                                            "</div>", 
    		        		                                            "images/paper/calendar.png", "images/paper/mark.png"},
    	
    		{"I015", "책/서적", "C01", 
    		        		                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                    
    		        		                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 이물질 및 커버 제거</b></p>" +
    		        		                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                    "      · 딱딱한 <span style='color: #ffcc00; font-weight: bold;'>양장본 표지나 비닐 코팅된 커버</span>는 분리하여 일반 쓰레기로 배출하세요.<br/>" +
    		        		                                                    "      · 책에 붙은 스티커, 테이프, 메모지 등 종이가 아닌 재질은 <span style='color: #ffcc00; font-weight: bold;'>모두 제거</span>해야 합니다." +
    		        		                                                    "    </div>" +

    		        		                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 부록 및 제본 장치 분리</b></p>" +
    		        		                                                    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                    "      · <span style='color: #ffcc00; font-weight: bold;'>부록 분리</span> : CD, DVD, 플라스틱 장난감 등 부록은 <span style='color: #ffcc00; font-weight: bold;'>반드시 재질별로 분류</span>해 주세요.<br/>" +
    		        		                                                    "      · <span style='color: #ffcc00; font-weight: bold;'>스프링 제거</span> : 스프링 제본된 책은 철제나 플라스틱 스프링을 <span style='color: #ffcc00; font-weight: bold;'>분리한 후 종이만 배출</span>합니다." +
    		        		                                                    "    </div>" +

    		        		                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                                    "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 여러 권을 배출할 때는 흩어지지 않게 <span style='color: #ffcc00; font-weight: bold;'>끈으로 묶거나 상자에 담아주세요!</span>" +
    		        		                                                    "    </div>" +

    		        		                                                    "  </div>" +
    		        		                                                    "</div>", 
    		        		                                                    "images/paper/book.png", "images/paper/mark.png"},
    		{"I016", "치킨박스", "C01", 
    		        		                                                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                        
    		        		                                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 오염 부위 확인 및 제거</b></p>" +
    		        		                                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                        "      · <span style='color: #ffcc00; font-weight: bold;'>기름기나 양념이 묻은 바닥면</span>은 종이로 재활용이 불가능하므로 반드시 잘라내어 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 버려주세요.<br/>" +
    		        		                                                        "      · 음식물 찌꺼기가 남지 않도록 <span style='color: #ffcc00; font-weight: bold;'>깨끗이 비우는 것</span>이 중요합니다." +
    		        		                                                        "    </div>" +

    		        		                                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 깨끗한 부분만 배출</b></p>" +
    		        		                                                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                        "      · <span style='color: #ffcc00; font-weight: bold;'>종이류 배출</span> : <span style='color: #ffcc00; font-weight: bold;'>오염되지 않은 </span> 윗면이나 측면 박스만 종이로 분리하여 배출하세요.<br/>" +
    		        		                                                        "      · <span style='color: #ffcc00; font-weight: bold;'>이물질 제거</span> : 박스에 붙은 테이프나 영수증, 비닐 등은 <span style='color: #ffcc00; font-weight: bold;'>모두 제거 </span>해야 합니다." +
    		        		                                                        "    </div>" +

    		        		                                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                                        "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 오염이 너무 심해 분리가 어렵다면 <span style='color: #ffcc00; font-weight: bold;'>전체를 일반 쓰레기(종량제 봉투)</span>로 배출해 주세요!" +
    		        		                                                        "    </div>" +

    		        		                                                        "  </div>" +
    		        		                                                        "</div>", 
    		        		                                                        "images/paper/chicken_box.png", "images/paper/mark.png"},
    	  
    		{"I017", "피자박스", "C01", 
    		        		                                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                            
    		        		                                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 오염된 바닥면 분리</b></p>" +
    		        		                                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                            "      · <span style='color: #ffcc00; font-weight: bold;'>기름과 소스가 밴 바닥 부분</span>은 재활용이 불가능하므로 잘라내어 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기(종량제 봉투)</span>로 버려주세요.<br/>" +
    		        		                                                            "      · <span style='color: #ffcc00; font-weight: bold;'>오염되지 않은 깨끗한</span> 윗부분만 종이류로 배출합니다." +
    		        		                                                            "    </div>" +

    		        		                                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 이물질 및 플라스틱 제거</b></p>" +
    		        		                                                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                            "      · <span style='color: #ffcc00; font-weight: bold;'>고정 핀 제거</span>: 피자를 고정하는 플라스틱 핀(삼발이)은 <span style='color: #ffcc00; font-weight: bold;'>반드시 분리</span>하여 플라스틱으로 배출하세요.<br/>" +
    		        		                                                            "      · <span style='color: #ffcc00; font-weight: bold;'>테이프 제거</span>: 박스 외부에 붙은 테이프나 영수증 스티커는 <span style='color: #ffcc00; font-weight: bold;'>모두 떼어내야 합니다</span>." +
    		        		                                                            "    </div>" +

    		        		                                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                                            "      · <span style='font-weight: bold;'>💡 Tip</span> : 피자 박스 안의 <span style='color: #ffcc00; font-weight: bold;'>은박지(알루미늄 호일)</span>는 재활용되지 않으니 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 버려주세요!" +
    		        		                                                            "    </div>" +

    		        		                                                            "  </div>" +
    		        		                                                            "</div>", 
    		        		                                                            "images/paper/pizza_box.png", "images/paper/mark.png"},
    	     // [비닐류 - C02] (I023 ~ I032) 보강 버전
    		{"I018", "과자/라면 봉지", "C02", 
    		        		                                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                                
    		        		                                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 내부 세척 및 비우기</b></p>" +
    		        		                                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                "      · 봉지 안의 <span style='color: #ffcc00; font-weight: bold;'>과자 부스러기나 스프 가루</span>를 완전히 비우고 물로 살짝 헹궈주세요.<br/>" +
    		        		                                                                "      · 이물질이 제거되지 않는 <span style='color: #ffcc00; font-weight: bold;'>오염된 봉지는 일반 쓰레기</span>로 버려야 합니다." +
    		        		                                                                "    </div>" +

    		        		                                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 딱지 접기 금지 (납작하게 배출)</b></p>" +
    		        		                                                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                "      · <span style='color: #ffcc00; font-weight: bold;'>공기 선별기 문제</span> : 딱지 모양은 무게가 무거워져 선별장의 기계가 비닐로 인식하지 못하고 떨어뜨립니다.<br/>" +
    		        		                                                                "      · <span style='color: #ffcc00; font-weight: bold;'>올바른 방법</span> : 가위로 잘라 펼치거나, 부피를 줄이려면 납작하게 접어서 <span style='color: #ffcc00; font-weight: bold;'>비닐류</span>로 배출하세요." +
    		        		                                                                "    </div>" +

    		        		                                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                                                "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 겉면의 비닐류(OTHER) 표시를 확인하고, 스티커 등이 붙어있다면 제거 후 배출해 주세요." +
    		        		                                                                "    </div>" +

    		        		                                                                "  </div>" +
    		        		                                                                "</div>", 
    		        		                                                                "images/vinyl/snack_bag.png", "images/vinyl/mark6.png"},
    		{"I019", "양파망", "C02", 
    		        		                                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                                    
    		        		                                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부속품 제거</b></p>" +
    		        		                                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                    "      · 입구를 조이고 있는 <span style='color: #ffcc00; font-weight: bold;'>플라스틱 고정 장치</span>와 <span style='color: #ffcc00; font-weight: bold;'>종이 라벨</span>은 반드시 칼로 잘라 제거하세요.<br/>" +
    		        		                                                                    "      · 순수한 합성수지 망사 재질만 비닐류로 배출 가능합니다." +
    		        		                                                                    "    </div>" +

    		        		                                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 방법 엄수</b></p>" +
    		        		                                                                    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                    "      · <span style='color: #ffcc00; font-weight: bold;'>내용물 비우기</span> : 망 안에 다른 쓰레기를 넣어 배출하는 행위는 재활용을 방해하므로 절대 금지입니다.<br/>" +
    		        		                                                                    "      · <span style='color: #ffcc00; font-weight: bold;'>비닐류 통합 배출</span> : 깨끗하게 비운 양파망은 비닐류 수거함에 함께 넣어주세요." +
    		        		                                                                    "    </div>" +

    		        		                                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                                                    "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 양파망은 비닐 재질이지만 'OTHER'로 분류되어 SRF(고형연료) 등으로 재활용됩니다." +
    		        		                                                                    "    </div>" +

    		        		                                                                    "  </div>" +
    		        		                                                                    "</div>", 
    		        		                                                                    "images/vinyl/onion_net.png", "images/vinyl/mark6.png"},

    		{"I020", "일회용 비닐장갑", "C02", 
    		        		                                                                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                                        
    		        		                                                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 오염 상태 확인</b></p>" +
    		        		                                                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                        "      · 요리 시 사용한 장갑에 <span style='color: #ffcc00; font-weight: bold;'>기름기나 양념</span>이 많이 묻어있나요?<br/>" +
    		        		                                                                        "      · 이물질이 묻은 비닐은 재활용 품질을 떨어뜨리는 주요 원인입니다." +
    		        		                                                                        "    </div>" +

    		        		                                                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 기준 결정</b></p>" +
    		        		                                                                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                        "      · <span style='color: #ffcc00; font-weight: bold;'>비닐류 배출</span> : 물로 헹궈서 깨끗해지거나 오염이 없는 상태일 때만 가능합니다.<br/>" +
    		        		                                                                        "      · <span style='color: #ffcc00; font-weight: bold;'>일반쓰레기 배출</span> : 양념, 고기 기름 등이 배어 세척이 어려운 경우는 종량제 봉투에 버려주세요." +
    		        		                                                                        "    </div>" +

    		        		                                                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b;'>" +
    		        		                                                                        "      · <span style='font-weight: bold; color: #ff6b6b;'>⚠️ 주의</span> : <span style='color: #ffcc00; font-weight: bold;'>염분이 많은 김치 양념 등이 묻은 비닐</span>은 다른 비닐의 재활용까지 방해하므로 주의가 필요합니다." +
    		        		                                                                        "    </div>" +

    		        		                                                                      "  </div>" +
    		        		                                                                        "</div>", 
    		        		                                                                        "images/vinyl/plastic_glove.png", "images/vinyl/mark4.png"},
    		{"I021", "라면 스프 봉지", "C02", 
    		        		                                                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                                            
    		        		                                                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 염분 및 가루 제거</b></p>" +
    		        		                                                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                            "      · 스프 가루의 <span style='color: #ffcc00; font-weight: bold;'>염분</span>은 비닐 재생 원료의 품질을 크게 떨어뜨리는 주범입니다.<br/>" +
    		        		                                                                            "      · 가루를 완전히 털어내고, 가급적 물로 내부를 가볍게 헹궈주세요." +
    		        		                                                                            "    </div>" +

    		        		                                                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 불가 기준</b></p>" +
    		        		                                                                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                            "      · <span style='color: #ffcc00; font-weight: bold;'>일반쓰레기 배출</span> : 물로 헹궈도 내부의 <span style='color: #ffcc00; font-weight: bold;'>빨간 양념 오염</span>이 지워지지 않는다면 종량제 봉투에 버려야 합니다.<br/>" +
    		        		                                                                            "      · 건더기 스프 봉지도 동일하게 이물질이 없는 상태로만 비닐류 배출이 가능합니다." +
    		        		                                                                            "    </div>" +

    		        		                                                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                                                            "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 크기가 작은 스프 봉지는 분리배출 시 흩어지기 쉬우므로, 큰 라면 봉지 안에 모아서 버리는 것도 좋은 방법입니다." +
    		        		                                                                            "    </div>" +

    		        		                                                                            "  </div>" +
    		        		                                                                            "</div>", 
    		        		                                                                            "images/vinyl/ramen_soup.png", "images/vinyl/mark6.png"},
    		{"I022", "필름류(빵 포장지)", "C02", 
    		        		                                                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                                                
    		        		                                                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 이물질 및 스티커 제거</b></p>" +
    		        		                                                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                "      · 빵 포장지 등에 붙은 <span style='color: #ffcc00; font-weight: bold;'>가격표 스티커나 투명 테이프</span>는 반드시 제거해야 합니다.<br/>" +
    		        		                                                                                "      · 접착제 성분은 재활용 품질을 떨어뜨리므로, 스티커 부위를 가위로 잘라내는 것이 가장 좋습니다." +
    		        		                                                                                "    </div>" +

    		        		                                                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 가능 품목 확인</b></p>" +
    		        		                                                                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                "      · <span style='color: #ffcc00; font-weight: bold;'>해당 품목</span> : 과채류 포장 필름, 과자 겉포장 비닐, 빵 봉투 등 투명하고 얇은 비닐류<br/>" +
    		        		                                                                                "      · <span style='color: #ffcc00; font-weight: bold;'>주의 사항</span> : 음식물이 묻어 있거나 기름기가 있는 필름은 반드시 일반 쓰레기로 버려주세요." +
    		        		                                                                                "    </div>" +

    		        		                                                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                                                                "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 아주 얇은 비닐들도 모이면 소중한 자원이 됩니다. 흩날리지 않게 큰 비닐에 차곡차곡 모아 배출하세요." +
    		        		                                                                                "    </div>" +

    		        		                                                                                "  </div>" +
    		        		                                                                                "</div>", 
    		        		                                                                                "images/vinyl/film_packaging.png", "images/vinyl/mark6.png"},
    		{"I023", "택배용 비닐 봉투", "C02", 
    		        		                                                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                                                    
    		        		                                                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부착물 완전 제거</b></p>" +
    		        		                                                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                    "      · <span style='color: #ffcc00; font-weight: bold;'>운송장 스티커</span> : 개인정보가 담긴 종이 스티커는 비닐 재활용을 방해하므로 반드시 떼어내야 합니다.<br/>" +
    		        		                                                                                    "      · <span style='color: #ffcc00; font-weight: bold;'>접착 테이프</span> : 봉투 입구의 강력한 접착제 부분도 함께 제거해 주세요." +
    		        		                                                                                    "    </div>" +

    		        		                                                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 방법 및 팁</b></p>" +
    		        		                                                                                    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                    "      · <span style='color: #ffcc00; font-weight: bold;'>가위 사용</span>: 스티커나 테이프가 잘 떨어지지 않는다면 해당 부위만 가위로 오려낸 후 배출하세요.<br/>" +
    		        		                                                                                    "      · <span style='color: #ffcc00; font-weight: bold;'>오염 확인</span>: 내부가 오염되었거나 음식물이 묻은 택배 봉투는 일반 쓰레기로 처리해야 합니다." +
    		        		                                                                                    "    </div>" +

    		        		                                                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b;'>" +
    		        		                                                                                    "      · <span style='font-weight: bold; color: #ff6b6b;'>⚠️ 주의</span> : 은박 코팅이 된 보냉 택배 봉투는 재질이 다르므로 별도의 배출 지침을 확인해야 합니다." +
    		        		                                                                                    "    </div>" +

    		        		                                                                                  "  </div>" +
    		        		                                                                                  "</div>", 
    		        		                                                                                  "images/vinyl/shipping_polybag.png", "images/vinyl/mark6.png"},
    		{"I024", "한약/즙 파우치", "C02", 
    		        		                                                                                	    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                                                	    
    		        		                                                                                	    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 내부 세척 및 건조</b></p>" +
    		        		                                                                                	    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	    "      · 한약이나 과일즙은 당분이 많아 곰팡이가 생기기 쉽습니다. <span style='color: #ffcc00; font-weight: bold;'>가위로 입구를 크게 절개</span>하여 내부를 물로 깨끗이 헹궈주세요.<br/>" +
    		        		                                                                                	    "      · 이물질이 남으면 재활용 수거함 전체를 오염시키므로 반드시 말려서 배출해야 합니다." +
    		        		                                                                                	    "    </div>" +

    		        		                                                                                	    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재질 확인 및 배출</b></p>" +
    		        		                                                                                	    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	    "      · <span style='color: #ffcc00; font-weight: bold;'>복합 재질(OTHER)</span>: 내부가 은색 알루미늄으로 되어 있더라도 '비닐류' 마크가 있다면 재활용이 가능합니다.<br/>" +
    		        		                                                                                	    "      · <span style='color: #ffcc00; font-weight: bold;'>주의 사항</span>: 빨대가 꽂혀 있던 구멍 주변의 비닐이 심하게 오염되었거나 끈적임이 남았다면 일반 쓰레기로 버려주세요." +
    		        		                                                                                	    "    </div>" +

    		        		                                                                                	    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                                                                	    "      · <span style='font-weight: bold; color: #00fff0;'>💡 Tip</span> : 부피를 줄이기 위해 잘 헹군 파우치를 차곡차곡 포개서 배출하면 수거 효율이 높아집니다." +
    		        		                                                                                	    "    </div>" +

    		        		                                                                                	    "  </div>" +
    		        		                                                                                	    "</div>", 
    		        		                                                                                	    "images/vinyl/juice_pouch.png", "images/vinyl/mark6.png"},
    		{"I025", "뽁뽁이(에어캡)", "C02", 
    		        		                                                                                	        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                                                	        
    		        		                                                                                	        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 이물질 제거 최우선</b></p>" +
    		        		                                                                                	        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	        "      · <span style='color: #ffcc00; font-weight: bold;'>테이프 및 송장</span>: 비닐 표면에 붙은 박스 테이프나 택배 종이 송장은 반드시 칼이나 가위로 오려내야 합니다.<br/>" +
    		        		                                                                                	        "      · 종이나 이물질이 묻은 채로 배출되면 재활용 공정에서 불량의 원인이 됩니다." +
    		        		                                                                                	        "    </div>" +

    		        		                                                                                	        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 팁</b></p>" +
    		        		                                                                                	        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	        "      · <span style='color: #ffcc00; font-weight: bold;'>공기 배출</span>: 에어캡의 공기를 일일이 터뜨릴 필요는 없으나, 부피를 줄이기 위해 꾹 눌러서 배출하면 좋습니다.<br/>" +
    		        		                                                                                	        "      · <span style='color: #ffcc00; font-weight: bold;'>색상 확인</span>: 투명한 에어캡 외에 유색(파란색, 은색 등) 에어캡도 '비닐류' 마크가 있다면 함께 배출 가능합니다." +
    		        		                                                                                	        "    </div>" +

    		        		                                                                                	        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b;'>" +
    		        		                                                                                	        "      · <span style='font-weight: bold; color: #ff6b6b;'>⚠️ 주의</span> : 오염이 심해 닦이지 않거나 식품 이물질이 묻은 에어캡은 종량제 봉투(일반쓰레기)로 버려주세요." +
    		        		                                                                                	        "    </div>" +

    		        		                                                                                	        "  </div>" +
    		        		                                                                                	        "</div>", 
    		        		                                                                                	        "images/vinyl/bubble_wrap.png", "images/vinyl/mark4.png"},
    		{"I026", "아이스팩 비닐", "C02", 
    		        		                                                                                	            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                                                	            
    		        		                                                                                	            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 친환경(물) 아이스팩</b></p>" +
    		        		                                                                                	            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	            "      · <span style='color: #ffcc00; font-weight: bold;'>배출 방법</span>: 가위로 잘라 물을 버린 후, 외부 비닐은 물기를 말려 '비닐류'로 배출하세요.<br/>" +
    		        		                                                                                	            "      · <span style='color: #ffffff;'>포장지가 종이 재질인 경우에도 내부는 비닐 코팅이 되어 있어 비닐로 분류되는 경우가 많습니다.</span>" +
    		        		                                                                                	            "    </div>" +

    		        		                                                                                	            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 일반(젤) 아이스팩</b></p>" +
    		        		                                                                                	            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	            "      · <span style='color: #ffcc00; font-weight: bold;'>재활용 불가</span>: 고분자화합물(젤)이 든 팩은 비닐로 재활용할 수 없습니다.<br/>" +
    		        		                                                                                	            "      · <span style='color: #ffcc00; font-weight: bold;'>처리 방법</span>: 뜯지 말고 <span style='color: #ffcc00; font-weight: bold;'>통째로 종량제 봉투</span>에 담아 버리거나 전용 수거함에 배출하세요." +
    		        		                                                                                	            "    </div>" +

    		        		                                                                                	            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b;'>" +
    		        		                                                                                	            "      · <span style='font-weight: bold; color: #ff6b6b;'>⚠️ 주의</span> : 젤 형태의 내용물을 하수구에 버리면 수질 오염과 배수구 막힘의 원인이 됩니다." +
    		        		                                                                                	            "    </div>" +

    		        		                                                                                	            "  </div>" +
    		        		                                                                                	            "</div>", 
    		        		                                                                                	            "images/vinyl/ice_pack.png", "images/vinyl/mark6.png"},
    		{"I027", "비닐 봉지(검정/투명)", "C02", 
    		        		                                                                                	                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #4a5d4e;'>" +
    		        		                                                                                	                
    		        		                                                                                	                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 배출 가능 봉투</b></p>" +
    		        		                                                                                	                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                "      · <span style='color: #ffcc00; font-weight: bold;'>종류</span>: 검정 비닐봉투, 투명 위생봉투(위생백), 편의점 봉투 등<br/>" +
    		        		                                                                                	                "      · <span style='color: #ffcc00; font-weight: bold;'>필수 조건</span>: 내용물을 완전히 비우고, 겉면에 붙은 스티커나 테이프를 제거해 주세요." +
    		        		                                                                                	                "    </div>" +

    		        		                                                                                	                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 매너</b></p>" +
    		        		                                                                                	                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                "      · <span style='color: #ffcc00; font-weight: bold;'>날림 방지</span>: 작은 비닐들이 바람에 날리지 않도록 큰 비닐 봉투 안에 차곡차곡 모아 묶어서 배출하는 것이 좋습니다.<br/>" +
    		        		                                                                                	                "      · <span style='color: #ffcc00; font-weight: bold;'>부피 감소</span>: 딱지 모양으로 접기보다는 펴서 모으는 것이 선별 작업에 도움이 됩니다." +
    		        		                                                                                	                "    </div>" +

    		        		                                                                                	                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b;'>" +
    		        		                                                                                	                "      · <span style='color: #ff6b6b; font-weight: bold; color: #ff6b6b;'>⚠️ 주의</span> : 음식물이 묻어 있거나 흙, 이물질로 오염된 비닐은 세척이 불가능할 경우 반드시 <span style='color: #ffffff; font-weight: bold;'>종량제 봉투</span>에 버려주세요." +
    		        		                                                                                	                "    </div>" + 

    		        		                                                                                	                "  </div>" +
    		        		                                                                                	                "</div>", 
    		        		                                                                                	                "images/vinyl/plastic_bag.png", "images/vinyl/mark4.png"},
    		{"I118", "과일봉지", "C02", 
    		        		                                                                                	                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                    
    		        		                                                                                	                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 방법</b></p>" +
    		        		                                                                                	                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                    "      · 내용물을 비우고 이물질을 깨끗이 제거한 후 <span style='color: #ffcc00; font-weight: bold;'>비닐류 수거함</span>으로 배출합니다.<br/>" +
    		        		                                                                                	                    "      · 흩날리지 않도록 차곡차곡 모아서 배출해 주세요." +
    		        		                                                                                	                    "    </div>" +

    		        		                                                                                	                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재활용 특징</b></p>" +
    		        		                                                                                	                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                    "      · 과일, 과자 포장용 비닐은 <span style='color: #ffcc00;'>생산자책임재활용제도(EPR)</span> 대상 품목으로 재활용 가치가 높습니다.<br/>" +
    		        		                                                                                	                    "      · 수거된 비닐은 용융 및 성형 과정을 거쳐 <span style='color: #ffcc00;'>건축 자재나 플라스틱 배수로</span> 등으로 재탄생합니다." +
    		        		                                                                                	                    "    </div>" +

    		        		                                                                                	                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                    "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                    "      · 테이프나 택배 송장 등 <span style='color: #ffcc00; font-weight: bold;'>다른 재질</span>이 붙어있다면 반드시 제거하고 배출해야 합니다." +
    		        		                                                                                	                    "    </div>" +

    		        		                                                                                	                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                    
    		        		                                                                                	                    "      <span><b style='color: #00fff0;'>💡 Tip</b> : 라면 봉지, 과자 봉지, 지퍼백, 위생팩, 세탁소 비닐 등도 모두 같은 방법으로 배출하세요!</span>" +
    		        		                                                                                	                    "    </div>" +

    		        		                                                                                	                    "  </div>" +
    		        		                                                                                	                    "</div>", 
    		        		                                                                                	                    "images/Vinyl/fruit_bag.png", "images/Vinyl/mark6.png"},
    	     // [유리병 - C03] (I033 ~ I042) 보강 버전
    		{"I028", "꿀 용기", "C03", 
    		        		                                                                                	                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                    
    		        		                                                                                	                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 절차</b></p>" +
    		        		                                                                                	                    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                    "      · <span style='color: #ffcc00; font-weight: bold;'>비우기 & 헹구기</span> : 남은 꿀은 비우고 <span style='color: #ffcc00;'>따뜻한 물</span>로 깨끗이 씻어주세요.<br/>" +
    		        		                                                                                	                    "      · <span style='color: #ffcc00; font-weight: bold;'>분리하기</span> : 뚜껑과 라벨을 제거한 후 <span style='color: #ffcc00; font-weight: bold;'>유리병 전용 수거함</span>에 배출합니다." +
    		        		                                                                                	                    "    </div>" +

    		        		                                                                                	                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 알아두면 좋은 팁</b></p>" +
    		        		                                                                                	                    "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                    "      · <span style='color: #ffcc00; font-weight: bold;'>라벨 제거</span> : 잘 떨어지지 않는 라벨은 물에 불리면 쉽게 제거됩니다.<br/>" +
    		        		                                                                                	                    "      · <span style='color: #ffcc00; font-weight: bold;'>재질 확인</span> : 플라스틱 재질의 꿀 용기는 유리병이 아닌 <span style='color: #ffcc00;'>플라스틱</span>으로 분류하세요." +
    		        		                                                                                	                    "    </div>" +

    		        		                                                                                	                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b;'>" +
    		        		                                                                                	                    "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 내용물 제거가 어렵거나 <span style='color: #ffcc00; font-weight: bold;'>깨진 유리</span>는 재활용이 불가하므로 신문지에 싸서 <span style='color: #ffcc00; font-weight: bold;'>종량제 봉투</span>에 버려주세요." +
    		        		                                                                                	                    "    </div>" +

    		        		                                                                                	                    "  </div>" +
    		        		                                                                                	                    "</div>", 
    		        		                                                                                	                    "images/glass_bottle/honey_pot.png", "images/glass_bottle/mark1.png"},

    		{"I029", "화장품 유리병", "C03", 
    		        		                                                                                	                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                        
    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 절차</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>비우기 & 헹구기</span> : 내용물을 비우고 내부를 물로 깨끗이 헹궈주세요.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>부속품 분리</span> : 펌프, 마개 등 <span style='color: #ffcc00;'>타 재질(플라스틱, 금속)</span> 부속품은 최대한 분리합니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>라벨 제거</span> : 겉면의 스티커나 비닐 라벨을 제거한 후 유리 수거함에 배출합니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 펌프 내부 스프링 등 분리가 어려운 <span style='color: #ffcc00; font-weight: bold;'>복합재질 펌프</span>는 일반 쓰레기로 버리고, <span style='color: #ffcc00; font-weight: bold;'>유리 몸체</span>만 따로 배출해 주세요." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 알아두면 좋은 팁</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>배출 불가</span> : 매니큐어병이나 아세톤병은 오염이 심해 <span style='color: #ffcc00;'>종량제 봉투</span>로 배출하세요.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>기름기 제거</span> : 크림통 등 유분기가 많다면 중성세제로 세척하는 것이 좋습니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "  </div>" +
    		        		                                                                                	                        "</div>", 
    		        		                                                                                	                        "images/glass_bottle/cosmetic_bottle.png", "images/glass_bottle/mark1.png"},

    		        		                                                                                	                    {"I030", "잼 유리병", "C03", 
    		        		                                                                                	                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                        
    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 잼 병 세척 절차</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>당분 녹이기</span> : 끈적한 당분은 <span style='color: #ffcc00;'>따뜻한 물</span>을 채워 흔들어 깨끗이 녹여주세요.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>철제 뚜껑 분리</span> : 금속 재질의 뚜껑은 반드시 <span style='color: #ffcc00; font-weight: bold;'>캔류(고철)</span>로 따로 분리 배출합니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 설탕 성분이 남으면 곰팡이가 생겨 재활용이 어렵습니다. <span style='color: #ffcc00; font-weight: bold;'>뽀득뽀득하게 세척</span>하여 배출하는 것이 핵심입니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 팁</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>스티커 제거</span> : 스티커 자국은 식용유를 묻혀 닦으면 깔끔하게 제거됩니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>파손 주의</span> : 깨진 유리병은 신문지에 싸서 반드시 <span style='color: #ffcc00;'>종량제 봉투</span>에 버려주세요." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "  </div>" +
    		        		                                                                                	                        "</div>", 
    		        		                                                                                	                        "images/glass_bottle/jam_jar.png", "images/glass_bottle/mark1.png"},

    		        		                                                                                	                    {"I031", "유리잔/드링크병", "C03", 
    		        		                                                                                	                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                        
    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 방법</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>드링크병</span> : 작은 드링크병도 뚜껑을 분리한 후 <span style='color: #ffcc00; font-weight: bold;'>유리병</span>으로 배출합니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>유리잔</span> : 일반적인 음료용 유리컵은 깨끗이 씻어 유리병류로 배출 가능합니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : <span style='color: #ffcc00; font-weight: bold;'>내열유리(락앤락 등), 크리스탈, 도자기, 거울</span>은 일반 유리와 녹는점이 달라 <span style='color: #ffcc00; font-weight: bold;'>절대 유리병으로 배출하면 안 됩니다.</span>" +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 알아두면 좋은 팁</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>대량 배출</span> : 재활용 안 되는 유리가 많을 때는 <span style='color: #ffcc00;'>불연성 마대</span>를 사용하세요.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>안전 배출</span> : 깨진 유리는 수거 기사님의 안전을 위해 충분히 감싸서 배출해 주세요." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "  </div>" +
    		        		                                                                                	                        "</div>", 
    		        		                                                                                	                        "images/glass_bottle/drink_glass.png", "images/glass_bottle/mark1.png"},

    		        		                                                                                	                    {"I032", "식용유 유리병", "C03", 
    		        		                                                                                	                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                        
    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 유분 제거 절차</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>세제 세척</span> : <span style='color: #ffcc00;'>따뜻한 물과 중성세제</span>를 넣어 흔들어 내부 유분을 완벽히 제거합니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>마개 분리</span> : 병 입구의 플라스틱 캡은 도구를 이용해 분리 배출합니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 세척 후에도 미끄러움이나 <span style='color: #ffcc00; font-weight: bold;'>기름기 오염</span>이 남았다면 재활용 공정에 치명적이므로 반드시 <span style='color: #ffcc00; font-weight: bold;'>종량제 봉투</span>에 배출하세요." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 세척 꿀팁</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>천연 세제</span> : 베이킹소다나 밀가루를 넣어 흔들면 기름기를 훨씬 쉽게 흡수합니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>캡 분리</span> : 입구 플라스틱이 안 빠질 때는 뜨거운 물에 잠시 담갔다 빼보세요." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "  </div>" +
    		        		                                                                                	                        "</div>", 
    		        		                                                                                	                        "images/glass_bottle/oil_bottle.png", "images/glass_bottle/mark1.png"},

    		        		                                                                                	                    {"I033", "소스병(케첩/머스타드)", "C03", 
    		        		                                                                                	                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                        
    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소스병 세척 및 분리</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>내부 세척</span> : 점도가 높은 양념은 <span style='color: #ffcc00;'>따뜻한 물</span>을 넣고 흔들어 완전히 제거합니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>부속품 제거</span> : 병 입구의 플라스틱 캡이나 비닐 실링은 도구로 말끔히 제거합니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 유리병 재활용은 <span style='color: #ffcc00; font-weight: bold;'>색상별(투명, 녹색, 갈색) 선별</span>이 중요합니다. 수거함이 구분되어 있다면 반드시 색상에 맞춰 배출해 주세요." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 알아두면 좋은 팁</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>재질 확인</span> : PET 재질 소스병은 <span style='color: #ffcc00;'>플라스틱</span>으로, 유리 재질만 <span style='color: #ffcc00;'>유리병</span>으로 분류합니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>세척 팁</span> : 좁은 입구는 베이킹소다 물에 불리면 쉽게 닦입니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "  </div>" +
    		        		                                                                                	                        "</div>", 
    		        		                                                                                	                        "images/glass_bottle/sauce_bottle.png", "images/glass_bottle/mark1.png"},

    		        		                                                                                	                    {"I034", "와인병", "C03", 
    		        		                                                                                	                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                        
    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부속품 완전 분리</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>캡 실 제거</span> : 병 입구를 감싸는 <span style='color: #ffcc00;'>알루미늄이나 비닐 실</span>을 도려내어 배출합니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>마개 분리</span> : 코르크 마개는 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로, 금속 캡은 <span style='color: #ffcc00; font-weight: bold;'>고철</span>로 분류합니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 와인병은 색상별로 녹여 재활용하므로, <span style='color: #ffcc00; font-weight: bold;'>파손되지 않은 상태</span>로 배출하는 것이 품질 유지에 매우 중요합니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 분리배출 팁</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>보증금 확인</span> : 대부분의 와인병은 보증금 대상이 아니므로 수거함에 바로 넣으시면 됩니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>장식물 제거</span> : 반짝이 등이 붙은 특수병은 재활용이 불가해 <span style='color: #ffcc00;'>일반 쓰레기</span>로 버려야 합니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "  </div>" +
    		        		                                                                                	                        "</div>", 
    		        		                                                                                	                        "images/glass_bottle/wine_bottle.png", "images/glass_bottle/mark1.png"},

    		        		                                                                                	                    {"I035", "참기름 유리병", "C03", 
    		        		                                                                                	                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                        
    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 유분 및 냄새 제거</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>열탕 세척</span> : <span style='color: #ffcc00;'>따뜻한 물과 베이킹소다</span>로 냄새와 유분을 깨끗이 제거하세요.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>조절 캡 분리</span> : 입구의 플라스틱 기름 조절 캡은 반드시 뽑아서 <span style='color: #ffcc00; font-weight: bold;'>플라스틱</span>으로 배출합니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 여러 번 씻어도 <span style='color: #ffcc00; font-weight: bold;'>기름 찌꺼기나 냄새</span>가 심하게 남았다면 재활용이 안 되므로 <span style='color: #ffcc00; font-weight: bold;'>종량제 봉투</span>로 배출해 주세요." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 마무리 정리</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>라벨 제거</span> : 비닐 라벨은 떼어서 비닐류로, 오염된 종이는 일반 쓰레기로 분류합니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>타월 배출</span> : 기름을 닦아낸 키친타월은 반드시 <span style='color: #ffcc00;'>일반 쓰레기</span>입니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "  </div>" +
    		        		                                                                                	                        "</div>", 
    		        		                                                                                	                        "images/glass_bottle/sesame_oil.png", "images/glass_bottle/mark1.png"},

    		        		                                                                                	                    {"I036", "커피 유리병", "C03", 
    		        		                                                                                	                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                        
    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 이물질 및 부속품 분리</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>실링지 제거</span> : 입구의 <span style='color: #ffcc00;'>종이/알루미늄 실링지</span> 찌꺼기를 말끔히 제거하세요.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>뚜껑 분리</span> : 플라스틱 재질의 뚜껑은 따로 <span style='color: #ffcc00; font-weight: bold;'>플라스틱</span>으로 배출합니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 내용물 배출 시 담배꽁초 등 <span style='color: #ffcc00; font-weight: bold;'>이물질</span>을 절대 병 안에 넣지 마세요. 가벼운 물 헹굼은 필수입니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 분리배출 팁</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>라벨 제거</span> : 겉면 라벨은 최대한 떼어내어 재질에 맞게(비닐/종이) 분류합니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>세척 팁</span> : 실링지가 잘 안 떨어지면 따뜻한 물에 잠시 불려주세요." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "  </div>" +
    		        		                                                                                	                        "</div>", 
    		        		                                                                                	                        "images/glass_bottle/coffee_jar.png", "images/glass_bottle/mark1.png"},

    		        		                                                                                	                    {"I037", "향수병", "C03", 
    		        		                                                                                	                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                        
    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 잔여물 비우기 및 세척</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>원액 배출</span> : 남은 향수는 키친타월에 흡수시켜 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 버려주세요.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>내부 세척</span> : 강한 향이 남지 않도록 내부를 가볍게 물로 헹궈냅니다." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 입구의 <span style='color: #ffcc00; font-weight: bold;'>금속/플라스틱 펌프 노즐</span>을 도구로 분리해야 재활용이 가능합니다. 분리가 불가능하다면 통째로 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 배출하세요." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 기타 부속품</b></p>" +
    		        		                                                                                	                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>장식 제거</span> : 병의 장식물이나 금속 라벨은 제거 후 배출하는 것이 좋습니다.<br/>" +
    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>재질 분리</span> : 뚜껑은 재질(금속/플라스틱)에 따라 따로 분류해 주세요." +
    		        		                                                                                	                        "    </div>" +

    		        		                                                                                	                        "  </div>" +
    		        		                                                                                	                        "</div>", 
    		        		                                                                                	                        "images/glass_bottle/perfume_bottle.png", "images/glass_bottle/mark1.png"},
    	     // [종이팩 - C04] (I043 ~ I052) 보강 버전
    		        		                                                                                	                    {"I038", "우유팩/종이팩", "C04", 
    		        		                                                                                	                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                            
    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 비우고 헹구기</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>내용물 비우기</span> : 가급적 내용물을 비우고 <span style='color: #ffcc00; font-weight: bold;'>물로 깨끗이 헹궈</span> 잔여물을 제거해 주세요.<br/>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>부속품 분리</span> : 빨대, 플라스틱 캡 등 종이팩과 다른 재질은 반드시 분리해서 배출합니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 펼치고 말리기</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>부피 줄이기</span> : 상자 모양의 종이팩을 가위로 잘라 평평하게 펼쳐 주세요.<br/>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>건조 필수</span> : 곰팡이가 생기지 않도록 잘 말린 후 배출하는 것이 좋습니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0;'>" +
    		        		                                                                                	                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 종이팩은 일반 폐지와 섞이지 않게 <span style='font-weight: bold; color: #ffcc00;'>전용 수거함</span>에 따로 넣어주세요!" +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "  </div>" +
    		        		                                                                                	                            "</div>", 
    		        		                                                                                	                            "images/paper_pack/milk_carton.png", "images/paper_pack/mark1.png"},

    		        		                                                                                	                        {"I039", "두유팩(멸균팩)", "C04", 
    		        		                                                                                	                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                            
    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 은박 코팅 확인 및 세척</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>멸균팩 확인</span> : 내부가 <span style='color: #ffcc00;'>은색 알루미늄</span>으로 코팅된 멸균팩은 일반 종이팩과 공정이 다릅니다.<br/>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>세척 후 펼치기</span> : 내용물을 비우고 헹군 뒤, 가위로 잘라 평평하게 펼쳐서 말려주세요." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 일반 폐지나 살균팩(우유팩)과 섞이지 않게 반드시 <span style='color: #ffcc00; font-weight: bold;'>멸균팩 전용 수거함</span>에 넣어주세요." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 매너</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>부속품 제거</span> : 부착된 플라스틱 빨대나 비닐 포장재는 반드시 따로 분리합니다.<br/>" +
    		        		                                                                                	                            "    </div>" +
    		        		                                                                                	                            
    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 전용함이 없다면 주민센터의 <span style='font-weight: bold; color: #ffcc00;'>종이팩 보상 제도</span>를 확인해 보세요!" +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "  </div>" +
    		        		                                                                                	                            "</div>", 
    		        		                                                                                	                            "images/paper_pack/soy_milk_carton.png", "images/paper_pack/mark2.png"},

    		        		                                                                                	                        {"I040", "일회용 종이컵", "C04", 
    		        		                                                                                	                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                            
    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 이물질 비우기 및 헹구기</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>내용물 세척</span> : 커피, 음료 등 <span style='color: #ffcc00;'>내용물을 깨끗이 비우고</span> 물로 헹궈주세요.<br/>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>오염된 컵</span> : 립스틱이나 음식물이 지워지지 않는다면 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 배출해야 합니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 전용 수거함이 없다면 일반 종이와 섞지 말고 <span style='color: #ffcc00; font-weight: bold;'>종량제 봉투</span>에 담아 배출하는 것이 올바른 방법입니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 장소 확인</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>전용함 이용</span> : 종이컵 전용 수거함 또는 종이팩 수거함에 넣습니다.<br/>" +
    		        		                                                                                	                            "    </div>" +
    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 일반 폐지와 섞이면 재활용되지 않으므로 따로 모아서 배출하세요." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "  </div>" +
    		        		                                                                                	                            "</div>", 
    		        		                                                                                	                            "images/paper_pack/paper_cup.png", "images/paper_pack/mark1.png"},

    		        		                                                                                	                        {"I041", "주스팩(테트라팩)", "C04", 
    		        		                                                                                	                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                            
    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부속품 완전 분리</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>캡/빨대 제거</span> : 플라스틱 캡은 <span style='color: #ffcc00;'>플라스틱</span>으로, 빨대 비닐은 <span style='color: #ffcc00;'>비닐류</span>로 분리합니다.<br/>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>내부 세척</span> : 주스 잔여물이 남으면 곰팡이가 생기므로 깨끗이 헹궈주세요." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 내부 은박 코팅이 된 <span style='color: #ffcc00; font-weight: bold;'>멸균팩</span>입니다. 일반 우유팩과 섞이지 않게 <span style='color: #ffcc00; font-weight: bold;'>멸균팩 전용 수거함</span>에 넣어주세요." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 팁</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>납작하게 펼치기</span>: 가위로 잘라 완전히 펼친 후 말려야 재활용 품질이 높아집니다.<br/>" +
    		        		                                                                                	                            "    </div>" +
    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 잘 펼쳐서 말린 팩은 훌륭한 화장지 자원이 됩니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "  </div>" +
    		        		                                                                                	                            "</div>", 
    		        		                                                                                	                            "images/paper_pack/juice_carton.png", "images/paper_pack/mark2.png"},

    		        		                                                                                	                        {"I042", "액상커피 종이팩", "C04", 
    		        		                                                                                	                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                            
    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 절단 및 내부 세척</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>상단 절단</span> : 대용량 커피 팩은 가위로 <span style='color: #ffcc00;'>상단을 완전히 잘라</span> 펼쳐주세요.<br/>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>물 세척 필수</span> : 커피 잔여물과 향이 남지 않도록 깨끗이 세척하는 것이 핵심입니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 은박 코팅된 팩이므로 반드시 <span style='color: #ffcc00; font-weight: bold;'>멸균팩 전용 수거함</span>에 넣어주세요. 잘 말리지 않으면 수거 과정에서 부패합니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 분리 팁</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>마개 분리</span>: 상단 플라스틱 마개는 제거하여 <span style='color: #ffcc00;'>플라스틱</span>으로 따로 배출하세요.<br/>" +
    		        		                                                                                	                            "    </div>" +
    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 뽀득뽀득 씻은 후 햇볕에 말려 배출하면 가장 좋습니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "  </div>" +
    		        		                                                                                	                            "</div>", 
    		        		                                                                                	                            "images/paper_pack/coffee_carton.png", "images/paper_pack/mark2.png"},

    		        		                                                                                	                        {"I043", "생수 종이팩", "C04", 
    		        		                                                                                	                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                            
    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부속품 분리 배출</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>뚜껑 분리</span> : 상단의 <span style='color: #ffcc00;'>플라스틱 뚜껑</span>은 돌려서 분리한 뒤 플라스틱으로 배출하세요.<br/>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>체결 부위</span> : 뚜껑 부위가 종이와 붙어있다면 무리하게 떼지 말고 그대로 멸균팩 전용함에 넣습니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 생수 종이팩은 대부분 <span style='color: #ffcc00; font-weight: bold;'>멸균팩</span>입니다. 일반 종이나 살균팩(우유팩)과 절대 섞이지 않도록 주의하세요." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 자원 정보</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>자원 가치</span>: 고품질 펄프를 사용하여 재활용 시 <span style='color: #ffcc00;'>핸드타월이나 화장지</span>로 재탄생합니다.<br/>" +
    		        		                                                                                	                            "    </div>" +
    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 작게 접기보다 펼쳐서 배출하는 것이 선별 효율을 높입니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "  </div>" +
    		        		                                                                                	                            "</div>", 
    		        		                                                                                	                            "images/paper_pack/water_carton.png", "images/paper_pack/mark2.png"},

    		        		                                                                                	                        {"I044", "와인 종이팩", "C04", 
    		        		                                                                                	                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                            
    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부피 축소 및 완전 건조</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>평평하게 펼치기</span> : 대용량 팩은 가위로 상하단을 잘라 <span style='color: #ffcc00;'>평평하게 펼쳐서</span> 배출해 주세요.<br/>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>악취 방지</span> : 와인 잔여물이 남으면 악취의 원인이 되므로 깨끗이 헹군 뒤 바짝 말려야 합니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 본체는 알루미늄 코팅이 된 <span style='color: #ffcc00; font-weight: bold;'>멸균팩 전용 수거함</span>으로 분류하세요. 일반 종량제 봉투 배출 시 자원이 낭비됩니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 부속품 제거</b></p>" +
    		        		                                                                                	                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>플라스틱 분리</span> : 입구의 나사산(스크류) 부위는 최대한 분리하여 <span style='color: #ffcc00;'>플라스틱</span>으로 배출하세요.<br/>" +
    		        		                                                                                	                            "    </div>" +
    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 와인팩은 종이 자원 회수량이 매우 높은 우수한 자원입니다." +
    		        		                                                                                	                            "    </div>" +

    		        		                                                                                	                            "  </div>" +
    		        		                                                                                	                            "</div>", 
    		        		                                                                                	                            "images/paper_pack/wine_carton.png", "images/paper_pack/mark2.png"},
    	     // [캔/고철 - C05] (I053 ~ I074) 보강 버전
    		        		                                                                                	                        {"I045", "스프레이/살충제", "C05", 
    		        		                                                                                	                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의 : 폭발 사고 주의</span><br/>" +
    		        		                                                                                	                                "      · 가스가 남으면 화재의 원인이 됩니다. <span style='color: #ffcc00; font-weight: bold;'>통풍이 잘되는 실외</span>에서 소리가 나지 않을 때까지 비워주세요." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 잔류 가스 제거</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>구멍 뚫기</span> : 안전을 위해 캔 옆면에 전용 도구로 구멍을 뚫어 남은 가스를 완전히 배출하는 것이 좋습니다.<br/>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>노즐 고장 시</span> : 내용물이 남았는데 나오지 않는다면 지자체의 유해폐기물 배출 방법을 확인하세요." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 부속품 분리 배출</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>캡 분리</span> : 플라스틱 캡은 분리하여 <span style='color: #ffcc00;'>플라스틱</span>으로 배출하세요.<br/>" +
    		        		                                                                                	                                "    </div>" +
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 캔 본체는 고철(캔류) 수거함으로 분류합니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "  </div>" +
    		        		                                                                                	                                "</div>", 
    		        		                                                                                	                                "images/can/spray_can.png", "images/can/mark1.png"},

    		        		                                                                                	                            {"I046", "부탄가스", "C05", 
    		        		                                                                                	                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                "      · 반드시 <span style='color: #ffcc00; font-weight: bold;'>화기가 없는 야외</span>에서 작업하세요. 수거 차량 화재의 주요 원인이 됩니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 완벽한 가스 제거</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>가스 비우기</span> : 노즐을 바닥에 눌러 '칙' 소리가 나지 않을 때까지 비웁니다.<br/>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>구멍 내기</span> : 가스 전용 펀치로 <span style='color: #ffffff; font-weight: bold;'>측면에 2~3개의 구멍</span>을 내어 배출하세요." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 팁</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>액체 가스 확인</span> : 흔들었을 때 찰랑거리는 소리가 들린다면 아직 가스가 남은 상태입니다.<br/>" +
    		        		                                                                                	                                "    </div>" +
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 뚜껑은 따로 분리하여 플라스틱으로 배출해 주세요." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "  </div>" +
    		        		                                                                                	                                "</div>", 
    		        		                                                                                	                                "images/can/butane_can.png", "images/can/mark1.png"},

    		        		                                                                                	                            {"I047", "애완동물 음식캔", "C05", 
    		        		                                                                                	                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                
    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 유분 제거 및 세척</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>깨끗이 헹구기</span> : 습식 사료의 기름기를 <span style='color: #ffcc00;'>주방세제</span>로 씻어내 주세요.<br/>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>이물질 금지</span> : 남은 음식물은 재활용 품질을 떨어뜨리는 주요 원인입니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 캔 뚜껑과 입구가 매우 날카롭습니다. 분리된 뚜껑은 <span style='color: #ffcc00; font-weight: bold;'>캔 안으로 밀어 넣어</span> 안전하게 배출하세요." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재질 확인</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>철/알루미늄</span> : 대부분 철이지만, 작은 캔은 알루미늄일 수 있으니 바닥면 마크를 확인하세요.<br/>" +
    		        		                                                                                	                                "    </div>" +
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 깨끗하게 세척된 캔은 고철류로 배출합니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "  </div>" +
    		        		                                                                                	                                "</div>", 
    		        		                                                                                	                                "images/can/pet_food_can.png", "images/can/mark1.png"},

    		        		                                                                                	                            {"I048", "공구류(망치/드라이버)", "C05", 
    		        		                                                                                	                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                
    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재질별 분리 배출</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>금속 부분</span> : 망치 머리나 드라이버 날은 <span style='color: #ffcc00;'>고철(캔류)</span>로 배출하세요.<br/>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>손잡이 분리</span> : 가능하면 플라스틱이나 고무 손잡이를 분리하여 배출합니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 분리가 불가능한 일체형 공구는 <span style='color: #ffcc00; font-weight: bold;'>불연성 마대(특수규격 봉투)</span>에 담아 배출해야 합니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 안전 배출 매너</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>안전 포장</span> : 뾰족한 공구는 작업자가 다치지 않게 <span style='color: #ffffff;'>신문지</span> 등으로 감싸서 배출하세요.<br/>" +
    		        		                                                                                	                                "    </div>" +
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 순수 금속 부분은 재활용 가치가 매우 높습니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "  </div>" +
    		        		                                                                                	                                "</div>", 
    		        		                                                                                	                                "images/can/tool_set.png", "images/can/mark1.png"},

    		        		                                                                                	                            {"I049", "국자(금속)", "C05", 
    		        		                                                                                	                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                
    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 복합 재질 분리</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>나사 분리</span> : 나무/플라스틱 손잡이는 <span style='color: #ffcc00;'>나사를 풀어</span> 각각 재질별로 분리해 주세요.<br/>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>일체형 배출</span> : 분리가 불가능한 경우에만 <span style='color: #ffcc00;'>고철(캔류)</span>로 배출합니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 전체가 실리콘으로 감싸진 국자는 재활용이 안 되므로 <span style='color: #ffcc00; font-weight: bold;'>종량제 봉투</span>에 버려야 합니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 팁</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>스테인리스</span> : 고철 중에서도 품질이 좋아 따로 모아 배출하면 매우 좋습니다.<br/>" +
    		        		                                                                                	                                "    </div>" +
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 음식물 오염이 없도록 깨끗이 씻어서 배출해 주세요." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "  </div>" +
    		        		                                                                                	                                "</div>", 
    		        		                                                                                	                                "images/can/ladle.png", "images/can/mark1.png"},

    		        		                                                                                	                            {"I050", "그릇(금속)", "C05", 
    		        		                                                                                	                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                
    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 배출 가능한 금속 그릇</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>순수 금속</span> : 스테인리스 밥그릇, 쟁반 등은 <span style='color: #ffcc00;'>고철</span>로 배출하세요.<br/>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>이물질 제거</span> : 음식물 찌꺼기를 물로 깨끗이 씻어낸 뒤 건조해야 합니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : <span style='color: #ffcc00;'>사기그릇, 뚝배기, 유기(놋그릇)</span>은 고철이 아닙니다. 반드시 <span style='color: #ffffff; font-weight: bold;'>불연성 마대</span>에 담아주세요." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 안전 팁</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>깨진 그릇</span> : 날카로운 부분은 <span style='color: #ffffff;'>신문지</span>로 감싸 배출하는 것이 안전합니다.<br/>" +
    		        		                                                                                	                                "    </div>" +
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 스테인리스 재질은 가치가 높은 재활용 자원입니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "  </div>" +
    		        		                                                                                	                                "</div>", 
    		        		                                                                                	                                "images/can/metal_bowl.png", "images/can/mark1.png"},

    		        		                                                                                	                            {"I051", "나사/못", "C05", 
    		        		                                                                                	                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                "      · 낱개 배출 시 타이어 펑크나 부상을 유발합니다. 반드시 <span style='color: #ffcc00; font-weight: bold;'>캔 속에 모아 넣고</span> 입구를 막아 배출하세요." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 안전 배출 방법</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>비닐 사용 금지</span> : 비닐봉지는 찢어질 위험이 커서 권장하지 않습니다.<br/>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>페트병 활용</span> : 양이 적다면 페트병에 모아 '고철'이라 적고 배출해도 좋습니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 자원 확인</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>자석 활용</span> : 자석에 붙는 못은 재활용 가치가 높은 '철'입니다.<br/>" +
    		        		                                                                                	                                "    </div>" +
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 흩어지기 쉬운 작은 나사들을 꼭 캔 안에 가둬주세요!" +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "  </div>" +
    		        		                                                                                	                                "</div>", 
    		        		                                                                                	                                "images/can/screws_and_nails.png", "images/can/mark1.png"},

    		        		                                                                                	                            {"I052", "낫/톱", "C05", 
    		        		                                                                                	                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                "      · 날카로운 날 부위는 <span style='color: #ffcc00; font-weight: bold;'>두꺼운 골판지</span>로 여러 번 감싸고 테이프로 단단히 고정해야 합니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재질별 분리</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>손잡이 배출</span> : 나무나 플라스틱 손잡이는 분리하여 <span style='color: #ffcc00;'>종량제 봉투</span>에 버려주세요.<br/>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>날 부위</span> : 금속 날은 고철로 분류하여 배출합니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 주의 표시</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>텍스트 작성</span> : 포장 겉면에 <span style='color: #ffcc00;'>'날카로움'</span> 또는 <span style='color: #ffcc00;'>'칼 주의'</span>라고 크게 적어주세요.<br/>" +
    		        		                                                                                	                                "    </div>" +
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 고철끼리 끈으로 묶어 배출하면 선별이 쉽습니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "  </div>" +
    		        		                                                                                	                                "</div>", 
    		        		                                                                                	                                "images/can/sickle_and_saw.png", "images/can/mark1.png"},

    		        		                                                                                	                            {"I053", "도끼", "C05", 
    		        		                                                                                	                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                
    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 분리 배출 및 포장</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>날 분리</span> : 손잡이를 분리할 수 있다면 <span style='color: #ffcc00;'>금속 날만 고철</span>로 배출하세요.<br/>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>단단한 테이핑</span> : 날 부위는 두꺼운 판지로 감싸 수거 작업자의 부상을 방지해야 합니다." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 크기가 크거나 일체형인 경우 <span style='color: #ffcc00; font-weight: bold;'>대형 폐기물 스티커</span>를 부착하여 정해진 장소에 내놓으세요." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 안전 팁</b></p>" +
    		        		                                                                                	                                "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>무게 주의</span> : 도끼날은 무거워 포장이 풀리기 쉬우니 노끈으로 여러 번 고정하세요.<br/>" +
    		        		                                                                                	                                "    </div>" +
    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 날카로운 도구 배출 시에는 항상 작업자의 안전을 먼저 생각해주세요." +
    		        		                                                                                	                                "    </div>" +

    		        		                                                                                	                                "  </div>" +
    		        		                                                                                	                                "</div>", 
    		        		                                                                                	                                "images/can/axe.png", "images/can/mark1.png"},
    		        		                                                                                	                            {"I054", "병뚜껑(금속)", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 캔 속에 모아서 배출</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · 크기가 작은 뚜껑은 선별 기계에서 유실되기 쉽습니다.<br/>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>빈 금속 캔(커피캔 등) 속에 모아</span> 가득 차면 입구를 압착해 배출하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                    "      · 안쪽의 얇은 <span style='color: #ffffff; font-weight: bold;'>고무 패킹</span>은 제거하기 어려우므로 그대로 금속류로 배출해도 괜찮습니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 주의</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>플라스틱 분리</span> : 플라스틱 병뚜껑과는 반드시 분리하여 금속만 따로 모아주세요.<br/>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>효과 :</span> 작은 조각들이 유실되지 않아 자원 회수율이 훨씬 높아집니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/bottle_caps.png", "images/can/mark1.png"},

    		        		                                                                                	                                {"I055", "분유 깡통", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 플라스틱 부속품 분리</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>캡/스푼 제거</span> : 상단의 플라스틱 겉뚜껑과 내부 스푼은 반드시 <span style='color: #ffcc00;'>플라스틱</span>으로 배출하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                    "      · 알루미늄 속뚜껑(리드)은 매우 날카롭습니다. 캔 안에 넣어 압착하거나 안전하게 따로 배출하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 세척 및 배출</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>가루 털어내기</span> : 내부 가루를 깨끗이 털어내거나 물로 가볍게 헹궈 건조해 주세요.<br/>" +
    		        		                                                                                	                                    "    </div>" +
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 깨끗한 분유 캔 본체는 금속(철)류 수거함에 배출합니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/formula_can.png", "images/can/mark1.png"},

    		        		                                                                                	                                {"I056", "쓰레기받기(금속)", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 이물질 및 부속품 제거</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>부속품 분리</span> : 바닥의 <span style='color: #ffcc00;'>고무날</span>이나 손잡이 끝의 <span style='color: #ffcc00;'>플라스틱 마개</span>는 가급적 제거해 주세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                    "      · 전체가 플라스틱인 제품은 '플라스틱', 나무 재질은 '종량제 봉투'로 배출해야 합니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 정보</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>철(Metal) 재질</span> : 색상이 코팅되어 있어도 주재질이 금속이라면 고철로 배출 가능합니다.<br/>" +
    		        		                                                                                	                                    "    </div>" +
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 부피가 크지 않다면 고철 수거함에 바로 배출하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/metal_dustpan.png", "images/can/mark1.png"},

    		        		                                                                                	                                {"I057", "아령/역기", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 고철 배출 가능 여부</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>순수 금속(쇠)</span> : 코팅 없는 쇠 아령, 역기 봉, 원판은 고철로 배출할 수 있습니다.<br/>" +
    		        		                                                                                	                                    "      · 무게가 무거우므로 수거함 바닥에 안전하게 놓아주세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                    "      · 고무/우레탄 코팅 제품이나 내부에 모래가 든 플라스틱 아령은 <span style='color: #ffcc00; font-weight: bold;'>대형 폐기물</span>로 신고하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 핵심 판별법</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>자석 테스트</span> : 자석이 직접 닿지 않거나 코팅이 두껍다면 고철이 아닙니다.<br/>" +
    		        		                                                                                	                                    "    </div>" +
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 분리가 어려운 복합 재질은 재활용이 어렵습니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/dumbbell.png", "images/can/mark1.png"},

    		        		                                                                                	                                {"I058", "압력솥/냄비", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부속품 철저 분리</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>타재질 제거</span> : 뚜껑의 <span style='color: #ffcc00;'>고무 패킹</span>, <span style='color: #00fff0;'>플라스틱 손잡이</span>, 추는 반드시 분리하세요.<br/>" +
    		        		                                                                                	                                    "      · 나사를 풀어 금속 본체만 따로 배출하는 것이 원칙입니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                    "      · 탄 자국이나 눌러붙은 음식물은 <span style='color: #ffffff; font-weight: bold;'>철수세미</span>로 깨끗이 닦아내야 재활용이 가능합니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 유리 뚜껑 주의</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>유리 분리</span> : 유리와 금속이 분리되지 않는다면 '불연성 폐기물'로 배출하세요.<br/>" +
    		        		                                                                                	                                    "    </div>" +
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip:</span> 스테인리스, 알루미늄 등 모든 금속제 냄비는 고철 배출 가능합니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/pressure_cooker.png", "images/can/mark1.png"},

    		        		                                                                                	                                {"I059", "옷걸이(철사)", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                    "      · 낱개로 버리면 선별장에서 엉킴 사고를 일으킵니다. 10개 내외로 모아 <span style='color: #ffcc00; font-weight: bold;'>끈으로 단단히 묶어</span> 배출하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 코팅 옷걸이</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>그대로 배출</span> : 플라스틱이 얇게 코팅된 철사 옷걸이는 코팅을 벗기지 않아도 됩니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 불가</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>전체 플라스틱/목재</span> : 플라스틱은 플라스틱류로, 목재는 종량제 봉투로 배출하세요.<br/>" +
    		        		                                                                                	                                    "    </div>" +
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 세탁소 옷걸이는 고철 자원으로 가치가 높습니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/wire_hanger.png", "images/can/mark1.png"},

    		        		                                                                                	                                {"I060", "의류건조대", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부피 최소화 및 배출</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>고철 배출</span> : 살대가 금속인 건조대는 최대한 <span style='color: #ffcc00;'>접어서</span> 고철로 배출합니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                    "      · 살대 끝의 플라스틱 부품은 떼어내는 것이 좋습니다. 제거 시 고철 품질이 높아집니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 대형 제품 주의</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>대형 폐기물</span> : 분리가 너무 힘들거나 부피가 너무 크면 대형 폐기물 스티커를 권장합니다.<br/>" +
    		        		                                                                                	                                    "    </div>" +
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 접힌 부분이 펴지지 않게 끈으로 묶어주면 수거가 쉽습니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/drying_rack.png", "images/can/mark1.png"},

    		        		                                                                                	                                {"I061", "재떨이(금속)", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 내부 세척 필수</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>꽁초 비우기</span> : 담배꽁초와 재를 비운 뒤 <span style='color: #ffcc00;'>물과 세제</span>로 깨끗이 닦아 배출하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>유리, 도자기, 석재</span> 재떨이는 고철이 아닙니다. 종량제 봉투에 버려야 합니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 고철 배출</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>금속 재질</span> : 스테인리스 등 금속 재질만 고철류로 분리 배출이 가능합니다.<br/>" +
    		        		                                                                                	                                    "    </div>" +
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 이물질이 묻은 상태로는 재활용되지 않습니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/ashtray.png", "images/can/mark1.png"},
    		        		                                                                                	                                {"I802", "니퍼", "C05", 
    		        		                                                                                	                                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                        
    		        		                                                                                	                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 고철 수거함 배출</b></p>" +
    		        		                                                                                	                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                        "      · 니퍼는 주재질이 금속이므로 <span style='color: #ffcc00; font-weight: bold;'>고철 수거함</span>으로 배출합니다.<br/>" +
    		        		                                                                                	                                        "      · 손잡이의 고무/플라스틱 부분은 제거가 가능하다면 제거 후 배출하고, 분리가 어렵다면 그대로 고철로 배출해도 무방합니다." +
    		        		                                                                                	                                        "    </div>" +

    		        		                                                                                	                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재활용 과정</b></p>" +
    		        		                                                                                	                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                        "      · 수거된 고철은 용융(녹이는) 과정을 거쳐 <span style='color: #ffcc00; font-weight: bold;'>새로운 철판이나 알루미늄 판</span>으로 재탄생합니다.<br/>" +
    		        		                                                                                	                                        "      · 금속은 재활용 효율이 매우 높은 소중한 자원입니다." +
    		        		                                                                                	                                        "    </div>" +

    		        		                                                                                	                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
    		        		                                                                                	                                        "      <b style='color: #4ebfad;'>💡 Tip</b><br/>" +
    		        		                                                                                	                                        "      <span style='font-size: 13px; color: #ffffff;'>" +
    		        		                                                                                	                                        "        니퍼뿐만 아니라 펜치, 스패너, 렌치 등 <b style='color: #ffcc00;'>대부분의 금속 공구류</b>는 동일하게 고철로 분류됩니다. 다만 날카로운 부분에 수거하시는 분들이 다치지 않도록 주의해 주세요." +
    		        		                                                                                	                                        "      </span>" +
    		        		                                                                                	                                        "    </div>" +

    		        		                                                                                	                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ffcc00; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                        "      <span><b style='color: #ffcc00;'>⚠️ 주의</b> : 스프링 등이 포함된 복합 구조의 큰 공구는 지자체에 따라 종량제 봉투 배출을 권고할 수 있으니 확인이 필요합니다.</span>" +
    		        		                                                                                	                                        "    </div>" +

    		        		                                                                                	                                        "  </div>" +
    		        		                                                                                	                                        "</div>", 
    		        		                                                                                	                                        "images/can/nipper.png", "images/can/mark1.png"},
    		        		                                                                                	                                {"I803", "네트망", "C05", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 고철 수거함 배출 (중소형)</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · 일반적인 크기의 인테리어용 네트망은 <span style='color: #ffcc00; font-weight: bold;'>고철 수거함</span>으로 배출합니다.<br/>" +
    		        		                                                                                	                                            "      · 겉면이 플라스틱으로 코팅되어 있어도 대부분 고철로 재활용이 가능합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 대형폐기물 신고 배출 (대형)</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · 반려동물 펜스나 대형 가림막처럼 <span style='color: #ffcc00; font-weight: bold;'>수거함에 들어가지 않는 크기</span>는 대형폐기물로 신고 후 스티커를 부착해 배출해야 합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
    		        		                                                                                	                                            "      <b style='color: #4ebfad;'>💡 Tip</b><br/>" +
    		        		                                                                                	                                            "      <span style='font-size: 13px; color: #ffffff;'>" +
    		        		                                                                                	                                            "        네트망과 같은 철제 제품은 제철소에서 녹여져 <b style='color: #ffcc00;'>철판이나 빔</b> 등 새로운 건축·산업 자재로 100% 가까이 재활용되는 우수한 자원입니다." +
    		        		                                                                                	                                            "      </span>" +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                            "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 고기 구이용 석쇠처럼 음식물이 심하게 눌어붙어 제거되지 않는 경우는 재활용이 어려울 수 있습니다.</span>" +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/can/wire_mesh.png", "images/can/mark1.png"},

    		        		                                                                                	                                {"I062", "주전자", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재질별 배출</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>고철류 배출</span> : 스테인리스나 알루미늄 재질의 일반 주전자는 고철로 분류합니다.<br/>" +
    		        		                                                                                	                                    "      · 플라스틱 손잡이는 제거가 가능하다면 제거 후 배출하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                    "      · 전기 주전자(무선 포트)는 가전제품입니다. <span style='color: #ffcc00; font-weight: bold;'>소형 가전 수거함</span>으로 배출하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 불가</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>도자기/유리</span> : 사기나 유리 주전자는 종량제 봉투나 불연성 마대로 배출해야 합니다.<br/>" +
    		        		                                                                                	                                    "    </div>" +
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 본체 재질이 금속인지를 먼저 확인하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/kettle.png", "images/can/mark1.png"},

    		        		                                                                                	                                {"I063", "철사/노끈(금속)", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                    "      · 가느다란 철사는 유실되기 쉽습니다. <span style='color: #ffcc00; font-weight: bold;'>야구공 크기 정도로 뭉쳐서</span> 부피를 만든 뒤 배출하세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 혼동 주의 품목</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>비금속 노끈</span> : 비닐 노끈, 마끈, 나일론 끈은 절대로 고철이 아닙니다.<br/>" +
    		        		                                                                                	                                    "      · 재활용이 불가능하므로 반드시 <span style='color: #ffcc00; font-weight: bold;'>종량제 봉투</span>에 버려주세요." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 팁</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>캔 활용</span> : 작은 금속 조각들을 캔 속에 넣고 입구를 찌그러뜨려 배출하면 더 좋습니다.<br/>" +
    		        		                                                                                	                                    "    </div>" +
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 낱개보다는 덩어리로 만드는 것이 재활용의 핵심입니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/wire_roll.png", "images/can/mark1.png"},

    		        		                                                                                	                                {"I064", "철판/불판", "C05", 
    		        		                                                                                	                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                    
    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 기름기 제거 필수</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>반드시 세척</span> : 기름기와 탄 찌꺼기는 세제로 씻어낸 후 배출해야 합니다.<br/>" +
    		        		                                                                                	                                    "      · 오염된 상태로 배출되면 재활용되지 못하고 폐기됩니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                    "      · 불소 코팅이 된 불판도 본체가 금속이므로 고철 배출이 가능합니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 팁</b></p>" +
    		        		                                                                                	                                    "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                    "      · <span style='color: #ffcc00; font-weight: bold;'>타 재질 분리</span> : 나무 손잡이 등은 최대한 분리해 주세요.<br/>" +
    		        		                                                                                	                                    "    </div>" +
    		        		                                                                                	                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip:</span> 고기 기름은 키친타월로 먼저 닦아내면 세척이 쉽습니다." +
    		        		                                                                                	                                    "    </div>" +

    		        		                                                                                	                                    "  </div>" +
    		        		                                                                                	                                    "</div>", 
    		        		                                                                                	                                    "images/can/grill_plate.png", "images/can/mark1.png"},
    		        		                                                                                	                                {"I065", "컵(텀블러/금속)", "C05", 
    		        		                                                                                	                                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                        
    		        		                                                                                	                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부속품 완전 분리</b></p>" +
    		        		                                                                                	                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                        "      · <span style='color: #ffcc00; font-weight: bold;'>본체 배출</span> : 스테인리스 본체만 <span style='color: #ffcc00;'>고철</span>로 배출하세요.<br/>" +
    		        		                                                                                	                                        "      · 플라스틱 뚜껑, 빨대, 내부 실링지는 반드시 제거해야 합니다." +
    		        		                                                                                	                                        "    </div>" +

    		        		                                                                                	                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                        "      · 내부가 <span style='color: #ffcc00; font-weight: bold;'>도자기(세라믹)</span>인 텀블러는 재활용이 불가능하므로 <span style='color: #ffcc00;'>종량제 봉투</span>로 배출하세요." +
    		        		                                                                                	                                        "    </div>" +

    		        		                                                                                	                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 이질 재질 제거</b></p>" +
    		        		                                                                                	                                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                        "      · <span style='color: #ffcc00; font-weight: bold;'>홀더 분리</span> : 외부의 가죽이나 고무 홀더는 모두 벗겨서 <span style='color: #ffffff;'>일반 쓰레기</span>로 버려주세요.<br/>" +
    		        		                                                                                	                                        "    </div>" +
    		        		                                                                                	                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                        "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 순수 금속 본체 위주로 배출하는 것이 핵심입니다." +
    		        		                                                                                	                                        "    </div>" +

    		        		                                                                                	                                        "  </div>" +
    		        		                                                                                	                                        "</div>", 
    		        		                                                                                	                                        "images/can/tumbler.png", "images/can/mark1.png"},

    		        		                                                                                	                                    {"I066", "후라이팬", "C05", 
    		        		                                                                                	                                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                        
    		        		                                                                                	                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 코팅 무관 고철 배출</b></p>" +
    		        		                                                                                	                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                        "      · <span style='color: #ffcc00; font-weight: bold;'>모든 코팅 가능</span> : 테플론, 세라믹 등 <span style='color: #ffcc00;'>코팅 재질에 상관없이</span> 고철로 배출 가능합니다.<br/>" +
    		        		                                                                                	                                        "      · 본체의 대부분이 금속이므로 재활용 가치가 매우 높습니다." +
    		        		                                                                                	                                        "    </div>" +

    		        		                                                                                	                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                        "      · 음식물 찌꺼기와 기름기는 <span style='color: #ffcc00; font-weight: bold;'>반드시 세제</span>로 깨끗이 씻어서 배출해 주세요." +
    		        		                                                                                	                                        "    </div>" +

    		        		                                                                                	                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 손잡이 관리</b></p>" +
    		        		                                                                                	                                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                        "      · <span style='color: #ffcc00; font-weight: bold;'>나사 분리</span> : 플라스틱 손잡이는 나사를 풀어 분리 배출하는 것이 가장 좋습니다.<br/>" +
    		        		                                                                                	                                        "    </div>" +
    		        		                                                                                	                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                        "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 기름기는 키친타월로 먼저 닦아낸 뒤 설거지하면 더 간편합니다." +
    		        		                                                                                	                                        "    </div>" +

    		        		                                                                                	                                        "  </div>" +
    		        		                                                                                	                                        "</div>", 
    		        		                                                                                	                                        "images/can/frying_pan.png", "images/can/mark1.png"},
    		        		                                                                                	                                    {"I119", "그레이터", "C05", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 방법</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · 그레이터(강판)는 본체가 금속으로 제작되어 있으므로 <span style='color: #ffcc00; font-weight: bold;'>고철 수거함</span>으로 배출합니다.<br/>" +
    		        		                                                                                	                                            "      · 배출 전 음식물 찌꺼기가 남지 않도록 깨끗이 세척해 주세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재활용 특징</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · 수거된 금속 제품은 철, 알루미늄, 스테인리스 등으로 정밀하게 선별됩니다.<br/>" +
    		        		                                                                                	                                            "      · 이후 제철소에서 용융 과정을 거쳐 새로운 <span style='color: #ffcc00;'>철판이나 알루미늄 판</span> 등 산업 자재로 다시 태어납니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                            "      · 날카로운 부분이 있어 수거 시 사고 위험이 있으므로, 가급적 <span style='color: #ffcc00; font-weight: bold;'>신문지나 종이</span>로 감싸서 안전하게 배출해 주세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "      <span><b style='color: #00fff0;'>💡 Tip</b> : 칼, 채칼, 스팸 슬라이서, 강판 등 대부분의 금속 주방기구는 고철로 분류됩니다.</span>" +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Can/grater.png", "images/Can/mark1.png"},
    		        		                                                                                	                                    {"I301", "가마솥", "C05", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                                
    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 고철류 배출 (기본)</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 가마솥은 무쇠(철) 재질이므로 <span style='color: #ffcc00; font-weight: bold;'>고철 수거함</span>에 배출하는 것이 원칙입니다.<br/>" +
    		        		                                                                                	                                                "      · 이물질을 깨끗이 제거하고 본체와 뚜껑을 함께 배출해 주세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 대형 폐기물 신고 배출</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 가마솥의 <span style='color: #ffcc00; font-weight: bold;'>크기나 무게가 너무 커서</span> 일반 고철함에 담기 어려운 경우,<br/>" +
    		        		                                                                                	                                                "      · 지자체에 <span style='color: #ffcc00; font-weight: bold;'>대형 폐기물</span>로 신고하고 수수료 납부 후 지정된 장소에 배출해야 합니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
    		        		                                                                                	                                                "      <b style='color: #4ebfad;'>💡 Tip</b><br/>" +
    		        		                                                                                	                                                "      <span style='font-size: 13px; color: #ffffff;'>" +
    		        		                                                                                	                                                "        금속 가전 및 주방도구는 선별장에서 철, 스테인리스 등으로 분류되어 제철소에서 <b style='color: #ffcc00;'>새로운 철판이나 금속 제품</b>으로 100% 재활용될 수 있는 소중한 자원입니다." +
    		        		                                                                                	                                                "      </span>" +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : <b style='color: #ffcc00;'>뚝배기나 내열 냄비</b>는 고철이 아니므로 절대 고철함에 넣지 마세요. (불연성 종량제 배출)</span>" +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/Can/gamasot.png", "images/Can/mark1.png"},
    		        		                                                                                	                                    {"I120", "금속 트로피", "C05", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                                
    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 방법</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 금속 재질(청동, 알루미늄 등)로 된 트로피는 <span style='color: #ffcc00; font-weight: bold;'>고철 수거함</span>으로 배출합니다.<br/>" +
    		        		                                                                                	                                                "      · 고철 외 다른 재질이 많이 섞여 분리가 어렵다면 <span style='color: #ffcc00;'>종량제 봉투</span>로 배출해야 합니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재활용 특징</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 순수 금속 트로피는 고철로 분류되어 녹인 후 새로운 금속 제품으로 <span style='color: #ffcc00;'>재활용이 가능</span>합니다.<br/>" +
    		        		                                                                                	                                                "      · 다만, 나무나 크리스탈 등 타 재질 함량이 높은 경우 재활용이 어렵습니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                                "      · 명패(플라스틱)나 받침대(나무/돌) 등 <span style='color: #ffcc00; font-weight: bold;'>다른 재질</span>은 최대한 분리하여 각각의 수거함에 배출하세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                
    		        		                                                                                	                                                "      <span><b style='color: #00fff0;'>💡 Tip</b> : 메달(도금), 금속 상패, 상장 케이스 내 금속 장식 등</span>" +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/Can/trophy.png", "images/Can/mark1.png"},
    	     // [스티로폼 - C06] (I075 ~ I084) 보강 버전
    		        		                                                                                	                                    {"I067", "스티로폼 완충재", "C06", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 이물질 철저 제거</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · 박스 조각,투명 테이프, 주소 송장을 모두 제거하세요.<br/>" +
    		        		                                                                                	                                            "      · 스티커 자국이 남지 않도록 깨끗한 상태로 배출해야 합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                            "      · 부피가 크다면 <span style='color: #ffcc00; font-weight: bold;'>적당한 크기로 쪼개서</span> 비닐 봉투에 담거나 끈으로 묶어 배출하세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재활용 정보</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>가전제품 완충재</span> : 오염되지 않은 흰색 완충재는 매우 우수한 재활용 자원이 됩니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Strofoam/buffer.png", ""},

    		        		                                                                                	                                        {"I068", "과일 포장재", "C06", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 색상별 배출 구분</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>흰색 그물망</span> : 오염이 없는 깨끗한 상태라면 <span style='color: #ffcc00;'>스티로폼</span>으로 배출하세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                            "      · 분홍, 노란색 등 <span style='color: #ffcc00; font-weight: bold;'>색깔이 있는 것</span>은 재활용이 안 되므로 <span style='color: #ffcc00;'>일반 쓰레기</span>로 버려야 합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 상태 확인</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                            "      · 과일즙 등 이물질이 묻은 경우 반드시 <span style='color: #ffcc00;'>일반 쓰레기</span>로 분류하세요.<br/>" +
    		        		                                                                                	                                            "    </div>" +
    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 지자체별로 과일망을 일반 쓰레기로만 받는 곳이 많으니 확인이 필요합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Strofoam/fruit_wrap.png", ""},

    		        		                                                                                	                                        {"I069", "신선식품 아이스박스", "C06", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 외부 부착물 완전 제거</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>송장/테이프 제거</span> : 칼로 긁어서라도 테이프와 스티커를 <span style='color: #ffcc00;'>완벽히 제거</span>해야 합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                            "      · 내용물이 묻어 세척되지 않는 박스는 조각내어 <span style='color: #ffcc00; font-weight: bold;'>종량제 봉투(일반 쓰레기)</span>에 담아 배출하세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 확인</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                            "      · 뚜껑과 본체를 분리하여 각각 내부에 이물질이 없는지 꼭 확인하세요.<br/>" +
    		        		                                                                                	                                            "    </div>" +
    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 깨끗한 흰색 아이스박스만 스티로폼 재활용이 가능합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Strofoam/ice_box.png", ""},

    		        		                                                                                	                                        {"I070", "컵라면 용기", "C06", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 햇빛을 이용한 색소 제거</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · 빨간 국물이 밴 용기는 물로 헹군 뒤 <span style='color: #ffcc00; font-weight: bold;'>햇빛에 하루 정도</span> 말려주세요.<br/>" +
    		        		                                                                                	                                            "      · 자외선에 의해 색소가 휘발되어 하얗게 변하면 재활용이 가능합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>은박지 뚜껑</span>이 남아있거나, 햇빛에 말려도 <span style='color: #ffcc00;'>오염이 심한 용기</span>는 일반 쓰레기로 버리세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재질 확인 팁</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>종이 재질</span> : 컵라면 용기 재질이 종이라면 '종량제 봉투'로 배출하는 것이 안전합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Strofoam/ramen_cup.png", ""},

    		        		                                                                                	                                        {"I071", "육류/생선 트레이", "C06", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 철저한 세척</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>주방세제 사용</span> : 핏물이나 기름기가 남지 않도록 깨끗이 씻어주세요.<br/>" +
    		        		                                                                                	                                            "      · 오염된 상태의 트레이는 재활용 가치가 현저히 떨어집니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                            "      · 바닥의 <span style='color: #ffcc00; font-weight: bold;'>흡수 패드</span>와 상단의 <span style='color: #ffcc00; font-weight: bold;'>랩 필름</span>은 반드시 제거하여 <span style='color: #ffcc00;'>일반 쓰레기</span>로 버리세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 유색 트레이 주의</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>색상 확인</span> : 검정색이나 유색 트레이는 지자체에 따라 일반 쓰레기로 분류될 수 있습니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Strofoam/meat_tray.png", ""},

    		        		                                                                                	                                        {"I072", "배달 용기(흰색)", "C06", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 흰색 스티로폼 선별</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>순수 흰색</span> : 음식물을 비우고 깨끗이 세척된 흰색 용기만 스티로폼으로 배출하세요.<br/>" +
    		        		                                                                                	                                            "      · 노란색이나 검정색 용기는 대부분 재활용이 어렵습니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                            "      · 세척 후에도 <span style='color: #ffcc00; font-weight: bold;'>붉은 양념 얼룩</span>이 심하게 남은 것은 <span style='color: #ffcc00;'>일반 쓰레기</span>로 버려야 합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 이물질 제거</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                            "      · 비닐 랩, 스티커 등 모든 이물질을 제거한 뒤 깨끗한 상태로 배출하세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Strofoam/white_delivery_box.png", ""},

    		        		                                                                                	                                        {"I073", "전자제품 보호 패드", "C06", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 정품 스티로폼 확인</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>딱딱한 재질</span> : 알갱이가 떨어지고 '뽀득' 소리가 나는 흰색 발포 스티렌은 스티로폼입니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                            "      · 스펀지처럼 <span style='color: #ffffff; font-weight: bold;'>말랑하고 질긴 재질</span>은 스티로폼이 아니므로 <span style='color: #ffcc00;'>일반 쓰레기</span>로 배출하세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 전 주의</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                            "      · 테이프나 스티커는 반드시 떼어내야 하며, 오염된 부분은 제거 후 배출하세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Strofoam/appliance_pad.png", ""},

    		        		                                                                                	                                        {"I074", "소형 택배 스티로폼", "C06", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 이물질 제거 필수</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>송장/테이프 제거</span> : 상자에 붙은 모든 부착물을 남김없이 떼어내세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                            "      · 소형 상자는 가벼워 잘 날아가므로 <span style='color: #ffffff; font-weight: bold;'>투명 비닐</span>에 담거나 <span style='color: #ffffff; font-weight: bold;'>노끈</span>으로 묶어서 배출하세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 오염 주의</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                            "      · 아이스팩 누출 등으로 오염이 심한 경우 <span style='color: #ffcc00;'>일반 쓰레기</span>로 처리해야 합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Strofoam/small_box.png", ""},
    		        		                                                                                	                                        {"I406", "난좌", "C06", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                                
    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 스티로폼 수거함 배출</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 깨끗하게 비운 난좌는 <span style='color: #ffcc00; font-weight: bold;'>스티로폼(발포합성수지) 수거함</span>에 배출합니다.<br/>" +
    		        		                                                                                	                                                "      · 부착된 테이프나 운송장 스티커 등 이물질은 반드시 제거해 주세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 종량제 봉투 배출 (오염 시)</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 음식물이나 오염 물질이 많이 묻어 <span style='color: #ffcc00; font-weight: bold;'>지워지지 않는 경우</span>에는 일반 쓰레기로 배출합니다.<br/>" +
    		        		                                                                                	                                                "      · 색상이 있는 스티로폼이나 과일 포장용 그물망 스티로폼도 지자체에 따라 일반 쓰레기로 분류될 수 있습니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
    		        		                                                                                	                                                "      <b style='color: #4ebfad;'>💡 놀라운 재활용 사실</b><br/>" +
    		        		                                                                                	                                                "      <span style='font-size: 13px; color: #ffffff;'>" +
    		        		                                                                                	                                                "        수거된 스티로폼은 열을 가해 녹인 뒤 <b style='color: #ffcc00;'>'잉고트(Ingot)'</b>라는 덩어리로 만들어집니다. 이 잉고트는 다시 가공되어 <b style='color: #ffcc00;'>사진 액자, 몰딩, 건축 자재</b> 등으로 화려하게 변신합니다." +
    		        		                                                                                	                                                "      </span>" +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ffcc00; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                "      <span><b style='color: #ffcc00;'>⚠️ 주의</b> : 컵라면 용기나 코팅된 스티로폼은 재활용 공정이 다르므로 별도 안내에 따라 배출해 주세요.</span>" +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/Strofoam/egg_foam_tray.png", ""},

    		        		                                                                                	                                        {"I075", "건축용 스티로폼 조각", "C06", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재활용 가능 조건</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>순수 단열재</span> : 인테리어 후 남은 <span style='color: #ffcc00;'>깨끗한 흰색 조각</span>만 스티로폼으로 배출 가능합니다.<br/>" +
    		        		                                                                                	                                            "      · 실리콘, 본드 등 부착물은 완벽히 제거해야 합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                            "      · 페인트나 시멘트가 묻은 것은 재활용이 절대 불가하므로 <span style='color: #ffffff; font-weight: bold;'>불연성 전용 마대</span>에 담아 배출하세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 주의사항</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                            "      · 건축 폐기물은 일반 종량제 봉투에 넣으면 수거가 거부될 수 있으니 주의하세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Strofoam/construction_insulation.png", ""},

    		        		                                                                                	                                        {"I084", "단열 벽지(스티로폼 재질)", "C06", 
    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                            
    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 배출 전 확인 (복합 재질)</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>알루미늄/접착제</span> : 뒷면에 은박 박막이나 강력 접착제가 붙은 제품은 재활용이 불가능합니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                            "      · 대부분의 단열 벽지는 복합 재질이므로 <span style='color: #ffffff; font-weight: bold;'>종량제 봉투(일반 쓰레기)</span>로 배출하는 것이 정석입니다." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 위험 안내</b></p>" +
    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
    		        		                                                                                	                                            "      · 접착 성분이 남은 채 배출하면 재활용 기계 고장이나 <span style='color: #ffcc00;'>화재의 원인</span>이 됩니다.<br/>" +
    		        		                                                                                	                                            "    </div>" +
    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 폼 블록형 시트지도 일반 쓰레기로 처리하세요." +
    		        		                                                                                	                                            "    </div>" +

    		        		                                                                                	                                            "  </div>" +
    		        		                                                                                	                                            "</div>", 
    		        		                                                                                	                                            "images/Strofoam/insulation_wallpaper.png", ""},
    	     // [플라스틱 - C07] (I085 ~ I102) 보강 버전
    		        		                                                                                	                                        {"I085", "국자(플라스틱)", "C07", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                                
    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재활용 가능 조건</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 전체가 <span style='color: #ffcc00; font-weight: bold;'>단일 플라스틱 재질(PP, PE 등)</span>로만 된 국자만 배출 가능합니다.<br/>" +
    		        		                                                                                	                                                "      · 음식물 등 이물질이 없도록 깨끗이 닦아서 배출해 주세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 불가 주의사항</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 손잡이가 <span style='color: #ffcc00; font-weight: bold;'>나무, 금속, 실리콘</span>인 혼합 재질은 재활용이 어렵습니다.<br/>" +
    		        		                                                                                	                                                "      · 분리가 불가능한 경우 <span style='color: #ffcc00;'>종량제 봉투(일반 쓰레기)</span>로 배출해야 합니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/plastic/plastic_ladle.png", "images/plastic/mark3.png"},

    		        		                                                                                	                                            {"I086", "그릇(플라스틱)", "C07", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                                
    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 깨끗한 세척 필수</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 남은 음식물과 기름기를 <span style='color: #ffcc00; font-weight: bold;'>주방세제로 세척</span> 후 건조하여 배출하세요.<br/>" +
    		        		                                                                                	                                                "      · 투명하거나 단색인 깨끗한 플라스틱만 재활용 가치가 높습니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                                "      · <span style='color: #ffcc00; font-weight: bold;'>양념 배임</span>이 심해 붉게 변했거나 기름기가 남은 그릇은 <span style='color: #ffcc00;'>일반 쓰레기</span>로 버리세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/plastic/plastic_bowl.png", "images/plastic/mark3.png"},

    		        		                                                                                	                                            {"I087", "도마(플라스틱)", "C07", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                                
    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 칼자국 사이 세척</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · <span style='color: #ffcc00; font-weight: bold;'>칼자국 틈새</span>에 낀 음식물을 솔로 깨끗이 닦아내고 배출해야 합니다.<br/>" +
    		        		                                                                                	                                                "      · 이물질이 남은 도마는 재활용 품질을 크게 떨어뜨립니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                                "      · <span style='color: #ffcc00; font-weight: bold;'>실리콘, 고무, 나무</span>가 섞인 도마는 플라스틱 배출함에 넣지 마세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                               
    		        		                                                                                	                                                "      <span><b style='color: #00fff0;'>💡 Tip</b> : <b style='color: #ffcc00;'>PP 재질</b> 위주로 배출하며, 마크가 없다면 일반 쓰레기로 권장합니다.</span>" +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/plastic/plastic_cutting_board.png", "images/plastic/mark3.png"},

    		        		                                                                                	                                            {"I088", "리코더", "C07", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                                
    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 본체 배출 방법</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 교육용 리코더는 <span style='color: #ffcc00; font-weight: bold;'>순수 플라스틱(ABS 등)</span>이므로 플라스틱류로 배출합니다.<br/>" +
    		        		                                                                                	                                                "      · 각 마디를 분해하여 부피를 줄인 뒤 배출하면 선별이 용이합니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                                "      · 청소용 솔과 천 케이스는 재활용 대상이 아니므로 <span style='color: #ffcc00;'>종량제 봉투</span>에 버려주세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/plastic/recorder.png", "images/plastic/mark3.png"},

    		        		                                                                                	                                            {"I089", "마요네즈/케찹 용기", "C07", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 내부 유분 제거</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 따뜻한 물과 세제를 넣고 흔들어 <span style='color: #ffcc00; font-weight: bold;'>미끈거림이 전혀 없게</span> 씻어내세요.<br/>" +
    		        		                                                                                	                                                "      · 소스 잔여물은 플라스틱 재활용 원료의 품질을 떨어뜨립니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                                "      · 입구의 <span style='color: #ffcc00; font-weight: bold;'>비닐 실링지</span>는 반드시 떼어내고, 뚜껑도 깨끗이 닦아 배출하세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                
    		        		                                                                                	                                                "      <span><b style='color: #00fff0;'>💡 Tip</b> : 세척이 불가능한 수준으로 오염되었다면 <b style='color: #ffcc00;'>일반 쓰레기</b>로 버리세요.</span>" +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/plastic/condiment_bottle.png", "images/plastic/mark3.png"},

    		        		                                                                                	                                            {"I090", "메가폰", "C07", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 본체 분해 배출</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 나사를 풀어 <span style='color: #ffcc00; font-weight: bold;'>내부 전선과 스피커 부품</span>을 모두 제거해야 합니다.<br/>" +
    		        		                                                                                	                                                "      · 부속이 제거된 <span style='color: #ffcc00; font-weight: bold;'>순수 플라스틱 외함</span>만 플라스틱류로 배출하세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                                "      · 분해가 어렵다면 <span style='color: #ffcc00; font-weight: bold;'>소형 가전 수거함</span>에 버리거나 <span style='color: #ffcc00;'>종량제 봉투</span>로 배출하세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                               
    		        		                                                                                	                                                "      <span><b style='color: #00fff0;'>💡 Tip</b> : 내부 <b style='color: #ffcc00;'>건전지</b>는 반드시 따로 폐건전지 수거함에 버려주세요.</span>" +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/plastic/megaphone.png", "images/plastic/mark7.png"},
    		        		                                                                                	                                            {"I910", "도시락 김 용기", "C07", 
    		        		                                                                                	                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                                    
    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 플라스틱 수거함 배출</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 내용물을 모두 비운 후 <span style='color: #ffcc00; font-weight: bold;'>플라스틱 전용 수거함</span>으로 배출합니다." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 15px;'>" +
    		        		                                                                                	                                                    "      <b style='color: #00fff0;'>💡 Tip</b><br/>" +
    		        		                                                                                	                                                    "      <span style='font-size: 13px; color: #ffffff;'>" +
    		        		                                                                                	                                                    "        김을 담았던 트레이는 <b style='color: #ffcc00;'>플라스틱 재질</b>로 제작되어 재활용이 가능합니다. 다만, 김 가루나 기름 등 이물질이 남아있으면 재활용 품질이 저하되므로 깨끗한 상태로 배출하는 것이 중요합니다." +
    		        		                                                                                	                                                    "      </span>" +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                    "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 김 봉지 안에 들어있는 <b style='color: #ffcc00;'>방습제(실리카겔)</b>는 재활용이 되지 않으므로 반드시 분리하여 <b style='color: #ffcc00;'>종량제 봉투</b>에 버려주세요.</span>" +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "  </div>" +
    		        		                                                                                	                                                    "</div>", 
    		        		                                                                                	                                                    "images/plastic/seaweed_tray.png", "images/plastic/mark4.png"},

    		        		                                                                                	                                            {"I091", "빨대", "C07", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 선별의 어려움</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 빨대는 크기가 너무 작아 선별 기계에서 분류되지 못하고 낙하합니다.<br/>" +
    		        		                                                                                	                                                "      · 가급적 <span style='color: #ffcc00; font-weight: bold;'>일반 종량제 봉투</span>로 배출하는 것이 가장 정확합니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
    		        		                                                                                	                                                "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
    		        		                                                                                	                                                "      · 수백 개를 모아 <span style='color: #ffcc00; font-weight: bold;'>페트병 안에 꽉 채우거나</span> 단단히 묶어 내놓을 때만 재활용이 가능합니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                             
    		        		                                                                                	                                                "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 작고 가벼운 플라스틱은 선별이 불가하여 <b style='color: #ffcc00;'>일반 쓰레기</b>로 권장됩니다.</span>" +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/plastic/straw.png", "images/plastic/mark7.png"},

    		        		                                                                                	                                            {"I092", "볼풀공", "C07", 
    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재질 확인 및 세척</b></p>" +
    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                "      · 볼풀공은 <span style='color: #ffcc00; font-weight: bold;'>PE(폴리에틸렌)</span> 재질로 재활용 가치가 매우 높습니다.<br/>" +
    		        		                                                                                	                                                "      · 먼지나 오염이 묻었다면 가볍게 닦아서 배출해 주세요." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                                "      · 공을 담고 있던 <span style='color: #ffffff; font-weight: bold;'>그물망</span>은 플라스틱이 아닌 <span style='color: #ffcc00;'>비닐류</span>로 따로 분리해야 합니다." +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                
    		        		                                                                                	                                                "      <span><b style='color: #00fff0;'>💡 Tip</b> : 깨끗한 볼풀공은 <b style='color: #ffcc00;'>최고의 재활용 원료</b>가 되니 잘 분리해 주세요.</span>" +
    		        		                                                                                	                                                "    </div>" +

    		        		                                                                                	                                                "  </div>" +
    		        		                                                                                	                                                "</div>", 
    		        		                                                                                	                                                "images/plastic/ball_pool.png", "images/plastic/mark6.png"},
    		        		                                                                                	                                            {"I093", "분무기", "C07", 
    		        		                                                                                	                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 헤드(노즐) 배출 방법</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 분무기 헤드 내부에는 <span style='color: #ffcc00; font-weight: bold;'>금속 스프링</span>이 포함되어 있어 재활용이 불가능합니다.<br/>" +
    		        		                                                                                	                                                    "      · 헤드 부분은 반드시 본체와 분리하여 <span style='color: #ffcc00;'>일반 종량제 봉투</span>에 버려주세요." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 몸체 배출 방법</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 몸체 안의 액체를 완전히 비우고 물로 깨끗이 세척합니다.<br/>" +
    		        		                                                                                	                                                    "      · 겉면의 <span style='color: #ffcc00; font-weight: bold;'>라벨을 깨끗이 제거</span>한 뒤 순수 플라스틱으로 배출하세요." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                    
    		        		                                                                                	                                                    "      <span><b style='color: #00fff0;'>💡 Tip</b> : <b style='color: #ffcc00;'>금속 스프링이 든 헤드</b>를 분리하는 것이 가장 중요합니다. 몸체만 플라스틱으로 배출하세요!</span>" +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "  </div>" +
    		        		                                                                                	                                                    "</div>", 
    		        		                                                                                	                                                    "images/plastic/spray_bottle.png", "images/plastic/mark3.png"},

    		        		                                                                                	                                                {"I094", "비디오테이프", "C07", 
    		        		                                                                                	                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 완전 분해 배출 방법</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 나사를 풀어 내부의 <span style='color: #ffcc00; font-weight: bold;'>자기 테이프(필름)와 금속 부품</span>을 반드시 제거해야 합니다.<br/>" +
    		        		                                                                                	                                                    "      · 필름을 제외한 <span style='color: #ffcc00; font-weight: bold;'>외부 플라스틱 케이스</span>만 플라스틱류로 배출이 가능합니다." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                                    "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
    		        		                                                                                	                                                    "      · 내부 필름은 재활용이 절대 불가능하므로 반드시 <span style='color: #ffcc00;'>일반 종량제 봉투</span>에 담아 버려주세요.<br/>" +
    		        		                                                                                	                                                    "      · 분해가 어렵다면 통째로 <span style='color: #ffffff;'>일반 쓰레기</span>로 배출하세요." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                    
    		        		                                                                                	                                                    "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 테이프는 복합 재질입니다. 분해하지 않고 배출하면 <b style='color: #ffcc00;'>100% 폐기</b>되니 주의하세요!</span>" +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "  </div>" +
    		        		                                                                                	                                                    "</div>", 
    		        		                                                                                	                                                    "images/plastic/vhs_tape.png", "images/plastic/mark7.png"},

    		        		                                                                                	                                                {"I095", "샴푸 용기", "C07", 
    		        		                                                                                	                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 펌프(노즐) 분리 필수</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 펌프 노즐 내부에는 <span style='color: #ffcc00; font-weight: bold;'>작은 금속 스프링</span>이 들어있어 재활용이 안 됩니다.<br/>" +
    		        		                                                                                	                                                    "      · 펌프는 반드시 <span style='color: #ffcc00;'>일반 종량제 봉투</span>로 배출해 주세요." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 본체 세척 및 배출</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 내부 잔여물이 없도록 <span style='color: #ffcc00; font-weight: bold;'>물로 여러 번 깨끗이</span> 헹궈주세요.<br/>" +
    		        		                                                                                	                                                    "      · 라벨을 제거한 뒤 깨끗해진 용기 본체만 <span style='color: #ffcc00; font-weight: bold;'>플라스틱류</span>로 배출합니다." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                    
    		        		                                                                                	                                                    "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 펌프를 꽂은 채 배출하면 <b style='color: #ffcc00;'>용기 전체가 폐기물</b>로 처리됩니다. 꼭 분리해 주세요!</span>" +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "  </div>" +
    		        		                                                                                	                                                    "</div>", 
    		        		                                                                                	                                                    "images/plastic/shampoo_bottle.png", "images/plastic/mark3.png"},

    		        		                                                                                	                                                {"I096", "쓰레받기", "C07", 
    		        		                                                                                	                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 부착물 제거 안내</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 끝부분의 <span style='color: #ffcc00; font-weight: bold;'>고무날이나 철핀, 고정 끈</span>은 반드시 제거하여 일반 쓰레기로 버려주세요.<br/>" +
    		        		                                                                                	                                                    "      · 부착물이 남은 경우 재활용 공정에서 선별되지 않습니다." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 전 준비</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 바닥면과 틈새의 <span style='color: #ffcc00; font-weight: bold;'>먼지와 흙</span>을 깨끗이 털어내야 합니다.<br/>" +
    		        		                                                                                	                                                    "      · 부착물이 완전히 제거된 <span style='color: #ffcc00; font-weight: bold;'>순수 플라스틱 본체</span>만 플라스틱류로 배출하세요." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                 
    		        		                                                                                	                                                    "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 대부분 <b style='color: #ffcc00;'>PP 재질</b>이지만, 고무날이 붙어있으면 재활용이 어렵습니다. 꼭 떼어내 주세요!</span>" +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "  </div>" +
    		        		                                                                                	                                                    "</div>", 
    		        		                                                                                	                                                    "images/plastic/dustpan.png", "images/plastic/mark3.png"},
    		        		                                                                                	                                                {"I122", "계량컵", "C07", 
    		        		                                                                                	                                                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
    		        		                                                                                	                                                        
    		        		                                                                                	                                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재질별 배출 방법</b></p>" +
    		        		                                                                                	                                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                        "      · <span style='color: #00fff0; font-weight: bold;'>플라스틱/금속:</span> 내용물을 비우고 전용 수거함으로 배출합니다.<br/>" +
    		        		                                                                                	                                                        "      · <span style='color: #ff6b6b; font-weight: bold;'>내열유리:</span> 재활용이 불가능하므로 <span style='color: #ffcc00;'>불연성 종량제 봉투(마대)</span>로 배출합니다." +
    		        		                                                                                	                                                        "    </div>" +

    		        		                                                                                	                                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 특징 및 처리 과정</b></p>" +
    		        		                                                                                	                                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                        "      · 계량컵은 내열유리, 스테인리스, 플라스틱 등 다양한 재질로 제작됩니다.<br/>" +
    		        		                                                                                	                                                        "      · 내열유리는 일반 유리와 녹는점이 달라 섞일 경우 재활용 공정에 차질을 줍니다.<br/>" +
    		        		                                                                                	                                                        "      · 불연성 폐기물은 수거 후 <span style='color: #00fff0;'>매립장으로 운반되어 매립</span> 처리됩니다." +
    		        		                                                                                	                                                        "    </div>" +

    		        		                                                                                	                                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
    		        		                                                                                	                                                        "      · <span style='color: #ff6b6b; font-weight: bold;'>🚨 주의사항</span><br/>" +
    		        		                                                                                	                                                        "      · 와인잔, 사기그릇, 내열냄비 등도 계량컵(유리)과 마찬가지로 <span style='color: #ffffff; font-weight: bold;'>불연성 종량제 봉투</span> 배출 대상입니다." +
    		        		                                                                                	                                                        "    </div>" +

    		        		                                                                                	                                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                        "      <img src='images/plastic/mark3.png' style='width: 45px; height: 45px;' alt='플라스틱'>" +
    		        		                                                                                	                                                        "      <span><b style='color: #00fff0;'>유사 품목:</b> 냄비뚜껑(내열), 뚝배기, 머그컵, 깨진 유리, 화분 등</span>" +
    		        		                                                                                	                                                        "    </div>" +

    		        		                                                                                	                                                        "  </div>" +
    		        		                                                                                	                                                        "</div>", 
    		        		                                                                                	                                                        "images/plastic/measuring_cup.png", "images/plastic/mark3.png"},

    		        		                                                                                	                                                {"I097", "식용유 용기", "C07", 
    		        		                                                                                	                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 유분 완전 제거</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · <span style='color: #ffcc00; font-weight: bold;'>주방세제와 따뜻한 물</span>을 넣어 유분이 전혀 없을 때까지 씻으세요.<br/>" +
    		        		                                                                                	                                                    "      · 제대로 씻지 않은 기름병은 <span style='color: #ffcc00; font-weight: bold;'>다른 깨끗한 자원까지 오염</span>시킵니다." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 라벨 및 부속품 분리</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 겉면의 <span style='color: #ffcc00; font-weight: bold;'>비닐 라벨</span>은 반드시 떼어내어 비닐류로 따로 배출해 주세요.<br/>" +
    		        		                                                                                	                                                    "      · 뚜껑 아래의 플라스틱 고리 등도 최대한 제거하는 것이 좋습니다." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                   
    		        		                                                                                	                                                    "      <span><b style='color: #00fff0;'>💡 Tip</b> : 세척 후에도 기름기가 남는다면 <b style='color: #ffcc00;'>일반 쓰레기</b>로 배출하는 것이 바람직합니다.</span>" +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "  </div>" +
    		        		                                                                                	                                                    "</div>", 
    		        		                                                                                	                                                    "images/plastic/oil_bottle.png", "images/plastic/mark2.png"},

    		        		                                                                                	                                                {"I098", "젖병", "C07", 
    		        		                                                                                	                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 젖꼭지(실리콘) 분리 필수</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 말랑한 젖꼭지는 <span style='color: #ffcc00; font-weight: bold;'>실리콘 재질</span>로, 플라스틱 재활용이 불가능합니다.<br/>" +
    		        		                                                                                	                                                    "      · 젖꼭지는 반드시 <span style='color: #ffcc00;'>일반 종량제 봉투</span>에 담아 배출해 주세요." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 본체 및 캡 세척</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 내부의 <span style='color: #ffcc00; font-weight: bold;'>우유 잔여물</span>이 없도록 깨끗이 세척 후 건조해 주세요.<br/>" +
    		        		                                                                                	                                                    "      · 투명한 본체와 뚜껑(캡)만 모아서 <span style='color: #ffcc00; font-weight: bold;'>플라스틱류</span>로 배출합니다." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ffcc00; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                   
    		        		                                                                                	                                                    "      <span><b style='color: #ffcc00;'>⚠️ 주의</b> : PPSU나 PP 재질이라도 <b style='color: #ffcc00;'>실리콘이 섞이면 재활용 품질이 저하</b>됩니다. 꼭 분리하세요!</span>" +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "  </div>" +
    		        		                                                                                	                                                    "</div>", 
    		        		                                                                                	                                                    "images/plastic/baby_bottle.png", "images/plastic/mark3.png"},

    		        		                                                                                	                                                {"I099", "치약 용기", "C07", 
    		        		                                                                                	                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
    		        		                                                                                	                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 튜브 본체는 일반 쓰레기</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 치약 튜브는 플라스틱과 은박지가 겹쳐진 <span style='color: #ffcc00; font-weight: bold;'>복합 재질(Other)</span>로 재활용이 어렵습니다.<br/>" +
    		        		                                                                                	                                                    "      · 본체는 그대로 <span style='color: #ffcc00;'>일반 종량제 봉투</span>에 담아 배출해 주세요." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 뚜껑(캡) 분리 배출</b></p>" +
    		        		                                                                                	                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
    		        		                                                                                	                                                    "      · 단일 플라스틱 재질인 <span style='color: #ffcc00; font-weight: bold;'>뚜껑만 따로 분리</span>하여 플라스틱류로 배출하세요.<br/>" +
    		        		                                                                                	                                                    "      · 만약 본체가 단일 재질 마크가 있다면 내부 세척 후 배출이 가능합니다." +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
    		        		                                                                                	                                                   
    		        		                                                                                	                                                    "      <span><b style='color: #00fff0;'>💡 Tip</b> : 튜브 속 잔여물은 오염의 원인이 됩니다. <b style='color: #ffcc00;'>씻기 힘든 튜브는 일반 쓰레기</b>로 버리는 것이 정답입니다!</span>" +
    		        		                                                                                	                                                    "    </div>" +

    		        		                                                                                	                                                    "  </div>" +
    		        		                                                                                	                                                    "</div>", 
    		        		                                                                                	                                                    "images/plastic/toothpaste.png", "images/plastic/mark7.png"
	                                                                                                            											},
    		        		                                                                                	                                                {"I100", "플라스틱 뚜껑/캡", "C07", 
	                                                                                                            										        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

	                                                                                                            										        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 작은 뚜껑의 선별 한계</b></p>" +
	                                                                                                            										        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										        "      · 병뚜껑처럼 크기가 작은 플라스틱은 선별 기계 벨트 사이로 떨어져 <span style='color: #ffcc00; font-weight: bold;'>폐기될 확률</span>이 매우 높습니다.<br/>" +
	                                                                                                            										        "      · 낱개로 배출하기보다 모아서 배출하는 것이 자원 순환에 훨씬 유리합니다." +
	                                                                                                            										        "    </div>" +

	                                                                                                            										        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 자원 순환을 위한 배출 팁</b></p>" +
	                                                                                                            										        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										        "      · <span style='color: #ffcc00; font-weight: bold;'>병뚜껑 전용 수거함</span>(제로웨이스트 샵 등)을 통해 따로 배출하는 것을 권장합니다.<br/>" +
	                                                                                                            										        "      · 여의치 않다면 투명 페트병 안에 <span style='color: #ffcc00; font-weight: bold;'>뚜껑을 가득 채워</span> 한 번에 배출해 주세요." +
	                                                                                                            										        "    </div>" +

	                                                                                                            										        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										        
	                                                                                                            										        "      <span><b style='color: #00fff0;'>💡 Tip</b> : 모인 뚜껑은 <b style='color: #ffcc00;'>치약 짜개, 인테리어 소품</b> 등 가치 있는 업사이클링 제품으로 100% 재탄생합니다!</span>" +
	                                                                                                            										        "    </div>" +

	                                                                                                            										        "  </div>" +
	                                                                                                            										        "</div>", 
	                                                                                                            										        "images/plastic/plastic_caps.png", "images/plastic/mark3.png"},
    		        		                                                                                	                                                
    		        		                                                                                	                                                

	                                                                                                            										    {"I101", "플라스틱 컵", "C07", 
	                                                                                                            										        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

	                                                                                                            										        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 내용물 비우기 및 분리</b></p>" +
	                                                                                                            										        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										        "      · 남은 음료는 비우고 물로 헹궈 <span style='color: #ffcc00; font-weight: bold;'>이물질을 제거</span>한 뒤 건조해 주세요.<br/>" +
	                                                                                                            										        "      · <span style='color: #ffcc00; font-weight: bold;'>빨대와 종이 홀더</span>는 반드시 분리하여 각각의 재질에 맞게 배출해야 합니다." +
	                                                                                                            										        "    </div>" +

	                                                                                                            										        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
	                                                                                                            										        "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
	                                                                                                            										        "      · 로고 인쇄가 과도하거나 <span style='color: #ffcc00; font-weight: bold;'>색깔이 있는 컵</span>은 일반 플라스틱으로 분류되므로 투명 페트병함에 넣지 마세요." +
	                                                                                                            										        "    </div>" +

	                                                                                                            										        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ffcc00; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										        
	                                                                                                            										        "      <span><b style='color: #ffcc00;'>💡 Tip</b> : 컵 보증금제 로고가 있다면 판매처 반납 시 <b style='color: #ffcc00;'>300원</b>을 돌려받을 수 있습니다!</span>" +
	                                                                                                            										        "    </div>" +

	                                                                                                            										        "  </div>" +
	                                                                                                            										        "</div>", 
	                                                                                                            										        "images/plastic/plastic_cup.png", "images/plastic/mark2.png"},

	                                                                                                            										    {"I102", "페트병(투명)", "C07", 
	                                                                                                            										        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +

	                                                                                                            										        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 고품질 배출 4단계</b></p>" +
	                                                                                                            										        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										        "      · <span style='color: #ffcc00; font-weight: bold;'>비우기 :</span> 남은 내용물을 깨끗이 비우고 헹굽니다.<br/>" +
	                                                                                                            										        "      · <span style='color: #ffcc00; font-weight: bold;'>라벨 떼기 :</span> 비닐 라벨을 제거하여 비닐류로 따로 배출하세요.<br/>" +
	                                                                                                            										        "      · <span style='color: #ffcc00; font-weight: bold;'>>찌그러뜨리기 :</span> 발로 밟아 부피를 최소화합니다.<br/>" +
	                                                                                                            										        "      · <span style='color: #ffcc00; font-weight: bold;'>뚜껑 닫기 :</span>> 이물질이 들어가지 않도록 뚜껑을 닫아 배출하세요." +
	                                                                                                            										        "    </div>" +

	                                                                                                            										        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 장소 엄수</b></p>" +
	                                                                                                            										        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										        "      · 유색 플라스틱과 섞이지 않게 반드시 <span style='color: #ffcc00; font-weight: bold;'>투명 페트병 전용 수거함</span>에 넣어주세요.<br/>" +
	                                                                                                            										        "      · 잘 분리된 페트병은 고품질 <span style='color: #ffcc00;'>기능성 의류나 가방</span>의 원료가 됩니다." +
	                                                                                                            										        "    </div>" +

	                                                                                                            										        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										        
	                                                                                                            										        "      <span><b style='color: #00fff0;'>💡 Tip</b> : 투명 페트병은 <b style='color: #ffcc00;'>순환 자원</b>으로서 가치가 매우 높습니다. 올바른 분리가 환경 보호의 첫걸음입니다!</span>" +
	                                                                                                            										        "    </div>" +

	                                                                                                            										        "  </div>" +
	                                                                                                            										        "</div>", 
	                                                                                                            										        "images/plastic/transparent_pet.png", "images/plastic/mark1.png"},
    	     // [기타 - C08] (I103 ~ I138) 보강 버전
	                                                                                                            										    {"I103", "공학용 계산기", "C08", 
	                                                                                                            										            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										            
	                                                                                                            										            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 가전 배출 방법</b></p>" +
	                                                                                                            										            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										            "      · 계산기는 소형 가전으로 분류되어 <span style='color: #ffcc00; font-weight: bold;'>소형전기전자제품 전용수거함</span>으로 배출합니다.<br/>" +
	                                                                                                            										            "      · 인근에 수거함이 없다면 <span style='color: #ffcc00;'>주민센터</span>에 문의하거나 가전 무상방문수거를 이용하세요." +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
	                                                                                                            										            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
	                                                                                                            										            "      · 내장된 <span style='color: #ffffff; font-weight: bold;'>건전지나 전지</span>는 반드시 분리하여 <span style='color: #ffcc00;'>폐건전지 수거함</span>에 따로 배출해야 합니다." +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										            "      <span><b style='color: #00fff0;'>💡 Tip</b> : 타 가전제품 배출 시 <b style='color: #ffcc00;'>무상방문수거서비스</b>를 이용하면 함께 배출이 가능하여 편리합니다.</span>" +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "  </div>" +
	                                                                                                            										            "</div>", 
	                                                                                                            										            "images/Clothing/calculator.png", ""},
	                                                                                                            										        
	                                                                                                            										        {"I104", "구두", "C08", 
	                                                                                                            										            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										            
	                                                                                                            										            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 재사용 가능 시 (상태 양호)</b></p>" +
	                                                                                                            										            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										            "      · 외관이 깨끗하고 굽이 살아있는 구두는 <span style='color: #ffcc00; font-weight: bold;'>헌옷 수거함</span>으로 배출 가능합니다.<br/>" +
	                                                                                                            										            "      · 좌우 한 켤레가 흩어지지 않게 <span style='color: #ffcc00;'>끈으로 묶거나 봉투에 담아</span> 배출해 주세요." +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재사용 불가능 시 (파손/오염)</b></p>" +
	                                                                                                            										            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										            "      · 심하게 낡거나 밑창이 떨어진 구두, 장화 등은 재활용 대상이 아닙니다.<br/>" +
	                                                                                                            										            "      · 이런 경우에는 <span style='color: #ffcc00; font-weight: bold;'>종량제 봉투(일반 쓰레기)</span>에 담아 배출해 주세요." +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										            "      <span><b style='color: #00fff0;'>💡 Tip</b> : 지역별로 헌옷 수거함에 <b style='color: #ffffff;'>신발류 투입을 금지</b>하는 곳이 있으니 수거함 안내 문구를 꼭 확인하세요.</span>" +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "  </div>" +
	                                                                                                            										            "</div>", 
	                                                                                                            										            "images/Clothing/shoes.png", ""},

	                                                                                                            										        {"I105", "샌들", "C08", 
	                                                                                                            										            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										            
	                                                                                                            										            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 헌옷 수거함 배출 (상태 양호)</b></p>" +
	                                                                                                            										            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										            "      · 가죽이나 고무 재질의 <span style='color: #ffcc00; font-weight: bold;'>일반 패션 샌들</span>은 깨끗한 경우 수거 가능합니다.<br/>" +
	                                                                                                            										            "      · 짝이 맞지 않으면 폐기되므로 반드시 <span style='color: #ffcc00;'>좌우 한 켤레를 묶어서</span> 배출해 주세요." +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
	                                                                                                            										            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
	                                                                                                            										            "      · <span style='color: #ffcc00; font-weight: bold;'>EVA 재질(크록스 등), 욕실 슬리퍼, 젤리슈즈</span>는 재활용 가치가 없어 수거 대상이 아닙니다." +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: #00fff0 4px solid; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										            "      <span><b style='color: #00fff0;'>💡 Tip</b> : 수거 불가 샌들은 고민 없이 <b style='color: #ffcc00;'>종량제 봉투(일반 쓰레기)</b>로 처리하는 것이 정확합니다.</span>" +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "  </div>" +
	                                                                                                            										            "</div>", 
	                                                                                                            										            "images/Clothing/sandals.png", ""},
	                                                                                                            										        {"I502", "노트북 파우치", "C08", 
	                                                                                                            										                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                
	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 의류수거함 배출 (재사용 가능 시)</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 오염이 없고 상태가 양호하여 <span style='color: #ffcc00; font-weight: bold;'>타인이 재사용할 수 있는 경우</span>에는 의류수거함에 배출합니다.<br/>" +
	                                                                                                            										                "      · 수거된 파우치는 선별 과정을 거쳐 국내외로 재사용(Reuse)됩니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 종량제 봉투 배출 (폐기 시)</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 심하게 해지거나 오염되어 <span style='color: #ffcc00; font-weight: bold;'>재사용이 불가능한 경우</span>에는 일반 쓰레기(종량제 봉투)로 배출합니다.<br/>" +
	                                                                                                            										                "      · 천, 가죽, 지퍼(금속) 등이 혼합된 복합재질 제품은 분리배출이 어려워 소각 또는 매립됩니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
	                                                                                                            										                "      <b style='color: #4ebfad;'>💡 분리배출 팁</b><br/>" +
	                                                                                                            										                "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                "        의류수거함은 단순히 버리는 곳이 아니라 <b style='color: #ffcc00;'>'나눔과 재사용'</b>을 위한 공간입니다. 사용 가능한 제품만 선별하여 배출하는 것이 자원 순환의 시작입니다." +
	                                                                                                            										                "      </span>" +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ffcc00; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                "      <span><b style='color: #ffcc00;'>⚠️ 주의</b> : 지자체별로 의류수거함의 수거 품목이 다를 수 있습니다. 가방류 배출 가능 여부를 미리 확인해 주세요.</span>" +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "  </div>" +
	                                                                                                            										                "</div>", 
	                                                                                                            										                "images/Clothing/laptop_pouch.png", ""},

	                                                                                                            										        {"I106", "슬리퍼", "C08", 
	                                                                                                            										            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										            
	                                                                                                            										            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 배출 원칙</b></p>" +
	                                                                                                            										            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										            "      · 슬리퍼는 고무, 스펀지, 천 등이 접착된 혼합 재질로 <span style='color: #ffcc00; font-weight: bold;'>재활용이 불가능</span>합니다.<br/>" +
	                                                                                                            										            "      · 실내용, 욕실용, 가죽형 모두 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기(종량제 봉투)</span>로 배출하세요." +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
	                                                                                                            										            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
	                                                                                                            										            "      · 대부분의 <span style='color: #ffcc00; font-weight: bold;'>헌옷 수거함</span>에서도 슬리퍼는 수거 금지 품목입니다. 섞여 들어가지 않게 주의하세요." +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										            "      <span><b style='color: #00fff0;'>💡 Tip</b> : EVA(말랑한 고무형) 재질이라도 재활용 선별 시스템상 <b style='color: #ffcc00;'>일반 쓰레기</b>로 버리는 것이 원칙입니다.</span>" +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "  </div>" +
	                                                                                                            										            "</div>", 
	                                                                                                            										            "images/Clothing/slippers.png", ""},

	                                                                                                            										        {"I107", "머플러/목도리", "C08", 
	                                                                                                            										            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										            
	                                                                                                            										            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 방법</b></p>" +
	                                                                                                            										            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										            "      · 깨끗한 면, 실크, 울 재질의 목도리는 <span style='color: #ffcc00; font-weight: bold;'>헌옷 수거함</span>에 배출할 수 있습니다.<br/>" +
	                                                                                                            										            "      · 타인이 <span style='color: #ffcc00;'>재사용할 수 있는 상태</span>일 때만 수거함으로 배출해 주세요." +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
	                                                                                                            										            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span><br/>" +
	                                                                                                            										            "      · 비에 젖으면 곰팡이가 생겨 다른 옷까지 훼손되므로, <span style='color: #ffcc00; font-weight: bold;'>깨끗한 비닐봉지</span>에 담아 묶어서 배출하세요." +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										            "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 심하게 낡거나 헤진 목도리는 재활용이 안 되므로 <b style='color: #ffcc00;'>종량제 봉투(일반 쓰레기)</b>로 처리해 주세요.</span>" +
	                                                                                                            										            "    </div>" +

	                                                                                                            										            "  </div>" +
	                                                                                                            										            "</div>", 
	                                                                                                            										            "images/Clothing/muffler.png", ""},
	                                                                                                            										        {"I108", "모자", "C08", 
	                                                                                                            										                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                
	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 헌옷 수거함 배출 (상태 양호)</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 형태가 보존된 야구모자, 스냅백, 비니 등은 <span style='color: #ffcc00; font-weight: bold;'>헌옷 수거함</span>에 배출 가능합니다.<br/>" +
	                                                                                                            										                "      · 배출 시 모자가 눌려 <span style='color: #ffcc00;'>앞챙이 꺾이지 않도록</span> 주의해서 넣어주세요." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재활용 불가 및 폐기 대상</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · <span style='color: #ffcc00; font-weight: bold;'>앞챙이 부러졌거나</span> 심하게 휘어진 모자<br/>" +
	                                                                                                            										                "      · 땀이나 화장품으로 인해 <span style='color: #ffcc00;'>변색 및 오염이 심한</span> 모자" +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 재사용이 불가능한 모자는 <b style='color: #ffcc00;'>일반 쓰레기(종량제 봉투)</b>로 배출하세요. 헬멧은 별도의 대형 폐기물 규정을 따릅니다.</span>" +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "  </div>" +
	                                                                                                            										                "</div>", 
	                                                                                                            										                "images/Clothing/hat.png", ""},
	                                                                                                            										        {"I201", "게임 컨트롤러", "C08", 
	                                                                                                            										                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                    
	                                                                                                            										                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 폐가전 수거함 배출</b></p>" +
	                                                                                                            										                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                    "      · 게임 컨트롤러는 <span style='color: #ffcc00; font-weight: bold;'>소형전기전자제품 전용수거함</span>으로 배출합니다.<br/>" +
	                                                                                                            										                    "      · 인근에 수거함이 없는 경우 관할 지자체(주민센터 등)에 문의 후 배출하세요." +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 무상 방문수거 서비스 (다량 배출)</b></p>" +
	                                                                                                            										                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                    "      · 컨트롤러 포함 소형 가전제품을 <span style='color: #ffcc00; font-weight: bold;'>5개 이상</span> 한 번에 배출 시 이용 가능합니다.<br/>" +
	                                                                                                            										                    "      · <span style='color: #ffcc00;font-weight: bold;'>폐가전 무상방문수거서비스(1599-0903)</span>를 통해 간편하게 배출하세요." +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
	                                                                                                            										                    "      <b style='color: #4ebfad;'>💡 Tip</b><br/>" +
	                                                                                                            										                    "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                    "        게임 컨트롤러는 유해물질 사용 억제 및 재활용 촉진 대상인 <b style='color: #ffcc00;'>환경성보장제도</b> 품목에 해당하여 자원순환 체계에 따라 안전하게 처리됩니다." +
	                                                                                                            										                    "      </span>" +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                    "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 지역별로 배출 방법이 상이할 수 있으므로, 해당 <b style='color: #ffcc00;'>지방자치단체의 규정</b>이 있는 경우 그에 따르시기 바랍니다.</span>" +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "  </div>" +
	                                                                                                            										                    "</div>", 
	                                                                                                            										                    "images/Clothing/game_controller.png", ""},
	                                                                                                            										        {"I202", "공유기", "C08", 
	                                                                                                            										                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                        
	                                                                                                            										                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 폐가전 수거함 배출</b></p>" +
	                                                                                                            										                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                        "      · 공유기는 <span style='color: #ffcc00; font-weight: bold;'>소형전기전자제품 전용수거함</span>으로 상시 배출이 가능합니다.<br/>" +
	                                                                                                            										                        "      · 배출 전 본체에 연결된 랜선과 어댑터를 정리하여 함께 배출해 주세요." +
	                                                                                                            										                        "    </div>" +

	                                                                                                            										                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 5개 이상 묶음 배출 (무상방문수거)</b></p>" +
	                                                                                                            										                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                        "      · 공유기 단독 배출이 아닌, 다른 소형 가전과 함께 <span style='color: #ffcc00; font-weight: bold;'>5개 이상</span> 배출 시 방문 수거가 가능합니다.<br/>" +
	                                                                                                            										                        "      · <span style='color: #ffcc00;font-weight: bold;'>폐가전 무상방문수거서비스(1599-0903)</span> 또는 홈페이지를 통해 예약하세요." +
	                                                                                                            										                        "    </div>" +

	                                                                                                            										                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
	                                                                                                            										                        "      <b style='color: #4ebfad;'>💡 Tip</b><br/>" +
	                                                                                                            										                        "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                        "        공유기는 자원순환법에 따른 <b style='color: #ffcc00;'>환경성보장제도</b> 대상 품목입니다. 유해물질을 억제하고 재활용이 용이하도록 관리되는 품목이므로 반드시 전용 수거 체계를 이용해야 합니다." +
	                                                                                                            										                        "      </span>" +
	                                                                                                            										                        "    </div>" +

	                                                                                                            										                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                        "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 핸드폰 케이스, 액정보호필름 등 <b style='color: #ffcc00;'>비가전 부속품</b>은 재활용이 불가하므로 <b style='color: #ffcc00;'>일반 종량제 봉투</b>에 배출하세요.</span>" +
	                                                                                                            										                        "    </div>" +

	                                                                                                            										                        "  </div>" +
	                                                                                                            										                        "</div>", 
	                                                                                                            										                        "images/Clothing/router.png", ""},
	                                                                                                            										        {"I203", "거품반죽기", "C08", 
	                                                                                                            										                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                            
	                                                                                                            										                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 폐가전 수거함 배출</b></p>" +
	                                                                                                            										                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                            "      · 거품반죽기는 <span style='color: #ffcc00; font-weight: bold;'>소형전기전자제품 전용수거함</span>에 배출해 주세요.<br/>" +
	                                                                                                            										                            "      · 배출 시 반죽 날(비터) 등 금속 부속품이 분실되지 않도록 본체와 함께 배출하는 것이 좋습니다." +
	                                                                                                            										                            "    </div>" +

	                                                                                                            										                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 무상 방문수거 (5개 이상)</b></p>" +
	                                                                                                            										                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                            "      · 거품반죽기를 포함하여 소형 가전제품을 <span style='color: #ffcc00; font-weight: bold;'>5개 이상 모았을 때</span> 방문 수거 신청이 가능합니다.<br/>" +
	                                                                                                            										                            "      · <span style='color: #ffcc00; font-weight: bold;'>폐가전 무상방문수거서비스(1599-0903)</span>를 이용하면 편리합니다." +
	                                                                                                            										                            "    </div>" +

	                                                                                                            										                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
	                                                                                                            										                            "      <b style='color: #4ebfad;'>💡 Tip</b><br/>" +
	                                                                                                            										                            "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                            "        거품반죽기는 자원의 효율적 이용을 위한 <b style='color: #ffcc00;'>환경성보장제도</b> 해당 품목입니다. 적정 절차에 따라 배출하면 유해물질은 안전하게 처리되고 소중한 자원으로 재탄생합니다." +
	                                                                                                            										                            "      </span>" +
	                                                                                                            										                            "    </div>" +

	                                                                                                            										                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                            "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 지역에 따라 전용 수거함 위치가 다를 수 있으니 <b style='color: #ffcc00;'>관할 주민센터</b> 홈페이지 등을 통해 확인 후 배출하시기 바랍니다.</span>" +
	                                                                                                            										                            "    </div>" +

	                                                                                                            										                            "  </div>" +
	                                                                                                            										                            "</div>", 
	                                                                                                            										                            "images/Clothing/hand_mixer.png", ""},
	                                                                                                            										        {"I204", "고데기", "C08", 
	                                                                                                            										                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                                
	                                                                                                            										                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 폐가전 수거함 배출</b></p>" +
	                                                                                                            										                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                "      · 고데기는 <span style='color: #ffcc00; font-weight: bold;'>소형전기전자제품 전용수거함</span>에 배출해 주세요.<br/>" +
	                                                                                                            										                                "      · 전선이 꼬이지 않게 잘 정리하여 배출하며, 열판에 이물질이 묻어있지 않도록 닦아주세요." +
	                                                                                                            										                                "    </div>" +

	                                                                                                            										                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 무상 방문수거 동시 배출</b></p>" +
	                                                                                                            										                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                "      · 고데기는 환경성보장제도 비대상 품목이지만, <span style='color: #ffcc00; font-weight: bold;'>다른 소형가전(5개 이상) 배출 시</span> 함께 배출할 수 있습니다.<br/>" +
	                                                                                                            										                                "      · 폐가전 무상방문수거서비스(1599-0903) 예약 시 목록에 포함하세요." +
	                                                                                                            										                                "    </div>" +

	                                                                                                            										                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
	                                                                                                            										                                "      <b style='color: #4ebfad;'>💡 Tip</b><br/>" +
	                                                                                                            										                                "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                                "        고데기는 환경성보장제도 50대 품목에는 포함되지 않으나, <b style='color: #ffcc00;'>재활용이 가능한 가전제품</b>입니다. 일반 쓰레기보다는 전용 수거함을 이용하는 것이 자원 순환에 큰 도움이 됩니다." +
	                                                                                                            										                                "      </span>" +
	                                                                                                            										                                "    </div>" +

	                                                                                                            										                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                                "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 인근에 수거함이 없거나 소량 배출 시 <b style='color: #ffcc00;'>지방자치단체의 소형가전 수거 규정</b>을 반드시 확인하시기 바랍니다.</span>" +
	                                                                                                            										                                "    </div>" +

	                                                                                                            										                                "  </div>" +
	                                                                                                            										                                "</div>", 
	                                                                                                            										                                "images/Clothing/hair_iron.png", ""},
	                                                                                                            										        {"I205", "공기청정기", "C08", 
	                                                                                                            										                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                                    
	                                                                                                            										                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 무상 방문수거 서비스 (권장)</b></p>" +
	                                                                                                            										                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                    "      · 공기청정기는 <span style='color: #ffcc00; font-weight: bold;'>단일 품목(1개)</span>으로도 무상 방문 수거 신청이 가능합니다.<br/>" +
	                                                                                                            										                                    "      · <span style='color: #ffcc00;'>폐가전 무상방문수거서비스(1599-0903)</span>를 이용하면 집 앞까지 직접 방문하여 수거해 드립니다." +
	                                                                                                            										                                    "    </div>" +

	                                                                                                            										                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 대형 폐기물 배출 및 재활용</b></p>" +
	                                                                                                            										                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                    "      · 방문 수거가 어려운 경우, 지자체에 신고 후 <span style='color: #ffcc00; font-weight: bold;'>대형 폐기물 스티커</span>를 부착하여 배출하세요.<br/>" +
	                                                                                                            										                                    "      · 상태가 양호하여 재사용이 가능하다면 인근 <span style='color: #ffcc00;'>재활용센터</span>에 문의하여 판매하거나 기증할 수 있습니다." +
	                                                                                                            										                                    "    </div>" +

	                                                                                                            										                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
	                                                                                                            										                                    "      <b style='color: #4ebfad;'>💡 Tip</b><br/>" +
	                                                                                                            										                                    "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                                    "        공기청정기는 환경성보장제도에 포함되어 유해물질 사용 억제 및 재활용 촉진 관리를 받습니다. 적정 절차에 따라 배출하는 것이 환경 보전에 매우 중요합니다." +
	                                                                                                            										                                    "      </span>" +
	                                                                                                            										                                    "    </div>" +

	                                                                                                            										                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                                    "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 내부 필터는 재활용이 불가하므로 <b style='color: #ffcc00;'>일반 종량제 봉투</b>에 따로 분리하여 배출하고, 본체만 가전제품으로 배출하세요.</span>" +
	                                                                                                            										                                    "    </div>" +

	                                                                                                            										                                    "  </div>" +
	                                                                                                            										                                    "</div>", 
	                                                                                                            										                                    "images/Clothing/air_purifier.png", ""},
	                                                                                                            										        {"I139", "가방", "C08", 
	                                                                                                            										                                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                                        
	                                                                                                            										                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 의류수거함 배출 (재사용 가능)</b></p>" +
	                                                                                                            										                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                        "      · <span style='color: #ffcc00; font-weight: bold;'>상태가 좋은 가방</span>(형태 변형 없음, 오염 적음)은 <span style='color: #ffcc00; font-weight: bold;'>의류수거함</span>에 배출합니다.<br/>" +
	                                                                                                            										                                        "      · 의류수거함에 배출된 가방은 선별 과정을 거쳐 국내외에서 재사용됩니다." +
	                                                                                                            										                                        "    </div>" +

	                                                                                                            										                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 일반 쓰레기 (종량제 봉투) 배출</b></p>" +
	                                                                                                            										                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                        "      · <span style='color: #ffcc00; font-weight: bold;'>해지거나 심하게 오염된 가방</span>, 형태 변형이 심해 재사용이 어려운 가방은 <span style='color: #ffcc00; font-weight: bold;'>종량제 봉투</span>에 넣어 일반 쓰레기로 버려야 합니다.<br/>" +
	                                                                                                            										                                        "      · 가죽, 천, 금속 등 복합재질로 이루어져 재활용이 어렵기 때문입니다." +
	                                                                                                            										                                        "    </div>" +

	                                                                                                            										                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
	                                                                                                            										                                        "      <b style='color: #4ebfad;'>💡 Tip</b><br/>" +
	                                                                                                            										                                        "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                                        "        의류수거함의 배출 가능 품목은 지자체마다 다를 수 있습니다. 배출 전 <b style='color: #ffcc00;'>해당 지역의 의류수거함 안내문</b>을 확인해 주세요." +
	                                                                                                            										                                        "      </span>" +
	                                                                                                            										                                        "    </div>" +

	                                                                                                            										                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                                        "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : <b style='color: #ffcc00;'>캐리어</b>는 크기와 재질에 따라 <b style='color: #ffcc00;'>대형 폐기물</b>로 신고 후 배출해야 합니다.</span>" +
	                                                                                                            										                                        "    </div>" +

	                                                                                                            										                                        "  </div>" +
	                                                                                                            										                                        "</div>", 
	                                                                                                            										                                        "images/Clothing/bag.png", ""},
	                                                                                                            										        {"I905", "내비게이션", "C08", 
	                                                                                                            										                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                                            
	                                                                                                            										                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 가전 전용수거함 배출</b></p>" +
	                                                                                                            										                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                            "      · 단품으로 배출 시 인근 주민센터나 아파트 단지에 비치된 <span style='color: #4ebfad; font-weight: bold;'>소형 폐가전 전용수거함</span>에 넣어주세요.<br/>" +
	                                                                                                            										                                            "      · 수거함이 없다면 지자체 조례에 따라 종량제 봉투 배출 혹은 별도 신고가 필요할 수 있습니다." +
	                                                                                                            										                                            "    </div>" +

	                                                                                                            										                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 무상 방문수거 서비스 (5개 이상)</b></p>" +
	                                                                                                            										                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                            "      · 내비게이션을 포함해 휴대폰, 카메라 등 소형 가전이 <span style='color: #ffcc00; font-weight: bold;'>5개 이상</span>일 경우 <b style='color: #ffffff;'>폐가전 무상방문수거(1599-0903)</b>를 이용할 수 있습니다." +
	                                                                                                            										                                            "    </div>" +

	                                                                                                            										                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #4ebfad; margin-bottom: 15px;'>" +
	                                                                                                            										                                            "      <b style='color: #4ebfad;'>💡 자원순환 : 환경성보장제도</b><br/>" +
	                                                                                                            										                                            "      <span style='font-size: 13px; color: #cccccc;'>" +
	                                                                                                            										                                            "        내비게이션은 유해물질 사용을 억제하고 재활용이 쉽도록 관리되는 <b style='color: #ffffff;'>환경성보장제도</b> 대상 품목입니다. 올바르게 배출된 가전은 금, 은, 구리 등 희귀 금속을 추출하는 귀한 자원이 됩니다." +
	                                                                                                            										                                            "      </span>" +
	                                                                                                            										                                            "    </div>" +

	                                                                                                            										                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                                            "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 배출 전 기기 내 저장된 개인정보(집 주소, 경로 기록 등)를 최대한 삭제하거나 초기화한 후 배출하는 것을 권장합니다.</span>" +
	                                                                                                            										                                            "    </div>" +

	                                                                                                            										                                            "  </div>" +
	                                                                                                            										                                            "</div>", 
	                                                                                                            										                                            "images/Clothing/navigation.png", ""},
	                                                                                                            										        {"I906", "냉장고", "C08", 
	                                                                                                            										                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                                                
	                                                                                                            										                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 무상 방문수거 서비스 (단일 배출 가능)</b></p>" +
	                                                                                                            										                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                                "      · 냉장고는 대형 가전으로 <span style='color: #ffcc00; font-weight: bold;'>단 1개만 있어도</span> 전담 수거반이 직접 방문하는 <b style='color: #ffcc00;'>무상방문수거(1599-0903)</b> 서비스를 이용할 수 있습니다.<br/>" +
	                                                                                                            										                                                "      · 신제품 구입 시에는 판매업체의 역회수 서비스를 통해 무상으로 배출이 가능합니다." +
	                                                                                                            										                                                "    </div>" +

	                                                                                                            										                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 지자체 대형 폐기물 배출</b></p>" +
	                                                                                                            										                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                                "      · 무상수거 서비스 이용이 어려운 경우, 관할 지자체(주민센터 등)에 <span style='color: #ffcc00; font-weight: bold;'>대형 폐기물 신고</span> 후 수수료 납부 및 스티커를 부착하여 배출해야 합니다." +
	                                                                                                            										                                                "    </div>" +

	                                                                                                            										                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 15px;'>" +
	                                                                                                            										                                                "      <b style='color: #00fff0;'>💡 Tip </b><br/>" +
	                                                                                                            										                                                "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                                                "        냉장고는 유해물질 사용 억제 및 재활용 촉진을 위한 <b style='color: #ffcc00;'>환경성보장제도</b>의 핵심 대상 품목입니다. 적정 절차를 통해 배출된 냉장고는 냉매 가스의 안전한 처리와 자원 재활용에 기여합니다." +
	                                                                                                            										                                                "      </span>" +
	                                                                                                            										                                                "    </div>" +

	                                                                                                            										                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                                                "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 음식물은 모두 비워주시고, 제품의 원형이 훼손되지 않은 상태에서 배출해야 무상 방문수거 서비스가 가능합니다.</span>" +
	                                                                                                            										                                                "    </div>" +

	                                                                                                            										                                                "  </div>" +
	                                                                                                            										                                                "</div>", 
	                                                                                                            										                                                "images/Clothing/refrigerator.png", ""},
	                                                                                                            										        {"I907", "노트북", "C08", 
	                                                                                                            										                                                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                                                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                                                    
	                                                                                                            										                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 폐가전 전용수거함 배출</b></p>" +
	                                                                                                            										                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                                    "      · 단품으로 배출 시 인근 주민센터나 아파트 단지에 비치된 <span style='color: #ffcc00; font-weight: bold;'>소형 전기전자제품 전용수거함</span>에 넣어주세요.<br/>" +
	                                                                                                            										                                                    "      · 인근에 수거함이 없는 경우 지자체 문의 후 배출이 필요합니다." +
	                                                                                                            										                                                    "    </div>" +

	                                                                                                            										                                                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 무상 방문수거 서비스 (5개 이상)</b></p>" +
	                                                                                                            										                                                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                                    "      · 노트북은 소형 가전으로 분류되어, 노트북을 포함한 소형 가전제품이 <span style='color: #ffcc00; font-weight: bold;'>5개 이상</span>일 경우 <b style='color: #ffcc00;'>폐가전 무상방문수거(1599-0903)</b> 서비스를 이용할 수 있습니다." +
	                                                                                                            										                                                    "    </div>" +

	                                                                                                            										                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 15px;'>" +
	                                                                                                            										                                                    "      <b style='color: #00fff0;'>💡 Tip</b><br/>" +
	                                                                                                            										                                                    "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                                                    "        노트북은 재활용을 촉진하고 유해물질 사용을 관리하는 <b style='color: #ffcc00;'>환경성보장제도</b> 대상 품목입니다. 올바른 배출을 통해 내부의 희귀 금속을 재자원화하여 환경 보전에 기여할 수 있습니다." +
	                                                                                                            										                                                    "      </span>" +
	                                                                                                            										                                                    "    </div>" +

	                                                                                                            										                                                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                                                    "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 배출 전 하드디스크나 메모리에 저장된 <b style='color: #ffcc00;'>중요 개인정보 및 문서를 반드시 삭제 또는 포맷</b>한 후 배출하시기 바랍니다.</span>" +
	                                                                                                            										                                                    "    </div>" +

	                                                                                                            										                                                    "  </div>" +
	                                                                                                            										                                                    "</div>", 
	                                                                                                            										                                                    "images/Clothing/laptop.png", ""},
	                                                                                                            										        {"I908", "녹즙기", "C08", 
	                                                                                                            										                                                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                                                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                                                        
	                                                                                                            										                                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 폐가전 전용수거함 배출</b></p>" +
	                                                                                                            										                                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                                        "      · 단품으로 배출 시 인근 주민센터나 아파트 단지에 비치된 <span style='color: #ffcc00; font-weight: bold;'>소형 전기전자제품 전용수거함</span>에 배출해 주세요.<br/>" +
	                                                                                                            										                                                        "      · 전용 수거함이 없는 경우 지자체별 배출 요령(유료 스티커 등)에 따라 배출해야 합니다." +
	                                                                                                            										                                                        "    </div>" +

	                                                                                                            										                                                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 무상 방문수거 서비스 (5개 이상)</b></p>" +
	                                                                                                            										                                                        "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                                        "      · 녹즙기를 포함하여 노트북, 청소기 등 소형 가전제품이 <span style='color: #ffcc00; font-weight: bold;'>5개 이상</span>일 경우 <b style='color: #ffcc00;'>폐가전 무상방문수거(1599-0903)</b> 서비스를 이용할 수 있습니다." +
	                                                                                                            										                                                        "    </div>" +

	                                                                                                            										                                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 15px;'>" +
	                                                                                                            										                                                        "      <b style='color: #00fff0;'>💡 Tip</b><br/>" +
	                                                                                                            										                                                        "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                                                        "        녹즙기는 자원순환체계 구축을 위한 <b style='color: #ffcc00;'>환경성보장제도</b> 대상 품목입니다. 유해물질을 억제하고 재활용이 용이하게 관리되므로, 올바른 배출이 자원 효율을 높이는 데 기여합니다." +
	                                                                                                            										                                                        "      </span>" +
	                                                                                                            										                                                        "    </div>" +

	                                                                                                            										                                                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                                                        "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 배출 전 기기 내부에 남은 <b style='color: #ffcc00;'>음식물 찌꺼기를 깨끗이 세척</b>하고 건조한 상태로 배출해 주시기 바랍니다.</span>" +
	                                                                                                            										                                                        "    </div>" +

	                                                                                                            										                                                        "  </div>" +
	                                                                                                            										                                                        "</div>", 
	                                                                                                            										                                                        "images/Clothing/juicer.png", ""},
	                                                                                                            										        {"I909", "디지털 체중계", "C08", 
	                                                                                                            										                                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                                                            
	                                                                                                            										                                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 폐가전 전용수거함 배출</b></p>" +
	                                                                                                            										                                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                                            "      · 인근 주민센터나 아파트 단지에 비치된 <span style='color: #ffcc00; font-weight: bold;'>소형 전기전자제품 전용수거함</span>에 배출할 수 있습니다.<br/>" +
	                                                                                                            										                                                            "      · 수거함 접근이 어려운 경우, 지자체 조례에 따라 종량제 봉투 배출 가능 여부를 확인하세요." +
	                                                                                                            										                                                            "    </div>" +

	                                                                                                            										                                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 대형 폐기물 신고 배출</b></p>" +
	                                                                                                            										                                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                                            "      · 전용 수거함이 없거나 크기가 큰 경우, 관할 지자체에 <span style='color: #ffcc00; font-weight: bold;'>대형 폐기물</span>로 신고하고 수수료 납부 후 스티커를 부착하여 배출해야 합니다." +
	                                                                                                            										                                                            "    </div>" +

	                                                                                                            										                                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 15px;'>" +
	                                                                                                            										                                                            "      <b style='color: #00fff0;'>💡 Tip</b><br/>" +
	                                                                                                            										                                                            "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                                                            "        디지털 체중계는 플라스틱, 금속, 유리가 혼합되어 재활용이 까다로운 품목입니다. 특히 <b style='color: #ffcc00;'>환경성보장제도 비대상 품목</b>이므로 무상 방문수거 서비스 대상에서 제외될 수 있음에 유의해야 합니다." +
	                                                                                                            										                                                            "      </span>" +
	                                                                                                            										                                                            "    </div>" +

	                                                                                                            										                                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                                                            "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 배출 시 반드시 내부의 <b style='color: #ffcc00;'>건전지(배터리)를 분리</b>하여 건전지 전용 수거함에 따로 배출해 주시기 바랍니다.</span>" +
	                                                                                                            										                                                            "    </div>" +

	                                                                                                            										                                                            "  </div>" +
	                                                                                                            										                                                            "</div>", 
	                                                                                                            										                                                            "images/Clothing/digital_scale.png", ""},
	                                                                                                            										        {"I911", "디지털 카메라", "C08", 
	                                                                                                            										                                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                                                                
	                                                                                                            										                                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 폐가전 전용수거함 배출</b></p>" +
	                                                                                                            										                                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                                                "      · 단품으로 배출 시 인근 주민센터나 아파트 단지에 비치된 <span style='color: #ffcc00; font-weight: bold;'>소형 전기전자제품 전용수거함</span>에 배출해 주세요.<br/>" +
	                                                                                                            										                                                                "      · 인근에 전용 수거함이 없는 경우에는 관할 지자체에 문의 후 배출하시기 바랍니다." +
	                                                                                                            										                                                                "    </div>" +

	                                                                                                            										                                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 무상 방문수거 서비스 (5개 이상)</b></p>" +
	                                                                                                            										                                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                                                                "      · 디지털 카메라는 소형 가전으로 분류되어, 카메라를 포함한 소형 가전제품이 <span style='color: #ffcc00; font-weight: bold;'>5개 이상</span>일 경우 <b style='color: #ffcc00;'>폐가전 무상방문수거(1599-0903)</b> 서비스를 이용할 수 있습니다." +
	                                                                                                            										                                                                "    </div>" +

	                                                                                                            										                                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 15px;'>" +
	                                                                                                            										                                                                "      <b style='color: #00fff0;'>💡 Tip</b><br/>" +
	                                                                                                            										                                                                "      <span style='font-size: 13px; color: #ffffff;'>" +
	                                                                                                            										                                                                "        디지털 카메라는 자원 효율을 높이기 위한 <b style='color: #ffcc00;'>환경성보장제도</b> 대상 품목입니다. 유해물질 사용 억제 및 재활용 촉진을 통해 환경 보전에 기여하며, E-순환거버넌스를 통해 적정하게 재활용됩니다." +
	                                                                                                            										                                                                "      </span>" +
	                                                                                                            										                                                                "    </div>" +

	                                                                                                            										                                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                                                                "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 배출 전 기기에 장착된 <b style='color: #ffcc00;'>메모리카드 및 배터리를 반드시 분리</b>하시고, 저장된 사진 등 개인정보를 삭제한 후 배출하는 것을 권장합니다.</span>" +
	                                                                                                            										                                                                "    </div>" +

	                                                                                                            										                                                                "  </div>" +
	                                                                                                            										                                                                "</div>", 
	                                                                                                            										                                                                "images/Clothing/digital_camera.png", ""},

	                                                                                                            										            {"I109", "의류", "C08", 
	                                                                                                            										                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                
	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 헌옷 수거함 배출 대상</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 티셔츠, 바지, 코트 등 <span style='color: #ffcc00; font-weight: bold;'>재사용 가능한 일반 의류</span><br/>" +
	                                                                                                            										                "      · 단추나 지퍼는 <span style='color: #ffcc00;'>제거하지 않고 그대로</span> 배출해도 됩니다.<br/>" +
	                                                                                                            										                "      · 여러 벌일 경우 투명 비닐봉투에 담아 배출하면 효율적입니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
	                                                                                                            										                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
	                                                                                                            										                "      · <span style='color: #ffcc00; font-weight: bold;'>속옷, 양말, 걸레, 행주, 솜이불, 베개</span><br/>" +
	                                                                                                            										                "      · 위 품목들은 위생 및 부피 문제로 반드시 <span style='color: #ffcc00;'>일반 쓰레기</span>로 버려야 합니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                "      <span><b style='color: #00fff0;'>💡 Tip</b> : 솜이불은 지자체에 따라 <b style='color: #ffcc00;'>대형 폐기물 스티커</b> 부착이 필요할 수 있으니 확인하세요.</span>" +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "  </div>" +
	                                                                                                            										                "</div>", 
	                                                                                                            										                "images/Clothing/clothes.png", ""},

	                                                                                                            										            {"I110", "식용유", "C08", 
	                                                                                                            										                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                
	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ff6b6b; font-size: 17px;'>⚠️ 절대 하수구에 버리지 마세요!</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 하수구에 버린 기름은 굳어서 <span style='color: #ffcc00; font-weight: bold;'>배관을 막고</span> 수질 오염을 일으킵니다.<br/>" +
	                                                                                                            										                "      · 유통기한 경과 기름이나 사용 후 폐유 모두 동일하게 적용됩니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 전용 수거함 이용</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 아파트나 주민센터의 <span style='color: #ffcc00; font-weight: bold;'>폐식용유 수거함</span>에 모아서 배출하세요.<br/>" +
	                                                                                                            										                "      · 음식물 찌꺼기 등 불순물이 섞이지 않도록 주의해야 합니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                "      <span><b style='color: #00fff0;'>💡 Tip</b> : 소량일 경우 <b style='color: #ffcc00;'>키친타월이나 우유팩의 신문지</b>에 흡수시켜 종량제 봉투에 버리세요.</span>" +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "  </div>" +
	                                                                                                            										                "</div>", 
	                                                                                                            										                "images/Clothing/cooking_oil.png", ""},

	                                                                                                            										            {"I111", "기계/엔진오일", "C08", 
	                                                                                                            										                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                
	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ff6b6b; font-size: 17px;'>⚠️ 유독성 폐기물 주의</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 기계유나 엔진오일은 환경을 심각하게 오염시키는 <span style='color: #ffcc00; font-weight: bold;'>지정폐기물</span>입니다.<br/>" +
	                                                                                                            										                "      · 식용유 수거함이나 하수구에 절대 버려서는 안 됩니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 전문 업체 위탁</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 자동차 엔진오일은 <span style='color: #ffcc00; font-weight: bold;'>정비소(카센터)</span> 방문 시 교체 및 수거 서비스를 이용하는 것이 가장 안전합니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 대량 발생 시 <b style='color: #ffffff;'>지정폐기물 처리 업체</b>를 통해 적법하게 처리해야 하며, 액체 상태 그대로 종량제에 버리면 안 됩니다.</span>" +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "  </div>" +
	                                                                                                            										                "</div>", 
	                                                                                                            										                "images/Clothing/engine_oil.png", ""},

	                                                                                                            										            {"I112", "자동차 부품/타이어", "C08", 
	                                                                                                            										                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                
	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 판매점 반납 원칙</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 타이어 교체 시 <span style='color: #ffcc00; font-weight: bold;'>기존 타이어를 판매점에 반납</span>하는 것이 가장 올바른 방법입니다.<br/>" +
	                                                                                                            										                "      · 개별 배출 시에는 지자체 신고 후 <span style='color: #ffcc00;'>대형 폐기물 스티커</span>를 부착해야 합니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
	                                                                                                            										                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
	                                                                                                            										                "      · <span style='color: #ffcc00; font-weight: bold;'>배터리, 범퍼, 엔진 부품</span>은 일반 쓰레기가 아닙니다.<br/>" +
	                                                                                                            										                "      · 위험 물질이 포함되므로 반드시 <span style='color: #ffcc00;'>카센터나 전문 폐기물 업체</span>에 위탁하세요." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                "      <span><b style='color: #00fff0;'>💡 Tip</b> : 타이어는 전문 선별 과정을 거쳐 <b style='color: #ffcc00;'>에너지 자원이나 보도블록</b> 등으로 재탄생합니다.</span>" +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "  </div>" +
	                                                                                                            										                "</div>", 
	                                                                                                            										                "images/Clothing/tire.png", ""},

	                                                                                                            										            {"I113", "빗/헤어브러시", "C08", 
	                                                                                                            										                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                
	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 방법</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 빗은 여러 재질이 섞여 있어 재활용이 어렵습니다. <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기(종량제 봉투)</span>로 배출하세요.<br/>" +
	                                                                                                            										                "      · 배출 전 빗살에 낀 <span style='color: #ffcc00;'>머리카락을 완전히 제거</span>하는 것이 좋습니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재활용이 안 되는 이유</b></p>" +
	                                                                                                            										                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                "      · 손잡이가 플라스틱이어도 빗살 부위가 <span style='color: #ffcc00;'>나무, 고무, 금속, 돈모</span> 등 복합 재질인 경우가 많습니다.<br/>" +
	                                                                                                            										                "      · 이처럼 분리가 어려운 <span style='color: #ffcc00; font-weight: bold;'>혼합 재질</span>은 모두 폐기 대상입니다." +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                "      <span><b style='color: #00fff0;'>💡 Tip</b> : 100% 한 가지 재질로만 된 단순한 플라스틱 빗이라면 분리배출이 가능하지만, 가급적 <b style='color: #ffcc00;'>종량제 봉투</b>를 권장합니다.</span>" +
	                                                                                                            										                "    </div>" +

	                                                                                                            										                "  </div>" +
	                                                                                                            										                "</div>", 
	                                                                                                            										                "images/Clothing/hairbrush.png", ""},
	                                                                                                            										            {"I114", "애완동물집/케이스", "C08", 
	                                                                                                            										                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                    
	                                                                                                            										                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 대형 폐기물 신고 (권장)</b></p>" +
	                                                                                                            										                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                    "      · 부피가 큰 플라스틱 이동장이나 대형 집은 <span style='color: #ffcc00; font-weight: bold;'>대형 폐기물 스티커</span>를 부착하여 배출하는 것이 가장 확실합니다.<br/>" +
	                                                                                                            										                    "      · 지자체 홈페이지나 주민센터에서 신고 후 지정된 장소에 내놓으세요." +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재질별 분리 배출</b></p>" +
	                                                                                                            										                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                    "      · <b style='color: #ffcc00;'>금속 문 :</b> 몸체와 분리 가능하다면 <span style='color: #ffcc00;'>고철류</span>로 배출하세요.<br/>" +
	                                                                                                            										                    "      · <b style='color: #ffcc00;'>플라스틱 몸체 :</b> 다른 재질을 모두 제거했다면 <span style='color: #ffcc00;'>플라스틱</span>으로 배출 가능합니다." +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                    "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 내부에 포함된 <b style='color: #ffffff;'>천이나 쿠션</b>은 재활용이 불가능하므로 반드시 <b style='color: #ffcc00;'>종량제 봉투</b>에 담아 따로 버려야 합니다.</span>" +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "  </div>" +
	                                                                                                            										                    "</div>", 
	                                                                                                            										                    "images/Clothing/pet_carrier.png", ""},

	                                                                                                            										                {"I115", "야구배트", "C08", 
	                                                                                                            										                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                    
	                                                                                                            										                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 알루미늄 배트 (재활용 가능)</b></p>" +
	                                                                                                            										                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                    "      · 전체가 금속으로 된 알루미늄 배트는 <span style='color: #ffcc00; font-weight: bold;'>고철류</span>로 분리 배출하세요.<br/>" +
	                                                                                                            										                    "      · 손잡이의 고무 그립은 최대한 제거하는 것이 좋지만, 어렵다면 그대로 고철로 배출 가능합니다." +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 나무 및 카본 배트 (재활용 불가)</b></p>" +
	                                                                                                            										                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                    "      · <b style='color: #ffcc00;'>나무(우드) 배트 :</b> 재활용이 되지 않으므로 <span style='color: #ffcc00;'>종량제 봉투</span>에 담아 배출하세요.<br/>" +
	                                                                                                            										                    "      · <b style='color: #ffcc00;'>카본/복합 소재 :</b> 특수 소재 역시 일반 쓰레기로 분류됩니다." +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                    "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 봉투에 들어가지 않는 크기라면 <b style='color: #ffffff;'>대형 폐기물</b>로 신고해야 하며, 부러진 배트는 날카로운 부분을 감싸서 배출하세요.</span>" +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "  </div>" +
	                                                                                                            										                    "</div>", 
	                                                                                                            										                    "images/Clothing/baseball_bat.png", ""},

	                                                                                                            										                {"I116", "와이퍼", "C08", 
	                                                                                                            										                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                    
	                                                                                                            										                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 방법</b></p>" +
	                                                                                                            										                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                    "      · 와이퍼는 금속과 고무가 강력하게 결합된 <span style='color: #ffcc00; font-weight: bold;'>복합 재질 폐기물</span>입니다.<br/>" +
	                                                                                                            										                    "      · 분리가 매우 어렵기 때문에 반드시 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기(종량제 봉투)</span>로 배출해야 합니다." +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ffcc00; margin-bottom: 20px;'>" +
	                                                                                                            										                    "      · <span style='color: #ffcc00; font-weight: bold;'>💡 왜 고철이 아닌가요?</span><br/>" +
	                                                                                                            										                    "      · 금속 프레임에 고무 날이 압착된 상태로는 고철 선별 공정에서 재활용 가치가 없어 탈락됩니다." +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                    "      <span><b style='color: #00fff0;'>💡 Tip</b> : 만약 완벽하게 분리했다면 <b style='color: #ffffff;'>금속은 고철</b>로, <b style='color: #ffffff;'>고무는 일반 쓰레기</b>로 배출이 가능합니다.</span>" +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "  </div>" +
	                                                                                                            										                    "</div>", 
	                                                                                                            										                    "images/Clothing/wiper.png", ""},

	                                                                                                            										                {"I117", "줄자", "C08", 
	                                                                                                            										                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
	                                                                                                            										                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
	                                                                                                            										                    
	                                                                                                            										                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 방법</b></p>" +
	                                                                                                            										                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
	                                                                                                            										                    "      · 줄자는 플라스틱(외관)과 금속(내부 태엽)이 섞여 있어 재활용이 불가능합니다.<br/>" +
	                                                                                                            										                    "      · 반드시 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기(종량제 봉투)</span>에 넣어 배출하세요." +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
	                                                                                                            										                    "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
	                                                                                                            										                    "      · 내부에 날카로운 금속 스프링이 있어 분해 시 다칠 위험이 큽니다. 분해하지 말고 통째로 배출하세요." +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
	                                                                                                            										                    "      <span><b style='color: #00fff0;'>💡 Tip</b> : 금속 날이 밖으로 나와 있다면 <b style='color: #ffcc00;'>안전하게 고정</b>하여 미화원분이 다치지 않게 배출해 주세요.</span>" +
	                                                                                                            										                    "    </div>" +

	                                                                                                            										                    "  </div>" +
	                                                                                                            										                    "</div>", 
	                                                                                                            										                    "images/Clothing/tape_measure.png", ""},
    	       
    	    };
    
    
    public static String getFoodWasteGuideHtml() {

        String basePath = "C:/Users/rnrnd/eclipse-workspace/recycle/src/Main/webapp/";
        
        String imgPathUrl = "file:/" + basePath + "images/food waste/food waste";

        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".section-title-red { color: #ff5555; font-size: 20px; font-weight: bold; border-bottom: 2px solid #ff5555; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" +
                ".highlight-place { color: #00fff0; font-weight: bold; }" +
                ".step-title { font-weight: bold; color: #ffffff; margin-bottom: 2px; }" +
                ".step-desc { margin-bottom: 15px; color: #e0e0e0; }" +
                ".img-group { margin-top: 25px; text-align: center; }" + 
                
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; margin: 10px; width: 100px; height: 100px; object-fit: cover; }" +
                
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" + 
                ".sub-text-cyan { font-size: 13px; color: #00fff0; margin-top: 8px; }" +
                "</style></head><body>" +
                
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🍎 음식물류 폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "가공 후 <span class='highlight-point'>동물의 사료나 퇴비</span>로 재활용이 가능한 유기물 폐기물입니다.<br><br>" +
                "• <b>곡류/채소:</b> 쌀밥, 면류, 과일 껍질(바나나, 사과 등), 배추, 무 등<br>" +
                "• <b>조리 음식:</b> 남겨진 반찬류, 국건더기(국물 제외), 상한 음식물" +
                "<div class='img-group'>" +
               
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "4.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "5.png'>" +
                "</div></div>" +

                "<div class='section-title-red'>⚠️ 음식물류가 아닌 품목 (일반 배출)</div>" +
                "<div class='content-box'>" +
                "동물이 먹을 수 없는 것들은 반드시 <span class='highlight-point'>일반 종량제 봉투</span>에 버려야 합니다.<br><br>" +
                "• <b>단단한 껍데기/뼈:</b> 조개·게·소라 껍데기, 소·돼지·닭의 뼈다귀<br>" +
                "• <b>딱딱한 씨앗/뿌리:</b> 복숭아·살구·감의 씨앗, 쪽파·대파의 뿌리, 양파 껍질<br>" +
                "• <b>기타:</b> 계란 껍질, 티백, 한약재 찌꺼기 등" +
                "<div class='img-group'>" +
                "<img class='guide-img' src='" + imgPathUrl + "6.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "7.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "8.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "9.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "10.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
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
                "<img class='guide-img' src='" + imgPathUrl + "12.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "13.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
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
                "</div>" +
                "</body></html>";
    }
    
    public static String getGeneralWasteGuideHtml() {
      
        String basePath = "C:/Users/rnrnd/eclipse-workspace/recycle/src/Main/webapp/";
        
        String imgPathUrl = "file:/" + basePath + "images/general standard waste/general standard waste";

        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" +
                ".highlight-place { color: #00fff0; font-weight: bold; }" +
                ".step-title { font-weight: bold; color: #ffffff; margin-bottom: 2px; }" +
                ".step-desc { margin-bottom: 15px; color: #e0e0e0; }" +
                
                ".img-group { margin-top: 25px; text-align: center; }" +
                
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; margin: 10px; width: 100px; height: 100px; object-fit: cover; }" +
                
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" + 
                ".sub-text-cyan { font-size: 13px; color: #00fff0; margin-top: 8px; }" +
                "</style></head><body>" +
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🗑️ 일반종량제 폐기물</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "재활용이 불가능하며 불에 잘 타는 <span class='highlight-point'>가연성 폐기물</span>이 해당됩니다.<br><br>" +
                "• <b>오염된 종이류:</b> 사용한 휴지, 기저귀, 음식물이 묻은 종이컵 등<br>" +
                "• <b>복합재질/기타:</b> 볼펜, 칫솔, 노끈, 고무장갑 등<br>" +
                "• <b>나무/가죽:</b> 나무젓가락, 가죽 지갑, 신발류 등<br>" +
                "• <b>비닐류:</b> 보온보냉 팩(은박 코팅), 소량의 비닐 조각" +
                "<div class='img-group'>" +
            
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "4.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "5.png'>" +
                "</div>" +
                "</div>" +

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
                "</div>" +
                "</body></html>";
    }
    
    public static String getNonFlammableWasteGuideHtml() {
   
        String basePath = "C:/Users/rnrnd/eclipse-workspace/recycle/src/Main/webapp/";
        
        String imgPathUrl = "file:/" + basePath + "images/incombustible waste/incombustible waste";

        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" +
                ".highlight-place { color: #00fff0; font-weight: bold; }" +
                ".step-title { font-weight: bold;  color: #ffffff; margin-bottom: 2px; }" +
                ".step-desc { margin-bottom: 15px;  color: #e0e0e0; }" +
                
                ".img-group { margin-top: 25px; text-align: center; }" +
                
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; margin: 10px; width: 100px; height: 100px; object-fit: cover; }" +
                
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" + 
                ".sub-text-cyan { font-size: 13px; color: #00fff0; margin-top: 8px; }" +
                "</style></head><body>" +
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🧱 불연성종량제 폐기물</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "재활용이 불가능하고 불길에 타지 않는 <span class='highlight-point'>불연성 자재</span>들이 해당됩니다.<br><br>" +
                "• <b>자기류/유리:</b> 사기그릇, 화분, 깨진 유리, 거울, 내열식기(뚝배기 등)<br>" +
                "• <b>소량 건설폐기물:</b> 집수리 시 발생하는 벽돌, 타일, 시멘트 블록 파편<br>" +
                "• <b>기타:</b> 조개껍데기, 연탄재, 장식용 수석, 고양이 배변 모래 등" +
                "<div class='img-group'>" +
            
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "4.png'>" +
                "</div>" +
                "</div>" +

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
                "</div>" +
                "</body></html>";
    }
    
    public static String getBulkyWasteGuideHtml() {
     
        String basePath = "C:/Users/rnrnd/eclipse-workspace/recycle/src/Main/webapp/";
        
        String imgPathUrl = "file:/" + basePath + "images/large-scale waste/large-scale waste";

        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" +
                ".highlight-place { color: #00fff0; font-weight: bold; }" +
                ".step-title { font-weight: bold;  color: #ffffff; margin-bottom: 2px; }" +
                ".step-desc { margin-bottom: 15px;  color: #e0e0e0; }" +
                
                ".img-group { margin-top: 25px; text-align: center; }" +
                
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; margin: 10px; width: 110px; height: 110px; object-fit: cover; }" +
                
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" + 
                ".sub-text-cyan { font-size: 13px; color: #00fff0; margin-top: 8px; }" +
                "</style></head><body>" +
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🛋️ 대형폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목</div>" +
                "<div class='content-box'>" +
                "종량제 봉투에 담기 어렵거나 생활 가구/가전 등 <span class='highlight-point'>개별 수거</span>가 필요한 물품입니다.<br><br>" +
                "• <b>가구류:</b> 장롱, 침대, 책상, 소파, 식탁 등<br>" +
                "• <b>가전류:</b> 냉장고, 세탁기, 에어컨, TV, 전자레인지 등<br>" +
                "• <b>기타:</b> 자전거, 유모차, 피아노, 거울, 전기장판 등" +
                "<div class='img-group'>" +
              
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "4.png'>" +
                "</div>" +
                "</div>" +

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
                "</div>" +
                "</body></html>";
    }
    
    public static String getConstructionWasteGuideHtml() {

        String basePath = "C:/Users/rnrnd/eclipse-workspace/recycle/src/Main/webapp/";
        
        String imgPathUrl = "file:/" + basePath + "images/construction site household waste/construction site household waste";

        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" +
                ".highlight-place { color: #00fff0; font-weight: bold; }" +
                ".step-title { font-weight: bold; color: #ffffff; margin-bottom: 2px; }" +
                ".step-desc { margin-bottom: 15px; color: #e0e0e0; }" +
                
                ".img-group { margin-top: 25px; text-align: center; }" +
                
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; margin: 10px; width: 110px; height: 110px; object-fit: cover; }" +
                
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" + 
                ".sub-text-cyan { font-size: 13px; color: #00fff0; margin-top: 8px; }" +
                "</style></head><body>" +
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🏗️ 공사장 생활폐기물 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목 및 기준</div>" +
                "<div class='content-box'>" +
                "인테리어 공사 등으로 발생하는 <span class='highlight-point'>5톤 미만</span>의 폐기물을 의미합니다.<br><br>" +
                "• <b>불연성 건설폐재류:</b> 폐벽돌, 폐타일, 폐콘크리트, 전선관, 흙 등<br>" +
                "• <b>가연성 폐기물:</b> 폐목재, 폐벽지, 장판, 폐합성수지(스티로폼 등)<br>" +
                "• <b>시설물/기타:</b> 싱크대, 세면대, 변기, 욕조, 문틀, 고철류" +
                "<div class='img-group'>" +
               
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "3.png'>" +
                "</div>" +
                "</div>" +

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
                "</div>" +
                "</body></html>";
    }
  
    public static String getHazardousWasteGuideHtml() {
        
        String basePath = "C:/Users/rnrnd/eclipse-workspace/recycle/src/Main/webapp/";
        
        String imgPathUrl = "file:/" + basePath + "images/household hazardous waste/household hazardous waste";

        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight-point { color: #ff9d00; font-weight: bold; }" + 
                ".highlight-place { color: #00fff0; font-weight: bold; }" + 
                ".step-title { font-weight: bold; color: #ffffff; margin-bottom: 2px; }" + 
                ".step-desc { margin-bottom: 15px; color: #e0e0e0; }" + 
                
                ".img-group { margin-top: 25px; text-align: center; }" +
                
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; margin: 10px; width: 110px; height: 110px; object-fit: cover; }" +
                
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" + 
                ".sub-text-cyan { font-size: 13px; color: #00fff0; margin-top: 8px; }" +
                "</style></head><body>" +
                "<div style='font-size: 26px; font-weight: bold; color: #00fff0;'>🧪 생활계 유해폐기물 상세 가이드</div>" +
                
                "<div class='section-title'>📍 대상품목 및 유해성</div>" +
                "<div class='content-box'>" +
                "인체에 치명적이거나 생태계 파괴 위험이 있어 <span class='highlight-point'>특별 관리</span>가 필요한 폐기물입니다.<br><br>" +
                "• <b>폐의약품:</b> 유통기한 경과 또는 미복용 알약, 가루약, 물약, 연고 등<br>" +
                "• <b>수은 제품:</b> 수은 체온계, 혈압계, 수은 온도계 등<br>" +
                "• <b>생활 화학제품:</b> 폐농약, 폐페인트, 살충제, 소독제, 강력 접착제 등<br>" +
                "• <b>기타 유해물:</b> 라돈 침대 등 천연방사성 생활폐기물" +
                "<div class='img-group'>" +
                
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<img class='guide-img' src='" + imgPathUrl + "2.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
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
                "</div>" +
                "</body></html>";    
    }
    
    public static String getOtherWasteGuideHtml() {
 
        String basePath = "C:/Users/rnrnd/eclipse-workspace/recycle/src/Main/webapp/";
        
        String imgPathUrl = "file:/" + basePath + "images/Other Waste/Other Waste";

        return "<html><head><style>" +
                "body { background-color: #191932; color: #ffffff; font-family: '맑은 고딕'; padding: 20px; line-height: 1.6; }" +
                ".section-title { color: #00fff0; font-size: 20px; font-weight: bold; border-bottom: 2px solid #825aff; padding-bottom: 5px; margin: 25px 0 15px 0; }" +
                ".content-box { background-color: #25254b; padding: 15px; border-radius: 10px; border: 1px solid #3d3d70; margin-bottom: 15px; }" +
                ".highlight-cyan { color: #00fff0; font-weight: bold; }" +
                ".highlight-orange { color: #ff9d00; font-weight: bold; }" +
                ".highlight-red { color: #ff5555; font-weight: bold; }" +
                
                ".img-group { margin-top: 25px; text-align: center; }" +
                
                ".guide-img { border: 2px solid #3d3d70; border-radius: 8px; margin: 10px; width: 110px; height: 110px; object-fit: cover; }" +
                
                ".sub-text-red { font-size: 13px; color: #ff5555; margin-top: 8px; font-weight: bold; }" + 
                ".sub-text-cyan { font-size: 13px; color: #00fff0; margin-top: 8px; }" +
                "</style></head><body>" +
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
           
                "<img class='guide-img' src='" + imgPathUrl + "1.png'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
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
                "</div>" +
                "</body></html>";
    }
    public static class ItemDetail {
        public String itemId, itemName, categoryName, disposalGuide;
        public String itemImagePath, markImagePath; 

        public ItemDetail(String id, String name, String cat, String guide, String itemImg, String markImg) {
            this.itemId = id;
            this.itemName = name;
            this.categoryName = cat;
            this.disposalGuide = guide;
            this.itemImagePath = itemImg; 
            this.markImagePath = markImg; 
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
                    "CREATE TABLE IF NOT EXISTS " + CATEGORIES_TABLE + " (" +
                            "CATEGORY_ID VARCHAR(10) PRIMARY KEY," +
                            "CATEGORY_NAME VARCHAR(50) NOT NULL UNIQUE," +
                            "REWARD_POINTS INT NOT NULL DEFAULT 0)";

            String createItemsSQL = 
                    "CREATE TABLE IF NOT EXISTS " + ITEMS_TABLE + " (" +
                            "ITEM_ID VARCHAR(10) PRIMARY KEY," + 
                            "ITEM_NAME VARCHAR(100) NOT NULL UNIQUE," +
                            "CATEGORY_ID VARCHAR(10) NOT NULL," + 
                            "DISPOSAL_GUIDE LONGTEXT NOT NULL," +
                            "ITEM_IMAGE_PATH VARCHAR(255)," +  
                            "MARK_IMAGE_PATH VARCHAR(255)," +  
                            "FOREIGN KEY (CATEGORY_ID) REFERENCES " + CATEGORIES_TABLE + "(CATEGORY_ID))";
            
            stmt.execute(createCategoriesSQL);
            stmt.execute(createItemsSQL);
            
            insertInitialData(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertInitialData(Connection conn) throws SQLException {
     
        String catSql = "INSERT INTO " + CATEGORIES_TABLE + " (CATEGORY_ID, CATEGORY_NAME, REWARD_POINTS) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE REWARD_POINTS = VALUES(REWARD_POINTS)";
        try (PreparedStatement pstmt = conn.prepareStatement(catSql)) {
            Object[][] cats = {{"C01", "종이류", 10}, {"C02", "비닐류", 10}, {"C03", "유리병", 20}, {"C04", "종이팩", 15}, {"C05", "캔ㆍ고철", 20}, {"C06", "스티로폼", 10}, {"C07", "플라스틱", 15}, {"C08", "기타", 5}};
            for (Object[] cat : cats) {
                pstmt.setString(1, (String)cat[0]); pstmt.setString(2, (String)cat[1]); pstmt.setInt(3, (Integer)cat[2]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }

        String itemSql = "INSERT INTO " + ITEMS_TABLE + " (ITEM_ID, ITEM_NAME, CATEGORY_ID, DISPOSAL_GUIDE, ITEM_IMAGE_PATH, MARK_IMAGE_PATH) " +
                         "VALUES (?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE DISPOSAL_GUIDE = VALUES(DISPOSAL_GUIDE), ITEM_IMAGE_PATH = VALUES(ITEM_IMAGE_PATH), MARK_IMAGE_PATH = VALUES(MARK_IMAGE_PATH)";
                         
        try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
            for (String[] item : ITEMS_FULL_DATA) {
                
                if (item.length < 6) {
                    System.out.println("데이터 형식 오류(항목 부족): " + item[1]);
                    continue; 
                }

                String catId = item[2];
                String disposalGuideHtml = item[3]; 
                String itemImagePath = item[4];    
                String markImagePath = item[5];    

                pstmt.setString(1, item[0]);
                pstmt.setString(2, item[1]); 
                pstmt.setString(3, catId);  
                pstmt.setString(4, disposalGuideHtml); 
                
                pstmt.setString(5, itemImagePath); 
                pstmt.setString(6, markImagePath);

                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("DB 데이터 저장 및 업데이트 완료!");
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
        String sql = "SELECT i.ITEM_ID, i.ITEM_NAME, c.CATEGORY_NAME, i.DISPOSAL_GUIDE, i.ITEM_IMAGE_PATH, i.MARK_IMAGE_PATH " +
                     "FROM " + ITEMS_TABLE + " i JOIN " + CATEGORIES_TABLE + " c ON i.CATEGORY_ID = c.CATEGORY_ID";
        try (Connection conn = RecycleDB.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ItemDetail(
                    rs.getString("ITEM_ID"), rs.getString("ITEM_NAME"), 
                    rs.getString("CATEGORY_NAME"), rs.getString("DISPOSAL_GUIDE"),
                    rs.getString("ITEM_IMAGE_PATH"), rs.getString("MARK_IMAGE_PATH")
                ));
            }
        }
        return list;
    }


    public static ItemDetail getItemDetail(String itemName, String categoryName) throws SQLException {
   
        String sql = "SELECT i.ITEM_ID, i.ITEM_NAME, c.CATEGORY_NAME, i.DISPOSAL_GUIDE, " +
                     "i.ITEM_IMAGE_PATH, i.MARK_IMAGE_PATH FROM ITEMS i " +
                     "JOIN CATEGORIES c ON i.CATEGORY_ID = c.CATEGORY_ID " +
                     "WHERE i.ITEM_NAME = ? AND c.CATEGORY_NAME = ?";
        
        try (Connection conn = RecycleDB.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemName);
            pstmt.setString(2, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ItemDetail(
                        rs.getString("ITEM_ID"), rs.getString("ITEM_NAME"), 
                        rs.getString("CATEGORY_NAME"), rs.getString("DISPOSAL_GUIDE"),
                        rs.getString("ITEM_IMAGE_PATH"), rs.getString("MARK_IMAGE_PATH")
                    );
                }
            }
        }
        return null;
    }
    
    public static String[] getCategoryResources(String itemId) {
        return CATEGORY_RESOURCES.get(itemId);
    }

}