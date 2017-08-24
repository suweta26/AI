/*
 * This part of code is use to call training and testing methods of different Models
 */
package iitg.cs561.project.alphabet.activity;

import iitg.cs561.project.alphabet.DataFileProcessor;
import iitg.cs561.project.alphabet.algorithm.HiddenMarkovModelImpl;
import iitg.cs561.project.alphabet.algorithm.NaiveBayesClassifierImpl;
import iitg.cs561.project.alphabet.util.AlphabetConstants;
import iitg.cs561.project.alphabet.util.CommonVarMethods;

/**
 *
 * @author TeamAlphabet
 */
public class ActivityProcessor {

    public void recognizeAndPredictActivity(DataFileProcessor fp, CommonVarMethods cvm) {
        System.out.println("#################### Activity Recognition Process ####################");

        // Intilialize training parameters
        cvm.initializeTrainingParameters();

        // Model is HMM
        if (AlphabetConstants.MODEL.equalsIgnoreCase("HMM")) {
            HiddenMarkovModelImpl hmmImpl = new HiddenMarkovModelImpl();
            System.out.println("#################### HMM Training ####################");
            if (AlphabetConstants.MODE.equalsIgnoreCase("TRAIN") || AlphabetConstants.MODE.equalsIgnoreCase("TRAIN_TEST")) {
                hmmImpl.training(fp, cvm);
            }
            System.out.println("####################  HMM Testing ####################");
            if (AlphabetConstants.MODE.equalsIgnoreCase("TEST") || AlphabetConstants.MODE.equalsIgnoreCase("TRAIN_TEST")) {
                hmmImpl.test(fp, cvm);
            }
        }

        // Model is NB
        if (AlphabetConstants.MODEL.equalsIgnoreCase("NB")) {
            NaiveBayesClassifierImpl nbImpl = new NaiveBayesClassifierImpl();
            System.out.println("#################### NB Training ####################");
            if (AlphabetConstants.MODE.equalsIgnoreCase("TRAIN") || AlphabetConstants.MODE.equalsIgnoreCase("TRAIN_TEST")) {
                nbImpl.training(fp, cvm);
            }
            System.out.println("#################### NB Testing ####################");
            if (AlphabetConstants.MODE.equalsIgnoreCase("TEST") || AlphabetConstants.MODE.equalsIgnoreCase("TRAIN_TEST")) {
                nbImpl.test(fp, cvm);
            }
        }

    }

}
