LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := inputsource

LOCAL_AAPT_FLAGS := --auto-add-overlay \
   --extra-packages android.support.v17.leanback

LOCAL_RESOURCE_DIR := frameworks/support/leanback/src/main/res \
   $(LOCAL_PATH)/res

LOCAL_STATIC_JAVA_LIBRARIES += \
   companionlibrary \
   android-support-v17-leanback

LOCAL_AIDL_INCLUDES := $(LOCAL_PATH)/aidl

LOCAL_SRC_FILES := $(call all-subdir-java-files) $(call all-subdir-Iaidl-files)

#TARGET_BUILD_APPS := inputsource # for normal app (embedded ndk jni)
LOCAL_JNI_SHARED_LIBRARIES := libplatform
LOCAL_REQUIRED_MODULES := libplatform
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PRIVATE_PLATFORM_APIS := true
include $(BUILD_PACKAGE)
include $(call all-makefiles-under, $(LOCAL_PATH))

