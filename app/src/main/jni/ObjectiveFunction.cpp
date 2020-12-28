//
// Created by admin on 07/01/2017.
//

#include "ObjectiveFunction.h"

ObjectiveFunction::ObjectiveFunction(ObjectiveFunctionHelper &phelper,
                                     OrthogonalProjectionModel &projection_model)
{
    ObjectiveFunction::helper = phelper;
    ObjectiveFunction::model = projection_model;
}

double ObjectiveFunction::operator()(const dlib::matrix<double> &arg) const
{
    //TODO: check that shape2d is set
    //dlib::matrix<double> meanShapes = ;//helper.faceModel3D.mean3D_shape;//
    //std::unordered_map<int, dlib::matrix<double> > blendshapes = helper.get_blendshapes();
    dlib::matrix<double> resids = model.get_residuals(arg, helper.get_mean3d(), helper.get_blendshapes(), shape2d);

    dlib::running_stats<double> rs;
    for (int i = 0; i < resids.nc(); ++i)
    {
        double score = dlib::length(dlib::colm(resids,i));
        rs.add(score);
    }

    return rs.mean();
}
void
ObjectiveFunction::extract2d_from_image(dlib::matrix<double> &image)
{
    ObjectiveFunction::shape2d = helper.get_y(image);
}
ObjectiveFunction::ObjectiveFunction() {}
