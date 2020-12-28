//
// Created by admin on 01/01/2017.
//

#include "3DFaceModel.h"
FaceModel3D::FaceModel3D(std::string path_to_models, int n_blendshapes)
{
    std::string mean3DPath = path_to_models + "//meanShape3D.csv";
    std::string idxs3DPath = path_to_models + "//idx3d.csv";
    std::string idxs2DPath = path_to_models + "//idx2d.csv";

    std::string blendshapesPath = path_to_models + "//blendshapes//";
    std::streambuf *cinbuf = std::cin.rdbuf();
    std::ifstream shape3d(mean3DPath);
    std::cin.rdbuf(shape3d.rdbuf());
    std::cin >> FaceModel3D::mean3D_shape_full;



    std::ifstream idx3d(idxs3DPath);
    std::cin.rdbuf(idx3d.rdbuf());
    std::cin >> FaceModel3D::idxs3D;

    std::ifstream idx2d(idxs2DPath);
    std::cin.rdbuf(idx2d.rdbuf());
    std::cin >> FaceModel3D::idxs2D;

    for (int i = 0; i < n_blendshapes; ++i)
    {
        std::ostringstream oss;
        oss << blendshapesPath << "blendshape_" << i << ".csv";
        std::string current_blendshape = oss.str();
        std::ifstream blendshapes(current_blendshape);
        dlib::matrix<double> current_matrix;
        std::cin.rdbuf(blendshapes.rdbuf());
        std::cin >> current_matrix;
        FaceModel3D::all_blendshapes[i] = current_matrix;

    }
    std::cin.rdbuf(cinbuf);
    FaceModel3D::blendshapes = calculate_blendshapes();
    mean3D_shape = calculate_mean_shape3d();
}
dlib::matrix<int>
FaceModel3D::getIdxs2D()const
{
    return idxs2D;
}
/*
dlib::matrix<int> &
FaceModel3D::getIdxs3D() const
{
    return idxs3D;
}*/
dlib::matrix<double>
FaceModel3D::calculate_mean_shape3d() const
{
    dlib::matrix<double> res;
    res.set_size(mean3D_shape_full.nr(),idxs3D.nr());
    for (int i = 0; i < idxs3D.nr(); ++i)
    {
        for (int j = 0; j < mean3D_shape_full.nr(); ++j)
        {
            res(j, i) = mean3D_shape_full(j, idxs3D(i, 0));
        }
    }
    return res;
}
std::unordered_map<int, dlib::matrix<double>>
FaceModel3D::calculate_blendshapes() const
{
    std::unordered_map<int,dlib::matrix<double> > cur_blendshapes;
    for(int n = 0; n < all_blendshapes.size(); n++)
    {
        dlib::matrix<double> blendshape_return;
        blendshape_return.set_size(all_blendshapes.at(n).nr(), idxs3D.nr());
        for (int i = 0; i < idxs3D.nr(); ++i)
        {
            for (int j = 0; j < all_blendshapes.at(n).nr(); ++j)
            {
                blendshape_return(j, i) = all_blendshapes.at(n)(j, idxs3D(i, 0));
            }
        }
        cur_blendshapes[n] = blendshape_return;
    }
    return cur_blendshapes;
}
FaceModel3D::FaceModel3D() {}
const std::unordered_map<int, dlib::matrix<double> >&
FaceModel3D::get_all_blendshapes() const
{
    return all_blendshapes;
}
dlib::matrix<double>
FaceModel3D::get_all_mean_shape3d() const
{
    return mean3D_shape_full;
}

