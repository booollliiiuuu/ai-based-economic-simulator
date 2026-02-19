package com.syf.economic_simulator.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ApplicantRequest {
    @JsonProperty("application_id")
    private String applicationId;

    @JsonProperty("applicant_details")
    private ApplicantDetails applicantDetails;

    @JsonProperty("bureau_data")
    private BureauData bureauData;

    @Data
    public static class ApplicantDetails {
        private String name;
        private int age;
        @JsonProperty("city_tier")
        private String cityTier;
        @JsonProperty("job_title")
        private String jobTitle;
        @JsonProperty("stated_monthly_income")
        private double statedMonthlyIncome;
        @JsonProperty("housing_status")
        private String housingStatus;
        private String education;
    }

    @Data
    public static class BureauData {
        @JsonProperty("credit_score")
        private int creditScore;
        @JsonProperty("total_revolving_limit")
        private double totalRevolvingLimit;
        @JsonProperty("current_revolving_balance")
        private double currentRevolvingBalance;
        @JsonProperty("utilization_ratio")
        private double utilizationRatio;
        @JsonProperty("total_monthly_emi_obligations")
        private double totalMonthlyEmiObligations;
        @JsonProperty("payment_history")
        private PaymentHistory paymentHistory;
        @JsonProperty("account_age_years")
        private int accountAgeYears;
    }

    @Data
    public static class PaymentHistory {
        @JsonProperty("avg_payment_amount")
        private double avgPaymentAmount;
        @JsonProperty("payment_type")
        private String paymentType;
    }
}
