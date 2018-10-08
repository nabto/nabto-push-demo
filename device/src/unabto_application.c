/**
 *  uNabto application logic implementation
 */
#include "demo_application.h"
#include "unabto/unabto_app.h"
#include "unabto/unabto_util.h"
#include "unabto/unabto_protocol_defines.h"
#include <modules/push_service/push_service.h>
#include <stdio.h>

#define PERSISTANCE_FILENAME "persistence.bin"
#define MAX_DEVICE_NAME_LENGTH 50
#define MAX_SUBSCRIBERS 10
#define MAX_DYNAMIC_DATA_LENGTH 256
enum fp_acl_response_status {
    PUSH_STATUS_OK = 0,
    PUSH_STATUS_FAILED = 1,
    PUSH_STATUS_USER_DB_FULL = 2,
    PUSH_STATUS_REMOVE_FAILED = 4
};
struct pushSubscriber subs_[MAX_SUBSCRIBERS];
int numSubs_ = 0;
uint16_t nextId_ = 1;
uint32_t CNT = 0;

int testContext = 394;
uint8_t dynData[MAX_DYNAMIC_DATA_LENGTH];

/* Callback function called when the core is done handling a push notification.
 * @param ptr   void pointer to some context passed to the core
 * @param hint  Hint to the success or failure of the push notification.
 */
void callback(void* ptr, const unabto_push_hint* hint){
    switch(*hint){
        case UNABTO_PUSH_HINT_OK:
            // The Push notification was handled successfully by the basestation
            NABTO_LOG_INFO(("Callback with hint: OK"));
            break;
        case UNABTO_PUSH_HINT_QUEUE_FULL:
            // The uNabto core queue has no room for more push notifications
            NABTO_LOG_INFO(("Callback with hint: QUEUE FULL"));
            break;
        case UNABTO_PUSH_HINT_INVALID_DATA_PROVIDED:
            // The uNabto core cannot handle the data provided, probably too large for its communication buffer
            NABTO_LOG_INFO(("Callback with hint: INVALID DATA PROVIDED"));
            break;
        case UNABTO_PUSH_HINT_NO_CRYPTO_CONTEXT:
            // uNabto core cannot send the notification as no cryptographic context exists, are you attached?
            NABTO_LOG_INFO(("Callback with hint: NO CRYPTO CONTEXT"));
            break;
        case UNABTO_PUSH_HINT_ENCRYPTION_FAILED:
            // uNabto core failed to encrypt the push notification
            NABTO_LOG_INFO(("Callback with hint: ENCRYPTION FAILED"));
            break;
        case UNABTO_PUSH_HINT_FAILED:
            // The Basestation received the notification but failed to handle it
            NABTO_LOG_INFO(("Callback with hint: FAILED"));
            break;
        case UNABTO_PUSH_HINT_QUOTA_EXCEEDED:
            // The basestation received the notification but retured quota exceeded, this causes the core to enforce a backoff period where no notifications will be send. This can be used to handle overloaded servers or faulty devices sending notifications at high rates.
            NABTO_LOG_INFO(("Callback with hint: QUOTA EXCEEDED"));
            break;
        case UNABTO_PUSH_HINT_QUOTA_EXCEEDED_REATTACH:
            // The basestation received the notification but return quota exceeded reattach. The uNabto core will not send notifications to the basestation before the device has been reattached. This can be used in extreme cases of faulty devices sending notifications at very high rates.
            NABTO_LOG_INFO(("Callback with hint: QUOTA EXCEEDED REATTACH"));
            break;
        default:
            NABTO_LOG_INFO(("Callback with unknown hint"));
    }
    // The provided context can be used to take notification specific actions
    NABTO_LOG_INFO(("Got the context value: %i",*(int*)ptr));
}

/* Initialization function called by the main function at startup*/
void demo_init() {
    // Loading persistence file if present to remember subscribed clients even after device restart.
    FILE* subFile = fopen(PERSISTANCE_FILENAME, "rb+");
    if (subFile == NULL) {
        // No file, initializing without known devices
        return;
    }
    numSubs_ = fread(subs_, sizeof(struct pushSubscriber), MAX_SUBSCRIBERS, subFile);
    fclose(subFile);
        
}

/* help function used to copy a string from a buffer */
static bool read_string_null_terminated(unabto_query_request* read_buffer, char* out, size_t outlen)
{
    uint8_t* list;
    uint16_t length;
    if (!unabto_query_read_uint8_list(read_buffer, &list, &length)) {
        return false;
    }

    memset(out, 0, outlen);
    
    memcpy(out, list, MIN(length, outlen-1));
    return true;
}

/* Help function used to update the persistance file when ever a client (un-)subscribes to push notifications */
void updatePersistanceFile(void){
    FILE* subFile = fopen(PERSISTANCE_FILENAME, "w+");
    if (subFile == NULL) {
        // Error Opening file
        NABTO_LOG_ERROR(("Unable to open persistance file for writing"));
        return;
    }
    int nmemb = fwrite(subs_, sizeof(struct pushSubscriber), numSubs_, subFile);
    if (nmemb != numSubs_){
        // Error writing to file
        NABTO_LOG_ERROR(("Error while writing to persistance file"));
        return;
    }
    fclose(subFile);

}

/* Function used to send push notifications, called from the main function */
void sendPN(void){
    int i;
    NABTO_LOG_INFO(("Sending Push notifications"));
    if(numSubs_ == 0){
        NABTO_LOG_INFO(("No clients subscribed"));
        return;
    }
    // For loop constructing a push notification for each subscribed clients
    for(i = 0; i<numSubs_; i++){
        push_message pm;
        char str[10];
        // Initialize the push notification with the pnsid and static data provided by the client device on subscription
        if(!init_push_message(&pm, subs_[i].pnsid,subs_[i].staticData)){
            NABTO_LOG_ERROR(("init_push_message failed"));
            return;
        }
        // Adding a title localization key to to the notification
        // Using the title localization key instead of the title allows languages to be handled at the client
        // Adding the key "title_1", this key must exists in the client app.
        if(!add_title_loc_key(&pm, "title_1")){
            NABTO_LOG_ERROR(("add_title_loc_key failed"));
            return;
        }
        // Adding a body localization key to the notification similar to the title localization key.
        // The "body_1" localization key must exist in the client app.
        if(!add_body_loc_key(&pm, "body_1")){
            NABTO_LOG_ERROR(("add_body_loc_key failed"));
            return;
        }
        // in this example, the body localization key requires an argument which is added here.
        // this function can be called several times if multiple arguments are needed.
        // for now only string arguments are supported.
        CNT++;
        sprintf(str, "%d", CNT);
        if(!add_body_loc_string_arg(&pm, str)){
            NABTO_LOG_ERROR(("add_body_loc_string_arg failed"));
            return ;
        }
        // The constructed push notification is sent to the push_service module
        // Additionally, the callback function to be called when the core has finished handling the notification is passed.
        // The context for the callback function is passed as a void pointer, for this example it is simply an integer.
        send_push_message(&pm,&callback, (void*)&testContext);
    }
}

application_event_result application_event(application_request* request,
                                           unabto_query_request* query_request,
                                           unabto_query_response* query_response) {

    NABTO_LOG_INFO(("Nabto application_event: %u", request->queryId));

    // handle requests as defined in interface definition shared with
    // client - for the default demo, see
    // https://github.com/nabto/nabto-push-demo/blob/master/NabtoPushFirebaseDemo/app/src/main/res/raw/queries.xml
    application_event_result res;
    uint16_t id;

    switch (request->queryId) {
    case 20000: 
        // push_subscribe_cert_fp.json
        // check if device is know, if so update the static information and PNS ID
        for(int i = 0; i < numSubs_; i++){
            if(memcmp(request->connection->fingerprint.value.data,subs_[i].fingerprint,NP_TRUNCATED_SHA256_LENGTH_BYTES)==0){
                // Client known, updating static data
                NABTO_LOG_INFO(("updating static data for know device"));
                if (!read_string_null_terminated(query_request, subs_[i].staticData, STATIC_DATA_BUFFER_LENGTH )){
                    return AER_REQ_TOO_SMALL;
                }
                if (!unabto_query_read_uint16(query_request, &subs_[i].pnsid)){
                    return AER_REQ_TOO_SMALL;
                }
                subs_[i].id = nextId_;
                if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
                nextId_++;
                updatePersistanceFile();
                return AER_REQ_RESPONSE_READY;
            }
        }
        // If the device is not found, its fingerprint is stored along with the static data and PNS ID 
        NABTO_LOG_INFO(("New device found adding to subs_"));
        memcpy(subs_[numSubs_].fingerprint, request->connection->fingerprint.value.data,NP_TRUNCATED_SHA256_LENGTH_BYTES);
        if (!read_string_null_terminated(query_request, subs_[numSubs_].staticData, STATIC_DATA_BUFFER_LENGTH )){
            return AER_REQ_TOO_SMALL;
        }
        if (!unabto_query_read_uint16(query_request, &subs_[numSubs_].pnsid)){
            return AER_REQ_TOO_SMALL;
        }
        subs_[numSubs_].id = nextId_;
        // fingerprint based subscription does not return ID
        if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
        numSubs_++;
        nextId_++;
        NABTO_LOG_INFO(("numSubs_ is now %i, Just added client with static data: %s",numSubs_,subs_[numSubs_-1].staticData));
        // The persistance file is updated with the new subscription information
        updatePersistanceFile();
        return AER_REQ_RESPONSE_READY;
        
    case 20010: 
        // push_subscribe_id.json
        // check if device is know, if so update the static information and PNS ID
        for(int i = 0; i < numSubs_; i++){
            if(memcmp(request->connection->fingerprint.value.data,subs_[i].fingerprint,NP_TRUNCATED_SHA256_LENGTH_BYTES)==0){
                // Client known, updating static data
                NABTO_LOG_INFO(("updating static data for know device"));
                if (!read_string_null_terminated(query_request, subs_[i].staticData, STATIC_DATA_BUFFER_LENGTH )){
                    return AER_REQ_TOO_SMALL;
                }
                if (!unabto_query_read_uint16(query_request, &subs_[i].pnsid)){
                    return AER_REQ_TOO_SMALL;
                }
                subs_[i].id = nextId_;
                if (!unabto_query_write_uint16(query_response, nextId_)) return AER_REQ_RSP_TOO_LARGE;
                if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
                nextId_++;
                updatePersistanceFile();
                return AER_REQ_RESPONSE_READY;
            }
        }
        // If the device is not found, its fingerprint is stored along with the static data and PNS ID 
        NABTO_LOG_INFO(("New device found adding to subs_"));
        memcpy(subs_[numSubs_].fingerprint, request->connection->fingerprint.value.data,NP_TRUNCATED_SHA256_LENGTH_BYTES);
        if (!read_string_null_terminated(query_request, subs_[numSubs_].staticData, STATIC_DATA_BUFFER_LENGTH )){
            return AER_REQ_TOO_SMALL;
        }
        if (!unabto_query_read_uint16(query_request, &subs_[numSubs_].pnsid)){
            return AER_REQ_TOO_SMALL;
        }
        subs_[numSubs_].id = nextId_;
        // return the ID to be used of unsubscribe request
        if (!unabto_query_write_uint16(query_response, nextId_)) return AER_REQ_RSP_TOO_LARGE;
        if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
        numSubs_++;
        nextId_++;
        NABTO_LOG_INFO(("numSubs_ is now %i, Just added client with static data: %s",numSubs_,subs_[numSubs_-1].staticData));
        // The persistance file is updated with the new subscription information
        updatePersistanceFile();
        return AER_REQ_RESPONSE_READY;

    case 20020:
        // push_unsubscribe_cert_fp.json
        // Find the device based on fingerprint, remove it from the subscription list, and update the persistance file
        for(int i = 0; i < numSubs_; i++){
            if(memcmp(request->connection->fingerprint.value.data,subs_[i].fingerprint,NP_TRUNCATED_SHA256_LENGTH_BYTES)==0){
                // Client known, updating static data
                memmove(&subs_[i],&subs_[i+1],sizeof(struct pushSubscriber)*(numSubs_-i-1));
                numSubs_--;
                if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
                updatePersistanceFile();
                return AER_REQ_RESPONSE_READY;
            }
        }
        // if device cannot be found return an error
        if (!unabto_query_write_uint8(query_response, PUSH_STATUS_REMOVE_FAILED)) return AER_REQ_RSP_TOO_LARGE;
        updatePersistanceFile();
        return AER_REQ_RESPONSE_READY;
    case 20030:
        // push_unsubscribe_id.json
        // Find the device based on id, remove it from the subscription list, and update the persistance file
        if (!unabto_query_read_uint16(query_request, &id)){
            return AER_REQ_TOO_SMALL;
        }
               
        for(int i = 0; i < numSubs_; i++){
            if(subs_[i].id == id){
                // Client known, updating static data
                memmove(&subs_[i],&subs_[i+1],sizeof(struct pushSubscriber)*(numSubs_-i-1));
                numSubs_--;
                if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
                updatePersistanceFile();
                return AER_REQ_RESPONSE_READY;
            }
        }
        // if device cannot be found return an error
        if (!unabto_query_write_uint8(query_response, PUSH_STATUS_REMOVE_FAILED)) return AER_REQ_RSP_TOO_LARGE;
        updatePersistanceFile();
        return AER_REQ_RESPONSE_READY;
    case 20040:
        // is_subscribed_cert_fp.json
        // Determine if the device exists in the subscription list based on fingerprint
        for(int i = 0; i < numSubs_; i++){
            if(memcmp(request->connection->fingerprint.value.data, subs_[i].fingerprint, NP_TRUNCATED_SHA256_LENGTH_BYTES)==0){
                // Client subscribed, returning OK
                if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
                return AER_REQ_RESPONSE_READY;
            }
        }
        // Client not subscribed returning FAILED
        if (!unabto_query_write_uint8(query_response, PUSH_STATUS_FAILED)) return AER_REQ_RSP_TOO_LARGE;
        return AER_REQ_RESPONSE_READY;
    case 20050:
        // is_subscribed_id.json
        // Determine if the device exists in the subscription list based on id
        if (!unabto_query_read_uint16(query_request, &id)){
            return AER_REQ_TOO_SMALL;
        }
        for(int i = 0; i < numSubs_; i++){
            if(subs_[i].id == id){
                // Client subscribed, returning OK
                if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
                return AER_REQ_RESPONSE_READY;
            }
        }
        // Client not subscribed returning FAILED
        if (!unabto_query_write_uint8(query_response, PUSH_STATUS_FAILED)) return AER_REQ_RSP_TOO_LARGE;
        return AER_REQ_RESPONSE_READY;
        
        
    default:
        NABTO_LOG_WARN(("Unhandled query id: %u", request->queryId));
        return AER_REQ_INV_QUERY_ID;
    }
}
