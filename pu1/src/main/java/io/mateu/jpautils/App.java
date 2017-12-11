package io.mateu.jpautils;

import io.mateu.jpautils.core.EntitiesDiscoverer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        System.out.println(EntitiesDiscoverer.discover("io.mateu"));
    }
}
