package requirementMining.test;

import requirementMining.helper.NLPResult;
import variabilityMining.Language;
import requirementMining.RequirementPreprocessor;

import java.util.LinkedHashMap;
import java.util.Map;

import static requirementMining.RequirementPreprocessor.*;

public class RequirementsVariabilityExtractorTester {

    static Language lang = Language.GERMAN;

    public static void main(String[] args) {
        //Basic Requirements Preprocessing Test
        Map <String, NLPResult> preproReqs = requirementPreprocessor_basic_test(getSampleRequirements());
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
}
