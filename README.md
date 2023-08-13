[![CI](https://github.com/blemale/scaffeine/actions/workflows/ci.yml/badge.svg)](https://github.com/blemale/scaffeine/actions/workflows/ci.yml)
[![Coverage Status](https://img.shields.io/coveralls/github/blemale/scaffeine/master?style=flat-square)](https://coveralls.io/github/blemale/scaffeine?branch=master)
[![scaffeine Scala version support](https://index.scala-lang.org/blemale/scaffeine/scaffeine/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/blemale/scaffeine/scaffeine)
[![License](https://img.shields.io/github/license/blemale/scaffeine?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Known Vulnerabilities](https://img.shields.io/snyk/vulnerabilities/github/blemale/scaffeine?style=flat-square)](https://snyk.io/test/github/blemale/scaffeine?targetFile=build.sbt)

# Scaffeine

A thin Scala wrapper for Caffeine (https://github.com/ben-manes/caffeine).

Browse the [API docs](http://www.javadoc.io/doc/com.github.blemale/scaffeine_2.12) for the latest release.

## Motivations

[Caffeine](https://github.com/ben-manes/caffeine) is an awesome Java caching library.
It has an impressive [performance](https://github.com/ben-manes/caffeine/wiki/Benchmarks) and a neat Java 8 API.

However the API does not play very well with Scala.
So this is the thinner wrapper we can came with to make Caffeine easy and idiomatic to use in Scala.

## API

### Cache

```scala
"Cache" should "be created from Scaffeine builder" in {
    import com.github.blemale.scaffeine.{ Cache, Scaffeine }
    import scala.concurrent.duration._

    val cache: Cache[Int, String] =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(1.hour)
        .maximumSize(500)
        .build[Int, String]()

    cache.put(1, "foo")

    cache.getIfPresent(1) should be(Some("foo"))
    cache.getIfPresent(2) should be(None)
  }
```

### LoadingCache

```scala
"LoadingCache" should "be created from Scaffeine builder" in {
    import com.github.blemale.scaffeine.{ LoadingCache, Scaffeine }
    import scala.concurrent.duration._

    val cache: LoadingCache[Int, String] =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(1.hour)
        .maximumSize(500)
        .build((i: Int) => s"foo$i")

    cache.get(1) should be("foo1")
  }
```

### AsyncLoadingCache

```scala
 "AsyncLoadingCache" should "be created from Scaffeine builder with synchronous loader" in {
    import com.github.blemale.scaffeine.{ AsyncLoadingCache, Scaffeine }
    import scala.concurrent.duration._

    val cache: AsyncLoadingCache[Int, String] =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(1.hour)
        .maximumSize(500)
        .buildAsync((i: Int) => s"foo$i")

    whenReady(cache.get(1)) { value =>
      value should be("foo1")
    }
  }

"AsyncLoadingCache" should "be created from Scaffeine builder with asynchronous loader" in {
    import com.github.blemale.scaffeine.{ AsyncLoadingCache, Scaffeine }
    import scala.concurrent.duration._

    val cache: AsyncLoadingCache[Int, String] =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(1.hour)
        .maximumSize(500)
        .buildAsyncFuture((i: Int) => Future.successful(s"foo$i"))

    whenReady(cache.get(1)) { value =>
      value should be("foo1")
    }
  }
```

### Download

Download from [Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.blemale/scaffeine_2.12) or depend via SBT:

```sbt
"com.github.blemale" %% "scaffeine" % "<version>" % "compile"
```

Snapshots of the development version are available in
[Sonatype's snapshots repository](https://oss.sonatype.org/content/repositories/snapshots).
