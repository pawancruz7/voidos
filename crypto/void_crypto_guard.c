#include <linux/random.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <utils/Log.h>

#undef LOG_TAG
#define LOG_TAG "VoidOS_CryptoGuard"

#define CRITICAL_KEY_SIZE_BYTES 64

int __attribute__((optimize("O2"))) inject_hardware_entropy(void* targetKeyBuffer, size_t bufferSize) {
    if (bufferSize > CRITICAL_KEY_SIZE_BYTES || targetKeyBuffer == NULL) {
        ALOGE("SECURITY FAULT: Invalid buffer size or null target pointer.");
        return -1;
    }

    int urandom_fd = open("/dev/urandom", O_RDONLY);
    if (urandom_fd < 0) {
        ALOGE("FATAL INTERCEPT: Hardware random source unreadable.");
        return -2;
    }

    unsigned char entropyToken[CRITICAL_KEY_SIZE_BYTES];
    if (read(urandom_fd, entropyToken, bufferSize) != (ssize_t)bufferSize) {
        ALOGE("ENTROPY STARVATION: Failed to gather cryptographic variance.");
        close(urandom_fd);
        return -3;
    }

    volatile unsigned char* primary = (volatile unsigned char*)targetKeyBuffer;
    for (size_t i = 0; i < bufferSize; i++) {
        primary[i] ^= entropyToken[i];
    }

    memset(entropyToken, 0x00, sizeof(entropyToken));
    close(urandom_fd);

    ALOGI("Cryptographic vector successfully mutated in memory space.");
    return 0;
}
