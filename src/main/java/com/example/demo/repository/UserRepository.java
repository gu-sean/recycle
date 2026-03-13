package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// 엔티티(User)와 @Id의 타입(Long)을 정확히 지정
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 유저 아이디로 조회 (String)
    User findByUserId(String userId); 
}