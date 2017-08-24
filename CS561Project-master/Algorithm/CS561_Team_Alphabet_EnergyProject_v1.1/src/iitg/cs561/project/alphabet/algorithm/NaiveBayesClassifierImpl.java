/*
 * Implementing activity recognition using naive bayes classifier
 */
package iitg.cs561.project.alphabet.algorithm;

import iitg.cs561.project.alphabet.DataFileProcessor;
import iitg.cs561.project.alphabet.util.AlphabetConstants;
import iitg.cs561.project.alphabet.util.CommonVarMethods;

/**
 *
 * @author Team Alphabet
 */
public class NaiveBayesClassifierImpl {

    // Train the classifier
    public void training(DataFileProcessor fp, CommonVarMethods cvm) {
        int lengthOfActivity, activityStartEventId;

        // Go through all activities.
        for (int i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            // Iterate over each occurence of activity
            for (int j = 0; j < fp.getActivityCountInWhole()[i]; j++) {
                // Get the start event of each activity
                activityStartEventId = fp.getActivityStartEventId()[i][j];
                lengthOfActivity = fp.getActivityLength()[i][j];
                for (int k = 0, count = 0; count < lengthOfActivity; k++) {
                   
                        // Hold the state 
                        cvm.holdState(fp.getEvents()[activityStartEventId + k], fp.getSizes()[i][j],
                                fp.getPreviousActivity()[i][j], fp, cvm);

                        // Update the total sensor events recoreded for for activity id i
                        cvm.increseTotalSensorEventsForActivity(i);

                        // Update the evidences for this activity
                        cvm.updateEvidence(i);
                        count++;
                   
                }
            }

        }
        //  Caluculate Prior probabilities of each activity Pr(Activity)
        cvm.calculatePriorProbabilities();
    }

    public void test(DataFileProcessor fp, CommonVarMethods cvm) {

        int lengthOfActivity,  start;
       
        double[] p = new double[AlphabetConstants.NUM_ACTIVITIES];
        double minProb;

        // For each activity
        for (int i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) 
        {
            // For each activity occurence
            for (int j = 0; j < fp.getActivityCountInWhole()[i]; j++) 
            {
                start = fp.getActivityStartEventId()[i][j];
                
                // Initialize test variables
                cvm.initializeTestParameters();   
                
                // get the length of occurence of activity
                lengthOfActivity = fp.getActivityLength()[i][j];
                for (int k = 0, n = 0; n < lengthOfActivity; k++) {
                    // Only consider events that are part of this activity, ignore events for overlapping activities
                    if (i == fp.getEvents()[start + k][AlphabetConstants.ACTIVITY_LABEL]) {
                        cvm.holdState(fp.getEvents()[start + k], fp.getSizes()[i][j], fp.getPreviousActivity()[i][j], fp, cvm);

                        // Calculate probability of feature values given activity
                        cvm.calculateTestEvidence();
                        n++;
                    }
                }
                // Get the activity which minimizes the loss function
                int predictedActivity = 0;
                minProb = 0;
                for (int k = 0; k < AlphabetConstants.NUM_ACTIVITIES; k++) {
                    p[k] = calculatLossFunction(cvm.getPriorProbActivity()[k], k, cvm);
                    if ((k == 0) || (p[k] < minProb)) {
                        predictedActivity = k;
                        minProb = p[k];
                    }
                }
                
                // Update the frequency for confusion matrix
                cvm.increseFreqByOne(i, predictedActivity);
                
                // Update prediction accuracy
                if (i == predictedActivity) 
                {
                    cvm.setRight(cvm.getRight() + 1);
                    
                } else {
                    cvm.setWrong(cvm.getWrong() + 1);
                }
            }
        }
    }
    
   
    // Calculate the loss funcction, minimize negative log likelihood
    private double calculatLossFunction(double prob, int activityId, CommonVarMethods cvm) {
        int i, j, trainval, testval;
        double ratio;

        prob = (double) -1.0 * Math.log(prob);
        for (i = 0; i < AlphabetConstants.NUM_FEATURES; i++) {
            for (j = 0; j < AlphabetConstants.NUM_FEATURES_VALUES[i]; j++) {
                
                // Get the count of feature value j for activity id at occurnce i
                trainval = cvm.getEvidence()[activityId][i][j];
                
                
                testval = cvm.getTestEvidence().get(i).get(j);
                
                if (cvm.getTestEvidence().get(i).get(j) != 0) // Only include evidence which exists
                {
                    // If the occurence of feature given the activity id is zero, discount the probability
                    if (trainval == 0) 
                    {
                        prob -= Math.log((double) testval) + Math.log((double) Double.MIN_VALUE);
                    } else 
                    {
                        // P(xi|A)
                        ratio = (double) trainval / (double) cvm.getTotalSensorEventsForActivity()[activityId];
                         // Add P(xi|A) in till now calculated probability
                        prob -= Math.log((double) testval) + Math.log(ratio);
                    }
                }
            }
        }

        return prob;
    }

}
