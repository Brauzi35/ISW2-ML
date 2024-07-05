package control;

import model.FinalMetrics;
import model.Version;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WekaEvaluationsController2 {

    private static final Logger LOGGER = Logger.getLogger(WekaEvaluationsController2.class.getName());

    private String projName;

    static String projNameBis;

    static List<List<Double>> precision = new ArrayList<>();
    static List<List<Double>> recall = new ArrayList<>();
    static List<List<Double>> auc = new ArrayList<>();
    static List<List<Double>> kappa = new ArrayList<>();
    static List<List<Double>> fp = new ArrayList<>();
    static List<List<Double>> fn = new ArrayList<>();
    static List<List<Double>> tp = new ArrayList<>();
    static List<List<Double>> tn = new ArrayList<>();

    public WekaEvaluationsController2(String projName) {
        this.projName = projName;

    }



    private static void classify(String trainingPath, String testingPath, boolean os, boolean fs, boolean cs, int index) throws Exception {
        DataSource source = new DataSource(trainingPath);
        Instances training = source.getDataSet();
        DataSource source2 = new DataSource(testingPath);
        Instances testing = source2.getDataSet();

        RandomForest randomForestClassifier = new RandomForest();
        NaiveBayes naiveBayesClassifier = new NaiveBayes();
        IBk ibkClassifier = new IBk();


        int numAttr = training.numAttributes();
        training.setClassIndex(numAttr - 1);
        testing.setClassIndex(numAttr - 1);

        Evaluation eval = new Evaluation(testing);


        try{
            Instances filteredTraining = training;
            Instances filteredTesting = testing;
            if(fs){
                BestFirst search = new BestFirst();
                CfsSubsetEval evalSub = new CfsSubsetEval();

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(evalSub);
                filter.setSearch(search);
                filter.setInputFormat(training);

                filteredTraining = Filter.useFilter(training, filter);
                filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);

            }
            if(os){
                SMOTE smoteFilter = new SMOTE();
                smoteFilter.setInputFormat(training);
                filteredTraining = Filter.useFilter(training, smoteFilter); //faccio solo il training perché sto facendo OVER

            }
            if(!cs && filteredTraining.numInstances() > 0 && filteredTesting.numInstances() > 0){

                    eval = new Evaluation(testing);
                    randomForestClassifier.buildClassifier(filteredTraining); //if ex os oversamlped
                    eval.evaluateModel(randomForestClassifier, filteredTesting);
                    precision.get(index).add(eval.precision(0));
                    recall.get(index).add(eval.recall(0));
                    auc.get(index).add(eval.areaUnderROC(0));
                    kappa.get(index).add(eval.kappa());
                    fp.get(index).add(eval.numFalsePositives(0));
                    fn.get(index).add(eval.numFalseNegatives(0));
                    tp.get(index).add(eval.numTruePositives(0));
                    tn.get(index).add(eval.numTrueNegatives(0));


                    eval = new Evaluation(testing);
                    naiveBayesClassifier.buildClassifier(filteredTraining);
                    eval.evaluateModel(naiveBayesClassifier, filteredTesting);
                    precision.get(index + 1).add(eval.precision(0));
                    recall.get(index + 1).add(eval.recall(0));
                    auc.get(index + 1).add(eval.areaUnderROC(0));
                    kappa.get(index + 1).add(eval.kappa());
                    fp.get(index + 1).add(eval.numFalsePositives(0));
                    fn.get(index + 1).add(eval.numFalseNegatives(0));
                    tp.get(index + 1).add(eval.numTruePositives(0));
                    tn.get(index + 1).add(eval.numTrueNegatives(0));

                    eval = new Evaluation(testing);
                    ibkClassifier.buildClassifier(filteredTraining);
                    eval.evaluateModel(ibkClassifier, filteredTesting);
                    precision.get(index + 2).add(eval.precision(0));
                    recall.get(index + 2).add(eval.recall(0));
                    auc.get(index + 2).add(eval.areaUnderROC(0));
                    kappa.get(index + 2).add(eval.kappa());
                    fp.get(index + 2).add(eval.numFalsePositives(0));
                    fn.get(index + 2).add(eval.numFalseNegatives(0));
                    tp.get(index + 2).add(eval.numTruePositives(0));
                    tn.get(index + 2).add(eval.numTrueNegatives(0));

            }
            if(cs && filteredTraining.numInstances() > 0 && filteredTesting.numInstances() > 0){

                    CostMatrix costMatrix = new CostMatrix(2); // 2x2 matrix -  CFN = 10*CFP
                    costMatrix.setCell(0, 0, 0.0);
                    costMatrix.setCell(1, 0, 10.0);
                    costMatrix.setCell(0, 1, 1.0);
                    costMatrix.setCell(1, 1, 0.0);

                    eval = new Evaluation(filteredTesting);

                    CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
                    //random forest cost sens
                    costSensitiveClassifier.setClassifier(randomForestClassifier);
                    costSensitiveClassifier.setCostMatrix(costMatrix);
                    costSensitiveClassifier.buildClassifier(filteredTraining);
                    eval.evaluateModel(costSensitiveClassifier, filteredTesting);
                    precision.get(index).add(eval.precision(0));
                    recall.get(index).add(eval.recall(0));
                    auc.get(index).add(eval.areaUnderROC(0));
                    kappa.get(index).add(eval.kappa());
                    fp.get(index).add(eval.numFalsePositives(0));
                    fn.get(index).add(eval.numFalseNegatives(0));
                    tp.get(index).add(eval.numTruePositives(0));
                    tn.get(index).add(eval.numTrueNegatives(0));


                    //naive Bayes cost sens
                    eval = new Evaluation(filteredTesting);
                    costSensitiveClassifier.setClassifier(naiveBayesClassifier);
                    costSensitiveClassifier.setCostMatrix(costMatrix);
                    costSensitiveClassifier.buildClassifier(filteredTraining);
                    eval.evaluateModel(costSensitiveClassifier, filteredTesting);
                    precision.get(index + 1).add(eval.precision(0));
                    recall.get(index + 1).add(eval.recall(0));
                    auc.get(index + 1).add(eval.areaUnderROC(0));
                    kappa.get(index + 1).add(eval.kappa());
                    fp.get(index + 1).add(eval.numFalsePositives(0));
                    fn.get(index + 1).add(eval.numFalseNegatives(0));
                    tp.get(index + 1).add(eval.numTruePositives(0));
                    tn.get(index + 1).add(eval.numTrueNegatives(0));


                    //ibk cost sens
                    eval = new Evaluation(filteredTesting);
                    costSensitiveClassifier.setClassifier(ibkClassifier);
                    costSensitiveClassifier.setCostMatrix(costMatrix);
                    costSensitiveClassifier.buildClassifier(filteredTraining);
                    eval.evaluateModel(costSensitiveClassifier, filteredTesting);
                    precision.get(index + 2).add(eval.precision(0));
                    recall.get(index + 2).add(eval.recall(0));
                    auc.get(index + 2).add(eval.areaUnderROC(0));
                    kappa.get(index + 2).add(eval.kappa());
                    fp.get(index + 2).add(eval.numFalsePositives(0));
                    fn.get(index + 2).add(eval.numFalseNegatives(0));
                    tp.get(index + 2).add(eval.numTruePositives(0));
                    tn.get(index + 2).add(eval.numTrueNegatives(0));

            }




        }catch (Exception e){
            Logger logger = Logger.getLogger(JiraController.class.getName());
            String out = "error in classify";
            logger.log(Level.INFO, out);

        }

    }











    public static void listFiller(List<List<Double>> thing){
        for(List<Double> small : thing){
            if(small.isEmpty()){
                for(int i = 0; i<13; i++){
                    small.add(0.0);
                }
            }
        }
    }


    public static void main(String[] args) throws IOException {
        try {
            projNameBis = "bookkeeper"; //bookkeeper or storm

            //25 classifiers
            for (int n = 0; n < 25; n++) {
                recall.add(new ArrayList<>());
                precision.add(new ArrayList<>());
                auc.add(new ArrayList<>());
                kappa.add(new ArrayList<>());
                fp.add(new ArrayList<>());
                fn.add(new ArrayList<>());
                tp.add(new ArrayList<>());
                tn.add(new ArrayList<>());
            }

            JiraController jc = new JiraController(projNameBis.toUpperCase());
            List<Version> versions = jc.getAllVersions();
            versions = versions.subList(0, versions.size() / 2);

            for(Version v : versions){
                if(v.getIndex() == 0 || v.getIndex() == 1){
                    continue;
                }
                String trainingPath = "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\"+ projNameBis+ v.getIndex() +"Training.arff";
                String testingPath = "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\"+ projNameBis + v.getIndex() +"Testing.arff";
                classify(trainingPath, testingPath, false, false, false,0);
                classify(trainingPath, testingPath, true, false, false,3);
                classify(trainingPath, testingPath, false, true, false,6);
                classify(trainingPath, testingPath, false, false, true,9);
                classify(trainingPath, testingPath, true, true, false,12);
                classify(trainingPath, testingPath, true, false, true,15);
                classify(trainingPath, testingPath, false, true, true,18);
                classify(trainingPath, testingPath, true, true, true,21);

            }


            CsvWriter csvWriter = new CsvWriter();

            List<List<Double>> precision2 = precision;
            List<List<Double>> recall2 = recall;
            List<List<Double>> auc2 = auc;
            List<List<Double>> kappa2 = kappa;
            List<List<Double>> fp2 = fp;
            List<List<Double>> fn2 = fn;
            List<List<Double>> tp2 = tp;
            List<List<Double>> tn2 = tn;
            listFiller(precision2);
            listFiller(recall2);
            listFiller(auc2);
            listFiller(kappa2);
            listFiller(fp2);
            listFiller(fn2);
            listFiller(tp2);
            listFiller(tn2);

            FinalMetrics fm = new FinalMetrics();
            fm.setAuc(auc2);
            fm.setFn(fn2);
            fm.setKappa(kappa2);
            fm.setPrecision(precision2);
            fm.setProjname(projNameBis);
            fm.setTn(tn2);
            fm.setTp(tp2);
            fm.setRecall(recall2);
            fm.setFp(fp2);
            csvWriter.csvFinal(fm);

        } catch (Exception e){
            Logger logger = Logger.getLogger(JiraController.class.getName());
            String out = "Error in classify";
            logger.log(Level.SEVERE, out);


        }
    }

}
