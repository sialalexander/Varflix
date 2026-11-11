package requirementMining;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import variabilityMining.Language;
import requirementMining.helper.NLPResult;
import requirementMining.helper.SimilarityResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SimilarityNetworkCalculator {

    //This class provides methods to load and process word embeddings. It uses the Word2Vec model from DeepLearning4j.
    public static class WordEmbeddingProvider {
        private final Word2Vec word2Vec;

        public WordEmbeddingProvider(String modelPath) {
            this.word2Vec = WordVectorSerializer.readWord2VecModel(new File(modelPath));
        }

        public double[] getVector(String word) {
            if (word2Vec.hasWord(word)) {
                return word2Vec.getWordVector(word);
            }
            return new double[word2Vec.getLayerSize()];
        }

        public boolean hasWord(String word) {
            return word2Vec.hasWord(word);
        }

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

    //This class calculates the word weights based on the document frequency and syntactic information. It uses the IDF (inverse document frequency) formula.
    public static class WordWeighting {
        private final Map<String, Double> idfMap = new HashMap<>();

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

        //This method calculates the weight for a given word based on its document frequency and syntactic information.
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

    //this class calculates the similarity matrix using BM25Plus and semantic similarity
    private static class BM25PlusSimilarityCalculator {
        private final double k1 = 1.5;
        private final double b = 0.75;
        private final double delta = 1.0;

        //This method calculates the similarity matrix using BM25Plus and semantic similarity.
        //It first calculates the raw BM25 scores for each pair of requirements, and then normalizes and averages them with semantic similarity.
        // 1.0 is the maximum value of the normalized BM25 scores.
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
                score += idfWeight * ((numerator / denominator) + delta);
            }
            return score;
        }

        private double computeSemanticScore(NLPResult reqA, NLPResult reqB, WordWeighting weighting, WordEmbeddingProvider embeddings) {
            double[] vecA = computeWeightedDocumentVector(reqA, weighting, embeddings);
            double[] vecB = computeWeightedDocumentVector(reqB, weighting, embeddings);
            return embeddings.calculateCosineSimilarity(vecA, vecB);
        }

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

    // Most important method for calculating the similarity matrix.
    public static SimilarityResult processRequirements(List<NLPResult> requirements, Language language, String baseModelDir) {
        // Determine language code and model paths
        String langCode = (language == Language.GERMAN) ? "de" : "en";
        String originalModelPath = baseModelDir + "/wiki." + langCode + ".vec";
        String trimmedModelPath = baseModelDir + "/wiki." + langCode + ".trimmed.vec";

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

    // This method removes all words from the original model that are not present in the requirements list.
    //This optimizes the loading time of the model and reduces the memory footprint.
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


    //TODO Remove when done with implementation
    // Method only for testing the methods
    public static void main(String[] args) {
        List<NLPResult> requirements = createSampleData();

        Language language = Language.GERMAN;
        String baseModelDir = "VariabilityMining\\varmining\\src\\main\\resources\\requirementMining\\models";

        SimilarityResult result =
                processRequirements(requirements, language, baseModelDir);

        System.out.println("Calculated similarity matrix (first 5x5 entries):");
        for (int i = 0; i < Math.min(5, result.similarityMatrix.length); i++) {
            for (int j = 0; j < Math.min(5, result.similarityMatrix[i].length); j++) {
                System.out.printf(java.util.Locale.US, "%.4f  ", result.similarityMatrix[i][j]);
            }
            System.out.println();
        }

        System.out.println("\nDimension of the entire matrix: " + result.similarityMatrix.length + "x" + result.similarityMatrix[0].length);
    }

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