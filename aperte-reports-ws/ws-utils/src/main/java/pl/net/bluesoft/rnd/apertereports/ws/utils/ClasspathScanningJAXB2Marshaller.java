package pl.net.bluesoft.rnd.apertereports.ws.utils;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

public class ClasspathScanningJAXB2Marshaller extends Jaxb2Marshaller {
    private String basePackage;

    public ClasspathScanningJAXB2Marshaller() {
    }

    public ClasspathScanningJAXB2Marshaller(String basePackage) {
        this.basePackage = basePackage;
    }

    public final Class<?>[] getXMLRootClasses() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(XmlRootElement.class));
        try {
            Set<BeanDefinition> components = scanner.findCandidateComponents(basePackage);
            Class<?>[] result = new Class[components.size()];
            int i = 0;
            for (BeanDefinition bd : components) {
                result[i++] = this.getClass().getClassLoader().loadClass(bd.getBeanClassName());
            }
            return result;
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
        this.setClassesToBeBound(getXMLRootClasses());
    }
}
