package requirementMining;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class RequirementPreprocessor {

    public enum Language {
        ENGLISH("requirementMining/models/en-pos-maxent.bin",
                "requirementMining/models/en-lemmatizer.bin",
                "requirementMining/stopwords/stopwords-en.json"),

        GERMAN("requirementMining/models/de-pos-maxent.bin",
                "requirementMining/models/de-lemmatizer.bin",
                "requirementMining/stopwords/stopwords-de.json");

        public final String posModelPath;
        public final String lemmaModelPath;
        public final String stopwordsPath;

        Language(String posModelPath, String lemmaModelPath, String stopwordsPath) {
            this.posModelPath = posModelPath;
            this.lemmaModelPath = lemmaModelPath;
            this.stopwordsPath = stopwordsPath;
        }
    }

    // TODO -> maybe move to datastructures???
    // Natural Language Processing - Result
    // Tokens (words) with tags (which type) and lemmas (normalized form)
    public static class NLPResult {
        public final String[] tokens;
        public final String[] tags;
        public final String[] lemmas;

        public NLPResult(String[] tokens, String[] tags, String[] lemmas) {
            this.tokens = tokens;
            this.tags = tags;
            this.lemmas = lemmas;
        }

        @Override
        public String toString() {
            return "Tokens: " + Arrays.toString(tokens) +
                    "\nTags:   " + Arrays.toString(tags) +
                    "\nLemmas: " + Arrays.toString(lemmas) + "\n";
        }
    }

    // Main method of the RequirementPreprocessor.java class
    // It uses all sub methods of this class and returns the required types
    public static Map<String, NLPResult> preprocess(Map<String, String> requirementsMap) {
        Map<String, NLPResult> processedResults = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : requirementsMap.entrySet()) {
            String reqId = entry.getKey();
            String reqText = entry.getValue();

            // 1. Detect the language of the requirements
            //TODO -> move out - detect only once
            Language lang = detectLanguage(reqText);
            // 2. Load the stopwords the language
            Set<String> stopwords = loadStopwords(lang);
            // 3. Create the tokens from the reqs
            String[] tokens = tokenize(reqText);
            // 4. Remove the stopwords from the tokens
            String[] filteredTokens = removeStopwords(tokens, stopwords);
            // 5. Generate the POSTags
            String[] tags = getPOSTags(filteredTokens, lang);
            // 6. Gernerate the Lemmas
            String[] lemmas = getLemmas(filteredTokens, tags, lang);

            // Combine all results and return it
            processedResults.put(reqId, new NLPResult(filteredTokens, tags, lemmas));
        }

        return processedResults;
    }

    //This method loads the requirement from text file and converts it into a map of two strings (req-id and req-desc)
    public static Map<String, String> loadRequirements(String filePath) {
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
    public static Language detectLanguage(String text) {
        if (text == null || text.isEmpty()) return Language.ENGLISH;

        String lowerText = text.toLowerCase();
        if (lowerText.contains(" die ") || lowerText.contains(" der ") || lowerText.contains(" das ")) {
            return Language.GERMAN;
        }
        return Language.ENGLISH;
    }

    //Load all stopwords for the specific language
    // TODO -> loading the stopwords, reduce to once?
    public static Set<String> loadStopwords(Language language) {
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

    //Generate tokens from sentence / string (split it)
    public static String[] tokenize(String text) {
        return SimpleTokenizer.INSTANCE.tokenize(text);
    }

    //Remove the stopwords from the tokens arrays
    public static String[] removeStopwords(String[] tokens, Set<String> stopWords) {
        List<String> filteredTokens = Arrays.stream(tokens)
                .filter(token -> !stopWords.contains(token.toLowerCase()))
                .collect(Collectors.toList());

        return filteredTokens.toArray(new String[0]);
    }

    //Analyize toeksn and create POS tags for each toekns
    //TODO -> loading the model, reduce to once?
    public static String[] getPOSTags(String[] tokens, Language language) {
        try {
            InputStream posStream = RequirementPreprocessor.class.getClassLoader().getResourceAsStream(language.posModelPath);
            if (posStream == null) throw new RuntimeException("POS Model not found: " + language.posModelPath);
            POSTaggerME tagger = new POSTaggerME(new POSModel(posStream));
            return tagger.tag(tokens); //return tags
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Analyize tokens and create lemmas for each token
    //TODO -> loading the model, reduce to once?
    public static String[] getLemmas(String[] tokens, String[] tags, Language language) {
        try {
            InputStream lemStream = RequirementPreprocessor.class.getClassLoader().getResourceAsStream(language.lemmaModelPath);
            if (lemStream == null) throw new RuntimeException("Lemmatizer Model not found: " + language.lemmaModelPath);
            Lemmatizer lemmatizer = new LemmatizerME(new LemmatizerModel(lemStream));
            return lemmatizer.lemmatize(tokens, tags); //return lemmas
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // Method only for testing the methods
    public static void main(String[] args) {
        // Small Set for Testing
        Map<String, String> myRequirements = new LinkedHashMap<>();
        myRequirements.put("REQ-01", "Die Anforderungen beschreiben das Testen von Systemen.");
        myRequirements.put("REQ-02", "The requirements describe the testing of systems.");

        System.out.println("Starting test....");

        // Call of the mian method
        Map<String, NLPResult> finalResults = preprocess(myRequirements);

        //PRint the results
        for (Map.Entry<String, NLPResult> entry : finalResults.entrySet()) {
            System.out.println("ID: " + entry.getKey());
            System.out.println(entry.getValue().toString());
        }
    }
}