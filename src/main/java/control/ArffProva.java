package control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class ArffProva {

    public static void main(String[] args) throws Exception {
        for(int i=2; i<15; i++){
            String partialName = "storm" + i + "Training.arff";
            String filePath = "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\" + partialName;

            // Numero di riga da modificare
            int lineToModify = 13;

            // Nuovo contenuto della riga
            String newLineContent = "@attribute BUGGY {Yes,No}";

            // Leggi il contenuto del file originale
            StringBuilder fileContent;
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                fileContent = new StringBuilder();
                int currentLine = 1;
                while ((line = reader.readLine()) != null) {
                    if (currentLine == lineToModify) {
                        // Sostituisci la riga con il nuovo contenuto
                        fileContent.append(newLineContent).append(System.lineSeparator());
                    } else {
                        // Mantieni le righe originali
                        fileContent.append(line).append(System.lineSeparator());
                    }
                    currentLine++;
                }

            }

            // Scrivi il nuovo contenuto nel file ARFF
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(fileContent.toString());

            }
        }
    }

}
