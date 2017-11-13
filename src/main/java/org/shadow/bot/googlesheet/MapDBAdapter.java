package org.shadow.bot.googlesheet;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArray;

import java.util.Arrays;

public class MapDBAdapter {

    private final DB db;
    private final HTreeMap<String, Object[]> sheetDataMap;

    private final DB fileDB;
    private final HTreeMap<Integer, String> contactDataMap;

    public MapDBAdapter() {
        db = DBMaker.memoryDB().make();
        sheetDataMap = db.hashMap("sheetDataMap")
                .keySerializer(Serializer.STRING)
                .valueSerializer(new SerializerArray(Serializer.STRING))
                .createOrOpen();

        fileDB = DBMaker.fileDB("./db/contact.map").make();

        contactDataMap = fileDB.hashMap("contact")
                .keySerializer(Serializer.INTEGER)
                .valueSerializer(Serializer.STRING)
                .createOrOpen();

    }

    public void clear() {
        sheetDataMap.clear();
    }

    public void putData(String key, Object[] data) {
        sheetDataMap.put(key, data);
    }

    public String[] getData(String s) {

        final Object[] objectArray = sheetDataMap.get(s);

        if (objectArray == null) {
            return null;
        }

        String[] stringArray = Arrays.copyOf(objectArray, objectArray.length, String[].class);

        return stringArray;
    }

    public String getPhoneContact(Integer userId) {
        String phone = contactDataMap.get(userId);
        return phone;
    }

    public String setPhoneContact(Integer userId, String phone) {
        contactDataMap.put(userId, phone);
        flushContact();
        return getPhoneContact(userId);
    }

    public void flushContact() {
        fileDB.commit();
    }

    public static void main(String[] args) {

        MapDBAdapter mapDBAdapter = new MapDBAdapter();
        mapDBAdapter.clear();

        String[] asd = new String[3];
        asd[0] = "1";
        asd[1] = "2";
        asd[2] = "3";

        mapDBAdapter.putData("123", asd);

        String[] rr = mapDBAdapter.getData("123");

        System.out.println(rr);

        mapDBAdapter.clear();


        rr = mapDBAdapter.getData("123");

        System.out.println(rr);

        mapDBAdapter.putData("111111", asd);

        rr = mapDBAdapter.getData("111111");

        System.out.println(rr);
    }
}
