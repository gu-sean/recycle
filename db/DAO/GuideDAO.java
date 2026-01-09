package db.DAO;

import java.sql.Connection;
import db.RecycleDB;
import db.DTO.GuideDTO; 
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

    private static final String CSS_STYLES = 
        "<style>"
        + "h1 { font-size: 1.4em; color: #2e7d32; margin-bottom: 10px; }"
        + "h2 { margin-top: 10px; margin-bottom: 5px; font-size: 1.1em; color: #007bff; }"
        + "h3 { margin-top: 10px; margin-bottom: 5px; font-size: 1.0em; color: #333; }"
        + "ul { margin-top: 5px; margin-left: 10px; padding-left: 10px; }"
        + "li { margin-bottom: 3px; list-style-type: disc; }"
        + ".note { padding-left: 15px; list-style-type: none; font-style: italic; color: #ff5722; }"
        + ".guide-text { line-height: 1.6; font-size: 14px; }"
        + "hr { border: 0; height: 1px; background-color: #eee; margin: 10px 0; }"
        + "</style>";

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
    		 // [종이류 - C01]
            {"I001", "가격표", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I002", "골판지", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I003", "광고전단지", "C01", "비닐코팅된 종이는 쓰레기 <종량제 봉투>로 배출합니다."},
            {"I004", "랩의 심", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I005", "명함", "C01", "플라스틱 합성지 등 다른 재질 포함 시 <종량제 봉투>에 배출합니다."},
            {"I006", "백과사전", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I007", "사전", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I008", "쌀포대", "C01", "<종이류> 또는 재질에 맞게 배출합니다."},
            {"I009", "서류봉투(갈색종이)", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I010", "수첩", "C01", "<종이류> 또는 재질에 맞게 배출합니다."},
            {"I011", "신문지", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I012", "잡지", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I013", "전단지", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I014", "전화번호부", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I015", "종이상자", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I016", "종이심", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I017", "종이조각", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I018", "책", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I019", "치킨박스", "C01", "기름에 오염된 내부 종이는 <종량제봉투>로 배출합니다."},
            {"I020", "탁상달력", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I021", "포스터, 포장지", "C01", "<종이류> 배출방법을 준수하여 배출합니다."},
            {"I022", "피자박스", "C01", "기름에 오염된 내부 종이는 <종량제봉투>로 배출합니다."},
            
            // [비닐 - C02]
            {"I023", "비닐봉지(일회용)", "C02", "<비닐류> 배출방법을 준수하여 배출합니다."},
            {"I024", "아이스팩", "C02", "(일반 아이스팩) 고흡수성수지(Gel 형태)가 포함된 재활용이 어려운 폐기물로서 쓰레기 <종량제봉투>로 배출합니다.\n\n" + "(물 아이스팩) 아이스팩을 뜯어 내용물을 비우고, 겉의 비닐을 깨끗하게 배출하면 재활용이 가능합니다.\n\n" + "※아이스팩 겉면에 고흡수성수지 포함 여부 표기"},
            {"I025", "완충재(뽁뽁이)", "C02", "<비닐류> 배출방법을 준수하여 배출합니다."},
            
            // [유리병 - C03]
            {"I026", "유리병", "C03", "<유리병류> 배출방법을 준수하여 배출합니다."},
            
            // [종이팩 - C04]
            {"I027", "우유팩(일반팩)", "C04", "<종이팩류> 배출방법을 준수하여 배출합니다."},
            {"I028", "두유팩(멸균팩)", "C04", "<종이팩류> 종이(폐지)와 구별하여 배출합니다."},
            
            // [캔/고철 - C05]
            {"I029", "스프레이", "C05", "<금속캔(부탄가스통)> 배출방법을 준수하여 배출합니다."},
            {"I030", "부탄가스", "C05", "<금속캔(부탄가스통)> 배출방법을 준수하여 배출합니다."},
            {"I031", "애완동물 음식캔", "C05", "<금속캔> 배출방법을 준수하여 배출합니다."},
            {"I032", "공구류(철)", "C05", "다른 재질이 많이 섞인 제품은 분리해서 배출하며, 분리가 어렵다면 쓰레기 <종량제 봉투>로 배출합니다."},
            {"I033", "국자(고철)", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I034", "그릇(고철)", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I035", "나사(못)", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I036", "낫", "C05", "<고철>로 배출하되, 가능하다면 손잡이 부분(나무재질 등)을 분리하여 배출합니다."},
            {"I037", "도끼", "C05", "<고철>로 배출하되, 가능하다면 손잡이 부분(나무재질 등)을 분리하여 배출합니다."},
            {"I038", "맥주병뚜껑(철, 알루미늄)", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I039", "못", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I040", "분유 깡통", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I041", "쓰레기받기(고철)", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I042", "아령", "C05", "<고철류> 또는 재질에 맞게 배출합니다."},
            {"I043", "압력솥", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I044", "역기", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I045", "옷걸이(세탁소 흰색 철사)", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I046", "유리병 뚜껑(철, 알루미늄)", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I047", "의류건조대", "C05", "<고철류>, <플라스틱류> 등 재질에 맞게 배출합니다."},
            {"I048", "재떨이(금속)", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I049", "주전자(철, 알루미늄)", "C05", "플라스틱 뚜껑 등은 돌려서 제거한 후 배출합니다."},
            {"I050", "철사", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I051", "철판(가정요리용)", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I052", "캔 따개", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I053", "컵(고철)", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I054", "톱", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            {"I055", "후라이팬", "C05", "<고철> 배출방법을 준수하여 배출합니다."},
            
            // [스티로폼 - C06]
            {"I056", "컵라면(스티로폼)", "C06", "<스티로폼> 배출방법을 준수하여 배출합니다."},
            {"I057", "스티로폼 상자", "C06", "<스티로폼> 배출방법을 준수하여 배출합니다."},
            
            // [플라스틱 - C07]
            {"I058", "국자(플라스틱)", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I059", "그릇(플라스틱)", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I060", "도마(플라스틱 도마)", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I061", "리코더(플라스틱)", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I062", "마요네즈 용기", "C07", "내부를 헹구고 플라스틱 또는 재질에 맞는 분리수거함으로 배출합니다."},
            {"I063", "메가폰(플라스틱)", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I064", "빨대", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I065", "볼풀공", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I066", "분무기(플라스틱)", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I067", "비디오테이프", "C07", "내부필름은 분리하여 쓰레기 <종량제봉투>로 배출합니다."},
            {"I068", "샴푸용기", "C07", "내부를 헹구고 배출합니다."},
            {"I069", "쓰레받기(플라스틱)", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I070", "식용유용기(플라스틱)", "C07", "모두 사용 후 배출합니다."},
            {"I071", "젖병(아기용품)", "C07", "젖병의 몸체와 윗부분의 젖꼭지는 분리하여 재질에 맞게 배출합니다."},
            {"I072", "치약용기", "C07", "<플라스틱류> 또는 재질에 맞게 배출합니다."},
            {"I073", "캡(플라스틱 뚜껑)", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I074", "컵(플라스틱)", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},
            {"I075", "케찹용기", "C07", "내부를 헹구고 플라스틱 또는 재질에 맞는 분리수거함으로 배출합니다."},
            {"I076", "페트병", "C07", "<플라스틱류> 배출방법을 준수하여 배출합니다."},

            // [기타 - C08]
            {"I077", "농약용기", "C08", "내용물을 다 사용한 후 따로 봉투에 담아 배출합니다."},
            {"I078", "구두", "C08", "<의류 및 원단류>의 배출방법을 참고하여 배출하거나 <종량제봉투>로 배출합니다."},
            {"I079", "샌들", "C08", "<의류 및 원단류>의 배출방법을 참고하여 배출하거나 <종량제봉투>로 배출합니다."},
            {"I080", "슬리퍼", "C08", "<의류 및 원단류>의 배출방법을 참고하여 배출하거나 <종량제봉투>로 배출합니다."},
            {"I081", "머플러", "C08", "<의류 및 원단류>의 배출방법을 참고하여 배출합니다."},
            {"I082", "목도리", "C08", "<의류 및 원단류>의 배출방법을 참고하여 배출합니다."},
            {"I083", "모자", "C08", "<의류 및 원단류>의 배출방법을 참고하여 배출하거나 <종량제봉투>로 배출합니다."},
            {"I084", "의류", "C08", "<의류 및 원단류>의 배출방법을 참고하여 배출합니다."},
            {"I085", "기름", "C08", "지역내 비치된 해당 제품의 <전용수거함>으로 배출하되, 별도 전용수거함이 없는 경우 지자체 조례에 따라 배출합니다."},
            {"I086", "기름(기계)", "C08", "판매처, 구입처 또는 <전문처리시설>을 통해 처리 또는 배출합니다."},
            {"I087", "엔진오일", "C08", "<전문처리시설>(카센터 등)로 배출합니다."},
            {"I088", "윤활유", "C08", "구입처와 상담 후 배출합니다."},
            {"I089", "자동차 부품", "C08", "중고센터, 판매처 등과 상담하여 처리합니다."},
            {"I090", "타이어", "C08", "구입처와 상담 후 배출합니다."},
            {"I091", "빗", "C08", "재질에 맞게 배출하되 나무 빗 등은 쓰레기 <종량제봉투>로 배출합니다."},
            {"I092", "헤어브러시", "C08", "재질에 맞게 배출하되 나무 빗 등은 쓰레기 <종량제봉투>로 배출합니다."},
            {"I093", "애완동물집", "C08", "재질별로 분류하여 배출합니다."},
            {"I094", "운반케이스", "C08", "재질별로 분류하여 배출합니다."},
            {"I095", "야구배트", "C08", "재질에 맞게 배출 또는 쓰레기 <종량제봉투>로 배출합니다."},
            {"I096", "와이퍼", "C08", "재질별로 분류하여 배출합니다."},
            {"I097", "줄자", "C08", "재질에 맞게 배출 또는 <종량제봉투>로 배출합니다."},
            {"I098", "바나나껍질", "C08", "<음식물쓰레기> 배출방법을 준수하여 배출합니다."},
            {"I099", "상한 음식", "C08", "<음식물쓰레기> 배출방법을 준수하여 배출합니다."},
            {"I100", "먹고 남은 생선", "C08", "생선뼈는 쓰레기 <종량제봉투>에 버리고, 나머지는 <음식물쓰레기>로 배출합니다."},
            {"I101", "오렌지껍질", "C08", "<음식물쓰레기> 배출방법을 준수하여 배출합니다."},
            {"I102", "깨진유리", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I103", "거울", "C08", "크기에 따라 <불연성폐기물> 또는 <대형폐기물>로 배출합니다."},
            {"I104", "그릇(도자기, 유기그릇)", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I105", "내열식기류", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I106", "도자기류", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I107", "뚝배기", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I108", "머그컵(도자기류)", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I109", "백열전구", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I110", "벽돌", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I111", "유리판, 유리제품", "C08", "<불연성폐기물> 또는 <대형폐기물>로 배출합니다."},
            {"I112", "재떨이(도자기, 유리)", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I113", "찻잔(도자기류)", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I114", "컵(도자기,유리)", "C08", "불에 타지 않는 폐기물이며 <불연성폐기물> 배출방법을 참조하여 배출합니다."},
            {"I115", "화분,화병", "C08", "<불연성폐기물>로 배출 등 재질에 맞게 배출합니다."},
            {"I116", "가습기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I117", "공기청정기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I118", "냉장고", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I119", "냉동고", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I120", "노트북", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I121", "다리미", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I122", "디지털카메라", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I123", "라디오", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I124", "런닝머신", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I125", "모니터", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I126", "컴퓨터", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I127", "TV", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I128", "복사기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I129", "(전기)비데", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I130", "비디오카메라", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I131", "선풍기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I132", "세탁기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I133", "스캐너", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I134", "스탠드", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I135", "스피커", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I136", "식기세척기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I137", "식기건조기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I138", "에어컨", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I139", "오디오세트", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I140", "온풍기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I141", "와인셀러", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I142", "전기밥솥", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I143", "전기오븐레인지", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I144", "전기포트", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I145", "전기히터", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I146", "전자레인지", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I147", "전자사전(전자수첩)", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I148", "전축", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I149", "전화기(팩스포함)", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I150", "정수기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I151", "청소기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I152", "카메라", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I153", "커피메이커", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I154", "키보드(컴퓨터용)", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I155", "탈수기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I156", "토스터기", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I157", "팩시밀리(팩스기기)", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I158", "프린터", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I159", "헤드폰", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I160", "화장품냉장고", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."},
            {"I161", "휴대전화", "C08", "<폐가전제품> 배출방법을 준수하여 배출합니다."}
        
    };

    public static class ItemDetail {
        public String itemId; 
        public String itemName;
        public String categoryName;
        public String disposalGuide; 

        public ItemDetail(String itemName, String categoryName, String disposalGuide) {
            this.itemName = itemName;
            this.categoryName = categoryName;
            this.disposalGuide = disposalGuide;
        }

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
            for (Map.Entry<String, String> entry : CATEGORY_ID_MAP.entrySet()) {
                pstmt.setString(1, entry.getValue());
                pstmt.setString(2, entry.getKey());
                pstmt.setInt(3, CATEGORY_REWARDS.get(entry.getKey()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }

        String itemSql = "INSERT INTO " + ITEMS_TABLE + " (ITEM_ID, ITEM_NAME, CATEGORY_ID, DISPOSAL_GUIDE) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
            for (String[] item : ITEMS_FULL_DATA) {
                pstmt.setString(1, item[0]);
                pstmt.setString(2, item[1]);
                pstmt.setString(3, item[2]);
                pstmt.setString(4, item[3]);
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

    public static List<ItemDetail> getAllItems() throws SQLException {
        List<ItemDetail> list = new ArrayList<>();
        String sql = "SELECT i.ITEM_NAME, c.CATEGORY_NAME, i.DISPOSAL_GUIDE FROM " + ITEMS_TABLE + " i " +
                     "JOIN " + CATEGORIES_TABLE + " c ON i.CATEGORY_ID = c.CATEGORY_ID";
        try (Connection conn = RecycleDB.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ItemDetail(rs.getString("ITEM_NAME"), rs.getString("CATEGORY_NAME"), rs.getString("DISPOSAL_GUIDE")));
            }
        }
        return list;
    }

    public static ItemDetail getItemDetail(String itemName, String categoryName) throws SQLException {
        String sql = "SELECT i.ITEM_NAME, c.CATEGORY_NAME, i.DISPOSAL_GUIDE FROM " + ITEMS_TABLE + " i " +
                     "JOIN " + CATEGORIES_TABLE + " c ON i.CATEGORY_ID = c.CATEGORY_ID " +
                     "WHERE i.ITEM_NAME = ? AND c.CATEGORY_NAME = ?";
        try (Connection conn = RecycleDB.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemName);
            pstmt.setString(2, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ItemDetail(rs.getString("ITEM_NAME"), rs.getString("CATEGORY_NAME"), rs.getString("DISPOSAL_GUIDE"));
                }
            }
        }
        return null;
    }

    public static String getCssStyles() { return CSS_STYLES; }
}