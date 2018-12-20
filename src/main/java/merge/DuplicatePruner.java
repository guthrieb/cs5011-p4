package merge;

import java.util.*;

public class DuplicatePruner {
    static void pruneDependencies(String label, List<Dependency> dependencies, List<Probability> probabilities) {
        System.out.println("\n\n*****Pruning duplicate dependencies*****");

//        for (String label : labels) {

            Set<Dependency> duplicates = getDuplicates(label, dependencies);

            if (duplicates.size() > 0) {
                System.out.println(label + " --- duplicates found --- " + duplicates);
            }

            for (Dependency dependency : duplicates) {
                removeContradictingRows(dependency.parent, probabilities);
                removeDuplicateColumns(dependency.parent, probabilities);
            }
        System.out.println("\n*****Pruning Complete*****");
    }
//    }


    private static void removeDuplicateColumns(String parent, List<Probability> probabilities) {
        System.out.println("Removing Duplicate " + parent + " columns");
        for (Probability probability : probabilities) {
            boolean initFound = false;
            for (int i = 0; i < probability.inputs.size(); i++) {
                String input = probability.inputs.get(i);
                if (input.equals(parent)) {
                    if (!initFound) {
                        initFound = true;
                    } else {
                        //Remove column
                        System.out.println("Duplicate found --- removing column: " + i);
                        probability.removeColumn(i);
                    }
                }
            }
        }

    }

    private static void removeContradictingRows(String parent, List<Probability> probabilities) {
        ListIterator<Probability> probabilityListIterator = probabilities.listIterator();
        int j = 0;
        while (probabilityListIterator.hasNext()) {
            Probability probability = probabilityListIterator.next();
            List<String> inputs = probability.inputs;


            Set<Boolean> values = new HashSet<>();
            for (int i = 0; i < inputs.size(); i++) {
                String input = inputs.get(i);

                //Add the boolean values of all duplicates
                if (input.equals(parent)) {
                    values.add(probability.arguments[i]);
                }
            }

            //If more than one value for same input, remove the row
            if (values.size() > 1) {
                System.out.println("Contradictory Arguments [" + parent +"= " + values + "] --- Removing Row " + j + "");
                probabilityListIterator.remove();
            }
            j++;
        }
    }

    private static Set<Dependency> getDuplicates(String label, List<Dependency> dependencies) {
        Set<Dependency> dependencySet = new HashSet<>();
        Set<Dependency> duplicates = new HashSet<>();
        for (Dependency dependency : dependencies) {
            if (dependency.dependant.equals(label)) {
                if (dependencySet.contains(dependency)) {
                    duplicates.add(dependency);
                }
                dependencySet.add(dependency);
            }
        }

        return duplicates;
    }
}
