package com.example.demo.repository;

import com.example.demo.model.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository // 스프링 빈으로 등록
public interface PointLogRepository extends JpaRepository<PointLog, Integer> {
    
    // 특정 유저의 포인트 로그를 최신순으로 가져오는 기능 추가 (선택 사항)
    List<PointLog> findByUserIdOrderByTimestampDesc(String userId);
}