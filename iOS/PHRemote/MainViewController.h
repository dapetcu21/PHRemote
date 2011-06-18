//
//  MainViewController.h
//  PHRemote
//
//  Created by Marius Petcu on 6/17/11.
//  Copyright 2011 Home. All rights reserved.
//

#include "URClient.h"

@class FlipsideViewController;

@interface MainViewController : UIViewController <UIAccelerometerDelegate> {
    URClient remote;
    NSString * host, * port;
    IBOutlet UILabel * status;
    BOOL sendAcc,sendTouch;
}


- (void)start;
- (void)stop;

@property(nonatomic,retain) NSString * host;
@property(nonatomic,retain) NSString * port;
@property(nonatomic,assign) BOOL sendAcc;
@property(nonatomic,assign) BOOL sendTouch;

- (void)flipsideViewControllerDidFinish:(FlipsideViewController *)controller;
- (void)sendTouchPack:(int)phase withTouch:(UITouch*)touch inView:(UIView*)view;

@end
