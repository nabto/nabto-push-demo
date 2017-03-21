//
//  Copyright (c) 2016 Google Inc.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

#import "ViewController.h"
#import "NabtoClient/NabtoClient.h"

@import FirebaseInstanceID;
@import FirebaseMessaging;

@implementation ViewController
{
    NSMutableArray* devices_;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [devices_ count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *simpleTableIdentifier = @"SimpleTableItem";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:simpleTableIdentifier];
    }
    
    cell.textLabel.text = [devices_ objectAtIndex:indexPath.row];
    return cell;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  [[NabtoClient instance] nabtoStartup];
  [[NabtoClient instance] nabtoOpenSessionGuest];
  [self refreshDevices];
}

- (void)refreshDevices {
    if (!devices_) {
        devices_ = [[NSMutableArray alloc] init];
    }
    [devices_ removeAllObjects];
    NSArray* discovered = [[NabtoClient instance] nabtoGetLocalDevices];
    if ([discovered count] == 0) {
        NSLog(@"No devices found!");
    } else {
        for (NSString *name in discovered) {
            NSLog(@"Got local device [%@]", name);
            [devices_ addObject:name];
        }
    }
    [self.deviceTable reloadData];
}

- (IBAction)handleRefresh:(id)sender {
  // [START get_iid_token]
  NSString *token = [[FIRInstanceID instanceID] token];
  NSLog(@"InstanceID token: %@", token);
    [self refreshDevices];
    
  // [END get_iid_token]
}

- (IBAction)handleSubscribeTouch:(id)sender {
  // [START subscribe_topic]
  [[FIRMessaging messaging] subscribeToTopic:@"/topics/news"];
  NSLog(@"Subscribed to news topic");
  // [END subscribe_topic]
}

@end
