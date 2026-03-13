package com.example.demo.repository;
import com.example.demo.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
    // 아이템 이름으로 검색하는 기능 추가
    java.util.List<Item> findByItemNameContaining(String itemName);
}