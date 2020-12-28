//
// Created by admin on 04/02/2017.
//

#include "dlib/assert.h"
#include "dlib/matrix/matrix_exp.h"

#ifndef SHAPE3D_FORWARDDIFFERENCE_H
#define SHAPE3D_FORWARDDIFFERENCE_H

using namespace dlib;
#endif //SHAPE3D_FORWARDDIFFERENCE_H
template <typename funct>
class forward_differences
{
public:
	forward_differences(const funct& f_, double eps_ = 1e-7) : f(f_), eps(eps_){}

	template <typename T>
	typename T::matrix_type operator()(const T& x) const
	{
		// T must be some sort of dlib matrix
		COMPILE_TIME_ASSERT(is_matrix<T>::value);
		const double f_x = f(x);
		typename T::matrix_type der(x.size());
		typename T::matrix_type e(x);
		for (long i = 0; i < x.size(); ++i)
		{
			const double old_val = e(i);

			e(i) += eps;
			const double delta_plus = f(e);
			der(i) = (delta_plus - f_x)/(eps);

			// and finally restore the old value of this element
			e(i) = old_val;
		}

		return der;
	}

	template <typename T, typename U>
	typename U::matrix_type operator()(const T& item, const U& x) const
	{
		// U must be some sort of dlib matrix
		COMPILE_TIME_ASSERT(is_matrix<U>::value);

		typename U::matrix_type der(x.size());
		typename U::matrix_type e(x);
		const double f_x = f(item,x);
		for (long i = 0; i < x.size(); ++i)
		{
			const double old_val = e(i);

			e(i) += eps;
			const double delta_plus = f(item,e);
			der(i) = (delta_plus - f_x)/(eps);

			// and finally restore the old value of this element
			e(i) = old_val;
		}

		return der;
	}


	double operator()(const double& x) const
	{
		return (f(x+eps)-f(x))/(eps);
	}

private:
	const funct& f;
	const double eps;
};

template <typename funct>
const forward_differences<funct> forward_derivative(const funct& f) { return forward_differences<funct>(f); }
template <typename funct>
const forward_differences<funct> forward_derivative(const funct& f, double eps)
{
	DLIB_ASSERT (
		eps > 0,
		"\tforward_differences derivative(f,eps)"
			<< "\n\tYou must give an epsilon > 0"
			<< "\n\teps:     " << eps
	);
	return forward_differences<funct>(f,eps);
}