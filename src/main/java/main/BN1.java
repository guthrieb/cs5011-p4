package main;

import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
import org.encog.ml.bayesian.EventType;
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

    public static void main(String[] args) {
        BayesianNetwork network = constructBN1();
        BayesianEvent emailDetected = network.getEvent("logAnomalous");
        EnumerationQuery query = new EnumerationQuery(network);
        query.defineEventType(emailDetected, EventType.Outcome);
        query.setEventValue(emailDetected, true);

        query.execute();
        System.out.println(query.getProbability());
    }

    public static BayesianNetwork constructBN1() {
        BayesianNetwork network = new BayesianNetwork();

        BayesianEvent emailDetected = network.createEvent("emailDetected");
        BayesianEvent businessEmailDetected = network.createEvent("businessEmailDetected");
        BayesianEvent personalEmailDetected = network.createEvent("personalEmailDetected");
        BayesianEvent emailMisinformationError = network.createEvent("emailMisinformationError");
        BayesianEvent actualEmailRiskLevel = network.createEvent("actualEmailRiskLevel");
        BayesianEvent detectedEmailRiskLevel = network.createEvent("detectedEmailRiskLevel");

        BayesianEvent maintenancePlanned = network.createEvent("maintenancePlanned");
        BayesianEvent firewallDown = network.createEvent("firewallDown");
        BayesianEvent maintenanceRiskLevel = network.createEvent("maintenanceRiskLevel");
        BayesianEvent maintenanceInfoOutOfDate = network.createEvent("maintenanceInfoOutOfDate");
        BayesianEvent outOfDateVulnerability = network.createEvent("outOfDateVulnerability");

        BayesianEvent holidayOrPoliticalDay = network.createEvent("holidayOrPoliticalDay");
        BayesianEvent highRiskDay = network.createEvent("highRiskDay");

        BayesianEvent highRiskLevel = network.createEvent("highRiskLevel");
        BayesianEvent alert = network.createEvent("alert");

        BayesianEvent investigation = network.createEvent("investigation");
        BayesianEvent loggingError = network.createEvent("loggingError");
        BayesianEvent anomalousRuling = network.createEvent("anomalousRuling");
        BayesianEvent normalRuling = network.createEvent("normalRuling");
        BayesianEvent logAnomalous = network.createEvent("logAnomalous");
        BayesianEvent logNormal = network.createEvent("logNormal");

        addEmailDependencies(network, emailDetected, businessEmailDetected, personalEmailDetected, emailMisinformationError, actualEmailRiskLevel, detectedEmailRiskLevel);
        addMaintenanceDependencies(network, maintenancePlanned, firewallDown, maintenanceRiskLevel, maintenanceInfoOutOfDate, outOfDateVulnerability);
        addDayDependencies(network, holidayOrPoliticalDay, highRiskDay);

        addAlertDependencies(network, highRiskLevel, alert, detectedEmailRiskLevel, maintenanceRiskLevel, highRiskDay);
        addLoggingDependencies(network, alert, investigation, loggingError, anomalousRuling, normalRuling, logAnomalous, logNormal);

        network.finalizeStructure();

        addEmailProbabilities(emailDetected, businessEmailDetected, personalEmailDetected, emailMisinformationError,
                actualEmailRiskLevel, detectedEmailRiskLevel);
        addMaintenanceProbabilities(maintenancePlanned, firewallDown, maintenanceRiskLevel, maintenanceInfoOutOfDate, outOfDateVulnerability);
        addDayProbabilities(holidayOrPoliticalDay, highRiskDay);
        addLoggingProbabilities(investigation, loggingError, anomalousRuling, normalRuling, logAnomalous, logNormal);

        addAlertProbabilities(highRiskLevel, alert, detectedEmailRiskLevel, maintenanceRiskLevel, highRiskDay);


        network.validate();
        return network;
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
        holidayOrPoliticalDay.getTable().addLine(100.0/360.0, true);
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

    private static void addEmailDependencies(BayesianNetwork network, BayesianEvent emailDetected, BayesianEvent businessEmailDetected, BayesianEvent personalEmailDetected, BayesianEvent emailMisinformationError, BayesianEvent actualEmailRiskLevel, BayesianEvent detectedEmailRiskLevel) {
        network.createDependency(emailDetected, businessEmailDetected);
        network.createDependency(emailDetected, personalEmailDetected);
        network.createDependency(emailDetected, emailMisinformationError);

        network.createDependency(businessEmailDetected, personalEmailDetected);
        network.createDependency(personalEmailDetected, actualEmailRiskLevel);

        network.createDependency(actualEmailRiskLevel, detectedEmailRiskLevel);

        network.createDependency(emailMisinformationError, detectedEmailRiskLevel);
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
