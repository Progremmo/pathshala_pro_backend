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
public class FeeGroupResponse {
    private Long id;
    private String name;
    private String description;
    private String grade;
    private List<FeeGroupItemResponse> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeGroupItemResponse {
        private Long id;
        private Long feeHeadId;
        private String feeHeadName;
        private BigDecimal amount;
    }
}
