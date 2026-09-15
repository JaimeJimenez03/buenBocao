package com.buenbocao.api.moderation.dto.response;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ModerationStatsResponse {

    private long pendingReports;
    private long reviewingReports;
    private long resolvedReports;
    private long dismissedReports;
    private long totalReports;
    private long bannedUsers;
}
