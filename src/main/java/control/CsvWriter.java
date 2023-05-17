package control;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.FinalInstance;
import model.FinalMetrics;
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

    public void csvFinal(FinalMetrics fm){
        List<List<Double>> precision = fm.getPrecision();
        List<List<Double>> recall = fm.getRecall();
        List<List<Double>> auc = fm.getAuc();
        List<List<Double>> kappa= fm.getKappa();
        List<List<Double>> tp = fm.getTp();
        List<List<Double>> tn = fm.getTn();
        List<List<Double>> fp = fm.getFp();
        List<List<Double>> fn = fm.getFn();
        String projname = fm.getProjname();


        try {
            String[] headers = {"Dataset", "#Training Release", "Classifier", "Balancing", "Feature Selection"
                    , "Sensitivity", "TP", "FP", "TN", "FN", "Precision", "Recall", "AUC", "Kappa"};



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
                        default:
                            temp.add("Error");
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

        } catch (IOException ioException){

            Logger logger = Logger.getLogger(JiraController.class.getName());
            String out ="IOException";
            logger.log(Level.INFO, out);

        }



    }


}

