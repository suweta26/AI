/*
 * This is the driver function which is used to start our system.
 */
package iitg.cs561.project.alphabet;

import iitg.cs561.project.alphabet.activity.ActivityProcessor;
import iitg.cs561.project.alphabet.util.AlphabetConstants;
import iitg.cs561.project.alphabet.util.CommonVarMethods;
import iitg.cs561.project.alphabet.util.OutputPrinter;

/**
 *
 * @author TeamAlphabet
 */
public class DriverImpl {

    public static void main(String args[]) {
        try {

            // Initialize the class which handles Common Variables and Methods
            CommonVarMethods cvm = new CommonVarMethods();
            System.out.println("#################### Initializing Common Variables and Methods ####################");
            cvm.initialize();

            System.out.println("####################Reading Data Set####################");

            DataFileProcessor fp = new DataFileProcessor();

            fp.readFile(AlphabetConstants.DATA_FILE_PATH, cvm);

            System.out.println("#################### Initializing Activity Processor ####################");

            ActivityProcessor ap = new ActivityProcessor();

            System.out.println("");

            System.out.println("#################### Define the domain for features ####################");
            
                fp.featureValuesRange();
            

            OutputPrinter op = new OutputPrinter();

            System.out.println("#################### Start Activity Recognition Process ####################");
            ap.recognizeAndPredictActivity(fp, cvm);

            op.printResults(fp, cvm);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
