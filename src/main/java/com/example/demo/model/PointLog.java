package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_logs")
@Getter @Setter
@NoArgsConstructor // 👈 기본 생성자가 있어야 JPA와 'new PointLog()'가 작동합니다.
public class PointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_ID")
    private Integer logId;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "DETAIL")
    private String detail;

    @Column(name = "POINTS")
    private Integer points;

    @Column(name = "TIMESTAMP")
    private LocalDateTime timestamp = LocalDateTime.now();

    // 💡 오류 방지를 위해 명시적 생성자 추가
    public PointLog(String userId, String type, String detail, Integer points) {
        this.userId = userId;
        this.type = type;
        this.detail = detail;
        this.points = points;
        this.timestamp = LocalDateTime.now();
    }
}