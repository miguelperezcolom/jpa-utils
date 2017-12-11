package io.mateu.jpautils;

import javax.persistence.EntityManager;

/**
 * Created by miguel on 13/9/16.
 */
public interface JPATransaction {

    public void run(EntityManager em) throws Throwable;

}
