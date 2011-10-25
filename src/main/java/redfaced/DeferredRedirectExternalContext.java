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
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextFactory;
import javax.faces.context.ExternalContextWrapper;
import javax.faces.context.FacesContext;

/**
 * Deferred redirect ExternalContext.
 */
public class DeferredRedirectExternalContext extends ExternalContextWrapper {
    /**
     * Attribute name.
     */
    public static final String DEFERRED_REDIRECT_URL_ATTRIBUTE = "redfaced.deferredRedirectUrl";

    /**
     * Deferred redirect ExternalContextFactory.
     */
    public static class Factory extends ExternalContextFactory {
        private final ExternalContextFactory wrapped;

        public Factory(ExternalContextFactory wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public ExternalContextFactory getWrapped() {
            return wrapped;
        }

        @Override
        public ExternalContext getExternalContext(Object context, Object request,
                                                  Object response) throws FacesException {
            return new DeferredRedirectExternalContext(wrapped.getExternalContext(context, request, response));
        }
    }

    private final ExternalContext wrapped;

    public DeferredRedirectExternalContext(ExternalContext wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExternalContext getWrapped() {
        return wrapped;
    }

    @Override
    public void redirect(String url) throws IOException {
        getRequestMap().put(DEFERRED_REDIRECT_URL_ATTRIBUTE, url);
        FacesContext.getCurrentInstance().renderResponse();
    }

    boolean isRedirectPending() {
        return getRequestMap().get(DEFERRED_REDIRECT_URL_ATTRIBUTE) != null;
    }

    /**
     * Perform deferred redirect if necessary.
     * @throws IOException
     */
    void performDeferredRedirect() throws IOException {
        String url = (String) getRequestMap().remove(DEFERRED_REDIRECT_URL_ATTRIBUTE);
        if (url != null) {
            getWrapped().redirect(url);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (!facesContext.getResponseComplete()) {
                facesContext.responseComplete();
            }
        }
    }
}
