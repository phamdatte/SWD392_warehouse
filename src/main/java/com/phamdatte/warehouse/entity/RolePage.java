package com.phamdatte.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_page")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(RolePageId.class)
public class RolePage {

    @Id
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Id
    @ManyToOne
    @JoinColumn(name = "page_id")
    private Page page;

    @Column
    private Boolean canView = true;

    @Column
    private Boolean canCreate = false;

    @Column
    private Boolean canEdit = false;

    @Column
    private Boolean canDelete = false;

    @Column
    private Boolean canApprove = false;
}
