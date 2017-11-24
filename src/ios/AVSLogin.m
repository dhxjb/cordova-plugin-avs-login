#import "AVSLogin.h"
#import "AppDelegate.h"

#import <Cordova/CDVAvailability.h>
#import <LoginWithAmazon/LoginWithAmazon.h>

@implementation AppDelegate (AVSLogin)




- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)
            url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {

    NSLog(@"AVSLogin Plugin handle openURL");
    return [AMZNAuthorizationManager handleOpenURL:url
                                 sourceApplication:options[UIApplicationOpenURLOptionsSourceApplicationKey]];

}

@end

@implementation AVSLogin

- (void)pluginInitialize {
    NSLog(@"AVSLogin Plugin init");


    NSNotificationCenter* defaultCenter = [NSNotificationCenter defaultCenter];

    [defaultCenter addObserver:self selector:@selector(onAppDidBecomeActive:) name:UIApplicationDidBecomeActiveNotification object:nil];
    [defaultCenter addObserver:self selector:@selector(onAppDidFinishLaunching:) name:UIApplicationDidFinishLaunchingNotification object:nil];
    [defaultCenter addObserver:self selector:@selector(onHandleOpenURL:) name:CDVPluginHandleOpenURLNotification object:nil];
}

- (void)onHandleOpenURL: (NSNotification*) notification
{
    NSURL* url = (NSURL*)[notification object];
    NSLog(@"%@ %@",@"AVSLogin onHandleOpenURL", url.absoluteString);

    [AMZNAuthorizationManager handleOpenURL:url
                                 sourceApplication:@"com.avs"];


//    [self setFacebookApplication:self.app withURL:url sourceApplication:@"com.apple.mobilesafari" annotation:nil];
}



- (void)onAppDidFinishLaunching: (NSNotification*) notification
{

    //NSLog(@"%@",@"AVSLogin onAppDidFinishLaunching");
}


- (void)onAppDidBecomeActive:(NSNotification*)notification
{
    //NSLog(@"%@",@"AVSLogin applicationDidBecomeActive");
}

- (void)authorize:(CDVInvokedUrlCommand *)command {
        NSLog(@"authorize request started");
        // Build an authorize request.
        NSDictionary *options = command.arguments[0];
        NSString *scope = [options valueForKey: @"scope"];
        NSDictionary *scopeData = [options valueForKey: @"scope_data"];

        id<AMZNScope> alexaAllScope = [AMZNScopeFactory scopeWithName:scope data:scopeData];

        AMZNAuthorizeRequest *request = [[AMZNAuthorizeRequest alloc] init];
        request.scopes = @[alexaAllScope];
        request.grantType = AMZNAuthorizationGrantTypeToken;

        //Make an Authorize call to the LWA SDK.
        AMZNAuthorizationManager *authManager = [AMZNAuthorizationManager sharedManager];
        [authManager authorize:request withHandler:^(AMZNAuthorizeResult *result, BOOL userDidCancel, NSError *error) {
            if (error) {
                // Notify the user that authorization failed
                [[[UIAlertView alloc] initWithTitle:@"" message:[NSString stringWithFormat:@"User authorization failed due to an error: %@", error.localizedDescription] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
            } else if (userDidCancel) {
                // Notify the user that the authorization was cancelled
                [[[UIAlertView alloc] initWithTitle:@"" message:@"Authorization was cancelled prior to completion. To continue, you will need to try logging in again." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
            } else {
                // Fetch the access token and return to controller
                self.token = result.token;
                NSDictionary *dictionary = @{@"accessToken": result.token};
                CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dictionary];

                // The sendPluginResult method is thread-safe.
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
        }];
}

- (void)getToken:(CDVInvokedUrlCommand *)command {
  NSLog(@"getToken");
}

- (void)signOut:(CDVInvokedUrlCommand *)command {
  NSLog(@"signOut");
    [[AMZNAuthorizationManager sharedManager] signOut:^(NSError * _Nullable error) {
        if (!error) {
            // error from the SDK or Login with Amazon authorization server.
            NSString* payload = error.userInfo[@"AMZNLWAErrorNonLocalizedDescription"];

            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:payload];

            // The sendPluginResult method is thread-safe.
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}


@end