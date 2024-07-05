package control;

import model.Bug;
import model.FinalInstance;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class WekaController {

    private static final Logger logger = Logger.getLogger(WekaController.class.getName());
    private static final String DEFAULT_PATH = "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\";
    private String projName;
    private String path;

    public WekaController(String projName) {
        this.projName = projName;
        this.path = DEFAULT_PATH;
    }

    static List<List<Double>> precision = new ArrayList<>();
    static List<List<Double>> recall = new ArrayList<>();
    static List<List<Double>> auc = new ArrayList<>();
    static List<List<Double>> kappa = new ArrayList<>();

    public void recalculator(String csvname, String arffname, String projectName, int versIdMax) throws Exception {
        JiraController jc = new JiraController(projectName);
        BugController bc = new BugController();
        List<Version> versions = jc.getAllVersions();
        versions = versions.subList(0, versIdMax);

        List<Bug> bugs = jc.getBugs(versions);

        ProportionController pc = new ProportionController();
        List<Bug> done = pc.iterativeProportion(bugs, versions);

        List<Bug> done1 = bc.bugTrimmer(done);

        List<Bug> avBugs = bc.definitiveAvBuilder(done1, versions);

        CodeLineCounter clc = new CodeLineCounter("C:\\Users\\vlrbr\\Desktop\\Testing\\" + this.projName);
        List<FinalInstance> finalInstances = clc.instanceListBuilder(projectName, versions);

        InstanceController ic = new InstanceController(this.projName);
        List<FinalInstance> buggyFinalInstances = ic.isBuggy2(finalInstances, avBugs);

        for (FinalInstance i : finalInstances) {
            if (buggyFinalInstances.contains(i)) {
                i.setBuggyness("Yes");
            }
        }

        CsvWriter csvw = new CsvWriter();
        csvw.csvBuilder(finalInstances, csvname);

        ArffConverter ac = new ArffConverter();
        ac.csv2arff(this.path + csvname, arffname);
    }

    public void walkForward(List<FinalInstance> finalInstances, List<Version> versions) throws Exception {
        for (Version v : versions.subList(1, versions.size())) {
            String partialName = this.projName + String.valueOf(v.getIndex());
            List<FinalInstance> instancesTesting = new ArrayList<>();
            List<String> arffpathstesting = new ArrayList<>();
            List<String> arffpathstraining = new ArrayList<>();

            for (FinalInstance i : finalInstances) {
                if (i.getVersion().equals(v.getName())) {
                    instancesTesting.add(i);
                }
            }

            String ta = "Testing.csv";
            String tarff = "Testing.arff";
            String trarff = "Training.arff";

            CsvWriter csvw = new CsvWriter();
            csvw.csvBuilder(instancesTesting, partialName + ta);

            ArffConverter ac = new ArffConverter();
            ac.csv2arff(this.path + partialName + ta, partialName + tarff);

            arffpathstesting.add(this.path + partialName + tarff);

            recalculator(partialName + "Training.csv", partialName + trarff, this.projName.toUpperCase(), v.getIndex() - 1);
            arffpathstraining.add(this.path + partialName + trarff);
        }
    }

    public static void randomForestEval(Instances training, Instances testing, Evaluation eval) {
        try {
            RandomForest randomForestClassifier = new RandomForest();
            randomForestClassifier.buildClassifier(training);
            eval.evaluateModel(randomForestClassifier, testing);

            precision.get(0).add(eval.precision(0));
            recall.get(0).add(eval.recall(0));
            kappa.get(0).add(eval.kappa());
            auc.get(0).add(eval.areaUnderROC(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Random forest error! ", e);
        }
    }

    public static void randomForestEvalFS(Instances training, Instances testing, Evaluation eval) {
        try {
            CfsSubsetEval subsetEval = new CfsSubsetEval();
            BestFirst search = new BestFirst();

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

            precision.get(1).add(eval.precision(0));
            recall.get(1).add(eval.recall(0));
            kappa.get(1).add(eval.kappa());
            auc.get(1).add(eval.areaUnderROC(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Random forest feature selection error! ", e);
        }
    }

    public static void randomForestEvalFSOS(Instances training, Instances testing, Evaluation eval) throws Exception {
        try {
            SMOTE smote = new SMOTE();
            smote.setInputFormat(training);
            training = Filter.useFilter(training, smote);

            CfsSubsetEval subsetEval = new CfsSubsetEval();
            BestFirst search = new BestFirst();

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

            precision.get(2).add(eval.precision(0));
            recall.get(2).add(eval.recall(0));
            kappa.get(2).add(eval.kappa());
            auc.get(2).add(eval.areaUnderROC(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Random forest feature selection smote error! ", e);
        }
    }

    public static void naiveBayesEval(Instances training, Instances testing, Evaluation eval) {
        try {
            NaiveBayes naiveBayesClassifier = new NaiveBayes();
            naiveBayesClassifier.buildClassifier(training);
            eval.evaluateModel(naiveBayesClassifier, testing);

            precision.get(3).add(eval.precision(0));
            recall.get(3).add(eval.recall(0));
            kappa.get(3).add(eval.kappa());
            auc.get(3).add(eval.areaUnderROC(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Naive Bayes error! ", e);
        }
    }

    public static void naiveBayesEvalFS(Instances training, Instances testing, Evaluation eval) {
        try {
            CfsSubsetEval subsetEval = new CfsSubsetEval();
            BestFirst search = new BestFirst();

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

            precision.get(4).add(eval.precision(0));
            recall.get(4).add(eval.recall(0));
            kappa.get(4).add(eval.kappa());
            auc.get(4).add(eval.areaUnderROC(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Naive feature selection error! ", e);
        }
    }

    public static void naiveBayesEvalFSOS(Instances training, Instances testing, Evaluation eval) throws Exception {
        try {
            SMOTE smote = new SMOTE();
            smote.setInputFormat(training);
            training = Filter.useFilter(training, smote);

            CfsSubsetEval subsetEval = new CfsSubsetEval();
            BestFirst search = new BestFirst();

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

            precision.get(5).add(eval.precision(0));
            recall.get(5).add(eval.recall(0));
            kappa.get(5).add(eval.kappa());
            auc.get(5).add(eval.areaUnderROC(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Naive feature selection error! ", e);
        }
    }

    public static void ibkEvalFSOS(Instances training, Instances testing, Evaluation eval) throws Exception {
        try {
            SMOTE smote = new SMOTE();
            smote.setInputFormat(training);
            training = Filter.useFilter(training, smote);

            CfsSubsetEval subsetEval = new CfsSubsetEval();
            BestFirst search = new BestFirst();

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

            precision.get(8).add(eval.precision(0));
            recall.get(8).add(eval.recall(0));
            kappa.get(8).add(eval.kappa());
            auc.get(8).add(eval.areaUnderROC(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "IBk feature selection error! ", e);
        }
    }

    public static void ibkEvalFS(Instances training, Instances testing, Evaluation eval) {
        try {
            CfsSubsetEval subsetEval = new CfsSubsetEval();
            BestFirst search = new BestFirst();

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

            precision.get(7).add(eval.precision(0));
            recall.get(7).add(eval.recall(0));
            kappa.get(7).add(eval.kappa());
            auc.get(7).add(eval.areaUnderROC(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "IBk feature selection error! ", e);
        }
    }

    public static void ibkEval(Instances training, Instances testing, Evaluation eval) {
        try {
            IBk ibkClassifier = new IBk();
            ibkClassifier.buildClassifier(training);
            eval.evaluateModel(ibkClassifier, testing);

            precision.get(6).add(eval.precision(0));
            recall.get(6).add(eval.recall(0));
            kappa.get(6).add(eval.kappa());
            auc.get(6).add(eval.areaUnderROC(0));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "IBk error! ", e);
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
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Data source error! ", e);
        }
    }

    public void main() {
        try {
            this.projName = "bookkeeper"; //bookkeeper or storm
            //9 classifiers
            for (int n = 0; n < 9; n++) {
                recall.add(new ArrayList<>());
                precision.add(new ArrayList<>());
                auc.add(new ArrayList<>());
                kappa.add(new ArrayList<>());
            }
            JiraController jc = new JiraController(this.projName.toUpperCase());
            List<Version> versions = jc.getAllVersions();
            versions = versions.subList(0, versions.size() / 2);

            for (Version v : versions) {
                String trainingPath = this.path + this.projName + v.getIndex() + "Training.arff";
                String testingPath = this.path + this.projName + v.getIndex() + "Testing.arff";
                wekaFlowClassification(trainingPath, testingPath);
            }
            String msg = "precision: " + precision + "\n recall: " + recall + "\n auc: " + auc + "\n kappa: " + kappa;
            logger.log(Level.INFO, msg);

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
}
