package requirementMining;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import variabilityMining.Language;
import requirementMining.helper.NLPResult;
import requirementMining.helper.SimilarityResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Description: Calculates requirement similarity matrices using BM25+ scoring combined with semantic word embeddings.
 */
public class SimilarityNetworkCalculator {

    /** STEP 2 **/
    /**
     * Description: Provides methods to load and process word embeddings using the Word2Vec model from DeepLearning4j.
     */
    public static class WordEmbeddingProvider {
        private final Word2Vec word2Vec;

        /**
         * Description: Initializes the WordEmbeddingProvider by loading the Word2Vec model from the specified file path.
         * Example: new WordEmbeddingProvider("path/to/model.vec")
         *
         * @param modelPath The path to the pre-trained Word2Vec model file.
         */
        public WordEmbeddingProvider(String modelPath) {
            this.word2Vec = WordVectorSerializer.readWord2VecModel(new File(modelPath));
        }

        /**
         * Description: Retrieves the vector representation (coordinates in semantic space) for a given word from the Word2Vec model.
         * Returns a zero vector if the word is not present in the vocabulary.
         * Example: getVector("door") -> [0.12, -0.45, 0.88, ... ] (Array with 300 values)
         *
         * @param word The word for which to retrieve the vector.
         * @return An array of doubles representing the word vector.
         */
        public double[] getVector(String word) {
            if (word2Vec.hasWord(word)) {
                return word2Vec.getWordVector(word);
            }
            return new double[word2Vec.getLayerSize()];
        }

        /**
         * Description: Checks whether the specified word exists in the Word2Vec model's vocabulary.
         * Example: hasWord("door") -> Returns true
         *
         * @param word The word to check.
         * @return True if the word exists in the vocabulary, false otherwise.
         */
        public boolean hasWord(String word) {
            return word2Vec.hasWord(word);
        }

        /**
         * Description: Measures the angle between two vectors in space. 1.0 means parallel (identical meaning),
         * 0.0 means orthogonal (unrelated), and -1.0 means opposite.
         * Example: calculateCosineSimilarity([vector for "door"], [vector for "window"]) -> 0.75 (high semantic similarity)
         *
         * @param vecA The first vector array.
         * @param vecB The second vector array.
         * @return The cosine similarity score between the two vectors.
         */
        public double calculateCosineSimilarity(double[] vecA, double[] vecB) {
            double dotProduct = 0.0;
            double normA = 0.0;
            double normB = 0.0;
            for (int i = 0; i < vecA.length; i++) {
                dotProduct += vecA[i] * vecB[i];
                normA += Math.pow(vecA[i], 2);
                normB += Math.pow(vecB[i], 2);
            }
            if (normA == 0.0 || normB == 0.0) return 0.0;
            return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        }
    }

    /** STEP 2 **/
    /**
     * Description: Calculates word weights based on document frequency (IDF) and syntactic information.
     */
    public static class WordWeighting {
        private final Map<String, Double> idfMap = new HashMap<>();

        /**
         * Description: Initializes the WordWeighting instance by computing Inverse Document Frequency (IDF) scores
         * for all unique lemmas across the requirements list. Rarer words across the dataset receive higher importance.
         * Example: With 10 reqs, if "door" appears in 5 -> IDF = ~1.2. If "unlock" appears in 1 -> IDF = ~2.8.
         *
         * @param requirements The list of preprocessed NLPResult requirements.
         */
        public WordWeighting(List<NLPResult> requirements) {
            int documentCount = requirements.size();
            Map<String, Integer> documentFrequencyMap = new HashMap<>();

            for (NLPResult req : requirements) {
                Set<String> uniqueLemmas = new HashSet<>(Arrays.asList(req.lemmas));
                for (String lemma : uniqueLemmas) {
                    if (lemma != null && !lemma.isBlank()) {
                        String lowerLemma = lemma.toLowerCase();
                        documentFrequencyMap.put(lowerLemma, documentFrequencyMap.getOrDefault(lowerLemma, 0) + 1);
                    }
                }
            }

            for (Map.Entry<String, Integer> entry : documentFrequencyMap.entrySet()) {
                double idf = Math.log((documentCount + 1.0) / (entry.getValue() + 0.5)) + 1.0;
                idfMap.put(entry.getKey(), idf);
            }
        }

        /**
         * Description: Calculates the final weight for a word lemma by combining its document frequency (IDF)
         * and syntactic Part-Of-Speech tag (nouns and verbs receive a multiplier because they form the core meaning).
         * Example: getWeight("door", "NN") -> IDF (1.2) * Noun-Factor (1.5) = final weight 1.8.
         *
         * @param lemma The normalized word lemma.
         * @param posTag The Part-Of-Speech tag of the word.
         * @return The calculated weight for the word.
         */
        public double getWeight(String lemma, String posTag) {
            String lowerLemma = lemma.toLowerCase();
            double baseIdf = idfMap.getOrDefault(lowerLemma, 1.0);
            double syntacticWeight = 1.0;

            if (posTag != null) {
                if (posTag.startsWith("N")) {
                    syntacticWeight = 1.5;
                } else if (posTag.startsWith("V")) {
                    syntacticWeight = 1.2;
                }
            }
            return baseIdf * syntacticWeight;
        }
    }

    /** STEP 3 **/
    /**
     * Description: Calculates the similarity matrix using BM25+ and semantic similarity.
     */
    private static class BM25PlusSimilarityCalculator {
        //TODO move to config class / or config file
        private final double k1 = 1.5;
        private final double b = 0.75;
        private final double delta = 1.0;

        /**
         * Description: Calculates the final NxN similarity matrix. It computes raw BM25 scores for exact word matches,
         * normalizes them between 0 and 1, and averages them with the semantic cosine similarity.
         * Example: matrix[0][2] = 0.86 means requirement 0 and requirement 2 have an 86% overall similarity.
         *
         * @param requirements The list of preprocessed NLPResult requirements.
         * @param weighting The WordWeighting helper instance.
         * @param embeddings The WordEmbeddingProvider helper instance.
         * @return A 2D array of doubles representing pairwise similarity scores between requirements.
         */
        public double[][] calculateMatrix(List<NLPResult> requirements, WordWeighting weighting, WordEmbeddingProvider embeddings) {
            int size = requirements.size();
            double[][] matrix = new double[size][size];
            double avgDocLength = 0;

            for (NLPResult req : requirements) {
                avgDocLength += req.lemmas.length;
            }
            avgDocLength /= size;

            // 1. Calculate all raw BM25 scores and find the maximum value
            double[][] rawBm25 = new double[size][size];
            double maxBm25 = 0.0001; // Prevents division by zero

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i != j) {
                        // Symmetric BM25: (A->B + B->A) / 2
                        double scoreAB = computeBM25PlusScore(requirements.get(i), requirements.get(j), weighting, avgDocLength);
                        double scoreBA = computeBM25PlusScore(requirements.get(j), requirements.get(i), weighting, avgDocLength);
                        double symScore = (scoreAB + scoreBA) / 2.0;

                        rawBm25[i][j] = symScore;
                        if (symScore > maxBm25) {
                            maxBm25 = symScore;
                        }
                    }
                }
            }

            // 2. Normalize (0-1) and average with Cosine similarity
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i == j) {
                        matrix[i][j] = 1.0;
                    } else {
                        double normalizedBm25 = rawBm25[i][j] / maxBm25;
                        double semanticScore = computeSemanticScore(requirements.get(i), requirements.get(j), weighting, embeddings);

                        // If cosine is negative (opposite direction), set to 0
                        if (semanticScore < 0) semanticScore = 0;

                        matrix[i][j] = (normalizedBm25 + semanticScore) / 2.0;
                    }
                }
            }
            return matrix;
        }

        /**
         * Description: Computes how well two requirements match based on exact word overlaps, while penalizing overly long sentences.
         * Example: ReqA ("The door opens") vs ReqB ("The system closes the door"). Overlap on "door" -> Score e.g., 3.42
         *
         * @param query The query requirement NLPResult.
         * @param document The target document requirement NLPResult.
         * @param weighting The WordWeighting helper instance.
         * @param avgDocLength The average document length across all requirements.
         * @return The raw BM25+ relevance score.
         */
        private double computeBM25PlusScore(NLPResult query, NLPResult document, WordWeighting weighting, double avgDocLength) {
            double score = 0.0;
            double docLength = document.lemmas.length;
            Map<String, Integer> termFrequencies = new HashMap<>();

            for (String lemma : document.lemmas) {
                if (lemma != null) {
                    String lower = lemma.toLowerCase();
                    termFrequencies.put(lower, termFrequencies.getOrDefault(lower, 0) + 1);
                }
            }

            for (int i = 0; i < query.lemmas.length; i++) {
                String lemma = query.lemmas[i];
                if (lemma == null || lemma.isBlank()) continue;

                String lowerLemma = lemma.toLowerCase();
                String posTag = query.tags[i];
                double idfWeight = weighting.getWeight(lowerLemma, posTag);
                int tf = termFrequencies.getOrDefault(lowerLemma, 0);

                double numerator = tf * (k1 + 1);
                double denominator = tf + k1 * (1 - b + b * (docLength / avgDocLength));

                // Core formula
                score += idfWeight * ((numerator / denominator) + delta);
            }
            return score;
        }

        /**
         * Description: Computes the semantic similarity between two requirements based on their final weighted document vectors.
         * Example: ReqA ("open door") and ReqB ("close window") might have no exact word overlap but share semantically related concepts -> Semantic Score e.g., 0.65
         *
         * @param reqA The first requirement NLPResult.
         * @param reqB The second requirement NLPResult.
         * @param weighting The WordWeighting helper instance.
         * @param embeddings The WordEmbeddingProvider helper instance.
         * @return The semantic similarity score between 0.0 and 1.0.
         */
        private double computeSemanticScore(NLPResult reqA, NLPResult reqB, WordWeighting weighting, WordEmbeddingProvider embeddings) {
            //TODO optimize: only compute once for each word vector
            double[] vecA = computeWeightedDocumentVector(reqA, weighting, embeddings);
            double[] vecB = computeWeightedDocumentVector(reqB, weighting, embeddings);
            return embeddings.calculateCosineSimilarity(vecA, vecB);
        }

        /**
         * Description: Builds a single 300-dimensional vector representing the entire requirement by adding all word vectors
         * (multiplied by their weight) and averaging them.
         * Example: (Vector("door")*1.8 + Vector("open")*1.4) / 3.2 -> [0.08, -0.22, 0.65, ... ] (Represents the meaning of the whole sentence)
         *
         * @param req The requirement NLPResult.
         * @param weighting The WordWeighting helper instance.
         * @param embeddings The WordEmbeddingProvider helper instance.
         * @return A double array representing the weighted document vector.
         */
        private double[] computeWeightedDocumentVector(NLPResult req, WordWeighting weighting, WordEmbeddingProvider embeddings) {
            double[] docVector = null;
            double totalWeight = 0.0;

            for (int i = 0; i < req.lemmas.length; i++) {
                String lemma = req.lemmas[i];
                if (lemma == null || lemma.isBlank()) continue;

                String lowerLemma = lemma.toLowerCase();
                if (embeddings.hasWord(lowerLemma)) {
                    double[] wordVec = embeddings.getVector(lowerLemma);
                    double weight = weighting.getWeight(lowerLemma, req.tags[i]);

                    if (docVector == null) {
                        docVector = new double[wordVec.length];
                    }

                    for (int d = 0; d < wordVec.length; d++) {
                        docVector[d] += wordVec[d] * weight;
                    }
                    totalWeight += weight;
                }
            }

            if (docVector != null && totalWeight > 0) {
                for (int d = 0; d < docVector.length; d++) {
                    docVector[d] /= totalWeight;
                }
            } else if (docVector == null) {
                docVector = new double[300];
            }

            return docVector;
        }
    }

    /** FINAL STEP **/
    /**
     * Description: Main entry point for calculating the similarity matrix for a list of requirements.
     * Trims word vector models if necessary, initializes weightings and embeddings, and executes BM25+ semantic calculation.
     * Example: processRequirements(requirements, Language.GERMAN) -> Returns SimilarityResult object
     *
     * @param requirements The list of preprocessed NLPResult requirements.
     * @param language The language enum for selecting vector models and base paths.
     * @return A SimilarityResult object containing the calculated matrix, embeddings, and weights.
     */
    public static SimilarityResult processRequirements(List<NLPResult> requirements, Language language) {
        String originalModelPath = language.baseModelDir + "/wiki." + language.langCode + ".vec";
        String trimmedModelPath = language.baseModelDir + "/wiki." + language.langCode + ".trimmed.vec";

        // Trim model to required words if not already done
        File trimmedFile = new File(trimmedModelPath);
        if (!trimmedFile.exists()) {
            trimVectorFile(originalModelPath, trimmedModelPath, requirements);
        }

        // Load word embeddings and calculate word weights
        WordEmbeddingProvider embeddings = new WordEmbeddingProvider(trimmedModelPath);
        WordWeighting weights = new WordWeighting(requirements);
        BM25PlusSimilarityCalculator calculator = new BM25PlusSimilarityCalculator();

        // Calculate similarity matrix based on the selected similarity measure and weighting
        double[][] similarityMatrix = calculator.calculateMatrix(requirements, weights, embeddings);

        return new SimilarityResult(similarityMatrix, embeddings, weights);
    }

    /** STEP 1 **/
    /**
     * Description: Trims an original Word2Vec vector file to keep only words present in the requirements list to optimize load time and memory usage.
     * Example: trimVectorFile("full.vec", "trimmed.vec", requirements)
     *
     * @param inputPath Path to the original full vector model file.
     * @param outputPath Destination path for the trimmed vector model file.
     * @param requirements List of requirements used to extract required vocabulary.
     */
    private static void trimVectorFile(String inputPath, String outputPath, List<NLPResult> requirements) {
        Set<String> requiredWords = new HashSet<>();

        for (NLPResult req : requirements) {
            if (req.lemmas != null) {
                for (String lemma : req.lemmas) {
                    if (lemma != null && !lemma.isBlank()) {
                        requiredWords.add(lemma.toLowerCase());
                    }
                }
            }
        }

        File inFile = new File(inputPath);
        File outFile = new File(outputPath);
        List<String> matchingLines = new ArrayList<>();
        int dimensions = 300;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header != null && header.contains(" ")) {
                String[] parts = header.split(" ");
                if (parts.length >= 2) {
                    try { dimensions = Integer.parseInt(parts[1].trim()); } catch (NumberFormatException ignored) {}
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                int spaceIdx = line.indexOf(' ');
                if (spaceIdx == -1) continue;

                String word = line.substring(0, spaceIdx).trim();
                if (requiredWords.contains(word)) {
                    matchingLines.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Original model could not be read: " + inputPath, e);
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8))) {
            writer.write(matchingLines.size() + " " + dimensions);
            writer.newLine();
            for (String line : matchingLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Trimmed model could not be written: " + outputPath, e);
        }
    }

    //TODO move to tester class
    //Test metohds
    public static void main(String[] args) {
        Language language = Language.GERMAN;

        System.out.println("======================================================");
        System.out.println("RUNNING BASELINE TEST (Full Matrix)");
        System.out.println("======================================================");
        runExistingTest(language);

        System.out.println("\n======================================================");
        System.out.println("RUNNING SCENARIO TESTS");
        System.out.println("======================================================");

        // 1. Test: Sentences that fit well together (high similarity expected, but not identical)
        runSpecificTest("TEST 1: High Similarity (Related but not identical)",
                createHighSimilarityData(), language);

        // 2. Test: Sentences that are identical (similarity ~ 1.0 expected)
        runSpecificTest("TEST 2: Identical Requirements",
                createIdenticalData(), language);

        // 3. Test: Sentences that are very different (lower similarity expected)
        runSpecificTest("TEST 3: Very Different Requirements",
                createVeryDifferentData(), language);

        // 4. Test: Sentences that do not fit together at all (similarity close to 0 expected)
        runSpecificTest("TEST 4: Completely Unrelated Requirements",
                createCompletelyUnrelatedData(), language);
    }

    // Description: Runs the original test with the full 30-item sample data set
    private static void runExistingTest(Language language) {
        List<NLPResult> requirements = createSampleData();
        SimilarityResult result = processRequirements(requirements, language);

        System.out.println("Calculated similarity matrix (first 5x5 entries):");
        for (int i = 0; i < Math.min(5, result.similarityMatrix.length); i++) {
            for (int j = 0; j < Math.min(5, result.similarityMatrix[i].length); j++) {
                System.out.printf(java.util.Locale.US, "%.4f  ", result.similarityMatrix[i][j]);
            }
            System.out.println();
        }
        System.out.println("Dimension of the entire matrix: " + result.similarityMatrix.length + "x" + result.similarityMatrix[0].length);
    }

    // Description: Helper method to run a specific test. Assumes the two test requirements are at index 0 and 1.
    private static void runSpecificTest(String testName, List<NLPResult> requirements, Language language) {
        SimilarityResult result = processRequirements(requirements, language);

        System.out.println("\n" + testName);
        System.out.println("Req A: " + String.join(" ", requirements.get(0).tokens));
        System.out.println("Req B: " + String.join(" ", requirements.get(1).tokens));

        // Matrix ist 32x32 groß, aber wir wollen nur den Vergleich der ersten beiden Sätze wissen.
        System.out.printf(java.util.Locale.US, "--> Similarity Score: %.4f\n", result.similarityMatrix[0][1]);
    }

    // Following Tests: Places the 2 test sentences at the start, followed by the 30 background requirements to stabilize IDF/BM25.

     // Test Data 1: High Similarity.
    private static List<NLPResult> createHighSimilarityData() {
        List<NLPResult> list = new ArrayList<>();
        list.add(new NLPResult(new String[]{"Die", "Steuerung", "öffnet", "das", "Fenster"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN"}, new String[]{"der", "steuerung", "öffnen", "der", "fenster"}));
        list.add(new NLPResult(new String[]{"Der", "Benutzer", "schließt", "das", "Fenster", "manuell"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN", "ADJD"}, new String[]{"der", "benutzer", "schließen", "der", "fenster", "manuell"}));
        list.addAll(createSampleData());
        return list;
    }

    //Test Data 2: Identical.
    private static List<NLPResult> createIdenticalData() {
        List<NLPResult> list = new ArrayList<>();
        list.add(new NLPResult(new String[]{"Ein", "defekter", "Sensor", "löst", "Alarm", "aus"}, new String[]{"ART", "ADJA", "NN", "VVFIN", "NN", "PTKVZ"}, new String[]{"ein", "defekt", "sensor", "lösen", "alarm", "aus"}));
        list.add(new NLPResult(new String[]{"Ein", "defekter", "Sensor", "löst", "Alarm", "aus"}, new String[]{"ART", "ADJA", "NN", "VVFIN", "NN", "PTKVZ"}, new String[]{"ein", "defekt", "sensor", "lösen", "alarm", "aus"}));
        list.addAll(createSampleData());
        return list;
    }

    // Test Data 3: Very Different.

    private static List<NLPResult> createVeryDifferentData() {
        List<NLPResult> list = new ArrayList<>();
        list.add(new NLPResult(new String[]{"Das", "System", "prüft", "die", "Anmeldung"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN"}, new String[]{"der", "system", "prüfen", "der", "anmeldung"}));
        list.add(new NLPResult(new String[]{"Ein", "defekter", "Sensor", "löst", "Alarm", "aus"}, new String[]{"ART", "ADJA", "NN", "VVFIN", "NN", "PTKVZ"}, new String[]{"ein", "defekt", "sensor", "lösen", "alarm", "aus"}));
        list.addAll(createSampleData());
        return list;
    }

    // Test Data 4: Completely Unrelated.
    private static List<NLPResult> createCompletelyUnrelatedData() {
        List<NLPResult> list = new ArrayList<>();
        list.add(new NLPResult(new String[]{"Das", "Passwort", "des", "Benutzers", "ist", "sicher"}, new String[]{"ART", "NN", "ART", "NN", "VAFIN", "ADJD"}, new String[]{"der", "passwort", "der", "benutzer", "sein", "sicher"}));
        list.add(new NLPResult(new String[]{"Temperatur", "wird", "vom", "Sensor", "gemessen"}, new String[]{"NN", "VAFIN", "APPRART", "NN", "VVPP"}, new String[]{"temperatur", "werden", "von", "sensor", "messen"}));
        list.addAll(createSampleData());
        return list;
    }

    // Description: Generates the background corpus of 30 NLPResult requirements for stable calculations.
    private static List<NLPResult> createSampleData() {
        List<NLPResult> list = new ArrayList<>();

        list.add(new NLPResult(new String[]{"Die", "Tür", "öffnet"}, new String[]{"ART", "NN", "VVFIN"}, new String[]{"der", "tür", "öffnen"}));
        list.add(new NLPResult(new String[]{"Das", "System", "schließt", "die", "Tür"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN"}, new String[]{"der", "system", "schließen", "der", "tür"}));
        list.add(new NLPResult(new String[]{"Ein", "Benutzer", "kann", "die", "Tür", "entriegeln"}, new String[]{"ART", "NN", "VMFIN", "ART", "NN", "VVINF"}, new String[]{"ein", "benutzer", "können", "der", "tür", "entriegeln"}));
        list.add(new NLPResult(new String[]{"Die", "Steuerung", "überwacht", "die", "Tür"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN"}, new String[]{"der", "steuerung", "überwachen", "der", "tür"}));
        list.add(new NLPResult(new String[]{"Der", "Alarm", "meldet", "eine", "offene", "Tür"}, new String[]{"ART", "NN", "VVFIN", "ART", "ADJA", "NN"}, new String[]{"der", "alarm", "melden", "ein", "offen", "tür"}));

        list.add(new NLPResult(new String[]{"Das", "Fenster", "bleibt", "geschlossen"}, new String[]{"ART", "NN", "VVFIN", "ADJD"}, new String[]{"der", "fenster", "bleiben", "schließen"}));
        list.add(new NLPResult(new String[]{"Die", "Steuerung", "öffnet", "das", "Fenster"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN"}, new String[]{"der", "steuerung", "öffnen", "der", "fenster"}));
        list.add(new NLPResult(new String[]{"Sensoren", "prüfen", "das", "Fenster"}, new String[]{"NN", "VVFIN", "ART", "NN"}, new String[]{"sensor", "prüfen", "der", "fenster"}));
        list.add(new NLPResult(new String[]{"Ein", "Alarm", "warnt", "bei", "gebrochenem", "Fenster"}, new String[]{"ART", "NN", "VVFIN", "APPR", "ADJA", "NN"}, new String[]{"ein", "alarm", "warnen", "bei", "brechen", "fenster"}));
        list.add(new NLPResult(new String[]{"Der", "Benutzer", "schließt", "das", "Fenster", "manuell"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN", "ADJD"}, new String[]{"der", "benutzer", "schließen", "der", "fenster", "manuell"}));

        list.add(new NLPResult(new String[]{"Der", "Benutzer", "meldet", "sich", "an"}, new String[]{"ART", "NN", "VVFIN", "PRF", "PTKVZ"}, new String[]{"der", "benutzer", "melden", "sich", "an"}));
        list.add(new NLPResult(new String[]{"Das", "System", "prüft", "die", "Anmeldung"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN"}, new String[]{"der", "system", "prüfen", "der", "anmeldung"}));
        list.add(new NLPResult(new String[]{"Fehlerhafte", "Anmeldung", "blockiert", "den", "Benutzer"}, new String[]{"ADJA", "NN", "VVFIN", "ART", "NN"}, new String[]{"fehlerhaft", "anmeldung", "blockieren", "der", "benutzer"}));
        list.add(new NLPResult(new String[]{"Der", "Administrator", "verwaltet", "Benutzer"}, new String[]{"ART", "NN", "VVFIN", "NN"}, new String[]{"der", "administrator", "verwalten", "benutzer"}));
        list.add(new NLPResult(new String[]{"Das", "Passwort", "des", "Benutzers", "ist", "sicher"}, new String[]{"ART", "NN", "ART", "NN", "VAFIN", "ADJD"}, new String[]{"der", "passwort", "der", "benutzer", "sein", "sicher"}));

        list.add(new NLPResult(new String[]{"Die", "Sensoren", "senden", "Daten"}, new String[]{"ART", "NN", "VVFIN", "NN"}, new String[]{"der", "sensor", "senden", "daten"}));
        list.add(new NLPResult(new String[]{"Das", "System", "verarbeitet", "Sensordaten"}, new String[]{"ART", "NN", "VVFIN", "NN"}, new String[]{"der", "system", "verarbeiten", "sensordaten"}));
        list.add(new NLPResult(new String[]{"Temperatur", "wird", "vom", "Sensor", "gemessen"}, new String[]{"NN", "VAFIN", "APPRART", "NN", "VVPP"}, new String[]{"temperatur", "werden", "von", "sensor", "messen"}));
        list.add(new NLPResult(new String[]{"Die", "Regelung", "basiert", "auf", "Sensoren"}, new String[]{"ART", "NN", "VVFIN", "APPR", "NN"}, new String[]{"der", "regelung", "basieren", "auf", "sensor"}));
        list.add(new NLPResult(new String[]{"Ein", "defekter", "Sensor", "löst", "Alarm", "aus"}, new String[]{"ART", "ADJA", "NN", "VVFIN", "NN", "PTKVZ"}, new String[]{"ein", "defekt", "sensor", "lösen", "alarm", "aus"}));

        list.add(new NLPResult(new String[]{"Das", "Netzwerk", "fällt", "aus"}, new String[]{"ART", "NN", "VVFIN", "PTKVZ"}, new String[]{"der", "netzwerk", "fallen", "aus"}));
        list.add(new NLPResult(new String[]{"Das", "System", "baut", "eine", "Verbindung", "auf"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN", "PTKVZ"}, new String[]{"der", "system", "bauen", "ein", "verbindung", "auf"}));
        list.add(new NLPResult(new String[]{"Die", "Verbindung", "ist", "verschlüsselt"}, new String[]{"ART", "NN", "VAFIN", "ADJD"}, new String[]{"der", "verbindung", "sein", "verschlüsseln"}));
        list.add(new NLPResult(new String[]{"Daten", "werden", "über", "das", "Netzwerk", "gesendet"}, new String[]{"NN", "VAFIN", "APPR", "ART", "NN", "VVPP"}, new String[]{"daten", "werden", "über", "der", "netzwerk", "senden"}));
        list.add(new NLPResult(new String[]{"Ein", "Netzwerkfehler", "wird", "protokolliert"}, new String[]{"ART", "NN", "VAFIN", "VVPP"}, new String[]{"ein", "netzwerkfehler", "werden", "protokollieren"}));

        list.add(new NLPResult(new String[]{"Das", "Licht", "schaltet", "sich", "ein"}, new String[]{"ART", "NN", "VVFIN", "PRF", "PTKVZ"}, new String[]{"der", "licht", "schalten", "sich", "ein"}));
        list.add(new NLPResult(new String[]{"Die", "Steuerung", "regelt", "die", "Helligkeit"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN"}, new String[]{"der", "steuerung", "regeln", "der", "helligkeit"}));
        list.add(new NLPResult(new String[]{"Der", "Benutzer", "dimmt", "das", "Licht"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN"}, new String[]{"der", "benutzer", "dimmen", "der", "licht"}));
        list.add(new NLPResult(new String[]{"Ein", "Timer", "deaktiviert", "das", "Licht"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN"}, new String[]{"ein", "timer", "deaktivieren", "der", "licht"}));
        list.add(new NLPResult(new String[]{"Das", "System", "meldet", "einen", "Lampendefekt"}, new String[]{"ART", "NN", "VVFIN", "ART", "NN"}, new String[]{"der", "system", "melden", "ein", "lampendefekt"}));

        return list;
    }
}