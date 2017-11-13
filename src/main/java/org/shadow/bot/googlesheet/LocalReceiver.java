package org.shadow.bot.googlesheet;

import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;

import java.io.IOException;

public class LocalReceiver implements VerificationCodeReceiver {
    public String getRedirectUri() throws IOException {
        return null;
    }

    public String waitForCode() throws IOException {
        return null;
    }

    public void stop() throws IOException {

    }
}
