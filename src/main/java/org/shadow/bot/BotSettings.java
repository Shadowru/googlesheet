package org.shadow.bot;

import org.shadow.bot.config.AuthInfo;
import org.shadow.bot.config.MenuEntry;
import org.shadow.bot.config.XmlConfig;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BotSettings {

    private final Timer reloadTimer;
    private XmlConfig xmlConfig;

    public BotSettings() throws JAXBException {
        loadXMLConfig();
        reloadTimer = new Timer();
        reloadTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    loadXMLConfig();
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }
        }, 10000, 10000);
    }

    private void loadXMLConfig() throws JAXBException {
        File file = new File("./data/config.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(XmlConfig.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        xmlConfig = (XmlConfig) jaxbUnmarshaller.unmarshal(file);
    }

    public AuthInfo getAuthInfo() {
        return xmlConfig.getAuthInfo();
    }

    public Map<String, MenuEntry> getMenu() {
        return xmlConfig.getMenuMap();
    }
}
