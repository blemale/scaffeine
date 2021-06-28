package com.github.blemale.scaffeine

import com.github.benmanes.caffeine
import com.github.benmanes.caffeine.cache.Scheduler
import com.github.benmanes.caffeine.cache.stats.StatsCounter
import org.scalatest.PrivateMethodTester
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.concurrent.Executor
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ScaffeineSpec extends AnyWordSpec with Matchers with PrivateMethodTester {

  "Scaffeine" should {
    "create builder" in {
      val scaffeine: Scaffeine[Any, Any] = Scaffeine()

      scaffeine shouldBe a[Scaffeine[_, _]]
    }

    "create builder from spec" in {
      val scaffeine: Scaffeine[Any, Any] =
        Scaffeine(caffeine.cache.CaffeineSpec.parse("initialCapacity=10"))

      scaffeine shouldBe a[Scaffeine[_, _]]
    }

    "create builder from spec as string" in {
      val scaffeine: Scaffeine[Any, Any] = Scaffeine("initialCapacity=10")

      scaffeine shouldBe a[Scaffeine[_, _]]
    }

    "set initial capacity" in {
      val scaffeine = Scaffeine().initialCapacity(99)

      val getInitialCapacity = PrivateMethod[Int](Symbol("getInitialCapacity"))
      val initialCapacity =
        scaffeine.underlying invokePrivate getInitialCapacity()

      initialCapacity should be(99)
    }

    "set executor" in {
      val scaffeine = Scaffeine().executor(ExecutionContext.global)

      val getExecutor = PrivateMethod[Executor](Symbol("getExecutor"))
      val executor    = scaffeine.underlying invokePrivate getExecutor()

      executor should be(ExecutionContext.global)
    }

    "set maximum size" in {
      val scaffeine = Scaffeine().maximumSize(99)

      val getMaximumWeight = PrivateMethod[Long](Symbol("getMaximum"))
      val maximumSize      = scaffeine.underlying invokePrivate getMaximumWeight()

      maximumSize should be(99L)
    }

    "set maximum weight" in {
      val scaffeine =
        Scaffeine().maximumWeight(99).weigher((_: Any, _: Any) => 1)

      val getMaximumWeight = PrivateMethod[Long](Symbol("getMaximum"))
      val maximumWeight    = scaffeine.underlying invokePrivate getMaximumWeight()

      maximumWeight should be(99L)
    }

    "set weigher" in {
      val scaffeine = Scaffeine().weigher((_: Any, _: Any) => 1)

      val isWeighted = PrivateMethod[Boolean](Symbol("isWeighted"))
      val weighted   = scaffeine.underlying invokePrivate isWeighted()

      weighted should be(true)
    }

    "set weak keys" in {
      val scaffeine = Scaffeine().weakKeys()

      val isStrongKeys = PrivateMethod[Boolean](Symbol("isStrongKeys"))
      val strongKeys   = scaffeine.underlying invokePrivate isStrongKeys()

      strongKeys should be(false)
    }

    "set weak values" in {
      val scaffeine = Scaffeine().weakValues()

      val isWeakValues = PrivateMethod[Boolean](Symbol("isWeakValues"))
      val weakValues   = scaffeine.underlying invokePrivate isWeakValues()

      weakValues should be(true)
    }

    "set soft values" in {
      val scaffeine = Scaffeine().softValues()

      val isStrongValues = PrivateMethod[Boolean](Symbol("isStrongValues"))
      val isWeakValues   = PrivateMethod[Boolean](Symbol("isWeakValues"))

      val strongValues = scaffeine.underlying invokePrivate isStrongValues()
      val weakValues   = scaffeine.underlying invokePrivate isWeakValues()

      strongValues should be(false)
      weakValues should be(false)
    }

    "set expire after write" in {
      val scaffeine = Scaffeine().expireAfterWrite(10.minutes)

      val getExpiresAfterWriteNanos =
        PrivateMethod[Long](Symbol("getExpiresAfterWriteNanos"))
      val expiresAfterWriteNanos =
        scaffeine.underlying invokePrivate getExpiresAfterWriteNanos()

      expiresAfterWriteNanos should be(10.minutes.toNanos)
    }

    "set expire after access" in {
      val scaffeine = Scaffeine().expireAfterAccess(10.minutes)

      val getExpiresAfterAccessNanos =
        PrivateMethod[Long](Symbol("getExpiresAfterAccessNanos"))
      val expiresAfterAccessNanos =
        scaffeine.underlying invokePrivate getExpiresAfterAccessNanos()

      expiresAfterAccessNanos should be(10.minutes.toNanos)
    }

    "set expire after" in {
      val scaffeine = Scaffeine().expireAfter(
        create = (_: Any, _: Any) => 10.minutes,
        update = (_: Any, _: Any, _) => 20.minutes,
        read = (_: Any, _: Any, _) => 30.minutes
      )

      val getExpiry =
        PrivateMethod[caffeine.cache.Expiry[Any, Any]](Symbol("getExpiry"))
      val expiry = scaffeine.underlying invokePrivate getExpiry(false)

      expiry.expireAfterCreate(null, null, 0) should be(10.minutes.toNanos)
      expiry.expireAfterUpdate(null, null, 0, 0) should be(20.minutes.toNanos)
      expiry.expireAfterRead(null, null, 0, 0) should be(30.minutes.toNanos)
    }

    "set refresh after write" in {
      val scaffeine = Scaffeine().refreshAfterWrite(10.minutes)

      val getRefreshAfterWriteNanos =
        PrivateMethod[Long](Symbol("getRefreshAfterWriteNanos"))
      val refreshAfterWriteNanos =
        scaffeine.underlying invokePrivate getRefreshAfterWriteNanos()

      refreshAfterWriteNanos should be(10.minutes.toNanos)
    }

    "set ticker" in {
      val scaffeine = Scaffeine().ticker(caffeine.cache.Ticker.disabledTicker())

      val getTicker = PrivateMethod[caffeine.cache.Ticker](Symbol("getTicker"))
      val ticker    = scaffeine.underlying invokePrivate getTicker()

      ticker should be(caffeine.cache.Ticker.disabledTicker())
    }

    "set removal listener" in {
      val scaffeine =
        Scaffeine().removalListener((_: Any, _: Any, _) => println("removed"))

      val getRemovalListener =
        PrivateMethod[caffeine.cache.RemovalListener[Any, Any]](
          Symbol("getRemovalListener")
        )
      val removalListener =
        scaffeine.underlying invokePrivate getRemovalListener(false)

      removalListener shouldNot be(null)
    }

    "set eviction listener" in {
      val scaffeine =
        Scaffeine().evictionListener((_: Any, _: Any, _) => println("evicted"))

      val getEvictionListener =
        PrivateMethod[caffeine.cache.RemovalListener[Any, Any]](
          Symbol("getEvictionListener")
        )
      val evictionListener =
        scaffeine.underlying invokePrivate getEvictionListener(false)

      evictionListener shouldNot be(null)
    }

    "set record stats" in {
      val scaffeine = Scaffeine().recordStats()

      val isRecordingStats = PrivateMethod[Boolean](Symbol("isRecordingStats"))
      val recordingStats   = scaffeine.underlying invokePrivate isRecordingStats()

      recordingStats should be(true)
    }

    "set record stats supplier" in {
      val scaffeine =
        Scaffeine().recordStats(() => StatsCounter.disabledStatsCounter())

      val isRecordingStats = PrivateMethod[Boolean](Symbol("isRecordingStats"))
      val recordingStats   = scaffeine.underlying invokePrivate isRecordingStats()

      recordingStats should be(true)
    }

    "set scheduler" in {
      val scaffeine = Scaffeine().scheduler(Scheduler.systemScheduler())

      val getScheduler = PrivateMethod[Scheduler](Symbol("getScheduler"))
      val scheduler    = scaffeine.underlying invokePrivate getScheduler()

      scheduler should be(Scheduler.systemScheduler())
    }

    "build cache" in {
      val cache = Scaffeine().build[Int, Int]()

      cache shouldBe a[Cache[_, _]]
    }

    "build loading cache from loading function" in {
      val cache = Scaffeine().build[Int, Int]((i: Int) => i + 1)

      cache shouldBe a[LoadingCache[_, _]]
    }

    "build loading cache from loading, all loading and reloading functions" in {
      val cache =
        Scaffeine()
          .build[Int, Int](
            loader = (key: Int) => key + 1,
            allLoader =
              Some((keys: Iterable[Int]) => keys.map(i => i -> (i + 1)).toMap),
            reloadLoader = Some((key: Int, _: Int) => key + 1)
          )

      cache shouldBe a[LoadingCache[_, _]]
    }

    "build async loading cache from sync loading function " in {
      val cache = Scaffeine().buildAsync[Int, Int]((i: Int) => i + 1)

      cache shouldBe a[AsyncLoadingCache[_, _]]
    }

    "build async loading cache from sync loading, all loading and reloading functions" in {
      val cache =
        Scaffeine()
          .buildAsync[Int, Int](
            loader = (key: Int) => key + 1,
            allLoader =
              Some((keys: Iterable[Int]) => keys.map(i => i -> (i + 1)).toMap),
            reloadLoader = Some((key: Int, _: Int) => key + 1)
          )

      cache shouldBe a[AsyncLoadingCache[_, _]]
    }

    "build async loading cache from async loading function" in {
      val cache = Scaffeine().buildAsyncFuture[Int, Int]((i: Int) =>
        Future.successful(i + 1)
      )

      cache shouldBe a[AsyncLoadingCache[_, _]]
    }

    "build async loading cache from async loading, all loading and reloading functions" in {
      val cache =
        Scaffeine()
          .buildAsyncFuture[Int, Int](
            loader = (key: Int) => Future.successful(key + 1),
            allLoader = Some((keys: Iterable[Int]) =>
              Future.successful(keys.map(i => i -> (i + 1)).toMap)
            ),
            reloadLoader =
              Some((key: Int, _: Int) => Future.successful(key + 1))
          )

      cache shouldBe a[AsyncLoadingCache[_, _]]
    }

  }

}
