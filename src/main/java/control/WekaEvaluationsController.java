package control;

import model.*;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;

import model.Version;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WekaEvaluationsController {

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

    public WekaEvaluationsController(String projName) {
        this.projName = projName;
        projNameBis = projName;
    }

    private static void classify(String trainingPath, String testingPath, boolean os, boolean fs, boolean cs) throws Exception {
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


        try {
            if(!fs && !cs && !os) {
                randomForestClassifier.buildClassifier(training);
                eval.evaluateModel(randomForestClassifier, testing);
                precision.get(0).add(eval.precision(0));
                recall.get(0).add(eval.recall(0));
                auc.get(0).add(eval.areaUnderROC(0));
                kappa.get(0).add(eval.kappa());
                fp.get(0).add(eval.numFalsePositives(0));
                fn.get(0).add(eval.numFalseNegatives(0));
                tp.get(0).add(eval.numTruePositives(0));
                tn.get(0).add(eval.numTrueNegatives(0));


                eval = new Evaluation(testing);
                naiveBayesClassifier.buildClassifier(training);
                eval.evaluateModel(naiveBayesClassifier, testing);
                precision.get(1).add(eval.precision(0));
                recall.get(1).add(eval.recall(0));
                auc.get(1).add(eval.areaUnderROC(0));
                kappa.get(1).add(eval.kappa());
                fp.get(1).add(eval.numFalsePositives(0));
                fn.get(1).add(eval.numFalseNegatives(0));
                tp.get(1).add(eval.numTruePositives(0));
                tn.get(1).add(eval.numTrueNegatives(0));

                eval = new Evaluation(testing);
                ibkClassifier.buildClassifier(training);
                eval.evaluateModel(ibkClassifier, testing);
                precision.get(2).add(eval.precision(0));
                recall.get(2).add(eval.recall(0));
                auc.get(2).add(eval.areaUnderROC(0));
                kappa.get(2).add(eval.kappa());
                fp.get(2).add(eval.numFalsePositives(0));
                fn.get(2).add(eval.numFalseNegatives(0));
                tp.get(2).add(eval.numTruePositives(0));
                tn.get(2).add(eval.numTrueNegatives(0));

            }
            else if(fs && !cs && !os){

                BestFirst search = new BestFirst();
                CfsSubsetEval evalSub = new CfsSubsetEval();

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(evalSub);
                filter.setSearch(search);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);

                eval = new Evaluation(testing);
                randomForestClassifier.buildClassifier(filteredTraining);
                eval.evaluateModel(randomForestClassifier, filteredTesting);
                precision.get(3).add(eval.precision(0));
                recall.get(3).add(eval.recall(0));
                auc.get(3).add(eval.areaUnderROC(0));
                kappa.get(3).add(eval.kappa());
                fp.get(3).add(eval.numFalsePositives(0));
                fn.get(3).add(eval.numFalseNegatives(0));
                tp.get(3).add(eval.numTruePositives(0));
                tn.get(3).add(eval.numTrueNegatives(0));

                eval = new Evaluation(testing);
                naiveBayesClassifier.buildClassifier(filteredTraining);
                eval.evaluateModel(naiveBayesClassifier, filteredTesting);
                precision.get(4).add(eval.precision(0));
                recall.get(4).add(eval.recall(0));
                auc.get(4).add(eval.areaUnderROC(0));
                kappa.get(4).add(eval.kappa());
                fp.get(4).add(eval.numFalsePositives(0));
                fn.get(4).add(eval.numFalseNegatives(0));
                tp.get(4).add(eval.numTruePositives(0));
                tn.get(4).add(eval.numTrueNegatives(0));

                eval = new Evaluation(testing);
                naiveBayesClassifier.buildClassifier(filteredTraining);
                eval.evaluateModel(naiveBayesClassifier, filteredTesting);
                precision.get(5).add(eval.precision(0));
                recall.get(5).add(eval.recall(0));
                auc.get(5).add(eval.areaUnderROC(0));
                kappa.get(5).add(eval.kappa());
                fp.get(5).add(eval.numFalsePositives(0));
                fn.get(5).add(eval.numFalseNegatives(0));
                tp.get(5).add(eval.numTruePositives(0));
                tn.get(5).add(eval.numTrueNegatives(0));

            } 
            else if (!fs && !cs && os) {

                SMOTE smoteFilter = new SMOTE();
                smoteFilter.setInputFormat(training);
                Instances oversampledData = Filter.useFilter(training, smoteFilter); //faccio solo il training perché sto facendo OVER

                eval = new Evaluation(testing);
                randomForestClassifier.buildClassifier(oversampledData);
                eval.evaluateModel(randomForestClassifier, testing);
                precision.get(6).add(eval.precision(0));
                recall.get(6).add(eval.recall(0));
                auc.get(6).add(eval.areaUnderROC(0));
                kappa.get(6).add(eval.kappa());
                fp.get(6).add(eval.numFalsePositives(0));
                fn.get(6).add(eval.numFalseNegatives(0));
                tp.get(6).add(eval.numTruePositives(0));
                tn.get(6).add(eval.numTrueNegatives(0));
                

                eval = new Evaluation(testing);
                naiveBayesClassifier.buildClassifier(oversampledData);
                eval.evaluateModel(naiveBayesClassifier, testing);
                precision.get(7).add(eval.precision(0));
                recall.get(7).add(eval.recall(0));
                auc.get(7).add(eval.areaUnderROC(0));
                kappa.get(7).add(eval.kappa());
                fp.get(7).add(eval.numFalsePositives(0));
                fn.get(7).add(eval.numFalseNegatives(0));
                tp.get(7).add(eval.numTruePositives(0));
                tn.get(7).add(eval.numTrueNegatives(0));
               
                eval = new Evaluation(testing);
                ibkClassifier.buildClassifier(oversampledData);
                eval.evaluateModel(ibkClassifier, testing);
                precision.get(8).add(eval.precision(0));
                recall.get(8).add(eval.recall(0));
                auc.get(8).add(eval.areaUnderROC(0));
                kappa.get(8).add(eval.kappa());
                fp.get(8).add(eval.numFalsePositives(0));
                fn.get(8).add(eval.numFalseNegatives(0));
                tp.get(8).add(eval.numTruePositives(0));
                tn.get(8).add(eval.numTrueNegatives(0));
                

            } else if (!fs && cs && !os) {

                CostMatrix costMatrix = new CostMatrix(2); // 2x2 matrix -  CFN = 10*CFP
                costMatrix.setCell(0,0,0.0);
                costMatrix.setCell(1,0, 10.0);
                costMatrix.setCell(0,1,1.0);
                costMatrix.setCell(1,1,0.0);

                eval = new Evaluation(testing);

                CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
                //random forest cost sens
                costSensitiveClassifier.setClassifier(randomForestClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(training);
                eval.evaluateModel(costSensitiveClassifier, testing);
                precision.get(9).add(eval.precision(0));
                recall.get(9).add(eval.recall(0));
                auc.get(9).add(eval.areaUnderROC(0));
                kappa.get(9).add(eval.kappa());
                fp.get(9).add(eval.numFalsePositives(0));
                fn.get(9).add(eval.numFalseNegatives(0));
                tp.get(9).add(eval.numTruePositives(0));
                tn.get(9).add(eval.numTrueNegatives(0));


                //naive Bayes cost sens
                eval = new Evaluation(testing);
                costSensitiveClassifier.setClassifier(naiveBayesClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(training);
                eval.evaluateModel(costSensitiveClassifier, testing);
                precision.get(10).add(eval.precision(0));
                recall.get(10).add(eval.recall(0));
                auc.get(10).add(eval.areaUnderROC(0));
                kappa.get(10).add(eval.kappa());
                fp.get(10).add(eval.numFalsePositives(0));
                fn.get(10).add(eval.numFalseNegatives(0));
                tp.get(10).add(eval.numTruePositives(0));
                tn.get(10).add(eval.numTrueNegatives(0));


                //ibk cost sens
                eval = new Evaluation(testing);
                costSensitiveClassifier.setClassifier(ibkClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(training);
                eval.evaluateModel(costSensitiveClassifier, testing);
                precision.get(11).add(eval.precision(0));
                recall.get(11).add(eval.recall(0));
                auc.get(11).add(eval.areaUnderROC(0));
                kappa.get(11).add(eval.kappa());
                fp.get(11).add(eval.numFalsePositives(0));
                fn.get(11).add(eval.numFalseNegatives(0));
                tp.get(11).add(eval.numTruePositives(0));
                tn.get(11).add(eval.numTrueNegatives(0));
                
                
            } else if (fs && !cs && os) {
                BestFirst search = new BestFirst();
                CfsSubsetEval evalSub = new CfsSubsetEval();

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(evalSub);
                filter.setSearch(search);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);
                if (filteredTraining.numInstances() > 0 && filteredTesting.numInstances() > 0) {

                    SMOTE smoteFilterSel = new SMOTE();
                    smoteFilterSel.setOptions(new String[]{"-M", "1.0"});
                    smoteFilterSel.setInputFormat(filteredTraining);
                    Instances oversampledDataSel = Filter.useFilter(filteredTraining, smoteFilterSel);


                    //random forest
                    eval = new Evaluation(testing);
                    randomForestClassifier.buildClassifier(oversampledDataSel);
                    eval.evaluateModel(randomForestClassifier, filteredTesting);
                    precision.get(12).add(eval.precision(0));
                    recall.get(12).add(eval.recall(0));
                    auc.get(12).add(eval.areaUnderROC(0));
                    kappa.get(12).add(eval.kappa());
                    fp.get(12).add(eval.numFalsePositives(0));
                    fn.get(12).add(eval.numFalseNegatives(0));
                    tp.get(12).add(eval.numTruePositives(0));
                    tn.get(12).add(eval.numTrueNegatives(0));
                    //naive bayes
                    eval = new Evaluation(testing);
                    naiveBayesClassifier.buildClassifier(oversampledDataSel);
                    eval.evaluateModel(naiveBayesClassifier, filteredTesting);
                    precision.get(13).add(eval.precision(0));
                    recall.get(13).add(eval.recall(0));
                    auc.get(13).add(eval.areaUnderROC(0));
                    kappa.get(13).add(eval.kappa());
                    fp.get(13).add(eval.numFalsePositives(0));
                    fn.get(13).add(eval.numFalseNegatives(0));
                    tp.get(13).add(eval.numTruePositives(0));
                    tn.get(13).add(eval.numTrueNegatives(0));
                   
                    //ibk
                    eval = new Evaluation(testing);
                    ibkClassifier.buildClassifier(oversampledDataSel);
                    eval.evaluateModel(ibkClassifier, filteredTesting);
                    precision.get(14).add(eval.precision(0));
                    recall.get(14).add(eval.recall(0));
                    auc.get(14).add(eval.areaUnderROC(0));
                    kappa.get(14).add(eval.kappa());
                    fp.get(14).add(eval.numFalsePositives(0));
                    fn.get(14).add(eval.numFalseNegatives(0));
                    tp.get(14).add(eval.numTruePositives(0));
                    tn.get(14).add(eval.numTrueNegatives(0));
                    
                } else {
                    System.out.println("error");
                }

            } else if (fs && cs && !os) {


                BestFirst search = new BestFirst();
                CfsSubsetEval evalSub = new CfsSubsetEval();

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(evalSub);
                filter.setSearch(search);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);

                CostMatrix costMatrix = new CostMatrix(2); // 2x2 matrix -  CFN = 10*CFP
                costMatrix.setCell(0,0,0.0);
                costMatrix.setCell(1,0, 10.0);
                costMatrix.setCell(0,1,1.0);
                costMatrix.setCell(1,1,0.0);

                eval = new Evaluation(filteredTesting);

                CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
                //random forest cost sens
                costSensitiveClassifier.setClassifier(randomForestClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(filteredTraining);
                eval.evaluateModel(costSensitiveClassifier, filteredTesting);
                precision.get(15).add(eval.precision(0));
                recall.get(15).add(eval.recall(0));
                auc.get(15).add(eval.areaUnderROC(0));
                kappa.get(15).add(eval.kappa());
                fp.get(15).add(eval.numFalsePositives(0));
                fn.get(15).add(eval.numFalseNegatives(0));
                tp.get(15).add(eval.numTruePositives(0));
                tn.get(15).add(eval.numTrueNegatives(0));


                //naive Bayes cost sens
                eval = new Evaluation(testing);
                costSensitiveClassifier.setClassifier(naiveBayesClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(filteredTraining);
                eval.evaluateModel(costSensitiveClassifier, filteredTesting);
                precision.get(16).add(eval.precision(0));
                recall.get(16).add(eval.recall(0));
                auc.get(16).add(eval.areaUnderROC(0));
                kappa.get(16).add(eval.kappa());
                fp.get(16).add(eval.numFalsePositives(0));
                fn.get(16).add(eval.numFalseNegatives(0));
                tp.get(16).add(eval.numTruePositives(0));
                tn.get(16).add(eval.numTrueNegatives(0));


                //ibk cost sens
                eval = new Evaluation(testing);
                costSensitiveClassifier.setClassifier(ibkClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(filteredTraining);
                eval.evaluateModel(costSensitiveClassifier, filteredTesting);
                precision.get(17).add(eval.precision(0));
                recall.get(17).add(eval.recall(0));
                auc.get(17).add(eval.areaUnderROC(0));
                kappa.get(17).add(eval.kappa());
                fp.get(17).add(eval.numFalsePositives(0));
                fn.get(17).add(eval.numFalseNegatives(0));
                tp.get(17).add(eval.numTruePositives(0));
                tn.get(17).add(eval.numTrueNegatives(0));

            } else if (!fs && cs && os) {

                SMOTE smoteFilter = new SMOTE();
                smoteFilter.setInputFormat(training);
                Instances oversampledData = Filter.useFilter(training, smoteFilter);

                CostMatrix costMatrix = new CostMatrix(2); // 2x2 matrix -  CFN = 10*CFP
                costMatrix.setCell(0,0,0.0);
                costMatrix.setCell(1,0, 10.0);
                costMatrix.setCell(0,1,1.0);
                costMatrix.setCell(1,1,0.0);

                eval = new Evaluation(testing);

                CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();

                costSensitiveClassifier.setClassifier(randomForestClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(oversampledData);
                eval.evaluateModel(costSensitiveClassifier, testing);
                precision.get(18).add(eval.precision(0));
                recall.get(18).add(eval.recall(0));
                auc.get(18).add(eval.areaUnderROC(0));
                kappa.get(18).add(eval.kappa());
                fp.get(18).add(eval.numFalsePositives(0));
                fn.get(18).add(eval.numFalseNegatives(0));
                tp.get(18).add(eval.numTruePositives(0));
                tn.get(18).add(eval.numTrueNegatives(0));



                eval = new Evaluation(testing);
                costSensitiveClassifier.setClassifier(naiveBayesClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(oversampledData);
                eval.evaluateModel(costSensitiveClassifier, testing);
                precision.get(19).add(eval.precision(0));
                recall.get(19).add(eval.recall(0));
                auc.get(19).add(eval.areaUnderROC(0));
                kappa.get(19).add(eval.kappa());
                fp.get(19).add(eval.numFalsePositives(0));
                fn.get(19).add(eval.numFalseNegatives(0));
                tp.get(19).add(eval.numTruePositives(0));
                tn.get(19).add(eval.numTrueNegatives(0));



                eval = new Evaluation(testing);
                costSensitiveClassifier.setClassifier(ibkClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(oversampledData);
                eval.evaluateModel(costSensitiveClassifier, testing);
                precision.get(20).add(eval.precision(0));
                recall.get(20).add(eval.recall(0));
                auc.get(20).add(eval.areaUnderROC(0));
                kappa.get(20).add(eval.kappa());
                fp.get(20).add(eval.numFalsePositives(0));
                fn.get(20).add(eval.numFalseNegatives(0));
                tp.get(20).add(eval.numTruePositives(0));
                tn.get(20).add(eval.numTrueNegatives(0));




            } else if (fs && cs && os) {

                BestFirst search = new BestFirst();
                CfsSubsetEval evalSub = new CfsSubsetEval();

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(evalSub);
                filter.setSearch(search);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);

                SMOTE smoteFilter = new SMOTE();
                smoteFilter.setInputFormat(filteredTraining);
                Instances oversampledData = Filter.useFilter(filteredTraining, smoteFilter);

                CostMatrix costMatrix = new CostMatrix(2); // 2x2 matrix -  CFN = 10*CFP
                costMatrix.setCell(0,0,0.0);
                costMatrix.setCell(1,0, 10.0);
                costMatrix.setCell(0,1,1.0);
                costMatrix.setCell(1,1,0.0);

                eval = new Evaluation(filteredTesting);

                CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();

                costSensitiveClassifier.setClassifier(randomForestClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(oversampledData);
                eval.evaluateModel(costSensitiveClassifier, filteredTesting);
                precision.get(21).add(eval.precision(0));
                recall.get(21).add(eval.recall(0));
                auc.get(21).add(eval.areaUnderROC(0));
                kappa.get(21).add(eval.kappa());
                fp.get(21).add(eval.numFalsePositives(0));
                fn.get(21).add(eval.numFalseNegatives(0));
                tp.get(21).add(eval.numTruePositives(0));
                tn.get(21).add(eval.numTrueNegatives(0));



                eval = new Evaluation(filteredTesting);
                costSensitiveClassifier.setClassifier(naiveBayesClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(oversampledData);
                eval.evaluateModel(costSensitiveClassifier, filteredTesting);
                precision.get(22).add(eval.precision(0));
                recall.get(22).add(eval.recall(0));
                auc.get(22).add(eval.areaUnderROC(0));
                kappa.get(22).add(eval.kappa());
                fp.get(23).add(eval.numFalsePositives(0));
                fn.get(23).add(eval.numFalseNegatives(0));
                tp.get(23).add(eval.numTruePositives(0));
                tn.get(23).add(eval.numTrueNegatives(0));



                eval = new Evaluation(filteredTesting);
                costSensitiveClassifier.setClassifier(ibkClassifier);
                costSensitiveClassifier.setCostMatrix(costMatrix);
                costSensitiveClassifier.buildClassifier(oversampledData);
                eval.evaluateModel(costSensitiveClassifier, filteredTesting);
                precision.get(23).add(eval.precision(0));
                recall.get(23).add(eval.recall(0));
                auc.get(23).add(eval.areaUnderROC(0));
                kappa.get(23).add(eval.kappa());
                fp.get(24).add(eval.numFalsePositives(0));
                fn.get(24).add(eval.numFalseNegatives(0));
                tp.get(24).add(eval.numTruePositives(0));
                tn.get(24).add(eval.numTrueNegatives(0));

            }
        } catch (Exception e){
            System.out.println(e.getMessage());
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


    public static void main(String args[]) throws IOException {
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
                String trainingPath = "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\"+ projNameBis+ String.valueOf(v.getIndex()) +"Training.arff";
                String testingPath = "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\"+ projNameBis + String.valueOf(v.getIndex()) +"Testing.arff";
                classify(trainingPath, testingPath, false, false, false);
                classify(trainingPath, testingPath, true, false, false);
                classify(trainingPath, testingPath, false, true, false);
                classify(trainingPath, testingPath, false, false, true);
                classify(trainingPath, testingPath, true, true, false);
                classify(trainingPath, testingPath, true, false, true);
                classify(trainingPath, testingPath, false, true, true);
                classify(trainingPath, testingPath, true, true, true);

            }

            System.out.println("precision: " + precision + "\n recall: " + recall + "\n auc: " +
                    auc + "\n kappa: " + kappa + "\n tp: " + tp + "\n tn: " + tn + "\n fp: " + fp + "\n fn: " + fn);
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
            System.out.println(e.getStackTrace());

        }
    }

}
