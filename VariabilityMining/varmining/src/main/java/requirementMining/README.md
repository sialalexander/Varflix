# Requirement Preprocessor

## External Resources

### Libraries
- `org.apache.opennlp` - `opennlp-tools` (version 2.5.12)
    - Used for removing stop words and lemmatization
- `com.fasterxml.jackson.core` - `jackson-databind` (version 2.15.2)

### Stop Word List
German and English

The lists are copied into the resources folder:
- German: https://github.com/stopwords-iso/stopwords-de
- English: https://github.com/stopwords-iso/stopwords-en

For the correct analysis of software requirements, standard stop word lists were cleaned. Certain words must **not** be removed, as otherwise the logic, binding nature, and domain context of the requirements would be destroyed.

The following word groups were removed from the JSON files (`stopwords-de.json` / `stopwords-en.json`):

- **Modal Verbs (Binding Nature & Priority):**
    - **DE:** muss, musst, müssen, soll, sollen, sollte, kann, können, darf, dürfen, wird, werden, würde
    - **EN:** shall, must, should, can, cannot, could, may, might, will, won't, would, ought

- **Negations (Logic Preservation):**
    - **DE:** nicht, nichts, kein, keine, keinem, keinen, keiner, keines, nie, niemand
    - **EN:** not, no, none, nothing, neither, nor, never

- **Domain-Specific Verbs & Nouns (Software Engineering):**
    - **DE:** machen, tun, recht, ordnung
    - **EN:** system, test, describe, need, specify, use, value, problem, information, results

### NLP Models Used (`src/main/resources/models/`)

The binary files required for POS tagging (Part of Speech) and lemmatization come from the official open-source models of the **Apache OpenNLP** project:

- **German:**
    - `de-pos-maxent.bin` (POS model)
    - `de-lemmatizer.bin` (Lemmatizer model)

- **English:**
    - `en-pos-maxent.bin` (POS model)
    - `en-lemmatizer.bin` (Lemmatizer model)

**Source / Downloads:**
- [Apache OpenNLP Models Documentation & Download](https://opennlp.apache.org/models.html)
- https://central.sonatype.com/artifact/org.apache.opennlp/opennlp-models-lemmatizer-de
- https://central.sonatype.com/artifact/org.apache.opennlp/opennlp-models-pos-de
- https://central.sonatype.com/artifact/org.apache.opennlp/opennlp-models-lemmatizer-en
- https://central.sonatype.com/artifact/org.apache.opennlp/opennlp-models-pos-en
- Version 1.3.0


# Similariy Network Calculuator
TODO -> add infos about vec files
TODO -> add used libraries
....