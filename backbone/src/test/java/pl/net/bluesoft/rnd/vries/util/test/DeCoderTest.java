/**
 *
 */
package pl.net.bluesoft.rnd.vries.util.test;

import org.junit.Test;
import pl.net.bluesoft.rnd.vries.util.DeCoder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author MW
 */
public class DeCoderTest {

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.vries.util.DeCoder#baosToEncodedString(java.io.ByteArrayOutputStream)}
     * {@link pl.net.bluesoft.rnd.vries.util.DeCoder#decodeStringToBais(char[])}
     * .
     *
     * @throws IOException
     */
    @Test
    public final void testBaosToEncodedString() throws IOException {
        String input = "test";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length());
        baos.write(input.getBytes());
        char[] encoded = DeCoder.baosToEncodedString(baos);
        ByteArrayInputStream decoded = DeCoder.decodeStringToBais(encoded);
        BufferedReader reader = new BufferedReader(new InputStreamReader(decoded));
        StringBuffer output = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        assertEquals("decoded string differs from input", input, output.toString());
    }

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.vries.util.DeCoder#serializeParameters(java.util.Map)}
     * {@link pl.net.bluesoft.rnd.vries.util.DeCoder#deserializeParameters(char[])}
     * .
     */
    @Test
    public final void testSerializeParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("a", "1");
        parameters.put("b", "2");

        char[] serialized = DeCoder.serializeParameters(parameters);
        Map<String, String> deserialized = DeCoder.deserializeParameters(serialized);

        for (Entry<String, String> entry : parameters.entrySet()) {
            assertTrue("key " + entry.getKey() + " not serialized", deserialized.containsKey(entry.getKey()));
            assertEquals("values don't match", entry.getValue(), deserialized.get(entry.getKey()));
        }
    }
}
