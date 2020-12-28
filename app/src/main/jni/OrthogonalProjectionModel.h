//
// Created by admin on 01/01/2017.
//

#ifndef SHAPE3D_ORTHOGONALPROJECTIONMODEL_H
#define SHAPE3D_ORTHOGONALPROJECTIONMODEL_H

#include "dlib/matrix.h"
#include <unordered_map>
class OrthogonalProjectionModel
{
public:
    OrthogonalProjectionModel(int n_blendshapes);
    OrthogonalProjectionModel();
    dlib::matrix<double> get_initial_parameters(dlib::matrix<double> xx, dlib::matrix<double> yy);
    dlib::matrix<double> get_residuals(const dlib::matrix<double> &params,
                                       const dlib::matrix<double> &mean3d,
                                       const std::unordered_map<int, dlib::matrix<double>> &blendshapes,
                                       const dlib::matrix<double> &y) const;


    dlib::matrix<double> get_full_shape3d(const dlib::matrix<double> &params,
                                          const dlib::matrix<double> &mean3d,
                                          const std::unordered_map<int, dlib::matrix<double> > &blendshapes) const;

    dlib::matrix<double> convert_mean_shape(const dlib::matrix<double> &params,
                                          const dlib::matrix<double> &mean3d,
                                          const std::unordered_map<int, dlib::matrix<double> > &blendshapes) const;

private:
    int n_params = 6;
    const dlib::matrix<double>
    rogrigues(const dlib::matrix<double> &rotation_vector) const;
    int n_blendshapes;
public:
    int
    get_n_blendshapes() const;
private:
    dlib::matrix<double> fun(const dlib::matrix<double> &x,
                             const std::unordered_map<int, dlib::matrix<double> > &blendshapes,
                             const dlib::matrix<double,
                                                0,
                                                0,
                                                dlib::default_memory_manager,
                                                dlib::row_major_layout> &params) const;
};

#endif //SHAPE3D_ORTHOGONALPROJECTIONMODEL_H
