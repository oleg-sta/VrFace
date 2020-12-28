//
// Created by admin on 08/01/2017.
//

#ifndef SHAPE3D_TESTOBJECTIVEFUNCTION_H
#define SHAPE3D_TESTOBJECTIVEFUNCTION_H

#include "dlib/matrix.h"
class TestObjectiveFunction
{
    public:
    TestObjectiveFunction();
    double operator() ( const dlib::matrix<double>& arg) const
    {
        const double x = arg(0);
        const double y = arg(1);

        // compute Rosenbrock's function and return the result
        return 100.0*pow(y - x*x,2) + pow(1 - x,2);
    }
};

#endif //SHAPE3D_TESTOBJECTIVEFUNCTION_H
