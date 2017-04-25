//
// Original source by Google, updated by Nabto to work with Nabto Push.
//
// Copyright (c) 2016 Google Inc.
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

- (void)viewDidLoad {
    [super viewDidLoad];
    [self refreshDevices];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(receiveRefreshNotification:)
                                                 name:@"AppActivatedNotification"
                                               object:nil];
}

- (void) dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void) receiveRefreshNotification:(NSNotification *) notification {
    [self refreshDevices];
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

- (void) tableView: (UITableView *) tableView didSelectRowAtIndexPath: (NSIndexPath *) indexPath
{
    UITableViewCell* cell = [tableView cellForRowAtIndexPath:indexPath];
    NSString* device = cell.textLabel.text;
    NSLog(@"Tapped device [%@]", device);
    char* jsonResponse;
    
    NSString* url = [NSString stringWithFormat:@"nabto://%@/push_subscribe_id.json?staticData=%@&pnsid=1", device, [self createPushConfig]];
    nabto_status_t status = [[NabtoClient instance] nabtoRpcInvoke:url withResultBuffer:&jsonResponse];
    if (status == NABTO_OK) {
        [self showAlert:@"Device successfully subscribed to notifications! Now suspend app and issue notification (just tap enter in demo device app)"];
        NSLog(@"Subscribed ok via url [%@]:\n %s", url, jsonResponse);
    } else {
        if (status == NABTO_FAILED_WITH_JSON_MESSAGE) {
            NSLog(@"Subscription failed with json response: %s", jsonResponse);
        } else {
            NSLog(@"Subscription failed with status %d", status);
        }
    }
}

- (NSString*) createPushConfig {
    NSMutableDictionary *data = [[NSMutableDictionary alloc]init];
    NSMutableDictionary *notification = [[NSMutableDictionary alloc]init];
    
    NSString* token = [[FIRInstanceID instanceID] token];
    
    [data setValue:token forKey:@"to"];
    [notification setValue:@"default" forKey:@"sound"];
    [data setValue:notification forKey:@"notification"];
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:data options:0 error:nil];
    return [[NSString alloc] initWithData:jsonData
                                 encoding:NSUTF8StringEncoding];
}

- (void)showAlert:(NSString*)msg {
    UIAlertController* alert = [UIAlertController
                                alertControllerWithTitle:@"Notice"
                                message:msg
                                preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"OK"
                                                       style:UIAlertActionStyleDefault
                                                     handler:^(UIAlertAction *action){}];
    [alert addAction:okAction];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self presentViewController:alert animated:YES completion:nil];
    });
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
  [self refreshDevices];
}

@end
