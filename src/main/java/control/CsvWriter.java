package control;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.FinalInstance;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CsvWriter {

    public void csvBuilder(List<FinalInstance> finalInstances, String csvname) throws IOException {
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

    public void csvFinal(List<List<Double>> precision, List<List<Double>> recall, List<List<Double>> auc, List<List<Double>> kappa
    , List<List<Double>> tp, List<List<Double>> tn, List<List<Double>> fp, List<List<Double>> fn, String projname){
        try {
            String[] headers = {"Dataset", "#Training Release", "Classifier", "Balancing", "Feature Selection"
                    , "Sensitivity", "TP", "FP", "TN", "FN", "Precision", "Recall", "AUC", "Kappa"};

            System.out.println(precision + "\n " + tp);

            List<List<String>> data = new ArrayList<>();
            for (int i = 0; i < tp.size() - 1; i++) {


                int reminder = i % 3;
                for (int j = 0; j < tp.get(0).size() - 1; j++) {
                    List<String> temp = new ArrayList<>();
                    temp.add(projname);
                    temp.add(String.valueOf(j));
                    switch (reminder) {
                        case 0:
                            temp.add("Random Forest");
                            break;
                        case 1:
                            temp.add("Naive Bayes");
                            break;
                        case 2:
                            temp.add("Ibk");
                            break;
                    }
                    temp.add("prova");
                    temp.add("prova");
                    temp.add("prova");
                    temp.add(String.valueOf(tp.get(i).get(j)));
                    temp.add(String.valueOf(fp.get(i).get(j)));
                    temp.add(String.valueOf(tn.get(i).get(j)));
                    temp.add(String.valueOf(fn.get(i).get(j)));
                    temp.add(String.valueOf(precision.get(i).get(j)));
                    temp.add(String.valueOf(recall.get(i).get(j)));
                    temp.add(String.valueOf(auc.get(i).get(j)));
                    temp.add(String.valueOf(kappa.get(i).get(j)));
                    data.add(temp);


                }

            }

            FileWriter out = new FileWriter(projname + "FinalEval.csv");
            CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers));

            for (List<String> row : data) {
                printer.printRecord(row);
            }

            printer.close();
            out.close();

            System.out.println("fatto chiuso tutto");
        } catch (IOException ioException){

        }



    }


}

