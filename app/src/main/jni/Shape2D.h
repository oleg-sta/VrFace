//
// Created by admin on 06/01/2017.
//

#include "dlib/image_processing/frontal_face_detector.h"
#include "dlib/image_processing.h"

#ifndef SHAPE3D_SHAPE2D_H
#define SHAPE3D_SHAPE2D_H

class Shape2D
{
public:
    Shape2D();
    dlib::matrix<double> get_shape2d(dlib::matrix<double>& image) ;
};

#endif //SHAPE3D_SHAPE2D_H
