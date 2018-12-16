package merge;

import java.util.List;

public class Event {
    String label;
    List<Dependency> dependencies;
    List<Probability> probabilityTable;

    public Event(String label, List<Dependency> dependencies, List<Probability> probabilityTable) {
        this.label = label;
        this.dependencies = dependencies;
        this.probabilityTable = probabilityTable;
    }

    public String getLabel() {
        return label;
    }
}
