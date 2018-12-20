package extension;

import main.BN1;
import org.encog.ml.bayesian.BayesianEvent;
import org.encog.ml.bayesian.BayesianNetwork;
import org.encog.ml.bayesian.EventType;
import org.encog.ml.bayesian.query.enumerate.EnumerationQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Agent {
    private static final String USAGE = "query = P(<outcome_label><,outcome_label>*|<evidence_label<,evidence_label>*>) [WARNING - QUERIES ARE WHITESPACE SENSITIVE]";

    public static void main(String[] args) {
        System.out.println("Welcome to the network agent");

        BayesianNetwork network = BN1.constructBN1(true);

        boolean running = true;
        while (running) {

            System.out.println("Would you like to perform a query? \"q\" to quit or " + USAGE);

            Scanner in = new Scanner(System.in);
            String nextLine = in.nextLine();
            if (nextLine.equals("q")) {
                running = false;
            } else {
                try {
                    List<QueryElement> queryElements = parseLine(nextLine);

                    executeQuery(network, queryElements);
                } catch (InvalidQueryException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

    }

    private static void executeQuery(BayesianNetwork network, List<QueryElement> queryElements) throws InvalidQueryException {
        EnumerationQuery query = new EnumerationQuery(network);
        for (QueryElement element : queryElements) {
            String label = element.getLabel();

            BayesianEvent event;
            if ((event = (network.getEvent(label))) == null) {
                throw new InvalidQueryException("Label \"" + label + "\" not found in network");
            } else {
                EventType type = EventType.Evidence;
                if (element.isOutcome()) {
                    type = EventType.Outcome;
                }

                query.defineEventType(event, type);
                query.setEventValue(event, element.getResult());
            }

        }

        System.out.println("Executing Query...");
        query.execute();
        System.out.println("Query Executed: " + query.getProblem() + " = " + query.getProbability()*100 + "%");
    }


    private static List<QueryElement> parseLine(String line) throws InvalidQueryException {
        String[] split = line.split("");
        boolean init = true;
        boolean readOutcome = false;
        boolean readOpen = false;
        boolean readClose = false;
        boolean validEnd = false;
        boolean readEvidence = false;
        List<QueryElement> outcomes = new ArrayList<>();
        List<QueryElement> evidences = new ArrayList<>();

        StringBuilder currentRead = new StringBuilder();
        for (String character : split) {
            System.out.println(init);
            System.out.println(readOpen);
            if (init) {
                if (!character.equals("P")) {
                    throw new InvalidQueryException("Usage: " + USAGE);
                } else {
                    init = false;
                    readOpen = true;
                }
            } else if (readOpen) {
                if (!character.equals("(")) {
                    throw new InvalidQueryException("Usage: " + USAGE);
                } else {

                    readOpen = false;
                    readOutcome = true;
                }
            } else if (readOutcome) {
                switch (character) {
                    case "|":
                        outcomes.add(convertToQueryElement(currentRead.toString(), true));
                        currentRead = new StringBuilder();
                        readOutcome = false;
                        readEvidence = true;
                        break;
                    case ")":


                        outcomes.add(convertToQueryElement(currentRead.toString(), true));
                        currentRead = new StringBuilder();

                        validEnd = true;
                        break;
                    case ",":

                        outcomes.add(convertToQueryElement(currentRead.toString(), true));
                        currentRead = new StringBuilder();
                        break;
                    default:
                        currentRead.append(character);
                        break;
                }
            } else if (readEvidence) {
                switch (character) {
                    case ")":
                        if (!currentRead.toString().equals("")) {
                            outcomes.add(convertToQueryElement(currentRead.toString(), false));
                            currentRead = new StringBuilder();
                        }


                        validEnd = true;
                        break;
                    case ",":
                        outcomes.add(convertToQueryElement(currentRead.toString(), false));
                        currentRead = new StringBuilder();
                        break;
                    default:
                        currentRead.append(character);
                        break;
                }
            }
        }

        if (!validEnd) {
            throw new InvalidQueryException(USAGE);
        }

        if (outcomes.size() == 0) {
            throw new InvalidQueryException(USAGE);
        }

        outcomes.addAll(evidences);

        return outcomes;
    }

    private static QueryElement convertToQueryElement(String currentRead, boolean outcome) throws InvalidQueryException {
        String[] elemSplit = currentRead.split("");
        if (elemSplit.length == 0) {
            throw new InvalidQueryException("Query Elements must have a non-zero length");
        }

        if (elemSplit[0].equals("+")) {
            return new QueryElement(currentRead.substring(1), true, outcome);
        } else if (elemSplit[0].equals("-")) {
            return new QueryElement(currentRead.substring(1), false, outcome);
        } else {
            throw new InvalidQueryException("\"" + currentRead + "\" Query elements should be prefaced with + or -");
        }
    }
}
