from subprocess import check_output
import time
import sys
sys.path.append('/home/pi/Adafruit_Python_ADS1x15')
import Adafruit_ADS1x15
import RPi.GPIO as GPIO
import Adafruit_BMP.BMP085 as BMP085
sensor = BMP085.BMP085()
GPIO.setwarnings(False)
# Initialise parameters and variables
GAIN = 1
TRIG = 23
ECHO = 24

# Instantiation and setup
adc = Adafruit_ADS1x15.ADS1115()
GPIO.setmode(GPIO.BCM)
GPIO.setup(TRIG,GPIO.OUT)
GPIO.setup(ECHO,GPIO.IN)

# Set trigger to False (Low)
GPIO.output(TRIG, False)

# Allow module to settle
time.sleep(0.5)

def read_ping(trigger,echo):
	# Send 10us pulse to trigger
	GPIO.output(trigger, True)
	time.sleep(0.00001)
	GPIO.output(trigger, False)
	start = time.time()
	cur_time = time.time()
	#If program get stuck for more than 2 second Break;
	while GPIO.input(echo)==0 and time.time()<=(cur_time+2):
	  start = time.time()

	while GPIO.input(echo)==1 and time.time()<=(cur_time+2):
	  stop = time.time()

	# Calculate pulse length
	elapsed = stop-start

	# Distance pulse travelled in that time is time
	# multiplied by the speed of sound (cm/s)
	distance = elapsed * 34300

	# That was the distance there and back so halve the value
	distance = distance / 2
	return distance

#=========================================================================================================================================================
from datetime import datetime
import time
import csv
days=0
size=0
now=datetime.now()
#set sleeping time till recording time start
target_time=datetime(now.year,now.month,now.day,7,0)
#print"sleeping till     ",target_time
#time.sleep((target_time-now).seconds)
all_data_len=0
days_to_record_for=5
#record 16 hrs
daily_recording_time=3
while days<days_to_record_for:
        print "=============================================================="
        print "Data collection : Day",days+1
        todays_data_len=0
        csvfile=open("DATA_"+str(now.day)+"_"+str(now.month)+"_"+str(now.year)+".CSV",'a')
        writer=csv.writer(csvfile)
        print "started..at",datetime.now()
        start_time=time.time()
        start_day = now.day;
        while 0<1:
                print('Temp = {0:0.2f} *C'.format(sensor.read_temperature()))
                print('Pressure = {0:0.2f} Pa'.format(sensor.read_pressure()))
                print('Altitude = {0:0.2f} m'.format(sensor.read_altitude()))
                print('Sealevel Pressure = {0:0.2f} Pa'.format(sensor.read_sealevel_pressure()))
                try:
                        pir_reading = adc.read_adc(0, gain=GAIN)
                        #print "PIR"
                        light_reading = int(str(check_output(["./light"])).rstrip())
                        #print "LIGHT"
                        ping_reading = read_ping(TRIG, ECHO)
                        #print "PING"
                        print "PIR :",pir_reading,"PING :",ping_reading,"Light :",light_reading
                        #Writing to file
                        writer.writerow([datetime.now(),pir_reading,ping_reading,light_reading,sensor.read_temperature(),sensor.read_pressure()])
                        print "done"
                        todays_data_len+=1
			#Wait before starting next iteration
                        time.sleep(1)
                except KeyboardInterrupt:
                        break
                except:
                        print 'Some error. continuing.. '
        csvfile.close()
        print "finished..at",datetime.now()
        print "Data recorded for day ",days+1        
        days+=1               
        all_data_len+=todays_data_len
        print todays_data_len,"- Records collected today"
        print all_data_len,"- Records collected till today"
        
        #sleep till start time next day
        if days<days_to_record_for:
                now=datetime.now()
                #print "Sleeping till...",datetime(now.year,now.month,now.day+1,7,0)
		#sleep 8 hours
                #time.sleep(28800)
# Reset GPIO settings
GPIO.cleanup()

