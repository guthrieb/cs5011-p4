package merge;

import org.apache.commons.lang3.ArrayUtils;
import org.encog.ml.bayesian.table.TableLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Probability {
    private final Origin origin;
    double probability;
    boolean[] arguments;
    boolean result;
    List<String> inputs;

    public void removeColumn(int i) {
        inputs.remove(i);
        arguments = ArrayUtils.remove(arguments, i);
    }

    enum Origin{
        MERGE, ONE, TWO
    }


    public Probability(double probability, boolean[] arguments, boolean result, List<String> inputs, Origin origin) {
        this.probability = probability;
        this.arguments = arguments;
        this.result = result;
        this.inputs = inputs;
        this.origin = origin;
    }

    public Probability(TableLine line, List<String> inputs, Origin origin) {
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
        this.origin = origin;
    }

    static Probability combineProbability(Probability p1, Probability p2) {
        double newProbability = p1.probability + p2.probability - p1.probability*p2.probability;

        boolean[] res = Arrays.copyOf(p1.arguments, p1.arguments.length + p2.arguments.length);
        System.arraycopy(p2.arguments, 0, res, p1.arguments.length, p2.arguments.length);

        List<String> inputs = new ArrayList<>(p1.inputs);
        inputs.addAll(p2.inputs);

        return new Probability(newProbability, res, p1.result, inputs, Origin.MERGE);
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
