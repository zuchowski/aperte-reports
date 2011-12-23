package org.apertereports.common.xml;

import com.thoughtworks.xstream.XStream;
import pl.net.bluesoft.util.lang.StringUtil;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A helper class that provides convenient methods for transforming the XML form of report parameters
 * to collections (lists, maps) and vice versa.
 * <p>The XML representation is marshaled and unmarshaled using XStream.
 */
public abstract class XmlHelper {
    private XStream xstream;
    private Set<Class> supportedClasses;

    protected abstract Class[] getSupportedClasses();

    protected XmlHelper() {
        Class[] classes = getSupportedClasses();
        xstream = new XStream();
        xstream.processAnnotations(classes);
        supportedClasses = new HashSet<Class>();
        Collections.addAll(supportedClasses, classes);
    }

    public String marshall(Object object) {
        if (object == null || !supportedClasses.contains(object.getClass())) {
            throw new IllegalArgumentException("Object of type: " + (object != null ? object.getClass() : "null") + " is not supported!");
        }
        return xstream.toXML(object);
    }

    public Object unmarshall(String xml) {
        if (!StringUtil.hasText(xml)) {
            throw new IllegalArgumentException("Cannot unmarshall an empty string!");
        }
        return xstream.fromXML(xml);
    }

    public Object unmarshall(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("Cannot unmarshall an empty input stream!");
        }
        return xstream.fromXML(stream);
    }
}
