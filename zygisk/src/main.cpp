#include <jni.h>
#include <string>

#include "VirtualCamBridge.h"

// The zygisk.hpp header is provided by Magisk during build; referenced here to outline module lifecycle.
// This file purposefully limits build-time dependencies so the project remains self-contained.
namespace zygisk {
class Api;
class AppSpecializeArgs;
class ServerSpecializeArgs;

class ModuleBase {
  public:
    virtual ~ModuleBase() = default;
    virtual void onLoad(Api *api, JNIEnv *env) = 0;
    virtual void preAppSpecialize(AppSpecializeArgs *args) {}
    virtual void postAppSpecialize(AppSpecializeArgs *args) {}
    virtual void preServerSpecialize(ServerSpecializeArgs *args) {}
    virtual void postServerSpecialize(ServerSpecializeArgs *args) {}
};
} // namespace zygisk

static VirtualCamBridge gBridge;
static std::string gPackageName;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_fakeandroidcam_zygisk_VirtualCamHooks_nativeShouldHook(JNIEnv *env, jclass, jstring pkg) {
    const char *raw = env->GetStringUTFChars(pkg, nullptr);
    std::string package(raw ? raw : "");
    if (raw) {
        env->ReleaseStringUTFChars(pkg, raw);
    }
    return gBridge.should_hook(package);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_fakeandroidcam_zygisk_VirtualCamHooks_nativeAssetFor(JNIEnv *env, jclass, jstring pkg) {
    const char *raw = env->GetStringUTFChars(pkg, nullptr);
    std::string package(raw ? raw : "");
    if (raw) {
        env->ReleaseStringUTFChars(pkg, raw);
    }
    auto route = gBridge.routing_for(package);
    return env->NewStringUTF(route.asset.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_fakeandroidcam_zygisk_VirtualCamHooks_nativeModeFor(JNIEnv *env, jclass, jstring pkg) {
    const char *raw = env->GetStringUTFChars(pkg, nullptr);
    std::string package(raw ? raw : "");
    if (raw) {
        env->ReleaseStringUTFChars(pkg, raw);
    }
    auto route = gBridge.routing_for(package);
    return env->NewStringUTF(route.mode.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_fakeandroidcam_zygisk_VirtualCamHooks_nativeReload(JNIEnv *, jclass) {
    gBridge.reload();
}

class VirtualCamZygisk : public zygisk::ModuleBase {
  public:
    void onLoad(zygisk::Api *api, JNIEnv *env) override {
        (void)api;
        jclass hooks = env->FindClass("com/fakeandroidcam/zygisk/VirtualCamHooks");
        if (hooks == nullptr) {
            return;
        }
        JNINativeMethod methods[] = {
            {const_cast<char *>("nativeShouldHook"), const_cast<char *>("(Ljava/lang/String;)Z"),
             reinterpret_cast<void *>(Java_com_fakeandroidcam_zygisk_VirtualCamHooks_nativeShouldHook)},
            {const_cast<char *>("nativeAssetFor"), const_cast<char *>("(Ljava/lang/String;)Ljava/lang/String;"),
             reinterpret_cast<void *>(Java_com_fakeandroidcam_zygisk_VirtualCamHooks_nativeAssetFor)},
            {const_cast<char *>("nativeModeFor"), const_cast<char *>("(Ljava/lang/String;)Ljava/lang/String;"),
             reinterpret_cast<void *>(Java_com_fakeandroidcam_zygisk_VirtualCamHooks_nativeModeFor)},
            {const_cast<char *>("nativeReload"), const_cast<char *>("()V"),
             reinterpret_cast<void *>(Java_com_fakeandroidcam_zygisk_VirtualCamHooks_nativeReload)},
        };
        env->RegisterNatives(hooks, methods, sizeof(methods) / sizeof(methods[0]));
    }

    void preAppSpecialize(zygisk::AppSpecializeArgs *args) override {
        (void)args; // In a complete build we would extract the package name from args.
    }

    void postAppSpecialize(zygisk::AppSpecializeArgs *args) override { (void)args; }
};

// Factory consumed by Magisk at runtime
extern "C" __attribute__((visibility("default"))) zygisk::ModuleBase *
zygisk_module_create() {
    return new VirtualCamZygisk();
}
