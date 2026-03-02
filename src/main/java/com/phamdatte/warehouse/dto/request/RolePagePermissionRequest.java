package com.phamdatte.warehouse.dto.request;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RolePagePermissionRequest {

    private List<PagePermItem> pages;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PagePermItem {
        private Integer pageId;
        private Boolean canView;
        private Boolean canCreate;
        private Boolean canEdit;
        private Boolean canDelete;
        private Boolean canApprove;
    }
}
