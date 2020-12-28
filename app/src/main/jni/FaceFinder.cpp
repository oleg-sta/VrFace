#include "FaceFinder.h"

//------------------------------------------------------------------------------------------//
//--------------------------------Constructor and destructor--------------------------------//
//------------------------------------------------------------------------------------------//

FaceFinder::FaceFinder() { }

FaceFinder::FaceFinder(std::string &cascadePath, std::string &cascadePathLbpFrontal, std::string &cascadePathLbpLeft, std::string &cascadePathLbpRight)
{
	pathToCascadeFace = cascadePath;

	//Get cascade face detector
	if (!faceCascadeHaar.load(pathToCascadeFace))
	{
		std::cout << "Error loading face cascade Haar" << std::endl;
		return;
	}

	//Get cascade face detector
	if (!faceCascadeLbpFrontal.load(cascadePathLbpFrontal))
	{
		std::cout << "Error loading face cascade LBP frontal" << std::endl;
		return;
	}

	//Get cascade face detector
	if (!faceCascadeLbpLeft.load(cascadePathLbpLeft))
	{
		std::cout << "Error loading face cascade LBP left" << std::endl;
		return;
	}

	//Get cascade face detector
	if (!faceCascadeLbpRight.load(cascadePathLbpRight))
	{
		std::cout << "Error loading face cascade LBP right" << std::endl;
		return;
	}
}

FaceFinder::~FaceFinder()
{
}

//------------------------------------------------------------------------------------------//
//-----------------------------------Setters and getters------------------------------------//
//------------------------------------------------------------------------------------------//

void FaceFinder::setNumIntersect(int input)
{
	numIntersect = input;
}

void FaceFinder::setNumRescalingsFullScan(int input)
{
	numRescalingsFullScan = input;
}

void FaceFinder::setMinFaceFractionScreenFullScan(double input)
{
	minFaceFractionScreenFullScan = input;
}

void FaceFinder::setMaxFaceFractionScreenFullScan(double input)
{
	maxFaceFractionScreenFullScan = input;
}

void FaceFinder::setNumRescalingsShortScan(int input)
{
	numRescalingsShortScan = input;
}

void FaceFinder::setMinFacePrevFraction(double input)
{
	minFacePrevFraction = input;
}

void FaceFinder::setMaxFacePrevFraction(double input)
{
	maxFacePrevFraction = input;
}


int FaceFinder::getNumIntersect() { return numIntersect; }
int FaceFinder::getNumRescalingsFullScan() { return numRescalingsFullScan; }
int FaceFinder::getNumRescalingsShortScan() { return numRescalingsShortScan; }
double FaceFinder::getMinFaceFractionScreenFullScan() { return minFaceFractionScreenFullScan; }
double FaceFinder::getMaxFaceFractionScreenFullScan() { return maxFaceFractionScreenFullScan; }
double FaceFinder::getMinFacePrevFraction() { return minFacePrevFraction; }
double FaceFinder::getMaxFacePrevFraction() { return maxFacePrevFraction; }
int FaceFinder::getMinFaceShortScan() { return minFaceShortScan; }
int FaceFinder::getMaxFaceShortScan() { return maxFaceShortScan; }
double FaceFinder::getScaleShortScan() { return scaleShortScan; }


//------------------------------------------------------------------------------------------//
//-------------------------------Parameters for Viola Jones --------------------------------//
//------------------------------------------------------------------------------------------//

void FaceFinder::violaJonesParametersFullScan(const cv::Mat &frame)
{
	if (frame.cols <= frame.rows)
	{
		minFaceFullScan = (int)(((double)frame.cols) * minFaceFractionScreenFullScan);
		maxFaceFullScan = (int)(((double)frame.cols) * maxFaceFractionScreenFullScan);
	}
	else
	{
		minFaceFullScan = (int)(((double)frame.rows) * minFaceFractionScreenFullScan);
		maxFaceFullScan = (int)(((double)frame.rows) * maxFaceFractionScreenFullScan);
	}

	scaleFullScan = pow(maxFaceFractionScreenFullScan / minFaceFractionScreenFullScan, 1.0 / ((double)numRescalingsFullScan - 1.0));
}

void FaceFinder::violaJonesParametersShortScan(const cv::Mat &frame, int prevWidth)
{
	minFaceShortScan = (int)(((double)prevWidth) * minFacePrevFraction);
	maxFaceShortScan = (int)(((double)prevWidth) * maxFacePrevFraction);

	//Check that maximum face size is not bigger than the frame
	if (maxFaceShortScan > frame.cols)
	{
		maxFaceShortScan = frame.cols - 1;
	}

	if (maxFaceShortScan > frame.rows || maxFaceShortScan > frame.cols)
	{
		maxFaceShortScan = frame.rows - 1;
	}

	//The same for minimum
	if (minFaceShortScan > frame.cols)
	{
		minFaceShortScan = frame.cols - 1;
	}

	if (minFaceShortScan > frame.rows || minFaceShortScan > frame.cols)
	{
		minFaceShortScan = frame.rows - 1;
	}

	scaleShortScan = pow(((double)maxFaceShortScan) / ((double)minFaceShortScan), 1.0 / ((double)numRescalingsShortScan - 1.0));
}


//------------------------------------------------------------------------------------------//
//------------------------------------Viola Jones scan--------------------------------------//
//------------------------------------------------------------------------------------------//

bool FaceFinder::violaJonesScanHaar(const cv::Mat &frame, cv::Rect &faceLocation)
{
	//Calculate parameters
	violaJonesParametersFullScan(frame);

	//Vector with faces
	std::vector<cv::Rect> faces;

	//Apply cascade face detector
	faceCascadeHaar.detectMultiScale(frame, faces, scaleFullScan, numIntersect, 0 | cv::CASCADE_SCALE_IMAGE | cv::CASCADE_FIND_BIGGEST_OBJECT, cv::Size(minFaceFullScan, minFaceFullScan), cv::Size(maxFaceFullScan, maxFaceFullScan));

	//Check that we found a face
	if (faces.size() > 0)
	{
		faceLocation = faces[0];
		return true;
	}
	else
	{
		return false;
	}
}

bool FaceFinder::violaJonesScanLbp(const cv::Mat &frame, cv::Rect &faceLocation)
{
	//Calculate parameters
	violaJonesParametersFullScan(frame);

	//Vector with faces
	std::vector<cv::Rect> faces;

	//Apply cascade face detector
	faceCascadeLbpFrontal.detectMultiScale(frame, faces, scaleFullScan, numIntersect, 0 | cv::CASCADE_SCALE_IMAGE | cv::CASCADE_FIND_BIGGEST_OBJECT, cv::Size(minFaceFullScan, minFaceFullScan), cv::Size(maxFaceFullScan, maxFaceFullScan));

	//Check that we found a face
	if (faces.size() > 0)
	{
		faceLocation = faces[0];
		return true;
	}
	else
	{
		return false;
	}
}

bool FaceFinder::violaJonesScanROIHaar(const cv::Mat & frame, int prevWidth, cv::Rect & newFaceLocation)
{
	//Calculate parameters for scan
	violaJonesParametersShortScan(frame, prevWidth);

	//Vector with faces
	std::vector<cv::Rect> faces;

	faceCascadeHaar.detectMultiScale(frame, faces, scaleShortScan, numIntersect, 0 | cv::CASCADE_SCALE_IMAGE | cv::CASCADE_FIND_BIGGEST_OBJECT, cv::Size(minFaceShortScan, minFaceShortScan), cv::Size(maxFaceShortScan, maxFaceShortScan));

	if (faces.size() > 0)
	{
		newFaceLocation = faces[0];
		return true;
	}
	else
	{
		return false;
	}
}

bool FaceFinder::violaJonesScanROILbp(const cv::Mat & frame, int prevWidth, cv::Rect & newFaceLocation)
{
	//Calculate parameters for scan
	violaJonesParametersShortScan(frame, prevWidth);

	//Vector with faces
	std::vector<cv::Rect> faces;

	//Apply frontal LBP
	faceCascadeLbpFrontal.detectMultiScale(frame, faces, scaleShortScan, numIntersect, 0 | cv::CASCADE_SCALE_IMAGE | cv::CASCADE_FIND_BIGGEST_OBJECT, cv::Size(minFaceShortScan, minFaceShortScan), cv::Size(maxFaceShortScan, maxFaceShortScan));

	if (faces.size() > 0)
	{
		newFaceLocation = faces[0];
		return true;
	}
	else
	{
		//Apply left LBP
		faceCascadeLbpLeft.detectMultiScale(frame, faces, scaleShortScan, numIntersect, 0 | cv::CASCADE_SCALE_IMAGE | cv::CASCADE_FIND_BIGGEST_OBJECT, cv::Size(minFaceShortScan, minFaceShortScan), cv::Size(maxFaceShortScan, maxFaceShortScan));

		if (faces.size() > 0)
		{
			newFaceLocation = faces[0];
			return true;
		}
		else
		{
			//Apply right LBP
			faceCascadeLbpRight.detectMultiScale(frame, faces, scaleShortScan, numIntersect, 0 | cv::CASCADE_SCALE_IMAGE | cv::CASCADE_FIND_BIGGEST_OBJECT, cv::Size(minFaceShortScan, minFaceShortScan), cv::Size(maxFaceShortScan, maxFaceShortScan));

			if (faces.size() > 0)
			{
				newFaceLocation = faces[0];
				return true;
			}
			else
			{
				return false;
			}
		}
	}
}



