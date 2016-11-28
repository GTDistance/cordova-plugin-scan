//
//  CDVScan.h
//  cordova-plugin-scan
//
//  Created by ZhangJian on 16/8/3.
//  Copyright © 2016年 zhangjian. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

@interface CDVScan : CDVPlugin
{}
- (void)recognize:(CDVInvokedUrlCommand*)command;

@end
