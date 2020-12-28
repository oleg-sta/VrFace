#include <vector>
#include <iostream>
#include <math.h>

#include <opencv2/features2d.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/opencv.hpp>

class FaceFinder
{
public:
	FaceFinder(std::string &cascadePathHaar, std::string &cascadePathLbpFrontal, std::string &cascadePathLbpLeft, std::string &cascadePathLbpRight);
	FaceFinder();
	~FaceFinder();

	//-----------------------Setters-------------------------//
	void setNumIntersect(int input);
	void setNumRescalingsFullScan(int input);
	void setNumRescalingsShortScan(int input);
	void setMinFaceFractionScreenFullScan(double input);
	void setMaxFaceFractionScreenFullScan(double input);
	void setMinFacePrevFraction(double input);
	void setMaxFacePrevFraction(double input);

	//-----------------------Getters-------------------------//
	int getNumIntersect();
	int getNumRescalingsFullScan();
	int getNumRescalingsShortScan();
	double getMinFaceFractionScreenFullScan();
	double getMaxFaceFractionScreenFullScan();
	double getMinFacePrevFraction();
	double getMaxFacePrevFraction();
	int getMinFaceShortScan();
	int getMaxFaceShortScan();
	double getScaleShortScan();

	//-----------------------Viola Jones parameters-------------------------//
	//Calculate Viola Joines paramters for Full scan
	void violaJonesParametersFullScan(const cv::Mat &frame);

	//Calculate Viola Jones paramters for Short scan
	void violaJonesParametersShortScan(const cv::Mat &frame, int prevWidth);//Correct only face size

	//-----------------------Run image scan--------------------------//
	//Full scan of an image - Haar
	bool violaJonesScanHaar(const cv::Mat &frame, cv::Rect &faceLocation);

	//Full scan of an image - Lbp
	bool violaJonesScanLbp(const cv::Mat &frame, cv::Rect &faceLocation);

	//Scan within in the whole frame but with size paramters - Haar
	bool violaJonesScanROIHaar(const cv::Mat & frame, int prevWidth, cv::Rect & newFaceLocation);

	//Scan within in the whole frame but with size paramters - LBP (frontal + left + right)
	bool violaJonesScanROILbp(const cv::Mat & frame, int prevWidth, cv::Rect & newFaceLocation);

private:

	//--------------Paths to the face detector--------------//
	std::string pathToCascadeFace;//Path to cascade classifier

	//-----------Parameters for cascade classifier----------//
	cv::CascadeClassifier faceCascadeHaar;//Cascade
	cv::CascadeClassifier faceCascadeLbpFrontal;//Cascade lbp frontal
	cv::CascadeClassifier faceCascadeLbpLeft;//Cascade lbp left
	cv::CascadeClassifier faceCascadeLbpRight;//Cascade lbp right
	int numIntersect = 3;//Number of intersections to detect face

	int numRescalingsFullScan = 6;//The number of rescalings during full scan
	int numRescalingsShortScan = 4;//The number of rescalings during short scan (when we found face in the previous frame)

	double minFaceFractionScreenFullScan = 0.3;//Minimum face fraction of the screen smallest side (full scan of the screen)
	double maxFaceFractionScreenFullScan = 0.8;//Maximum face fraction of the screen smallest side (full scan of the screen)

	double minFacePrevFraction = 0.8;//Minimum face fraction of the previous frame's face size (when we found face in the previous frame)
	double maxFacePrevFraction = 1.2;//Maximum face fraction of the previous frame's face size (when we found face in the previous frame)

	//-----------------Parameters that are calculated for full scan----------------//
	double scaleFullScan;
	int minFaceFullScan;
	int maxFaceFullScan;

	//-----------------Parameters that are calculated for short scan----------------//
	double scaleShortScan;
	int minFaceShortScan;
	int maxFaceShortScan;
};

