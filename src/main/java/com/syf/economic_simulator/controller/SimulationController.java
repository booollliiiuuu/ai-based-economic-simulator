package com.syf.economic_simulator.controller;

import com.syf.economic_simulator.dto.ApplicantRequest;
import com.syf.economic_simulator.dto.AnalysisResponse;
import com.syf.economic_simulator.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {

    private final GeminiService geminiService;

    public SimulationController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<List<AnalysisResponse>> analyze(
            @RequestBody ApplicantRequest request) {

        List<AnalysisResponse> responses =
                geminiService.processApplicant(request);

        return ResponseEntity.ok(responses);
    }
}
