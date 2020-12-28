LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)/Users/oleg.stadnichenko/home/OpenCV-android-sdk-3.3.1/sdk/native/jni
#include (/Users/oleg.stadnichenko/home/dlib-18.18/dlib-18.18/dlib/cmake)

OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_MODULES:=contrib legacy stitching superres ocl objdetect ml ts videostab video photo calib3d features2d highgui imgproc flann androidcamera core java
OPENCV_LIB_TYPE:=SHARED
#OPENCV_LIB_TYPE:=STATIC
#include ../../sdk/native/jni/OpenCV.mk
include /Users/oleg.stadnichenko/home/OpenCV-android-sdk-3.3.1/sdk/native/jni/OpenCV.mk

#LOCAL_SRC_FILES  := DetectionBasedTracker_jni.cpp 3DFaceModel.cpp 3DFaceModel.h OrthogonalProjectionModel.cpp OrthogonalProjectionModel.h ObjectiveFunctionHelper.cpp ObjectiveFunctionHelper.h Shape2D.cpp Shape2D.h ObjectiveFunction.cpp ObjectiveFunction.h TestObjectiveFunction.cpp TestObjectiveFunction.h FaceFollower.cpp FaceFollower.h FaceFinder.cpp FaceFinder.h LandmarkDetector.cpp LandmarkDetector.h
LOCAL_SRC_FILES  := test.cpp test.h
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_CFLAGS += -std=c++11

LOCAL_MODULE     := detection_based_tracker

include $(BUILD_SHARED_LIBRARY)