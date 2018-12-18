import merge.NetworkMerger;
import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
import org.encog.ml.bayesian.EventType;
import org.encog.ml.bayesian.query.enumerate.EnumerationQuery;
import org.junit.Assert;
import org.junit.Test;

public class NetworkMergerTest {
    public static final double TOLERANCE = 0.01;

    @Test
    public void mergedCPTTest() {
        BayesianNetwork network1 = new BayesianNetwork();

        BayesianEvent a = network1.createEvent("a");
        BayesianEvent c1 = network1.createEvent("c");

        BayesianNetwork network2 = new BayesianNetwork();
        BayesianEvent b = network2.createEvent("b");
        BayesianEvent c2 = network2.createEvent("c");

        network1.createDependency(a, c1);
        network1.createDependency(b, c2);

        network1.finalizeStructure();
        network2.finalizeStructure();

        a.getTable().addLine(0.1, true);
        c1.getTable().addLine(0.8, true, true);
        c1.getTable().addLine(0.6, true, false);

        b.getTable().addLine(0.6, true);
        c2.getTable().addLine(0.3, true, true);
        c2.getTable().addLine(0.6, true, false);

        network1.validate();
        network2.validate();

        NetworkMerger merger = new NetworkMerger();
        BayesianNetwork merge = merger.merge(network1, network2);

        EnumerationQuery query = new EnumerationQuery(merge);
        BayesianEvent aRes = merge.getEvent("a");
        BayesianEvent bRes = merge.getEvent("b");
        BayesianEvent cRes = merge.getEvent("c");


        query.defineEventType(aRes, EventType.Outcome);
        checkProbabilityCorrect(0.1, TOLERANCE, aRes, query);


        query = new EnumerationQuery(merge);
        query.defineEventType(bRes, EventType.Outcome);
        checkProbabilityCorrect(0.6, TOLERANCE, bRes, query);


        query = new EnumerationQuery(merge);
        query.defineEventType(cRes, EventType.Outcome);
        query.defineEventType(bRes, EventType.Evidence);
        query.defineEventType(aRes, EventType.Evidence);
        query.setEventValue(bRes, true);
        query.setEventValue(aRes, true);

        checkProbabilityCorrect(0.53, TOLERANCE, cRes, query);


        query.setEventValue(aRes, true);
        query.setEventValue(bRes, false);

        checkProbabilityCorrect(0.64, TOLERANCE, cRes, query);

        query.setEventValue(aRes, false);
        query.setEventValue(bRes, true);

        checkProbabilityCorrect(0.47, TOLERANCE, cRes, query);

        query.setEventValue(aRes, false);
        query.setEventValue(bRes, false);

        checkProbabilityCorrect(0.57, TOLERANCE, cRes, query);
    }

    @Test
    public void checkLinearAugmentation() {
        BayesianNetwork network1 = new BayesianNetwork();

        BayesianEvent a = network1.createEvent("a");
        BayesianEvent b1 = network1.createEvent("b");

        BayesianNetwork network2 = new BayesianNetwork();
        BayesianEvent b2 = network2.createEvent("b");
        BayesianEvent c = network2.createEvent("c");

        network1.createDependency(a, b1);
        network1.createDependency(b2, c);

        network1.finalizeStructure();
        network2.finalizeStructure();

        a.getTable().addLine(0.1, true);
        b1.getTable().addLine(0.8, true, true);
        b1.getTable().addLine(0.6, true, false);

        b2.getTable().addLine(0.6, true);
        b2.getTable().addLine(0.3, true);
        c.getTable().addLine(0.2, true, false);
        c.getTable().addLine(0.4, true, true);

        network1.validate();
        network2.validate();

        NetworkMerger merger = new NetworkMerger();

        BayesianNetwork merge = merger.merge(network1, network2);

        BayesianEvent aRes = merge.getEvent("a");
        BayesianEvent bRes = merge.getEvent("b");
        BayesianEvent cRes = merge.getEvent("c");

        EnumerationQuery query = new EnumerationQuery(merge);
        query.defineEventType(aRes, EventType.Outcome);
        checkProbabilityCorrect(0.1, TOLERANCE, aRes, query);

        query = new EnumerationQuery(merge);
        query.defineEventType(aRes, EventType.Evidence);
        query.defineEventType(bRes, EventType.Outcome);
        query.setEventValue(aRes, true);
        checkProbabilityCorrect(0.8, TOLERANCE, bRes, query);

        query = new EnumerationQuery(merge);
        query.defineEventType(aRes, EventType.Evidence);
        query.defineEventType(bRes, EventType.Outcome);
        query.setEventValue(aRes, false);
        checkProbabilityCorrect(0.6, TOLERANCE, bRes, query);

        query = new EnumerationQuery(merge);
        query.defineEventType(bRes, EventType.Evidence);
        query.defineEventType(cRes, EventType.Outcome);
        query.setEventValue(bRes, true);
        checkProbabilityCorrect(0.4, TOLERANCE, cRes, query);

        query = new EnumerationQuery(merge);
        query.defineEventType(bRes, EventType.Evidence);
        query.defineEventType(cRes, EventType.Outcome);
        query.setEventValue(bRes, false);
        checkProbabilityCorrect(0.2, TOLERANCE, cRes, query);
    }


    private void checkProbabilityCorrect(double expectedTrueOutput, double delta, BayesianEvent target, EnumerationQuery query) {
        query.setEventValue(target, true);
        query.execute();
        Assert.assertEquals(expectedTrueOutput, query.getProbability(), delta);
        query.setEventValue(target, false);
        query.execute();
        Assert.assertEquals(1 - expectedTrueOutput, query.getProbability(), delta);
    }
}
