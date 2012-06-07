/* Copyright (c) Marius Petcu, Porkholt Labs!. All rights reserved. */

#import <UIKit/UIKit.h>

@class MainViewController;
@interface CaptureView : UIView {
    MainViewController * delegate;
}

@property(nonatomic,assign) IBOutlet MainViewController * delegate;

@end
