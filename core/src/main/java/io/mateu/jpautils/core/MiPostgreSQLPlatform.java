package io.mateu.jpautils.core;


import org.eclipse.persistence.internal.databaseaccess.FieldTypeDefinition;
import org.eclipse.persistence.platform.database.PostgreSQLPlatform;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Hashtable;

/**
 * Created by miguel on 13/10/16.
 */
public class MiPostgreSQLPlatform extends PostgreSQLPlatform {

    public MiPostgreSQLPlatform() {
        super();
    }

    @Override
    protected Hashtable buildFieldTypes() {
        Hashtable t = super.buildFieldTypes();
        t.put(String.class, new FieldTypeDefinition("TEXT", false));
        t.put(LocalDate.class, new FieldTypeDefinition("DATE", false));
        t.put(LocalDateTime.class, new FieldTypeDefinition("TIMESTAMP WITHOUT TIME ZONE", false));

        return t;
    }
}
