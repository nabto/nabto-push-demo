/**
 *  uNabto application logic implementation
 */
#include "demo_application.h"
#include "unabto/unabto_app.h"
#include <unabto/unabto_util.h>
#include <modules/stateful_push_service/stateful_push_service.h>
#include <stdio.h>

static int32_t heatpump_room_temperature_ = 19;
static int32_t heatpump_target_temperature_ = 23;

#define DEVICE_NAME_DEFAULT "Push stub"
#define PERSISTANCE_FILENAME "persistence.bin"
#define MAX_DEVICE_NAME_LENGTH 50
#define MAX_SUBSCRIBERS 10
enum fp_acl_response_status {
    PUSH_STATUS_OK = 0,
    PUSH_STATUS_USER_DB_FULL = 2,
    PUSH_STATUS_REMOVE_FAILED = 4
};
static char device_name_[MAX_DEVICE_NAME_LENGTH];
struct pushSubscriber subs_[MAX_SUBSCRIBERS];
int numSubs_ = 0;

int testContext = 394;
const uint8_t* msgData = "{\"temp\": 943}";

void callback(void* ptr, const unabto_push_hint* hint){
    int i;
    switch(*hint){
        case UNABTO_PUSH_HINT_OK:
            NABTO_LOG_INFO(("Callback with hint: OK"));
            break;
        case UNABTO_PUSH_HINT_QUEUE_FULL:
            NABTO_LOG_INFO(("Callback with hint: QUEUE FULL"));
            break;
        case UNABTO_PUSH_HINT_INVALID_DATA_PROVIDED:
            NABTO_LOG_INFO(("Callback with hint: INVALID DATA PROVIDED"));
            break;
        case UNABTO_PUSH_HINT_NO_CRYPTO_CONTEXT:
            NABTO_LOG_INFO(("Callback with hint: NO CRYPTO CONTEXT"));
            break;
        case UNABTO_PUSH_HINT_ENCRYPTION_FAILED:
            NABTO_LOG_INFO(("Callback with hint: ENCRYPTION FAILED"));
            break;
        case UNABTO_PUSH_HINT_FAILED:
            NABTO_LOG_INFO(("Callback with hint: FAILED"));
            break;
        case UNABTO_PUSH_HINT_QUOTA_EXCEEDED:
            NABTO_LOG_INFO(("Callback with hint: QUOTA EXCEEDED"));
            break;
        case UNABTO_PUSH_HINT_QUOTA_EXCEEDED_REATTACH:
            NABTO_LOG_INFO(("Callback with hint: QUOTA EXCEEDED REATTACH"));
            break;
        default:
            NABTO_LOG_INFO(("Callback with unknown hint"));
    }
    NABTO_LOG_INFO(("Got the context value: %i",*(int*)ptr));
}
void demo_init() {
    snprintf(device_name_, sizeof(device_name_), DEVICE_NAME_DEFAULT);
    FILE* subFile = fopen(PERSISTANCE_FILENAME, "rb+");
    if (subFile == NULL) {
        
    }
        
}

void demo_application_set_device_name(const char* name) {
    strncpy(device_name_, name, MAX_DEVICE_NAME_LENGTH);
}

void demo_application_tick() {
#ifndef WIN32
    static time_t time_last_update_ = 0;
    time_t now = time(0);
    if (now - time_last_update_ > 2) {
        if (heatpump_room_temperature_ < heatpump_target_temperature_) {
            heatpump_room_temperature_++;
        } else if (heatpump_room_temperature_ > heatpump_target_temperature_) {
            heatpump_room_temperature_--;
        }
        time_last_update_ = now;
    }
#else
    size_t ticks_ = 0;
    heatpump_room_temperature_ = heatpump_target_temperature_ + ticks++ % 2;
#endif
}

int copy_buffer(unabto_query_request* read_buffer, uint8_t* dest, uint16_t bufSize, uint16_t* len) {
    uint8_t* buffer;
    if (!(unabto_query_read_uint8_list(read_buffer, &buffer, len))) {
        return AER_REQ_TOO_SMALL;
    }
    if (*len > bufSize) {
        return AER_REQ_TOO_LARGE;
    }
    memcpy(dest, buffer, *len);
    return AER_REQ_RESPONSE_READY;
}

int copy_string(unabto_query_request* read_buffer, char* dest, uint16_t destSize) {
    uint16_t len;
    int res = copy_buffer(read_buffer, (uint8_t*)dest, destSize-1, &len);
    if (res != AER_REQ_RESPONSE_READY) {
        return res;
    }
    dest[len] = 0;
    return AER_REQ_RESPONSE_READY;
}

int write_string(unabto_query_response* write_buffer, const char* string) {
    return unabto_query_write_uint8_list(write_buffer, (uint8_t *)string, strlen(string));
}

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

void sendPN(void){
    push_payload_data msg;
    push_payload_data staticData;
    NABTO_LOG_INFO(("Sending Push notifications"));
    msg.data = msgData;
    msg.len = strlen(msgData);
    msg.purpose = 2;
    msg.encoding = 1;
    staticData.purpose = 1;
    staticData.encoding = 1;

    for(int i = 0; i<numSubs_; i++){
        staticData.data = subs_[i].staticData;
        staticData.len = strlen(subs_[i].staticData);
        NABTO_LOG_INFO(("sending: staticData.len %i, staticData.data: %s, msg.len: %i, msg.data: %s",staticData.len,staticData.data, msg.len, msg.data));
        send_push_notification(1,staticData,msg,&callback, (void*)&testContext);
    }
}

application_event_result application_event(application_request* request,
                                           unabto_query_request* query_request,
                                           unabto_query_response* query_response) {

    NABTO_LOG_INFO(("Nabto application_event: %u", request->queryId));

    // handle requests as defined in interface definition shared with
    // client - for the default demo, see
    // https://github.com/nabto/ionic-starter-nabto/blob/master/www/nabto/unabto_queries.xml

    application_event_result res;
    
    switch (request->queryId) {
    case 20000: 
        // push_subscribe.json
        for(int i = 0; i < numSubs_; i++){
            if(memcmp(request->connection->fingerprint,subs_[i].fingerprint,NP_TRUNCATED_SHA256_LENGTH_BYTES)==0){
                // Client known, updating static data
                NABTO_LOG_INFO(("updating static data for know device"));
                if (!read_string_null_terminated(query_request, subs_[i].staticData, STATIC_DATA_BUFFER_LENGTH )){
                    return AER_REQ_TOO_SMALL;
                }
                if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
                return AER_REQ_RESPONSE_READY;
            }
        }
        NABTO_LOG_INFO(("New device found adding to subs_"));
        memcpy(subs_[numSubs_].fingerprint, request->connection->fingerprint,NP_TRUNCATED_SHA256_LENGTH_BYTES);
        if (!read_string_null_terminated(query_request, subs_[numSubs_].staticData, STATIC_DATA_BUFFER_LENGTH )){
            return AER_REQ_TOO_SMALL;
        }
        if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
        numSubs_++;
        NABTO_LOG_INFO(("numSubs_ is now %i, Just added client with static data: %s",numSubs_,subs_[numSubs_-1].staticData));
        return AER_REQ_RESPONSE_READY;

        case 20010:
            // push_unsubscribe.json
        for(int i = 0; i < numSubs_; i++){
            if(memcmp(request->connection->fingerprint,subs_[i].fingerprint,NP_TRUNCATED_SHA256_LENGTH_BYTES)==0){
                // Client known, updating static data
                memmove(&subs_[i],&subs_[i+1],sizeof(struct pushSubscriber)*(numSubs_-i-1));
                numSubs_--;
                if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
                return AER_REQ_RESPONSE_READY;
            }
        }
        if (!unabto_query_write_uint8(query_response, PUSH_STATUS_REMOVE_FAILED)) return AER_REQ_RSP_TOO_LARGE;
        return AER_REQ_RESPONSE_READY;
    case 20020:
        // trigger.json
        sendPN();
        if (!unabto_query_write_uint8(query_response, PUSH_STATUS_OK)) return AER_REQ_RSP_TOO_LARGE;
        return AER_REQ_RESPONSE_READY;
        
    default:
        NABTO_LOG_WARN(("Unhandled query id: %u", request->queryId));
        return AER_REQ_INV_QUERY_ID;
    }
}
