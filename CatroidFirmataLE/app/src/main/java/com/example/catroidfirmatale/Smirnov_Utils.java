package com.example.catroidfirmatale;

public class Smirnov_Utils {
    private static final int PINS_IN_A_PORT = 8;


    public static int getPortFromPin(int pin) {
        return pin / PINS_IN_A_PORT;
    }

    public static int getIndexOfPinOnPort(int pin) {
        return pin % PINS_IN_A_PORT;
    }

    public static int setBit(int number, int index, int value) {
        if ((index >= 0) && (index < 32)) {
            if (value == 0) {
                return number & ~(1 << index);
            } else {
                return number | (1 << index);
            }
        }
        return number;
    }

    public static int getDigitalPinValue(int pin, int value) {
        int port = getPortFromPin(pin);
        int index = getIndexOfPinOnPort(pin);
        return setBit(0, index, value);
    }
}
