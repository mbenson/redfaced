package redfaced;

import java.util.LinkedList;
import java.util.List;
import javax.faces.FacesWrapper;

/**
 * FacesWrapper utils.
 */
class FacesWrapperUtils {

    interface Unwrapper {
        boolean handles(Object o);

        Object unwrap(Object wrapper);
    }
    
    private static final List<Unwrapper> UNWRAPPERS = new LinkedList<Unwrapper>();

    static void add(Unwrapper u) {
        UNWRAPPERS.add(u);
    }

    static <T> T unwrap(Class<T> type, Object o) {
        Object next = o;
        while (next != null) {
            if (type.isInstance(next)) {
                return (T) next;
            }
            if (next instanceof FacesWrapper<?>) {
                next = ((FacesWrapper<?>) next).getWrapped();
            } else {
                next = unwrapSpecial(type, o);
            }
        }
        return null;
    }

    static Object unwrapSpecial(Class<?> type, Object o) {
        for (Unwrapper u : UNWRAPPERS) {
            if (u.handles(o)) {
                return u.unwrap(o);
            }
        }
        return null;
    }

}
