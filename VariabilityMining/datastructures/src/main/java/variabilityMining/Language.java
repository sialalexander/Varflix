package variabilityMining;

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