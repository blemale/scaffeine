package com.github.blemale.scaffeine

import org.scalatest.{ Matchers, WordSpec }

class FunctionConvertersSpec extends WordSpec with Matchers {
  import FunctionConverters._

  "FunctionConverters" should {
    "convert a function to a Java supplier" in {
      asJavaSupplier(() => "foo").get() shouldEqual "foo"
    }

    "convert a function to a Java function" in {
      asJavaFunction((s: String) => s).apply("foo") shouldEqual "foo"
    }

    "convert a function to a Java bi-function" in {
      asJavaBiFunction((s1: String, s2: String) => s1 + s2).apply("foo", "bar") shouldEqual "foobar"
    }
  }

}
