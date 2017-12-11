package io.mateu.jpautils;

import io.mateu.jpautils.pu1.User;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Throwable {
        System.out.println( "Hello World!" );

        System.setProperty("javax.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/xxx");

        test1();

    }

    private static void test1() throws Throwable {
        Helper.transact((em) -> {
            User u = new User();
            u.setName("xxx");
            em.persist(u);
        });

    }
}
