package io.mateu.jpautils.core;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({ "io.mateu.jpautils.core.PU" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AnnotationProcessor extends AbstractProcessor {

    private int round = 0;

    private Messager messager;
    private Elements elementsUtils;
    private Types typeUtils;
    private Filer filer;

    List<String> pendiente = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // reference often used tools from the processingEnv
        messager = processingEnv.getMessager();
        elementsUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();

        messager.printMessage(Diagnostic.Kind.WARNING, "Generando fuentes: Inicio");

        messager.printMessage(Diagnostic.Kind.NOTE, "filer=" + filer.getClass().getCanonicalName());

        System.out.println("Round: " + round);
        for (Element element : roundEnv.getRootElements()) {
            System.out.printf("%s = %s (%s)\n", element.getSimpleName(), element.asType(), element.asType().getKind());
        }
        round++;


        for (String cl : pendiente) {
            try {

                Class c = Class.forName("io.mateu.jpautils.pu0.PU0");

                //Class c = Class.forName(cl);
                Object o = c.newInstance();
                PU a = (PU) c.getAnnotation(PU.class);
                Class[] clases = a.classes();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }


        // generate code for annotated elements
        Set<? extends Element> annotatedElements;
        try {
            annotatedElements = roundEnv.getElementsAnnotatedWith((Class<? extends Annotation>) Class.forName("io.mateu.jpautils.core.PU"));
            for (TypeElement element : ElementFilter.typesIn(annotatedElements)) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Generando fuentes para " + element.getQualifiedName());
                if (false) generatePersistenceXml(messager, elementsUtils, typeUtils, filer, element);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // claim the annotation
        return false;
    }

    private void generatePersistenceXml(Messager messager, Elements elementsUtils, Types typeUtils, Filer filer, TypeElement clase) {

        messager.printMessage(Diagnostic.Kind.NOTE, "generando interfaz asíncrona...");
        try {

            try {
                Class c = Class.forName(String.valueOf(clase.getQualifiedName()));
                Object o = c.newInstance();
                PU a = (PU) c.getAnnotation(PU.class);
                Class[] clases = a.classes();

            } catch (ClassNotFoundException e) {
                pendiente.add(String.valueOf(clase.getQualifiedName()));
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }


            String simpleName = clase.getSimpleName().toString();
            String packageName = elementsUtils.getPackageOf(clase).getQualifiedName().toString();
            String typeName = clase.getSimpleName().toString() + "Async";

            packageName = packageName.replaceAll("\\.shared\\.", ".client.");
            if (packageName.endsWith(".shared")) packageName = packageName.substring(0, packageName.lastIndexOf(".") + 1) + "client";

            //JavaFileObject javaFile = filer.createSourceFile("/META-INF/persistence.xml", clase);


            List<String> entidades = new ArrayList<>();

            try {
                acumular(entidades, clase);
            } catch (Exception e) {
                e.printStackTrace();
            }


            FileObject file = null;
            try {
                file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/persistence.xml", clase);
            } catch (Exception e) {
                e.printStackTrace();
            }
            messager.printMessage(Diagnostic.Kind.NOTE, "generando " + file.toUri() + "...");
            Writer writer = file.openWriter();
            PrintWriter pw = new PrintWriter(writer);

            String clases = "";
            for (String x : entidades) clases += "<class>" + x + "</class>\n";

            pw.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence persistence_2_1.xsd\" version=\"2.1\">\n" +
                    "    <persistence-unit name=\"default\" transaction-type=\"RESOURCE_LOCAL\">\n" +
                    "    <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>\n" +
                    "        <!--\n" +
                    "\n" +
                    "        <class>io.mateu.erp.model.product.AbstractContract</class>\n" +
                    "        <class>io.mateu.erp.model.taxes.VATPercent</class>\n" +
                    "        <class>io.mateu.erp.model.taxes.VATSettlement</class>\n" +
                    "\n" +
                    "        <class>io.mateu.erp.model.product.hotel.MaxCapacitiesConverter</class>\n" +
                    "        <class>io.mateu.erp.model.product.hotel.offer.DatesRangeListConverter</class>\n" +
                    "        <class>io.mateu.erp.model.product.hotel.contracting.HotelContractPhotoConverter</class>\n" +
                    "\n" +
                    "        <class>io.mateu.erp.model.util.LocalDateAttributeConverter</class>\n" +
                    "        <class>io.mateu.erp.model.util.LocalDateTimeAttributeConverter</class>\n" +
                    "        <class>io.mateu.erp.model.util.IntArrayAttributeConverter</class>\n" +
                    "\n" +
                    "        <exclude-unlisted-classes>true</exclude-unlisted-classes>\n" +
                    "\n" +
                    "-->\n" +
                    "" +
                    "" + clases +
                    "" +
                    "        <exclude-unlisted-classes>true</exclude-unlisted-classes>\n" +
                    "\n" +
                    "\n" +
                    "<validation-mode>AUTO</validation-mode>\n" +
                    "\n" +
                    "" +
                    "    <!--\n" +
                    "    CHECK: https://wiki.eclipse.org/EclipseLink/FAQ/JPA/PostgreSQL\n" +
                    "    -->\n" +
                    "    <properties>\n" +
                    "\n" +
                    "        <property name=\"eclipselink.logging.level\" value=\"FINE\"/>\n" +
                    "\n" +
                    "        <property name=\"eclipselink.weaving\" value=\"static\"/>\n" +
                    "\n" +
                    "\n" +
                    "        <!-- this property is overrided by eclipselink.ddl-generation -->\n" +
                    "        <property name=\"javax.persistence.schema-generation.database.action\" value=\"drop-and-create\"/>\n" +
                    "        <property name=\"xxjavax.persistence.schema-generation.database.action\" value=\"create\"/>\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "        <property name=\"javax.persistence.jdbc.driver\" value=\"org.postgresql.Driver\" />\n" +
                    "        <property name=\"javax.persistence.jdbc.url\"    value=\"jdbc:postgresql://localhost:5432/xxx\" />\n" +
                    "        <property name=\"javax.persistence.jdbc.user\" value=\"postgres\" />\n" +
                    "        <property name=\"javax.persistence.jdbc.password\" value=\"aa\" />\n" +
                    "\n" +
                    "        <!--\n" +
                    "        <property name=\"eclipselink.ddl-generation\" value=\"create-tables\" />\n" +
                    "        -->\n" +
                    "        <property name=\"eclipselink.ddl-generation\" value=\"create-or-extend-tables\" />\n" +
                    "        <property name=\"xxeclipselink.ddl-generation\" value=\"drop-and-create-tables\" />\n" +
                    "        <property name=\"eclipselink.ddl-generation.output-mode\" value=\"database\" />\n" +
                    "        <property name=\"eclipselink.jdbc.uppercase-columns\" value=\"true\"/>\n" +
                    "        <property name=\"eclipselink.jpa.uppercase-column-names\" value=\"true\"/>\n" +
                    "" +
                    "<!-- <property name=\"javax.persistence.validation.mode\" value=\"AUTO\" /> -->" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "        <property name=\"xxeclipselink.cache.coordination.protocol\" value=\"jms\"/>\n" +
                    "        <property name=\"eclipselink.cache.coordination.jms.topic\" value=\"java:comp/env/jms/l2cache\"/>\n" +
                    "        <property name=\"eclipselink.cache.coordination.jms.factory\" value=\"java:comp/env/jms/mateu\"/>\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    //"        <property name=\"eclipselink.target-database\" value=\"io.mateu.jpautils.core.MiPostgreSQLPlatform\"/>\n" +
                    "    </properties>\n" +
                    "</persistence-unit>\n" +
                    "</persistence>");
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private void acumular(List<String> entidades, TypeElement clase) {
        entidades.addAll(extraer(clase.getAnnotation(PU.class)));
        if (clase.getSuperclass() != null && clase.getSuperclass().getAnnotation(PU.class) != null) {
            entidades.addAll(extraer(clase.getSuperclass().getAnnotation(PU.class)));
        }
    }

    private List<String> extraer(PU a) {
        //@io.mateu.jpautils.core.PU(classes=io.mateu.jpautils.pu1.User)

        List<String> l = new ArrayList<>();
        String s = a.toString();
        s = s.substring(s.indexOf("@io.mateu.jpautils.core.PU(classes=") + "@io.mateu.jpautils.core.PU(classes=".length());
        s = s.substring(0, s.indexOf(")"));
        System.out.println(s);

        for (String x : s.split(",")) l.add(x);

        return l;
    }
}
