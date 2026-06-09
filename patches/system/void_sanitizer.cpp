// ============================================================================
//  VoidOS Native Core Security Subsystem
//  Component: Hardened Memory Sanitizer & Anti-Forensics Engine [Daemon]
//  Architect: voidOS Core Platform Architecture
// ============================================================================

#include <iostream>
#include <vector>
#include <string>
#include <fstream>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <signal.h>
#include <android/log.h>
#include <utils/Log.h>

#undef LOG_TAG
#define LOG_TAG "VoidOS_MemorySanitizer"

namespace android {
namespace os {
namespace security {

class VoidMemorySanitizer {
private:
    bool mIsEngineActive;
    size_t mTotalPurgedBytes;
    const std::string PROC_VM_DROP_CACHES = "/proc/sys/vm/drop_caches";

    // Volatile memset — compiler cannot optimize this away
    void secure_memset_volatile(void* ptr, int value, size_t num) {
        volatile unsigned char* p = static_cast<volatile unsigned char*>(ptr);
        while (num--) {
            *p++ = static_cast<unsigned char>(value);
        }
    }

    // Drop kernel page cache, dentries, and inodes
    bool dropKernelCaches() {
        std::ofstream vm_trigger(PROC_VM_DROP_CACHES, std::ios::out | std::ios::binary);
        if (!vm_trigger.is_open()) {
            ALOGE("SYSTEM EXCEPTION: Cannot open drop_caches. Root capability required.");
            return false;
        }
        vm_trigger << "3" << std::endl;
        vm_trigger.close();
        ALOGI("Kernel page cache dropped.");
        return true;
    }

    // One-shot scratchpad wipe — allocate, zero, free
    // Called ONCE at boot or shutdown — NOT in a loop
    bool wipeScratchpad() {
        // 4MB is enough for cache eviction without RAM thrashing
        const size_t wipeSize = 1024 * 1024 * 4;
        void* scratchpad = mmap(nullptr, wipeSize,
                                PROT_READ | PROT_WRITE,
                                MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
        if (scratchpad == MAP_FAILED) {
            ALOGE("CRITICAL: mmap failed for scratchpad wipe.");
            return false;
        }
        secure_memset_volatile(scratchpad, 0x00, wipeSize);
        munmap(scratchpad, wipeSize);
        mTotalPurgedBytes += wipeSize;
        return true;
    }

public:
    VoidMemorySanitizer() : mIsEngineActive(true), mTotalPurgedBytes(0) {
        ALOGI("Initializing VoidOS Memory Sanitizer...");
    }

    // Full sanitization pass — call once at boot and once at shutdown
    bool runSanitizationPass() {
        if (!mIsEngineActive) return false;
        ALOGI("Running one-shot sanitization pass.");

        bool ok = dropKernelCaches();
        ok &= wipeScratchpad();

        ALOGI("Sanitization complete. Total bytes wiped: %zu", mTotalPurgedBytes);
        return ok;
    }

    /**
     * Daemon loop — wakes up on SIGUSR1 or at shutdown signal.
     * Does NOT run continuously to avoid RAM/battery thrash.
     *
     * Trigger manually:   kill -SIGUSR1 <pid>
     * Trigger at shutdown: call from init.rc shutdown hook
     */
    [[noreturn]] void spawnMonitorDaemon() {
        ALOGI("VoidOS Sanitizer Daemon active. Waiting for shutdown signal or SIGUSR1.");

        // Run once at startup (boot-time wipe)
        runSanitizationPass();

        // Block waiting for signals — no busy loop, no RAM thrash
        sigset_t waitset;
        sigemptyset(&waitset);
        sigaddset(&waitset, SIGUSR1);
        sigaddset(&waitset, SIGTERM);
        sigprocmask(SIG_BLOCK, &waitset, nullptr);

        while (true) {
            int sig = 0;
            sigwait(&waitset, &sig);

            if (sig == SIGUSR1) {
                ALOGI("SIGUSR1 received — running on-demand sanitization.");
                runSanitizationPass();
            } else if (sig == SIGTERM) {
                ALOGI("SIGTERM received — running shutdown sanitization pass.");
                runSanitizationPass();
                ALOGI("VoidOS Sanitizer shutting down cleanly.");
                _exit(0);
            }
        }
    }
};

} // namespace security
} // namespace os
} // namespace android

int main(int argc, char** argv) {
    android::os::security::VoidMemorySanitizer sanitizerInstance;
    sanitizerInstance.spawnMonitorDaemon();
    return 0;
}