package org.restcomm.connect.rvd.http.resources;

import org.apache.commons.lang.NotImplementedException;
import org.restcomm.connect.rvd.ApplicationContext;
import org.restcomm.connect.rvd.RvdConfiguration;
import org.restcomm.connect.rvd.exceptions.AuthorizationException;
import org.restcomm.connect.rvd.http.RestService;
import org.restcomm.connect.rvd.identity.AccountProvider;
import org.restcomm.connect.rvd.identity.UserIdentityContext;
import org.restcomm.connect.rvd.restcomm.RestcommAccountInfo;

/**
 * @author otsakir@gmail.com - Orestis Tsakiridis
 */
public class SecuredRestService extends RestService {
    private UserIdentityContext userIdentityContext;

    public enum SecureBehavior {
        AllowNonActive, //  secure() won't fail if an account is not in active state
        Default
    }

    public void init() {
        super.init();
        RvdConfiguration config = applicationContext.getConfiguration();
        AccountProvider accountProvider = applicationContext.getAccountProvider();
        // if it is secured by keycloak try to create a deployment too
        if (config.keycloakEnabled()) {
            throw new UnsupportedOperationException();
        }
        String authorizationHeader = request.getHeader("Authorization");
        userIdentityContext = new UserIdentityContext(authorizationHeader, accountProvider);
    }

    public SecuredRestService() {
    }

    // used for testing
    SecuredRestService(UserIdentityContext context) {
        this.userIdentityContext = context;
    }

    // used for testing
    public SecuredRestService(ApplicationContext applicationContext, UserIdentityContext userIdentityContext) {
        super(applicationContext);
        this.userIdentityContext = userIdentityContext;
    }

    public UserIdentityContext getUserIdentityContext() {
        return userIdentityContext;
    }

    /**
     * Makes sure the request is done by an authenticated user.
     */
    protected void secure() {
        secure(SecureBehavior.Default);
    }

    protected void secure(SecureBehavior policy) {
        RestcommAccountInfo account = userIdentityContext.getAccountInfo();
        if (account != null) {
            if (policy == SecureBehavior.AllowNonActive) {
                return;
            } else if (policy == SecureBehavior.Default){
                if ( "active".equals(account.getStatus())) {
                    return;
                }
            } else {
                throw new NotImplementedException("RVD secure() behavior not defined:  " + policy.toString());
            }
        }
        throw new AuthorizationException();
    }

    /**
     * Convenience function to quickly access logged username without going through getUserIdentityContext().get...
     * @return
     */
    protected String getLoggedUsername() {
        return userIdentityContext.getAccountUsername();
    }
}
