package com.phamdatte.warehouse.dto.response;

import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class UserPageResponse {
    private Integer pageId;
    private String pageCode;
    private String pageName;
    private String pageUrl;
    private String pageGroup;
    private String icon;
    private Integer displayOrder;
    private Boolean isMenu;
    private Boolean canView;
    private Boolean canCreate;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canApprove;
}
