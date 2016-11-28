//
//  CDVScan.m
//  cordova-plugin-scan
//
//  Created by ZhangJian on 16/8/3.
//  Copyright © 2016年 zhangjian. All rights reserved.
//

#import "CDVScan.h"

#import <Cordova/CDVViewController.h>
#import "CDVScan.h"
#import "CDVScanViewController.h"
@interface CDVScan () {
    CDVInvokedUrlCommand *_command;
}
@end

@implementation CDVScan
-(void)pluginInitialize{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleScan:) name:@"scan" object:nil];
}

-(void)handleScan:(NSNotification*)ns{
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:ns.userInfo[@"content"]];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:_command.callbackId];
}


- (void)recognize:(CDVInvokedUrlCommand*)command{
    _command = command;
    UINavigationController *navController =[[UINavigationController alloc] initWithRootViewController:[[CDVScanViewController alloc] init]];
    [navController.navigationBar setBarTintColor:[UIColor colorWithRed:43/255.f green:173/255.f blue:222/255.f alpha:1]];
    [navController.navigationBar setTitleTextAttributes:
     @{NSForegroundColorAttributeName:[UIColor whiteColor]}];
    [navController.navigationBar setTintColor:[UIColor whiteColor]];
    [navController.navigationBar setTranslucent:NO];
    [[[[[UIApplication sharedApplication] delegate] window] rootViewController] presentViewController:navController animated:YES completion:NULL];
}

- (void) dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}
@end

