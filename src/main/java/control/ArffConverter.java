package control;


import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;


public class ArffConverter {



    public void csv2arff(String csvpath,String arffname) throws Exception {

        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csvpath));
        Instances data = loader.getDataSet();//get instances object

        String[] options = new String[]{"-R", "2"};
        Remove removeFilter = new Remove();
        removeFilter.setOptions(options);
        removeFilter.setInputFormat(data);
        Instances newData = Filter.useFilter(data, removeFilter);
        //non funziona fino a save arff



        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(newData);//set the dataset we want to convert
        //and save as ARFF
        saver.setFile(new File("C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\" + arffname));
        saver.writeBatch();

    }

}
