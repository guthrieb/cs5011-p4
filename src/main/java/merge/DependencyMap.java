package merge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DependencyMap {
    Set<String> labels = new HashSet<String>();
    HashMap<String, Event> events = new HashMap<>();

    public DependencyMap(List<Event> events) {
        for (Event event : events) {
            labels.add(event.label);
        }
    }
}
