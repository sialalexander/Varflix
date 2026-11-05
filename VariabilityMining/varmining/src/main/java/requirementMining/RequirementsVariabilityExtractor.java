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

    @Override
    public List<V> getVariants() {
        //TODO sialalex
        return List.of();
    }

    @Override
    public List<E> getElements() {
        //TODO sialalex
        return List.of();
    }

    //This method loads the requirement from text file and converts it into a map of two strings (req-id and req-desc)
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

    //This method detects the language
    // TODO -> required or different method available
    public Language detectLanguage(String text) {
        if (text == null || text.isEmpty()) return Language.ENGLISH;

        String lowerText = text.toLowerCase();
        if (lowerText.contains(" die ") || lowerText.contains(" der ") || lowerText.contains(" das ")) {
            return Language.GERMAN;
        }
        return Language.ENGLISH;
    }
}