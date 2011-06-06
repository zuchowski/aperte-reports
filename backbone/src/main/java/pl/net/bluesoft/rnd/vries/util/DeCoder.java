package pl.net.bluesoft.rnd.vries.util;

import org.apache.commons.codec.binary.Base64;
import pl.net.bluesoft.rnd.vries.xml.XmlHelper;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Utility class for converting XML and <code>Base64</code> strings.
 */
public class DeCoder {

    /**
     * Gets a <code>Base64</code> encoded string from an input object of type <code>ByteArrayOutputStream</code>.
     *
     * @param baos A <code>ByteArrayOutputStream</code> instance
     * @return A <code>Base64</code> encoded character array
     */
    public static char[] baosToEncodedString(ByteArrayOutputStream baos) {
        return new String(Base64.encodeBase64(baos.toByteArray())).toCharArray();
    }

    /**
     * Decodes a <code>Base64</code> character array to a <code>ByteArrayInputStream</code> instance.
     *
     * @param input A <code>Base64</code> encoded character array
     * @return A <code>ByteArrayInputStream</code> instance
     */

    public static ByteArrayInputStream decodeStringToBais(char[] input) {
        return new ByteArrayInputStream(Base64.decodeBase64(new String(input).getBytes()));
    }

    /**
     * Legacy method. Invokes {@link XmlHelper#xmlAsMap(String)}.
     *
     * @param parameters Character array that should be XML of report parameters
     * @return A map of report parameters
     * @see XmlHelper
     */
    @Deprecated
    public static Map<String, String> deserializeParameters(char[] parameters) {
        try {
            return XmlHelper.xmlAsMap(String.valueOf(parameters));
        }
        catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Legacy method. Invokes {@link XmlHelper#mapAsXml(java.util.Map)}.
     *
     * @param parameters Map of report parameters
     * @return parameters transformed into character array
     * @see XmlHelper
     */
    @Deprecated
    public static char[] serializeParameters(Map<String, String> parameters) {
        try {
            return XmlHelper.mapAsXml(parameters).toCharArray();
        }
        catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
