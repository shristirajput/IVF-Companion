package com.ivf.companion.service;

import com.ivf.companion.dto.RecommendationRequest;
import com.ivf.companion.dto.RecommendationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    public RecommendationResponse generateRecommendation(RecommendationRequest request) {
        String method = "Standard IVF";
        String likelihood = "Moderate";
        double successRate = 40.0;
        
        List<String> rationale = new ArrayList<>();
        List<String> wellness = new ArrayList<>();
        List<String> meds = new ArrayList<>();

        int age = request.getAge();
        double amh = request.getAmhLevel();
        double fsh = request.getFshLevel();
        int yearsInfertility = request.getYearsInfertility();
        int prevCycles = request.getPrevCycles();
        String dx = request.getDiagnosis().toUpperCase();

        // 1. Core Clinical Rules for Treatment Path
        if (dx.equals("MALE_FACTOR")) {
            method = "Intracytoplasmic Sperm Injection (ICSI)";
            rationale.add("Diagnosed Male Factor infertility. ICSI bypasses sperm penetration barriers by injecting a single high-quality sperm directly into the mature oocyte.");
            successRate = 50.0;
            meds.add("Gonal-F (Follitropin Alfa)");
            meds.add("Ovidrel (Trigger Shot)");
        } else if (age >= 40) {
            if (amh < 0.8) {
                method = "Donor Egg IVF";
                likelihood = "Moderate";
                successRate = 55.0;
                rationale.add("Patient age >= 40 combined with Diminished Ovarian Reserve (AMH < 0.8 ng/mL). Donor oocytes provide a significantly higher likelihood of healthy blastocyst development and live birth.");
                meds.add("Estrogen Patches (Luteal support)");
                meds.add("Progesterone in Oil injections");
            } else {
                method = "IVF with PGT-A (Genetic Screening)";
                likelihood = "Guarded";
                successRate = 30.0;
                rationale.add("Advanced Maternal Age (40+). Preimplantation Genetic Testing for Aneuploidies (PGT-A) is highly recommended to screen for chromosomal abnormalities prior to embryo transfer.");
                meds.add("Menopur");
                meds.add("Cetrotide (Ganirelix)");
                meds.add("Progesterone inserts");
            }
        } else if (amh < 1.0 || fsh > 10.0) {
            method = "IVF with Microdose Flare Protocol";
            likelihood = "Guarded";
            successRate = 35.0;
            rationale.add("Hormonal markers suggest diminished ovarian reserve (AMH < 1.0 ng/mL or FSH > 10.0 mIU/mL). A microdose flare protocol utilizes endogenous LH release to aggressively stimulate follicle recruitment.");
            meds.add("Microdose Lupron");
            meds.add("Gonal-F (High dose)");
            meds.add("Menopur");
        } else if (age < 35 && amh >= 2.0 && yearsInfertility <= 2 && prevCycles == 0) {
            method = "Standard IVF / Antagonist Protocol";
            likelihood = "High";
            successRate = 60.0;
            rationale.add("Favorable biological profile: patient age < 35 and robust ovarian reserve (AMH >= 2.0). A standard antagonist protocol provides excellent yield with reduced risk of Ovarian Hyperstimulation Syndrome (OHSS).");
            meds.add("Gonal-F");
            meds.add("Cetrotide");
            meds.add("Lupron Trigger");
        } else {
            method = "Standard IVF";
            likelihood = "Moderate";
            successRate = 42.0;
            rationale.add("Standard antagonist protocol recommended. Ovarian reserve and age parameters indicate an average follicle response probability.");
            meds.add("Gonal-F");
            meds.add("Menopur");
            meds.add("Progesterone support");
        }

        // 2. Adjust Success Rates and Likelihood based on other clinical factors
        if (prevCycles >= 2) {
            successRate -= (prevCycles * 4.0);
            rationale.add(String.format("History of %d previous unsuccessful IVF cycles slightly lowers current statistical success rate. Protocol fine-tuning (e.g., changing trigger type or adding co-culture) should be considered.", prevCycles));
            if (likelihood.equals("High")) likelihood = "Moderate";
            else if (likelihood.equals("Moderate")) likelihood = "Guarded";
        }

        if (yearsInfertility >= 5) {
            successRate -= 3.0;
            rationale.add("Longer duration of infertility (>5 years) correlates with slightly reduced implantation efficiency.");
        }

        // Clip success rate boundaries
        if (successRate < 10.0) successRate = 12.0;
        if (successRate > 75.0) successRate = 72.0;

        // 3. Populate Emotional Wellness Advice
        wellness.add("Practice the 4-7-8 deep breathing technique during daily hormone injection cycles to manage cortisol spikes.");
        wellness.add("Join the IVF Companion Community Forum for peer support, particularly during the challenging 'Two-Week Wait' phase.");
        wellness.add("Maintain moderate light physical activity (e.g., walking 30 minutes daily). Avoid high-intensity exercises during ovarian stimulation.");
        wellness.add("Prioritize 7-8 hours of sleep per night to maximize natural melatonin production, which promotes healthy oocyte environments.");

        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendedProtocol(method);
        response.setSuccessLikelihood(likelihood);
        response.setConfidenceScore(Math.round(successRate * 10.0) / 10.0);
        response.setRationalePoints(rationale);
        response.setWellnessTips(wellness);
        response.setRecommendedMeds(meds);
        return response;
    }
}
