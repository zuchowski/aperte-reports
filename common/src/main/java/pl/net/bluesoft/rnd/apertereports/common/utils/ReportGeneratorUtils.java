package pl.net.bluesoft.rnd.apertereports.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.ByteArrayDataSource;

import javax.activation.DataHandler;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;

public class ReportGeneratorUtils {

    public static <T> T resolveFieldValue(ClassLoader classLoader, String className, String fieldName) throws ClassNotFoundException,
            NoSuchFieldException, IllegalAccessException {
        Field field = classLoader.loadClass(className).getDeclaredField(fieldName);
        return (T) field.get(null);
    }

    public static byte[] decodeContent(String content) throws UnsupportedEncodingException {
        return Base64.decodeBase64(content.getBytes("UTF-8"));
    }

    public static Object deserializeObject(byte[] bytes) throws Exception {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return ois.readObject();
        }
        finally {
            ois.close();
        }
    }

    public static byte[] serializeObject(Object object) throws Exception {
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            return bos.toByteArray();
        }
        finally {
            oos.close();
        }
    }

    public static DataHandler wrapBytesInDataHandler(byte[] content, String mimeType) throws IOException {
        return new DataHandler(new ByteArrayDataSource(content, mimeType));
    }

    public static byte[] unwrapDataHandler(DataHandler handler) throws IOException {
        return IOUtils.toByteArray(handler.getInputStream());
    }

    public static Object convertBytesToInputStreams(Object object) {
        if (object instanceof Map) {
            Map map = (Map) object;
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                if (canConvertToInputStreams(value)) {
                    map.put(key, convertBytesToInputStreams(value));
                }
            }
        }
        if (object instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) object);
        }
        return object;
    }

    public static boolean canConvertToInputStreams(Object object) {
        return object instanceof byte[] || object instanceof Map;
    }
}
