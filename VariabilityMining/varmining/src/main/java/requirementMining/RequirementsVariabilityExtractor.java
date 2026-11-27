package requirementMining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import requirementMining.HACClusterer.ThresholdCriterion;
import requirementMining.helper.ClusterNode;
import requirementMining.helper.NLPResult;
import requirementMining.helper.SimilarityResult;
import variabilityMining.IVariabilityExtractor;
import variabilityMining.Language;
import varflixModel.IVariability;
import varflixModel.IVariabilityGroup;
import varflixModel.IVariant;
import varflixModel.requirements.RequirementVariability;
import varflixModel.requirements.RequirementVariabilityGroup;
import varflixModel.requirements.RequirementVariant;

public class RequirementsVariabilityExtractor<V extends IVariant, E extends IVariability>
        implements IVariabilityExtractor<V, E> {

    private List<RequirementVariant> extractedVariants = new ArrayList<>();
    private List<RequirementVariability> extractedElements = new ArrayList<>();

    /**
     * Description: Performs automatic variability mining from either a single requirements file (1 variant)
     * or a directory of variant files (N variants), each formatted as semicolon-separated lines (ID;Description).
     *
     * @param variantPath Path to the variant file or directory (checked first if provided).
     * @param inputPath   Fallback path to the variant file or directory if variantPath is null/blank.
     * @return A list of extracted variability groups containing variants and requirement elements.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<IVariabilityGroup<V, E>> performAutomaticMining(String variantPath, String inputPath) {
        String source = (variantPath != null && !variantPath.isBlank()) ? variantPath : inputPath;
        if (source == null || source.isBlank()) {
            return List.of();
        }

        // 0. Load requirements and Requirement-to-Variant (R-V) mapping
        Map<String, String> requirementsMap = new LinkedHashMap<>();
        Map<String, Set<String>> reqToVariantsMap = new LinkedHashMap<>();
        loadRequirementsAndVariants(Paths.get(source), requirementsMap, reqToVariantsMap);

        if (requirementsMap.isEmpty()) {
            return List.of();
        }

        // Extract all distinct variant names
        Set<String> allVariantNames = new LinkedHashSet<>();
        reqToVariantsMap.values().forEach(allVariantNames::addAll);

        // Instantiate canonical RequirementVariant objects while preserving order
        Map<String, RequirementVariant> variantLookup = new LinkedHashMap<>();
        for (String variantName : allVariantNames) {
            variantLookup.put(variantName, new RequirementVariant(variantName));
        }
        this.extractedVariants = new ArrayList<>(variantLookup.values());

        // 1. Requirements reading & NLP preprocessing
        Language lang = detectLanguage(String.join(" ", requirementsMap.values()));
        Map<String, NLPResult> nlpMap = RequirementPreprocessor.preprocess(requirementsMap, lang);

        List<String> orderedReqIds = new ArrayList<>(requirementsMap.keySet());
        List<NLPResult> orderedNlpResults = orderedReqIds.stream()
                .map(nlpMap::get)
                .collect(Collectors.toList());

        // 2. Create Similarity Network (Word Embeddings + BM25+)
        SimilarityResult similarityResult = SimilarityNetworkCalculator.processRequirements(orderedNlpResults, lang);

        // 3. HAC Clustering with automatic Silhouette threshold selection
        HACClusterer clusterer = new HACClusterer();
        List<ClusterNode> clusters = clusterer.cluster(
                similarityResult.similarityMatrix,
                orderedReqIds,
                ThresholdCriterion.SILHOUETTE
        );

        // 4. Construct Feature Tree (F-R mapping) & evaluate variability heuristics (F-V mapping)
        FeatureModelGenerator modelGenerator = new FeatureModelGenerator();
        ClusterNode featureRoot = modelGenerator.buildFeatureTree(clusters, nlpMap, similarityResult.weights);
        modelGenerator.evaluateVariabilityHeuristics(featureRoot, reqToVariantsMap, allVariantNames);

        // 5. Create variability groups, store elements, and generate pcm.csv
        List<RequirementVariabilityGroup> groups = modelGenerator.createVariabilityGroups(
                featureRoot,
                requirementsMap,
                variantLookup
        );

        // Flatten all elements from the groups into a single list
        this.extractedElements = groups.stream()
                .flatMap(g -> g.getElements().stream())
                .collect(Collectors.toList());

        // Cast each group to the generic IVariabilityGroup interface
        List<IVariabilityGroup<V, E>> castedGroups = groups.stream()
                .map(g -> (IVariabilityGroup<V, E>) g)
                .collect(Collectors.toList());

        // Build the Product Comparison Matrix and write pcm.csv
        buildPCM(getVariants(), castedGroups);

        return castedGroups;
    }

    /**
     * Description: Retrieves the unmodifiable list of variants identified during the mining process.
     *
     * @return A list of extracted variants.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<V> getVariants() {
        return (List<V>) Collections.unmodifiableList(extractedVariants);
    }

    /**
     * Description: Retrieves the unmodifiable list of requirement variability elements identified during mining.
     *
     * @return A list of extracted variability elements.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<E> getElements() {
        return (List<E>) Collections.unmodifiableList(extractedElements);
    }

    /**
     * Description: Loads requirements and Requirement-to-Variant (R-V) mappings from either
     * a single variant file (1 variant) or a directory of variant files (N variants).
     */
    private void loadRequirementsAndVariants(Path sourcePath,
                                             Map<String, String> requirementsMap,
                                             Map<String, Set<String>> reqToVariantsMap) {
        try {
            if (Files.isDirectory(sourcePath)) {
                try (Stream<Path> files = Files.list(sourcePath)) {
                    List<Path> variantFiles = files.filter(Files::isRegularFile).sorted().toList();
                    for (Path file : variantFiles) {
                        extractVariantFiles(file, requirementsMap, reqToVariantsMap);
                    }
                }
            } else if (Files.isRegularFile(sourcePath)) {
                extractVariantFiles(sourcePath, requirementsMap, reqToVariantsMap);
            }
        } catch (IOException e) {
            System.err.println("Error loading requirements source: " + e.getMessage());
        }
    }

    /**
     * Description: Parses a semicolon-separated variant file (ID;Description) and merges its
     * requirements and variant associations into the target maps.
     */
    private void extractVariantFiles(Path file,
                                  Map<String, String> requirementsMap,
                                  Map<String, Set<String>> reqToVariantsMap) throws IOException {
        String fileName = file.getFileName().toString();
        int dotIdx = fileName.lastIndexOf('.');
        String variantName = (dotIdx > 0) ? fileName.substring(0, dotIdx) : fileName;

        for (String line : Files.readAllLines(file)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String[] parts = trimmed.split(";", 2);
            if (parts.length < 2) continue;

            String rawId = parts[0].trim();
            String text = parts[1].trim();

            if (!requirementsMap.containsKey(rawId)) {
                requirementsMap.put(rawId, text);
                reqToVariantsMap.computeIfAbsent(rawId, k -> new LinkedHashSet<>()).add(variantName);
            } else if (requirementsMap.get(rawId).equals(text)) {
                reqToVariantsMap.computeIfAbsent(rawId, k -> new LinkedHashSet<>()).add(variantName);
            } else {
                String scopedId = variantName + "_" + rawId;
                requirementsMap.put(scopedId, text);
                reqToVariantsMap.computeIfAbsent(scopedId, k -> new LinkedHashSet<>()).add(variantName);
            }
        }
    }

    /**
     * Description: Detects the language of the provided text based on common German articles.
     *
     * @param text The text input to be analyzed for language detection.
     * @return The detected Language enum (Language.GERMAN or Language.ENGLISH).
     */
    //TODO optimize this method (e.g., use a trie or regex)
    private Language detectLanguage(String text) {
        if (text == null || text.isEmpty()) return Language.ENGLISH;

        String lowerText = " " + text.toLowerCase() + " ";
        if (lowerText.contains(" die ") || lowerText.contains(" der ") || lowerText.contains(" das ")) {
            return Language.GERMAN;
        }
        return Language.ENGLISH;
    }
}