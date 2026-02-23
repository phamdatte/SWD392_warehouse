package com.phamdatte.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "page")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pageId;

    @Column(nullable = false, unique = true, length = 50)
    private String pageCode;

    @Column(nullable = false, length = 100)
    private String pageName;

    @Column(nullable = false, length = 200)
    private String pageUrl;

    @Column(length = 50)
    private String pageGroup;

    @Column(length = 100)
    private String icon;

    @Column
    private Integer displayOrder = 0;

    @Column
    private Boolean isMenu = true;

    @Column
    private Boolean isActive = true;
}
