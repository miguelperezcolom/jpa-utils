package io.mateu.jpautils;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.lifecycle.Phase;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.persistence.Converter;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Mojo(name = "procesarpus", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.COMPILE)
public class PUJoinerMojo extends AbstractMojo {

    @Parameter(required = true)
    private String puname;

    @Parameter
    private String extendspu;

    @Parameter
    private String packages;

    @Parameter
    private String targetdatabase;

    @Parameter(property = "project.build.directory", readonly = true, required = true)
    private File outputDirectory;

    @Parameter(property = "project", readonly = true, required = true)
    private MavenProject project;

    @Component
    private MojoExecution execution;


    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("Fase=" + execution.getLifecyclePhase());

        getLog().info("Creando persistence.xml");

        long t0 = System.currentTimeMillis();

        List<String> classpathElements = null;
        try {
            classpathElements = "test-compile".equalsIgnoreCase(execution.getLifecyclePhase())?project.getTestClasspathElements():project.getCompileClasspathElements();
            List<URL> projectClasspathList = new ArrayList<URL>();
            for (String element : classpathElements) {
                try {
                    File f = new File(element);
                    if (f.getName().endsWith(".jar")) {
                        JarFile jar = new JarFile(f);
                        JarEntry entry = jar.getJarEntry("META-INF/persistence.xml");
                        if (entry != null) {
                            // META-INF/file.txt exists in foo.jar
                            String xml = CharStreams.toString(new InputStreamReader(jar.getInputStream(entry)));
                            if ((!Strings.isNullOrEmpty(extendspu) && xml.contains("name=\"" + extendspu + "\"")) || xml.contains("name=\"" + puname + "\"")) projectClasspathList.add(f.toURI().toURL());
                        } else {
                            projectClasspathList.add(f.toURI().toURL());
                        }
                    } else {
                        projectClasspathList.add(f.toURI().toURL());
                    }
                } catch (MalformedURLException e) {
                    throw new MojoExecutionException(element + " is an invalid classpath element", e);
                }
            }

            ConfigurationBuilder cb = new ConfigurationBuilder();

            List<String> packageNames = new ArrayList<String>();

            if (!Strings.isNullOrEmpty(packages)) {

                getLog().info("packages = " + packages);

                for (String s : packages.split("[,; ]")) packageNames.add(s);

            } else {

                getLog().info("packages no está presente");

                for (File f : new File(project.getBuild().getOutputDirectory()).listFiles()) {
                    if (!f.getName().contains("-")) packageNames.add(f.getName());
                }
                if (!packageNames.contains("org")) packageNames.add("org");
                if (!packageNames.contains("com")) packageNames.add("com");
                if (!packageNames.contains("io")) packageNames.add("io");
            }

            for (String p : packageNames) {
                getLog().info("package " + p);
            }


            for (String pn : packageNames) cb.addUrls(ClasspathHelper.forPackage(pn));

            URLClassLoader loader = new URLClassLoader(projectClasspathList.toArray(new URL[0]));
            // ... and now you can pass the above classloader to Reflections

            Reflections reflections = new Reflections(cb.build(loader));


            Set<Class<?>> clases = reflections.getTypesAnnotatedWith(Entity.class);
            clases.addAll(reflections.getTypesAnnotatedWith(Embeddable.class));
            clases.addAll(reflections.getTypesAnnotatedWith(Converter.class));

            for (Class c : new ArrayList<Class<?>>(clases)) {
                getLog().info("clase " + c.getName());
                boolean ok = false;
                for (String p : packageNames) if (c.getName().startsWith(p)) {
                    ok = true;
                    break;
                }
                if (!ok) clases.remove(c);
            }


            escribirPersistenceXml(clases);

        } catch (DependencyResolutionRequiredException e) {
            new MojoExecutionException("Dependency resolution failed", e);
        } catch (IOException e) {
            new MojoExecutionException("Fallo al escribir el fichero persistence.xml", e);
        }

        long t = System.currentTimeMillis();
        getLog().info("Crear el fichero persistence.xml ha necesitado " + (t - t0) + " ms.");
    }
    private void escribirPersistenceXml(Set<Class<?>> clases) throws IOException {

        File f = new File(("test-compile".equalsIgnoreCase(execution.getLifecyclePhase())?project.getBuild().getTestOutputDirectory():project.getBuild().getOutputDirectory()) + "/META-INF");
        if (!f.exists()) f.mkdirs();

        f = new File(f.getAbsolutePath() + "/persistence.xml");

        String xml = "";
        for (Class c : clases) xml += "         <class>" + c.getCanonicalName() + "</class>\n";

        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence persistence_2_1.xsd\" version=\"2.1\">\n" +
                "    <persistence-unit name=\"" + puname + "\" transaction-type=\"RESOURCE_LOCAL\">\n" +
                "        <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>\n" +
                "        \n" +
                xml +
                "\n" +
                "        <exclude-unlisted-classes>true</exclude-unlisted-classes>\n" +
                "\n" +
                "        <validation-mode>AUTO</validation-mode>\n" +
                "\n" +
                "        <properties>\n" +
                //"            <property name=\"eclipselink.target-database\" value=\"" + ((!Strings.isNullOrEmpty(getTargetdatabase()))?getTargetdatabase():"io.mateu.common.model.util.MiPostgreSQLPlatform") + "\"/>\n" +
                "        </properties>\n" +
                "    </persistence-unit>\n" +
                "</persistence>";

        Files.write(s.getBytes(), f);

    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setPuname(String puname) {
        this.puname = puname;
    }

    public void setExtendspu(String extendspu) {
        this.extendspu = extendspu;
    }


    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public String getTargetdatabase() {
        return targetdatabase;
    }

    public void setTargetdatabase(String targetdatabase) {
        this.targetdatabase = targetdatabase;
    }
}
