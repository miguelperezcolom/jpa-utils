package io.mateu.jpautils;

import io.mateu.jpautils.model.Entidad;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @org.junit.Test
    public void test1() throws Throwable {

        System.out.println( "Hello World!" );

        Helper.loadProperties();

        Helper.transact(em -> {


            em.persist(new Entidad());

        });


        assertTrue(true);
    }

}
