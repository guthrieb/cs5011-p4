package main;

import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
import org.encog.ml.bayesian.EventType;
import org.encog.ml.bayesian.query.enumerate.EnumerationQuery;

public class Main {

    public static final double PROB_EMAIL_DETECTED_BUSINESS = 0.9;
    public static final double PROB_OF_MISINFORMATION_ERROR = 0.03;
    public static final double PROB_OF_MAINTENANCE = 0.3;
    public static final double PROB_FIREWALL_DOWN_DURING_MAINTENANCE = 0.05;
    private static final double PROB_OF_OUT_OF_DATE_INFORMATION = 0.02;
    public static final double PROB_NORMAL_INVESTIGATION_RESULT = 0.8;
    public static final double PROB_LOGGING_ERROR = 0.3;

    public static void main(String[] args) {
        BayesianNetwork network = constructBN1();


    }

    public static BayesianNetwork constructBN1() {
        BayesianNetwork network = new BayesianNetwork();

        BayesianEvent emailDetected = network.createEvent("emailDetected");
        BayesianEvent emailTypeDetected = network.createEvent("emailTypeDetected");
        BayesianEvent emailMisinformationError = network.createEvent("emailMisinformationError");
        BayesianEvent emailRiskLevelAssessed = network.createEvent("emailRiskLevelAssessed");

        BayesianEvent maintenancePlanned = network.createEvent("maintenancePlanned");
        BayesianEvent firewallDown = network.createEvent("firewallDown");
        BayesianEvent maintenanceRiskLevel = network.createEvent("maintenanceRiskLevel");
        BayesianEvent maintenanceInfoOutOfDate = network.createEvent("maintenanceInfoOutOfDate");
        BayesianEvent outOfDateVulnerability = network.createEvent("outOfDateVulnerability");

        BayesianEvent holidayOrPoliticalDay = network.createEvent("holidayOrPoliticalDay");
        BayesianEvent highRiskDay = network.createEvent("highRiskDay");

        BayesianEvent highRiskLevel = network.createEvent("highRiskLevel");
        BayesianEvent alert = network.createEvent("alert");

        BayesianEvent investigationOccurs = network.createEvent("investigationOccurs");
        BayesianEvent investigationResult = network.createEvent("investigationResult");
        BayesianEvent loggingError = network.createEvent("loggingError");
        BayesianEvent loggingResult = network.createEvent("loggingResult");
        BayesianEvent logInformation = network.createEvent("logInformation");
        BayesianEvent logAnomalous = network.createEvent("logAnomalous");
        BayesianEvent logNormal = network.createEvent("logNormal");

        addEmailDependencies(network, emailDetected, emailTypeDetected, emailMisinformationError, emailRiskLevelAssessed);
        addMaintenanceDependencies(network, maintenancePlanned, firewallDown, maintenanceRiskLevel, maintenanceInfoOutOfDate, outOfDateVulnerability);
        addDayDependencies(network, holidayOrPoliticalDay, highRiskDay);

        addAlertDependencies(network, highRiskLevel, alert, emailRiskLevelAssessed, maintenanceRiskLevel, highRiskDay);
        addLoggingDependencies(network, alert, investigationOccurs, investigationResult, loggingError, loggingResult, logInformation, logAnomalous, logNormal);

        network.finalizeStructure();

        addEmailProbabilities(emailDetected, emailTypeDetected, emailMisinformationError, emailRiskLevelAssessed);
        addMaintenanceProbabilities(maintenancePlanned, firewallDown, maintenanceRiskLevel, maintenanceInfoOutOfDate, outOfDateVulnerability);
        addDayProbabilities(holidayOrPoliticalDay, highRiskDay);
        addLoggingProbabilities(investigationOccurs, investigationResult, loggingError, loggingResult,
                logInformation, logAnomalous, logNormal);

        addAlertProbabilities(highRiskLevel, alert, emailRiskLevelAssessed, maintenanceRiskLevel, highRiskDay);


        network.validate();

        //EXECUTE QUERY
        EnumerationQuery query = new EnumerationQuery(network);

//        query.defineEventType(loggingResult, EventType.Evidence);
        query.defineEventType(logInformation, EventType.Evidence);
        query.defineEventType(logAnomalous, EventType.Outcome);
        query.setEventValue(logInformation, true);
//        query.setEventValue(loggingResult, true);
        query.setEventValue(logAnomalous, true);

        query.execute();
        System.out.println(query.toString());


        return network;
    }

    private static void addLoggingProbabilities(BayesianEvent investigationOccurs,
                                                BayesianEvent investigationResult, BayesianEvent loggingError,
                                                BayesianEvent loggingResult, BayesianEvent logInformation,
                                                BayesianEvent logAnomalous, BayesianEvent logNormal) {
        investigationOccurs.getTable().addLine(1.0, true, true);
        investigationOccurs.getTable().addLine(0.0, true, false);

        investigationResult.getTable().addLine(PROB_NORMAL_INVESTIGATION_RESULT, true, true);
        investigationResult.getTable().addLine(0.5, true, false);

        loggingError.getTable().addLine(PROB_LOGGING_ERROR, true, true);
        loggingError.getTable().addLine(0.0, true, false);

        loggingResult.getTable().addLine(0.0, true, true, true);
        loggingResult.getTable().addLine(1.0, true, true, false);
        loggingResult.getTable().addLine(0.0, true, false, false);
        loggingResult.getTable().addLine(1.0, true, false, true);

        logInformation.getTable().addLine(1.0, true, true);
        logInformation.getTable().addLine(0.0, true, false);

        logAnomalous.getTable().addLine(1.0, true, true, false);
        logAnomalous.getTable().addLine(0.0, true, true, true);
        logAnomalous.getTable().addLine(0.0, true, false, true);
        logAnomalous.getTable().addLine(0.0, true, false, false);

        logNormal.getTable().addLine(1.0, true, true, true);
        logNormal.getTable().addLine(0.0, true, false, true);
        logNormal.getTable().addLine(0.0, true, true, false);
        logNormal.getTable().addLine(0.0, true, false, false);
    }

    private static void addLoggingDependencies(BayesianNetwork network, BayesianEvent alert, BayesianEvent investigationOccurs,
                                               BayesianEvent investigationResult, BayesianEvent loggingError,
                                               BayesianEvent loggingResult, BayesianEvent logInformation,
                                               BayesianEvent logAnomalous, BayesianEvent logNormal) {

        network.createDependency(alert, investigationOccurs);

        network.createDependency(investigationOccurs, investigationResult);
        network.createDependency(investigationOccurs, logInformation);
        network.createDependency(investigationOccurs, loggingError);

        network.createDependency(investigationResult, loggingResult);

        network.createDependency(loggingError, loggingResult);

        network.createDependency(logInformation, logAnomalous);
        network.createDependency(logInformation, logNormal);

        network.createDependency(loggingResult, logAnomalous);
        network.createDependency(loggingResult, logNormal);
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

    private static void addAlertDependencies(BayesianNetwork network, BayesianEvent highRiskLevel, BayesianEvent alert, BayesianEvent emailRiskLevelAssessed, BayesianEvent maintenanceRiskLevel, BayesianEvent highRiskDay) {
        network.createDependency(emailRiskLevelAssessed, highRiskLevel);
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
        network.createDependency(firewallDown, maintenanceRiskLevel);
        network.createDependency(maintenanceInfoOutOfDate, outOfDateVulnerability);
        network.createDependency(maintenancePlanned, outOfDateVulnerability);
        network.createDependency(outOfDateVulnerability, maintenanceRiskLevel);
    }

    private static void addEmailDependencies(BayesianNetwork network, BayesianEvent emailDetected, BayesianEvent emailTypeDetected, BayesianEvent emailMisinformationError, BayesianEvent emailRiskLevelAssessed) {
        network.createDependency(emailDetected, emailTypeDetected);
        network.createDependency(emailDetected, emailMisinformationError);

        network.createDependency(emailDetected, emailRiskLevelAssessed);
        network.createDependency(emailTypeDetected, emailRiskLevelAssessed);
        network.createDependency(emailMisinformationError, emailRiskLevelAssessed);
    }

    private static void addEmailProbabilities(BayesianEvent emailDetected, BayesianEvent emailTypeDetected, BayesianEvent emailMisinformationError, BayesianEvent emailRiskLevelAssessed) {
        emailDetected.getTable().addLine(PROB_EMAIL_DETECTED_BUSINESS, true);

        emailMisinformationError.getTable().addLine(PROB_OF_MISINFORMATION_ERROR, true, true);
        emailMisinformationError.getTable().addLine(1.0, false, false);

        emailTypeDetected.getTable().addLine(0.9, true, true);
        emailTypeDetected.getTable().addLine(0.5, true, false);

        emailRiskLevelAssessed.getTable().addLine(1.0, true, true, true, true);
        emailRiskLevelAssessed.getTable().addLine(0.0, true, true, true, false);
        emailRiskLevelAssessed.getTable().addLine(0.0, true, true, false, true);
        emailRiskLevelAssessed.getTable().addLine(1.0, true, true, false, false);
        emailRiskLevelAssessed.getTable().addLine(0.0, true, false, true, true);
        emailRiskLevelAssessed.getTable().addLine(0.0, true, false, true, false);
        emailRiskLevelAssessed.getTable().addLine(0.0, true, false, false, true);
        emailRiskLevelAssessed.getTable().addLine(0.0, true, false, false, false);
    }
}
