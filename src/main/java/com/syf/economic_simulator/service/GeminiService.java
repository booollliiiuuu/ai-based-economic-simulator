package com.syf.economic_simulator.service;

import com.syf.economic_simulator.dto.AnalysisResponse;
import com.syf.economic_simulator.dto.AnalysisResponse.DigitalTwin;
import com.syf.economic_simulator.dto.AnalysisResponse.Scenario;
import com.syf.economic_simulator.dto.ApplicantRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final ChatClient chatClient;

    public GeminiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AnalysisResponse processApplicant(ApplicantRequest request) {

        DigitalTwin twin = generateTwin(request);

        List<Scenario> scenarios = generateScenarios(request);

        SimulationResult simResult = runSimulation(twin, scenarios);

        AnalysisResponse response = new AnalysisResponse();
        response.setApplicationId(request.getApplicationId());
        if (request.getApplicantDetails() != null) {
            response.setName(request.getApplicantDetails().getFullName());
        }

        response.setTwinProfile(twin);
        response.setScenarios(mapScenariosToSurvival(scenarios, simResult.getScenarioSurvivalRates()));
        response.setScore(simResult.getOverallScore());
        response.setStatus(simResult.getOverallScore() > 70 ? AnalysisResponse.Status.APPROVED : AnalysisResponse.Status.REJECTED);
        response.setAiNarrative(simResult.getNarrative());

        return response;
    }

    private Map<Scenario, Integer> mapScenariosToSurvival(List<Scenario> originalScenarios, List<ScenarioResult> aiResults) {
        Map<Scenario, Integer> resultMap = new HashMap<>();

        for (Scenario scenario : originalScenarios) {
            int survivalRate = -1;
            for (ScenarioResult res : aiResults) {
                if (res.getName().equalsIgnoreCase(scenario.getName())) {
                    survivalRate = res.getSurvivalRate();
                    break;
                }
            }
            resultMap.put(scenario, survivalRate);
        }

        return resultMap;
    }


    private DigitalTwin generateTwin(ApplicantRequest request) {
        BeanOutputConverter<DigitalTwin> converter = new BeanOutputConverter<>(DigitalTwin.class);
        String prompt = """
            You are a Credit Risk AI. Create a Behavioral Digital Twin.
            DATA: %s
            LOGIC:
            - liquidity_buffer: (Limit - Balance) / (Est. Monthly Spend).
            - spending_elasticity: High (0.85) if 'Transactor' & Renting. Low (0.2) if 'Revolver'.
            - income_volatility: Format "High (0.35)" / "Low (0.05)".
            - burn_rate: Est. Fixed Costs.
            - archetype: 2-word summary.
            Return JSON only.
            """.formatted(request.toString());
        return chatClient.prompt().user(prompt).call().entity(converter);
    }

    private List<Scenario> generateScenarios(ApplicantRequest request) {
        BeanOutputConverter<List<Scenario>> converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<Scenario>>() {});
        String prompt = """
            You are an Economic Risk Engine. Generate 5 unique stress scenarios.
            PROFILE: Job=%s, Sector=%s, City=%s
            RULES:
            1. 'name': Specific to sector.
            2. 'likelihood': 0.0-1.0.
            3. 'impact_type': 'income_cut', 'expense_hike', 'one_time_loss'.
            4. 'impact_value': e.g. 0.5.
            Return JSON list only.
            """.formatted(request.getApplicantDetails().getJobTitle(), request.getApplicantDetails().getSector(), request.getApplicantDetails().getCity());
        return chatClient.prompt().user(prompt).call().entity(converter);
    }

    private SimulationResult runSimulation(DigitalTwin twin, List<Scenario> scenarios) {
        BeanOutputConverter<SimulationResult> converter = new BeanOutputConverter<>(SimulationResult.class);

        String prompt = """
            You are a Stochastic Simulation Engine.
            
            INPUTS:
            1. Digital Twin: %s
            2. Scenarios: %s
            
            TASK:
            Simulate the user's financial survival for EACH scenario based on their Twin Profile (Liquidity/Elasticity).
            
            LOGIC:
            - If 'spending_elasticity' is High (>0.6), they survive 'expense_hike' better.
            - If 'liquidity_buffer' is Low (<2.0), they fail 'income_cut' scenarios > 2 months.
            
            OUTPUT:
            1. 'overallScore': 0-100 (Weighted average of survival).
            2. 'narrative': Brief summary.
            3. 'scenarioSurvivalRates': List matching input scenarios with 'survivalRate' (0-100).
            
            Return strictly JSON matching the schema.
            """.formatted(twin.toString(), scenarios.toString());

        return chatClient.prompt()
                .user(u -> u.text(prompt))
                .call()
                .entity(converter);
    }


    @Data
    public static class SimulationResult {
        private int overallScore;
        private String narrative;
        private List<ScenarioResult> scenarioSurvivalRates;
    }

    @Data
    public static class ScenarioResult {
        private String name;
        private int survivalRate;
    }
}