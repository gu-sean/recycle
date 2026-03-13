package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import db.DAO.ProductsDAO;
import db.DAO.UserDAO;
import db.DTO.ProductsDTO;
import db.DTO.UserDTO;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    private final UserDAO userDAO = new UserDAO();
    private final ProductsDAO productsDAO = new ProductsDAO();

    // [상품 목록 조회]
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.ok(productsDAO.getAllProducts());
    }

    // [상품 생성] - POST /api/admin/products
    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@RequestBody ProductsDTO product) {
        try {
            // ID 자동 생성 (중복 방지)
            if (product.getProductId() == null || product.getProductId().isEmpty()) {
                product.setProductId("PROD_" + System.currentTimeMillis());
            }
            boolean success = productsDAO.insertProduct(product);
            return success ? ResponseEntity.ok("등록 성공") : ResponseEntity.status(400).body("등록 실패");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // [상품 수정] - PUT /api/admin/products
    @PutMapping("/products")
    public ResponseEntity<?> updateProduct(@RequestBody ProductsDTO product) {
        try {
            boolean success = productsDAO.updateProduct(product);
            return success ? ResponseEntity.ok("수정 성공") : ResponseEntity.status(400).body("수정 실패");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // [상품 삭제]
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable String productId) {
        try {
            boolean success = productsDAO.deleteProduct(productId);
            return success ? ResponseEntity.ok("삭제 성공") : ResponseEntity.status(400).body("삭제 실패");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // [회원 목록 조회]
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userDAO.getAllUsersPaged(1000, 0));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // [회원 정보 수정]
    @PutMapping("/users")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO user) {
        try {
            boolean success = userDAO.updateUserByAdmin(user);
            return success ? ResponseEntity.ok("회원 수정 성공") : ResponseEntity.status(400).body("수정 실패");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
 // [회원 생성] - POST /api/admin/users
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) {
        try {
            // 이미 존재하는 아이디인지 체크
            if (userDAO.getUserById(user.getUserId()) != null) {
                return ResponseEntity.status(400).body("이미 존재하는 아이디입니다.");
            }
            
            // UserDAO.registerUser 로직 활용 (비밀번호 해싱 포함)
            boolean success = userDAO.registerUser(user.getUserId(), user.getPassword(), user.getNickname());
            
            // 생성 후 추가 정보(포인트, 권한) 업데이트
            if (success) {
                userDAO.updateUserByAdmin(user); 
                return ResponseEntity.ok("회원 등록 성공");
            }
            return ResponseEntity.status(400).body("등록 실패");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // [회원 삭제] - DELETE /api/admin/users/{userId}
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            // UserDAO에 deleteUser 메서드가 있다고 가정 (없으면 추가 필요)
            boolean success = userDAO.deleteUser(userId); 
            return success ? ResponseEntity.ok("삭제 성공") : ResponseEntity.status(400).body("삭제 실패");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}