package com.pathshalapro.dto.fee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeGroupRequest {
    private String name;
    private String description;
    private String grade;
    private List<FeeGroupItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeGroupItemRequest {
        private Long feeHeadId;
        private BigDecimal amount;
    }
}
