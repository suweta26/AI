/*
 * This part of project process the data file and extract events and features
 */
package iitg.cs561.project.alphabet;

import iitg.cs561.project.alphabet.util.AlphabetConstants;
import iitg.cs561.project.alphabet.util.CommonVarMethods;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Neelesh
 */
public class DataFileProcessor {
    
    // Event id when activity starts activityStarts[Activity_Id][Event_id] = 1 if activity Activity_Id starts at event_id
    private int[][] activityStartEventId = new int[AlphabetConstants.NUM_ACTIVITIES][AlphabetConstants.MAX_EVENTS];
    
    // The number of occurrences of each activity in whole data set
    private int[] activityCountInWhole = new int[AlphabetConstants.NUM_ACTIVITIES];
    
    // Maintianing the state between begin and end of activity
    private int[] activityBeingContinued = new int[AlphabetConstants.NUM_ACTIVITIES];
    
    // Length of each activity occurrence
    private int[][] activityLength = new int[AlphabetConstants.NUM_ACTIVITIES][AlphabetConstants.MAX_EVENTS];

    // Activity performed before the occurence event_id
    private int[][] previousActivity = new int[AlphabetConstants.NUM_ACTIVITIES][AlphabetConstants.MAX_EVENTS];

    int[][] sizes;
    
    // Events and their features
    private int[][] events = new int[AlphabetConstants.MAX_EVENTS][AlphabetConstants.NUM_FEATURES];
    
    
    private ArrayList<ArrayList<Integer>> thresholds = new ArrayList<ArrayList<Integer>>();
    
    private HashMap<String, Integer> sensorLabelMap;
    
    // variable to keep track current event being processed. Each line in data file is an event
    int eventId=0;
   
   
    // Read data set and attach events to activities
    public void readFile(String filepath, CommonVarMethods cvm)
    {
        System.out.println("##################### Inside Read file #################");
        for(int i=0; i< AlphabetConstants.NUM_ACTIVITIES; i++)
        {
            activityCountInWhole[i]=0;
            activityBeingContinued[i]=0;
        }
        String date, time, sensorId, sensorValue, activityLabel, activityStatus;
        int isSignleEventActivity=0; // Activity will sigle sensor event
        int isContinuedActivity =0; // Line which begin and end
        int previousActivityId = 0;
        int activityId;
       
        HashMap<String, Integer> activityLabelMap = cvm.getActivityLabelMap();
        sensorLabelMap = cvm.getSensorLabelIdMap();
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String line;
            int count=0;
         
            while((line = br.readLine())!=null)
            {
                System.out.println("Processing line number "+count);
                activityLabel = AlphabetConstants.NONE_ACTIVITY_LABEL;
                // Ignore commented lines
                if(line.charAt(0)=='%')
                    continue;
                line = line.trim();
              
                String[] parts= line.split(" ");
              
                date = parts[AlphabetConstants.LINE_PART_DATE];
                time = parts[AlphabetConstants.LINE_PART_TIME];
                sensorId = parts[AlphabetConstants.LINE_PART_SENSOR_ID];
                sensorValue = parts[AlphabetConstants.LINE_PART_SENSOR_VALUE];
                
                if(parts.length== AlphabetConstants.LINE_HAS_SIX_PARTS)
                {
                   activityLabel = parts[AlphabetConstants.LINE_PART_ACTIVITY_LABEL];
                   activityStatus = parts[AlphabetConstants.LINE_PART_ACTIVITY_STATUS];
                }
                
               // If activity has no begin and end
               if(isSignleEventActivity == 1)
               {
                   activityId = activityLabelMap.get(activityLabel);
                  // Add event to activity if open
                   if(activityBeingContinued[activityId] == 1)
                       addActivity(date, time, sensorId, sensorValue, activityId, 0, isContinuedActivity, previousActivityId);
                  // Single Activity
                   else
                   {
                       addActivity(date, time, sensorId, sensorValue, activityId, 1, isContinuedActivity, previousActivityId); //begin
                       addActivity(date, time, sensorId, sensorValue, activityId, 1, isContinuedActivity, previousActivityId); //end
                       previousActivityId = activityId;
                   }
                   
                   isSignleEventActivity=0;
               }
               
               // Activity has no label
               else if(activityLabel.equalsIgnoreCase(AlphabetConstants.NONE_ACTIVITY_LABEL))
               {
                   // If continuedActivity is not zero continue with previous activity
                   if(isContinuedActivity > 0)
                   {
                       for(int i=0; i< AlphabetConstants.NUM_ACTIVITIES; i++)
                       {
                           if(activityBeingContinued[i]==1)
                               addActivity(date, time, sensorId, sensorValue, i, 0, isContinuedActivity, previousActivityId);
                       }
                   
                   }
               }
               // line has activity label with being or end
               else
               {
                   activityId = activityLabelMap.get(activityLabel);
                   isContinuedActivity = addActivity(date, time, sensorId, sensorValue, activityId, 1, isContinuedActivity, previousActivityId);
                   
                   // If activity is finished, update previous activity as current activity
                   if(isContinuedActivity == 0)
                       previousActivityId = activityId;
                   
                   // Check for other current activities
                   for(int i =0; i< AlphabetConstants.NUM_ACTIVITIES ; i++)
                   {
                       // If some other activity is open add them 
                       if((i!=activityId) && (activityBeingContinued[i] == 1))
                               addActivity(date, time, sensorId, sensorValue, i, 0, isContinuedActivity, previousActivityId);
                   }
               }
                
            count++;
            }
          
        }
        catch(Exception e)
        {
            System.out.println("Error reading data file");
            e.printStackTrace();;
        }

    }
    
    private int addActivity(String date, String time, String sensorId, String sensorValue, int activityId, int isNewActivity, int isContinuedActivity, int previousActivityId) throws ParseException
    {
        
        int occurence, length;
        occurence = activityCountInWhole[activityId];
        length = activityLength[activityId][occurence];
        
        if(eventId < AlphabetConstants.MAX_EVENTS)
        {
            processEvent(activityId, date, time, sensorId, sensorValue);
            activityLength[activityId][occurence]+=1;
        }
        else
        {
            System.out.println("Event "+date+ " "+time +" "+sensorId+" "+sensorValue);
            System.out.println("Activity length for "+ AlphabetConstants.ACTIVITY_LABELS[activityId]+" exceeds maximum.");
        }
        
        // Starting a new activity
        if(isNewActivity == 1 && activityBeingContinued[activityId] == 0)
        {
            activityBeingContinued[activityId] = 1;
            previousActivity[activityId][occurence] = previousActivityId;
            activityStartEventId[activityId][occurence] = eventId - 1;
            return(isContinuedActivity+1);
        }
        else if (isNewActivity == 0)                            // Continue existing activity
                return(isContinuedActivity);
        else
        {
            activityCountInWhole[activityId]+=1;
            activityBeingContinued[activityId] =0;
            
            activityStartEventId[activityId][activityCountInWhole[activityId]] = 0; 
            previousActivity[activityId][activityCountInWhole[activityId]] = 0; 
            activityLength[activityId][activityCountInWhole[activityId]] = 0;
            return(isContinuedActivity-1);      
        }
        
    }
    
    // Extract the Event information from the event
    private void processEvent(int acticityId, String strDate, String strTime, String strSensorId, String strSensorValue) throws ParseException
    {
        
        int dayOfWeek=0, sensorId, sensorValue, time=0;
       
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(strDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dayOfWeek =calendar.get(Calendar.DAY_OF_WEEK)-1;
        
        
        char[] temp2 = new char[2];
        
        temp2[0]= strTime.charAt(0);
        temp2[1]= strTime.charAt(1);
        
        time = Integer.parseInt(String.valueOf(temp2));
        
        sensorId = sensorLabelMap.get(strSensorId); 
        
        if(strSensorValue.equalsIgnoreCase("OFF"))
            sensorValue = AlphabetConstants.OFF;
        else
            sensorValue = AlphabetConstants.ON;
        
        events[eventId][AlphabetConstants.SENSOR_USED] = sensorId;
        events[eventId][AlphabetConstants.TIME] = time;
        events[eventId][AlphabetConstants.DAY_OF_WEEK] = dayOfWeek;
        events[eventId][AlphabetConstants.SENSOR_VALUE] = sensorValue;
        events[eventId][AlphabetConstants.ACTIVITY_LABEL] = acticityId;
        
        eventId++;
       }
    
    // Determine ranges for feature values. Put selected feature values into ranges using user-supplied range values or equal-frequency binning.
    public void featureValuesRange()
    {
        int discretize, num, i, j, k;
        int[] data; 
        for(i=0; i<AlphabetConstants.NUM_FEATURES; i++)
        {
            discretize =1;
            thresholds.add(i, new ArrayList<>());
            if(AlphabetConstants.FEATURE_VALUES_RANGE[i] == 0)
            {
                if(i == AlphabetConstants.TIME)
                {
                    
                    for(j=0; j < AlphabetConstants.NUM_FEATURES_VALUES[i]; j++)
                    {
                        if (j == 0)
                            thresholds.get(i).add(j, 5); // Night / morning threshold
                        else if (j == 1)
                            thresholds.get(i).add(j, 100);          // Morning / mid day threshold
                        else if (j == 2)
                            thresholds.get(i).add(j, 15);   // Mid day / afternoon threshold
                        else thresholds.get(i).add(j, 20);     // Afternoon / evening threshold
                    }
                }
                else if(i == AlphabetConstants.ACTIVITY_LENGTH)
                {
                    for (j=0; j < AlphabetConstants.NUM_FEATURES_VALUES[i]; j++)
                        {
                            if (j == 0)
                                thresholds.get(i).add(j, 150);  // Small / medium threshold
                            else                                  // Medium / large threshold
                                thresholds.get(i).add(j, 500);
                        }
                    
                }
                // Other features do not use ranges
                else discretize =0;
            }
            // Use equal frequency binning to select threshold values
            else
            {
                num = 0;
                if(i== AlphabetConstants.ACTIVITY_LENGTH)
                {
                    for(j =0 ; j< AlphabetConstants.NUM_ACTIVITIES; j++)
                    {
                        num+=activityCountInWhole[j];
                    }
                }
                else
                    num = eventId;
                
                data = new int[num];
                
                int n =0;
                
                if(i == AlphabetConstants.ACTIVITY_LENGTH)
                {
                    for (j=0; j<AlphabetConstants.NUM_ACTIVITIES; j++)
                            for (k=0; k<activityCountInWhole[j]; k++)
                                    data[n++] = activityLength[j][k];
                }
                else
                {
                    for (j=0; j<eventId; j++)
                            data[n++] = events[j][i];
                }
                
                Arrays.sort(data);
                
               // System.out.print("The range values for feature "+i+" are ");
                int tvalue;
                for (j=0; j<AlphabetConstants.NUM_FEATURES_VALUES[i] - 1; j++)
                    {
                        tvalue = (j + 1) * (n / AlphabetConstants.NUM_FEATURES_VALUES[i]);
                        
                        if(thresholds.get(i) == null)
                            thresholds.add(i, new ArrayList<>());
                        //System.out.print("data value "+ tvalue);
                        //System.out.print("Length of data "+ data.length);
                        thresholds.get(i).add(j, data[tvalue]);
                         //System.out.println(thresholds.get(i).get(j));
                    }
                
            }
            
            if(discretize == 1)
            {
                // Create array of discretized activity lengths
                if(i == AlphabetConstants.ACTIVITY_LENGTH)
                {
                   
                        sizes = new int[AlphabetConstants.NUM_ACTIVITIES][];
                        for (j=0; j<AlphabetConstants.NUM_ACTIVITIES; j++)
                        {
                            sizes[j] = new int[activityCountInWhole[j]];
                            for(k=0; k< activityCountInWhole[j]; k++)
                            {
                                num =0;
                                while((num < (AlphabetConstants.NUM_FEATURES_VALUES[i]-1)) && (activityLength[j][k] > thresholds.get(i).get(num)))
                                    num++;
                                sizes[j][k]= num;
                            }
                        }
                    
                }
                
                else
                {
                    for (j=0; j<eventId; j++)
                     {
                        num = 0;
                            while ((num < (AlphabetConstants.NUM_FEATURES_VALUES[i] - 1)) &&
                                (events[j][i] > thresholds.get(i).get(num)))
                                num++;
                        events[j][i] = num;
                    }
                }
            }
        }
    }

    public int[][] getActivityStartEventId() {
        return activityStartEventId;
    }

    public void setActivityStartEventId(int[][] activityStarts) {
        this.activityStartEventId = activityStarts;
    }

    public int[] getActivityCountInWhole() {
        return activityCountInWhole;
    }

    public void setActivityCountInWhole(int[] activityCountInWhole) {
        this.activityCountInWhole = activityCountInWhole;
    }

    public int[] getActivityBeingContinued() {
        return activityBeingContinued;
    }

    public void setActivityBeingContinued(int[] activityBeingContinued) {
        this.activityBeingContinued = activityBeingContinued;
    }

    public int[][] getActivityLength() {
        return activityLength;
    }

    public void setActivityLength(int[][] activityLength) {
        this.activityLength = activityLength;
    }

    public int[][] getPreviousActivity() {
        return previousActivity;
    }

    public void setPreviousActivity(int[][] previousActivity) {
        this.previousActivity = previousActivity;
    }

    public int[][] getEvents() {
        return events;
    }

    public void setEvents(int[][] activityEvents) {
        this.events = activityEvents;
    }

    public ArrayList<ArrayList<Integer>> getThresholds() {
        return thresholds;
    }

    public void setThresholds(ArrayList<ArrayList<Integer>> thresholds) {
        this.thresholds = thresholds;
    }

    public HashMap<String, Integer> getSensorLabelMap() {
        return sensorLabelMap;
    }

    public void setSensorLabelMap(HashMap<String, Integer> sensorLabelMap) {
        this.sensorLabelMap = sensorLabelMap;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int[][] getSizes() {
        return sizes;
    }

    public void setSizes(int[][] sizes) {
        this.sizes = sizes;
    }
    
    
    
}
