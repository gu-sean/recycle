package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter @Setter
public class Category {

    @Id
    @Column(name = "CATEGORY_ID")
    private String categoryId;

    @Column(name = "CATEGORY_NAME", nullable = false, unique = true)
    private String categoryName;

    @Column(name = "REWARD_POINTS", nullable = false)
    private Integer rewardPoints;
}