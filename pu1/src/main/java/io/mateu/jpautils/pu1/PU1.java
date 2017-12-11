package io.mateu.jpautils.pu1;

import io.mateu.jpautils.core.AbstractPU;
import io.mateu.jpautils.core.PU;
import io.mateu.jpautils.pu0.PU0;

import java.util.List;

@PU(classes = {
        User.class
})
public class PU1 extends PU0 {
    public List<Class> getClasses() {
        return null;
    }
}
