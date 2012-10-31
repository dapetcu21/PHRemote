/* Copyright (c) 2012 Marius Petcu, Porkholt Labs!. All rights reserved. */

#import "CaptureView.h"
#import "MainViewController.h"


@implementation CaptureView
@synthesize delegate;

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    for (UITouch* touch in touches)
        [delegate sendTouchPack:0 withTouch:touch inView:self];
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    for (UITouch* touch in touches)
        [delegate sendTouchPack:1 withTouch:touch inView:self];
}

- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event
{
    for (UITouch* touch in touches)
        [delegate sendTouchPack:2 withTouch:touch inView:self];
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event
{
    for (UITouch* touch in touches)
        [delegate sendTouchPack:3 withTouch:touch inView:self];
}

@end
