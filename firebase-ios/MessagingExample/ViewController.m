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
@import FirebaseInstanceID;
@import FirebaseMessaging;

@implementation ViewController
{
    NSArray *tableData;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [tableData count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *simpleTableIdentifier = @"SimpleTableItem";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:simpleTableIdentifier];
    }
    
    cell.textLabel.text = [tableData objectAtIndex:indexPath.row];
    return cell;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  tableData = [NSArray arrayWithObjects:@"Egg Benedict", @"Mushroom Risotto", @"Full Breakfast", @"Hamburger", @"Ham and Egg Sandwich", @"Creme Brelee", @"White Chocolate Donut", @"Starbucks Coffee", @"Vegetable Curry", @"Instant Noodle with Egg", @"Noodle with BBQ Pork", @"Japanese Noodle with Pork", @"Green Tea", @"Thai Shrimp Cake", @"Angry Birds Cake", @"Ham and Cheese Panini", nil];
}

- (IBAction)handleLogTokenTouch:(id)sender {
  // [START get_iid_token]
  NSString *token = [[FIRInstanceID instanceID] token];
  NSLog(@"InstanceID token: %@", token);
  // [END get_iid_token]
}

- (IBAction)handleDiscover:(id)sender {
    // [START get_iid_token]
    NSString *token = [[FIRInstanceID instanceID] token];
    NSLog(@"InstanceID token: %@", token);
    // [END get_iid_token]
}


- (IBAction)handleSubscribeTouch:(id)sender {
  // [START subscribe_topic]
  [[FIRMessaging messaging] subscribeToTopic:@"/topics/news"];
  NSLog(@"Subscribed to news topic");
  // [END subscribe_topic]
}

@end
