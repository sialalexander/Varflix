package variabilityMining;

/**
 * Enum defining supported languages and their file paths for NLP models and stopwords.
 */
public enum Language {
    ENGLISH("en", "requirementMining/models/en-pos-maxent.bin",
            "requirementMining/models/en-lemmatizer.bin",
            "requirementMining/stopwords/stopwords-en.json"),

    GERMAN("de", "requirementMining/models/de-pos-maxent.bin",
            "requirementMining/models/de-lemmatizer.bin",
            "requirementMining/stopwords/stopwords-de.json");

    public final String langCode;
    public final String posModelPath;
    public final String lemmaModelPath;
    public final String stopwordsPath;

    Language(String langCode, String posModelPath, String lemmaModelPath, String stopwordsPath) {
        this.langCode = langCode;
        this.posModelPath = posModelPath;
        this.lemmaModelPath = lemmaModelPath;
        this.stopwordsPath = stopwordsPath;
    }
}