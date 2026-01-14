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
import db.DTO.GuideDTO;

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
//    		{"I405", "계란판", "C01", 
//            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
//            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
//            
//            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 종이(펄프) 계란판 배출</b></p>" +
//            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
//            "      · 종이 재질의 계란판은 <span style='color: #ffcc00; font-weight: bold;'>종이류 수거함</span>에 배출합니다.<br/>" +
//            "      · 계란 껍데기 등 이물질이 묻지 않도록 주의하고, 깨끗한 상태로 배출해 주세요." +
//            "    </div>" +
//
//            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 플라스틱 계란판 배출</b></p>" +
//            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
//            "      · 투명한 플라스틱 재질은 <span style='color: #ffcc00; font-weight: bold;'>플라스틱(PET) 수거함</span>에 배출합니다.<br/>" +
//            "      · 가급적 부착된 상표 스티커를 제거하여 분리배출 하시기 바랍니다." +
//            "    </div>" +
//
    		
//    		"    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; color: #ffffff;'>" +
//		    "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> :  종이 계란판은 이미 한 번 재활용된 펄프인 경우가 많아 다시 <b style='color: #ffcc00;'>신문지나 종이 완충재</b> 등으로 재탄생하며, 플라스틱 계란판은 고품질 재생 원료가 될 수 있습니다." +
//		    "    </div>" +

//      
//
//            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; display: flex; align-items: center; gap: 15px;'>" +
//            "      <span><b style='color: #ff6b6b;'>⚠️ 주의</b> : 계란 껍데기는 재활용이 되지 않는 <b style='color: #ffcc00;'>일반 쓰레기(종량제)</b>입니다. 계란판에 섞이지 않도록 따로 분리해 주세요.</span>" +
//            "    </div>" +
//
//            "  </div>" +
//            "</div>", 
//            "images/Paper/egg_tray.png", "images/paper/mark.png"},
    
//    		
    	     // [비닐류 - C02] (I023 ~ I032) 보강 버전

//   
//
//
//    		{"I118", "과일봉지", "C02", 
//    		        		                                                                                	                    "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
//    		        		                                                                                	                    "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
//    		        		                                                                                	                    
//    		        		                                                                                	                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 올바른 배출 방법</b></p>" +
//    		        		                                                                                	                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
//    		        		                                                                                	                    "      · 내용물을 비우고 이물질을 깨끗이 제거한 후 <span style='color: #ffcc00; font-weight: bold;'>비닐류 수거함</span>으로 배출합니다.<br/>" +
//    		        		                                                                                	                    "      · 흩날리지 않도록 차곡차곡 모아서 배출해 주세요." +
//    		        		                                                                                	                    "    </div>" +
//
//    		        		                                                                                	                    "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 재활용 특징</b></p>" +
//    		        		                                                                                	                    "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
//    		        		                                                                                	                    "      · 과일, 과자 포장용 비닐은 <span style='color: #ffcc00;'>생산자책임재활용제도(EPR)</span> 대상 품목으로 재활용 가치가 높습니다.<br/>" +
//    		        		                                                                                	                    "      · 수거된 비닐은 용융 및 성형 과정을 거쳐 <span style='color: #ffcc00;'>건축 자재나 플라스틱 배수로</span> 등으로 재탄생합니다." +
//    		        		                                                                                	                    "    </div>" +
//
//    		        		                                                                                	                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
//    		        		                                                                                	                    "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
//    		        		                                                                                	                    "      · 테이프나 택배 송장 등 <span style='color: #ffcc00; font-weight: bold;'>다른 재질</span>이 붙어있다면 반드시 제거하고 배출해야 합니다." +
//    		        		                                                                                	                    "    </div>" +
//
//    		        		                                                                                	                    "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
//    		        		                                                                                	                    
//    		        		                                                                                	                    "      <span><b style='color: #00fff0;'>💡 Tip</b> : 라면 봉지, 과자 봉지, 지퍼백, 위생팩, 세탁소 비닐 등도 모두 같은 방법으로 배출하세요!</span>" +
//    		        		                                                                                	                    "    </div>" +
//
//    		        		                                                                                	                    "  </div>" +
//    		        		                                                                                	                    "</div>", 
//    		        		                                                                                	                    "images/Vinyl/fruit_bag.png", "images/Vinyl/mark6.png"},
    	     // [유리병 - C03] (I033 ~ I042) 보강 버전
    	

    		

    		        		                                                                                	                    

    		        		                                                                                	                   

    		        		                                                                                	                  

    		        		                                                                                	            

    		        		                                                                                	                  

    		        		                                                                                	                   

    		        		                                                                                	                   

//    		        		                                                                                	                    {"I037", "향수병", "C03", 
//    		        		                                                                                	                        "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
//    		        		                                                                                	                        "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
//    		        		                                                                                	                        
//    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 잔여물 비우기 및 세척</b></p>" +
//    		        		                                                                                	                        "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
//    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>원액 배출</span> : 남은 향수는 키친타월에 흡수시켜 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 버려주세요.<br/>" +
//    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>내부 세척</span> : 강한 향이 남지 않도록 내부를 가볍게 물로 헹궈냅니다." +
//    		        		                                                                                	                        "    </div>" +
//
//    		        		                                                                                	                        "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
//    		        		                                                                                	                        "       <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 입구의 <span style='color: #ffcc00; font-weight: bold;'>금속/플라스틱 펌프 노즐</span>을 도구로 분리해야 재활용이 가능합니다. 분리가 불가능하다면 통째로 <span style='color: #ffcc00; font-weight: bold;'>일반 쓰레기</span>로 배출하세요." +
//    		        		                                                                                	                        "    </div>" +
//
//    		        		                                                                                	                        "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 기타 부속품</b></p>" +
//    		        		                                                                                	                        "    <div style='padding-left: 10px; color: #ffffff;'>" +
//    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>장식 제거</span> : 병의 장식물이나 금속 라벨은 제거 후 배출하는 것이 좋습니다.<br/>" +
//    		        		                                                                                	                        "      · <span style='color: #ffcc00; font-weight: bold;'>재질 분리</span> : 뚜껑은 재질(금속/플라스틱)에 따라 따로 분류해 주세요." +
//    		        		                                                                                	                        "    </div>" +
//
//    		        		                                                                                	                        "  </div>" +
//    		        		                                                                                	                        "</div>", 
//    		        		                                                                                	                        "images/glass_bottle/perfume_bottle.png", "images/glass_bottle/mark1.png"},
    	     // [종이팩 - C04] (I043 ~ I052) 보강 버전
    		        		                                                                                	                  

//    		        		                                                                                	                        {"I039", "두유팩(멸균팩)", "C04", 
//    		        		                                                                                	                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
//    		        		                                                                                	                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
//    		        		                                                                                	                            
//    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 은박 코팅 확인 및 세척</b></p>" +
//    		        		                                                                                	                            "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
//    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>멸균팩 확인</span> : 내부가 <span style='color: #ffcc00;'>은색 알루미늄</span>으로 코팅된 멸균팩은 일반 종이팩과 공정이 다릅니다.<br/>" +
//    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>세척 후 펼치기</span> : 내용물을 비우고 헹군 뒤, 가위로 잘라 평평하게 펼쳐서 말려주세요." +
//    		        		                                                                                	                            "    </div>" +
//
//    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
//    		        		                                                                                	                            "       <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span> : 일반 폐지나 살균팩(우유팩)과 섞이지 않게 반드시 <span style='color: #ffcc00; font-weight: bold;'>멸균팩 전용 수거함</span>에 넣어주세요." +
//    		        		                                                                                	                            "    </div>" +
//
//    		        		                                                                                	                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 배출 매너</b></p>" +
//    		        		                                                                                	                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
//    		        		                                                                                	                            "      · <span style='color: #ffcc00; font-weight: bold;'>부속품 제거</span> : 부착된 플라스틱 빨대나 비닐 포장재는 반드시 따로 분리합니다.<br/>" +
//    		        		                                                                                	                            "    </div>" +
//    		        		                                                                                	                            
//    		        		                                                                                	                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
//    		        		                                                                                	                            "       <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 전용함이 없다면 주민센터의 <span style='font-weight: bold; color: #ffcc00;'>종이팩 보상 제도</span>를 확인해 보세요!" +
//    		        		                                                                                	                            "    </div>" +
//
//    		        		                                                                                	                            "  </div>" +
//    		        		                                                                                	                            "</div>", 
//    		        		                                                                                	                            "images/paper_pack/soy_milk_carton.png", "images/paper_pack/mark2.png"},

    		        		                                                                                	                       

    		        		                                                                                	                      

    		        		                                                                                	                     

    		        		                                                                                	                      

    		        		                                                                                	                   
    	     // [캔/고철 - C05] (I053 ~ I074) 보강 버전
//    		        		                                                                                	                        {"I045", "스프레이/살충제", "C05", 
//    		        		                                                                                	                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
//    		        		                                                                                	                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
//    		        		                                                                                	                                
//    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
//    		        		                                                                                	                                "       <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의 : 폭발 사고 주의</span><br/>" +
//    		        		                                                                                	                                "      · 가스가 남으면 화재의 원인이 됩니다. <span style='color: #ffcc00; font-weight: bold;'>통풍이 잘되는 실외</span>에서 소리가 나지 않을 때까지 비워주세요." +
//    		        		                                                                                	                                "    </div>" +
//
//    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 잔류 가스 제거</b></p>" +
//    		        		                                                                                	                                "    <div style='padding-left: 10px; margin-bottom: 25px; color: #ffffff;'>" +
//    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>구멍 뚫기</span> : 안전을 위해 캔 옆면에 전용 도구로 구멍을 뚫어 남은 가스를 완전히 배출하는 것이 좋습니다.<br/>" +
//    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>노즐 고장 시</span> : 내용물이 남았는데 나오지 않는다면 지자체의 유해폐기물 배출 방법을 확인하세요." +
//    		        		                                                                                	                                "    </div>" +
//
//    		        		                                                                                	                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 부속품 분리 배출</b></p>" +
//    		        		                                                                                	                                "    <div style='padding-left: 10px; color: #ffffff;'>" +
//    		        		                                                                                	                                "      · <span style='color: #ffcc00; font-weight: bold;'>캡 분리</span> : 플라스틱 캡은 분리하여 <span style='color: #ffcc00;'>플라스틱</span>으로 배출하세요.<br/>" +
//    		        		                                                                                	                                "    </div>" +
//    		        		                                                                                	                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
//    		        		                                                                                	                                "       <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 캔 본체는 고철(캔류) 수거함으로 분류합니다." +
//    		        		                                                                                	                                "    </div>" +
//
//    		        		                                                                                	                                "  </div>" +
//    		        		                                                                                	                                "</div>", 
//    		        		                                                                                	                                "images/can/spray_can.png", "images/can/mark1.png"},
  
    	     // [스티로폼 - C06] (I075 ~ I084) 보강 버전
    		        		                                                                                	                                   

//    		        		                                                                                	                                        {"I068", "과일 포장재", "C06", 
//    		        		                                                                                	                                            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
//    		        		                                                                                	                                            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
//    		        		                                                                                	                                            
//    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 색상별 배출 구분</b></p>" +
//    		        		                                                                                	                                            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
//    		        		                                                                                	                                            "      · <span style='color: #ffcc00; font-weight: bold;'>흰색 그물망</span> : 오염이 없는 깨끗한 상태라면 <span style='color: #ffcc00;'>스티로폼</span>으로 배출하세요." +
//    		        		                                                                                	                                            "    </div>" +
//
//    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
//    		        		                                                                                	                                            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
//    		        		                                                                                	                                            "      · 분홍, 노란색 등 <span style='color: #ffcc00; font-weight: bold;'>색깔이 있는 것</span>은 재활용이 안 되므로 <span style='color: #ffcc00;'>일반 쓰레기</span>로 버려야 합니다." +
//    		        		                                                                                	                                            "    </div>" +
//
//    		        		                                                                                	                                            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>2. 상태 확인</b></p>" +
//    		        		                                                                                	                                            "    <div style='padding-left: 10px; color: #ffffff;'>" +
//    		        		                                                                                	                                            "      · 과일즙 등 이물질이 묻은 경우 반드시 <span style='color: #ffcc00;'>일반 쓰레기</span>로 분류하세요.<br/>" +
//    		        		                                                                                	                                            "    </div>" +
//    		        		                                                                                	                                            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; margin-bottom: 20px;'>" +
//    		        		                                                                                	                                            "      · <span style='color: #00fff0; font-weight: bold;'>💡 Tip</span> : 지자체별로 과일망을 일반 쓰레기로만 받는 곳이 많으니 확인이 필요합니다." +
//    		        		                                                                                	                                            "    </div>" +
//
//    		        		                                                                                	                                            "  </div>" +
//    		        		                                                                                	                                            "</div>", 
//    		        		                                                                                	                                            "images/Strofoam/fruit_wrap.png", ""},

    		        		                                                                                	                                    

    		        		                                                                                	                                  

    		        		                                                                                	                                   

    		        		                                                                                	                                     

    		        		                                                                                	                                     

    		        		                                                                                	                                    
    		        		                                                                                	                                

    		        		                                                                                	                                    

    		        		                                                                                	                          
    	     // [플라스틱 - C07] (I085 ~ I102) 보강 버전
    		        		                                                                                	                               

    		        		                                                                                	                                     

//    		        		                                                                                	                                            {"I087", "도마(플라스틱)", "C07", 
//    		        		                                                                                	                                                "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
//    		        		                                                                                	                                                "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
//    		        		                                                                                	                                                
//    		        		                                                                                	                                                "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 칼자국 사이 세척</b></p>" +
//    		        		                                                                                	                                                "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
//    		        		                                                                                	                                                "      · <span style='color: #ffcc00; font-weight: bold;'>칼자국 틈새</span>에 낀 음식물을 솔로 깨끗이 닦아내고 배출해야 합니다.<br/>" +
//    		        		                                                                                	                                                "      · 이물질이 남은 도마는 재활용 품질을 크게 떨어뜨립니다." +
//    		        		                                                                                	                                                "    </div>" +
//
//    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
//    		        		                                                                                	                                                "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
//    		        		                                                                                	                                                "      · <span style='color: #ffcc00; font-weight: bold;'>실리콘, 고무, 나무</span>가 섞인 도마는 플라스틱 배출함에 넣지 마세요." +
//    		        		                                                                                	                                                "    </div>" +
//
//    		        		                                                                                	                                                "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
//    		        		                                                                                	                                               
//    		        		                                                                                	                                                "      <span><b style='color: #00fff0;'>💡 Tip</b> : <b style='color: #ffcc00;'>PP 재질</b> 위주로 배출하며, 마크가 없다면 일반 쓰레기로 권장합니다.</span>" +
//    		        		                                                                                	                                                "    </div>" +
//
//    		        		                                                                                	                                                "  </div>" +
//    		        		                                                                                	                                                "</div>", 
//    		        		                                                                                	                                                "images/plastic/plastic_cutting_board.png", "images/plastic/mark3.png"},

    		        		                                                                                	                                        

    		        		                                                                                	                                          

    		        		                                                                                	                                      
    		        		                                                                                	                                           

    		        		                                                                                	                                        

    		        		                                                                                	                                            
    		        		                                                                                	                                          

    		        		                                                                                	                                             

    		        		                                                                                	                                               

    		        		                                                                                	                                              
    		        		                                                                                	                                            

    		        		                                                                                	                                              

    		        		                                                                                	                                              

    		        		                                                                                	                                               
    		        		                                                                                	                                             
    		        		                                                                                	                                                
    		        		                                                                                	                                                

	                                                                                                            										 

	                                                                                                            										  
    	     // [기타 - C08] (I103 ~ I138) 보강 버전
//	                                                                                                            										    {"I103", "공학용 계산기", "C08", 
//	                                                                                                            										            "<div style='font-family: \"맑은 고딕\"; line-height: 1.8; color: #ffffff;'>" +
//	                                                                                                            										            "  <div style='background-color: #25254b; padding: 20px; border-radius: 12px; border: 1px solid #3d3d70;'>" +
//	                                                                                                            										            
//	                                                                                                            										            "    <p style='margin-bottom: 10px;'><b style='color: #ffffff; font-size: 17px;'>1. 소형 가전 배출 방법</b></p>" +
//	                                                                                                            										            "    <div style='padding-left: 10px; margin-bottom: 25px;'>" +
//	                                                                                                            										            "      · 계산기는 소형 가전으로 분류되어 <span style='color: #ffcc00; font-weight: bold;'>소형전기전자제품 전용수거함</span>으로 배출합니다.<br/>" +
//	                                                                                                            										            "      · 인근에 수거함이 없다면 <span style='color: #ffcc00;'>주민센터</span>에 문의하거나 가전 무상방문수거를 이용하세요." +
//	                                                                                                            										            "    </div>" +
//
//	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b6b; margin-bottom: 20px;'>" +
//	                                                                                                            										            "      · <span style='color: #ff6b6b; font-weight: bold;'>⚠️ 주의</span><br/>" +
//	                                                                                                            										            "      · 내장된 <span style='color: #ffffff; font-weight: bold;'>건전지나 전지</span>는 반드시 분리하여 <span style='color: #ffcc00;'>폐건전지 수거함</span>에 따로 배출해야 합니다." +
//	                                                                                                            										            "    </div>" +
//
//	                                                                                                            										            "    <div style='background-color: #1a1a3a; padding: 15px; border-radius: 8px; border-left: 4px solid #00fff0; display: flex; align-items: center; gap: 15px;'>" +
//	                                                                                                            										            "      <span><b style='color: #00fff0;'>💡 Tip</b> : 타 가전제품 배출 시 <b style='color: #ffcc00;'>무상방문수거서비스</b>를 이용하면 함께 배출이 가능하여 편리합니다.</span>" +
//	                                                                                                            										            "    </div>" +
//
//	                                                                                                            										            "  </div>" +
//	                                                                                                            										            "</div>", 
//	                                                                                                            										            "images/Clothing/calculator.png", ""},
	           
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
        public int itemId; 
        public String itemName, categoryName, disposalGuide;
        public String itemImagePath, markImagePath;

        public ItemDetail(int id, String name, String cat, String guide, String itemImg, String markImg) {
            this.itemId = id;
            this.itemName = name;
            this.categoryName = cat;
            this.disposalGuide = guide;
            this.itemImagePath = itemImg;
            this.markImagePath = markImg;
        }
        public String getDisplayId(String categoryId) {
            String prefix = "I";
            if (categoryId != null) {
                switch (categoryId) {
                    case "C01": prefix = "P"; break; 
                    case "C02": prefix = "V"; break; 
                    case "C03": prefix = "G"; break; 
               
                }
            }
            return String.format("%s%03d", prefix, this.itemId);
        }
    }
    public static void initializeDatabase() {
        try (Connection conn = RecycleDB.connect();
             Statement stmt = conn.createStatement()) {


            String createCategoriesSQL = 
                    "CREATE TABLE IF NOT EXISTS " + CATEGORIES_TABLE + " (" +
                            "CATEGORY_ID VARCHAR(10) PRIMARY KEY," +
                            "CATEGORY_NAME VARCHAR(50) NOT NULL UNIQUE," +
                            "REWARD_POINTS INT NOT NULL DEFAULT 0)";

            String createItemsSQL = "CREATE TABLE IF NOT EXISTS " + ITEMS_TABLE + " (" +
                    "ITEM_ID INT AUTO_INCREMENT PRIMARY KEY," + 
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
        // 1. 카테고리 삽입 (기존 로직 유지)
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

        // 2. 아이템 삽입 수정
        String itemSql = "INSERT INTO " + ITEMS_TABLE + " (ITEM_NAME, CATEGORY_ID, DISPOSAL_GUIDE, ITEM_IMAGE_PATH, MARK_IMAGE_PATH) " +
                "VALUES (?, ?, ?, ?, ?) " + 
                "ON DUPLICATE KEY UPDATE DISPOSAL_GUIDE = VALUES(DISPOSAL_GUIDE), ITEM_IMAGE_PATH = VALUES(ITEM_IMAGE_PATH), MARK_IMAGE_PATH = VALUES(MARK_IMAGE_PATH)";    
        try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
            for (String[] item : ITEMS_FULL_DATA) {

                pstmt.setString(1, item[1]); 
                pstmt.setString(2, item[2]); 
                pstmt.setString(3, item[3]); 
                pstmt.setString(4, item[4]); 
                pstmt.setString(5, item[5]);

                pstmt.addBatch();
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

 // 모든 아이템 가져오기
    public static List<GuideDTO> getAllItems() throws SQLException {
        List<GuideDTO> list = new ArrayList<>();
        String sql = "SELECT i.ITEM_ID, i.ITEM_NAME, i.CATEGORY_ID, c.CATEGORY_NAME, i.DISPOSAL_GUIDE, i.ITEM_IMAGE_PATH, i.MARK_IMAGE_PATH " +
                     "FROM " + ITEMS_TABLE + " i JOIN " + CATEGORIES_TABLE + " c ON i.CATEGORY_ID = c.CATEGORY_ID";
        
        try (Connection conn = RecycleDB.connect(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
 
                list.add(new GuideDTO(
                    rs.getInt("ITEM_ID"),          
                    rs.getString("ITEM_NAME"),       
                    rs.getString("CATEGORY_ID"),    
                    rs.getString("CATEGORY_NAME"),   
                    rs.getString("DISPOSAL_GUIDE"),  
                    rs.getString("ITEM_IMAGE_PATH"), 
                    rs.getString("MARK_IMAGE_PATH")  
                ));
            }
        }
        return list;
    }

    // 특정 아이템 상세 가져오기
    public static GuideDTO getItemDetail(String itemName, String categoryName) throws SQLException {
        String sql = "SELECT i.ITEM_ID, i.ITEM_NAME, i.CATEGORY_ID, c.CATEGORY_NAME, i.DISPOSAL_GUIDE, " +
                     "i.ITEM_IMAGE_PATH, i.MARK_IMAGE_PATH FROM " + ITEMS_TABLE + " i " +
                     "JOIN " + CATEGORIES_TABLE + " c ON i.CATEGORY_ID = c.CATEGORY_ID " +
                     "WHERE i.ITEM_NAME = ? AND c.CATEGORY_NAME = ?";
        
        try (Connection conn = RecycleDB.connect(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemName);
            pstmt.setString(2, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                
                    return new GuideDTO(
                        rs.getInt("ITEM_ID"),           
                        rs.getString("ITEM_NAME"),     
                        rs.getString("CATEGORY_ID"),     
                        rs.getString("CATEGORY_NAME"),   
                        rs.getString("DISPOSAL_GUIDE"), 
                        rs.getString("ITEM_IMAGE_PATH"), 
                        rs.getString("MARK_IMAGE_PATH")  
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