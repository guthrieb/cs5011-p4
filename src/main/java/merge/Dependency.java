package merge;

import org.encog.ml.bayesian.BayesianEvent;

import java.util.Objects;

public class Dependency {
    String dependant;
    String parent;

    public Dependency(String parent, String dependant) {
        this.dependant = dependant;
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(dependant, that.dependant) &&
                Objects.equals(parent, that.parent);
    }

    @Override
    public String toString() {
        return parent + " --> " + dependant;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependant, parent);
    }
}
