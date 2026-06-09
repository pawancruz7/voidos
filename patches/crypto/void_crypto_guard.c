// ============================================================================
//  VoidOS Cryptographic Subsystem
//  Component: Hardened Crypto-Guard & Entropy Injection Matrix
//  Architect: voidOS Core Platform Architecture
// ============================================================================

#include <linux/random.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <utils/Log.h>

#undef LOG_TAG
#define LOG_TAG "VoidOS_CryptoGuard"

#define CRITICAL_KEY_SIZE_BYTES 64
#define ENTROPY_POOL_THRESHOLD  4096

/**
 * VoidOS Real-Time Entropy Injector & Cryptographic Guard
 *
 * Fills targetKeyBuffer with cryptographically strong random bytes
 * sourced directly from /dev/urandom (kernel entropy pool).
 *
 * Fix note: Previous version XOR'd entropy onto an uninitialized buffer,
 * which produces garbage output if the buffer has never been set.
 * Correct approach: direct copy from /dev/urandom into the buffer.
 */
int __attribute__((optimize("O2")))
inject_hardware_entropy(void* targetKeyBuffer, size_t bufferSize) {

    if (targetKeyBuffer == NULL) {
        ALOGE("SECURITY FAULT: NULL target buffer passed.");
        return -1;
    }

    if (bufferSize == 0 || bufferSize > CRITICAL_KEY_SIZE_BYTES) {
        ALOGE("SECURITY FAULT: Buffer size %zu out of secure bounds (1-%d).",
              bufferSize, CRITICAL_KEY_SIZE_BYTES);
        return -1;
    }

    // Open kernel hardware entropy pool
    int urandom_fd = open("/dev/urandom", O_RDONLY);
    if (urandom_fd < 0) {
        ALOGE("FATAL: /dev/urandom unreadable. Cannot inject entropy.");
        return -2;
    }

    // Read exactly bufferSize bytes of entropy
    ssize_t bytes_read = read(urandom_fd, targetKeyBuffer, bufferSize);
    close(urandom_fd);

    if (bytes_read != (ssize_t)bufferSize) {
        ALOGE("ENTROPY STARVATION: Expected %zu bytes, got %zd.", bufferSize, bytes_read);
        // Zero out partial write — do not leave partial entropy in buffer
        memset(targetKeyBuffer, 0x00, bufferSize);
        return -3;
    }

    // No XOR — buffer is now directly filled with strong random bytes
    // Previous XOR logic was: primary[i] ^= entropyToken[i]
    // That is only safe if primary[] is already initialized with a known value.
    // Direct fill is correct for key generation use case.

    ALOGI("Entropy injected successfully: %zu bytes from /dev/urandom.", bufferSize);
    return 0;
}
