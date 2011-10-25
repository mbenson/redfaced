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

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.faces.FacesException;
import javax.faces.FacesWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

/**
 * Deferred redirect Lifecycle.
 */
public class DeferredRedirectLifecycle extends Lifecycle implements FacesWrapper<Lifecycle> {
    public static class Factory extends LifecycleFactory {
        private final LifecycleFactory wrapped;
        private final ConcurrentMap<String, Lifecycle> lifecycles = new ConcurrentHashMap<String, Lifecycle>();

        public Factory(LifecycleFactory wrapped) {
            this.wrapped = wrapped;
        }


        @Override
        public LifecycleFactory getWrapped() {
            return wrapped;
        }

        @Override
        public Lifecycle getLifecycle(String lifecycleId) {
            Lifecycle result = lifecycles.get(lifecycleId);
            if (result == null) {
                result = new DeferredRedirectLifecycle(getWrapped().getLifecycle(lifecycleId));
                Lifecycle faster = lifecycles.putIfAbsent(lifecycleId, result);
                if (faster != null) {
                    return faster;
                }
            }
            return result;
        }

        @Override
        public void addLifecycle(String lifecycleId, Lifecycle lifecycle) {
            lifecycles.put(lifecycleId, new DeferredRedirectLifecycle(lifecycle));
        }

        @Override
        public Iterator<String> getLifecycleIds() {
            return lifecycles.keySet().iterator();
        }
    }

    private final Lifecycle wrapped;

    public DeferredRedirectLifecycle(Lifecycle wrapped) {
        this.wrapped = wrapped;
    }

    public Lifecycle getWrapped() {
        return wrapped;
    }

    @Override
    public void addPhaseListener(PhaseListener listener) {
        getWrapped().addPhaseListener(listener);
    }

    @Override
    public void execute(FacesContext context) throws FacesException {
        getWrapped().execute(context);
    }

    @Override
    public PhaseListener[] getPhaseListeners() {
        return getWrapped().getPhaseListeners();
    }

    @Override
    public void removePhaseListener(PhaseListener listener) {
        getWrapped().removePhaseListener(listener);
    }

    @Override
    public void render(FacesContext context) throws FacesException {
        DeferredRedirectExternalContext externalContext
                = FacesWrapperUtils.unwrap(DeferredRedirectExternalContext.class, context.getExternalContext());
        if (externalContext.isRedirectPending()) {
            try {
                externalContext.performDeferredRedirect();
            } catch (IOException e) {
                throw new FacesException(e);
            }
        }
        getWrapped().render(context);
    }
}
