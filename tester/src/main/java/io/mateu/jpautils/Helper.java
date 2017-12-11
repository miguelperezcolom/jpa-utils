package io.mateu.jpautils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class Helper {

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
        transact("default", t);
    }

    private static EntityManagerFactory getEMF() {
        return getEMF("default");
    }

    private static EntityManagerFactory getEMF(String persistenceUnit) {
        EntityManagerFactory v;
        if ((v = emf.get(persistenceUnit)) == null) {
            emf.put(persistenceUnit, v = Persistence.createEntityManagerFactory(persistenceUnit));
        }
        return v;
    }


}
