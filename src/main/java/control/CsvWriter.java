package control;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.FinalInstance;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CsvWriter {

    public void csv_builder(List<FinalInstance> finalInstances, String csvname) throws IOException {
        String[] headers = {"Version", "Filename", "NR", "NAUTHORS" ,"LOC", "LOC_ADDED", "AVGLOCADDED"
                , "MAXLOCADDED", "CHURN", "AVGCHURN", "MAXCHURN", "BUGGY"};

        List<List<String>> data = new ArrayList<>();

        for(FinalInstance i : finalInstances){
            List<String> temp = new ArrayList<>();
            temp.add(i.getVersion());
            temp.add(i.getName());
            temp.add(String.valueOf(i.getNr()));
            temp.add(String.valueOf(i.getnAuthors()));
            temp.add(String.valueOf(i.getSize()));
            temp.add(String.valueOf(i.getLocAdded()));
            temp.add(String.valueOf(i.getAvgLocAdded()));
            temp.add(String.valueOf(i.getMaxLocAdded()));
            temp.add(String.valueOf(i.getChurn()));
            temp.add(String.valueOf(i.getAvgChurn()));
            temp.add(String.valueOf(i.getMaxChurn()));
            temp.add(i.getBuggyness());
            data.add(temp);
        }


        FileWriter out = new FileWriter(csvname);
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers));

        for (List<String> row : data) {
            printer.printRecord(row);
        }

        printer.close();
        out.close();

    }


}

