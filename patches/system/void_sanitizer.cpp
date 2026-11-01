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

    void secure_memset_volatile(void* ptr, int value, size_t num) {
        volatile unsigned char* p = static_cast<volatile unsigned char*>(ptr);
        while (num--) {
            *p++ = static_cast<unsigned char>(value);
        }
    }

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

    bool wipeScratchpad() {
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

    bool runSanitizationPass() {
        if (!mIsEngineActive) return false;
        ALOGI("Running one-shot sanitization pass.");

        bool ok = dropKernelCaches();
        ok &= wipeScratchpad();

        ALOGI("Sanitization complete. Total bytes wiped: %zu", mTotalPurgedBytes);
        return ok;
    }

    [[noreturn]] void spawnMonitorDaemon() {
        ALOGI("VoidOS Sanitizer Daemon active. Waiting for shutdown signal or SIGUSR1.");

        runSanitizationPass();

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

}
}
}

int main(int argc, char** argv) {
    android::os::security::VoidMemorySanitizer sanitizerInstance;
    sanitizerInstance.spawnMonitorDaemon();
    return 0;
}
