//
// Created by admin on 06/01/2017.
//

#include "ObjectiveFunctionHelper.h"
ObjectiveFunctionHelper::ObjectiveFunctionHelper(FaceModel3D faceModel3D,
                                                 Shape2D shape2D) : faceModel3D(faceModel3D), shape2D(shape2D)
{

}
dlib::matrix<double>
ObjectiveFunctionHelper::get_y(dlib::matrix<double> &image)
{
    dlib::matrix<int> idx2d = faceModel3D.getIdxs2D();
    dlib::matrix<double> shape = shape2D.get_shape2d(image);
    dlib::matrix<double> res;
    res.set_size(2,idx2d.nr());
    for (int i = 0; i < idx2d.nr(); ++i)
    {
        res(0,i) = shape(0,idx2d(i,0));
        res(1,i) = shape(1,idx2d(i,0));
    }
    return res;
}
const dlib::matrix<double> &
ObjectiveFunctionHelper::get_mean3d() const
{
    return faceModel3D.get_mean_shape3d();
}
ObjectiveFunctionHelper::ObjectiveFunctionHelper() {}
const std::unordered_map<int, dlib::matrix<double>> &
ObjectiveFunctionHelper::get_blendshapes() const
{
    return faceModel3D.get_blendshapes();
}
