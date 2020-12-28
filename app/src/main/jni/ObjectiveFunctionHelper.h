//
// Created by admin on 06/01/2017.
//

#ifndef SHAPE3D_OBJECTIVEFUNCTION_H
#define SHAPE3D_OBJECTIVEFUNCTION_H

#include "3DFaceModel.h"
#include "Shape2D.h"
class ObjectiveFunctionHelper
{
public:
    ObjectiveFunctionHelper(FaceModel3D faceModel3D,
                                                     Shape2D shape2D);

    ObjectiveFunctionHelper();

    dlib::matrix<double> get_y(dlib::matrix<double> &image) ;
    const dlib::matrix<double> &
    get_mean3d() const;
    const std::unordered_map<int, dlib::matrix<double>> &
    get_blendshapes() const;
    FaceModel3D faceModel3D;
private:
    Shape2D shape2D;
};

#endif //SHAPE3D_OBJECTIVEFUNCTION_H
