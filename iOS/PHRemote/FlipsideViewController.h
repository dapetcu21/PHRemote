//
//  FlipsideViewController.h
//  PHRemote
//
//  Created by Marius Petcu on 6/17/11.
//  Copyright 2011 Home. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MainViewController;

@interface FlipsideViewController : UIViewController {
    IBOutlet UITextField * hostfield,*portfield;
    IBOutlet UISwitch * accswitch,*touchswitch;
    MainViewController * delegate;
}

@property (nonatomic, assign) MainViewController * delegate;

- (IBAction)done:(id)sender;
- (IBAction)settingsChanged:(id)sender;

@end


@protocol FlipsideViewControllerDelegate
- (void)flipsideViewControllerDidFinish:(FlipsideViewController *)controller;
@end
