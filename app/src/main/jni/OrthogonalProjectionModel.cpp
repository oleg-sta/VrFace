//
// Created by admin on 01/01/2017.
//


#include "OrthogonalProjectionModel.h"
OrthogonalProjectionModel::OrthogonalProjectionModel(int nblendshapes) : n_blendshapes(nblendshapes)
{
    n_params+=nblendshapes;
}
dlib::matrix<double>
OrthogonalProjectionModel::get_initial_parameters(dlib::matrix<double> xx, dlib::matrix<double> yy)
{
    dlib::matrix<double> out;
    out.set_size(n_params,1);
    dlib::matrix<double> mean3dshape = dlib::trans(xx);
    dlib::matrix<double> shape2d = dlib::trans(yy);

    dlib::matrix<double> shape3dcentered ;
    //TODO: extract normalization to method
    shape3dcentered.set_size(mean3dshape.nr(),mean3dshape.nc());
    for (int i = 0; i < mean3dshape.nc(); ++i)
    {
       dlib::matrix<double > cur_column= dlib::colm(mean3dshape,i);
       dlib::set_colm(shape3dcentered,i) = cur_column - dlib::mean(cur_column);
    }
    dlib::matrix<double> shape2dcentered;
    shape2dcentered.set_size(shape2d.nr(),shape2d.nc());
    for (int i = 0; i < shape2d.nc(); ++i)
    {
        dlib::matrix<double > cur_column= dlib::colm(shape2d,i);
        dlib::set_colm(shape2dcentered,i) = cur_column - dlib::mean(cur_column);
    }
    double norm2d = sqrt(dlib::sum(dlib::squared(shape2dcentered)));//norm of matrix

    dlib::matrix<double> shape3d_centered_1_2 = dlib::subm(shape3dcentered,0,0,shape3dcentered.nr(),2);

    double norm3d = sqrt(dlib::sum(dlib::squared(shape3d_centered_1_2)));
    out(0, 0) = norm2d/ norm3d;
    out(1,0) = 0;
    out(2,0) = 0;
    out(3,0) = 0;
    out(4,0) = dlib::mean(dlib::colm(shape2d,0)) - dlib::mean(dlib::colm(mean3dshape,0));
    out(5,0) = dlib::mean(dlib::colm(shape2d,1)) - dlib::mean(dlib::colm(mean3dshape,1));
    for (int j = 6; j < out.nr(); ++j)
    {
        out(j,0) = 0;
    }
    return out;
}
dlib::matrix<double>
OrthogonalProjectionModel::get_residuals(const dlib::matrix<double> &params,
                                         const dlib::matrix<double> &mean3d,
                                         const std::unordered_map<int, dlib::matrix<double> > &blendshapes,
                                         const dlib::matrix<double> &y) const
{
    //TODO: check arguments consistency
    dlib::matrix<double> r = y - OrthogonalProjectionModel::fun(mean3d, blendshapes, params);
    return r;
}
dlib::matrix<double>
OrthogonalProjectionModel::fun(const dlib::matrix<double> &mean3d,
                               const std::unordered_map<int, dlib::matrix<double> > &blendshapes,
                               const dlib::matrix<double> &params) const
{

    dlib::matrix<double> full_shape = OrthogonalProjectionModel::get_full_shape3d(params,mean3d,blendshapes);
    return dlib::subm(full_shape,0,0,2,full_shape.nc());

}
const dlib::matrix<double>
OrthogonalProjectionModel::rogrigues(const dlib::matrix<double,
                                                        0,
                                                        0,
                                                        dlib::default_memory_manager,
                                                        dlib::row_major_layout> &rotation_vector) const
{
    //TODO: assert that rotation vector is 3x1
    double theta =  sqrt(dlib::sum(dlib::squared(rotation_vector)));
    dlib::matrix<double> r = rotation_vector;
    if(theta != 0)
        r = rotation_vector/theta;
    dlib::matrix<double,3,3> transformation_matrix = dlib::zeros_matrix<double>(3,3);
    transformation_matrix(0,1) = - r(2,0);
    transformation_matrix(0,2) =  r(1,0);
    transformation_matrix(1,0) =  r(2,0);
    transformation_matrix(1,2) =  -r(0,0);
    transformation_matrix(2,0) =  -r(1,0);
    transformation_matrix(2,1) =  r(0,0);

    double cos_theta = cos(theta);
    dlib::matrix<double,3,3> R = dlib::identity_matrix<double>(3)*cos_theta + r * dlib::trans(r)*(1 - cos_theta) +  transformation_matrix * sin(theta);

    return R;
}
OrthogonalProjectionModel::OrthogonalProjectionModel() :n_blendshapes(14){}
dlib::matrix<double>
OrthogonalProjectionModel::convert_mean_shape(const dlib::matrix<double> &params,
                                              const dlib::matrix<double> &mean3d,
                                              const std::unordered_map<int, dlib::matrix<double> > &blendshapes) const
{
    dlib::matrix<double> shape3D;
    dlib::matrix<double> w = dlib::rowm(params, dlib::range(6,params.nr()-1));
    dlib::matrix<double> sum_w = dlib::zeros_matrix<double>(mean3d.nr(),mean3d.nc());
    for (int i = 0; i < blendshapes.size(); ++i)
    {
        dlib::matrix<double> current_blendshape = blendshapes.at(i) * w(i,0);
        sum_w += current_blendshape;
    }
    shape3D = mean3d + sum_w;
    return shape3D;
}
dlib::matrix<double>
OrthogonalProjectionModel::get_full_shape3d(const dlib::matrix<double> &params,
                                            const dlib::matrix<double> &mean3d,
                                            const std::unordered_map<int, dlib::matrix<double> > &blendshapes) const
{
    double s = params(0);
    dlib::matrix<double,3,1> r = dlib::rowm(params, dlib::range(1,3));
    dlib::matrix<double,2,1> t_tmp = dlib::rowm(params, dlib::range(4,5));
    dlib::matrix<double,3,1> t;
    dlib::set_rowm(t,dlib::range(0,1)) = t_tmp;
    t(2,0) = 0;
    dlib::matrix<double> shape3D = OrthogonalProjectionModel::convert_mean_shape(params, mean3d, blendshapes);
    //TODO: get Rodrigues transformation from OpenCV
    dlib::matrix<double> R = OrthogonalProjectionModel::rogrigues(r);
    dlib::matrix<double> projected = R * shape3D * s;
    for (int j = 0; j < projected.nc(); ++j)
    {
        dlib::matrix<double> curr_column = dlib::colm(projected, j) + t;
        dlib::set_colm(projected,j) = curr_column;
    }
    return projected;
}
int
OrthogonalProjectionModel::get_n_blendshapes() const
{
    return n_blendshapes;
};
