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

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.context.FacesContextWrapper;
import javax.faces.lifecycle.Lifecycle;

/**
 * Stubbornly reports getResponseComplete() as false
 * while a deferred redirect is pending.
 */
public class DeferredRedirectFacesContext extends FacesContextWrapper {
    public static class Factory extends FacesContextFactory {
        private final FacesContextFactory wrapped;

        public Factory(FacesContextFactory wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public FacesContextFactory getWrapped() {
            return wrapped;
        }

        @Override
        public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle)
                throws FacesException {
            return new DeferredRedirectFacesContext(getWrapped().getFacesContext(context, request, response,
                    lifecycle));
        }
    }

    private final FacesContext wrapped;

    public DeferredRedirectFacesContext(FacesContext wrapped) {
        this.wrapped = wrapped;
        setCurrentInstance(this);
    }

    @Override
    public FacesContext getWrapped() {
        return wrapped;
    }

    @Override
    public boolean getResponseComplete() {
        return !FacesWrapperUtils.unwrap(DeferredRedirectExternalContext.class,
                getExternalContext()).isRedirectPending() && super.getResponseComplete();
    }

}
