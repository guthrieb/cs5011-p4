package merge;

import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
import org.encog.ml.bayesian.EventType;
import org.encog.ml.bayesian.query.enumerate.EnumerationQuery;
import org.encog.ml.bayesian.table.BayesianTable;
import org.encog.ml.bayesian.table.TableLine;

import java.util.*;

public class NetworkMerger {
    @SuppressWarnings("Duplicates")
    public BayesianNetwork merge(BayesianNetwork a, BayesianNetwork b) {
        Set<String> aLabels = getLabels(a);
        Set<String> bLabels = getLabels(b);
        Set<String> allLabels = new HashSet<>(aLabels);
        allLabels.addAll(bLabels);

        Set<String> intersection = getIntersection(aLabels, bLabels);
        Set<String> notIntersection = getSubtraction(intersection, allLabels);

        Set<String> zi = getZi(intersection, a, b);
        Set<String> ze = getSubtraction(zi, intersection);

        HashMap<String, List<Dependency>> aDependencies = getDependencies(a);
        HashMap<String, List<Dependency>> bDependencies = getDependencies(b);
        HashMap<String, List<Probability>> aProbabilities = getProbabilities(a);
        HashMap<String, List<Probability>> bProbabilities = getProbabilities(b);

        System.out.println(aProbabilities);

        HashMap<String, List<Dependency>> newDependencies = new HashMap<>();
        HashMap<String, List<Probability>> newProbabilityTables = new HashMap<>();

        addQ(notIntersection, aDependencies, aProbabilities, bDependencies, bProbabilities, newDependencies, newProbabilityTables);
        addZi(zi, intersection, aDependencies, aProbabilities, bDependencies, bProbabilities, newDependencies, newProbabilityTables);

        addZe(ze, aDependencies, aProbabilities, bDependencies, bProbabilities, newDependencies, newProbabilityTables);

        System.out.println(newProbabilityTables);
        NetworkConstructor constructor = new NetworkConstructor();
        List<Dependency> dependencyList = new ArrayList<>();
        for(List<Dependency> dependencies : newDependencies.values()) {
            if(dependencies != null) {
                dependencyList.addAll(dependencies);
            }
        }

        BayesianNetwork construct = constructor.construct(allLabels, dependencyList, newProbabilityTables);

        return construct;
    }

    private void addZe(Set<String> ze,
                       HashMap<String, List<Dependency>> aDependencies, HashMap<String, List<Probability>> aProbabilities,
                       HashMap<String, List<Dependency>> bDependencies, HashMap<String, List<Probability>> bProbabilities,
                       HashMap<String, List<Dependency>> newDependencies, HashMap<String, List<Probability>> newProbabilityTables) {

        for (String label : ze) {
            List<Dependency> aLabelDependencies = aDependencies.get(label);
            List<Dependency> bLabelDependencies = bDependencies.get(label);
            List<Probability> aLabelProbabilities = aProbabilities.get(label);
            List<Probability> bLabelProbabilities = bProbabilities.get(label);

            Set<Dependency> combinedDependencies = new HashSet<>();
            combinedDependencies.addAll(aLabelDependencies);
            combinedDependencies.addAll(bLabelDependencies);

            List<Probability> combinedProbabilities = new ArrayList<>();

            for (Probability aProbability : aLabelProbabilities) {
                for (Probability bProbability : bLabelProbabilities) {
                    if (aProbability.result == bProbability.result) {
                        combinedProbabilities.add(Probability.combineProbability(aProbability, bProbability));
                    }
                }
            }

            HashMap<String, Double> sums = new HashMap<>();
            for (Probability probability : combinedProbabilities) {
                String convertToString = convertToString(probability.arguments);
                double sum = 0;
                if(sums.containsKey(convertToString)) {

                    sum = sums.get(convertToString);
                }

                sum += probability.probability;
                sums.put(convertToString, sum);
            }

            for (Probability probability : combinedProbabilities) {
                double sum = sums.get(convertToString(probability.arguments));

                probability.probability /= sum;
            }

            newDependencies.put(label, new ArrayList<>(combinedDependencies));
            newProbabilityTables.put(label, combinedProbabilities);
        }
    }

    private String convertToString(boolean[] arguments) {
        StringBuilder res = new StringBuilder();
        for(boolean argument : arguments) {
            res.append(Boolean.toString(argument));
        }
        return res.toString();
    }

    private void addZi(Set<String> zi, Set<String> z,
                       HashMap<String, List<Dependency>> aDependencies, HashMap<String, List<Probability>> aProbabilities,
                       HashMap<String, List<Dependency>> bDependencies, HashMap<String, List<Probability>> bProbabilities,
                       HashMap<String, List<Dependency>> newDependencies, HashMap<String, List<Probability>> newProbabilityTables) {

        for (String label : zi) {
            List<Dependency> aLabelDependencies = aDependencies.get(label);
            List<Dependency> bLabelDependencies = bDependencies.get(label);

            List<Probability> aLabelProbabilities = aProbabilities.get(label);
            List<Probability> bLabelProbabilities = bProbabilities.get(label);

            if (notSubsetOf(aLabelDependencies, z)) {
                newDependencies.put(label, bLabelDependencies);
                newProbabilityTables.put(label, bLabelProbabilities);
            } else if (notSubsetOf(bLabelDependencies, z)) {
                newDependencies.put(label, aLabelDependencies);
                newProbabilityTables.put(label, aLabelProbabilities);
            } else {
                if (aLabelDependencies.size() > bLabelDependencies.size()) {
                    newDependencies.put(label, aLabelDependencies);
                    newProbabilityTables.put(label, aLabelProbabilities);
                } else {
                    newDependencies.put(label, bLabelDependencies);
                    newProbabilityTables.put(label, bLabelProbabilities);
                }
            }
        }
    }

    private boolean notSubsetOf(List<Dependency> aLabelDependencies, Set<String> z) {
        for (Dependency dependency : aLabelDependencies) {
            String parent = dependency.parent;
            if (!z.contains(parent)) {
                return true;
            }
        }
        return false;
    }

    private void addQ(Set<String> notIntersection, HashMap<String,
            List<Dependency>> aDependencies, HashMap<String, List<Probability>> aProbabilities,
                      HashMap<String, List<Dependency>> bDependencies, HashMap<String, List<Probability>> bProbabilities,
                      HashMap<String, List<Dependency>> newDependencies, HashMap<String, List<Probability>> newProbabilityTables) {

        for (String label : notIntersection) {
            System.out.println(aDependencies.containsKey(label));
            List<Dependency> dependencies;
            List<Probability> probabilities;
            if (aDependencies.containsKey(label)) {
                dependencies = aDependencies.get(label);
                probabilities = aProbabilities.get(label);
            } else {
                dependencies = bDependencies.get(label);
                probabilities = bProbabilities.get(label);
            }

            newDependencies.put(label, dependencies);
            newProbabilityTables.put(label, probabilities);
        }
    }

    private HashMap<String, List<Probability>> getProbabilities(BayesianNetwork a) {
        HashMap<String, List<Probability>> probabilities = new HashMap<>();

        for (BayesianEvent event : a.getEvents()) {
            BayesianTable table = event.getTable();
            for (TableLine line : table.getLines()) {

                Probability probability = new Probability(line);

                List<Probability> probabilitiesList;
                if (probabilities.containsKey(event.getLabel())) {
                    probabilitiesList = probabilities.get(event.getLabel());
                } else {
                    probabilitiesList = new ArrayList<>();
                }

                probabilitiesList.add(probability);
                probabilities.put(event.getLabel(), probabilitiesList);

            }
        }

        return probabilities;
    }

    private Set<String> getZi(Set<String> intersection, BayesianNetwork a, BayesianNetwork b) {
        Set<String> zi = new HashSet<String>();

        for (String label : intersection) {


            BayesianEvent aEvent = a.getEvent(label);
            List<BayesianEvent> aParents = aEvent.getParents();

            boolean inner = true;
            for (BayesianEvent parent : aParents) {
                if (!intersection.contains(parent.getLabel())) {
                    inner = false;
                }
            }

            if (inner) {
                zi.add(label);
                break;
            }

            BayesianEvent bEvent = b.getEvent(label);
            List<BayesianEvent> bParents = bEvent.getParents();
            inner = true;
            for (BayesianEvent parent : bParents) {
                if (!intersection.contains(parent.getLabel())) {
                    inner = false;
                }
            }

            if (inner) {
                zi.add(label);
                break;
            }
        }

        return zi;
    }

    private Set<String> getIntersection(Set<String> aLabels, Set<String> bLabels) {
        Set<String> intersection = new HashSet<String>(aLabels);
        intersection.retainAll(bLabels);

        return intersection;
    }

    private Set<String> getSubtraction(Set<String> intersection, Set<String> allLabels) {
        Set<String> tmp = new HashSet<String>(allLabels);
        tmp.removeAll(intersection);
        return tmp;
    }

    private Set<String> getLabels(BayesianNetwork a) {
        List<BayesianEvent> events = a.getEvents();
        Set<String> labels = new HashSet<>();
        for (BayesianEvent event : events) {
            labels.add(event.getLabel());
        }
        return labels;
    }

    private HashMap<String, List<Dependency>> getDependencies(BayesianNetwork network) {
        HashMap<String, List<Dependency>> dependencies = new HashMap<String, List<Dependency>>();
        List<BayesianEvent> aEvents = network.getEvents();
        for (BayesianEvent event : aEvents) {
            List<BayesianEvent> parents = event.getParents();
            List<Dependency> currentDependencies;
            currentDependencies = new ArrayList<>();

            for (BayesianEvent parent : parents) {
                Dependency dependency = new Dependency(parent.getLabel(), event.getLabel());
                currentDependencies.add(dependency);
            }
            dependencies.put(event.getLabel(), currentDependencies);
        }
        return dependencies;
    }

    private boolean intersectionContainsParents(BayesianEvent bayesianEvent, HashMap<String, List<BayesianEvent>> intersection) {
        for (BayesianEvent parent : bayesianEvent.getParents()) {
            if (!intersection.containsKey(parent.getLabel())) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        BayesianNetwork network1 = new BayesianNetwork();
        BayesianEvent e1 = network1.createEvent("e");
        BayesianEvent d1 = network1.createEvent("d");

        BayesianNetwork network2 = new BayesianNetwork();
        BayesianEvent f1 = network2.createEvent("f");
        BayesianEvent d2 = network2.createEvent("d");

        network1.createDependency(e1, d1);
        network2.createDependency(f1, d2);
        network1.finalizeStructure();
        network2.finalizeStructure();
        d1.getTable().addLine(0.8, true, true);
        d1.getTable().addLine(0.6, true, false);


        d2.getTable().addLine(0.3, true, true);
        d2.getTable().addLine(0.6, true, false);

        e1.getTable().addLine(0.5, true);
        f1.getTable().addLine(0.5, true);
        network1.validate();
        network2.validate();

        NetworkMerger merger = new NetworkMerger();
        BayesianNetwork merge = merger.merge(network1, network2);
        System.out.println(merge.getEvents());

        EnumerationQuery query = new EnumerationQuery(merge);
        BayesianEvent d = merge.getEvent("d");
        BayesianEvent e = merge.getEvent("e");
        BayesianEvent f = merge.getEvent("f");

        System.out.println(merge.toString());
        query.defineEventType(e, EventType.Evidence);
        query.defineEventType(f, EventType.Evidence);
        query.defineEventType(d, EventType.Outcome);

        query.setEventValue(e, true);
        query.setEventValue(d, true);
        query.setEventValue(f, true);

        query.execute();
        System.out.println(query.toString());

    }
}
