package requirementMining;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import requirementMining.helper.NLPModels;
import variabilityMining.Language;
import variabilityMining.NLPResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class RequirementPreprocessor {
    //TODO -> standardize method comments / descriptions

    // Main preprocessing method that orchestrates the NLP pipeline
    // Processes all requirements and returns structured NLP results
    public static Map<String, NLPResult> preprocess(Map<String, String> requirementsMap, Language lang) {
        Map<String, NLPResult> processedResults = new LinkedHashMap<>();

        // 1. Load the stopwords for the language
        Set<String> stopwords = loadStopwords(lang);

        // 2. Load the NLP models (POS tagger and lemmatizer)
        NLPModels models = loadModels(lang);

        for (Map.Entry<String, String> entry : requirementsMap.entrySet()) {
            String reqId = entry.getKey();
            String reqText = entry.getValue();

            // 3. Create tokens from the requirements text
            String[] tokens = tokenize(reqText);
            // 4. Remove stopwords from the tokens
            String[] filteredTokens = removeStopwords(tokens, stopwords);
            // 5. Generate POS tags for filtered tokens
            String[] tags = getPOSTags(filteredTokens, models.posTagger);
            // 6. Generate lemmas for filtered tokens
            String[] lemmas = getLemmas(filteredTokens, tags, models.lemmatizer);

            // Combine all results and store
            processedResults.put(reqId, new NLPResult(filteredTokens, tags, lemmas));
        }

        return processedResults;
    }

    // Load all stopwords for the specified language
    private static Set<String> loadStopwords(Language language) {
        try {
            InputStream stream = RequirementPreprocessor.class.getClassLoader().getResourceAsStream(language.stopwordsPath);
            if (stream == null) {
                throw new RuntimeException("Stopwords file not found: " + language.stopwordsPath);
            }
            ObjectMapper mapper = new ObjectMapper();
            List<String> words = mapper.readValue(stream, new TypeReference<List<String>>() {
            });
            return new HashSet<>(words);
        } catch (Exception e) {
            System.err.println("Error loading stopwords: " + e.getMessage());
            return new HashSet<>();
        }
    }

    // Load NLP models (POS tagger and lemmatizer) for the specified language
    private static NLPModels loadModels(Language language) {
        try {
            // Load POS model
            InputStream posStream = RequirementPreprocessor.class.getClassLoader().getResourceAsStream(language.posModelPath);
            if (posStream == null) throw new RuntimeException("POS Model not found: " + language.posModelPath);
            POSTaggerME posTagger = new POSTaggerME(new POSModel(posStream));

            // Load Lemmatizer model
            InputStream lemStream = RequirementPreprocessor.class.getClassLoader().getResourceAsStream(language.lemmaModelPath);
            if (lemStream == null) throw new RuntimeException("Lemmatizer Model not found: " + language.lemmaModelPath);
            Lemmatizer lemmatizer = new LemmatizerME(new LemmatizerModel(lemStream));

            return new NLPModels(posTagger, lemmatizer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Tokenize text into individual words/tokens
    private static String[] tokenize(String text) {
        return SimpleTokenizer.INSTANCE.tokenize(text);
    }

    // Remove stopwords from the token array
    private static String[] removeStopwords(String[] tokens, Set<String> stopWords) {
        List<String> filteredTokens = Arrays.stream(tokens)
                .filter(token -> !stopWords.contains(token.toLowerCase()))
                .collect(Collectors.toList());

        return filteredTokens.toArray(new String[0]);
    }

    // Analyze tokens and generate POS tags for each token
    private static String[] getPOSTags(String[] tokens, POSTaggerME tagger) {
        return tagger.tag(tokens);
    }

    // Generate lemmas for each token based on POS tags
    private static String[] getLemmas(String[] tokens, String[] tags, Lemmatizer lemmatizer) {
        return lemmatizer.lemmatize(tokens, tags);
    }


    //TODO Remove when done with implementation
    // Method only for testing the methods
    public static void main(String[] args) {
        // Small Set for Testing
        Map<String, String> myRequirements = new LinkedHashMap<>();
        myRequirements.put("REQ-01", "Die Anforderungen beschreiben das Testen von Systemen.");
        myRequirements.put("REQ-02", "The requirements describe the testing of systems.");

        System.out.println("Starting test....");

        // Call the main method (set to GERMAN for testing purposes)
        Map<String, NLPResult> finalResults = preprocess(myRequirements, Language.GERMAN);

        // Print the results
        for (Map.Entry<String, NLPResult> entry : finalResults.entrySet()) {
            System.out.println("ID: " + entry.getKey());
            System.out.println(entry.getValue().toString());
        }
    }
}