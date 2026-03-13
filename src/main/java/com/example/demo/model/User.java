package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users") 
public class User {

    @Id
    @Column(name = "USER_ID")
    private String userId;

    // 현재 보유 포인트 (사용 가능한 포인트)
    @Column(name = "BALANCE_POINTS") 
    private Integer balancePoints; // 필드명을 더 명확하게 변경 추천
    
    // ✅ [추가] 누적 포인트 필드 매핑
    @Column(name = "TOTAL_POINTS")
    private Integer totalPoints;

    // --- Getter/Setter ---
    
    // 기존 getPoints()는 balancePoints를 반환하거나 이름을 변경해야 합니다.
    public Integer getBalancePoints() {                
        return balancePoints;
    }

    public void setBalancePoints(Integer balancePoints) {
        this.balancePoints = balancePoints;
    }

    // ✅ [추가] TOTAL_POINTS Getter/Setter
    public Integer getTotalPoints() {                
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // 💡 닉네임 필드도 DB에 있다면 추가하고 구현해야 합니다.
    @Column(name = "NICKNAME")
    private String nickname;

    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}