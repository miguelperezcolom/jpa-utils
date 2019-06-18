package io.mateu.jpautils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Helper {


    private static boolean propertiesLoaded;

    public static void loadProperties() {
        if (!propertiesLoaded) {

            System.out.println("Loading properties...");
            propertiesLoaded = true;
            InputStream s = null;
            try {
                if (System.getProperty("appconf") != null) {
                    System.out.println("Loading properties from file " + System.getProperty("appconf"));
                    s = new FileInputStream(System.getProperty("appconf"));
                } else {
                    s = Helper.class.getResourceAsStream("/appconf.properties");
                    System.out.println("Loading properties classpath /appconf.properties");
                }

                if (s != null) {

                    Properties p = new Properties();
                    p.load(s);

                    for (Map.Entry<Object, Object> e : p.entrySet()) {
                        System.out.println("" + e.getKey() + "=" + e.getValue());
                        if (System.getProperty("" + e.getKey()) == null) {
                            System.setProperty("" + e.getKey(), "" + e.getValue());
                            System.out.println("property fixed");
                        } else {
                            System.out.println("property " + e.getKey() + " is already set with value " + System.getProperty("" + e.getKey()));
                        }
                    }

                    if (System.getProperty("heroku.database.url") != null) {

                        System.out.println("adjusting jdbc properties for Heroku...");

                        URI dbUri = null;
                        try {
                            dbUri = new URI(System.getProperty("heroku.database.url"));
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }


                        System.setProperty("eclipselink.target-database", "io.mateu.common.model.util.MiPostgreSQLPlatform");
                        System.setProperty("javax.persistence.jdbc.driver", "org.postgresql.Driver");

                        String username = dbUri.getUserInfo().split(":")[0];
                        String password = dbUri.getUserInfo().split(":")[1];
                        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require&user=" + username + "&password=" + password;


                        System.setProperty("javax.persistence.jdbc.url", dbUrl);
                        System.getProperties().remove("javax.persistence.jdbc.user");
                        System.getProperties().remove("javax.persistence.jdbc.password");


                    } else if (System.getProperty("javax.persistence.jdbc.url", "").contains("postgres")) {
                        System.setProperty("eclipselink.target-database", "io.mateu.common.model.util.MiPostgreSQLPlatform");
                    }
                    s.close();
                } else {
                    System.out.println("No appconf. Either set -Dappconf=xxxxxx.properties or place an appconf.properties file in your classpath.");
                }

            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } else {
            System.out.println("Properties already loaded");
        }
    }



    private static Map<String, EntityManagerFactory> emf = new HashMap<>();

    public static void transact(String persistenceUnit, JPATransaction t) throws Throwable {
        EntityManager em = getEMF(persistenceUnit).createEntityManager();

        try {

            em.getTransaction().begin();

            t.run(em);


            em.getTransaction().commit();


        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
            throw e;
        }

        em.close();

    }

    public static void transact(JPATransaction t) throws Throwable {
        transact(System.getProperty("defaultpuname", "default"), t);
    }

    private static EntityManagerFactory getEMF() {
        return getEMF(System.getProperty("defaultpuname", "default"));
    }

    private static EntityManagerFactory getEMF(String persistenceUnit) {
        EntityManagerFactory v;
        if ((v = emf.get(persistenceUnit)) == null) {
            emf.put(persistenceUnit, v = Persistence.createEntityManagerFactory(persistenceUnit));
        }
        return v;
    }


}
