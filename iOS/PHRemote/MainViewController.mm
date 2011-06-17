//
//  MainViewController.m
//  PHRemote
//
//  Created by Marius Petcu on 6/17/11.
//  Copyright 2011 Home. All rights reserved.
//

#import "FlipsideViewController.h"
#import "MainViewController.h"
#include <stdint.h>
@implementation MainViewController
@synthesize sendAcc;
@synthesize sendTouch;

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [[UIAccelerometer sharedAccelerometer] setDelegate:self];
    [super viewDidLoad];
    self.port = @"2221";
    self.host = @"255.255.255.255";
    self.sendAcc = YES;
    self.sendTouch = YES;
    [self start];
}

- (void)accelerometer:(UIAccelerometer *)accelerometer didAccelerate:(UIAcceleration *)acceleration
{
    if (!sendAcc) return;
    remote.beginPacket(0xAC);
    int64_t acc[3];
    acc[0] = (int64_t)([acceleration x]*1048576); //this is ugly
    acc[1] = (int64_t)([acceleration y]*1048576);
    acc[2] = (int64_t)([acceleration z]*1048576);
    URField field;
    field.setTag(0x01);
    field.setInt64s((uint64_t*)acc, 3);
    remote.addField(field);
    remote.endPacket(0);
}

- (void)start
{
    status.text = @"";
    try {remote.start();}
    catch (std::string ex) {status.text = [NSString stringWithUTF8String:ex.c_str()];};
}

- (void)stop
{
    status.text = @"";
    remote.stop();
}

- (void)setHost:(NSString *)h
{
    [h retain];
    [host release];
    host = h;
    try {remote.setHostname([host UTF8String]);}
    catch (std::string ex) {status.text = [NSString stringWithUTF8String:ex.c_str()];}
}

- (NSString*)host
{
    return host;
}

- (void)setPort:(NSString *)p
{
    [p retain];
    [port release];
    port = p;
    status.text = @"";
    try {remote.setPort([port UTF8String]);}
    catch (std::string ex) {status.text = [NSString stringWithUTF8String:ex.c_str()];}
}

- (NSString*)port
{
    return port;
}

- (void)flipsideViewControllerDidFinish:(FlipsideViewController *)controller
{
    [[UIApplication sharedApplication] setStatusBarHidden:YES];
    [self dismissModalViewControllerAnimated:YES];
    [self start];
}

- (IBAction)showInfo:(id)sender
{    
    FlipsideViewController *controller = [[FlipsideViewController alloc] initWithNibName:@"FlipsideView" bundle:nil];
    controller.delegate = self;
    
    controller.modalTransitionStyle = UIModalTransitionStyleFlipHorizontal;
    [[UIApplication sharedApplication] setStatusBarHidden:NO];
    [self presentModalViewController:controller animated:YES];
    [self stop];
    
    [controller release];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations.
    return (interfaceOrientation == UIInterfaceOrientationLandscapeRight);
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload
{
    [super viewDidUnload];

    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)dealloc
{
    [host release];
    [port release];
    [super dealloc];
}

@end
