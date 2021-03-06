package org.shadow.bot;

import org.shadow.bot.googlesheet.GoogleSheetBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public class StartBot {

    public static void main(String[] args) throws JAXBException {

        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new GoogleSheetBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
