package com.syf.economic_simulator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AnalysisResponse {

    @JsonProperty("application_id")
    private String applicationId;

    private String name;
    private int score;
    private String status;

    @JsonProperty("twin_profile")
    private DigitalTwin twinProfile;

    private List<Scenario> scenarios;

    @JsonProperty("ai_narrative")
    private String aiNarrative;

    @Data
    public static class DigitalTwin {
        @JsonProperty("liquidity_buffer")
        private double liquidityBuffer;

        @JsonProperty("spending_elasticity")
        private double spendingElasticity;

        @JsonProperty("burn_rate")
        private double burnRate;

        @JsonProperty("income_volatility")
        private String incomeVolatility;

        private String archetype;
    }

    @Data
    public static class Scenario {
        private String name;
        private double probability;

        @JsonProperty("survival_rate")
        private int survivalRate;
    }
}

