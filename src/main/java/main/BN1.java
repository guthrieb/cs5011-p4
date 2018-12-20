package main;

import merge.NetworkMerger;
import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
import org.encog.ml.bayesian.query.enumerate.EnumerationQuery;

public class BN1 {

    public static final double PROB_EMAIL_DETECTED = 0.99;
    public static final double PROB_OF_MISINFORMATION_ERROR = 0.03;
    public static final double PROB_OF_MAINTENANCE = 0.2;
    public static final double PROB_FIREWALL_DOWN_DURING_MAINTENANCE = 0.05;
    private static final double PROB_OF_OUT_OF_DATE_INFORMATION = 0.02;
    public static final double PROB_ANOMALOUS_INVESTIGATION_RESULT = 0.1;
    public static final double PROB_LOGGING_ERROR = 0.3;
    private static final double PROB_BUSINESS_EMAIL_DETECTED = 0.9;
    public static final String DAY_RISK_LABEL = "highRiskDay";
    public static final String HOLIDAY_OR_POLITICAL_DAY_LABEL = "holidayOrPoliticalDay";
    public static final String OUT_OF_DATE_VULNERABILITY_LABEL = "outOfDateVulnerability";
    public static final String MAINTENANCE_INFO_OUT_OF_DATE_LABEL = "maintenanceInfoOutOfDate";
    public static final String MAINTENANCE_RISK_LEVEL_LABEL = "maintenanceRiskLevel";
    public static final String FIREWALL_DOWN_LABEL = "firewallDown";
    public static final String MAINTENANCE_PLANNED_LABEL = "maintenancePlanned";
    public static final String DETECTED_EMAIL_RISK_LEVEL_LABEL = "detectedEmailRiskLevel";
    public static final String ACTUAL_EMAIL_RISK_LEVEL_LABEL = "actualEmailRiskLevel";
    public static final String EMAIL_MISINFORMATION_ERROR_LABEL = "emailMisinformationError";
    public static final String PERSONAL_EMAIL_DETECTED_LABEL = "personalEmailDetected";
    public static final String BUSINESS_EMAIL_DETECTED_LABEL = "businessEmailDetected";
    public static final String EMAIL_DETECTED_LABEL = "emailDetected";
    public static final String OVERALL_RISK_LEVEL_LABEL = "highRiskLevel";
    public static final String ALERT_LABEL = "alert";
    public static final String INVESTIGATION_LABEL = "investigation";
    public static final String LOGGING_ERROR_LABEL = "loggingError";
    public static final String ANOMALOUS_RULING_LABEL = "anomalousRuling";
    public static final String NORMAL_RULING_LABEL = "normalRuling";
    public static final String LOG_ANOMALOUS_LABEL = "logAnomalous";
    public static final String LOG_NORMAL_LABEL = "logNormal";
    public static final String BUREAUCRATIC_FAILURE_LABEL = "bureaucraticFailure";
    public static final String FLAGGED_FORBIDDEN_LABEL = "flaggedForbidden";

    public static void main(String[] args) {
        BayesianNetwork network2 = constructBN1(true);
        BayesianNetwork network1 = constructBN1(false);
//
        NetworkMerger merger = new NetworkMerger();
        BayesianNetwork merge = merger.merge(network1, network2);

//        performPredictiveQuery(network2, network1, merge);

//        performDiagnosticQuery(network2, network1, merge);

        observeRiskAlertRelationships(network2, network1, true);
        observeRiskAlertRelationships(network2, network1, false);
        observeAlertLoggingRelationships(network2, network1, true);
        observeAlertLoggingRelationships(network2, network1, false);

    }


    private static void observeRiskAlertRelationships(BayesianNetwork network2, BayesianNetwork network1, boolean positive) {
        System.out.println("\nPROFILING QUERY");
        EnumerationQuery n1q3p1 = Queries.probabilityEmailRiskCausedAlert(network1, positive);
        EnumerationQuery n2q3p1 = Queries.probabilityEmailRiskCausedAlert(network2,positive);

        System.out.println();
        System.out.println( n1q3p1);
        System.out.println( n2q3p1);

        EnumerationQuery n1q3p2 = Queries.probabilityDayRiskCausedAlert(network1, positive);
        EnumerationQuery n2q3p2 = Queries.probabilityDayRiskCausedAlert(network2, positive);

        System.out.println();
        System.out.println( n1q3p2);
        System.out.println(n2q3p2);

        EnumerationQuery n1q3p3 = Queries.probabilityMaintenanceRiskCausedAlert(network1, positive);
        EnumerationQuery n2q3p3 = Queries.probabilityMaintenanceRiskCausedAlert(network2, positive);

        System.out.println();
        System.out.println(n1q3p3);
        System.out.println(n2q3p3);
    }

    private static void observeAlertLoggingRelationships(BayesianNetwork network2, BayesianNetwork network1, boolean positive) {
        System.out.println("\nPROFILING QUERY");

        EnumerationQuery n1q3p4 = Queries.probabilityAlertLeadsToAnomalousLogging(network1, positive);
        EnumerationQuery n2q3p4 = Queries.probabilityAlertLeadsToAnomalousLogging(network2, positive);

        System.out.println();
        System.out.println(n1q3p4);
        System.out.println(n2q3p4);

        EnumerationQuery n1q3p5 = Queries.probabilityAlertLeadsToNormalLogging(network1, positive);
        EnumerationQuery n2q3p5 = Queries.probabilityAlertLeadsToNormalLogging(network2, positive);

        System.out.println();
        System.out.println(n1q3p5);
        System.out.println(n2q3p5);
    }


    public static BayesianNetwork constructBN1(boolean network2) {
        BayesianNetwork network = new BayesianNetwork();

        BayesianEvent emailDetected = network.createEvent(EMAIL_DETECTED_LABEL);
        BayesianEvent businessEmailDetected = network.createEvent(BUSINESS_EMAIL_DETECTED_LABEL);
        BayesianEvent personalEmailDetected = network.createEvent(PERSONAL_EMAIL_DETECTED_LABEL);
        BayesianEvent emailMisinformationError = network.createEvent(EMAIL_MISINFORMATION_ERROR_LABEL);
        BayesianEvent actualEmailRiskLevel = network.createEvent(ACTUAL_EMAIL_RISK_LEVEL_LABEL);
        BayesianEvent detectedEmailRiskLevel = network.createEvent(DETECTED_EMAIL_RISK_LEVEL_LABEL);

        BayesianEvent maintenancePlanned = network.createEvent(MAINTENANCE_PLANNED_LABEL);
        BayesianEvent firewallDown = network.createEvent(FIREWALL_DOWN_LABEL);
        BayesianEvent maintenanceRiskLevel = network.createEvent(MAINTENANCE_RISK_LEVEL_LABEL);
        BayesianEvent maintenanceInfoOutOfDate = network.createEvent(MAINTENANCE_INFO_OUT_OF_DATE_LABEL);
        BayesianEvent outOfDateVulnerability = network.createEvent(OUT_OF_DATE_VULNERABILITY_LABEL);

        BayesianEvent holidayOrPoliticalDay = network.createEvent(HOLIDAY_OR_POLITICAL_DAY_LABEL);
        BayesianEvent highRiskDay = network.createEvent(DAY_RISK_LABEL);

        BayesianEvent highRiskLevel = network.createEvent(OVERALL_RISK_LEVEL_LABEL);
        BayesianEvent alert = network.createEvent(ALERT_LABEL);

        BayesianEvent investigation = network.createEvent(INVESTIGATION_LABEL);
        BayesianEvent loggingError = network.createEvent(LOGGING_ERROR_LABEL);
        BayesianEvent anomalousRuling = network.createEvent(ANOMALOUS_RULING_LABEL);
        BayesianEvent normalRuling = network.createEvent(NORMAL_RULING_LABEL);
        BayesianEvent logAnomalous = network.createEvent(LOG_ANOMALOUS_LABEL);
        BayesianEvent logNormal = network.createEvent(LOG_NORMAL_LABEL);

        BayesianEvent bureaucraticFailure = null;
        BayesianEvent flaggedForbidden = null;
        if (network2) {
            bureaucraticFailure = network.createEvent(BUREAUCRATIC_FAILURE_LABEL);
            flaggedForbidden = network.createEvent(FLAGGED_FORBIDDEN_LABEL);
        }

        if (flaggedForbidden != null) {
            addEmailDependencies(network, emailDetected, businessEmailDetected, personalEmailDetected, emailMisinformationError, actualEmailRiskLevel, detectedEmailRiskLevel, flaggedForbidden);
        } else {
            addEmailDependencies(network, emailDetected, businessEmailDetected, personalEmailDetected, emailMisinformationError, actualEmailRiskLevel, detectedEmailRiskLevel);
        }
        addMaintenanceDependencies(network, maintenancePlanned, firewallDown, maintenanceRiskLevel, maintenanceInfoOutOfDate, outOfDateVulnerability);
        addDayDependencies(network, holidayOrPoliticalDay, highRiskDay);

        addAlertDependencies(network, highRiskLevel, alert, detectedEmailRiskLevel, maintenanceRiskLevel, highRiskDay);

        if (bureaucraticFailure != null) {
            addLoggingDependencies(network, alert, investigation, loggingError, anomalousRuling, normalRuling, logAnomalous, logNormal, bureaucraticFailure);
        } else {
            addLoggingDependencies(network, alert, investigation, loggingError, anomalousRuling, normalRuling, logAnomalous, logNormal);
        }


        network.finalizeStructure();

        if (flaggedForbidden != null) {
            addEmailProbabilities(emailDetected, businessEmailDetected, personalEmailDetected, emailMisinformationError,
                    actualEmailRiskLevel, detectedEmailRiskLevel, flaggedForbidden);
        } else {
            addEmailProbabilities(emailDetected, businessEmailDetected, personalEmailDetected, emailMisinformationError,
                    actualEmailRiskLevel, detectedEmailRiskLevel);
        }
        addMaintenanceProbabilities(maintenancePlanned, firewallDown, maintenanceRiskLevel, maintenanceInfoOutOfDate,
                outOfDateVulnerability);
        addDayProbabilities(holidayOrPoliticalDay, highRiskDay);

        if (bureaucraticFailure != null) {
            addLoggingProbabilities(investigation, loggingError, anomalousRuling, normalRuling, logAnomalous, logNormal,
                    bureaucraticFailure);
        } else {
            addLoggingProbabilities(investigation, loggingError, anomalousRuling, normalRuling, logAnomalous, logNormal);
        }

        addAlertProbabilities(highRiskLevel, alert, detectedEmailRiskLevel, maintenanceRiskLevel, highRiskDay);


        network.validate();
        return network;
    }


    private static void addLoggingProbabilities(BayesianEvent investigation, BayesianEvent loggingError,
                                                BayesianEvent anomalousRuling, BayesianEvent normalRuling,
                                                BayesianEvent logAnomalous, BayesianEvent logNormal,
                                                BayesianEvent bureaucraticFailure) {

        investigation.getTable().addLine(0.0, true, true, true);
        investigation.getTable().addLine(1.0, true, true, false);
        investigation.getTable().addLine(0.0, true, false, true);
        investigation.getTable().addLine(0.0, true, false, false);

        bureaucraticFailure.getTable().addLine(0.01, true, true);
        bureaucraticFailure.getTable().addLine(0.00, true, false);

        loggingError.getTable().addLine(PROB_LOGGING_ERROR, true, true);
        loggingError.getTable().addLine(0.0, true, false);

        anomalousRuling.getTable().addLine(PROB_ANOMALOUS_INVESTIGATION_RESULT, true, true);
        anomalousRuling.getTable().addLine(0, true, false);

        normalRuling.getTable().addLine(0.0, true, true, true);
        normalRuling.getTable().addLine(1.0, true, true, false);
        normalRuling.getTable().addLine(0.0, true, false, true);
        normalRuling.getTable().addLine(0.0, true, false, false);

        logAnomalous.getTable().addLine(0.0, true, true, true);
        logAnomalous.getTable().addLine(1.0, true, true, false);
        logAnomalous.getTable().addLine(1.0, true, false, true);
        logAnomalous.getTable().addLine(0.0, true, false, false);

        logNormal.getTable().addLine(0.0, true, true, true);
        logNormal.getTable().addLine(1.0, true, true, false);
        logNormal.getTable().addLine(1.0, true, false, true);
        logNormal.getTable().addLine(0.0, true, false, false);

    }

    private static void addLoggingDependencies(BayesianNetwork network, BayesianEvent alert, BayesianEvent investigation,
                                               BayesianEvent loggingError, BayesianEvent anomalousRuling,
                                               BayesianEvent normalRuling, BayesianEvent logAnomalous,
                                               BayesianEvent logNormal, BayesianEvent bureaucraticFailure) {
        addLoggingDependencies(network, alert, investigation, loggingError, anomalousRuling, normalRuling, logAnomalous,
                logNormal);

        network.createDependency(alert, bureaucraticFailure);
        network.createDependency(bureaucraticFailure, investigation);
    }

    private static void addEmailProbabilities(BayesianEvent emailDetected, BayesianEvent businessEmailDetected,
                                              BayesianEvent personalEmailDetected, BayesianEvent emailMisinformationError,
                                              BayesianEvent actualEmailRiskLevel, BayesianEvent detectedEmailRiskLevel,
                                              BayesianEvent flaggedForbidden) {
        emailDetected.getTable().addLine(PROB_EMAIL_DETECTED, true);

        businessEmailDetected.getTable().addLine(PROB_BUSINESS_EMAIL_DETECTED, true, true);
        businessEmailDetected.getTable().addLine(0.0, true, false);

        personalEmailDetected.getTable().addLine(0.0, true, true, true);
        personalEmailDetected.getTable().addLine(1.0, true, true, false);
        personalEmailDetected.getTable().addLine(0.0, true, false, true);
        personalEmailDetected.getTable().addLine(0.0, true, false, false);

        actualEmailRiskLevel.getTable().addLine(1.0, true, true, true);
        actualEmailRiskLevel.getTable().addLine(1.0, true, true, false);
        actualEmailRiskLevel.getTable().addLine(1.0, true, false, true);
        actualEmailRiskLevel.getTable().addLine(0.0, true, false, false);

        emailMisinformationError.getTable().addLine(PROB_OF_MISINFORMATION_ERROR, true, true);
        emailMisinformationError.getTable().addLine(0.0, true, false);

        detectedEmailRiskLevel.getTable().addLine(0.0, true, true, true);
        detectedEmailRiskLevel.getTable().addLine(1.0, true, true, false);
        detectedEmailRiskLevel.getTable().addLine(1.0, true, false, true);
        detectedEmailRiskLevel.getTable().addLine(0.0, true, false, false);

        flaggedForbidden.getTable().addLine(0.05, true, true);
        flaggedForbidden.getTable().addLine(0.0, true, false);
    }

    private static void addLoggingProbabilities(BayesianEvent investigation, BayesianEvent loggingError, BayesianEvent anomalousRuling, BayesianEvent normalRuling, BayesianEvent logAnomalous, BayesianEvent logNormal) {
        investigation.getTable().addLine(1.0, true, true);
        investigation.getTable().addLine(0.0, true, false);

        loggingError.getTable().addLine(PROB_LOGGING_ERROR, true, true);
        loggingError.getTable().addLine(0.0, true, false);

        anomalousRuling.getTable().addLine(PROB_ANOMALOUS_INVESTIGATION_RESULT, true, true);
        anomalousRuling.getTable().addLine(0, true, false);

        normalRuling.getTable().addLine(0.0, true, true, true);
        normalRuling.getTable().addLine(1.0, true, true, false);
        normalRuling.getTable().addLine(0.0, true, false, true);
        normalRuling.getTable().addLine(0.0, true, false, false);

        logAnomalous.getTable().addLine(0.0, true, true, true);
        logAnomalous.getTable().addLine(1.0, true, true, false);
        logAnomalous.getTable().addLine(1.0, true, false, true);
        logAnomalous.getTable().addLine(0.0, true, false, false);

        logNormal.getTable().addLine(0.0, true, true, true);
        logNormal.getTable().addLine(1.0, true, true, false);
        logNormal.getTable().addLine(1.0, true, false, true);
        logNormal.getTable().addLine(0.0, true, false, false);


    }

    private static void addLoggingDependencies(BayesianNetwork network, BayesianEvent alert, BayesianEvent investigation, BayesianEvent loggingError, BayesianEvent anomalousRuling, BayesianEvent normalRuling, BayesianEvent logAnomalous, BayesianEvent logNormal) {

        network.createDependency(alert, investigation);

        network.createDependency(investigation, loggingError);
        network.createDependency(investigation, anomalousRuling);
        network.createDependency(investigation, normalRuling);

        network.createDependency(anomalousRuling, normalRuling);

        network.createDependency(loggingError, logAnomalous);
        network.createDependency(loggingError, logNormal);

        network.createDependency(anomalousRuling, logAnomalous);
        network.createDependency(normalRuling, logNormal);


    }

    private static void addAlertProbabilities(BayesianEvent highRiskLevel, BayesianEvent alert, BayesianEvent emailRiskLevelAssessed, BayesianEvent maintenanceRiskLevel, BayesianEvent highRiskDay) {
        highRiskLevel.getTable().addLine(1.0, true, true, true, true);
        highRiskLevel.getTable().addLine(1.0, true, true, false, true);
        highRiskLevel.getTable().addLine(1.0, true, true, false, false);
        highRiskLevel.getTable().addLine(1.0, true, true, true, false);
        highRiskLevel.getTable().addLine(1.0, true, false, true, true);
        highRiskLevel.getTable().addLine(1.0, true, false, true, false);
        highRiskLevel.getTable().addLine(1.0, true, false, false, true);
        highRiskLevel.getTable().addLine(0.0, true, false, false, false);

        alert.getTable().addLine(0.8, true, true);
        alert.getTable().addLine(0.0, true, false);
    }

    private static void addAlertDependencies(BayesianNetwork network, BayesianEvent highRiskLevel, BayesianEvent alert, BayesianEvent detectedEmailRiskLevelAssessed, BayesianEvent maintenanceRiskLevel, BayesianEvent highRiskDay) {
        network.createDependency(detectedEmailRiskLevelAssessed, highRiskLevel);
        network.createDependency(highRiskDay, highRiskLevel);
        network.createDependency(maintenanceRiskLevel, highRiskLevel);

        network.createDependency(highRiskLevel, alert);
    }

    private static void addDayProbabilities(BayesianEvent holidayOrPoliticalDay, BayesianEvent highRiskDay) {
        holidayOrPoliticalDay.getTable().addLine(100.0 / 360.0, true);
        highRiskDay.getTable().addLine(1.0, true, true);
        highRiskDay.getTable().addLine(0.0, true, false);
    }

    private static void addDayDependencies(BayesianNetwork network, BayesianEvent holidayOrPoliticalDay, BayesianEvent highRiskDay) {
        network.createDependency(holidayOrPoliticalDay, highRiskDay);
    }

    private static void addMaintenanceProbabilities(BayesianEvent maintenancePlanned, BayesianEvent firewallDown, BayesianEvent maintenanceRiskLevel, BayesianEvent maintenanceInfoOutOfDate, BayesianEvent outOfDateVulnerability) {
        maintenancePlanned.getTable().addLine(PROB_OF_MAINTENANCE, true);

        firewallDown.getTable().addLine(PROB_FIREWALL_DOWN_DURING_MAINTENANCE, true, true);
        firewallDown.getTable().addLine(0.0, true, false);

        maintenanceInfoOutOfDate.getTable().addLine(PROB_OF_OUT_OF_DATE_INFORMATION, true);

        outOfDateVulnerability.getTable().addLine(1.0, true, true, true);
        outOfDateVulnerability.getTable().addLine(0.0, true, true, false);
        outOfDateVulnerability.getTable().addLine(0.0, true, false, true);
        outOfDateVulnerability.getTable().addLine(0.0, true, false, false);

        maintenanceRiskLevel.getTable().addLine(1.0, true, true, true);
        maintenanceRiskLevel.getTable().addLine(1.0, true, true, false);
        maintenanceRiskLevel.getTable().addLine(1.0, true, false, true);
        maintenanceRiskLevel.getTable().addLine(0.0, true, false, false);
    }

    private static void addMaintenanceDependencies(BayesianNetwork network, BayesianEvent maintenancePlanned, BayesianEvent firewallDown, BayesianEvent maintenanceRiskLevel, BayesianEvent maintenanceInfoOutOfDate, BayesianEvent outOfDateVulnerability) {
        network.createDependency(maintenancePlanned, firewallDown);
        network.createDependency(maintenancePlanned, outOfDateVulnerability);

        network.createDependency(firewallDown, maintenanceRiskLevel);

        network.createDependency(maintenanceInfoOutOfDate, outOfDateVulnerability);


        network.createDependency(outOfDateVulnerability, maintenanceRiskLevel);
    }

    private static void addEmailDependencies(BayesianNetwork network, BayesianEvent emailDetected,
                                             BayesianEvent businessEmailDetected, BayesianEvent personalEmailDetected,
                                             BayesianEvent emailMisinformationError, BayesianEvent actualEmailRiskLevel,
                                             BayesianEvent detectedEmailRiskLevel) {
        network.createDependency(emailDetected, businessEmailDetected);
        network.createDependency(emailDetected, personalEmailDetected);
        network.createDependency(emailDetected, emailMisinformationError);

        network.createDependency(businessEmailDetected, personalEmailDetected);
        network.createDependency(personalEmailDetected, actualEmailRiskLevel);

        network.createDependency(actualEmailRiskLevel, detectedEmailRiskLevel);

        network.createDependency(emailMisinformationError, detectedEmailRiskLevel);
    }

    private static void addEmailDependencies(BayesianNetwork network, BayesianEvent emailDetected,
                                             BayesianEvent businessEmailDetected, BayesianEvent personalEmailDetected,
                                             BayesianEvent emailMisinformationError, BayesianEvent actualEmailRiskLevel,
                                             BayesianEvent detectedEmailRiskLevel, BayesianEvent flaggedForbidden) {
        addEmailDependencies(network, emailDetected, businessEmailDetected, personalEmailDetected,
                emailMisinformationError, actualEmailRiskLevel, detectedEmailRiskLevel);

        network.createDependency(flaggedForbidden, actualEmailRiskLevel);
        network.createDependency(businessEmailDetected, flaggedForbidden);
    }

    private static void addEmailProbabilities(BayesianEvent emailDetected, BayesianEvent businessEmailDetected, BayesianEvent personalEmailDetected, BayesianEvent emailMisinformationError, BayesianEvent actualEmailRiskLevel, BayesianEvent detectedEmailRiskLevel) {
        emailDetected.getTable().addLine(PROB_EMAIL_DETECTED, true);

        businessEmailDetected.getTable().addLine(PROB_BUSINESS_EMAIL_DETECTED, true, true);
        businessEmailDetected.getTable().addLine(0.0, true, false);

        personalEmailDetected.getTable().addLine(0.0, true, true, true);
        personalEmailDetected.getTable().addLine(1.0, true, true, false);
        personalEmailDetected.getTable().addLine(0.0, true, false, true);
        personalEmailDetected.getTable().addLine(0.0, true, false, false);

        actualEmailRiskLevel.getTable().addLine(1.0, true, true);
        actualEmailRiskLevel.getTable().addLine(0.0, true, false);

        emailMisinformationError.getTable().addLine(PROB_OF_MISINFORMATION_ERROR, true, true);
        emailMisinformationError.getTable().addLine(0.0, true, false);

        detectedEmailRiskLevel.getTable().addLine(0.0, true, true, true);
        detectedEmailRiskLevel.getTable().addLine(1.0, true, true, false);
        detectedEmailRiskLevel.getTable().addLine(1.0, true, false, true);
        detectedEmailRiskLevel.getTable().addLine(0.0, true, false, false);
    }
}
