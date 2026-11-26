package varflixModel.requirements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import varflixModel.IVariabilityGroup;

public class RequirementVariabilityGroup implements IVariabilityGroup<RequirementVariant, RequirementVariability> {

    private int id;
    private String label;
    private List<RequirementVariability> elements;
    private Set<RequirementVariant> occurrences;
    private String attributeName;

    public RequirementVariabilityGroup(int id, String label, List<RequirementVariability> elements) {
        this.id = id;
        this.label = label;
        this.elements = elements != null ? elements : new ArrayList<>();
        this.occurrences = new HashSet<>();
    }

    public RequirementVariabilityGroup() {
        this.elements = new ArrayList<>();
        this.occurrences = new HashSet<>();
    }

    @Override
    public List<RequirementVariability> getElements() {
        return elements;
    }

    @Override
    public void setElements(List<RequirementVariability> elements) {
        this.elements = elements;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public Set<RequirementVariant> getOccurrences() {
        return occurrences;
    }

    @Override
    public void setOccurrences(Set<RequirementVariant> occurrences) {
        this.occurrences = occurrences;
    }

    public void addOccurrences(List<RequirementVariant> variants) {
        occurrences.addAll(variants);
    }

    public void addOccurrence(RequirementVariant variant) {
        occurrences.add(variant);
    }

    @Override
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String getAttributeName() {
        return attributeName;
    }

    public int getId() {
        return id;
    }
}