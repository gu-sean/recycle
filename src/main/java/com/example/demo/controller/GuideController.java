package com.example.demo.controller;

import db.DAO.GuideDAO;
import db.DTO.GuideDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/guide")
@CrossOrigin(origins = "*")
public class GuideController {

    @GetMapping("/items")
    public ResponseEntity<List<GuideDTO>> getItemsByCategory(@RequestParam String categoryId) {
        try {
            // GuideDAO의 메서드명이 getItemsByCategory인지 확인하세요
            List<GuideDTO> items = GuideDAO.getItemsByCategory(categoryId); 
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/detail")
    public ResponseEntity<GuideDTO> getItemDetail(@RequestParam String itemName, @RequestParam String categoryName) {
        try {
            GuideDTO detail = GuideDAO.getItemDetail(itemName, categoryName);
            if (detail != null) return ResponseEntity.ok(detail);
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        List<String> suggestions = GuideDAO.getAutoComplete(query);
        return ResponseEntity.ok(suggestions);
    }
 // GuideController.java 에 추가
    @GetMapping("/search")
    public ResponseEntity<?> searchItem(@RequestParam String query) {
        try {
            GuideDTO item = GuideDAO.searchItemByName(query);
            if (item != null) {
                return ResponseEntity.ok(item); // 품목 발견 시 데이터 반환
            } else {
                return ResponseEntity.status(404).body("검색 결과가 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}