package com.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Pavel Zeger
 */
public class ConsoleHelper {

    private final static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {
        System.out.println(message);
    }

    public static String readString() {
        String text;
        try {
            text = reader.readLine();
        } catch (IOException e) {
            writeMessage("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            return readString();
        }
        return text;
    }

    public static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e) {
                writeMessage("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }
    }

}
