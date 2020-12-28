//#include "FaceFinder.h"

#include <vector>
#include <iostream>
#include <math.h>

#include "dlib/image_processing/frontal_face_detector.h"
#include "dlib/image_processing.h"
#include "dlib/image_io.h"
//#include "dlib/gui_widgets/widgets.h"
#include "dlib/data_io.h"
#include "dlib/image_processing.h"
#include "dlib/opencv/cv_image.h"

class LandmarkDetector
{
public:
	LandmarkDetector(std::string &pathToModel);
	LandmarkDetector();
	~LandmarkDetector();

	//---------------------Setters & Getters-------------------------//
	void setModelPath(std::string pathToModel);
	std::string getModelPath();

	//----------------Apply Dlib Landmark detector-----------------//
	void applyDlib(cv::Mat &frame, cv::Rect &faceLocation, float(&newShape)[2][68]);

private:
	
	//Shape predictor
	dlib::shape_predictor sp;

	//Rectangle from dlib
	dlib::rectangle rectDlib;

	//Result shape
	dlib::full_object_detection shape;

	//--------------Paths to the face detector--------------//
	std::string modelPath;//Path to shape predictor
};
