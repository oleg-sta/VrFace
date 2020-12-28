cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)
set(libs "/Users/oleg.stadnichenko/home/opencv-android-4.5.1/OpenCV-android-sdk/sdk/native/libs")
set(staticlibs "/Users/oleg.stadnichenko/home/opencv-android-4.5.1/OpenCV-android-sdk/sdk/native/staticlibs")
include_directories(/Users/oleg.stadnichenko/home/opencv-android-4.5.1/OpenCV-android-sdk/sdk/native/jni/include)

#-----------------adding libraries-------------------------

add_library(libopencv_java4 SHARED IMPORTED )
set_target_properties(libopencv_java4 PROPERTIES
        IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_java4.so")

add_library(libopencv_core STATIC IMPORTED )
set_target_properties(libopencv_core PROPERTIES
        IMPORTED_LOCATION "${staticlibs}/${ANDROID_ABI}/libopencv_core.a")

add_library(detection_based_tracker SHARED
        DetectionBasedTracker_jni.cpp 3DFaceModel.cpp 3DFaceModel.h OrthogonalProjectionModel.cpp OrthogonalProjectionModel.h ObjectiveFunctionHelper.cpp ObjectiveFunctionHelper.h Shape2D.cpp Shape2D.h ObjectiveFunction.cpp ObjectiveFunction.h TestObjectiveFunction.cpp TestObjectiveFunction.h FaceFollower.cpp FaceFollower.h FaceFinder.cpp FaceFinder.h LandmarkDetector.cpp LandmarkDetector.h
        )

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 -fexceptions -frtti")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 ")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 ")

set(OpenCV_DIR "/Users/oleg.stadnichenko/home/opencv-android-4.5.1/OpenCV-android-sdk/sdk/native/jni")
#find_package(OpenCV REQUIRED)
message(STATUS "OpenCV libraries: ${OpenCV_LIBS}")

target_link_libraries(detection_based_tracker
        #        ${OpenCV_LIBS}
        libopencv_java4
        libopencv_core
        android
        #        log
        )