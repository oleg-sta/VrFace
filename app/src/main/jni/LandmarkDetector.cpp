#include "LandmarkDetector.h"

//-------------------------------------------------------//
//---------------Constructor & Destructor----------------//
//-------------------------------------------------------//

LandmarkDetector::LandmarkDetector()
{

}

LandmarkDetector::LandmarkDetector(std::string &pathToModel)
{
	//Desirialize shape predictor
	dlib::deserialize(pathToModel) >> sp;
	modelPath = pathToModel;
}

LandmarkDetector::~LandmarkDetector()
{
}

//-------------------------------------------------------//
//------------------Setters & Getters--------------------//
//-------------------------------------------------------//

void LandmarkDetector::setModelPath(std::string pathToModel)
{
	dlib::deserialize(modelPath) >> sp;
	modelPath = pathToModel;
}

std::string LandmarkDetector::getModelPath()
{
	return modelPath;
}

//Method to apply Dlib
//A user has to allocate appropriate result array size beforehand (e.g 68 landmarks)
void LandmarkDetector::applyDlib(cv::Mat &frame, cv::Rect &faceLocation, float(&newShape)[2][68])
{
	//Create rectangle
	rectDlib = dlib::rectangle((long)faceLocation.tl().x, (long)faceLocation.tl().y, (long)faceLocation.br().x - 1, (long)faceLocation.br().y - 1);

	//Apply shape predictor
	shape = sp(dlib::cv_image<uchar>(frame), rectDlib);

	//Get all the points
	for (unsigned long i = 0; i < shape.num_parts(); i++)
	{
		newShape[0][i] = (float)shape.part(i).x();
		newShape[1][i] = (float)shape.part(i).y();
	}
}

