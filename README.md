# SmartRC
Android controlled, Arduino based, bluetooth smart RC vehicle platform.

Designed and developed between late 2015 and early 2016 as my final year A-Level Computing project.

Includes:

- Android app for controlling device and recieveing sensor data via bluetooth and video feed via WiFi.

- Arduino sketch for recieving and executing commands as well as interfacing with onboard sensors and communicating via a HC-06 Bluetooth transciever.

Android app forked from Google's [BluetoothChat](https://github.com/googlesamples/android-BluetoothChat) example app and Arduino Sketch uses 
Servo, [DHT](https://github.com/adafruit/DHT-sensor-library) and [CS_MQ7](https://github.com/jmsaavedra/Citizen-Sensor/blob/master/sensors/MQ7%20Breakout/CS_MQ7/CS_MQ7.h) Libraries, which are all optional addons for extra equiptment. A base vehicle with no attachments doesn't require these.

WiFi FPV is supported using a webcam, Raspberry Pi with a WiFi AP dongle and [Motion CCTV software.](https://packages.debian.org/jessie/video/motion)

With it being Arduino based, the vehicles are completely customisable in terms of software via the sketch and also hardware.
The Android app can also be updated to accomodate these, however the platform is based on an 8 control code, so there are limits as to how many extra functions can be implemented.

Screenshots, example hardware diagrams and more information (such as the example hardware below) can be found in the [wiki.](https://github.com/RDP-1/SmartRC/wiki) 

Detailed information about usage of the system can be found in this [manual](https://drive.google.com/file/d/0B9BkMVJY4sRwRmM2NkVrSXkxa00/view)

![SmartRC Hardware Example](http://projects.exlercs.com/images/smartrc.jpg)


![Basic Usage](http://i.imgur.com/fizbAAVr.jpg)
![Camera Control](http://i.imgur.com/6gBZ5iC.jpg)
