package com.example.android.bluetoothchat;

/**
 * Created by notroot on 09/05/16.
 */
public class control {

    BluetoothChatFragment BCF = new BluetoothChatFragment();
    int control_code;

    public void moveForward(){
        control_code += 128;
        BCF.sendMessage(control_code);
    }

    public void stopmovingForward(){
        control_code -= 128;
        BCF.sendMessage(control_code);

    }

    public void moveBackward(){
        control_code += 64;
        BCF.sendMessage(control_code);
    }

    public void stopmoveBackward(){
        control_code -= 64;
        BCF.sendMessage(control_code);
    }

    public void turnRight(){
        control_code +=  32;
        BCF.sendMessage(control_code);
    }
    public void stopturnRight(){
        control_code -=  32;
        BCF.sendMessage(control_code);
    }

    public void turnLeft(){
        control_code += 16;
        BCF.sendMessage(control_code);

    }

    public void stopturnLeft(){
        control_code -= 16;
        BCF.sendMessage(control_code);

    }

    public void toggleLEDs(){
        BCF.sendMessage(192);

    }
}
