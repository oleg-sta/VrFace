#include "FaceFollower.h"

//------------------------------------------------------------------//
//------------------Constructor & Destructor------------------------//
//------------------------------------------------------------------//

FaceFollower::FaceFollower(std::string shapePredictorPath, std::string haarPath, std::string lbpFrontalPath, std::string lbpLeftPath, std::string lbpRightPath)
{
	//Initialize landmark detector and face finder
	landmarkDetector = LandmarkDetector(shapePredictorPath);
	faceFinder = FaceFinder(haarPath, lbpFrontalPath, lbpLeftPath, lbpRightPath);
}

FaceFollower::FaceFollower()
{
}

FaceFollower::~FaceFollower()
{
}


//------------------------------------------------------------------//
//--------------Methods to find new face & landmarks----------------//
//------------------------------------------------------------------//

//If it's first image search
bool FaceFollower::findNewLocation(cv::Mat &frame, cv::Point2f(&newPosition)[4], float(&newLandmarks)[2][68])
{
	//Rectangle for Viola Jones search
	bool faceFound = faceFinder.violaJonesScanLbp(frame, faceRectangle);

	//If we found face
	if (faceFound)
	{
		//Apply Dlib landmark detector
		landmarkDetector.applyDlib(frame, faceRectangle, newLandmarks);

		//Get face location via shape
		faceFound = facePointsViaShape(frame, newLandmarks, newPosition);

		return true;
	}
	else
	{
		return false;
	}
}

//If we have face position in the previous frame
bool FaceFollower::findNewLocation(cv::Mat &frame, cv::Point2f(&previousPosition)[4], cv::Point2f(&newPosition)[4], float(&newLandmarks)[2][68])
{
	bool success;

	//Widen face rectangle to get space for rotation
	success = faceRectIncr(frame, previousPosition);

	if (success)
	{
		//Get part of the frame to rotate
		searchArea.release();
		searchArea = frame(cv::Rect((int)widenFaceRectangle[0].x, (int)widenFaceRectangle[0].y, (int)widenFaceRectangle[1].x - (int)widenFaceRectangle[0].x, (int)widenFaceRectangle[2].y - (int)widenFaceRectangle[1].y));

		//Calculate rotation angle and get rotation matrix
		double angle = getDegrees(previousPosition[0].x, previousPosition[0].y, previousPosition[1].x, previousPosition[1].y);
		rotationMatrix.release();
		rotationMatrix = cv::getRotationMatrix2D(rotationPoint, -angle, 1.0);

		//Rotate
		rotatedArea.release();
		warpAffine(searchArea, rotatedArea, rotationMatrix, searchArea.size());

		//First we check if there is face inside the rotated image - use Viola Jones ROI search using previous face location
		success = faceFinder.violaJonesScanROILbp(rotatedArea, faceRectangle.width, tempRect);

		//If there is a face
		if (success)
		{
			//Find landmarks:
			landmarkDetector.applyDlib(rotatedArea, faceRectangle, tempFaceLandmarks);

			//Find new rectangle with face (we don't care about border conditions for a while 'cause after rotation and move back they may fix themselves)
			success = facePointsViaShape(rotatedArea, tempFaceLandmarks, tempFacePosition);

			//Update face and landmarks location (rotate back the coordinates)
			rotationMatrix.release();
			rotationMatrix = cv::getRotationMatrix2D(rotationPoint, angle, 1.0);//Get affine rotation matrix (anti-clockwise!)
			cv::Point2f point2f(widenFaceRectangle[0].x, widenFaceRectangle[0].y);
			success = calcGlobalCoordinates(frame, tempFaceLandmarks, newLandmarks, tempFacePosition, newPosition, point2f, rotationMatrix);

			if (success)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	else
	{
		return false;
	}
}


//------------------------------------------------------------------//
//----------------------Technical methods---------------------------//
//------------------------------------------------------------------//

//Method to calculate face rectangle points based on shape
bool FaceFollower::facePointsViaShape(cv::Mat &frame, float(&shape)[2][68], cv::Point2f(&faceRectangle)[4])
{
	float left_x, left_y, right_x, right_y;

	//Eyes coordinates
	left_x = (shape[0][36] + shape[0][37] + shape[0][38] + shape[0][39] + shape[0][40] + shape[0][41]) / 6.0;
	left_y = (shape[1][36] + shape[1][37] + shape[1][38] + shape[1][39] + shape[1][40] + shape[1][41]) / 6.0;
	right_x = (shape[0][42] + shape[0][43] + shape[0][44] + shape[0][45] + shape[0][46] + shape[0][47]) / 6.0;
	right_y = (shape[1][42] + shape[1][43] + shape[1][44] + shape[1][45] + shape[1][46] + shape[1][47]) / 6.0;

	//Distance between eyes
	float distEyes = sqrt((left_x - right_x)*(left_x - right_x) + (left_y - right_y)*(left_y - right_y));
	float distNoseChin = sqrt((shape[0][27] - shape[0][8])*(shape[0][27] - shape[0][8]) + (shape[1][27] - shape[1][8])*(shape[1][27] - shape[1][8]));
	//float faceWidth = 2.3*distEyes;
	float faceWidth = 1.25*distNoseChin;

	//Rotation angle
	float cosAlpha = (right_x - left_x) / distEyes;
	float sinAlpha = (left_y - right_y) / distEyes;

	//Angle inside frame
	float distCorner = sqrt(0.25 + noseFraction*noseFraction);
	float cosBetta = 0.5 / distCorner;
	float sinBetta = noseFraction / distCorner;

	//Coordinates
	//1
	faceRectangle[0].x = shape[0][27] - faceWidth*distCorner*(cosAlpha*cosBetta + sinAlpha*sinBetta);
	faceRectangle[0].y = shape[1][27] - faceWidth*distCorner*(sinBetta*cosAlpha - sinAlpha*cosBetta);

	//2
	faceRectangle[1].x = faceRectangle[0].x + faceWidth*cosAlpha;
	faceRectangle[1].y = faceRectangle[0].y - faceWidth*sinAlpha;

	//4
	faceRectangle[3].x = faceRectangle[0].x + faceWidth*sinAlpha;
	faceRectangle[3].y = faceRectangle[0].y + faceWidth*cosAlpha;

	//3
	faceRectangle[2].x = faceRectangle[3].x + faceWidth*cosAlpha;
	faceRectangle[2].y = faceRectangle[3].y - faceWidth*sinAlpha;

	//Check border conditions
	if (faceRectangle[0].x >= 0.0 && faceRectangle[0].x <= (float) frame.cols && faceRectangle[0].y >= 0.0 && faceRectangle[0].y <= (float) frame.rows && faceRectangle[1].x >= 0.0 && faceRectangle[1].x <= (float) frame.cols && faceRectangle[1].y >= 0.0 && faceRectangle[1].y <= (float) frame.rows && faceRectangle[2].x >= 0.0 && faceRectangle[2].x <= (float) frame.cols && faceRectangle[2].y >= 0.0 && faceRectangle[2].y <= (float) frame.rows && faceRectangle[3].x >= 0.0 && faceRectangle[3].x <= (float) frame.cols && faceRectangle[3].y >= 0.0 && faceRectangle[3].y <= (float) frame.rows)
	{
		return true;
	}
	else
	{
		return false;
	}
}

//Method to find coordinates of a wider rectangle (increase by factor incrFactor (e.g. 1.5))
bool FaceFollower::faceRectIncr(cv::Mat &frame, cv::Point2f(&prevFaceRectangle)[4])
{
	float faceWidth = sqrt((prevFaceRectangle[0].x - prevFaceRectangle[1].x)*(prevFaceRectangle[0].x - prevFaceRectangle[1].x) + (prevFaceRectangle[0].y - prevFaceRectangle[1].y)*(prevFaceRectangle[0].y - prevFaceRectangle[1].y));
	float centerX = (prevFaceRectangle[0].x + prevFaceRectangle[1].x + prevFaceRectangle[2].x + prevFaceRectangle[3].x) / 4.0;
	float centerY = (prevFaceRectangle[0].y + prevFaceRectangle[1].y + prevFaceRectangle[2].y + prevFaceRectangle[3].y) / 4.0;

	//1
	widenFaceRectangle[0].x = centerX - 0.5*faceWidth*widenPreviousFacePosition;
	widenFaceRectangle[0].y = centerY - 0.5*faceWidth*widenPreviousFacePosition;

	//2
	widenFaceRectangle[1].x = centerX + 0.5*faceWidth*widenPreviousFacePosition;
	widenFaceRectangle[1].y = centerY - 0.5*faceWidth*widenPreviousFacePosition;

	//3
	widenFaceRectangle[2].x = centerX + 0.5*faceWidth*widenPreviousFacePosition;
	widenFaceRectangle[2].y = centerY + 0.5*faceWidth*widenPreviousFacePosition;

	//4
	widenFaceRectangle[3].x = centerX - 0.5*faceWidth*widenPreviousFacePosition;
	widenFaceRectangle[3].y = centerY + 0.5*faceWidth*widenPreviousFacePosition;

	//Bring in checks for border conditions
	if (widenFaceRectangle[0].x < 0.0)
	{
		widenFaceRectangle[0].x = 0.0;
		widenFaceRectangle[3].x = 0.0;
	}

	if (widenFaceRectangle[1].x > (float) frame.cols)
	{
		widenFaceRectangle[1].x = (float) frame.cols;
		widenFaceRectangle[2].x = (float) frame.cols;
	}

	if (widenFaceRectangle[0].y < 0.0)
	{
		widenFaceRectangle[0].y = 0.0;
		widenFaceRectangle[1].y = 0.0;
	}

	if (widenFaceRectangle[2].y > (float) frame.rows)
	{
		widenFaceRectangle[2].y = (float) frame.rows;
		widenFaceRectangle[3].y = (float) frame.rows;
	}

	//Calculate rotation point
	rotationPoint.x = centerX - widenFaceRectangle[0].x;
	rotationPoint.y = centerY - widenFaceRectangle[0].y;

	//Coordinates of previous face after rotation
	x = (int) (rotationPoint.x - 0.5*faceWidth);
	y = (int) (rotationPoint.y - 0.5*faceWidth);
	width = (int) faceWidth;
	faceRectangle.x = x;
	faceRectangle.y = y;
	faceRectangle.width = width;
	faceRectangle.height = width;

	//Check that the rotation center inside the frame
	if (centerX < 0.0 || centerY < 0.0 || centerX > (float) frame.cols || centerY > (float) frame.rows)
	{
		return false;
	}
	else
	{
		return true;
	}
}

//Get degrees from coordinates
double FaceFollower::getDegrees(float leftX, float leftY, float rightX, float rightY)
{
	//Positive angle
	if (leftY > rightY)
	{
		return (double)(atan2(leftY - rightY, rightX - leftX) * 180 / 3.14159265359);
	}
	else
	{
		return -(double)(atan2(rightY - leftY, rightX - leftX) * 180 / 3.14159265359);
	}
}

//Calculate global coordinates based on rotation matrix and corner coordinates
bool FaceFollower::calcGlobalCoordinates(cv::Mat &frame, float(&shape)[2][68], float(&globalShape)[2][68], cv::Point2f(&faceRectangle)[4], cv::Point2f(&faceGlobalRectangle)[4], cv::Point2f &coordinate, cv::Mat &affineMatrix)
{
	//Access to elements of a Mat is fastest by pointers
	const double* ptr0 = affineMatrix.ptr<double>(0);
	const double* ptr1 = affineMatrix.ptr<double>(1);

	//Rotate and move coordinates
	for (int i = 0; i < 4; i++)
	{
		faceGlobalRectangle[i].x = faceRectangle[i].x * ptr0[0] + faceRectangle[i].y * ptr0[1] + ptr0[2];
		faceGlobalRectangle[i].y = faceRectangle[i].x * ptr1[0] + faceRectangle[i].y * ptr1[1] + ptr1[2];
		faceGlobalRectangle[i].x = faceGlobalRectangle[i].x + coordinate.x;
		faceGlobalRectangle[i].y = faceGlobalRectangle[i].y + coordinate.y;
	}

	for (int i = 0; i < 68; i++)
	{
		globalShape[0][i] = shape[0][i] * ptr0[0] + shape[1][i] * ptr0[1] + ptr0[2];
		globalShape[1][i] = shape[0][i] * ptr1[0] + shape[1][i] * ptr1[1] + ptr1[2];
		globalShape[0][i] = globalShape[0][i] + coordinate.x;
		globalShape[1][i] = globalShape[1][i] + coordinate.y;
	}

	//We return false if the center of the face is not in the frame
	float centerX = (faceGlobalRectangle[0].x + faceGlobalRectangle[1].x + faceGlobalRectangle[2].x + faceGlobalRectangle[3].x) / 4.0;
	float centerY = (faceGlobalRectangle[0].y + faceGlobalRectangle[1].y + faceGlobalRectangle[2].y + faceGlobalRectangle[3].y) / 4.0;

	if (centerX < 0.0 || centerY < 0.0 || centerX > (float) frame.cols || centerY > (float) frame.rows)
	{
		return false;
	}
	else
	{
		return true;
	}
}


//------------------------------------------------------------------//
//-----------------------Drawing methods----------------------------//
//------------------------------------------------------------------//

//Method to draw face rectangle in an image
void FaceFollower::drawRectangle(cv::Mat &frame, cv::Point2f (&rectPoints)[4])
{
	for (int j = 0; j < 4; j++)
	{
		line(frame, rectPoints[j], rectPoints[(j + 1) % 4], cv::Scalar(255, 0, 255), 8, 8);
	}
}

//Method to draw face landmarks in an image
void FaceFollower::drawAllLandmarks(cv::Mat & frame, float(&landmarks)[2][68])
{
	for (int i = 0; i < 68; i++)
	{
		drawPoint(frame, (int)landmarks[0][i], (int)landmarks[1][i], 4, 2);
	}
}

//Draw point
void FaceFollower::drawPoint(cv::Mat & frame, int x, int y, int radius, int thinkness)
{
	circle(frame, cv::Point(x, y), radius, cv::Scalar(255, 0, 255), thinkness);
}