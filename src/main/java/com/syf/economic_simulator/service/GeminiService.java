package com.syf.economic_simulator.service;

import com.syf.economic_simulator.dto.AnalysisResponse;
import com.syf.economic_simulator.dto.AnalysisResponse.DigitalTwin;
import com.syf.economic_simulator.dto.AnalysisResponse.Scenario;
import com.syf.economic_simulator.dto.ApplicantRequest;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    private final ChatClient chatClient;
    private final RestTemplate restTemplate;


    public GeminiService(ChatClient.Builder chatClientBuilder,RestTemplate restTemplate) {
        this.chatClient = chatClientBuilder.build();
        this.restTemplate = restTemplate;
    }

    // ✅ NOW RETURNS LIST
    public List<AnalysisResponse> processApplicant(ApplicantRequest request) {

        // 1️⃣ Generate Digital Twin
        DigitalTwin twin = generateTwin(request);

        // 2️⃣ Generate Scenarios
        List<Scenario> scenarios = generateScenarios(request);

        // 3️⃣ Run Simulation
        SimulationResult simResult = runSimulation(twin, scenarios);

        // 4️⃣ Build RAG Response
        AnalysisResponse ragResponse = new AnalysisResponse();
        ragResponse.setApplicationId(request.getApplicationId());

        if (request.getApplicantDetails() != null) {
            ragResponse.setName(request.getApplicantDetails().getName());
        }

        ragResponse.setTwinProfile(twin);

        ragResponse.setScenarios(
                mapScenariosToSurvival(
                        scenarios,
                        simResult != null
                                ? simResult.getScenarioSurvivalRates()
                                : Collections.emptyList()
                )
        );

        int score = simResult != null ? simResult.getOverallScore() : 0;
        ragResponse.setScore(score);
        ragResponse.setStatus(score > 70 ? "APPROVED" : "REJECTED");

        ragResponse.setAiNarrative(
                simResult != null
                        ? simResult.getNarrative()
                        : "Simulation unavailable."
        );

        // 🔥 For now returning only RAG inside list
        // Later you will add Monte Carlo response here
        // 5️⃣ Call Monte Carlo (Python API)
        AnalysisResponse monteCarloResponse = callMonteCarloService(request);

// 6️⃣ Return BOTH
        return List.of(ragResponse, monteCarloResponse);

    }
    private AnalysisResponse callMonteCarloService(ApplicantRequest request) {

        String pythonUrl = "http://localhost:8000/analyze";

        try {
            return restTemplate.postForObject(
                    pythonUrl,
                    request,
                    AnalysisResponse.class
            );
        } catch (Exception e) {

            // fallback if python fails
            AnalysisResponse fallback = new AnalysisResponse();
            fallback.setApplicationId(request.getApplicationId());
            fallback.setName(request.getApplicantDetails().getName());
            fallback.setScore(0);
            fallback.setStatus("REJECTED");
            fallback.setAiNarrative("Monte Carlo service unavailable.");

            return fallback;
        }
    }

    // ✅ FIXED: NOW RETURNS List<Scenario>
    private List<Scenario> mapScenariosToSurvival(
            List<Scenario> originalScenarios,
            List<ScenarioResult> aiResults) {

        if (originalScenarios == null) {
            return Collections.emptyList();
        }

        Map<String, Integer> survivalMap = new HashMap<>();

        if (aiResults != null) {
            survivalMap = aiResults.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            r -> r.getName().toLowerCase(),
                            ScenarioResult::getSurvivalRate,
                            (a, b) -> a
                    ));
        }

        for (Scenario scenario : originalScenarios) {

            int survivalRate = survivalMap.getOrDefault(
                    scenario.getName().toLowerCase(),
                    -1
            );

            scenario.setSurvivalRate(survivalRate);
        }

        return originalScenarios;
    }

    private DigitalTwin generateTwin(ApplicantRequest request) {

        BeanOutputConverter<DigitalTwin> converter =
                new BeanOutputConverter<>(DigitalTwin.class);

        String prompt = """
            You are a Credit Risk AI. Create a Behavioral Digital Twin.
            DATA: %s
            Return JSON only.
            """.formatted(request.toString());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }

    private List<Scenario> generateScenarios(ApplicantRequest request) {

        BeanOutputConverter<List<Scenario>> converter =
                new BeanOutputConverter<>(
                        new ParameterizedTypeReference<List<Scenario>>() {}
                );

        String prompt = """
            You are an Economic Risk Engine. Generate 5 unique stress scenarios.
            PROFILE: Job=%s, CityTier=%s, Housing=%s
            Return JSON list only.
            """.formatted(
                request.getApplicantDetails().getJobTitle(),
                request.getApplicantDetails().getCityTier(),
                request.getApplicantDetails().getHousingStatus()
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }

    private SimulationResult runSimulation(DigitalTwin twin,
                                           List<Scenario> scenarios) {

        BeanOutputConverter<SimulationResult> converter =
                new BeanOutputConverter<>(SimulationResult.class);

        String prompt = """
            You are a Stochastic Simulation Engine.
            INPUTS:
            Digital Twin: %s
            Scenarios: %s
            
            OUTPUT:
            - overallScore (0-100)
            - narrative
            - scenarioSurvivalRates (with survivalRate 0-100)
            
            Return strict JSON.
            """.formatted(twin.toString(), scenarios.toString());

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(converter);
    }

    // ---------------------------
    // Simulation Result DTOs
    // ---------------------------

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
