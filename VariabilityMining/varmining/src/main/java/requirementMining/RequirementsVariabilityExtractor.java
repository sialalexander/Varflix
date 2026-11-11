package requirementMining;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import variabilityMining.IVariabilityExtractor;
import varflixModel.IVariability;
import varflixModel.IVariant;
import varflixModel.IVariabilityGroup;
import variabilityMining.Language;

public class RequirementsVariabilityExtractor <V extends IVariant, E extends IVariability>
        implements IVariabilityExtractor<V, E> {

    /**
     * Description: Performs automatic variability mining by orchestrating the process of loading requirements, preprocessing, creating a similarity network, clustering, applying heuristics, and creating variability groups.
     * Example: performAutomaticMining("path/to/variants", "path/to/input") -> Returns List of IVariabilityGroup objects
     *
     * @param variantPath The file path to the variants.
     * @param inputPath The file path to the input data.
     * @return A list of extracted variability groups containing variants and variability elements.
     */
    @Override
    public List<IVariabilityGroup<V, E>> performAutomaticMining(String variantPath, String inputPath) {
        //TODO sialalex
        // 0. Load requirements
        // 1. Requirements reading & preprocessing
        // 2. Create imilarity Network (Word Embeddings + BM25+)
        // 3. HAC Clustering  (create Feature Tree)
        // 4. Use heuristics anwenden (mandatory / optional)
        // 5. Create variability groups
        return List.of();
    }

    /**
     * Description: Retrieves the list of variants identified during the mining process.
     * Example: getVariants() -> Returns List of IVariant objects
     *
     * @return A list of extracted variants.
     */
    @Override
    public List<V> getVariants() {
        //TODO sialalex
        return List.of();
    }

    /**
     * Description: Retrieves the list of variability elements identified during the mining process.
     * Example: getElements() -> Returns List of IVariability objects
     *
     * @return A list of extracted variability elements.
     */
    @Override
    public List<E> getElements() {
        //TODO sialalex
        return List.of();
    }

    /**
     * Description: Loads the requirements from a text file and converts them into a map of two strings (req-id and req-desc).
     * Example: loadRequirements("reqs.txt") -> Returns Map containing {"REQ1": "Description 1"}
     *
     * @param filePath The path to the text file containing the requirements.
     * @return A map mapping requirement IDs to their respective descriptions.
     */
    public Map<String, String> loadRequirements(String filePath) {
        try {
            Path path = Paths.get(filePath);
            String rawString =  Files.readString(path);
            Map<String, String> requirementMap = Arrays.stream(rawString.split("\n"))
                    .map(line -> line.trim())
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.split(";", 2)) // Splits into 2 parts: [Key, Value]
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1]));

            return requirementMap;
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Description: Detects the language of the provided text based on common articles. (TODO -> required or different method available)
     * Example: detectLanguage("Das System soll...") -> Returns Language.GERMAN
     *
     * @param text The text input to be analyzed for language detection.
     * @return The detected Language enum (e.g., Language.ENGLISH or Language.GERMAN).
     */
    public Language detectLanguage(String text) {
        if (text == null || text.isEmpty()) return Language.ENGLISH;

        String lowerText = text.toLowerCase();
        if (lowerText.contains(" die ") || lowerText.contains(" der ") || lowerText.contains(" das ")) {
            return Language.GERMAN;
        }
        return Language.ENGLISH;
    }
}