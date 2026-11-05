package variabilityMining;

import java.util.Arrays;

// TODO -> maybe move to datastructures???
// Natural Language Processing - Result
// Tokens (words) with tags (which type) and lemmas (normalized form)
public class NLPResult {
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