package merge;

import org.encog.ml.bayesian.table.TableLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Probability {
    double probability;
    boolean[] arguments;
    boolean result;
    List<String> inputs = new ArrayList<>();

    public Probability(double probability, boolean[] arguments, boolean result, List<String> inputs) {
        this.probability = probability;
        this.arguments = arguments;
        this.result = result;
        this.inputs = inputs;
    }

    public Probability(TableLine line, List<String> inputs) {
        double probability = line.getProbability();
        int[] arguments = line.getArguments();
        boolean[] boolArguments = new boolean[arguments.length];
        for(int i = 0; i < arguments.length; i++) {
            boolArguments[i] = arguments[i] != 1;
        }

        this.probability = probability;
        this.arguments = boolArguments;
        this.result = line.getResult() ==0;
        this.inputs = inputs;
    }

    static Probability combineProbability(Probability p1, Probability p2) {
        double newProbability = p1.probability + p2.probability - p1.probability*p2.probability;

        boolean[] res = Arrays.copyOf(p1.arguments, p1.arguments.length + p2.arguments.length);
        System.arraycopy(p2.arguments, 0, res, p1.arguments.length, p2.arguments.length);

        List<String> inputs = new ArrayList<>(p1.inputs);
        inputs.addAll(p2.inputs);

        return new Probability(newProbability, res, p1.result, inputs);
    }

    @Override
    public String toString() {
        return "Probability{" +
                "probability=" + probability +
                ", arguments=" + Arrays.toString(arguments) +
                ", inputs=" + inputs +
                ", result=" + result +
                '}';
    }
}
