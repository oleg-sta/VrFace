#pragma once

#ifndef ORTHOGRAPHICCAMERAESTIMATIONLINEAR_HPP_
#define ORTHOGRAPHICCAMERAESTIMATIONLINEAR_HPP_

#include "opencv2/core/core.hpp"
#include <iostream>
#include <vector>
#include <cassert>

namespace eos {
	namespace fitting {

/**
 * Parameters of an estimated scaled orthographic projection.
 */
struct ScaledOrthoProjectionParameters {
	cv::Mat R;
	double tx, ty;
	double s;
};

/**
 * Estimates the parameters of a scaled orthographic projection.
 *
 * Given a set of 2D-3D correspondences, this algorithm estimates rotation,
 * translation (in x and y) and a scaling parameters of the scaled orthographic
 * projection model using a closed-form solution. It does so by first computing
 * an affine camera matrix using algorithm [1], and then finds the closest
 * orthonormal matrix to the estimated affine transform using SVD.
 * This algorithm follows the original implementation [2] of William Smith,
 * University of York.
 *
 * Requires >= 4 corresponding points.
 *
 * [1]: Gold Standard Algorithm for estimating an affine camera matrix from
 * world to image correspondences, Algorithm 7.2 in Multiple View Geometry,
 * Hartley & Zisserman, 2nd Edition, 2003.
 * [2]: https://github.com/waps101/3DMM_edges/blob/master/utils/POS.m
 *
 * @param[in] image_points A list of 2D image points.
 * @param[in] model_points Corresponding points of a 3D model.
 * @param[in] is_viewport_upsidedown Flag to set whether the viewport of the image points is upside-down (e.g. as in OpenCV).
 * @param[in] viewport_height Height of the viewport of the image points (needs to be given if is_viewport_upsidedown == true).
 * @return Rotation, translation and scaling of the estimated scaled orthographic projection.
 */
ScaledOrthoProjectionParameters estimate_orthographic_projection_linear(std::vector<cv::Vec2f> image_points, std::vector<cv::Vec4f> model_points)
{
	using cv::Mat;
	assert(image_points.size() == model_points.size());
	assert(image_points.size() >= 4); // Number of correspondence points given needs to be equal to or larger than 4

	
	const int num_correspondences = static_cast<int>(image_points.size());

	Mat A = Mat::zeros(2 * num_correspondences, 8, CV_32FC1);
	int row_index = 0;
	for (int i = 0; i < model_points.size(); ++i)
	{
		Mat p = Mat(model_points[i]).t();
		p.copyTo(A.row(row_index++).colRange(0, 4)); // even row - copy to left side (first row is row 0)
		p.copyTo(A.row(row_index++).colRange(4, 8)); // odd row - copy to right side
	} // 4th coord (homogeneous) is already 1

	Mat b(2 * num_correspondences, 1, CV_32FC1);
	row_index = 0;
	for (int i = 0; i < image_points.size(); ++i)
	{
		b.at<float>(row_index++) = image_points[i][0];
		b.at<float>(row_index++) = image_points[i][1];
	}

	Mat k; // resulting affine matrix (8x1)
	bool solved = cv::solve(A, b, k, cv::DECOMP_SVD);

	const Mat R1 = k.rowRange(0, 3);
	const Mat R2 = k.rowRange(4, 7);
	const float sTx = k.at<float>(3);
	const float sTy = k.at<float>(7);
	const auto s = (cv::norm(R1) + cv::norm(R2)) / 2.0;
	Mat r1 = R1 / cv::norm(R1);
	Mat r2 = R2 / cv::norm(R2);
	Mat r3 = r1.cross(r2);
	Mat R;
	r1 = r1.t();
	r2 = r2.t();
	r3 = r3.t();
	R.push_back(r1);
	R.push_back(r2);
	R.push_back(r3);
	// Set R to the closest orthonormal matrix to the estimated affine transform:
	Mat S, U, Vt;
	cv::SVDecomp(R, S, U, Vt);
	Mat R_ortho = U * Vt;
	// The determinant of R must be 1 for it to be a valid rotation matrix
	if (cv::determinant(R_ortho) < 0)
	{
		U.row(2) = -U.row(2); // not sure this works...
		R_ortho = U * Vt;
	}

	// Remove the scale from the translations:
	const auto t1 = sTx;// / s;
	const auto t2 = sTy;// / s;

	return ScaledOrthoProjectionParameters{ R_ortho, t1, t2, s };
};

	} /* namespace fitting */
} /* namespace eos */

#endif /* ORTHOGRAPHICCAMERAESTIMATIONLINEAR_HPP_ */
