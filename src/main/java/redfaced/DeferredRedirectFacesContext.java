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
