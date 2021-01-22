package com.chat.client;

import com.chat.ConsoleHelper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Zeger
 */
public class BotClient extends Client {

    @Override
    protected String getUserName() {
        int min = 0;
        int max = 99;
        int randomInt = (int)(Math.random() * ((max - min) + 1)) + min;
        return String.format("date_bot_%s", randomInt);

    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;

    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();

    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (message.contains(":")) {
                String[] messageArray = message.split(":");
                String name = messageArray[0];
                Map<String, String> answerFormats = new HashMap<String, String>() {{
                    put("дата" , "d.MM.YYYY");
                    put("день", "d");
                    put("месяц", "MMMM");
                    put("год", "YYYY");
                    put("время", "H:mm:ss");
                    put("час", "H");
                    put("минуты", "m");
                    put("секунды", "s"); }};
                String text = messageArray[1];
                for (Map.Entry<String, String> entry: answerFormats.entrySet()) {
                    if (text.contains(entry.getKey())) {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(entry.getValue());
                        Calendar calendar = new GregorianCalendar();
                        simpleDateFormat.setCalendar(calendar);
                        String answer = String.format("Информация для %s: %s", name, simpleDateFormat.format(calendar.getTime()));
                        sendTextMessage(answer);
                    }

                }

            }

        }

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();

        }
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();

    }

}
