Requirement Preprocessor

-------------------------------------
External resources
Library 
org.apache.opennlp - opennlp-tools (version 2.5.12)
Used for Removing Stop words and lemmatisation


com.fasterxml.jackson.core jackson-databind(verison 2.15.2)



-------------------------------------
Stop Word List
German and Elgisch

The lsits are just copoied into the resource folder
German: https://github.com/stopwords-iso/stopwords-de
English: https://github.com/stopwords-iso/stopwords-en

Für die korrekte Analyse von Softwareanforderungen wurden Standard-Stopwortlisten bereinigt. Bestimmte Wörter dürfen **nicht** entfernt werden, da sonst die Logik, Verbindlichkeit und der Fachkontext der Requirements zerstört werden.

Folgende Wortgruppen wurden aus den JSON-Dateien (`stopwords-de.json` / `stopwords-en.json`) entfernt:
* **Modalverben (Verbindlichkeit & Priorität):**
    * *DE:* muss, musst, müssen, soll, sollen, sollte, kann, können, darf, dürfen, wird, werden, würde
    * *EN:* shall, must, should, can, cannot, could, may, might, will, won't, would, ought
* **Negationen (Logik-Erhaltung):**
    * *DE:* nicht, nichts, kein, keine, keinem, keinen, keiner, keines, nie, niemand
    * *EN:* not, no, none, nothing, neither, nor, never
* **Domänenspezifische Verben & Substantive (Software Engineering):**
    * *DE:* machen, tun, recht, ordnung
    * *EN:* system, test, describe, need, specify, use, value, problem, information, results



-------------------------------------
### Verwendete NLP-Modelle (`src/main/resources/models/`)
Die für das POS-Tagging (Part of SPeech) und die Lemmatisierung benötigten Binärdateien stammen von den offiziellen Open-Source-Modellen des **Apache OpenNLP**-Projekts:
* **Deutsch:**
    * `de-pos-maxent.bin` (POS-Modell)
    * `de-lemmatizer.bin` (Lemmatizer-Modell)
* **Englisch:**
    * `en-pos-maxent.bin` (POS-Modell)
    * `en-lemmatizer.bin` (Lemmatizer-Modell)

**Quelle / Downloads:** [Apache OpenNLP Models Documentation & Download](https://opennlp.apache.org/models.html)
https://central.sonatype.com/artifact/org.apache.opennlp/opennlp-models-lemmatizer-de
https://central.sonatype.com/artifact/org.apache.opennlp/opennlp-models-pos-de
https://central.sonatype.com/artifact/org.apache.opennlp/opennlp-models-lemmatizer-en
https://central.sonatype.com/artifact/org.apache.opennlp/opennlp-models-pos-en
Verison 1.3.0