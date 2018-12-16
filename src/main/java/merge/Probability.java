package merge;

import org.encog.ml.bayesian.table.TableLine;

import java.util.Arrays;

public class Probability {
    double probability;
    boolean[] arguments;
    boolean result;
    public Probability(double probability, boolean[] arguments, boolean result) {
        this.probability = probability;
        this.arguments = arguments;
        this.result = result;
    }

    public Probability(TableLine line) {
        double probability = line.getProbability();
        int[] arguments = line.getArguments();
        boolean[] boolArguments = new boolean[arguments.length];
        for(int i = 0; i < arguments.length; i++) {
            boolArguments[i] = arguments[i] != 1;
        }

        this.probability = probability;
        this.arguments = boolArguments;
        this.result = line.getResult() ==0;
    }

    static Probability combineProbability(Probability p1, Probability p2) {
        double newProbability = p1.probability + p2.probability - p1.probability*p2.probability;

        boolean[] res = Arrays.copyOf(p1.arguments, p1.arguments.length + p2.arguments.length);
        System.arraycopy(p2.arguments, 0, res, p1.arguments.length, p2.arguments.length);

        return new Probability(newProbability, res, p1.result);
    }

    @Override
    public String toString() {
        return "Probability{" +
                "probability=" + probability +
                ", arguments=" + Arrays.toString(arguments) +
                ", result=" + result +
                '}';
    }
}
