package io.mateu.jpautils.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity@Getter
@Setter
public class Entidad {

    @Id@GeneratedValue
    private long id;

    private String nombre = "Entidad " + LocalDateTime.now();

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (id > 0 && obj != null && obj instanceof Entidad && id == ((Entidad) obj).getId());
    }

}
