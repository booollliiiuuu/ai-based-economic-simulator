package com.syf.economic_simulator.controller;

import com.syf.economic_simulator.dto.ApplicantRequest;
import com.syf.economic_simulator.dto.AnalysisResponse;
import com.syf.economic_simulator.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {

    private final GeminiService GeminiService;

    public SimulationController(GeminiService GeminiService) {
        this.GeminiService = GeminiService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody ApplicantRequest request) {
        return ResponseEntity.ok(GeminiService.processApplicant(request));
    }
}