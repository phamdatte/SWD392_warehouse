package com.phamdatte.warehouse.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class RolePageId implements Serializable {
    private Integer role;
    private Integer page;
}
