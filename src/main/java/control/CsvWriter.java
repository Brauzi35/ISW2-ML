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

    static final String fls = "false";
    static final String tr = "true";

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


        try (FileWriter out = new FileWriter(csvname)) {
            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers))) {

                for (List<String> row : data) {
                    printer.printRecord(row);
                }


            }
        }

    }

    private String definingFunc(int i, int flag){ //flag: 1 = Balancing, 2 = Feature Selection, 3 = Sensitivity


        
        if(flag == 1){ //balancing
            return balancingFunc(i);
        } else if (flag == 2) { //feature selection
            return featureSelectionFunc(i);
        } else if (flag == 3) { //sensitivity
            if(i < 9){
                return fls;
            } else if (i < 12) {
                return tr;
            } else if (i < 15) {
                return fls;
            } else if (i < 21) {
                return tr;
            } else{
                return tr;
            }
        }

        return "error";
    }

    private String balancingFunc(int i){

        if(i < 6){
            return fls;
        } else if (i < 9) { //6-7-8
            return tr;
        } else if (i < 12) {
            return fls;
        } else if (i < 15) {
            return tr;
        } else if (i < 18) {
            return fls;
        } else if (i < 21) {
            return tr;
        } else{
            return tr;
        }
    }

    private String featureSelectionFunc(int i){
        String f = fls;
        if(i < 3){
            return f;
        } else if (i < 6) {
            return tr;
        } else if (i < 12) {
            return f;
        } else if (i < 18) {
            return tr;
        } else if (i < 21) {
            return f;
        } else{
            return tr;
        }
    }

    /*
    public void csvFinal(FinalMetrics fm) {
        List<List<Double>> precision = fm.getPrecision();
        List<List<Double>> recall = fm.getRecall();
        List<List<Double>> auc = fm.getAuc();
        List<List<Double>> kappa = fm.getKappa();
        List<List<Double>> tp = fm.getTp();
        List<List<Double>> tn = fm.getTn();
        List<List<Double>> fp = fm.getFp();
        List<List<Double>> fn = fm.getFn();
        String projname = fm.getProjname();

        try {
            String[] headers = {"Dataset", "#Training Release", "Classifier", "Balancing", "Feature Selection",
                    "Sensitivity", "TP", "FP", "TN", "FN", "Precision", "Recall", "AUC", "Kappa"};

            List<List<String>> data = new ArrayList<>();
            for (int i = 0; i < tp.size(); i++) {
                int reminder = i % 3;
                for (int j = 0; j < tp.get(i).size(); j++) {
                    if (tp.get(i).size() <= j || fp.get(i).size() <= j || tn.get(i).size() <= j || fn.get(i).size() <= j ||
                            precision.get(i).size() <= j || recall.get(i).size() <= j || auc.get(i).size() <= j || kappa.get(i).size() <= j) {
                        continue; // Salta gli indici non validi
                    }

                    List<String> temp = new ArrayList<>();
                    temp.add(projname);
                    temp.add(String.valueOf(j + 1));
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
                    temp.add(definingFunc(i, 1));
                    temp.add(definingFunc(i, 2));
                    temp.add(definingFunc(i, 3));
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

            try (FileWriter out = new FileWriter(projname + "FinalEval.csv")) {
                try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers))) {
                    for (List<String> row : data) {
                        printer.printRecord(row);
                    }
                }
            }

        } catch (IOException ioException) {
            Logger logger = Logger.getLogger(JiraController.class.getName());
            String out = "IOException";
            logger.log(Level.INFO, out);
        }
    }


     */

    public void csvFinal(FinalMetrics fm) {
        List<List<Double>> precision = fm.getPrecision();
        List<List<Double>> recall = fm.getRecall();
        List<List<Double>> auc = fm.getAuc();
        List<List<Double>> kappa = fm.getKappa();
        List<List<Double>> tp = fm.getTp();
        List<List<Double>> tn = fm.getTn();
        List<List<Double>> fp = fm.getFp();
        List<List<Double>> fn = fm.getFn();
        String projname = fm.getProjname();

        try {
            String[] headers = {"Dataset", "#Training Release", "Classifier", "Balancing", "Feature Selection",
                    "Sensitivity", "TP", "FP", "TN", "FN", "Precision", "Recall", "AUC", "Kappa"};

            List<List<String>> data = new ArrayList<>();
            for (int i = 0; i < tp.size(); i++) {
                int reminder = i % 3;
                for (int j = 0; j < tp.get(i).size(); j++) {

                    if (tp.get(i).size() <= j || fp.get(i).size() <= j || tn.get(i).size() <= j || fn.get(i).size() <= j ||
                            precision.get(i).size() <= j || recall.get(i).size() <= j || auc.get(i).size() <= j || kappa.get(i).size() <= j) {
                        continue; // Salta gli indici non validi
                    }

                    List<String> temp = new ArrayList<>();
                    temp.add(projname);
                    temp.add(String.valueOf(j + 1));
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
                    temp.add(definingFunc(i, 1));
                    temp.add(definingFunc(i, 2));
                    temp.add(definingFunc(i, 3));
                    temp.add(String.valueOf(tp.get(i).get(j)));
                    temp.add(String.valueOf(fp.get(i).get(j)));
                    temp.add(String.valueOf(tn.get(i).get(j)));
                    temp.add(String.valueOf(fn.get(i).get(j)));
                    temp.add(String.valueOf(precision.get(i).get(j)));
                    temp.add(String.valueOf(recall.get(i).get(j)));
                    temp.add(String.valueOf(auc.get(i).get(j)));
                    temp.add(String.valueOf(kappa.get(i).get(j)));
                    if(tp.get(i).get(j) != 0.0 || tn.get(i).get(j) != 0.0) {
                        data.add(temp);
                    }
                }
            }

            try (FileWriter out = new FileWriter(projname + "FinalEval.csv")) {
                try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers))) {
                    for (List<String> row : data) {
                        printer.printRecord(row);
                    }
                }
            }

        } catch (IOException ioException) {
            Logger logger = Logger.getLogger(JiraController.class.getName());
            String out = "IOException";
            logger.log(Level.INFO, out);
        }
    }


}

