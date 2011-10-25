/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
