/* Copyright (c) Marius Petcu, Porkholt Labs!. All rights reserved. */

#import <UIKit/UIKit.h>

@class MainViewController;

@interface PHRemoteAppDelegate : NSObject <UIApplicationDelegate> {

}

@property (nonatomic, retain) IBOutlet UIWindow *window;

@property (nonatomic, retain) IBOutlet MainViewController *mainViewController;

@end
