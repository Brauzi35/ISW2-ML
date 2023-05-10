package control;

import model.Bug;
import model.FinalInstance;
import model.Version;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instance;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WekaController {

    private String projName;

    static String projNameBis;

    public WekaController(String projName) {
        this.projName = projName;
        projNameBis = projName;
    }

    static List<List<Double>> precision = new ArrayList<>();
    static List<List<Double>> recall = new ArrayList<>();
    static List<List<Double>> auc = new ArrayList<>();
    static List<List<Double>> kappa = new ArrayList<>();

    public void recalculator(String csvname, String arffname, String projectName, int versIdMax) throws Exception {
        JiraController jc = new JiraController(projectName);
        BugController bc = new BugController();
        List<Version> versions = jc.getAllVersions();
        //get partial
        versions = versions.subList(0, versIdMax);

        List<Bug> bugs = jc.getBugs(versions);


        ProportionController pc = new ProportionController();
        List<Bug> done = pc.iterativeProportion(bugs, versions);

        List<Bug> done1 = bc.bugTrimmer(done);



        List<Bug> av_bugs = bc.definitiveAvBuilder(done1, versions);

        CodeLineCounter clc = new CodeLineCounter("C:\\Users\\vlrbr\\Desktop\\" + this.projName);
        List<FinalInstance> finalInstances = clc.instanceListBuilder(projectName, versions);

        InstanceController ic = new InstanceController(projNameBis);
        List<FinalInstance> buggyFinalInstances = ic.isBuggy2(finalInstances, av_bugs);

        for(FinalInstance i : finalInstances) {
            if (buggyFinalInstances.contains(i)) {
                i.setBuggyness("Yes");
            }
        }

        CsvWriter csvw = new CsvWriter();
        csvw.csv_builder(finalInstances, csvname);

        ArffConverter ac = new ArffConverter();
        ac.csv2arff("C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\" + csvname, arffname);
    }


    public void walkForward(List<FinalInstance> finalInstances, List<Version> versions) throws Exception {
        //training set must be relistic, testing must be as true as possible
        for(Version v : versions.subList(1, versions.size())){
            String partialName = this.projName + String.valueOf(v.getIndex());
            //building testing set
            List<FinalInstance> instancesTesting = new ArrayList<>();
            List<String> arff_paths_testing = new ArrayList<>();
            List<String> arff_paths_training = new ArrayList<>();

            for(FinalInstance i : finalInstances){
                if(i.getVersion().equals(v.getName())){
                    instancesTesting.add(i);
                    //System.out.println("ciaeee " + i.getName() + " " + i.getVersion());
                }
            }



            CsvWriter csvw = new CsvWriter();
            csvw.csv_builder(instancesTesting, partialName + "Testing.csv");

            ArffConverter ac = new ArffConverter();
            ac.csv2arff("C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\" + partialName + "Testing.csv", partialName + "Testing.arff");
            //testing done
            //"C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\" + partialName + "Testing.arff"
            arff_paths_testing.add("C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\" + partialName + "Testing.arff");
            //"C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\" + partialName + "Training.arff"

            //training builder
            recalculator(partialName+"Training.csv",partialName+"Training.arff", this.projName.toUpperCase(),v.getIndex()-1);
            arff_paths_training.add("C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\" + partialName + "Training.arff");
        }


    }


    public static void randomForestEval(Instances training, Instances testing, Evaluation eval){
        try{
            RandomForest randomForestClassifier = new RandomForest();
            randomForestClassifier.buildClassifier(training);
            eval.evaluateModel(randomForestClassifier, testing);

            precision.get(0).add(eval.precision(0));
            recall.get(0).add(eval.recall(0));
            kappa.get(0).add(eval.kappa());
            auc.get(0).add(eval.areaUnderROC(0));


        } catch (Exception e){
            System.out.println("Random forest error! \n"
                    + e.getMessage());
        }


    }

    //fs= feature selection without sampling (greedy backward search)
    public static void randomForestEvalFS(Instances training, Instances testing, Evaluation eval){
        double precisionDmax = 0.0;
        double recallDmax = 0.0;
        double aucDmax = 0.0;
        double kappaDmax = 0.0;
        double precisionD = 0.0;
        double recallD = 0.0;
        double aucD = 0.0;
        double kappaD = 0.0;
        try{
            for(int i = 1; i<11; i++) {
                CfsSubsetEval subsetEval = new CfsSubsetEval();
                BestFirst search = new BestFirst();
                String[] options = {"-N", String.valueOf(i)};
                search.setOptions(options);


                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(subsetEval);
                filter.setSearch(search);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);

                RandomForest randomForestClassifier = new RandomForest();
                randomForestClassifier.buildClassifier(filteredTraining);
                eval.evaluateModel(randomForestClassifier, filteredTesting);

                precisionD = eval.precision(0);
                recallD = eval.recall(0);
                kappaD = eval.kappa();
                aucD = eval.areaUnderROC(0);
                if(precisionD>precisionDmax && recallD>recallDmax){
                    precisionDmax = precisionD;
                    recallDmax = recallD;
                    kappaDmax = kappaD;
                    aucDmax = aucD;
                }
            }

            precision.get(1).add(precisionDmax);
            recall.get(1).add(recallDmax);
            kappa.get(1).add(kappaDmax);
            auc.get(1).add(aucDmax);


        } catch (Exception e){
            System.out.println("Random forest feature selection error! \n"
                    + e.getMessage());
        }


    }

    public static void randomForestEvalFSOS(Instances training, Instances testing, Evaluation eval) throws Exception {
        double precisionDmax = 0.0;
        double recallDmax = 0.0;
        double aucDmax = 0.0;
        double kappaDmax = 0.0;
        double precisionD = 0.0;
        double recallD = 0.0;
        double aucD = 0.0;
        double kappaD = 0.0;
        //smote oversampling
        SMOTE smote = new SMOTE();
        smote.setInputFormat(training);
        training = Filter.useFilter(training, smote);


        try{
            for(int i = 1; i<11; i++) {
                CfsSubsetEval subsetEval = new CfsSubsetEval();
                BestFirst search = new BestFirst();
                String[] options = {"-N", String.valueOf(i)};
                search.setOptions(options);


                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(subsetEval);
                filter.setSearch(search);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);

                RandomForest randomForestClassifier = new RandomForest();
                randomForestClassifier.buildClassifier(filteredTraining);
                eval.evaluateModel(randomForestClassifier, filteredTesting);

                precisionD = eval.precision(0);
                recallD = eval.recall(0);
                kappaD = eval.kappa();
                aucD = eval.areaUnderROC(0);
                if(precisionD>precisionDmax && recallD>recallDmax){
                    precisionDmax = precisionD;
                    recallDmax = recallD;
                    kappaDmax = kappaD;
                    aucDmax = aucD;
                }
            }

            precision.get(2).add(precisionDmax);
            recall.get(2).add(recallDmax);
            kappa.get(2).add(kappaDmax);
            auc.get(2).add(aucDmax);


        } catch (Exception e){
            System.out.println("Random forest feature selection smote error! \n"
                    + e.getMessage());
        }


    }



    public static void naiveBayesEval(Instances training, Instances testing, Evaluation eval){
        try{
            NaiveBayes naiveBayesClassifier = new NaiveBayes();
            naiveBayesClassifier.buildClassifier(training);
            eval.evaluateModel(naiveBayesClassifier, testing);

            precision.get(3).add(eval.precision(0));
            recall.get(3).add(eval.recall(0));
            kappa.get(3).add(eval.kappa());
            auc.get(3).add(eval.areaUnderROC(0));


        } catch (Exception e){
            System.out.println("Naive Bayes error! \n"
                    + e.getMessage());
        }


    }


    public static void naiveBayesEvalFS(Instances training, Instances testing, Evaluation eval){
        double precisionDmax = 0.0;
        double recallDmax = 0.0;
        double aucDmax = 0.0;
        double kappaDmax = 0.0;
        double precisionD = 0.0;
        double recallD = 0.0;
        double aucD = 0.0;
        double kappaD = 0.0;
        try{
            for (int i = 1; i<11; i++){
                CfsSubsetEval subsetEval = new CfsSubsetEval();
                BestFirst search = new BestFirst();
                String[] options = {"-N", String.valueOf(i)};
                search.setOptions(options);

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(subsetEval);
                filter.setSearch(search);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);

                NaiveBayes naiveBayes = new NaiveBayes();
                naiveBayes.buildClassifier(filteredTraining);
                eval.evaluateModel(naiveBayes, filteredTesting);


                precisionD = eval.precision(0);
                recallD = eval.recall(0);
                kappaD = eval.kappa();
                aucD = eval.areaUnderROC(0);
                if(precisionD>precisionDmax && recallD>recallDmax){
                    precisionDmax = precisionD;
                    recallDmax = recallD;
                    kappaDmax = kappaD;
                    aucDmax = aucD;
                }
            }

            precision.get(4).add(precisionDmax);
            recall.get(4).add(recallDmax);
            kappa.get(4).add(kappaDmax);
            auc.get(4).add(aucDmax);


        } catch (Exception e){
            System.out.println("Naive feature selection error! \n"
                    + e.getMessage());
        }


    }

    public static void naiveBayesEvalFSOS(Instances training, Instances testing, Evaluation eval) throws Exception {
        double precisionDmax = 0.0;
        double recallDmax = 0.0;
        double aucDmax = 0.0;
        double kappaDmax = 0.0;
        double precisionD = 0.0;
        double recallD = 0.0;
        double aucD = 0.0;
        double kappaD = 0.0;

        //smote oversampling
        SMOTE smote = new SMOTE();
        smote.setInputFormat(training);
        training = Filter.useFilter(training, smote);
        try{
            for (int i = 1; i<11; i++){
                CfsSubsetEval subsetEval = new CfsSubsetEval();
                BestFirst search = new BestFirst();
                String[] options = {"-N", String.valueOf(i)};
                search.setOptions(options);

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(subsetEval);
                filter.setSearch(search);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);

                NaiveBayes naiveBayes = new NaiveBayes();
                naiveBayes.buildClassifier(filteredTraining);
                eval.evaluateModel(naiveBayes, filteredTesting);


                precisionD = eval.precision(0);
                recallD = eval.recall(0);
                kappaD = eval.kappa();
                aucD = eval.areaUnderROC(0);
                if(precisionD>precisionDmax && recallD>recallDmax){
                    precisionDmax = precisionD;
                    recallDmax = recallD;
                    kappaDmax = kappaD;
                    aucDmax = aucD;
                }
            }

            precision.get(5).add(precisionDmax);
            recall.get(5).add(recallDmax);
            kappa.get(5).add(kappaDmax);
            auc.get(5).add(aucDmax);


        } catch (Exception e){
            System.out.println("Naive feature selection error! \n"
                    + e.getMessage());
        }


    }

    public static void ibkEvalFSOS(Instances training, Instances testing, Evaluation eval) throws Exception {

        //smote oversampling
        SMOTE smote = new SMOTE();
        smote.setInputFormat(training);
        training = Filter.useFilter(training, smote);

        try{
            double precisionDmax = 0.0;
            double recallDmax = 0.0;
            double aucDmax = 0.0;
            double kappaDmax = 0.0;
            double precisionD = 0.0;
            double recallD = 0.0;
            double aucD = 0.0;
            double kappaD = 0.0;

            for(int i = 1; i<11; i++) {
                CfsSubsetEval subsetEval = new CfsSubsetEval();
                BestFirst search = new BestFirst();
                String[] options = {"-N", String.valueOf(i)};
                search.setOptions(options);

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(subsetEval);
                filter.setSearch(search);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);


                IBk ibkClassifier = new IBk();
                ibkClassifier.buildClassifier(filteredTraining);
                eval.evaluateModel(ibkClassifier, filteredTesting);

                precisionD = eval.precision(0);
                recallD = eval.recall(0);
                kappaD = eval.kappa();
                aucD = eval.areaUnderROC(0);
                if(precisionD>precisionDmax && recallD>recallDmax){
                    precisionDmax = precisionD;
                    recallDmax = recallD;
                    kappaDmax = kappaD;
                    aucDmax = aucD;
                }
            }

            precision.get(8).add(precisionDmax);
            recall.get(8).add(recallDmax);
            kappa.get(8).add(kappaDmax);
            auc.get(8).add(aucDmax);


        } catch (Exception e){
            System.out.println("Naive feature selection error! \n"
                    + e.getMessage());
        }


    }

    public static void ibkEvalFS(Instances training, Instances testing, Evaluation eval){
        try{
            double precisionDmax = 0.0;
            double recallDmax = 0.0;
            double aucDmax = 0.0;
            double kappaDmax = 0.0;
            double precisionD = 0.0;
            double recallD = 0.0;
            double aucD = 0.0;
            double kappaD = 0.0;

            for(int i = 1; i<11; i++) {
                CfsSubsetEval subsetEval = new CfsSubsetEval();
                BestFirst search = new BestFirst();
                String[] options = {"-N", String.valueOf(i)};
                search.setOptions(options);

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(subsetEval);
                filter.setSearch(search);
                filter.setInputFormat(training);

                Instances filteredTraining = Filter.useFilter(training, filter);
                Instances filteredTesting = Filter.useFilter(testing, filter);

                int numAttrFiltered = filteredTraining.numAttributes();
                filteredTraining.setClassIndex(numAttrFiltered - 1);


                IBk ibkClassifier = new IBk();
                ibkClassifier.buildClassifier(filteredTraining);
                eval.evaluateModel(ibkClassifier, filteredTesting);

                precisionD = eval.precision(0);
                recallD = eval.recall(0);
                kappaD = eval.kappa();
                aucD = eval.areaUnderROC(0);
                if(precisionD>precisionDmax && recallD>recallDmax){
                    precisionDmax = precisionD;
                    recallDmax = recallD;
                    kappaDmax = kappaD;
                    aucDmax = aucD;
                }
            }

            precision.get(7).add(precisionDmax);
            recall.get(7).add(recallDmax);
            kappa.get(7).add(kappaDmax);
            auc.get(7).add(aucDmax);


        } catch (Exception e){
            System.out.println("Naive feature selection error! \n"
                    + e.getMessage());
        }


    }

    public static void ibkEval(Instances training, Instances testing, Evaluation eval){
        try{
            IBk ibkClassifier = new IBk();
            ibkClassifier.buildClassifier(training);
            eval.evaluateModel(ibkClassifier, testing);

            precision.get(6).add(eval.precision(0));
            recall.get(6).add(eval.recall(0));
            kappa.get(6).add(eval.kappa());
            auc.get(6).add(eval.areaUnderROC(0));


        } catch (Exception e){
            System.out.println("IBk error! \n"
                    + e.getMessage());
        }


    }
    public static void wekaFlowClassification(String trainingPath, String testingPath) throws Exception {
        try {
            DataSource source = new DataSource(trainingPath);
            Instances training = source.getDataSet();
            DataSource source1 = new DataSource(testingPath);
            Instances testing = source1.getDataSet();

            int numAttr = training.numAttributes();
            training.setClassIndex(numAttr - 1);
            testing.setClassIndex(numAttr - 1);

            Evaluation eval = new Evaluation(testing);

            randomForestEval(training, testing, eval);
            ibkEval(training, testing, eval);
            naiveBayesEval(training, testing, eval);
            randomForestEvalFS(training, testing, eval);
            naiveBayesEvalFS(training, testing, eval);
            ibkEvalFS(training, testing, eval);
            randomForestEvalFSOS(training, testing, eval);
            naiveBayesEvalFSOS(training, testing, eval);
            ibkEvalFSOS(training, testing, eval);







        } catch (Exception e){
            System.out.println("Data source error! \n"
                    + e.getMessage());
        }
    }



    public static void main(String args[]){
        try {
            //9 classifiers
            for(int n = 0; n<9; n++){
                recall.add(new ArrayList<>());
                precision.add(new ArrayList<>());
                auc.add(new ArrayList<>());
                kappa.add(new ArrayList<>());
            }

            for(int i = 2; i<6; i++){
                System.out.println("iteration: " + (i-2));
                String trainingPath = "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\"+ projNameBis+ String.valueOf(i) +"Training.arff";
                String testingPath = "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\"+ projNameBis + String.valueOf(i) +"Testing.arff";
                wekaFlowClassification(trainingPath, testingPath);

            }


                System.out.println("precision: " + precision + "\n recall: " + recall + "\n auc: " + auc + "\n kappa: " + kappa);






        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            // Print message on the console
            System.out.println("Error Occurred!!!! \n"
                    + e.getMessage());
        }
    }



}
