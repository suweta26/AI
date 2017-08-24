/*
 * This class holds all the variables and methods which are shared among different modules
 */
package iitg.cs561.project.alphabet.util;

import iitg.cs561.project.alphabet.DataFileProcessor;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Neelesh
 */
public class CommonVarMethods {

    // Prior probability of activities
    private double[] priorProbActivity = new double[AlphabetConstants.NUM_ACTIVITIES];
    private double[] likelihood = new double[AlphabetConstants.NUM_ACTIVITIES];
    private int[][][] evidence = new int[AlphabetConstants.NUM_ACTIVITIES][AlphabetConstants.NUM_FEATURES][];

    private double[][][] emissionProb = new double[AlphabetConstants.NUM_ACTIVITIES][AlphabetConstants.NUM_FEATURES][];

    // Frequency of actual activities and activity classifications for connfusion matrix
    private int[][] freq = new int[AlphabetConstants.NUM_ACTIVITIES][AlphabetConstants.NUM_ACTIVITIES];
    private double[][] transition = new double[AlphabetConstants.NUM_ACTIVITIES][AlphabetConstants.NUM_ACTIVITIES];
    private ArrayList<ArrayList<Integer>> testEvidence = new ArrayList<ArrayList<Integer>>();

    // Total number of sensor activities for an activity
    private int[] totalSensorEventsForActivity = new int[AlphabetConstants.NUM_ACTIVITIES];
    private int[] featureValue = new int[AlphabetConstants.NUM_FEATURES];

    // Using which sensor is used and for how many times for an activity
    private int[][] sensorFreq = new int[AlphabetConstants.NUM_ACTIVITIES][AlphabetConstants.NUM_SENSORS];

    private int right = 0;
    private int wrong = 0;

    // HashMap to map sensor label with sensor id
    // {"Bed_to_toilet" : 0, "Breakfast": 1, "Bed": 3,....}
    private HashMap<String, Integer> activityLabelIdMap = new HashMap<String, Integer>();

    // HashMap to map sensor label with sensor id
    // {"M001" : 0, "M002" : 1, "M003": 2, ....}
    private HashMap<String, Integer> sensorLabelIdMap = new HashMap<String, Integer>();

    //  Caluculate Prior probabilities of each activity Pr(Activity) using MLE which is 
    public void calculatePriorProbabilities() {

        int i, atotal;
        atotal = 0;
        // Calculate total number of sensor events in training data
        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            atotal += totalSensorEventsForActivity[i];
        }

        // Calculate prior probability for activity as #events/#total events
        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            priorProbActivity[i] = (double) totalSensorEventsForActivity[i] / (double) atotal;
        }
    }

    public void initialize() {
        int j;
        for (int i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            for (j = 0; j < AlphabetConstants.NUM_ACTIVITIES; j++) {
                freq[i][j] = 0;
            }
            for (j = 0; j < AlphabetConstants.NUM_ACTIVITIES; j++) {
                sensorFreq[i][j] = 0;
            }
        }

        // Build Activity label map
        for (int i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            activityLabelIdMap.put(AlphabetConstants.ACTIVITY_LABELS[i], i);

        }

        // Build Sensor name map
        for (int i = 0; i < AlphabetConstants.NUM_SENSORS; i++) {
            sensorLabelIdMap.put(AlphabetConstants.SENSOR_LABELS[i], i);

        }
        for (int i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            for (j = 0; j < AlphabetConstants.NUM_FEATURES; j++) {
                evidence[i][j] = new int[AlphabetConstants.NUM_FEATURES_VALUES[j]];
                emissionProb[i][j] = new double[AlphabetConstants.NUM_FEATURES_VALUES[j]];
            }
        }
    }

    public void initializeTrainingParameters() {
        int i, j, k;

        for (i = 0; i < AlphabetConstants.NUM_FEATURES; i++) {
            featureValue[i] = 0;
        }

        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            totalSensorEventsForActivity[i] = 0;
            priorProbActivity[i] = (double) 0.0;
            for (j = 0; j < AlphabetConstants.NUM_SENSORS; j++) {
                sensorFreq[i][j] = 0;
            }

            for (j = 0; j < AlphabetConstants.NUM_ACTIVITIES; j++) {
                transition[i][j] = (double) 0.0;
            }

            for (j = 0; j < AlphabetConstants.NUM_FEATURES; j++) {
                for (k = 0; k < AlphabetConstants.NUM_FEATURES_VALUES[j]; k++) {

                    evidence[i][j][k] = 0;
                }
            }
        }
    }

    public void initializeTestParameters() {
        int i, j;

        for (i = 0; i < AlphabetConstants.NUM_FEATURES; i++) {
            this.featureValue[i] = 0;
            this.testEvidence.add(i, new ArrayList<>());
            for (j = 0; j < AlphabetConstants.NUM_FEATURES_VALUES[i]; j++) {
                this.testEvidence.get(i).add(j, 0);
            }
        }
        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            this.likelihood[i] = 1.0;
        }
    }

    // Hold the current state  to be processed
    public void holdState(int[] event, int size, int previous, DataFileProcessor fp, CommonVarMethods cvm) {

        this.featureValue[AlphabetConstants.SENSOR_USED] = event[AlphabetConstants.SENSOR_USED];
        this.featureValue[AlphabetConstants.TIME] = event[AlphabetConstants.TIME];
        this.featureValue[AlphabetConstants.DAY_OF_WEEK] = event[AlphabetConstants.DAY_OF_WEEK];
        this.featureValue[AlphabetConstants.PREVIOUS_ACTIVITY] = event[AlphabetConstants.PREVIOUS_ACTIVITY];
        this.featureValue[AlphabetConstants.ACTIVITY_LENGTH] = size;
    }

    public void increseSensorFreqByOne(int i, int j) {
        //System.out.println("sensorFreq: "+i+" " +j+" " +sensorFreq[i][j]);
        this.sensorFreq[i][j] += 1;
        //System.out.println("sensorFreq: "+i+" " +j+" " +sensorFreq[i][j]);
    }

    public void increseTotalSensorEventsForActivity(int i) {
        this.totalSensorEventsForActivity[i] += 1;
    }

    // Update transtion value from activity i to activity j
    public void updateTransitionCount(int i, int j) {
        this.transition[i][j] += (double) 1.0;
    }

    public void increseFreqByOne(int i, int j) {
        this.freq[i][j] += (double) 1.0;
    }

    // Update the evidences for each activity
    public void updateEvidence(int label) {
        int sensorId = featureValue[AlphabetConstants.SENSOR_USED];
        int time = featureValue[AlphabetConstants.TIME];
        int dow = featureValue[AlphabetConstants.DAY_OF_WEEK];
        int previous = featureValue[AlphabetConstants.PREVIOUS_ACTIVITY];
        int length = featureValue[AlphabetConstants.ACTIVITY_LENGTH];
        evidence[label][AlphabetConstants.SENSOR_USED][sensorId] += 1;
        evidence[label][AlphabetConstants.TIME][time] += 1;
        evidence[label][AlphabetConstants.DAY_OF_WEEK][dow] += 1;
        evidence[label][AlphabetConstants.PREVIOUS_ACTIVITY][previous] += 1;
        evidence[label][AlphabetConstants.ACTIVITY_LENGTH][length] += 1;
    }

    public void calculateTestEvidence() {
        int sensorId = featureValue[AlphabetConstants.SENSOR_USED];
        int time = featureValue[AlphabetConstants.TIME];
        int dow = featureValue[AlphabetConstants.DAY_OF_WEEK];
        int previous = featureValue[AlphabetConstants.PREVIOUS_ACTIVITY];
        int length = featureValue[AlphabetConstants.ACTIVITY_LENGTH];
        testEvidence.get(AlphabetConstants.SENSOR_USED).set(sensorId, testEvidence.get(AlphabetConstants.SENSOR_USED).get(sensorId) + 1);
        testEvidence.get(AlphabetConstants.TIME).set(time, testEvidence.get(AlphabetConstants.TIME).get(time) + 1);
        testEvidence.get(AlphabetConstants.DAY_OF_WEEK).set(dow, testEvidence.get(AlphabetConstants.DAY_OF_WEEK).get(dow) + 1);
        testEvidence.get(AlphabetConstants.PREVIOUS_ACTIVITY).set(previous, testEvidence.get(AlphabetConstants.PREVIOUS_ACTIVITY).get(previous) + 1);
        testEvidence.get(AlphabetConstants.ACTIVITY_LENGTH).set(length, testEvidence.get(AlphabetConstants.ACTIVITY_LENGTH).get(length) + 1);
    }

    // Normalize the transition probabilities so they sum to one.
    public void normalizeTransitionProb() {
        int i, j;
        double total;

        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            total = (double) 0;
            for (j = 0; j < AlphabetConstants.NUM_ACTIVITIES; j++) {
                total += transition[i][j];
            }

            for (j = 0; j < AlphabetConstants.NUM_ACTIVITIES; j++) {
                if (total != (double) 0) {
                    transition[i][j] /= total;
                }
            }
        }
    }

// Calculate the emission probabilities from the observed evidence, or the
// probability of observing the feature values given a particular activity.
    public void CalculateEmission() {
        int i, j, k;
        double val, min = 0.0000001;

        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {

            for (j = 0; j < AlphabetConstants.NUM_FEATURES; j++) {

                for (k = 0; k < AlphabetConstants.NUM_FEATURES_VALUES[j]; k++) {

                    if (evidence[i][j][k] == 0) // Replace 0 values with small number
                    {
                        val = min;
                    } else {
                        val = (double) evidence[i][j][k];
                    }

                    // If activity haven't occured, apply discounted probability
                    if (totalSensorEventsForActivity[i] == 0) {
                        emissionProb[i][j][k] = min;
                    } else {
                        emissionProb[i][j][k] = (val / (double) totalSensorEventsForActivity[i]);
                    }
                }
            }
        }
    }

// Update the likelihood of Pr(A(t)|E(t)) using Viterbi Alogrithm
    public void updateLikelihoodOfActivitiesViterbi() {

        double emission;
        double total = 0.0;
        double zero = 0.0;

        // Update likelihoods for each activity
        for (int i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            emission = 1.0;

            // Get the probability of evidence by combining the probability of each indivisual feature value.
            // Assuming each feature value to be independent to each other
            for (int j = 0; j < AlphabetConstants.NUM_FEATURES; j++) {
                emission = emission * emissionProb[i][j][featureValue[j]];
            }

            // For each previous activity j, update likelihood of activity i at time t by 
            // combining the probability of previous activity j and tr[j][i] and emission probability
            // Likelihood was initialized earlier to the prior Probability for the activity.
            for (int j = 0; j < AlphabetConstants.NUM_ACTIVITIES; j++) {
                likelihood[i] = likelihood[i] + (priorProbActivity[j] * (transition[j][i] * emission));
            }
        }

        // Compute total of prior likelihoods
        for (int i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            total = total + likelihood[i];
        }
        // Normalize to make total equal 1
        for (int i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            if (total != zero) {
                priorProbActivity[i] = likelihood[i] / total;
            }
        }

    }

    // Return activity with the maximum likelihood.
    public int getActivityMaxLikelihood() {
        int i, activityId = 0;
        double max = 0;

        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            if (likelihood[i] > max) {
                max = likelihood[i];
                activityId = i;
            }
        }

        return (activityId);
    }

    // Use the prediction. Pr(A(t+1)|e(1:t)) = Summation over at(Pr(A(t+1)|a(t)).Pr(a(t)|e(1:t)))
    public int getNextActivity() {
        int i, j, activityId = 0;
        double[] prediction = new double[AlphabetConstants.NUM_ACTIVITIES];
        double max = 0;

        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {

            for (j = 0; j < AlphabetConstants.NUM_ACTIVITIES; j++) {

                prediction[i] += transition[j][i] * priorProbActivity[j];

            }
        }

        for (i = 0; i < AlphabetConstants.NUM_ACTIVITIES; i++) {
            //System.out.println("Prediction of activity: "+AlphabetConstants.ACTIVITY_LABELS[i]+"is: "+prediction[i]);
            if (prediction[i] > max) {
                max = prediction[i];
                activityId = i;
            }
        }

        return (activityId);
    }

    public void printEvent(int[] event) {
//        System.out.print(event[AlphabetConstants.SENSORID]+" ");
//        System.out.print(event[AlphabetConstants.TIME]+" ");
//        System.out.print(event[AlphabetConstants.DAY_OF_WEEK]+" ");
//        System.out.print(event[AlphabetConstants.SENSOR_VALUE]+" ");
//        System.out.print(event[AlphabetConstants.ACTIVITY_LABEL]);
//        System.out.print("\n");
    }

    public double[] getPriorProbActivity() {
        return priorProbActivity;
    }

    public void setPriorProbActivity(double[] priorProbActivity) {
        this.priorProbActivity = priorProbActivity;
    }

    public double[] getLikelihood() {
        return likelihood;
    }

    public void setLikelihood(double[] likelihood) {
        this.likelihood = likelihood;
    }

    public int[][][] getEvidence() {
        return evidence;
    }

    public void setEvidence(int[][][] evidence) {
        this.evidence = evidence;
    }

    public double[][][] getEmissionProb() {
        return emissionProb;
    }

    public void setEmissionProb(double[][][] emissionProb) {
        this.emissionProb = emissionProb;
    }

    public int[][] getFreq() {
        return freq;
    }

    public void setFreq(int[][] freq) {
        this.freq = freq;
    }

    public double[][] getTransition() {
        return transition;
    }

    public void setTransition(double[][] transition) {
        this.transition = transition;
    }

    public ArrayList<ArrayList<Integer>> getTestEvidence() {
        return testEvidence;
    }

    public void setTestEvidence(ArrayList<ArrayList<Integer>> testEvidence) {
        this.testEvidence = testEvidence;
    }

    public int[] getTotalSensorEventsForActivity() {
        return totalSensorEventsForActivity;
    }

    public void setTotalSensorEventsForActivity(int[] totalSensorEventsForActivity) {
        this.totalSensorEventsForActivity = totalSensorEventsForActivity;
    }

    public int[] getFeatureValue() {
        return featureValue;
    }

    public void setFeatureValue(int[] featureValue) {
        this.featureValue = featureValue;
    }

    public int[][] getSensorFreq() {
        return sensorFreq;
    }

    public void setSensorFreq(int[][] sensorFreq) {
        this.sensorFreq = sensorFreq;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getWrong() {
        return wrong;
    }

    public void setWrong(int wrong) {
        this.wrong = wrong;
    }

    public HashMap<String, Integer> getActivityLabelMap() {
        return activityLabelIdMap;
    }

    public HashMap<String, Integer> getSensorLabelIdMap() {
        return sensorLabelIdMap;
    }

}
