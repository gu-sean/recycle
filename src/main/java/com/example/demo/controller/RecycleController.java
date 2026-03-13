package com.example.demo.controller;

import db.DTO.RecycleRequest; 
import db.DTO.RecycleResponse;
import com.example.demo.service.RecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/recycle")
public class RecycleController {

    @Autowired
    private RecycleService recycleService;

    // 1. 분리수거 기록 저장 API
    @PostMapping("/save")
    public ResponseEntity<?> saveRecycleLog(@RequestBody RecycleRequest request) {
        try {
            recycleService.saveLogAndAddPoint(request.getUserId(), request.getItem());
            return ResponseEntity.ok().body("적립이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("저장 실패: " + e.getMessage());
        }
    }

    // 2. 오늘 내가 한 분리수거 목록 조회 API
    @GetMapping("/today")
    public ResponseEntity<?> getTodayLogs(@RequestParam String userId) {
        try {
            // 서비스에서 오늘 날짜의 기록 리스트를 가져옴
            List<RecycleResponse> logs = recycleService.getTodayLogs(userId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 3. AI 이미지 분석 API
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeImage(@RequestParam("image") MultipartFile image) {
        try {
            // 서비스의 AI 분석 메서드 호출 (이미지 파일명 기반 분석 등)
            String category = recycleService.predictCategory(image);
            
            // 유효한 8가지 카테고리(종이, 플라스틱 등)인지 확인
            if (recycleService.isValidItem(category)) {
                Map<String, String> res = new HashMap<>();
                res.put("category", category);
                return ResponseEntity.ok(res);
            } else {
                return ResponseEntity.badRequest().body("인식된 [" + category + "] 항목은 등록할 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("이미지 처리 중 오류: " + e.getMessage());
        }
    }
}