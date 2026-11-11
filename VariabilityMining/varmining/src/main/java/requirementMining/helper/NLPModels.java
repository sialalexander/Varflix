package requirementMining.helper;

import opennlp.tools.lemmatizer.Lemmatizer;
import opennlp.tools.postag.POSTaggerME;

/**
 * Helper class to hold loaded NLP models.
 */
public class NLPModels {
    public POSTaggerME posTagger;
    public Lemmatizer lemmatizer;

    public NLPModels(POSTaggerME posTagger, Lemmatizer lemmatizer) {
        this.posTagger = posTagger;
        this.lemmatizer = lemmatizer;
    }
}