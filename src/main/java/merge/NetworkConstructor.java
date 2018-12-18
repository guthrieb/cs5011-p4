package merge;

import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
import org.encog.ml.bayesian.table.BayesianTable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class NetworkConstructor {
    public BayesianNetwork construct(Collection<String> labels, List<Dependency> dependencies, HashMap<String, List<Probability>> probabilities)  {
        BayesianNetwork network = new BayesianNetwork();
        for(String label : labels) {
            network.createEvent(label);
        }

        for(Dependency dependency : dependencies) {
            network.createDependency(dependency.parent, dependency.dependant);
        }
        network.finalizeStructure();


        for(String label : labels) {
            List<Probability> probabilityList = probabilities.get(label);

            BayesianEvent event = network.getEvent(label);
            List<BayesianEvent> parents = event.getParents();

            BayesianTable table = event.getTable();
            for(Probability probability : probabilityList) {
                boolean[] args = new boolean[probability.arguments.length];
                for(int i = 0; i < probability.arguments.length; i++) {
                    int correctIndex = probability.inputs.indexOf(parents.get(i).getLabel());
                    args[correctIndex] = probability.arguments[i];
                }

                table.addLine(probability.probability, probability.result, args);
            }
        }

        network.validate();

        return network;
    }
}
