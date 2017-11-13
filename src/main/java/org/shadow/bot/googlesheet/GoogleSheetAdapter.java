package org.shadow.bot.googlesheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;

public class GoogleSheetAdapter {

    public static final String APPLICATION_NAME = "Google sheets to TeleBot";

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private final GoogleCredential credential;
    private final Timer reloadTimer;

    private String[] header;
    private MapDBAdapter dbAdapter;

    private static GoogleSheetAdapter googleSheetAdapter;

    public GoogleSheetAdapter() throws IOException {
        credential = MyGoogleConnection.authorize();
        loadData();
        googleSheetAdapter = this;

        reloadTimer = new Timer();
        reloadTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    googleSheetAdapter.loadData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 10000, 10000);

    }

    public static GoogleSheetAdapter getGoogleSheetAdapter() {
        return googleSheetAdapter;
    }

    public String[] getHeader() {
        return header;
    }

    public boolean isKnownUser(Integer userId) {

        String phone = dbAdapter.getPhoneContact(userId);

        if (phone == null) {
            return false;
        }

        return true;
    }

    public IGoogleSheetData getGoogleSheetData(Integer userId) {

        String phone = dbAdapter.getPhoneContact(userId);

        if (phone == null) {
            return null;
        }

        String[] ss = dbAdapter.getData(phone);

        if (ss == null) {
            return null;
        }
        return new GoogleSheetData(ss);
    }

    private List<List<Object>> readTable(Sheets service, String spreadsheetId, String sheetName) throws IOException {
        ValueRange table = service.spreadsheets().values().get(spreadsheetId, sheetName).execute();
        List<List<Object>> values = table.getValues();
        return values;
    }

    private void loadData() throws IOException {
        Sheets sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();

        List<List<Object>> lists = readTable(sheets, "1-zCqmmPu-QgveDS-OyUtEXBYr1LNubItvVFUZp6LIcA", "Лист1");

        boolean isHeader = true;

        clearMapDB();

        for (List<Object> list : lists) {

            if (isHeader) {
                readHeader(list);
                isHeader = false;
            } else {

                putData(list);

            }


        }


    }

    private void putData(List<Object> list) {

        String key = (String) list.get(0);

        ArrayList<String> arrayList = new ArrayList<String>();

        for (int i = 1; i < list.size(); i++) {
            Object tt = list.get(i);
            if (tt != null) {
                arrayList.add((String) tt);
            }
        }

        Object[] data = new Object[arrayList.size()];
        data = arrayList.toArray(data);
        dbAdapter.putData(key, data);
    }

    private void clearMapDB() {
        if (dbAdapter == null) {
            dbAdapter = new MapDBAdapter();
        } else {
            dbAdapter.clear();
        }
    }

    private void readHeader(List<Object> list) {

        header = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            header[i] = (String) list.get(i);
        }
    }

    public static void main(String[] args) throws IOException {
        GoogleSheetAdapter googleSheetAdapter = new GoogleSheetAdapter();

        googleSheetAdapter.loadData();

    }

    public void saveContact(Integer user_id, String phoneNumber) {
        dbAdapter.setPhoneContact(user_id, phoneNumber);
    }
}
