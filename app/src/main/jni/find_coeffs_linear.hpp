#pragma once

#ifndef FIND_COEFFS_LINEAR_HPP_
#define FIND_COEFFS_LINEAR_HPP_

#include "dlib/image_processing.h"
#include "dlib/image_io.h"


#include "opencv2/core/core.hpp"
#include <iostream>
#include <vector>
#include <cassert>


dlib::matrix<double> find_coeffs_linear(eos::fitting::ScaledOrthoProjectionParameters& pars, std::vector<cv::Vec2f> image_points, const dlib::matrix<double>& mean3D_shape, const std::unordered_map<int, dlib::matrix<double>>& blendshapes)
{
	using cv::Mat;
	const int num_coeffs_real = static_cast<int>(blendshapes.size());
	const int num_coeffs = static_cast<int>(blendshapes.size());
	const int num_points = image_points.size();
	double s = pars.s;
	dlib::matrix<double, 3, 3> R = dlib::zeros_matrix<double>(3, 3);
	for (int i = 0; i < 3; i++)
	{
	  for (int j = 0; j < 3; j++)
	  {
	    R(i, j) = pars.R.at<float>(i, j);
	  }
	}

	Mat A = Mat::zeros(2 * num_points, num_coeffs, CV_32FC1);
	int row_index = 0;
	for (int i = 0; i < num_coeffs; ++i)
	{
	  dlib::matrix<double> blendshape = blendshapes.at(i);
	  //std::cout << "blendshape i " << dlib::trans(blendshape) << std::endl;
	  dlib::matrix<double> projected = R * blendshape * s;
	  for (int j = 0; j < num_points; j++)
	  {
	    A.at<float>(j * 2, i) = projected(0, j);
	    A.at<float>(j * 2 + 1, i) = projected(1, j);
	  }

	}

	dlib::matrix<double> shaped3dTo2d = R * mean3D_shape * s;
	//std::cout << "mean3D_shape " << mean3D_shape.nr() << " " << mean3D_shape.nc() << std::endl;
	//std::cout << "mean3D_shape " << mean3D_shape << std::endl;
	//std::cout << "mean3D_shape " << shaped3dTo2d << std::endl;
	Mat b(2 * num_points, 1, CV_32FC1);
	row_index = 0;
	for (int i = 0; i < image_points.size(); ++i)
	{
		b.at<float>(row_index++) = image_points[i][0] - pars.tx - shaped3dTo2d(0, i);
		b.at<float>(row_index++) = image_points[i][1] - pars.ty - shaped3dTo2d(1, i);
	}

	Mat k; // resulting affine matrix (num_coeffs x 1)
	
	//std::cout << "A " << A << std::endl;
	//std::cout << "b " << b << std::endl;
	bool solved = cv::solve(A, b, k, cv::DECOMP_SVD);

	
	dlib::matrix<double> ret = dlib::zeros_matrix<double>(num_coeffs_real, 1);
	for (int i = 0; i < k.rows; i++)
	{
	  //std::cout << "i " << i << " " << k.at<float>(i) << std::endl;
	  ret(i)=k.at<float>(i);
	}
	return ret;
};

#endif /* FIND_COEFFS_LINEAR_HPP__ */
