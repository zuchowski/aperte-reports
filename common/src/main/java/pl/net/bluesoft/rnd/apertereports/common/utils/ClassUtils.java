package pl.net.bluesoft.rnd.apertereports.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Utility methods for extracting classes from a package.
 */
public class ClassUtils {
    /**
     * The main workhorse of this class. Extracts all classes from a given package.
     * Uses current thread's class loader to retrieve the reference to the package.
     *
     * @param pack Package signature
     * @return List of classes from the given package
     * @throws ClassNotFoundException When the package is invalid
     */
    public static Class<?>[] getClassesFromPackage(String pack) throws ClassNotFoundException {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

        File directory = null;
        String protocol = null;
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            String path = pack.replace('.', '/');
            URL resource = cld.getResource(path);
            if (resource == null) {
                throw new ClassNotFoundException("No resource for " + path);
            }
            directory = new File(resource.getFile());
            protocol = resource.getProtocol();
        }
        catch (NullPointerException x) {
            throw new ClassNotFoundException(pack + " (" + directory + ") does not appear to be a valid package");
        }
        if ("jar".equals("" + protocol)) {
            JarInputStream jarFile = null;
            try {
                String path = directory.getPath();
                jarFile = new JarInputStream(new FileInputStream(path.substring(5, path.indexOf("!"))));
                JarEntry jarEntry = jarFile.getNextJarEntry();
                while (jarEntry != null) {
                    Class<?> clazz = extractClassFromJar(pack.replace('.', '/'), jarEntry);
                    if (clazz != null) {
                        classes.add(clazz);
                    }
                    jarEntry = jarFile.getNextJarEntry();
                }
                closeJarFile(jarFile);
            }
            catch (Exception e) {
                throw new ClassNotFoundException(pack + " does not appear to be a valid package", e);
            }
            finally {
                closeJarFile(jarFile);
            }
        }
        else if (directory.exists()) {
            String[] files = directory.list();
            for (String file : files) {
                if (file.endsWith(".class")) {
                    classes.add(Class.forName(pack + '.' + file.substring(0, file.length() - 6)));
                }
            }
        }
        else {
            throw new ClassNotFoundException(pack + " does not appear to be a valid package");
        }

        return classes.toArray(new Class[0]);
    }

    private static void closeJarFile(final JarInputStream jarFile) throws ClassNotFoundException {
        if (jarFile != null) {
            try {
                jarFile.close();
            }
            catch (IOException e) {
                throw new ClassNotFoundException("Closing jarfile ended with exception " + e);
            }
        }
    }

    private static Class<?> extractClassFromJar(final String path, JarEntry jarEntry) throws ClassNotFoundException {
        String className = jarEntry.getName();
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - ".class".length());
            if (className.startsWith(path)) {
                return Class.forName(className.replace('/', '.'));
            }
        }
        return null;
    }

}
