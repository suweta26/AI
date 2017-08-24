/*
 * This file holds the configuration of the model and experiment
 */
package iitg.cs561.project.alphabet.util;

/**
 *
 * @author TeamAlphabet
 */
public class AlphabetConstants {
    
    
    // Model to be used for activity recognition. NB or HMM
    public static String MODEL = "HMM";
    
    // Mode of experiment : TRAIN, TEST, TRAIN_TEST
    public static String MODE = "TRAIN_TEST"; 
    
    public static String DATA_FILE_PATH = "data/cairo.data";
    
    // Upper limit of events present in data set. We have 647487 events
    public static int MAX_EVENTS = 700000;
    
    // File path to Store HMM model 
    public static String HMM_PRIOR_PROB_SER_PATH = "model/hmm/priorprob.ser";
    
    // File path to Store Naive Bayes model
    public static String NAIVE_BAYES_PRIOR_PROB_SER_PATH = "model/naivebayes/priorprob.ser";
    
    
    /* CAUTION: Do not change following parameters until and unless required*/

    // Number of parts in a line
   public static int LINE_HAS_SIX_PARTS = 6;  // 2009-06-10 03:20:59.087874 M006 ON Night_wandering begin
    
    
    // Line parts in the data file
    public static int LINE_PART_DATE = 0;
    public static int LINE_PART_TIME = 1;
    public static int LINE_PART_SENSOR_ID = 2;
    public static int LINE_PART_SENSOR_VALUE = 3;
    public static int LINE_PART_ACTIVITY_LABEL = 4;
    public static int LINE_PART_ACTIVITY_STATUS = 5;
    
    public static String NONE_ACTIVITY_LABEL = "NONE";
    
    // Number of activites
    public static final int NUM_ACTIVITIES = 10;
    // Number of sensors used for collecting data
    public static final int NUM_SENSORS = 27;
    // Number of features that are used to identify an event
    public static int  NUM_FEATURES = 5;
    
    // Number of values that are defined for each feature value.
    // Number of sensors, time, day_of_week, num_activities, activity length
    // Feature Value 1: The default number of feature values is equal to the number of sensors
    // Feature Value 2: The default number of feature values is 5.
    // Feature Value 3: The default number of feature values is 7.
    // Feature Value 4: The default number of feature values is equal to the number of activities
    // Feature Value 5: The default number of feature values is 3
    public static int NUM_FEATURES_VALUES[] = {NUM_SENSORS, 5, 7, NUM_ACTIVITIES, 2};
    
    // The range values for a given feature are determined.    
    public static int FEATURE_VALUES_RANGE[] = {0, 0, 0, 0, 3};
    
    //Gives name of the sensor sensormap[i][0], sensormap[i][1] = Gives Id which is index in my case
    public static String[] SENSOR_LABELS = {"M001", "M002", "M003", "M004", "M005", "M006", "M007", "M008", "M009",
                                            "M010", "M011", "M012", "M013", "M014", "M015", "M016", "M017", "M018", 
                                            "M019", "M020", "M021", "M022", "M023", "M024", "M025", "M026", "M027"};
    
    // Named labels given to activities
    public static String[] ACTIVITY_LABELS= {"Bed_to_toilet", "Breakfast", "Bed", "C_work", "Dinner", "Laundry", "Leave_home", "Lunch", "Night_wandering", "R_medicine"}; 
    
    
    
     // Define Sensor state
    public static int OFF = 0;
    public static int ON = 1;
    
    
    // Currently using features location, time of day, day of week, previous activity, and activity length
    // Common to both feature and activity event
    
    //This is an integer value in the range of 0 to the number of logical sensor values 
    public static int SENSOR_USED = 0;
    
    // This is the input time of the sensor event but is discretized to an integer value 
    public static int TIME = 1;
    
    //The input date of the sensor event is converted into a value in the range of 0 to 6 
    // that represents the day of the week on which the sensor event occurred.
    public static int DAY_OF_WEEK = 2;
    
    // Specific to feature
    
    // This feature is an integer value that represents the activity that occurred before the current activity
    public static int PREVIOUS_ACTIVITY = 3;
    
    // This feature represents the length of the current activity measured in number of sensor events.
    public static int ACTIVITY_LENGTH = 4;
    
    // Specific to event
    public static int SENSOR_VALUE = 3;
    public static int ACTIVITY_LABEL = 4;
    
    
    
    
}
