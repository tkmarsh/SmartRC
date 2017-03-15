//rip_firmware v1.1
//Bugfree but messily programmed; loads of stuff needs cleaned up.

#include <Servo.h> //Arduino Library For Controlling Servo Motors.
#include <DHT.h> //Arduino Library To Interface With Digital Hygrometer/Thermometer (DHT11) .
#include <CS_MQ7.h> //Arduino Library to Interface with MQ7 Carbon Monoxide Sensor, includes power management.
#include <math.h>//C Numerics Library, this includes the ceil function used to round up.

//Hardware Declarations
const int input_pin = 0;  //RX From Bluetooth Module
const int output_pin = 1; //TX To Bluetooth Module
const int output_mtr_left = 2; //Steering Motor, Turn left,
const int output_mtr_right = 3;//Steering Motor, Turn right
const int output_mtr_fwd = 4; //Driving Motor, Move Forward
const int output_mtr_bck = 5; //Driving Motor, Move Backward
const int led = 6; //IR LED Array Output
const int indicator_led = 8;
const int rpi_reboot = 12; //Transistor for Resetting RPi MPC
const int battery_voltage_pin = A2;
const int LDR = A3;
const int CoSensorOutput = 0;
DHT dht(14, DHT11); //Defines Type Of DHT Sensor And Input Pin
Servo pan;
Servo tilt;
CS_MQ7 MQ7(12, 8); 

//Variables
int co_data;
int previous_input;
int LDR_Reading;
int bluetooth_input; //Data Received From Smartphone Via Bluetooth
int temp; //Thermometer Data Variable
int humidity; // Hygrometer Data Variable
int bluetooth_output; //Data To Be Transmitted To Smartphone Via Bluetooth
int count; //Keeps track of which bit is being worked on for each function.
int dec_to_bin_output[8]; //dec_to_bin_output control data loaded into this array
int servo_horizontal_pos; //Current position of horizontal servo
int servo_vertical_pos; //Current position of vertical servo
float battery_voltage;
long int average_co;
boolean led_on;
boolean co_state;
String state = "";


void dec_to_bin(int bluetooth_input){
count = 0;
  while (bluetooth_input !=0){
    dec_to_bin_output[count] = (bluetooth_input % 2);
    bluetooth_input = (bluetooth_input / 2);
    count++;
  }
}

int clear_dec_to_bin(){
for (count=0;count!=8;count ++){
  dec_to_bin_output[count] = 0;
  }
}

int get_average_co(){
  average_co = 0;
  co_state = get_co_sensor_state();
  
  for (count=0;count<25;count ++){
      if(co_state){   //Powering CO Sensor at 1.4V, CO Levels can now be read.
      co_data = analogRead(CoSensorOutput);
      average_co = average_co + co_data;
      //Serial.print("CO Reading No.") && Serial.print(count) && Serial.print(": ") && Serial.println(co_data);
     }    
 }
     
  average_co /= 25; //Calculates average
  average_co = 1024 - average_co;//Inverts value
  average_co = ceil(average_co * 0.0977);
  
 return average_co;
}

int getAverageLight(){
  int avgLDR = 0;

  for (count=0;count<25;count ++){
    int ldrData = analogRead(A3);
      avgLDR += ldrData;
     } 
       avgLDR /= 25; //Calculates average
       avgLDR = (1024 - avgLDR) - 15;//Inverts value and removes 15 to compensate for LDR inaccuracy
       
if (avgLDR < 0){
  avgLDR = 0;
}
 
  return avgLDR; 
  }
 

boolean get_co_sensor_state(){
   if(MQ7.CurrentState() == LOW){ 
    return true;
  }
  else{
    return false;
  }
}

int getVoltage(){
 long int voltage_sum = 0;
 for(count=0;count<10;count++){
  voltage_sum = analogRead(battery_voltage_pin);
  delay(5);
  }
 float voltage = (voltage_sum / 10);
  return voltage;
 }

void setup() {
  Serial.begin(9600); //Starts Serial Data Stream, 9600 Is The Baud Rate Of The HC-06 Bluetooth Module And DHT11
  dht.begin();
  pinMode(output_mtr_left, OUTPUT); //Defining Values Assigned To Each Variable As Physical I/O Pins
  pinMode(output_mtr_right, OUTPUT);
  pinMode(output_mtr_fwd, OUTPUT);
  pinMode(output_mtr_bck, OUTPUT);
  pinMode(rpi_reboot, OUTPUT);
  pinMode(output_pin, OUTPUT);
  pinMode(led, OUTPUT);
  pinMode(indicator_led, OUTPUT);
  pinMode(battery_voltage_pin, INPUT);
  pinMode(LDR, INPUT);

  led_on = false;
  previous_input = 0;

  //Assigns an output pin for the servo objects
  pan.attach(10);
  tilt.attach(11);

  servo_horizontal_pos = 110; //Assigns and Moves Servo Positions to 0, this is their starting position.
  pan.write(servo_horizontal_pos);
  servo_vertical_pos = 45;
  tilt.write(servo_vertical_pos);
}

void loop() {
  // **Start Of Control Data Processing**
  bluetooth_input = Serial.read(); //Control Data Read In From Serial RX Port
  
  if (bluetooth_input != -1){
    previous_input = bluetooth_input;
   }
 
  MQ7.CoPwrCycler();  
  co_data = 0;

 if (bluetooth_input == -1) { //-1 is the default input when no data is recieved
    bluetooth_input = previous_input; 
  }

  else if (bluetooth_input == 192){
       if (led_on == false){
        digitalWrite(led, HIGH);
        led_on = true;
       }
       else if (led_on == true){
        digitalWrite(led, LOW);
        led_on = false;
       }
  }
 dec_to_bin(bluetooth_input);

 boolean pause = false;
   
  for (count = 0; count < 8; count++)
  {
    switch (count)
    {

      case 7://Driving Motor - Forwards
        if (dec_to_bin_output[count] == 1 && dec_to_bin_output[count - 1] == 0) //Checks opposing motor function, as sending power in both configs would damage motor*
        {
          digitalWrite(output_mtr_fwd, HIGH);
          pause = true;
          //Serial.println("MOVING FORWARD");
        }
        else
        {
          digitalWrite(output_mtr_fwd, LOW);
          pause = false;
          //Serial.println("NOT MOVING FORWARD");
        }
        break;

      case 6:// Driving Motor - Reverse
        if (dec_to_bin_output[count] == 1 && dec_to_bin_output[count + 1] == 0) //*
        {
          digitalWrite(output_mtr_bck, HIGH);
          pause = true;
          //Serial.println("MOVING BACKWARD");
        }
        else
        {
          digitalWrite(output_mtr_bck, LOW);
          pause = false;
          //Serial.println("NOT MOVING BACKWARD");
        }
        break;

      case 5://Steering Motor - Turn Left
        if (dec_to_bin_output[count] == 1 && dec_to_bin_output[count - 1] == 0)//*
        {
          digitalWrite(output_mtr_left, HIGH);
          pause = true;
          //Serial.println("TURNING LEFT");
        }
        else
        {
          digitalWrite(output_mtr_left, LOW);
          pause = false;
        }
        break;

      case 4: //Steering Motor - Turn Right
        if (dec_to_bin_output[count] == 1 && dec_to_bin_output[count + 1] == 0)//*
        {
          digitalWrite(output_mtr_right, HIGH);
          pause = true;
          //Serial.println("TURNING RIGHT");
        }
        else
        {
          digitalWrite(output_mtr_right, LOW);
          pause = false;
        }
        break;

      case 3:// Camera Pan - Left
        if (dec_to_bin_output[count] == 1 && dec_to_bin_output[count - 1] == 0)//*
        {
          if (servo_horizontal_pos > 0 && servo_horizontal_pos < 180){
          servo_horizontal_pos = (servo_horizontal_pos - 10);
          pan.write(servo_horizontal_pos);
          pause = false;
          //Serial.println("CAMERA PAN RIGHT");
          }
        }
        break;

      case 2://Camera Pan - Right
        if (dec_to_bin_output[count] == 1 && dec_to_bin_output[count + 1] == 0)//*
        {
          if  (servo_horizontal_pos < 180) {
          servo_horizontal_pos = (servo_horizontal_pos + 10);
          pan.write(servo_horizontal_pos);
          pause = false;
          //Serial.println("CAMERA PAN LEFT");
          }
        }
       
        break;

      case 1://Camera Tilt - Up
        if (dec_to_bin_output[count] == 1 && dec_to_bin_output[count - 1] == 0)//*
        {
          if  (servo_vertical_pos > 0){
          servo_vertical_pos = (servo_vertical_pos - 10);
          //Serial.print(" servo_vertical_pos: ") && Serial.println(servo_vertical_pos);
          tilt.write(servo_vertical_pos);
          //Serial.println("CAMERA TILT UP");
          pause = false;
          }
        }

        break;

      case 0://Camera Tilt - Down
        if (dec_to_bin_output[count] == 1 && dec_to_bin_output[count + 1] == 0)//*
        {
          if  (servo_vertical_pos < 180){
          servo_vertical_pos = (servo_vertical_pos + 10);
          tilt.write(servo_vertical_pos);
          //Serial.println("CAMERA TILT DOWN");
          pause = false;
          }
        }
 
        break;
    }
    if (dec_to_bin_output[6] == 1 && dec_to_bin_output[7] == 1){
     digitalWrite(rpi_reboot, HIGH);
   }
    else{
    digitalWrite(rpi_reboot, LOW);
    }
  }

   
    
  // **Start Of Sensor Data Processing**
  temp = dht.readTemperature();
  humidity = dht.readHumidity();
  average_co = get_average_co();
  co_state = get_co_sensor_state();
  battery_voltage = getVoltage();
  LDR_Reading = getAverageLight();
  
 
    battery_voltage *= 0.1214;
  battery_voltage = floor(battery_voltage);

 
  Serial.print(temp);
  Serial.print("/");
  Serial.print(humidity);
  Serial.print("/");
  Serial.print(LDR_Reading);


 
  clear_dec_to_bin();
}
