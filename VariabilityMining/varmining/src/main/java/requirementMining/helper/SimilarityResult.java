package requirementMining.helper;

import requirementMining.SimilarityNetworkCalculator.WordEmbeddingProvider;
import requirementMining.SimilarityNetworkCalculator.WordWeighting;

//TODO -> move to data structure
public class SimilarityResult {
    public final double[][] similarityMatrix;
    public final WordEmbeddingProvider embeddings;
    public final WordWeighting weights;

    public SimilarityResult(double[][] similarityMatrix, WordEmbeddingProvider embeddings, WordWeighting weights) {
        this.similarityMatrix = similarityMatrix;
        this.embeddings = embeddings;
        this.weights = weights;
    }
}