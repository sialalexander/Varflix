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
import requirementMining.helper.NLPResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class RequirementPreprocessor {
    /**
     * Description: Main preprocessing method that orchestrates the NLP pipeline. Processes all requirements and returns structured NLP results.
     * Example: preprocess({"REQ1": "The system shall authenticate users"}, Language.ENGLISH) -> Returns Map with NLPResult containing tokens, POS tags, and lemmas
     *
     * @param requirementsMap The map of requirement IDs to requirement text.
     * @param lang The language for NLP processing.
     * @return A map of requirement IDs to NLPResult objects containing tokens, POS tags, and lemmas.
     */
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

    /**
     * Description: Load all stopwords for the specified language.
     * Example: loadStopwords(Language.ENGLISH) -> Returns Set containing {"the", "a", "an", ...}
     *
     * @param language The language for which to load stopwords.
     * @return A set of stopwords for the specified language.
     */
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

    /**
     * Description: Load NLP models (POS tagger and lemmatizer) for the specified language.
     * Example: loadModels(Language.ENGLISH) -> Returns NLPModels object containing POSTaggerME and LemmatizerME
     *
     * @param language The language for which to load the models.
     * @return An NLPModels object containing initialized POS tagger and lemmatizer.
     */
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

    /**
     * Description: Tokenize text into individual words/tokens.
     * Example: tokenize("The system shall authenticate") -> Returns String array ["The", "system", "shall", "authenticate"]
     *
     * @param text The input string requirement to be tokenized.
     * @return An array of string tokens.
     */
    private static String[] tokenize(String text) {
        return SimpleTokenizer.INSTANCE.tokenize(text);
    }

    /**
     * Description: Remove stopwords from the token array.
     * Example: removeStopwords(["The", "system", "shall"], {"the", "a"}) -> Returns String array ["system", "shall"]
     *
     * @param tokens The array of text tokens to filter.
     * @param stopWords The set of stopwords to remove from the array.
     * @return An array of filtered tokens with stopwords removed.
     */
    private static String[] removeStopwords(String[] tokens, Set<String> stopWords) {
        List<String> filteredTokens = Arrays.stream(tokens)
                .filter(token -> !stopWords.contains(token.toLowerCase()))
                .collect(Collectors.toList());

        return filteredTokens.toArray(new String[0]);
    }

    /**
     * Description: Analyze tokens and generate POS tags for each token.
     * Example: getPOSTags(["system", "authenticate"], posTagger) -> Returns String array ["NN", "VB"]
     *
     * @param tokens The array of filtered text tokens.
     * @param tagger The initialized POSTaggerME model.
     * @return An array of Part-Of-Speech (POS) tags corresponding to the input tokens.
     */
    private static String[] getPOSTags(String[] tokens, POSTaggerME tagger) {
        return tagger.tag(tokens);
    }

    /**
     * Description: Generate lemmas for each token based on POS tags.
     * Example: getLemmas(["authenticates"], ["VBZ"], lemmatizer) -> Returns String array ["authenticate"]
     *
     * @param tokens The array of text tokens to lemmatize.
     * @param tags The array of Part-Of-Speech (POS) tags for the tokens.
     * @param lemmatizer The initialized Lemmatizer model.
     * @return An array of lemmatized strings representing the base forms of the tokens.
     */
    private static String[] getLemmas(String[] tokens, String[] tags, Lemmatizer lemmatizer) {
        return lemmatizer.lemmatize(tokens, tags);
    }
}