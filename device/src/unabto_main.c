/**
 *  Implementation of main for uNabto SDK
 */
#include <sys/types.h>
#include <modules/cli/gopt/gopt.h> // http://www.purposeful.co.uk/software/gopt/
#include <modules/util/read_hex.h>
#include "unabto/unabto_env_base.h"
#include "unabto/unabto_common_main.h"
#include "unabto/unabto_logging.h"
#include "demo_application.h"
#include <stdio.h>
#include <unistd.h>

#define STDIN 0

struct configuration {
    const char *device_id;
    const char *pre_shared_key;
    const char *local_port_str;
    const char *device_name;
    const char *product_name;
    const char *icon_url;
};
    
void nabto_yield(int msec);
static void help(const char* errmsg, const char *progname);
bool parse_argv(int argc, char* argv[], struct configuration* config);

static void help(const char* errmsg, const char *progname)
{
    if (errmsg) {
        printf("ERROR: %s\n", errmsg);
    }
    printf("\nPush notification demo stub application to how to use push notifications.\n");
    printf("Obtain a device id and crypto key from www.appmyproduct.com\n\n");

    printf("Usage: %s -d <device id> -k <crypto key> [-N <demo display name>] [-P <product name>] [-I <icon url>] [-p <local/discovery port>]\n\n", progname);
}


int main(int argc, char* argv[])
{
    struct configuration config;
    memset(&config, 0, sizeof(struct configuration));

    if (!parse_argv(argc, argv, &config)) {
        help(0, argv[0]);
        return 1;
    }

    nabto_main_setup* nms = unabto_init_context();
    nms->id = strdup(config.device_id);

    nms->secureAttach = true;
    nms->secureData = true;
    nms->cryptoSuite = CRYPT_W_AES_CBC_HMAC_SHA256;

    if (!unabto_read_psk_from_hex(config.pre_shared_key, nms->presharedKey, 16)) {
        help("Invalid cryptographic key specified", argv[0]);
        return false;
    }
  
    if (config.local_port_str) {
        nms->localPort = atoi(config.local_port_str);
    }

    if (!unabto_init()) {
        NABTO_LOG_FATAL(("Failed at nabto_main_init"));
    }

    demo_init();

    NABTO_LOG_INFO(("Push demo stub [%s] running!", nms->id));
    fd_set fds;
    int maxfd = STDIN;
    struct timeval tv;
    char buf[5];
    while (true) {
        tv.tv_sec = 0;
        tv.tv_usec = 500000;
        unabto_tick();
        FD_ZERO(&fds);
        FD_SET(STDIN, &fds); 

        int retval = select(maxfd+1, &fds, NULL, NULL, &tv); 

        if (FD_ISSET(STDIN, &fds)){
            read(STDIN_FILENO, buf, 5);
            NABTO_LOG_INFO(("User triggered a push notification"));
            sendPN();
        }
    }

    unabto_close();
    return 0;
}

bool parse_argv(int argc, char* argv[], struct configuration* config) {
    const char x0s[] = "h?";     const char* x0l[] = { "help", 0 };
    const char x1s[] = "d";      const char* x1l[] = { "deviceid", 0 };
    const char x2s[] = "k";      const char* x2l[] = { "presharedkey", 0 };
    const char x3s[] = "p";      const char* x3l[] = { "localport", 0 };
    const char x4s[] = "N";      const char* x4l[] = { "devicename", 0 };
    const char x5s[] = "P";      const char* x5l[] = { "productname", 0 };
    const char x6s[] = "I";      const char* x6l[] = { "iconurl", 0 };

    const struct { int k; int f; const char *s; const char*const* l; } opts[] = {
        { 'h', 0,           x0s, x0l },
        { 'd', GOPT_ARG,    x1s, x1l },
        { 'k', GOPT_ARG,    x2s, x2l },
        { 'p', GOPT_ARG,    x3s, x3l },
        { 'N', GOPT_ARG,    x4s, x4l },
        { 'P', GOPT_ARG,    x5s, x5l },
        { 'I', GOPT_ARG,    x6s, x6l },
        { 0, 0, 0, 0 }
    };
    void *options = gopt_sort( & argc, (const char**)argv, opts);

    if( gopt( options, 'h')) {
        help(0, argv[0]);
        exit(0);
    }

    if (!gopt_arg(options, 'd', &config->device_id)) {
        return false;
    }

    if (!gopt_arg(options, 'k', &config->pre_shared_key)) {
        return false;
    }

    gopt_arg(options, 'p', &config->local_port_str);
    gopt_arg(options, 'N', &config->device_name);
    gopt_arg(options, 'P', &config->product_name);
    gopt_arg(options, 'I', &config->icon_url);

    return true;
}

