package requirementMining.test;

import requirementMining.SimilarityNetworkCalculator;
import requirementMining.helper.NLPResult;
import requirementMining.helper.SimilarityResult;
import variabilityMining.Language;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static requirementMining.RequirementPreprocessor.*;

public class RequirementsVariabilityExtractorTester {

    static Language lang = Language.GERMAN;

    // Word pool for guaranteed different (unrelated) test sentences
    private static final String[] DIFF_NOUNS = {
            "hund", "auto", "sonne", "kaffee", "baum", "flugzeug", "uhr", "computer", "buch", "fluss",
            "katze", "kuchen", "telefon", "regen", "bild", "lampe", "stuhl", "glas", "musik", "wind",
            "feuer", "pflanze", "vogel", "schiff", "strasse", "apfel", "kind", "maus", "berg", "schuh"
    };

    private static final String[] DIFF_VERBS = {
            "bellen", "bremsen", "scheinen", "dampfen", "wachsen", "landen", "ticken", "rechnen", "liegen", "fliessen",
            "schlafen", "duften", "klingeln", "fallen", "haengen", "leuchten", "wackeln", "brechen", "spielen", "wehen",
            "brennen", "bluehen", "singen", "schwimmen", "trocknen", "reifen", "lachen", "rennen", "stehen", "druecken"
    };

    /**
     * Description: Main entry point to test individual components separately as well as the complete requirements variability extraction pipeline.
     */
    public static void main(String[] args) {
        // Basic Requirements Preprocessing Test (Original test)
        Map<String, NLPResult> preproReqs = requirementPreprocessor_basic_test(getSampleRequirements());

        System.out.println("\n");

        // Similarity Network Calculator Tests
        testSimilarityNetworkCalculator();
    }

    private static Map<String, NLPResult> requirementPreprocessor_basic_test(Map<String, String> requirements){
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Start [requirementPreprocessor_basic_test]");

        // Call the main method (set to GERMAN for testing purposes)
        Map<String, NLPResult> finalResults = preprocess(requirements, lang);

        // Print the results
        for (Map.Entry<String, NLPResult> entry : finalResults.entrySet()) {
            System.out.println("ID: " + entry.getKey());
            System.out.println(entry.getValue().toString());
        }

        System.out.println("End [requirementPreprocessor_basic_test]");
        System.out.println("-------------------------------------------------------------------------");
        return finalResults;
    }

    private static Map<String, String> getSampleRequirements(){
        Map<String, String> sampleRequirements = new LinkedHashMap<>();

        sampleRequirements.put("REQ-01", "Beim Ueberschreiten einer definierten Geschwindigkeit, werden saemtliche Tueren abgeschlossen, wenn sie nicht abgeschlossen sind.");
        sampleRequirements.put("REQ-02", "Beim Ueberschreiten einer definierten Geschwindigkeit passiert nichts, wenn die Tueren bereits manuell abgeschlossen wurden.");
        sampleRequirements.put("REQ-03", "Wird die Geschwindigkeit verringert, sodass sie den definierten Wert unterschreitet und wurden die Tueren durch das Automatic Locking Feature abgeschlossen, werden die Tueren aufgeschlossen.");
        sampleRequirements.put("REQ-04", "Wird die Geschwindigkeit verringert, sodass sie den definierten Wert unterschreitet und wurden die Tueren nicht durch das Automatic Locking Feature, sondern zuvor manuell abgeschlossen, werden die Tueren nicht aufgeschlossen.");
        sampleRequirements.put("REQ-05", "Druecken auf den Knopf zum Oeffnen des Fensters fuehrt dazu, dass das Fenster komplett geoeffnet wird.");
        sampleRequirements.put("REQ-06", "Druecken auf den Knopf zum Schliessen des Fensters fuehrt dazu, dass das Fenster komplett geschlossen wird.");
        sampleRequirements.put("REQ-07", "Wird das Fenster gerade geoeffnet, fuehrt Druecken der Taste zum Schliessen dazu, dass das Fenster anhaelt.");
        sampleRequirements.put("REQ-08", "Wird das Fenster gerade geoeffnet, passiert durch Druecken der Taste zum Oeffnen nichts.");
        sampleRequirements.put("REQ-09", "Wird das Fenster gerade geschlossen, fuehrt Druecken der Taste zum Oeffnen dazu, dass das Fenster anhaelt.");
        sampleRequirements.put("REQ-10", "Wird das Fenster gerade geschlossen, passiert durch Druecken der Taste zum Schliessen nichts.");
        sampleRequirements.put("REQ-11", "Ist das Fenster komplett geoeffnet, hat das Druecken der Taste zum Oeffnen keinen Effekt.");
        sampleRequirements.put("REQ-12", "Ist das Fenster komplett geschlossen, hat das Druecken der Taste zum Schliessen keinen Effekt.");
        sampleRequirements.put("REQ-13", "Gewaltsames Oeffnen einer Tuer fuehrt bei aktiver Alarmanlage zum Ausloesen von Alarm.");
        sampleRequirements.put("REQ-14", "Gewaltsames Oeffnen einer Tuer fuehrt bei deaktivierter Alarmanlage nicht zum Ausloesen von Alarm.");
        sampleRequirements.put("REQ-15", "Laufender Alarm wird, falls LEDs vorhanden sind, durch Aufleuchten einer LED signalisiert.");
        sampleRequirements.put("REQ-16", "Alarm wird automatisch nach 20 Sekunden beendet, woraufhin, falls LEDs vorhanden sind, eine Mitteilung ueber diese angezeigt wird.");
        sampleRequirements.put("REQ-17", "Alarm kann durch das Aufschliessen des Fahrzeugs deaktiviert werden. Dies fuehrt, wenn LEDs vorhanden sind, nicht zu einer Mitteilung.");
        sampleRequirements.put("REQ-18", "Durch Druck auf den entsprechenden Knopf auf der Fernbedienung, wird die Alarmanlage aktiviert, wenn sie zuvor inaktiv war.");
        sampleRequirements.put("REQ-19", "Ist die Alarmanlage aktiv, has das Druecken der Fernbedienungstaste zum Aktivieren keinen Effekt.");
        sampleRequirements.put("REQ-20", "Durch Druck auf den entsprechenden Knopf auf der Fernbedienung, wird die Alarmanlage deaktiviert, wenn sie zuvor aktiv war.");
        sampleRequirements.put("REQ-21", "Ist die Alarmanlage nicht aktiv, hat das Druecken der Fernbedienungstaste zum Deaktivieren keinen Effekt.");
        sampleRequirements.put("REQ-22", "Druck auf den Fernbedienungsknopf zum Oeffnen des Fensters laesst das Fenster zum Oeffnen nach unten fahren.");
        sampleRequirements.put("REQ-23", "Druck auf den Fernbedienungsknopf zum Schliessen des Fensters laesst das Fenster zum Schliessen nach oben fahren.");
        sampleRequirements.put("REQ-24", "Wird das Fenster gerade geoeffnet, fuehrt Druecken der Fernbedienungstaste zum Schliessen dazu, dass das Fenster anhaelt.");
        sampleRequirements.put("REQ-25", "Wird das Fenster gerade geoeffnet, passiert durch Druecken der Taste zum Oeffnen nichts.");
        sampleRequirements.put("REQ-26", "Wird das Fenster gerade geschlossen, fuehrt Druecken der Fernbedienungstaste zum Oeffnen dazu, dass das Fenster anhaelt.");
        sampleRequirements.put("REQ-27", "Wird das Fenster gerade geschlossen, passiert durch Druecken der Taste zum Schliessen nichts.");
        sampleRequirements.put("REQ-28", "Ist das Fenster komplett geoeffnet, hat das Druecken der Fernbedienungstaste zum Oeffnen keinen Effekt.");
        sampleRequirements.put("REQ-29", "Ist das Fenster komplett geschlossen, hat das Druecken der Fernbedienungstaste zum Schliessen keinen Effekt.");
        sampleRequirements.put("REQ-30", "Bei Druck auf den Verriegelungsknopf und inaktiver Zentralverriegelung wird die Zentralverriegelung aktiviert, wodurch alle Tueren abgeschlossen werden.");
        sampleRequirements.put("REQ-31", "Bei Druck auf den Verriegelungsknopf und aktiver Zentralverriegelung wird keine Aktion ausgeloest.");
        sampleRequirements.put("REQ-32", "Bei Druck auf den Entriegelungsknopf und aktiver Zentralverriegelung wird die Zentralverriegelung deaktiviert, wodurch alle Tueren aufgeschlossen werden.");
        sampleRequirements.put("REQ-33", "Bei Druck auf den Entriegelungsknopf und inaktiver Zentralverriegelung wird keine Aktion ausgeloest.");
        sampleRequirements.put("REQ-34", "Durch Abschliessen einer Tuer bei inaktiver Zentralverriegelung wird die Zentralverriegelung aktiviert, wodurch alle Tueren abgeschlossen werden.");
        sampleRequirements.put("REQ-35", "Durch Abschliessen einer Tuer bei aktiver Zentralverriegelung wird keine Aktion ausgeloest.");
        sampleRequirements.put("REQ-36", "Durch Aufschliessen einer Tuer bei aktiver Zentralverriegelung wird die Zentralverriegelung deaktiviert, wodurch alle Tueren aufgeschlossen werden.");
        sampleRequirements.put("REQ-37", "Durch Aufschliessen einer Tuer bei inaktiver Zentralverriegelung wird keine Aktion ausgeloest.");
        sampleRequirements.put("REQ-38", "Bei Verriegelung des Fensters, inaktiver Zentralverriegelung und Existenz des automatischen Fensterhebers werden alle Fenster automatisch geschlossen, falls sie geoeffnet sind. Andernfalls werden sie blockiert.");
        sampleRequirements.put("REQ-39", "Bei Entriegelung des Fensters, aktiver Zentralverriegelung und Existenz des automatischen Fensterhebers wird die Blockierung aller Fenster aufgehoben.");
        sampleRequirements.put("REQ-40", "Bei Verriegelung des Fensters, inaktiver Zentralverriegelung und Existenz des manuellen Fensterhebers werden alle Fenster blockiert.");
        sampleRequirements.put("REQ-41", "Bei Entriegelung des Fensters, aktiver Zentralverriegelung und Existenz des manuellen Fensterhebers wird die Blockierung aller Fenster aufgehoben.");
        sampleRequirements.put("REQ-42", "Wenn der Spiegel eingeklappt ist, bewirkt das Druecken der zentralen Taste, dass der Spiegel ausgeklappt wird.");
        sampleRequirements.put("REQ-43", "Wenn der Spiegel ausgeklappt ist und das Fahrzeug steht, bewirkt das Druecken der zentralen Taste, dass der Spiegel eingeklappt wird.");
        sampleRequirements.put("REQ-44", "Wenn der Spiegel ausgeklappt ist und sich das Fahrzeug in Bewegung befindet, hat das Druecken der zentralen Taste keinen Effekt.");
        sampleRequirements.put("REQ-45", "Wenn die LED vorhanden ist und der Spiegel eingeklappt ist, leuchtet die LED.");
        sampleRequirements.put("REQ-46", "Wenn die LED vorhanden ist und der Spiegel ausgeklappt ist, leuchtet die LED nicht.");
        sampleRequirements.put("REQ-47", "Druck auf die Richtungstaste links des elektrischen Aussenspiegels bewirkt, dass sich der Aussenspiegel nach links bewegt. Dies funktioniert nur, wenn der Aussenspiegel Bewegungsspielraum nach links hat.");
        sampleRequirements.put("REQ-48", "Druck auf die Richtungstaste rechts des elektrischen Aussenspiegels bewirkt, dass sich der Aussenspiegel nach rechts bewegt. Dies funktioniert nur, wenn der Aussenspiegel Bewegungsspielraum nach rechts hat.");
        sampleRequirements.put("REQ-49", "Druck auf die Richtungstaste oben des elektrischen Aussenspiegels bewirkt, dass sich der Aussenspiegel nach oben bewegt. Dies funktioniert nur, wenn der Aussenspiegel Bewegungsspielraum nach oben hat.");
        sampleRequirements.put("REQ-50", "Druck auf die Richtungstaste unten des elektrischen Aussenspiegels bewirkt, dass sich der Aussenspiegel nach unten bewegt. Dies funktioniert nur, wenn der Aussenspiegel Bewegungsspielraum nach unten hat.");
        return sampleRequirements;
    }


    /* =========================================================================
     * SIMILARITY NETWORK CALCULATOR TEST METHODS
     * ========================================================================= */

    private static void testSimilarityNetworkCalculator() {
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Start [SimilarityNetworkCalculator_Baseline_Test]");
        runExistingTest(lang);
        System.out.println("End [SimilarityNetworkCalculator_Baseline_Test]");
        System.out.println("-------------------------------------------------------------------------");

        System.out.println("\n-------------------------------------------------------------------------");
        System.out.println("Start [SimilarityNetworkCalculator_Scenario_Pairs]");
        runSpecificTest("TEST 1: High Similarity (Related but not identical)", createHighSimilarityData(), lang);
        runSpecificTest("TEST 2: Identical Requirements", createIdenticalData(), lang);
        runSpecificTest("TEST 3: Very Different Requirements", createVeryDifferentData(), lang);
        runSpecificTest("TEST 4: Completely Unrelated Requirements", createCompletelyUnrelatedData(), lang);
        System.out.println("End [SimilarityNetworkCalculator_Scenario_Pairs]");
        System.out.println("-------------------------------------------------------------------------");

        System.out.println("\n-------------------------------------------------------------------------");
        System.out.println("Start [SimilarityNetworkCalculator_Matrix_Analysis]");
        runMatrixAnalysisTest("TEST 5: Large Set - All completely different (expected: very low values)",
                createAllDifferentData(), lang, -1);

        runMatrixAnalysisTest("TEST 6: Large Set - All different, 4 similar outliers at the end (expected: high values at index 26-29)",
                createDifferentWithSimilarOutliers(), lang, 26);

        runMatrixAnalysisTest("TEST 7: Large Set - All very similar (expected: very high values/1.00 everywhere)",
                createAllSimilarData(), lang, -1);

        runMatrixAnalysisTest("TEST 8: Large Set - All very similar, 4 different outliers at the end (expected: isolated outliers with low values)",
                createSimilarWithDifferentOutliers(), lang, 26);
        System.out.println("End [SimilarityNetworkCalculator_Matrix_Analysis]");
        System.out.println("-------------------------------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // Execution Helpers
    // -------------------------------------------------------------------------

    private static void runExistingTest(Language language) {
        List<NLPResult> requirements = createSampleData();
        SimilarityResult result = SimilarityNetworkCalculator.processRequirements(requirements, language);

        System.out.println("Calculated similarity matrix (first 5x5 entries):");
        printMatrixSnippet(result.similarityMatrix, 0, 5, 0, 5);
        System.out.println("Dimension of the entire matrix: " + result.similarityMatrix.length + "x" + result.similarityMatrix[0].length);
    }

    private static void runSpecificTest(String testName, List<NLPResult> requirements, Language language) {
        SimilarityResult result = SimilarityNetworkCalculator.processRequirements(requirements, language);

        System.out.println("\n" + testName);
        System.out.println("Req A: " + String.join(" ", requirements.get(0).tokens));
        System.out.println("Req B: " + String.join(" ", requirements.get(1).tokens));
        System.out.printf(Locale.US, "--> Similarity Score: %.4f\n", result.similarityMatrix[0][1]);
    }

    private static void runMatrixAnalysisTest(String testName, List<NLPResult> requirements, Language language, int outlierStartIndex) {
        SimilarityResult result = SimilarityNetworkCalculator.processRequirements(requirements, language);
        double[][] matrix = result.similarityMatrix;
        int size = matrix.length;

        System.out.println("\n" + testName);
        System.out.println("Set size: " + size + " Requirements");

        // Calculate average similarity
        double sum = 0;
        int count = 0;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                sum += matrix[i][j];
                count++;
            }
        }
        System.out.printf(Locale.US, "Average similarity (excluding diagonal): %.4f\n", (sum / count));

        if (outlierStartIndex == -1) {
            System.out.println("Matrix snippet (5x5):");
            printMatrixSnippet(matrix, 0, 5, 0, 5);
        } else {
            System.out.println("Matrix snippet of the outliers among themselves (indices " + outlierStartIndex + " to " + (size-1) + "):");
            printMatrixSnippet(matrix, outlierStartIndex, size, outlierStartIndex, size);

            System.out.println("Comparison of an outlier (index " + outlierStartIndex + ") with the base set (indices 0 to 3):");
            printMatrixSnippet(matrix, outlierStartIndex, outlierStartIndex + 1, 0, 4);
        }
    }

    private static void printMatrixSnippet(double[][] matrix, int rowStart, int rowEnd, int colStart, int colEnd) {
        System.out.print("      ");
        for (int j = colStart; j < colEnd; j++) {
            System.out.printf(" [%02d] ", j);
        }
        System.out.println();
        for (int i = rowStart; i < rowEnd; i++) {
            System.out.printf("[%02d]  ", i);
            for (int j = colStart; j < colEnd; j++) {
                System.out.printf(Locale.US, "%.2f   ", matrix[i][j]);
            }
            System.out.println();
        }
    }

    // -------------------------------------------------------------------------
    // Data Generation Helpers
    // -------------------------------------------------------------------------

    /**
     * Helper method to easily create an NLPResult object from string literals, making test data readable.
     */
    private static NLPResult createNLP(String tokens, String tags, String lemmas) {
        return new NLPResult(tokens.split(" "), tags.split(" "), lemmas.split(" "));
    }

    private static List<NLPResult> createHighSimilarityData() {
        String reqA = "Die Steuerung öffnet das Fenster";
        String reqB = "Der Benutzer schließt das Fenster manuell";

        List<NLPResult> list = new ArrayList<>();
        list.add(createNLP(reqA, "ART NN VVFIN ART NN", "der steuerung öffnen der fenster"));
        list.add(createNLP(reqB, "ART NN VVFIN ART NN ADJD", "der benutzer schließen der fenster manuell"));
        list.addAll(createSampleData());
        return list;
    }

    private static List<NLPResult> createIdenticalData() {
        String reqA = "Ein defekter Sensor löst Alarm aus";
        String reqB = "Ein defekter Sensor löst Alarm aus";

        List<NLPResult> list = new ArrayList<>();
        list.add(createNLP(reqA, "ART ADJA NN VVFIN NN PTKVZ", "ein defekt sensor lösen alarm aus"));
        list.add(createNLP(reqB, "ART ADJA NN VVFIN NN PTKVZ", "ein defekt sensor lösen alarm aus"));
        list.addAll(createSampleData());
        return list;
    }

    private static List<NLPResult> createVeryDifferentData() {
        String reqA = "Das System prüft die Anmeldung";
        String reqB = "Ein defekter Sensor löst Alarm aus";

        List<NLPResult> list = new ArrayList<>();
        list.add(createNLP(reqA, "ART NN VVFIN ART NN", "der system prüfen der anmeldung"));
        list.add(createNLP(reqB, "ART ADJA NN VVFIN NN PTKVZ", "ein defekt sensor lösen alarm aus"));
        list.addAll(createSampleData());
        return list;
    }

    private static List<NLPResult> createCompletelyUnrelatedData() {
        String reqA = "Das Passwort des Benutzers ist sicher";
        String reqB = "Temperatur wird vom Sensor gemessen";

        List<NLPResult> list = new ArrayList<>();
        list.add(createNLP(reqA, "ART NN ART NN VAFIN ADJD", "der passwort der benutzer sein sicher"));
        list.add(createNLP(reqB, "NN VAFIN APPRART NN VVPP", "temperatur werden von sensor messen"));
        list.addAll(createSampleData());
        return list;
    }

    private static List<NLPResult> createSampleData() {
        List<NLPResult> list = new ArrayList<>();

        // Group 1: Door
        list.add(createNLP("Die Tür öffnet", "ART NN VVFIN", "der tür öffnen"));
        list.add(createNLP("Das System schließt die Tür", "ART NN VVFIN ART NN", "der system schließen der tür"));
        list.add(createNLP("Ein Benutzer kann die Tür entriegeln", "ART NN VMFIN ART NN VVINF", "ein benutzer können der tür entriegeln"));
        list.add(createNLP("Die Steuerung überwacht die Tür", "ART NN VVFIN ART NN", "der steuerung überwachen der tür"));
        list.add(createNLP("Der Alarm meldet eine offene Tür", "ART NN VVFIN ART ADJA NN", "der alarm melden ein offen tür"));

        // Group 2: Window
        list.add(createNLP("Das Fenster bleibt geschlossen", "ART NN VVFIN ADJD", "der fenster bleiben schließen"));
        list.add(createNLP("Die Steuerung öffnet das Fenster", "ART NN VVFIN ART NN", "der steuerung öffnen der fenster"));
        list.add(createNLP("Sensoren prüfen das Fenster", "NN VVFIN ART NN", "sensor prüfen der fenster"));
        list.add(createNLP("Ein Alarm warnt bei gebrochenem Fenster", "ART NN VVFIN APPR ADJA NN", "ein alarm warnen bei brechen fenster"));
        list.add(createNLP("Der Benutzer schließt das Fenster manuell", "ART NN VVFIN ART NN ADJD", "der benutzer schließen der fenster manuell"));

        // Group 3: User/Login
        list.add(createNLP("Der Benutzer meldet sich an", "ART NN VVFIN PRF PTKVZ", "der benutzer melden sich an"));
        list.add(createNLP("Das System prüft die Anmeldung", "ART NN VVFIN ART NN", "der system prüfen der anmeldung"));
        list.add(createNLP("Fehlerhafte Anmeldung blockiert den Benutzer", "ADJA NN VVFIN ART NN", "fehlerhaft anmeldung blockieren der benutzer"));
        list.add(createNLP("Der Administrator verwaltet Benutzer", "ART NN VVFIN NN", "der administrator verwalten benutzer"));
        list.add(createNLP("Das Passwort des Benutzers ist sicher", "ART NN ART NN VAFIN ADJD", "der passwort der benutzer sein sicher"));

        // Group 4: Sensor/Hardware
        list.add(createNLP("Die Sensoren senden Daten", "ART NN VVFIN NN", "der sensor senden daten"));
        list.add(createNLP("Das System verarbeitet Sensordaten", "ART NN VVFIN NN", "der system verarbeiten sensordaten"));
        list.add(createNLP("Temperatur wird vom Sensor gemessen", "NN VAFIN APPRART NN VVPP", "temperatur werden von sensor messen"));
        list.add(createNLP("Die Regelung basiert auf Sensoren", "ART NN VVFIN APPR NN", "der regelung basieren auf sensor"));
        list.add(createNLP("Ein defekter Sensor löst Alarm aus", "ART ADJA NN VVFIN NN PTKVZ", "ein defekt sensor lösen alarm aus"));

        // Group 5: Network
        list.add(createNLP("Das Netzwerk fällt aus", "ART NN VVFIN PTKVZ", "der netzwerk fallen aus"));
        list.add(createNLP("Das System baut eine Verbindung auf", "ART NN VVFIN ART NN PTKVZ", "der system bauen ein verbindung auf"));
        list.add(createNLP("Die Verbindung ist verschlüsselt", "ART NN VAFIN ADJD", "der verbindung sein verschlüsseln"));
        list.add(createNLP("Daten werden über das Netzwerk gesendet", "NN VAFIN APPR ART NN VVPP", "daten werden über der netzwerk senden"));
        list.add(createNLP("Ein Netzwerkfehler wird protokolliert", "ART NN VAFIN VVPP", "ein netzwerkfehler werden protokollieren"));

        // Group 6: Lighting
        list.add(createNLP("Das Licht schaltet sich ein", "ART NN VVFIN PRF PTKVZ", "der licht schalten sich ein"));
        list.add(createNLP("Die Steuerung regelt die Helligkeit", "ART NN VVFIN ART NN", "der steuerung regeln der helligkeit"));
        list.add(createNLP("Der Benutzer dimmt das Licht", "ART NN VVFIN ART NN", "der benutzer dimmen der licht"));
        list.add(createNLP("Ein Timer deaktiviert das Licht", "ART NN VVFIN ART NN", "ein timer deaktivieren der licht"));
        list.add(createNLP("Das System meldet einen Lampendefekt", "ART NN VVFIN ART NN", "der system melden ein lampendefekt"));

        return list;
    }

    // -------------------------------------------------------------------------
    // New Large Scenario Generators
    // -------------------------------------------------------------------------

    private static List<NLPResult> createAllDifferentData() {
        List<NLPResult> list = new ArrayList<>();
        // Generate 30 completely different sentences to force a similarity close to 0
        for (int i = 0; i < 30; i++) {
            String token = "Der " + DIFF_NOUNS[i] + " " + DIFF_VERBS[i];
            String lemma = "der " + DIFF_NOUNS[i] + " " + DIFF_VERBS[i];
            list.add(createNLP(token, "ART NN VVFIN", lemma));
        }
        return list;
    }

    private static List<NLPResult> createDifferentWithSimilarOutliers() {
        List<NLPResult> list = new ArrayList<>();
        // 26 completely different
        for (int i = 0; i < 26; i++) {
            String token = "Der " + DIFF_NOUNS[i] + " " + DIFF_VERBS[i];
            String lemma = "der " + DIFF_NOUNS[i] + " " + DIFF_VERBS[i];
            list.add(createNLP(token, "ART NN VVFIN", lemma));
        }
        // 4 identical outliers
        for (int i = 0; i < 4; i++) {
            list.add(createNLP("Das Sicherheitssystem sperrt die Haupttür",
                    "ART NN VVFIN ART NN",
                    "der sicherheitssystem sperren der haupttür"));
        }
        return list;
    }

    private static List<NLPResult> createAllSimilarData() {
        List<NLPResult> list = new ArrayList<>();
        // The exact same sentence 30 times guarantees a similarity of 1.00
        for (int i = 0; i < 30; i++) {
            list.add(createNLP("Das Steuermodul öffnet das Fenster",
                    "ART NN VVFIN ART NN",
                    "der steuermodul öffnen der fenster"));
        }
        return list;
    }

    private static List<NLPResult> createSimilarWithDifferentOutliers() {
        List<NLPResult> list = new ArrayList<>();
        // 26 exactly identical sentences
        for (int i = 0; i < 26; i++) {
            list.add(createNLP("Das Steuermodul öffnet das Fenster",
                    "ART NN VVFIN ART NN",
                    "der steuermodul öffnen der fenster"));
        }
        // 4 completely different outliers from the pool (indices 26-29)
        for (int i = 26; i < 30; i++) {
            String token = "Der " + DIFF_NOUNS[i] + " " + DIFF_VERBS[i];
            String lemma = "der " + DIFF_NOUNS[i] + " " + DIFF_VERBS[i];
            list.add(createNLP(token, "ART NN VVFIN", lemma));
        }
        return list;
    }
}