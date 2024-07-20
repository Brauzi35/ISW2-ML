package control;

import com.opencsv.CSVWriter;
import model.Version;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AcumeController {

    private static final Logger logger = Logger.getLogger(AcumeController.class.getName());
    static final String projNameBis = "bookkeeper"; //bookkeeper or storm
    static int iteration = 0;


    public static int findLargestNumericAttributeIndex(Instance instance) {
        int largestNumericIndex = -1;
        double largestValue = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < instance.numAttributes(); i++) {
            if (instance.attribute(i).isNumeric()) {
                double value = instance.value(i);
                if (value > largestValue) {
                    largestValue = value;
                    largestNumericIndex = i;
                }
            }
        }

        return largestNumericIndex;
    }

    public static int findLastAttributeIndex(Instance instance) {
        return instance.numAttributes() - 1;
    }
    public static void acumeFiles(Instances testing, Classifier classifier, int idx, String filename, int version) throws Exception {
        logger.log(Level.INFO, "Starting acumeFiles method with classifier: " + filename + " and index: " + idx);
        // Define the directory and file path
        System.out.println(idx+filename);
        String directoryPath = "ACUME";
        String filePath = directoryPath + "/" + idx + filename + "_tv"+(version-1)+".csv";

        // Create the directory if it doesn't exist
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Create the file
        if(iteration == 0) {
            iteration++;
            File file = new File(filePath);
            if (file.exists()) {
                file.delete(); // Delete the file if it exists
            }
            file.createNewFile(); // Create a new file
        }

        // Create FileWriter and CSVWriter objects
        FileWriter outputfile = new FileWriter(filePath);
        CSVWriter writer = new CSVWriter(outputfile, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

        // Write header to the CSV file
        String[] header = {"Instance", "Size", "Probability (Yes)", "Actual Buggy"};
        writer.writeNext(header);

        // Predict probabilities for each instance in the testing data and write to CSV
        for (int i = 0; i < testing.numInstances(); i++) {
            try {
                Instance instance = testing.instance(i);
                int l = findLastAttributeIndex(instance);//10;
                int size= findLargestNumericAttributeIndex(instance);//3;
                double[] distribution = classifier.distributionForInstance(instance);
                String[] data = {
                        String.valueOf(i + 1),
                        String.valueOf(instance.value(size)),
                        String.valueOf(distribution[1]),
                        instance.stringValue(l)
                };

                writer.writeNext(data);
            }catch (Exception e){
                logger.log(Level.SEVERE, "exceprion in probs");
            }

        }

        // Close the writer
        writer.close();

        logger.log(Level.INFO, "Completed acumeFiles method with classifier: " + filename + " and index: " + idx);
    }


    public static void acume(String trainingPath, String testingPath, boolean os, boolean fs, boolean cs, int index, int version) throws Exception {
        logger.log(Level.INFO, "Starting acume method with trainingPath: " + trainingPath + ", testingPath: " + testingPath + ", os: " + os + ", fs: " + fs + ", cs: " + cs + ", index: " + index);

        ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainingPath);
        Instances training = source.getDataSet();
        ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(testingPath);
        Instances testing = source2.getDataSet();

        RandomForest randomForestClassifier = new RandomForest();
        NaiveBayes naiveBayesClassifier = new NaiveBayes();
        IBk ibkClassifier = new IBk();

        int numAttr = training.numAttributes();
        training.setClassIndex(numAttr - 1);
        testing.setClassIndex(numAttr - 1);

        try {
            Instances filteredTraining = training;
            Instances filteredTesting = testing;

            if (fs) {
                logger.log(Level.INFO, "Feature selection is enabled");
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

            if (os) {
                logger.log(Level.INFO, "Over-sampling is enabled");
                SMOTE smoteFilter = new SMOTE();
                smoteFilter.setInputFormat(training);
                filteredTraining = Filter.useFilter(training, smoteFilter); //faccio solo il training perché sto facendo OVER
            }

            if (!cs && (filteredTraining.numInstances() > 0 && filteredTesting.numInstances() > 0)) {
                logger.log(Level.INFO, "Cost-sensitive classification is disabled");


                    logger.log(Level.INFO, "Building and evaluating RandomForest classifier");
                    randomForestClassifier.buildClassifier(filteredTraining);
                    acumeFiles(filteredTesting, randomForestClassifier, index, "RandomForest", version);



                    logger.log(Level.INFO, "Building and evaluating NaiveBayes classifier");
                    naiveBayesClassifier.buildClassifier(filteredTraining);
                    acumeFiles(filteredTesting, naiveBayesClassifier, index, "NaiveBayes", version);



                    logger.log(Level.INFO, "Building and evaluating IBk classifier");
                    ibkClassifier.buildClassifier(filteredTraining);

                    acumeFiles(filteredTesting, ibkClassifier, index, "IBk", version);


            }

            if (cs && (filteredTraining.numInstances() > 0 && filteredTesting.numInstances() > 0)) {
                logger.log(Level.INFO, "Cost-sensitive classification is enabled");

                CostMatrix costMatrix = new CostMatrix(2); // 2x2 matrix -  CFN = 10*CFP
                costMatrix.setCell(0, 0, 0.0);
                costMatrix.setCell(1, 0, 10.0);
                costMatrix.setCell(0, 1, 1.0);
                costMatrix.setCell(1, 1, 0.0);

                CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();


                    logger.log(Level.INFO, "Building and evaluating CostSensitive RandomForest classifier");
                    costSensitiveClassifier.setClassifier(randomForestClassifier);
                    costSensitiveClassifier.setCostMatrix(costMatrix);
                    costSensitiveClassifier.buildClassifier(filteredTraining);
                    acumeFiles(filteredTesting, costSensitiveClassifier, index, "RandomForest", version);



                    logger.log(Level.INFO, "Building and evaluating CostSensitive NaiveBayes classifier");
                    costSensitiveClassifier.setClassifier(naiveBayesClassifier);
                    costSensitiveClassifier.setCostMatrix(costMatrix);
                    costSensitiveClassifier.buildClassifier(filteredTraining);
                    acumeFiles(filteredTesting, costSensitiveClassifier, index, "NaiveBayes", version);



                    logger.log(Level.INFO, "Building and evaluating CostSensitive IBk classifier");
                    costSensitiveClassifier.setClassifier(ibkClassifier);
                    costSensitiveClassifier.setCostMatrix(costMatrix);
                    costSensitiveClassifier.buildClassifier(filteredTraining);
                    acumeFiles(filteredTesting, costSensitiveClassifier, index, "IBk", version);


            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in acume method", e);
        }

        logger.log(Level.INFO, "Completed acume method with trainingPath: " + trainingPath + ", testingPath: " + testingPath + ", os: " + os + ", fs: " + fs + ", cs: " + cs + ", index: " + index);
    }

    public static void main(String[] args) throws Exception {
        try {


            JiraController jc = new JiraController(projNameBis.toUpperCase());
            List<Version> versions = jc.getAllVersions();
            versions = versions.subList(0, versions.size() / 2);

            for (Version v : versions) {
                if (v.getIndex() == 0 || v.getIndex() == 1) {
                    continue;
                }
                String trainingPath = "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\" + projNameBis + v.getIndex() + "Training.arff";
                String testingPath = "C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\" + projNameBis + v.getIndex() + "Testing.arff";
                logger.log(Level.INFO, "Processing version: " + v.getIndex());
                acume(trainingPath, testingPath, false, false, false, 0, v.getIndex());
                acume(trainingPath, testingPath, true, false, false, 3, v.getIndex());
                acume(trainingPath, testingPath, false, true, false, 6, v.getIndex());
                acume(trainingPath, testingPath, false, false, true, 9, v.getIndex());
                acume(trainingPath, testingPath, true, false, true, 15, v.getIndex());
                acume(trainingPath, testingPath, false, true, true, 18, v.getIndex());
            }
        }catch (Exception e){
            Logger logger = Logger.getLogger(JiraController.class.getName());
            String out = "Error in acume";
            logger.log(Level.SEVERE, out);


        }
    }
}
