#include <ArduinoBLE.h>
#include <Arduino_LSM9DS1.h>
#include "RunningAverage.h"
#include <MadgwickAHRS.h>

Madgwick filter;


// Madgwick

float roll, pitch, heading;
long previousMillis = 0;  // last timechecked, in ms
unsigned long micros_per_reading, micros_previous;

RunningAverage avgPres_hill(3);
RunningAverage avgPres_toe(3);
RunningAverage acc1(3);
RunningAverage acc2(3);
RunningAverage acc3(3);

RunningAverage mag1(3);
RunningAverage mag2(3);
RunningAverage mag3(3);


///////////*initializations*///////////

// initializing analog inputs
int fsranalog1 = A1; int fsranalog2 = A3; int fsr1; int fsr2;

//initializing variables

const int ledPin = LED_BUILTIN;



///////////*initializing BLE connection*//////////

//service
BLEService ArduinoService("19B10001-E8F2-537E-4F6C-D104768A1214");

//Acceleration
BLEStringCharacteristic Char_acc("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 43);
BLEStringCharacteristic Char_gyro("19B10011-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 43);
BLEStringCharacteristic Char_magn("19B10111-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 43);
BLEStringCharacteristic Char_pres("19B11111-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 43);


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void setup() {
  Serial.begin(9600);
  if (!BLE.begin()) {
    Serial.print("BLE failed to Initiate");
    while (1);
  }
  if (!IMU.begin()) {
    Serial.println("Failed to initialize IMU!");
    while (1);
  }

  // Defining BLE name
  BLE.setConnectionInterval(0x0004, 0x0008);
  BLE.setConnectable(true);
  BLE.setLocalName("Left Foot");
  BLE.setAdvertisedService(ArduinoService);

  //Add charachteristic to the service
  ArduinoService.addCharacteristic(Char_acc); ArduinoService.addCharacteristic(Char_gyro);
  ArduinoService.addCharacteristic(Char_magn); ArduinoService.addCharacteristic(Char_pres);


  //add service and advertise
  BLE.addService(ArduinoService);
  BLE.advertise();
  Serial.print("Bluetooth device is now active, waiting for connections...");



  ////pressure sesnor filter
  avgPres_hill.clear() ;
  avgPres_toe.clear();
  acc1.clear();
  acc2.clear();
  acc3.clear();
  mag1.clear();
  mag2.clear();
  mag3.clear();



  ///calibration
  // Magnetometer code


  IMU.setAccelFS(2);
  IMU.setAccelODR(3);           //
  IMU.setAccelOffset(-0.031, -0.0031, -0.0031);  //   uncalibrated
  IMU.setAccelSlope (0.9970, 0.9970, 0.9970);  //   uncalibrated
  IMU.accelUnit = GRAVITY;   

  IMU.setGyroFS(2);
  IMU.setGyroODR(3);
  IMU.setGyroOffset (1.2, 0.7, 0.6);  // = uncalibrated
  IMU.setGyroSlope  (1.2, 1.2, 1.2);  // = uncalibrated
  IMU.gyroUnit = DEGREEPERSECOND;

  IMU.setMagnetFS(0);
  IMU.setMagnetODR(8);
  IMU.setMagnetOffset(1.826782, 16.745605, -118.595581);
  IMU.setMagnetSlope (1.230582, 1.157466, 1.263245);
  IMU.magnetUnit = MICROTESLA;  //   GAUSS   MICROTESLA   NANOTESLA


  filter.begin(100);
  micros_per_reading = 1000000 / 100;
  micros_previous = micros();


  delay(1);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
void loop() {

  // listen for BLE peripherals to connect:
  BLEDevice central = BLE.central();
  if (central) {
    Serial.print("Connected to central: ");
    // print the central's BT address:
    Serial.println(central.address());
    //initializing variables

    while (central.connected()) {
      unsigned long micros_now;
      micros_now = micros();

      ///temporal parameters
      fsr1 = analogRead(fsranalog1);
      fsr2 = analogRead(fsranalog2);



      avgPres_hill.addValue(fsr1);
      int hillavg = avgPres_hill.getAverage() ;


      avgPres_toe.addValue(fsr2);
      int toeavg = avgPres_toe.getAverage() ;

      String hill_plus_toe = String(hillavg) + "," + String(toeavg) ;





      //////* IMU VALUES*//////


      //reading and writing IMU values into the characteristics
      float a_x, a_y, a_z, g_x, g_y, g_z, m_x, m_y, m_z ;
      IMU.readAccel(a_x, a_y, a_z);
      IMU.readGyro(g_x, g_y, g_z);
      IMU.readMagnet(m_x, m_y, m_z);
       String kk = String(a_x) + "," + String(a_y) + "," + String(a_z) + "," + String(g_x)  + "," + String(g_y)  + "," + String(g_z) + "," + String( m_x) + "," + String(m_y) + "," +  String(m_z) ; 

      if ((micros_now - micros_previous >= micros_per_reading)) {
        filter.update(g_x, g_y, -g_z, a_x, a_y, -a_z, -m_x, m_y, -m_z); //for all 3
        roll = filter.getRoll();
        pitch = filter.getPitch();
        heading = filter.getYaw();
//        Serial.print("Orientation: ");
//        Serial.print(roll);
//        Serial.print(" ");
//        Serial.print(pitch );
//        Serial.print(" ");
//        Serial.println(heading);
//        Serial.print(",");
      }

      
      acc1.addValue(a_x);
      acc2.addValue(a_y);
      acc3.addValue(a_z);
      float accelix = acc1.getAverage();
      float acceliy = acc2.getAverage();
      float acceliz = acc3.getAverage();

      mag1.addValue(m_x);
      mag2.addValue(m_y);
      mag3.addValue(m_z);
      float magix = mag1.getAverage();
      float magiy = mag2.getAverage();
      float magiz = mag3.getAverage();


      int ax = round(accelix * 100) ;
      int ay = round(acceliy * 100) ;
      int az = round(acceliz * 100) ;

      int gx = round(g_x) ;
      int gy = round(g_y) ;
      int gz = round(g_z) ;

      int mx = round(magix * 10) ;
      int my = round(magiy * 10) ;
      int mz = round(magiz * 10) ;

      String a_x_s = String(ax); a_x_s.remove(4);
      String a_y_s = String(ay); a_y_s.remove(4);
      String a_z_s = String(az); a_z_s.remove(4);
      String x1 = a_x_s + "," + a_y_s + "," + a_z_s ;


      String g_x_s = String(gx); g_x_s.remove(4);
      String g_y_s = String(gy); g_y_s.remove(4);
      String g_z_s = String(gz); g_z_s.remove(4);
      String in_2 = String(g_x_s + g_y_s + g_z_s);
      String x2 = g_x_s + "," + g_y_s + "," + g_z_s ;

      //find offset and gain
      String m_x_s = String(mx); m_x_s.remove(4);
      String m_y_s = String(my); m_y_s.remove(4);
      String m_z_s = String(mz); m_z_s.remove(4);
      String x3 = m_x_s + "," + m_y_s + "," + m_z_s ;
      Char_acc.writeValue(x1);
      Char_gyro.writeValue(x2);
      Char_magn.writeValue(x3);
      Char_pres.writeValue(hill_plus_toe);






      Serial.print(x1+","+x2+","+x3);
      Serial.print("\r\n");
      Serial.print("'"+kk);
      //        //Serial.print(a_res_d);
      Serial.print("\r\n");

      micros_previous = micros_previous + micros_per_reading;




    }

  }
  digitalWrite(LED_BUILTIN, LOW);
  Serial.print("Disconnected from central: ");
  Serial.println(central.address());




}


//
//LSM9DS1 imu;
//void setup(){
//    imu.calibrate(false);         //calibrates but does not store bias
//    imu.calibrateMag(true);       //calibrates and stores bias
//}










