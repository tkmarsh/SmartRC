# SmartRC
Android controlled Arduino based bluetooth smart RC vehicles.

Includes:

- Android app for controlling device and recieveing sensor data via bluetooth and video feed via WiFi.

- Arduino sketch for recieving and executing commands as well as interfacing with onboard sensors and communicating via a HC-06 Bluetooth transciever.

Android app forked from Google's [BluetoothChat](https://github.com/googlesamples/android-BluetoothChat) example app and Arduino Sketch uses 
Servo, DHT and [CS_MQ7](https://github.com/jmsaavedra/Citizen-Sensor/blob/master/sensors/MQ7%20Breakout/CS_MQ7/CS_MQ7.h) Libraries, which are all optional addons for extra equiptment. A base vehicles with no attachments doesn't require these.

With it being Arduino based, the vehicles are completely customisable in terms of software via the sketch and also hardware.

WiFi FPV supported using a webcam, Raspberry Pi with a WiFi AP dongle and [Motion CCTV software.](https://packages.debian.org/jessie/video/motion)

Screenshots, hardware diagrams and more information can be found in the wiki. 
