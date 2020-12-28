//
// Created by admin on 07/01/2017.
//

#include "ObjectiveFunctionHelper.h"
#include "OrthogonalProjectionModel.h"

#ifndef SHAPE3D_OBEJCTIVEFUNCTION_H
#define SHAPE3D_OBEJCTIVEFUNCTION_H

class ObjectiveFunction
{
public:
    ObjectiveFunction(ObjectiveFunctionHelper &,
                      OrthogonalProjectionModel &projection_model);
    double operator()(const dlib::matrix<double> &arg) const;

    ObjectiveFunction();

private:
    ObjectiveFunctionHelper helper;

    dlib::matrix<double> shape2d;


public:
    void extract2d_from_image(dlib::matrix<double> &image);

    OrthogonalProjectionModel model;
};

#endif //SHAPE3D_OBEJCTIVEFUNCTION_H
