package org.shadow.bot.googlesheet;

import org.shadow.bot.BotSettings;
import org.shadow.bot.config.MenuEntry;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.*;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GoogleSheetBot extends TelegramLongPollingBot {

    private static final String CHOOSE_TOPIC = "Choose info topic : ";
    private String REQUEST_CONTACT = "Send me your contact info";

    private GoogleSheetAdapter googleSheetAdapter;
    private BotSettings botSettings;


    public GoogleSheetBot() throws IOException, JAXBException {
        botSettings = new BotSettings();
        googleSheetAdapter = new GoogleSheetAdapter();
    }

    @Override
    public void onUpdateReceived(Update update) {

        System.out.println("update = " + update);


        try {
            Integer user_id = getUserId(update);

            if (update.hasCallbackQuery()) {

                CallbackQuery callbackQuery = update.getCallbackQuery();

                Long chatId = update.getCallbackQuery().getMessage().getChatId();

                String callbackData = callbackQuery.getData();

                String specialMessage;

                if ("google_sheets".equals(callbackData)) {

                    IGoogleSheetData iGoogleSheetData = googleSheetAdapter.getGoogleSheetData(user_id);


                    if (iGoogleSheetData == null) {
                        specialMessage = "Your data isn't present in Goole sheet. Contact our managers";
                    } else {
                        specialMessage = fillSpecialMessage(iGoogleSheetData);
                    }

                } else {
                    Map<String, MenuEntry> menuEntryMap = botSettings.getMenu();
                    MenuEntry menuEntry = menuEntryMap.get(callbackData);
                    specialMessage = menuEntry.getText();
                }

                sendHTMLMessage(this, chatId, specialMessage);

            } else if (update.hasMessage()) {


                Message message = update.getMessage();

                Contact contact = message.getContact();

                if (contact != null && contact.getPhoneNumber() != null) {
                    googleSheetAdapter.saveContact(user_id, contact.getPhoneNumber());
                }

                boolean isKnownUser = googleSheetAdapter.isKnownUser(user_id);


                if (!isKnownUser) {
                    sendWelcomeMessage(this, message);
                    return;
                }

                sendTopicButton(this, message);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendTopicButton(AbsSender sender, Message incomingMessage) throws TelegramApiException {
        Long chatId = incomingMessage.getChatId();

        sendInlineButtons(sender, chatId);

    }

    private void sendInlineButtons(AbsSender sender, Long chatId) throws TelegramApiException {

        Map<String, MenuEntry> menuEntryMap = botSettings.getMenu();

        final SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(CHOOSE_TOPIC);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = null;

        int topicNo = 0;

        for (String keyTopic : menuEntryMap.keySet()) {

            if (topicNo % 3 == 0) {
                if (rowInline != null) {
                    // Set the keyboard to the markup
                    rowsInline.add(rowInline);
                }
                rowInline = new ArrayList<>();
            }
            MenuEntry menuEntry = menuEntryMap.get(keyTopic);
            rowInline.add(new InlineKeyboardButton().setText(menuEntry.getName()).setCallbackData(keyTopic));
            topicNo++;
        }

        if(rowInline.size() > 0)
        {
            rowsInline.add(rowInline);
        }
        // Add it to the message
        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
        sendMessage.setParseMode(ParseMode.HTML);

        sender.sendMessage(sendMessage);
    }

    private String fillSpecialMessage(IGoogleSheetData iGoogleSheetData) {

        return iGoogleSheetData.getSpecialMessage();
    }

    private void sendWelcomeMessage(AbsSender sender, Message incomingMessage) throws TelegramApiException {

        Long chatId = incomingMessage.getChatId();

        User user = incomingMessage.getFrom();

        String userText = "Hello, " + user.getFirstName() + " " + user.getLastName();

        sendTextMessage(sender, chatId, userText);

        sendTextMessage(sender, chatId, "Let me know who are you");

        sendRequestContactMessage(sender, chatId);

    }

    private void sendRequestContactMessage(AbsSender sender, Long chatId) throws TelegramApiException {
        final SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(REQUEST_CONTACT);

        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup();
        replyKeyboard.setOneTimeKeyboard(true);

        KeyboardButton requestContact = new KeyboardButton();
        requestContact.setText(REQUEST_CONTACT);
        requestContact.setRequestContact(true);

        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(requestContact);

        replyKeyboard.setKeyboard(Collections.singletonList(keyboardRow));

        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setReplyMarkup(replyKeyboard);

        sender.sendMessage(sendMessage);

    }

    private void sendTextMessage(AbsSender sender, Long chatId, String messageText) throws TelegramApiException {
        final SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageText);
        sender.sendMessage(sendMessage);
    }

    private void sendHTMLMessage(AbsSender sender, Long chatId, String messageText) throws TelegramApiException {
        final SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageText);
        sendMessage.setParseMode(ParseMode.HTML);
        sender.sendMessage(sendMessage);
    }

    private Integer getUserId(final Update update) {

        if (update.getMessage() != null) {
            return update.getMessage().getFrom().getId();
        } else if (update.getCallbackQuery() != null) {
            return update.getCallbackQuery().getFrom().getId();
        } else if (update.getChosenInlineQuery() != null) {
            return update.getChosenInlineQuery().getFrom().getId();
        } else if (update.getInlineQuery() != null) {
            return update.getInlineQuery().getFrom().getId();
        }

        return -1;
    }

    public String getBotUsername() {
        return botSettings.getAuthInfo().getUsername();
    }

    public String getBotToken() {
        return botSettings.getAuthInfo().getToken();
    }

    public void onClosing() {

    }
}
