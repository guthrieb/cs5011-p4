package main;

import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
import org.encog.ml.bayesian.EventType;
import org.encog.ml.bayesian.query.enumerate.EnumerationQuery;

import static main.BN1.*;

public class Queries {
    public static EnumerationQuery probabilityEmailRiskCausedAlert(BayesianNetwork network, boolean positive) {
        BayesianEvent alert = network.getEvent(ALERT_LABEL);
        BayesianEvent detectedEmailRisk = network.getEvent(DETECTED_EMAIL_RISK_LEVEL_LABEL);

        EnumerationQuery query = performDoubleTargetQuery(network, alert, positive, detectedEmailRisk, true);

        return query;
    }

    public static EnumerationQuery probabilityMaintenanceRiskCausedAlert(BayesianNetwork network, boolean positive) {
        BayesianEvent alert = network.getEvent(ALERT_LABEL);
        BayesianEvent maintenanceRiskLevel = network.getEvent(MAINTENANCE_RISK_LEVEL_LABEL);

        EnumerationQuery query = performDoubleTargetQuery(network, alert, positive, maintenanceRiskLevel, true);

        return query;
    }

    private static EnumerationQuery performDoubleTargetQuery(BayesianNetwork network, BayesianEvent evidence, boolean evidenceValue,  BayesianEvent outcome, boolean outcomeValue) {
        EnumerationQuery query = new EnumerationQuery(network);
        query.defineEventType(evidence, EventType.Evidence);
        query.defineEventType(outcome, EventType.Outcome);

        query.setEventValue(evidence, evidenceValue);
        query.setEventValue(outcome, outcomeValue);
        query.execute();
        return query;
    }

    public static EnumerationQuery probabilityDayRiskCausedAlert(BayesianNetwork network, boolean positive) {
        BayesianEvent alert = network.getEvent(ALERT_LABEL);
        BayesianEvent dayRisk = network.getEvent(DAY_RISK_LABEL);

        EnumerationQuery query = performDoubleTargetQuery(network, alert, positive, dayRisk, true);

        return query;
    }

    private static void performPredictiveQuery(BayesianNetwork network2, BayesianNetwork network1, BayesianNetwork merge) {
        double n1q1 = query1(network1);
        double n2q1 = query1(network2);
        double nmq1 = query1(merge);

        System.out.println("\n\nNetwork 1: " +n1q1);
        System.out.println("Network 2: " + n2q1);
        System.out.println("Network 3: " + nmq1);
    }

    private static void performDiagnosticQuery(BayesianNetwork network2, BayesianNetwork network1, BayesianNetwork merge) {
        double n1q2 = query2(network1);
        double n2q2 = query2(network2);
        double nmq2 = query2(merge);

        System.out.println();
        System.out.println("Network 1: " +n1q2);
        System.out.println("Network 2: " + n2q2);
        System.out.println("Network 3: " + nmq2);
    }

    public static EnumerationQuery probabilityAlertLeadsToAnomalousLogging(BayesianNetwork network1, boolean positive) {
        BayesianEvent alert = network1.getEvent(ALERT_LABEL);
        BayesianEvent logAnomalous = network1.getEvent(LOG_ANOMALOUS_LABEL);

        EnumerationQuery query = performDoubleTargetQuery(network1, alert, positive, logAnomalous, true);

        return query;
    }

    public static EnumerationQuery probabilityAlertLeadsToNormalLogging(BayesianNetwork network, boolean positive) {
        BayesianEvent alert = network.getEvent(ALERT_LABEL);
        BayesianEvent logNormal = network.getEvent(LOG_NORMAL_LABEL);

        EnumerationQuery query = performDoubleTargetQuery(network, alert, positive, logNormal, true);

        return query;
    }

    public static double query1(BayesianNetwork network) {
        BayesianEvent evidence = network.getEvent(ALERT_LABEL);
        BayesianEvent outcome = network.getEvent(LOG_ANOMALOUS_LABEL);
        EnumerationQuery query = new EnumerationQuery(network);
        query.defineEventType(outcome, EventType.Outcome);
        query.defineEventType(evidence, EventType.Evidence);
        query.setEventValue(outcome, true);
        query.setEventValue(evidence, true);
        query.execute();
        return query.getProbability();
    }

    public static double query2(BayesianNetwork network) {
        BayesianEvent evidence = network.getEvent(DETECTED_EMAIL_RISK_LEVEL_LABEL);
        BayesianEvent outcome = network.getEvent(BUSINESS_EMAIL_DETECTED_LABEL);
        EnumerationQuery query = new EnumerationQuery(network);
        query.defineEventType(outcome, EventType.Outcome);
        query.defineEventType(evidence, EventType.Evidence);
        query.setEventValue(outcome, true);
        query.setEventValue(evidence, true);
        query.execute();
        return query.getProbability();
    }
}
