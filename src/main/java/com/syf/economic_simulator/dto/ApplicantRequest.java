package com.syf.economic_simulator.dto;

import lombok.Data;
import java.util.List;

@Data
public class ApplicantRequest {
    private String applicationId;
    private ApplicantDetails applicantDetails;
    private BureauData bureauData;

    @Data
    public static class ApplicantDetails {
        private String fullName;
        private int age;
        private String city;
        private String jobTitle;
        private String sector;
        private double statedMonthlyIncome;
        private String housingStatus;
        private String education;
    }

    @Data
    public static class BureauData {
        private int creditScore;
        private double totalRevolvingLimit;
        private double currentRevolvingBalance;
        private double totalMonthlyEmi;
        private int accountAgeYears;
        private String paymentHistoryType; // "Transactor" or "Revolver"
        private int inquiriesLast6Months;
    }
}