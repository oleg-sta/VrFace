//
// Created by admin on 01/01/2017.
//

#ifndef SHAPE3D_SHAPEMODEL_H
#define SHAPE3D_SHAPEMODEL_H

#include "dlib/matrix.h"
#include <unordered_map>

class FaceModel3D
{
public:
    FaceModel3D(std::string models_path, int n);
    FaceModel3D();
    dlib::matrix<double> get_all_mean_shape3d() const;

    dlib::matrix<int>  getIdxs2D() const;

    const std::unordered_map<int, dlib::matrix<double> >& get_all_blendshapes() const;
    const std::unordered_map<int, dlib::matrix<double>>& get_blendshapes() const{ return blendshapes;};

    const dlib::matrix<double>& get_mean_shape3d() const {return mean3D_shape;};
private:
    dlib::matrix<double> calculate_mean_shape3d() const;
    std::unordered_map<int, dlib::matrix<double>> calculate_blendshapes() const;
    std::unordered_map<int, dlib::matrix<double> > all_blendshapes;
    std::unordered_map<int, dlib::matrix<double> > blendshapes;
    dlib::matrix<double> mean3D_shape;
    dlib::matrix<double> mean3D_shape_full;
    dlib::matrix<int> idxs2D;


    dlib::matrix<int> idxs3D;
};

#endif //SHAPE3D_SHAPEMODEL_H
