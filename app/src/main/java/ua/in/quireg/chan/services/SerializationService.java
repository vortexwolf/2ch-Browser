package ua.in.quireg.chan.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import ua.in.quireg.chan.common.utils.IoUtils;

public class SerializationService {

    public static void serializeToFile(File file, Serializable obj) {

        Timber.v("serializeToFile() - %s", file.toString());
        Completable.create(
                e -> {

                    ObjectOutputStream os = null;
                    try {
                        os = new ObjectOutputStream(new FileOutputStream(file));
                        os.writeObject(obj);
                        e.onComplete();
                    } catch (IOException ex) {
                        Timber.e(ex, "serialization failed for %s", file.toString());
                    } finally {
                        IoUtils.closeStream(os);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public static Object deserializeFromFile(File file) {
        Timber.v("deserializeFromFile() - %s", file.toString());

        if (!file.exists()) {
            return null;
        }

        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new FileInputStream(file));

            return is.readObject();
        } catch (Exception e) {
            Timber.e(e, "deserialization failed for %s", file.toString());
        } finally {
            IoUtils.closeStream(is);
        }

        return null;
    }

    public static String serializeToString(Serializable obj) throws IOException {
        if (obj == null) return "";
        try {
            ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
            objStream.writeObject(obj);
            objStream.close();
            return encodeBytes(serialObj.toByteArray());
        } catch (Exception e) {
            Timber.e("Serialization error: %s", e.getMessage());
            return null;
        }
    }

    public static Object deserializeFromString(String str) throws IOException {
        if (str == null || str.length() == 0) return null;
        try {
            ByteArrayInputStream serialObj = new ByteArrayInputStream(decodeBytes(str));
            ObjectInputStream objStream = new ObjectInputStream(serialObj);
            return objStream.readObject();
        } catch (Exception e) {
            Timber.e("Deserialization error: " + e.getMessage());
            return null;
        }
    }

    private static String encodeBytes(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();

        for (byte aByte : bytes) {
            stringBuilder.append((char) (((aByte >> 4) & 0xF) + ((int) 'a')));
            stringBuilder.append((char) (((aByte) & 0xF) + ((int) 'a')));
        }

        return stringBuilder.toString();
    }

    private static byte[] decodeBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length(); i += 2) {
            char c = str.charAt(i);
            bytes[i / 2] = (byte) ((c - 'a') << 4);
            c = str.charAt(i + 1);
            bytes[i / 2] += (c - 'a');
        }
        return bytes;
    }

}
