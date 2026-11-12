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


# Similarity Network Calculator

## External Resources

### Pre-trained Word Vectors (`fastText`)
German and English pre-trained wiki vectors are required for calculating similarity networks.

- **Documentation & Info:** [fastText Pretrained Vectors](https://fasttext.cc/docs/en/pretrained-vectors.html)

Due to their large file size, the vector files cannot be included directly in the repository and are excluded via `.gitignore`. They must be downloaded and added manually to the resources folder after cloning.

### Vector Downloads & Setup
1. Download the zip archives:
    - **German:** https://dl.fbaipublicfiles.com/fasttext/vectors-wiki/wiki.de.zip
    - **English:** https://dl.fbaipublicfiles.com/fasttext/vectors-wiki/wiki.en.zip
2. Extract the downloaded zip files.
3. Place the extracted `.vec` files into the designated resources folder (`models`).

### Runtime Vector Optimization (`wiki.xx.trimmed.vec`)
During the application execution, a shortened version (`wiki.xx.trimmed.vec`) is automatically created.
- **Purpose:** The vector space is trimmed down to only contain words relevant to the dataset for each run.
- **Benefit:** Significantly improves performance and reduces memory consumption during similarity calculations.