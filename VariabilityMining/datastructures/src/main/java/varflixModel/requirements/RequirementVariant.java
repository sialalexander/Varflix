package varflixModel.requirements;

import varflixModel.IVariant;
import java.util.Objects;

public class RequirementVariant implements IVariant {

    private String name;

    public RequirementVariant(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequirementVariant)) return false;
        RequirementVariant that = (RequirementVariant) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}