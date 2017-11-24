#import <Cordova/CDVPlugin.h>

@interface AVSLogin : CDVPlugin {
}

- (void)authorize:(CDVInvokedUrlCommand *)command;
- (void)getToken:(CDVInvokedUrlCommand *)command;
- (void)signOut:(CDVInvokedUrlCommand *)command;

@end