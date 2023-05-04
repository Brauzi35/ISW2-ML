package control;


import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import java.io.File;
public class ArffConverter {

    public void csv2arff() throws Exception {

        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\output.csv"));
        Instances data = loader.getDataSet();//get instances object

        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);//set the dataset we want to convert
        //and save as ARFF
        saver.setFile(new File("C:\\Users\\vlrbr\\IdeaProjects\\ISW2-ML\\output.arff"));
        saver.writeBatch();
    }

}
