package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.PointLogRepository;
import com.example.demo.repository.UserRepository;
import db.DAO.UserDAO;
import db.DTO.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user")
public class UserController {

    // 1. 기존 DAO 방식 (로그인/회원가입 등 커스텀 SQL용)
    private UserDAO userDAO = new UserDAO();

    // 2. 새로운 JPA 방식 (랭킹 조회, 대시보드 통계용)
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointLogRepository pointLogRepository;

    // --- 회원가입 API ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        String userId = userData.get("userId");
        String password = userData.get("password");
        String nickname = userData.get("nickname");

        try {
            boolean isSuccess = userDAO.registerUser(userId, password, nickname);
            if (isSuccess) return ResponseEntity.ok("성공");
            else return ResponseEntity.status(400).body("실패");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // --- 로그인 API ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String userId = loginData.get("userId");
        String password = loginData.get("password");

        try {
            UserDTO user = userDAO.loginUser(userId, password);
            if (user != null) return ResponseEntity.ok(user);
            else return ResponseEntity.status(401).body("아이디 또는 비밀번호가 틀립니다.");
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("서버 오류");
        }
    }

    // --- 🏆 랭킹 API ---
    @GetMapping("/ranking")
    public ResponseEntity<?> getTopRankings() {
        try {
            List<User> allUsers = userRepository.findAll();
            
            if (allUsers == null || allUsers.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            // 💡 JPA 엔티티의 필드명에 맞게 getBalancePoints() 또는 getTotalPoints() 사용
            List<Map<String, Object>> rankings = allUsers.stream()
                .sorted((u1, u2) -> {
                    // 랭킹은 보통 누적 포인트(Total) 기준으로 합니다.
                    int p1 = (u1.getTotalPoints() != null) ? u1.getTotalPoints() : 0;
                    int p2 = (u2.getTotalPoints() != null) ? u2.getTotalPoints() : 0;
                    return Integer.compare(p2, p1);
                })
                .limit(10)
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    String nickname = u.getNickname();
                    if (nickname == null || nickname.trim().isEmpty()) {
                        nickname = u.getUserId();
                    }
                    
                    int userPoints = (u.getTotalPoints() != null) ? u.getTotalPoints() : 0;
                    
                    map.put("nickname", nickname);
                    map.put("points", userPoints);
                    map.put("grade", calculateGrade(userPoints));
                    return map;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(rankings);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // --- 사용자 정보 조회 API (마이페이지용) ---
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestParam String userId) {
        try {
            // DAO를 통해 최신 데이터 가져오기
            UserDTO user = userDAO.getUserById(userId); 
            
            if (user != null) {
                System.out.println("DEBUG >>> 유저 정보 조회: " + userId);
                System.out.println("DEBUG >>> Total Points: " + user.getTotalPoints());
                System.out.println("DEBUG >>> Balance Points: " + user.getBalancePoints());
                
                Map<String, Object> response = new HashMap<>();
                response.put("userId", user.getUserId());
                response.put("nickname", user.getNickname());
                response.put("balancePoints", user.getBalancePoints());
                response.put("totalPoints", user.getTotalPoints()); 

                // 등급 계산
                String grade = calculateGrade(user.getTotalPoints());
                response.put("userGrade", grade);

                // 환경 기여도 계산
                int count = userDAO.getEnvironmentContribution(userId);
                response.put("contribution", count);
                
                // CO2 절감량 및 나무 심기 효과
                response.put("co2Saved", String.format("%.2f", count * 0.12));
                response.put("treesPlanted", String.format("%.1f", (count * 0.12) / 0.5));

                // 포인트 내역 로드
                List<Map<String, Object>> history = userDAO.getRecentPointLogs(userId);
                response.put("pointHistory", history != null ? history : new ArrayList<>());

                System.out.println("DEBUG >>> 최종 응답 데이터: " + response);

                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.status(404).body("유저를 찾을 수 없습니다.");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 에러: " + e.getMessage());
        }
    }

    // --- 통계 대시보드 API ---
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            long totalHeroes = userRepository.count();
            long totalDisposals = pointLogRepository.count();
            double totalCarbon = totalDisposals * 0.12;

            stats.put("totalCarbon", String.format("%.1f", totalCarbon));
            stats.put("totalDisposals", totalDisposals);
            stats.put("totalHeroes", totalHeroes);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/addPoints")
    public ResponseEntity<?> addPoints(@RequestBody Map<String, Object> payload) {
        try {
            String userId = (String) payload.get("userId");
            Integer pointsToAdd = Integer.parseInt(payload.get("points").toString());

            User user = userRepository.findByUserId(userId);
            
            if (user != null) {
                // 💡 엔티티에 맞게 포인트 업데이트
                int currentBalance = (user.getBalancePoints() != null) ? user.getBalancePoints() : 0;
                int currentTotal = (user.getTotalPoints() != null) ? user.getTotalPoints() : 0;
                
                user.setBalancePoints(currentBalance + pointsToAdd);
                user.setTotalPoints(currentTotal + pointsToAdd);
                
                User updatedUser = userRepository.save(user);
                return ResponseEntity.ok(updatedUser); 
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러: " + e.getMessage());
        }
    }
    // --- 비밀번호 재확인 API ---
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String password = request.get("password");
        
        try {
            boolean isValid = userDAO.checkPassword(userId, password);
            if (isValid) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 틀립니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("DB 오류");
        }
    }

    // --- 닉네임 중복 확인 API ---
    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam("nickname") String nickname) {
        try {
            if (nickname == null || nickname.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("닉네임을 입력해주세요.");
            }
            boolean isAvailable = userDAO.isNicknameAvailable(nickname.trim());
            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류: " + e.getMessage());
        }
    }

    // --- 포인트 내역 로드 API ---
    @GetMapping("/points/history")
    public ResponseEntity<?> getPointHistory(@RequestParam String userId) {
        try {
            List<Map<String, Object>> history = userDAO.getRecentPointLogs(userId);
            return ResponseEntity.ok(history != null ? history : new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("내역 로드 실패: " + e.getMessage());
        }
    }

    // --- 회원 정보 수정 API ---
    @PostMapping("/update")
    public ResponseEntity<?> updateUserInfo(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String nickname = params.get("nickname");
        String password = params.get("password");

        System.out.println("Update Request -> ID: " + userId + ", Nick: " + nickname);

        boolean success = userDAO.updateUser(userId, nickname, password);
        
        if (success) {
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(400).body("DB 업데이트에 실패했습니다. (ID 일치 확인 필요)");
        }
    }

    // --- 회원 탈퇴 API ---
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdrawUser(@RequestParam String userId) {
        try {
            boolean success = userDAO.deleteUser(userId);
            return success ? ResponseEntity.ok("success") : ResponseEntity.badRequest().body("fail");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // --- 등급 계산 로직 ---
    private String calculateGrade(int pts) {
        if (pts >= 10000) return "전설의 숲 🌳";
        if (pts >= 5000) return "푸른 나무 🌲";
        if (pts >= 2000) return "파릇한 새싹 🌱";
        return "희망의 씨앗 🌰";
    }
}