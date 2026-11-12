package variabilityMining;

/**
 * Enum defining supported languages and their file paths for NLP models, stopwords, and the base model directory.
 */
public enum Language {
    ENGLISH("en", "requirementMining/models/en-pos-maxent.bin",
            "requirementMining/models/en-lemmatizer.bin",
            "requirementMining/stopwords/stopwords-en.json",
            "VariabilityMining\\varmining\\src\\main\\resources\\requirementMining\\models"),

    GERMAN("de", "requirementMining/models/de-pos-maxent.bin",
            "requirementMining/models/de-lemmatizer.bin",
            "requirementMining/stopwords/stopwords-de.json",
            "VariabilityMining\\varmining\\src\\main\\resources\\requirementMining\\models");

    public final String langCode;
    public final String posModelPath;
    public final String lemmaModelPath;
    public final String stopwordsPath;
    public final String baseModelDir;

    Language(String langCode, String posModelPath, String lemmaModelPath, String stopwordsPath, String baseModelDir) {
        this.langCode = langCode;
        this.posModelPath = posModelPath;
        this.lemmaModelPath = lemmaModelPath;
        this.stopwordsPath = stopwordsPath;
        this.baseModelDir = baseModelDir;
    }
}