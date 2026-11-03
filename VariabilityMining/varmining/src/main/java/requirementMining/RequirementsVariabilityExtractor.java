package requirementMining;

import java.util.List;
import variabilityMining.IVariabilityExtractor;
import varflixModel.IVariability;
import varflixModel.IVariant;
import varflixModel.IVariabilityGroup;

public class RequirementsVariabilityExtractor <V extends IVariant, E extends IVariability>
        implements IVariabilityExtractor<V, E> {

    @Override
    public List<IVariabilityGroup<V, E>> performAutomaticMining(String variantPath, String inputPath) {
        //TODO sialalex
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
}