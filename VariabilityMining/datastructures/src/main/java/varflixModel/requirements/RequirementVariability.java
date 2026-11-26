package varflixModel.requirements;

import varflixModel.IVariability;
import java.util.Objects;

public class RequirementVariability implements IVariability {

    private final String requirementId;
    private String description;
    private String featureName;

    public RequirementVariability(String requirementId, String description) {
        this.requirementId = requirementId;
        this.description = description;
    }

    public RequirementVariability(String requirementId, String description, String featureName) {
        this.requirementId = requirementId;
        this.description = description;
        this.featureName = featureName;
    }

    //TODO sialalex check if this is the best way to do it
    @Override
    public int getId() {
        return (requirementId != null) ? (requirementId.hashCode() & Integer.MAX_VALUE) : 0;
    }

    @Override
    public String getLocation() {
        return (featureName != null && !featureName.trim().isEmpty()) ? featureName : "Root";
    }

    @Override
    public String getElementName() {
        return requirementId;
    }

    public String getRequirementId() {
        return requirementId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    @Override
    public String toString() {
        return requirementId + ": " + description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RequirementVariability)) return false;
        RequirementVariability that = (RequirementVariability) o;
        return Objects.equals(requirementId, that.requirementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requirementId);
    }
}