package com.agentica.api.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventStatsResponse(

    String tenantId,

    long pendingCount,

    long processingCount,

    long actionableCount,

    long filteredCount,

    long completedCount,

    long failedCount

) {

    public long totalCount() {
        return pendingCount + processingCount + actionableCount + filteredCount + completedCount + failedCount;
    }

}
