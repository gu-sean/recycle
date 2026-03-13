package com.example.demo.service;

import db.DAO.RecycleLogDAO;
import db.DTO.RecycleResponse;
import org.springframework.beans.factory.annotation.Autowired; // 추가
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.sql.SQLException;
import java.util.*;
import db.DAO.UserDAO;
@Service
public class RecycleService {

    // [수정] 직접 new 하지 않고 스프링으로부터 주입받습니다.
    // 이를 위해 RecycleLogDAO 클래스 상단에도 @Repository 어노테이션이 있어야 합니다.
    @Autowired
    private RecycleLogDAO logDAO;
    @Autowired
    private UserDAO userDAO;
    
    private final Map<String, Integer> itemPoints = new LinkedHashMap<>() {{
        put("종이", 15); put("비닐", 10); put("유리병", 25); put("종이팩", 20);
        put("캔/고철", 40); put("스티로폼", 10); put("플라스틱", 10); put("기타", 5);
    }};

    /**
     * 분리수거 로그 저장 및 포인트 적립
     */
    /**
     * 분리수거 로그 저장 및 포인트 적립
     */
    public void saveLogAndAddPoint(String userId, String itemName) throws Exception {
        // 1. 오늘 이미 등록한 품목인지 확인
        if (logDAO.isAlreadyRecycledToday(userId, itemName)) {
            throw new Exception("오늘 이미 [" + itemName + "] 기록을 완료하셨습니다. (1일 1회만 가능)");
        }

        // 2. 중복이 아닐 경우 포인트 계산
        int point = itemPoints.getOrDefault(itemName, 0);

        // 3. 포인트 로그 저장 (RecycleLogDAO 사용)
        logDAO.insertPointLog(userId, itemName, point);
        
        userDAO.addPoints(userId, point);
        
        System.out.println("DEBUG >>> 유저 테이블 포인트 증가: " + userId + ", +" + point);
    }

    /**
     * AI 이미지 분석 (파일명 기반 키워드 판별)
     */
    public String predictCategory(MultipartFile image) {
        if (image == null || image.isEmpty()) return "파일없음";
        
        String fileName = image.getOriginalFilename().toLowerCase();
        
        // 키워드 판별 로직
        if(fileName.contains("paper") || fileName.contains("종이") || fileName.contains("box")) return "종이";
        if(fileName.contains("plastic") || fileName.contains("플라스틱") || fileName.contains("pet")) return "플라스틱";
        if(fileName.contains("glass") || fileName.contains("유리") || fileName.contains("bottle")) return "유리병";
        if(fileName.contains("can") || fileName.contains("캔") || fileName.contains("철")) return "캔/고철";
        if(fileName.contains("vinyl") || fileName.contains("비닐") || fileName.contains("봉투")) return "비닐";
        if(fileName.contains("pack") || fileName.contains("팩") || fileName.contains("우유")) return "종이팩";
        if(fileName.contains("foam") || fileName.contains("스티로폼") || fileName.contains("ice")) return "스티로폼";
        if(fileName.contains("etc") || fileName.contains("기타")) return "기타";
        
        return "미분류항목"; 
    }

    /**
     * 유효한 카테고리 여부 확인
     */
    public boolean isValidItem(String category) {
        return itemPoints.containsKey(category);
    }

    /**
     * 오늘 적립한 분리수거 목록 조회
     */
    public List<RecycleResponse> getTodayLogs(String userId) throws SQLException {
        // DAO에서 오늘 적립된 아이템 이름 리스트를 가져옴
        List<String> items = logDAO.getTodayRecycleItems(userId); 
        
        List<RecycleResponse> responseList = new ArrayList<>();
        
        if (items != null) {
            for (String itemName : items) {
                // 아이템 이름에 맞는 포인트를 맵에서 찾아 응답 객체(DTO) 생성
                int point = itemPoints.getOrDefault(itemName, 0);
                responseList.add(new RecycleResponse(itemName, point));
            }
        }
        return responseList;
    }
}