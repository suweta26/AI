/*
 * Hidden Markov Model implementation for Activity Recognition & Prediction
 */
package iitg.cs561.project.alphabet.algorithm;

import dnl.utils.text.table.TextTable;
import iitg.cs561.project.alphabet.DataFileProcessor;
import iitg.cs561.project.alphabet.util.AlphabetConstants;
import iitg.cs561.project.alphabet.util.CommonVarMethods;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 *
 * @author Team Alphabet
 */
public class HiddenMarkovModelImpl {

    public void training(DataFileProcessor fp, CommonVarMethods cvm) {
        int activityOccurenceLength, eventId, activityStartEventId;
      
        for (int i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
               
            // Look at each activity occurrence
                for (int j = 0; j < fp.getActivityCountInWhole()[i]; j++) {
                        activityStartEventId = fp.getActivityStartEventId()[i][j];
                        activityOccurenceLength = fp.getActivityLength()[i][j];

                        for (int k = 0, count = 0; count < activityOccurenceLength; k++) {
                            
                            
                            eventId = fp.getEvents()[activityStartEventId + k][AlphabetConstants.SENSOR_USED];
                            
                            // Get the features for this event
                           
                                cvm.holdState(fp.getEvents()[activityStartEventId + k], fp.getSizes()[i][j], fp.getPreviousActivity()[i][j], fp, cvm);

                                // Update frequency for these sensor event values for this activity
                                cvm.increseSensorFreqByOne(i, eventId);
                                
                                // Add this event as evidence for this activity which will be used for calculating
                                // Pr(Evidence|Activity) using MLE
                                cvm.updateEvidence(i);
                                
                                // Increase the total number of events for this activity
                                cvm.increseTotalSensorEventsForActivity(i);
                                count++;

                          
                        }

                        // Update transition frequency from previous activity to this activity which will be used for calculating
                      // Pr(Prev|Ai) using MLE
                        cvm.updateTransitionCount(fp.getPreviousActivity()[i][j], i);
                }
            }
            
            // Calculate prior probability for each activity
         cvm.calculatePriorProbabilities();

        // Calculate feature value probabilities for each activity (hidden state)
        cvm.CalculateEmission();
        cvm.normalizeTransitionProb();
        

        // Serialize the trained model to be used further in testing
        storeHMMModel(cvm);

    }

    public void test(DataFileProcessor fp, CommonVarMethods cvm) {
        
        Double[][] transitionObj = new Double[AlphabetConstants.NUM_ACTIVITIES][AlphabetConstants.NUM_ACTIVITIES];
        
        for(int i =0; i< AlphabetConstants.NUM_ACTIVITIES; i++)
            for(int j =0; j< AlphabetConstants.NUM_ACTIVITIES; j++)
                    transitionObj[i][j] = cvm.getTransition()[i][j];
        TextTable tt = new TextTable(AlphabetConstants.ACTIVITY_LABELS, transitionObj);
         System.out.println("\n");
        tt.printTable();
        System.out.println("\n");
        
        // If only testing,  load the model
        if(AlphabetConstants.MODE.equalsIgnoreCase("TEST"))
        {
            readHMMModel(cvm);
            
        }
        int lengthOfActivity, id, predictedActivity = -1, prev, activityStartEventId, predictNextActivity=-1;
        
            // Process data
            // For each activity
           for (int i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) 
            {
                // For each activity occurence
                for (int j = 0; j < fp.getActivityCountInWhole()[i]; j++) // Look at each activity occurrence
                {
                        activityStartEventId = fp.getActivityStartEventId()[i][j];
                        cvm.initializeTestParameters();
                        lengthOfActivity = fp.getActivityLength()[i][j];
                        
                        for (int k = 0, count = 0; count < lengthOfActivity; k++) {
                            // Consider events that are part of this activity,
                            
                           // if (i == fp.getEvents()[activityStartEventId + k][AlphabetConstants.ACTIVITY_LABEL]) {
                                id = fp.getEvents()[activityStartEventId + k][AlphabetConstants.SENSOR_USED];
                                cvm.holdState(fp.getEvents()[activityStartEventId + k], fp.getSizes()[i][j], fp.getPreviousActivity()[i][j], fp, cvm);
                                
                                // Compute the evidence for current event id
                                cvm.calculateTestEvidence();
                                
                                // Update the likelihood after seeing this evidence
                                cvm.updateLikelihoodOfActivitiesViterbi();
                                
                                count++;

                            //}
                        }

                        // Get the activity with maximum likelihood
                        predictedActivity = cvm.getActivityMaxLikelihood();
                        
                        // Predict Next Activity
                        predictNextActivity = cvm.getNextActivity();
                        
                        
                        // Update the entry for confusion matrix 
                        cvm.increseFreqByOne(i, predictedActivity);
                      
                        // Compare predicted activity with current activitty
                        if (predictedActivity == i) {
                            cvm.setRight(cvm.getRight() + 1);
                        } else {
                            cvm.setWrong(cvm.getWrong() + 1);
                        }
                }
            }
        
    }

    private void storeHMMModel(CommonVarMethods cvm) {
        try {
            // Store the prior probabilities
            FileOutputStream priorPrbfout = new FileOutputStream(AlphabetConstants.HMM_PRIOR_PROB_SER_PATH);
            ObjectOutputStream priorPrbOut = new ObjectOutputStream(priorPrbfout);
            priorPrbOut.writeObject(cvm.getPriorProbActivity());
            priorPrbOut.close();
            priorPrbfout.close();

        } catch (Exception e) {
            System.out.println("Error while storing HMM model");
            e.printStackTrace();
        }
    }

    private void readHMMModel(CommonVarMethods cvm) {
        try {
            // Read the prior probabilities
            FileInputStream priorPrbfin = new FileInputStream(AlphabetConstants.HMM_PRIOR_PROB_SER_PATH);
            ObjectInputStream priorPrbIn = new ObjectInputStream(priorPrbfin);
            cvm.setPriorProbActivity((double[])priorPrbIn.readObject());
            priorPrbfin.close();
            priorPrbIn.close();

        } catch (Exception e) {
            System.out.println("Error while reading HMM model");
            e.printStackTrace();
        }

    }

}
