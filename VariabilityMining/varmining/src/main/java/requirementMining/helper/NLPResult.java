package requirementMining.helper;

import java.util.Arrays;

/**
 * Data structure storing NLP tokens, POS tags, and lemmas.
 */
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