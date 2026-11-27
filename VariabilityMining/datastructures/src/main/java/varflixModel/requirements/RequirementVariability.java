package varflixModel.requirements;

import varflixModel.IVariability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RequirementVariability implements IVariability {

    private final String requirementId;
    private String description;
    private String featureName;
    private List<String> lemmas;
    private List<String> posTags;

    public RequirementVariability(String requirementId, String description) {
        this(requirementId, description, null, Collections.emptyList(), Collections.emptyList());
    }

    public RequirementVariability(String requirementId, String description, String featureName) {
        this(requirementId, description, featureName, Collections.emptyList(), Collections.emptyList());
    }

    public RequirementVariability(String requirementId,
                                  String description,
                                  String featureName,
                                  List<String> lemmas,
                                  List<String> posTags) {
        this.requirementId = requirementId;
        this.description = description;
        this.featureName = featureName;
        this.lemmas = (lemmas != null) ? new ArrayList<>(lemmas) : new ArrayList<>();
        this.posTags = (posTags != null) ? new ArrayList<>(posTags) : new ArrayList<>();
    }

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

    public List<String> getLemmas() {
        return Collections.unmodifiableList(lemmas);
    }

    public void setLemmas(List<String> lemmas) {
        this.lemmas = (lemmas != null) ? new ArrayList<>(lemmas) : new ArrayList<>();
    }

    public List<String> getPosTags() {
        return Collections.unmodifiableList(posTags);
    }

    public void setPosTags(List<String> posTags) {
        this.posTags = (posTags != null) ? new ArrayList<>(posTags) : new ArrayList<>();
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