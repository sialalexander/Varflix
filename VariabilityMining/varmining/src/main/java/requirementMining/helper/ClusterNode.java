package requirementMining.helper;

import java.util.*;

/**
 * Description: Represents a cluster / feature node in the hierarchy, holding mapped requirement IDs,
 * resolved variants, variability classification, and optional child nodes.
 */
public class ClusterNode {

    public enum VariabilityType {
        MANDATORY,
        OPTIONAL,
        ALTERNATIVE
    }

    private final int clusterId;
    private String featureName;
    private final List<String> requirementIds;
    private final Set<String> variants;
    private VariabilityType variabilityType;
    private final double mergeDistance;
    private final List<ClusterNode> children;

    public ClusterNode(int clusterId, List<String> requirementIds, double mergeDistance) {
        this.clusterId = clusterId;
        this.requirementIds = new ArrayList<>(requirementIds);
        this.variants = new LinkedHashSet<>();
        this.mergeDistance = mergeDistance;
        this.children = new ArrayList<>();
    }

    public int getClusterId() {
        return clusterId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public List<String> getRequirementIds() {
        return requirementIds;
    }

    public Set<String> getVariants() {
        return variants;
    }

    public void addVariants(Collection<String> variantNames) {
        this.variants.addAll(variantNames);
    }

    public VariabilityType getVariabilityType() {
        return variabilityType;
    }

    public void setVariabilityType(VariabilityType variabilityType) {
        this.variabilityType = variabilityType;
    }

    public double getMergeDistance() {
        return mergeDistance;
    }

    public List<ClusterNode> getChildren() {
        return children;
    }

    public void addChild(ClusterNode child) {
        this.children.add(child);
    }
}