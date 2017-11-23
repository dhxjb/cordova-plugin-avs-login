
var Amazon = require('login1.js');
var AccessedToken;

module.exports = {
    authorize: function(options, success, failure)
    {
        console.log('authorize: ' + JSON.stringify(options));
        if (! options.client_id)
        {
            console.log('the client id can\'t be null');
            if(failure) failure('parameter wrong!');
        }

        window.amazon.Login.setClientId(options.client_id)
        window.amazon.Login.authorize(options, (response) =>
        {
            if (response.error)
            {
                console.log('[Implicit Grant] oauth error: ' + response.error);
                if (failure) failure(response.error);
                return;
            }
            AccessedToken = response.AccessedToken;

            if (success)
                success(AccessedToken)
        });
    },
    getToken: function(success, failure)
    {
        if (success && AccessedToken)
            success(AccessedToken);
        else if (failure)
            failure();
    },
    signOut: function(success, failure)
    {
        window.amazon.Login.logout();

        if (success)
            success();
    },
};
