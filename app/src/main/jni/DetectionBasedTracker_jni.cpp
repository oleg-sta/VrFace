#include "DetectionBasedTracker_jni.h"
#include <opencv2/core/core.hpp>
#include <opencv2/objdetect/detection_based_tracker.hpp>
#include "opencv2/imgproc/imgproc.hpp"
#include <opencv2/calib3d/calib3d.hpp>

#include <string>
#include <vector>
#include <queue>
#include <pthread.h>

#include <android/log.h>

#include "constants.h"
#include "helpers.h"
#include "ModelClass.cpp"
#include "Line.cpp"
#include "Triangle.cpp"
#include "ForwardDifference.h"

#include "orthographic_camera_estimation_linear.hpp"
#include "find_coeffs_linear.hpp"

#include "dlib/image_processing/frontal_face_detector.h"
#include "dlib/image_processing/render_face_detections.h"
#include "dlib/image_processing.h"
#include "dlib/gui_widgets.h"
#include "dlib/image_io.h"
#include "dlib/opencv.h"
#include "dlib/opencv/cv_image.h"

#define LOG_TAG "FaceDetection/DetectionBasedTracker"
//#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#ifndef DEBUG
#define LOGD(...)
#else
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#endif

using namespace std;
using namespace cv;
using namespace dlib;
const int n_blendshapes = 5; // 0,1,2 - mouth, 3 - face broader, 4 - chin lower

//inline void vector_Rect_to_Mat(cv::vector<Rect>& v_rect, Mat& mat)
//{
//    mat = Mat(v_rect, true);
//}

JNIEXPORT jlong JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_nativeCreateObject
(JNIEnv * jenv, jclass, jstring jFileName, jint faceSize)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject enter");
    const char* jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
    string stdFileName(jnamestr);
    jlong result = 0;

    try
    {
//        DetectionBasedTracker::Parameters DetectorParams;
//        if (faceSize > 0)
//            DetectorParams.minObjectSize = faceSize;
//        result = (jlong)new DetectionBasedTracker(stdFileName, DetectorParams);
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeCreateObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeCreateObject()");
        return 0;
    }

    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject exit");
    return result;
}

JNIEXPORT jlong JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_nativeCreateModel
(JNIEnv * jenv, jclass, jstring jFileName)
{
	LOGD("findEyes119 dd");
	jlong result = 0;
	result = (jlong)new ModelClass(jenv->GetStringUTFChars(jFileName, NULL));
	LOGD("findEyes119 dde111 %i", result);
	return result;
}

JNIEXPORT void JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_nativeDestroyObject
(JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDestroyObject enter");
    try
    {
        if(thiz != 0)
        {
            ((DetectionBasedTracker*)thiz)->stop();
            delete (DetectionBasedTracker*)thiz;
        }
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeestroyObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDestroyObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeDestroyObject()");
    }
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDestroyObject exit");
}

JNIEXPORT void JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_nativeStart
(JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeStart enter");
    try
    {
        ((DetectionBasedTracker*)thiz)->run();
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStart caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeStart caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeStart()");
    }
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeStart exit");
}

JNIEXPORT void JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_nativeStop
(JNIEnv * jenv, jclass, jlong thiz)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeStop enter");
    try
    {
        ((DetectionBasedTracker*)thiz)->stop();
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStop caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeStop caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeStop()");
    }
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeStop exit");
}

JNIEXPORT void JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_nativeSetFaceSize
(JNIEnv * jenv, jclass, jlong thiz, jint faceSize)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeSetFaceSize enter");
    try
    {
        if (faceSize > 0)
        {
            DetectionBasedTracker::Parameters DetectorParams = \
            ((DetectionBasedTracker*)thiz)->getParameters();
//            DetectorParams.minObjectSize = faceSize;
            ((DetectionBasedTracker*)thiz)->setParameters(DetectorParams);
        }
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeStop caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeSetFaceSize caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeSetFaceSize()");
    }
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeSetFaceSize exit");
}

JNIEXPORT void JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_mergeAlpha
(JNIEnv * jenv, jclass, jlong imageFrom, jlong imageTo)
{
	LOGD("findEyes Java_com_stoleg_vrface_DetectionBasedTracker_mergeAlpha");
	cv::Mat imageFromMat = *((Mat*)imageFrom);
	cv::Mat imageToMat = *((Mat*)imageTo);
	LOGD("findEyes Java_com_stoleg_vrface_DetectionBasedTracker_mergeAlpha2 %i %i", imageFromMat.cols, imageFromMat.rows);
	for (int i = 0; (i < imageFromMat.rows && i < imageToMat.rows); i++) {
		  //LOGD("findEyes1124");
		//LOGD("findEyes Java_com_stoleg_vrface_DetectionBasedTracker_mergeAlpha3 %i", i);
		  for (int j = 0; (j < imageFromMat.cols && j < imageToMat.cols) ; j++) {
			  //LOGD("findEyes115");
			  //img[i][j] = frame_gray.at<uchar>(i, j);
			  cv::Vec4b pixelFrom = imageFromMat.at<cv::Vec4b>(i, j);
			  cv::Vec4b pixelTo = imageToMat.at<cv::Vec4b>(i, j);
			  int alpha = pixelFrom[3] / 2;
			  for (int ij = 0; ij < 3; ij++) {
				  pixelTo[ij] = (pixelTo[ij] * (255 - alpha) + pixelFrom[ij] * alpha) / 255;
			  }
			  imageToMat.at<cv::Vec4b>(i, j) = pixelTo;
		  }
	}
}

double getObjectFieldD(JNIEnv* env, jobject obj, jclass clsFeature, const char* name) {
	jfieldID x1FieldId2 = env->GetFieldID(clsFeature, name, "D");
	return env->GetDoubleField(obj, x1FieldId2);
}

int getObjectFieldI(JNIEnv* env, jobject obj, jclass clsFeature, const char* name) {
	jfieldID x1FieldId2 = env->GetFieldID(clsFeature, name, "I");
	return env->GetIntField(obj, x1FieldId2);
}

JNIEXPORT jobjectArray JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_findLandMarks
(JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jint x, jint y, jint width, jint height, jlong thizModel)
{
	LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_findLandMarks");

	cv::Mat imageGrayInner = *((Mat*)imageGray);
	cv::Rect faceRect(x, y,  width, height);
	LOGD("findEyes imageGray %d %d", imageGrayInner.rows, imageGrayInner.cols);
	LOGD("findEyes face %d %d %d %d", faceRect.x, faceRect.y, faceRect.height, faceRect.width);
	std::vector<cv::Point> pixels;
	findLandMarks(imageGrayInner, faceRect, pixels, (ModelClass*)thizModel);


	jclass clsPoint = jenv->FindClass("org/opencv/core/Point");
	jobjectArray jobAr = jenv->NewObjectArray(pixels.size(), clsPoint, NULL);

	jmethodID constructorPoint = jenv->GetMethodID(clsPoint, "<init>", "(DD)V");
	int i = 0;
	for (std::vector<cv::Point>::iterator it = pixels.begin() ; it != pixels.end(); ++it) {
		jobject objPoint = jenv->NewObject(clsPoint, constructorPoint, (double)(*it).x, (double)(*it).y);
		jenv->SetObjectArrayElement(jobAr, i, objPoint);
		i++;
	}

	return jobAr;
}

JNIEXPORT void JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_nativeDetect
(JNIEnv * jenv, jclass, jlong thiz, jlong imageGray, jlong faces)
{
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDetect enter");
    try
    {
//        cv::vector<Rect> RectFaces;
//        ((DetectionBasedTracker*)thiz)->process(*((Mat*)imageGray));
//        ((DetectionBasedTracker*)thiz)->getObjects(RectFaces);
//        vector_Rect_to_Mat(RectFaces, *((Mat*)faces));
    }
    catch(cv::Exception& e)
    {
        LOGD("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass("org/opencv/core/CvException");
        if(!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...)
    {
        LOGD("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code DetectionBasedTracker.nativeDetect()");
    }
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeDetect exit");
}

matrix<double> box_constrain_parameters(dlib::matrix<double> parameters)
{
    dlib::matrix<double> constrained_parameters = parameters;
    for (int i = 6; i < parameters.nr(); ++i)
    {
        if (parameters(i,0)<0)
           constrained_parameters(i,0) = 0;
        if (parameters(i,0)>1)
                   constrained_parameters(i,0) = 1;
    }
    return constrained_parameters;
}

JNIEXPORT jlong JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_morhpFaceInit
(JNIEnv * jenv, jclass, jstring path)
{
    LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_morhpFaceInit enter");
    const char* jnamestr = jenv->GetStringUTFChars(path, NULL);
    std::string str(jnamestr);
    FaceModel3D* model3d = new FaceModel3D(str, n_blendshapes);
    LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_morhpFaceInit exit");
    return (jlong)model3d;
}

JNIEXPORT jlong JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_trackFaceInit
        (JNIEnv * jenv, jclass, jstring path, jstring path2, jstring jlbpFrontalPath, jstring jlbpLeftPath, jstring jlbpRightPath)
{
    LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_trackFaceInit enter");
    const char* jnamestr = jenv->GetStringUTFChars(path, NULL);
    std::string str(jnamestr);
    const char* jnamestr2 = jenv->GetStringUTFChars(path2, NULL);
    std::string str2(jnamestr2);

    const char* jnamestr3 = jenv->GetStringUTFChars(jlbpFrontalPath, NULL);
    std::string lbpFrontalPath(jnamestr3);
    const char* jnamestr4 = jenv->GetStringUTFChars(jlbpLeftPath, NULL);
    std::string lbpLeftPath(jnamestr4);
    const char* jnamestr5 = jenv->GetStringUTFChars(jlbpRightPath, NULL);
    std::string lbpRightPath(jnamestr5);

    FaceFollower* model3d = new FaceFollower(str, str2, lbpFrontalPath, lbpLeftPath, lbpRightPath);
    LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_trackFaceInit exit");
    return (jlong)model3d;
}

JNIEXPORT jint JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_trackFace
        (JNIEnv * jenv, jclass, jlong jGreyImg, jlong jmatrixFacePrev, jlong jmatrix2dLands, jint flag, jlong jFaceFollower)
{
    LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_trackFace enter");
    float shapeCurrent[2][68];
    cv::Point2f faceRectangle[4];
    bool prevFaceFound = (flag == 1 ? true : false);
    cv::Mat matrix2dLands = *((Mat*)jmatrix2dLands);
    for (int i = 0; i < matrix2dLands.rows; i++) {
        shapeCurrent[0][i] = matrix2dLands.at<double>(i, 0);
        shapeCurrent[1][i] = matrix2dLands.at<double>(i, 1);
    }
    cv::Mat matrixFacePrev = *((Mat*)jmatrixFacePrev);
    for (int i = 0; i < matrixFacePrev.rows; i++) {
        faceRectangle[i].x = matrixFacePrev.at<double>(i, 0);
        faceRectangle[i].y = matrixFacePrev.at<double>(i, 1);
    }
    cv::Mat greyImg = *((Mat*)jGreyImg);
    LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_trackFace22");
    FaceFollower* ff = ((FaceFollower*)jFaceFollower);
    if (prevFaceFound)
    {
        LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_trackFace1");
        prevFaceFound = ff->findNewLocation(greyImg, faceRectangle, faceRectangle, shapeCurrent);
    }
    else
    {
        LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_trackFace2");
        prevFaceFound = ff->findNewLocation(greyImg, faceRectangle, shapeCurrent);
    }
    // output
    for (int i = 0; i < matrix2dLands.rows; i++) {
        matrix2dLands.at<double>(i, 0) = shapeCurrent[0][i];
        matrix2dLands.at<double>(i, 1) = shapeCurrent[1][i];
    }
    for (int i = 0; i < matrixFacePrev.rows; i++) {
        matrixFacePrev.at<double>(i, 0) = faceRectangle[i].x;
        matrixFacePrev.at<double>(i, 1) = faceRectangle[i].y;
    }
    LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_trackFace exit");
    return prevFaceFound ? 1 : 0;
}

JNIEXPORT void JNICALL Java_com_stoleg_vrface_DetectionBasedTracker_morhpFace
(JNIEnv * jenv, jclass, jlong jmatrix2dLands, jlong jmatrix3dFace, jlong jinitialParams, jlong model3dL, jint flag, jint juseLinear, jint useBroader, jlong jmatrix3dOrtho, jint useBlends)
{
    LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_morhpFace enter");
    LOGD("morhpFace3 %i", juseLinear );
    LOGD("morhpFace3 %i useBroader", useBroader );
    bool useLinear = (juseLinear == 1)? true : false;
    cv::Mat matrix3dFace = *((Mat*)jmatrix3dFace);
    cv::Mat initialParams0 = *((Mat*)jinitialParams);
    cv::Mat matrix2dLands = *((Mat*)jmatrix2dLands);
    cv::Mat matrix3dOrtho = *((Mat*)jmatrix3dOrtho);
    matrix<double> landmarks; // TODO
    landmarks.set_size(2, matrix2dLands.rows);
    for (int i = 0; i < matrix2dLands.rows; i++) {
        landmarks(0,i) = matrix2dLands.at<double>(i, 0);
        landmarks(1,i) = matrix2dLands.at<double>(i, 1);
    }
    LOGD("morhpFace1 %i %i", matrix2dLands.rows, landmarks.nc());
    FaceModel3D model3d = *((FaceModel3D*)model3dL);
    LOGD("morhpFace3");
    Shape2D model2d = Shape2D();
    OrthogonalProjectionModel projection_model = OrthogonalProjectionModel(n_blendshapes);
    LOGD("morhpFace4");
    const matrix<double> &xx = model3d.get_mean_shape3d();
    LOGD("morhpFace41");
    const matrix<double> &yy = model2d.get_shape2d(landmarks);
    LOGD("morhpFace42 %i %i", xx.nc(), yy.nc());
    dlib::matrix<double,6+n_blendshapes,1> initialParameters= projection_model.get_initial_parameters(xx, yy);
    if (flag == 1) {
       for (int i = 0; i < 6+n_blendshapes; i++)
       {
           LOGD("morhpFace411 %i %f", i, initialParams0.at<double>(i, 0));
           initialParameters(0, i) = initialParams0.at<double>(i, 0);
       }
    }
    LOGD("morhpFace43");
    ObjectiveFunctionHelper helper = ObjectiveFunctionHelper(model3d, model2d);
    LOGD("morhpFace5");
    ObjectiveFunction objFun = ObjectiveFunction(helper, projection_model);
    LOGD("morhpFace6");
    objFun.extract2d_from_image(landmarks);
    LOGD("morhpFace7");
    //objFun.set(model3d.get_all_blendshapes());
    //objFun.set(model3d.get_blendshapes());
    double val_init = objFun(initialParameters);
    LOGD("morhpFace8");

    dlib::matrix<double, 6+n_blendshapes, 1> lower = -dlib::ones_matrix<double>(6+n_blendshapes,1) * 0;
    dlib::set_subm(lower,0,0,6,1) = - 10000000;
    dlib::matrix<double, 6+n_blendshapes, 1> upper = dlib::ones_matrix<double>(6+n_blendshapes,1) * 1;
    dlib::set_subm(upper,0,0,6,1) =  10000000;
    dlib::matrix<double, 6+n_blendshapes, 1> initial_parameters_box_constrained = box_constrain_parameters(initialParameters);

    dlib::matrix<double> initial_parameters = initialParameters;


    if (!useLinear)
    {
    LOGD("morhpFace8 iteration");
    auto forward_derivative_fun = forward_derivative(objFun);
    double val = find_min_box_constrained(bfgs_search_strategy(),
                                                        objective_delta_stop_strategy(1e-2),
                                                        objFun,
                                                        forward_derivative_fun,
                                                        initial_parameters_box_constrained, lower,
                                                        upper);
     initial_parameters(6,0) = initial_parameters_box_constrained(6,0);
     initial_parameters(7,0) = initial_parameters_box_constrained(7,0);
     initial_parameters(8,0) = initial_parameters_box_constrained(8,0);

        if (useBroader == 1)
            initial_parameters(9,0) = initial_parameters_box_constrained(3,0);

        initial_parameters(9,0) = 1.4;

     }
     else
     {
     LOGD("morhpFace8 linear");
    //////experiment
    const int n_transformation_parameters = 6;
    const int n_blendshapes_parameters = n_blendshapes;
    dlib::matrix<double> blend_coeffs;
    eos::fitting::ScaledOrthoProjectionParameters resOrtho;

    dlib::matrix<double> current_3dshape = xx;//dlib::zeros_matrix<double>(xx.nc(), xx.nr());
    std::vector<cv::Vec2f> yy2d;
    std::vector<cv::Vec4f> xx3d;
    dlib::matrix<double> yy2 = helper.get_y(landmarks);
    	    for (int i2 = 0; i2 < xx.nc(); i2++)
    	    {
                    cv::Vec2f vec2f = cv::Vec2f(yy2(0, i2), yy2(1, i2));
    		yy2d.emplace_back(vec2f);
    		cv::Vec4f vec4f = cv::Vec4f(current_3dshape(0, i2), current_3dshape(1, i2), current_3dshape(2, i2), 1);
    		xx3d.emplace_back(vec4f);
    	    }
    	    resOrtho = eos::fitting::estimate_orthographic_projection_linear(yy2d, xx3d);

    	    //current_3dshape = projection_model.convert_mean_shape(blend_coeffs,xx,model3d.get_blendshapes());



    	    cv::Mat rodr;
    	    cv::Rodrigues(resOrtho.R, rodr);

    	    initial_parameters(0,0) = resOrtho.s;
    	    initial_parameters(1,0) = rodr.at<float>(0, 0);
    	    initial_parameters(2,0) = rodr.at<float>(1, 0);
    	    initial_parameters(3,0) = rodr.at<float>(2, 0);
    	    initial_parameters(4,0) = resOrtho.tx;
    	    initial_parameters(5,0) = resOrtho.ty;

         // do we need to use blendshapes?
         if (useBlends == 1) {
             blend_coeffs = find_coeffs_linear(resOrtho, yy2d, xx, model3d.get_blendshapes());
             LOGD("morhpFace9 %i %i %i %i", blend_coeffs.nc(), blend_coeffs.nr(),
                  initial_parameters.nc(), initial_parameters.nr());
             //set_subm(initial_parameters,0,0,n_transformation_parameters,1) = initial_parameters_tr;
             //set_subm(initial_parameters,n_transformation_parameters+1,0,n_blendshapes_parameters,1) = blend_coeffs;
             initial_parameters(6, 0) = blend_coeffs(0, 0);
             initial_parameters(7, 0) = blend_coeffs(1, 0);
             initial_parameters(8, 0) = blend_coeffs(2, 0);

             if (useBroader == 1) {
                 initial_parameters(9, 0) = blend_coeffs(3, 0) + 0.1; // magic number for wide
                 initial_parameters(10, 0) = blend_coeffs(4, 0) + 1.4; // magic number for chin
             }
         }

            //initial_parameters(9,0) = 1.4;

     }
     // experiment

    /*
    double val = find_min_using_approximate_derivatives(bfgs_search_strategy(),
                                                            objective_delta_stop_strategy(1e-5),
                                                            objFun,
                                                            initialParameters, -1);
    */
    LOGD("morhpFace9 %f", initialParameters(0, 6));
    dlib::matrix<double> full_mean_3d = model3d.get_all_mean_shape3d();
    LOGD("morhpFace10");
    std::unordered_map<int, dlib::matrix<double>> all_blendshapes = model3d.get_all_blendshapes();
    //dlib::matrix<double> final_shape_3d = projection_model.convert_mean_shape(initialParameters, full_mean_3d, all_blendshapes);
    dlib::matrix<double> final_shape_3d = projection_model.convert_mean_shape(initial_parameters,full_mean_3d,all_blendshapes);

    dlib::matrix<double> final_shape_3d_projected = projection_model.get_full_shape3d(initial_parameters,full_mean_3d,all_blendshapes);
    LOGD("morhpFace2323 %i %i", final_shape_3d_projected.nc(), final_shape_3d_projected.nr());
    LOGD("morhpFace2323 %f %f %f", final_shape_3d_projected(0, 0), final_shape_3d_projected(1, 0), final_shape_3d_projected(2, 0));

    LOGD("morhpFace2 %i %i %i", final_shape_3d.nc(), initialParameters.nr(), initialParameters.nc());
    LOGD("morhpFace2 params %f %f %f %f", initial_parameters(0, 6), initial_parameters(0, 7), initial_parameters(0, 8), initial_parameters(0, 9));
    //LOGD("morhpFace2 %f %f %f", final_shape_3d(0,0), final_shape_3d(1,0), final_shape_3d(2,0));
    //LOGD("morhpFace2 %f %f %f", full_mean_3d(0,0), full_mean_3d(1,0), full_mean_3d(2,0));
    for (int i = 0; i < final_shape_3d.nc(); ++i)
    {
       matrix3dFace.at<double>(i, 0) = final_shape_3d(0,i);
       matrix3dFace.at<double>(i, 1) = final_shape_3d(1,i);
       matrix3dFace.at<double>(i, 2) = final_shape_3d(2,i);

        // debug for ortho
        matrix3dOrtho.at<double>(i, 0) = final_shape_3d_projected(0,i);
        matrix3dOrtho.at<double>(i, 1) = final_shape_3d_projected(1,i);
        matrix3dOrtho.at<double>(i, 2) = final_shape_3d_projected(2,i);
    }
    for (int i = 0; i < 6+n_blendshapes; i++)
    {
       initialParams0.at<double>(i, 0) = initial_parameters(0, i);
    }
    LOGD("Java_com_stoleg_vrface_DetectionBasedTracker_morhpFace exit");
}

void findLandMarks(cv::Mat frame_gray, cv::Rect face, std::vector<cv::Point> &pixels, ModelClass *modelClass) {
	LOGD("findEyes1121 %i", frame_gray.type());
	LOGD("findEyes1122");
	cv_image<uchar> img(frame_gray);
	LOGD("findEyes114");
	dlib::rectangle d(face.x, face.y, face.x + face.width,
			face.y + face.height);
	LOGD("findEyes115");
	LOGD("findEyes113");
	full_object_detection shape = modelClass->getsp(img, d);
	LOGD("findEyes116 %i", shape.num_parts());
	if (shape.num_parts() > 2) {
		LOGD("findEyes116 %i %i", shape.part(0).x(), shape.part(0).y());
		LOGD("findEyes116 %i %i", shape.part(1).x(), shape.part(1).y());
	}
	for (int i = 0; i < shape.num_parts(); i++) {
		pixels.push_back(cv::Point(shape.part(i).x(), shape.part(i).y()));
	}
	LOGD("findEyes116");

	LOGD("findEyes116");
}
