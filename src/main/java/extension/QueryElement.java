package extension;

public class QueryElement {
    private final String label;
    private final boolean result;
    private final boolean outcome;

    public QueryElement(String label, boolean result, boolean outcome) {
        this.label = label;
        this.result = result;
        this.outcome = outcome;
    }

    public String getLabel() {
        return label;
    }

    public boolean getResult() {
        return result;
    }

    public boolean isOutcome() {
        return outcome;
    }
}
