package org.shadow.bot.googlesheet;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

public class MyGoogleConnection {

    public static GoogleCredential authorize() throws IOException {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("./auth/1111.json"))
                .createScoped(Collections.singleton(com.google.api.services.sheets.v4.SheetsScopes.SPREADSHEETS_READONLY));
        return credential;
    }

    public static void main(String[] args) throws IOException {

    }

}
