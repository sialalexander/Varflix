package requirementMining;

import requirementMining.SimilarityNetworkCalculator.WordWeighting;
import requirementMining.helper.ClusterNode;
import requirementMining.helper.ClusterNode.VariabilityType;
import requirementMining.helper.NLPResult;
import varflixModel.requirements.RequirementVariability;
import varflixModel.requirements.RequirementVariabilityGroup;
import varflixModel.requirements.RequirementVariant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description: Constructs the feature tree from HAC clusters (F-R mapping), evaluates variability heuristics
 * (Mandatory, Optional, Alternative) via R-V mapping, and produces IVariabilityGroup instances.
 */
public class FeatureModelGenerator {

    /**
     * Description: Assigns descriptive feature names to clusters and constructs the root Feature Tree (F-R Mapping).
     *
     * @param clusters        Clusters produced by HACClusterer.
     * @param nlpResults      Preprocessed NLP results per requirement ID.
     * @param wordWeighting   IDF + syntactic POS weighting helper from SimilarityResult.
     * @return Root ClusterNode containing all extracted feature nodes as children.
     */
    public ClusterNode buildFeatureTree(List<ClusterNode> clusters,
                                        Map<String, NLPResult> nlpResults,
                                        WordWeighting wordWeighting) {
        ClusterNode root = new ClusterNode(0, Collections.emptyList(), 1.0);
        root.setFeatureName("SystemRoot");
        root.setVariabilityType(VariabilityType.MANDATORY);

        Set<String> usedNames = new HashSet<>();

        for (ClusterNode cluster : clusters) {
            String baseName = generateFeatureName(cluster.getRequirementIds(), nlpResults, wordWeighting);
            String uniqueName = baseName;
            int suffix = 2;
            while (usedNames.contains(uniqueName)) {
                uniqueName = baseName + "_" + suffix++;
            }
            usedNames.add(uniqueName);
            cluster.setFeatureName(uniqueName);
            root.addChild(cluster);
        }

        return root;
    }

    /**
     * Description: Evaluates Mandatory, Optional, and Alternative heuristics by linking the F-R mapping
     * with the Requirement-to-Variant (R-V) mapping.
     *
     * @param featureRoot                Root node of the feature tree.
     * @param requirementToVariantsMap   R-V mapping (Requirement ID -> Set of Variant names).
     * @param allVariantNames            Complete set of system variants (V1, V2, ...).
     */
    public void evaluateVariabilityHeuristics(ClusterNode featureRoot,
                                              Map<String, Set<String>> requirementToVariantsMap,
                                              Set<String> allVariantNames) {
        List<ClusterNode> features = featureRoot.getChildren();

        // 1. Compute Feature-to-Variant (F-V) mapping and classify Mandatory vs. Optional
        for (ClusterNode feature : features) {
            for (String reqId : feature.getRequirementIds()) {
                Set<String> reqVariants = requirementToVariantsMap.getOrDefault(reqId, Collections.emptySet());
                feature.addVariants(reqVariants);
            }

            Set<String> featureVariants = feature.getVariants();
            if (!allVariantNames.isEmpty() && featureVariants.containsAll(allVariantNames)) {
                feature.setVariabilityType(VariabilityType.MANDATORY);
            } else {
                feature.setVariabilityType(VariabilityType.OPTIONAL);
            }
        }

        // 2. Detect Alternative (XOR) feature pairs: mutually exclusive optional features covering all variants
        List<ClusterNode> optionalFeatures = features.stream()
                .filter(f -> f.getVariabilityType() == VariabilityType.OPTIONAL && !f.getVariants().isEmpty())
                .toList();

        for (int i = 0; i < optionalFeatures.size(); i++) {
            for (int j = i + 1; j < optionalFeatures.size(); j++) {
                ClusterNode fA = optionalFeatures.get(i);
                ClusterNode fB = optionalFeatures.get(j);

                boolean disjoint = Collections.disjoint(fA.getVariants(), fB.getVariants());
                Set<String> union = new HashSet<>(fA.getVariants());
                union.addAll(fB.getVariants());

                if (disjoint && union.equals(allVariantNames)) {
                    fA.setVariabilityType(VariabilityType.ALTERNATIVE);
                    fB.setVariabilityType(VariabilityType.ALTERNATIVE);
                }
            }
        }
    }

    /**
     * Description: Converts evaluated feature nodes into RequirementVariabilityGroup instances (Step 2.5).
     *
     * @param featureRoot     Root node of the evaluated feature tree.
     * @param rawRequirements Map of requirement IDs to original requirement descriptions.
     * @param variantLookup   Map of variant names to canonical RequirementVariant instances.
     * @return List of RequirementVariabilityGroup objects ready for PCM export and GUI visualization.
     */
    public List<RequirementVariabilityGroup> createVariabilityGroups(ClusterNode featureRoot,
                                                                     Map<String, String> rawRequirements,
                                                                     Map<String, RequirementVariant> variantLookup) {
        List<RequirementVariabilityGroup> groups = new ArrayList<>();

        for (ClusterNode feature : featureRoot.getChildren()) {
            List<RequirementVariability> elements = new ArrayList<>();
            for (String reqId : feature.getRequirementIds()) {
                String desc = rawRequirements.getOrDefault(reqId, "");
                elements.add(new RequirementVariability(reqId, desc, feature.getFeatureName()));
                //TODO sialalex: add all informations to the feature name
            }

            RequirementVariabilityGroup group = new RequirementVariabilityGroup(
                    feature.getClusterId(),
                    feature.getFeatureName(),
                    elements
            );
            group.setAttributeName(feature.getVariabilityType().name());

            for (String variantName : feature.getVariants()) {
                RequirementVariant variantObj = variantLookup.get(variantName);
                if (variantObj != null) {
                    group.addOccurrence(variantObj);
                }
            }

            groups.add(group);
        }

        return groups;
    }

    /**
     * Derives a concise feature label from the highest-weighted domain terms (nouns/verbs) in the cluster.
     */
    private String generateFeatureName(List<String> reqIds,
                                       Map<String, NLPResult> nlpResults,
                                       WordWeighting wordWeighting) {
        Map<String, Double> termScores = new LinkedHashMap<>();

        for (String reqId : reqIds) {
            NLPResult nlp = nlpResults.get(reqId);
            if (nlp == null || nlp.lemmas == null) continue;

            for (int i = 0; i < nlp.lemmas.length; i++) {
                String lemma = nlp.lemmas[i];
                String tag = (nlp.tags != null && i < nlp.tags.length) ? nlp.tags[i] : "";

                if (lemma == null || lemma.length() < 3) continue;
                // Focus on content words (Nouns, Verbs, Adjectives)
                if (!(tag.startsWith("N") || tag.startsWith("V") || tag.startsWith("ADJ") || tag.startsWith("JJ"))) {
                    continue;
                }

                String normalized = lemma.toLowerCase();
                double weight = wordWeighting.getWeight(normalized, tag);
                termScores.merge(normalized, weight, Double::sum);
            }
        }

        List<String> topTerms = termScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(2)
                .map(e -> capitalize(e.getKey()))
                .collect(Collectors.toList());

        if (topTerms.isEmpty()) {
            return "Feature_" + String.join("_", reqIds);
        }
        return String.join(" ", topTerms);
    }

    private String capitalize(String word) {
        if (word == null || word.isEmpty()) return word;
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }
}