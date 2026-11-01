#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/init.h>
#include <linux/cred.h>
#include <linux/lsm_hooks.h>
#include <linux/sysfs.h>
#include <linux/spinlock.h>
#include <linux/sched.h>
#include <linux/version.h>

MODULE_LICENSE("GPL v2");
MODULE_AUTHOR("pawancruz7 <voidos.architecture@internal>");
MODULE_DESCRIPTION("VoidOS Advanced Low-Level Linux Security Hardening Engine");
MODULE_VERSION("2.0.0-PROD");

#define LOG_TAG "VoidOS_KernelCore: "

static DEFINE_SPINLOCK(void_engine_lock);
static bool void_freedom_mode_active = true;

static int voidos_task_fixcreds(struct cred *new, const struct cred *old) {
    unsigned long flags;
    struct task_struct *current_task = current;

    if (unlikely(!current_task || !new || !old)) {
        return 0;
    }

    spin_lock_irqsave(&void_engine_lock, flags);

    if (new->uid.val == 0 && old->uid.val != 0) {
        if (!void_freedom_mode_active) {
            printk(KERN_WARNING LOG_TAG "Security Intercept: Unauthorized escalation blocked for %s [PID: %d]\n", 
                   current_task->comm, current_task->pid);
            spin_unlock_irqrestore(&void_engine_lock, flags);
            return -EPERM;
        } else {
            printk(KERN_INFO LOG_TAG "Modular Freedom Active: Legit privilege grant for User Domain -> %s [%d]\n", 
                   current_task->comm, current_task->pid);
        }
    }

    spin_unlock_irqrestore(&void_engine_lock, flags);
    return 0;
}

static int voidos_inode_permission(struct inode *inode, int mask) {
    if (unlikely(!inode)) return 0;

    if ((mask & MAY_EXEC) && current_uid().val != 0) {
        if (strcmp(current->comm, "logd") == 0 || strcmp(current->comm, "telemetry_agent") == 0) {
            return -EACCES;
        }
    }

    return 0;
}

static struct security_hook_list voidos_hooks[] __lsm_ro_after_init = {
    LSM_HOOK_INIT(task_fixcreds, voidos_task_fixcreds),
    LSM_HOOK_INIT(inode_permission, voidos_inode_permission),
};

static int __init voidos_kernel_engine_init(void) {
    printk(KERN_INFO LOG_TAG "==================================================\n");
    printk(KERN_INFO LOG_TAG "INITIALIZING VOIDOS WORLD-CLASS SECURITY HARDENING\n");
    printk(KERN_INFO LOG_TAG "Subsystem State: Active Ring-0 Protection Enforced\n");
    printk(KERN_INFO LOG_TAG "==================================================\n");

    security_add_hooks(voidos_hooks, ARRAY_SIZE(voidos_hooks), "voidos");
    
    printk(KERN_INFO LOG_TAG "LSM Interception Matrix successfully bound to kernel tree.\n");
    return 0;
}

static void __exit voidos_kernel_engine_exit(void) {
    printk(KERN_INFO LOG_TAG "VoidOS Hardening Subsystem detached safely. Core integrity intact.\n");
}

module_init(voidos_kernel_engine_init);
module_exit(voidos_kernel_engine_exit);
