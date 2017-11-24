package com.avs;

import javax.net.ssl.ExtendedSSLSession;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.workflow.RequestContext;

public class AVSLogin extends CordovaPlugin {
    private static final String TAG = "AVSLogin";

    private static final String ACTION_AUTHORIZE = "authorize";
    private static final String ACTION_GET_TOKEN = "getToken";
    private static final String ACTION_SIGNOUT = "signOut";

    private static final String FIELD_ACCESS_TOKEN = "accessToken";
    private static final String FIELD_AUTHORIZATION_CODE = "authorizationCode";
    private static final String FIELD_USER = "user";
    private static final String FIELD_CLIENT_ID = "clientId";

    private static final String ARG_SCOPE = "scope";
    private static final String ARG_SCOPE_DATA = "scope_data";

    private static final String Default_Scope = "alexa:all";

    private RequestContext mRequestContext;
    private CallbackContext mCallbackContext;

    private String CurrScope;
    private JSONObject CurrScopeData;

    public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
        mRequestContext = RequestContext.create(cordova.getActivity());
        mRequestContext.registerListener(new AuthorizeListener() {

            @Override
            public void onSuccess(AuthorizeResult result) {
                Log.d(TAG, "Authorization was completed successfully.");
                /* Your app is now authorized for the requested scopes */
            }

            @Override
            public void onError(AuthError ae) {
                Log.e(TAG, "There was an error during the attempt to authorize the application.");
                /* Inform the user of the error */
                mCallbackContext.error("Trouble during the attempt to authorize the application");
            }

            @Override
            public void onCancel(AuthCancellation cancellation) {
                Log.d(TAG, "Authorization was cancelled before it could be completed. ");
                /* Reset the UI to a ready-to-login state */
                mCallbackContext.error("Authorization was cancelled before it could be completed.");
            }
        });
    }

    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Runnable TempRunnable;
        if (action.equals(ACTION_AUTHORIZE)) {
            JSONObject Params = args.getJSONObject(0);
            CurrScope = Params.optString(ARG_SCOPE, Default_Scope);
            CurrScopeData = Params.optJSONObject(ARG_SCOPE_DATA);

            if (CurrScope.equals(Default_Scope) && CurrScopeData == null) {
                callbackContext.error("no scope data!");
                return false;
            }
            TempRunnable = Authroize;
        } else if (action.equals(ACTION_GET_TOKEN)) {
            TempRunnable = GetToken;
        } else if (action.equals(ACTION_SIGNOUT)) {
            TempRunnable = SignOut;
        } else {
            callbackContext.error("Not supported actions" + action);
            return false;
        }

        mCallbackContext = callbackContext;
        cordova.getThreadPool().execute(TempRunnable);
        return true;
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        mRequestContext.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (CurrScope == null)
            CurrScope = Default_Scope;

        AuthorizationManager.getToken(cordova.getActivity(), new Scope[] { ScopeFactory.scopeNamed(CurrScope) },
            new Listener<AuthorizeResult, AuthError>() {
                @Override
                public void onSuccess(AuthorizeResult result) {
                    if (result.getAccessToken() != null) {
                        /* The user is signed in */
                        //fetchUserProfile();
                        Log.d(TAG, "getToken Success----->" + result.getAccessToken());
                        sendAuthorizeResult(result);
                    } else {
                        /* The user is not signed in */
                        Log.d(TAG, "getToken fail----->The user is not signed in");
                    }
                }

                @Override
                public void onError(AuthError ae) {
                    /* The user is not signed in */
                }
            });
    }

    private final Runnable Authroize = new Runnable() {

        @Override
        public void run() {
            AuthorizationManager.authorize(new AuthorizeRequest.Builder(mRequestContext)
                .addScope(ScopeFactory.scopeNamed(CurrScope, CurrScopeData))
                .forGrantType(AuthorizeRequest.GrantType.ACCESS_TOKEN).shouldReturnUserData(false)
                .build());
        }
    };

    private final Runnable GetToken = new Runnable() {

        @Override
        public void run() {

        }
    };

    private final Runnable SignOut = new Runnable() {

        @Override
        public void run() {
            AuthorizationManager.signOut(cordova.getActivity().getApplicationContext(),
                    new Listener<Void, AuthError>() {
                        @Override
                        public void onSuccess(Void response) {
                        }

                        @Override
                        public void onError(AuthError authError) {
                            Log.e(TAG, "Error clearing authorization state.", authError);
                        }
                    });
        }
    };

    private void sendAuthorizeResult(AuthorizeResult result) {
        if (mCallbackContext == null) {
            return;
        }
        JSONObject authResult = new JSONObject();
        try {
            authResult.put(FIELD_ACCESS_TOKEN, result.getAccessToken());
            mCallbackContext.success(authResult);
        } catch (Exception e) {
            mCallbackContext.error("Trouble obtaining Authorize Result, error: " + e.getMessage());
        }
    }
}
