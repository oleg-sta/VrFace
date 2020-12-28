//
// Created by admin on 06/01/2017.
//

#include "Shape2D.h"

dlib::matrix<double> Shape2D::get_shape2d(dlib::matrix<double> &image)  {
    dlib::matrix<double> result;
    result.set_size(2, image.nc());
    for (int i = 0; i < image.nc(); ++i)
    {
        result(0,i) = image(0, i);
        result(1,i) = image(1, i);
    }
    return result;

}
Shape2D::Shape2D() {}
