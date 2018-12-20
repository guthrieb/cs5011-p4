package merge;

import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
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
        System.out.println("Q Found: " + notIntersection);
        System.out.println("Z Found: " + intersection);

        Set<String> zi = getZi(intersection, a, b);
        Set<String> ze = getSubtraction(zi, intersection);

        System.out.println("ZI Found: " + zi);
        System.out.println("ZE Found: " + ze);

        System.out.println("Building List Of Dependencies");
        HashMap<String, List<Dependency>> aDependencies = getDependencies(a);
        HashMap<String, List<Dependency>> bDependencies = getDependencies(b);
        HashMap<String, List<Probability>> aProbabilities = getProbabilities(a, Probability.Origin.ONE);
        HashMap<String, List<Probability>> bProbabilities = getProbabilities(b, Probability.Origin.TWO);

        System.out.println("\n\n*****Initial Network A*****");
        printNetworkInfo(aProbabilities, aDependencies);

        System.out.println("\n\n*****Initial Network B*****");
        printNetworkInfo(bProbabilities, bDependencies);

        HashMap<String, List<Dependency>> newDependencies = new HashMap<>();
        HashMap<String, List<Probability>> newProbabilityTables = new HashMap<>();

        addQ(notIntersection, aDependencies, aProbabilities, bDependencies, bProbabilities, newDependencies, newProbabilityTables);

        addZi(zi, intersection, aDependencies, aProbabilities, bDependencies, bProbabilities, newDependencies, newProbabilityTables);

        addZe(ze, aDependencies, aProbabilities, bDependencies, bProbabilities, newDependencies, newProbabilityTables);

        NetworkConstructor constructor = new NetworkConstructor();
        Set<Dependency> dependencyList = new HashSet<>();
        for(List<Dependency> dependencies : newDependencies.values()) {
            if(dependencies != null) {
                dependencyList.addAll(dependencies);
            }
        }

//        DuplicatePruner.pruneDependencies(allLabels, dependencyList, newProbabilityTables);
        printNetworkInfo(newProbabilityTables, dependencyList);
        return constructor.construct(allLabels, dependencyList, newProbabilityTables);
    }

    private void printNetworkInfo(HashMap<String, List<Probability>> newProbabilityTables, Set<Dependency> dependencyList) {
        System.out.println("\n***NETWORK INFO***");
        System.out.println("**Dependencies**:");
        for(Dependency dependency : dependencyList) {
            System.out.println("\t" + dependency);
        }

        System.out.println("\n**CPTs**:");
        for(Map.Entry<String, List<Probability>> probabilityEntry : newProbabilityTables.entrySet()) {
            System.out.println("\n\n---" + probabilityEntry.getKey().toUpperCase() + "---");
            System.out.println("\t" + probabilityEntry.getValue().get(0).inputs + "|" + probabilityEntry.getKey());
            for(Probability probability : probabilityEntry.getValue()) {
                if(probability.result) {
                    System.out.print("\n\t" + Arrays.toString(probability.arguments));
                    System.out.print("|" + probability.probability);
                }
            }
        }
    }

    private void printNetworkInfo(HashMap<String, List<Probability>> newProbabilityTables, HashMap<String, List<Dependency>> dependencyList) {
        Set<Dependency> combinedDependencies = new HashSet<>();
        for(Map.Entry<String, List<Dependency>> entry : dependencyList.entrySet()) {
            combinedDependencies.addAll(entry.getValue());
        }

        printNetworkInfo(newProbabilityTables, combinedDependencies);
    }

    private void addZe(Set<String> ze,
                       HashMap<String, List<Dependency>> aDependencies, HashMap<String, List<Probability>> aProbabilities,
                       HashMap<String, List<Dependency>> bDependencies, HashMap<String, List<Probability>> bProbabilities,
                       HashMap<String, List<Dependency>> newDependencies, HashMap<String, List<Probability>> newProbabilityTables) {
        System.out.println("\n\n*****Adding ZE to new dependencies*****");

        for (String label : ze) {
            System.out.println("\nHandling ze ⊆ ZE: " + label);
            List<Dependency> aLabelDependencies = aDependencies.get(label);
            List<Dependency> bLabelDependencies = bDependencies.get(label);
            List<Probability> aLabelProbabilities = aProbabilities.get(label);
            List<Probability> bLabelProbabilities = bProbabilities.get(label);

            List<Dependency> combinedDependencies = new ArrayList<>();
            combinedDependencies.addAll(aLabelDependencies);
            combinedDependencies.addAll(bLabelDependencies);
            System.out.println("Combining Dependencies From Both Networks: " + combinedDependencies);

            List<Probability> combinedProbabilities = new ArrayList<>();
            List<Probability> unalteredCombinedProbabilities = new ArrayList<>(aLabelProbabilities);
            unalteredCombinedProbabilities.addAll(bLabelProbabilities);

            HashMap<String, List<Double>> probLines = new HashMap<>();
            for (Probability aProbability : aLabelProbabilities) {
                for (Probability bProbability : bLabelProbabilities) {
                    if (aProbability.result == bProbability.result) {
                        Probability combinedProbability = Probability.combineProbability(aProbability, bProbability);
                        System.out.println("\nCombining Probabilities From Both Networks: " + aProbability.probability + "," + bProbability.probability);
                        System.out.println("Result: " + combinedProbability);
                        combinedProbabilities.add(combinedProbability);
                    }
                }
            }



            DuplicatePruner.pruneDependencies(label, combinedDependencies, combinedProbabilities);

            for(Probability probability : combinedProbabilities) {
                String stringified = convertToString(probability.arguments);

                List<Double> probLine = new ArrayList<>();
                if(probLines.containsKey(stringified)) {
                    probLine = probLines.get(stringified);
                }

                probLine.add(probability.probability);

                probLines.put(stringified, probLine);
            }

            for(Probability probability : combinedProbabilities) {
                List<Double> probabilities = probLines.get(convertToString(probability.arguments));

                double sum = 0;
                for(Double probability1 : probabilities) {
                    sum += probability1;
                }

                System.out.println("Normalising Probability: " + probability.probability + "/" + sum + "=" + probability.probability/sum);
                probability.probability/=sum;
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

        System.out.println("\n\n*****Adding ZI to new dependencies*****");

        for (String label : zi) {
            System.out.println("\nHandling zi ⊆ ZI: " + label);
            List<Dependency> aLabelDependencies = aDependencies.get(label);
            List<Dependency> bLabelDependencies = bDependencies.get(label);

            List<Probability> aLabelProbabilities = aProbabilities.get(label);
            List<Probability> bLabelProbabilities = bProbabilities.get(label);

            if (notSubsetOf(aLabelDependencies, z)) {
                System.out.println(label + " --- parents in BN1 are not a subset of Z --- Adding BN1 dependencies and CPT to new network");
                newDependencies.put(label, aLabelDependencies);
                newProbabilityTables.put(label, aLabelProbabilities);
            } else if (notSubsetOf(bLabelDependencies, z)) {
                System.out.println(label + " --- parents in BN2 are not a subset of Z --- Adding BN2 dependencies and CPT to new network");
                newDependencies.put(label, bLabelDependencies);
                newProbabilityTables.put(label, bLabelProbabilities);
            } else {
                System.out.println("Parents in BN1 and BN2 are subsets of Z");
                if (aLabelDependencies.size() > bLabelDependencies.size()) {
                    System.out.println(label + " --- More parents in BN1 --- Adding BN1 dependencies and CPT to network");
                    newDependencies.put(label, aLabelDependencies);
                    newProbabilityTables.put(label, aLabelProbabilities);
                } else {
                    System.out.println(label + " --- More parents in BN2 --- Adding BN2 dependencies and CPT to network");
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

        System.out.println("\n*****Adding Q To New Dependencies*****");
        for (String label : notIntersection) {
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

    private HashMap<String, List<Probability>> getProbabilities(BayesianNetwork a, Probability.Origin origin) {
        HashMap<String, List<Probability>> probabilities = new HashMap<>();

        for (BayesianEvent event : a.getEvents()) {
            BayesianTable table = event.getTable();
            List<BayesianEvent> parents = event.getParents();
            List<String> inputs = new ArrayList<>();
            for(BayesianEvent parent : parents) {
                inputs.add(parent.getLabel());
            }

            for (TableLine line : table.getLines()) {

                Probability probability = new Probability(line, inputs, origin);

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
        Set<String> zi = new HashSet<>();

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
                continue;
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
        Set<String> tmp = new HashSet<>(allLabels);
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
}
