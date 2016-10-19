package com.github.blemale.scaffeine

import java.util.function.{ BiFunction, Function, Supplier }

private[scaffeine] object FunctionConverters {

  def asJavaSupplier[R](f: () => R): Supplier[R] =
    new Supplier[R] {
      override def get(): R = f()
    }

  def asJavaFunction[T, R](f: T => R): Function[T, R] =
    new Function[T, R] {
      override def apply(t: T): R = f(t)
    }

  def asJavaBiFunction[T, U, R](f: (T, U) => R): BiFunction[T, U, R] =
    new BiFunction[T, U, R] {
      override def apply(t: T, u: U): R = f(t, u)
    }
}
