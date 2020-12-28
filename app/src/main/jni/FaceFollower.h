#include "LandmarkDetector.h"
#include "FaceFinder.h"

class FaceFollower
{
public:
	FaceFollower(std::string shapePredictorPath, std::string haarPath, std::string lbpFrontalPath, std::string lbpLeftPath, std::string lbpRightPath);
	FaceFollower();
	~FaceFollower();

	//Method to update face location: returns false if there is no face in a new image
	bool findNewLocation(cv::Mat &frame, cv::Point2f(&newPosition)[4], float(&newLandmarks)[2][68]);//If there is no face in the previous frame
	bool findNewLocation(cv::Mat &frame, cv::Point2f(&previousPosition)[4], cv::Point2f(&newPosition)[4], float(&newLandmarks)[2][68]);

	//Drawing methods
	void drawRectangle(cv::Mat &frame, cv::Point2f (&rectPoints)[4]);
	void drawAllLandmarks(cv::Mat & frame, float(&landmarks)[2][68]);
	void drawPoint(cv::Mat &frame, int x, int y, int radius, int thinkness);

private:

	//--------------Paths to the face detector and landmark detector--------------//
	std::string shapePredictorPath;//Path to shape predictor
	std::string haarPath;

	//----------------------Face finder and Landmark detector---------------------//
	FaceFinder faceFinder;
	LandmarkDetector landmarkDetector;

	//--------------------------Parameters of the tracker-------------------------//
	float noseFraction = 0.3;//Fraction from landmark number 27 to the upper border of face rectangle (fraction from face size)
	float faceWidth = 1.25;//This is how we multiply distnace from landmark 27 to landmark 8 (upper nose to chin) to get face size
	float widenPreviousFacePosition = 1.5;//how much we widen previous face position

	//--------------------------Technical parameters-------------------------------//
	cv::Rect faceRectangle;//Open CV rectangle for Viola Jones search
	cv::Rect tempRect;//Temprorary rectangle
	float tempFaceLandmarks[2][68];//Landmarks in the rotated image
	cv::Point2f tempFacePosition[4];//position of face in the rotated image
	int x, y, width;//Coordinates of the previous face in turned face image
	cv::Point2f widenFaceRectangle[4];//Widen face rectangle for rotation
	cv::Point2f rotationPoint;//Point aorund which we rotate cut widen rectangle
	cv::Mat searchArea;//Area for landmarks searchMat 
	cv::Mat rotatedArea;//Image after rotation
	cv::Mat rotationMatrix;//Rotation matrix

	//-----------------------Internal technical methods----------------------------//
	bool facePointsViaShape(cv::Mat &frame, float(&shape)[2][68], cv::Point2f(&faceRectangle)[4]);//Method to get face location with the aid of landmark coordinates
	bool faceRectIncr(cv::Mat &frame, cv::Point2f(&prevFaceRectangle)[4]);//Method that calculates widen face rectangle
	double getDegrees(float leftX, float leftY, float rightX, float rightY);//Method to calculate angles
	bool calcGlobalCoordinates(cv::Mat &frame, float(&shape)[2][68], float(&globalShape)[2][68], cv::Point2f(&faceRectangle)[4], cv::Point2f(&faceGlobalRectangle)[4], cv::Point2f &coordinate, cv::Mat &affineMatrix);//Get all the coordinates after rotation back

};

