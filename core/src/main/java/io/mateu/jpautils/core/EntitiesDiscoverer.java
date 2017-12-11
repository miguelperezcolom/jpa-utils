package io.mateu.jpautils.core;

import org.reflections.Reflections;

import javax.persistence.Converter;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntitiesDiscoverer {

    public static void main(String... args) {
        System.out.println(discover("io.mateu"));
    }

    public static String discover(String pkg) {

        Reflections reflections = new Reflections(pkg);

        Set<Class<?>> clases = reflections.getTypesAnnotatedWith(Entity.class);
        clases.addAll(reflections.getTypesAnnotatedWith(Embeddable.class));
        clases.addAll(reflections.getTypesAnnotatedWith(Converter.class));

        String r = "";
        for (Class c : clases) {
            if (!"".equals(r)) r += "\n,";
            r += c.getName() + ".class";
        }
        return r;

    }

}
