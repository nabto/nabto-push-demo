#ifndef _DEMO_APPLICATION_H
#define _DEMO_APPLICATION_H

#define STATIC_DATA_BUFFER_LENGTH 1000
#include <stdint.h>
#include <unabto/unabto_protocol_defines.h>

struct pushSubscriber {
    char staticData[STATIC_DATA_BUFFER_LENGTH];
    uint8_t fingerprint[NP_TRUNCATED_SHA256_LENGTH_BYTES];
    uint16_t pnsid;
};

void sendPN();
void demo_application_tick();
void demo_init();

#endif
