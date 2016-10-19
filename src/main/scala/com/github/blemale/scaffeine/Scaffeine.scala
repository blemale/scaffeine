package com.github.blemale.scaffeine

import java.util.concurrent.{ CompletableFuture, Executor, TimeUnit }
import java.{ lang, util }

import com.github.benmanes.caffeine.cache._
import com.github.benmanes.caffeine.cache.stats.StatsCounter
import com.github.blemale.scaffeine.FunctionConverters._
import com.github.blemale.scaffeine.FutureConverters._

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }

object Scaffeine {
  /**
   * Constructs a new `Scaffeine` instance with default settings, including strong keys, strong
   * values, and no automatic eviction of any kind.
   *
   * @return a new instance with default settings
   */
  def apply(): Scaffeine[Any, Any] =
    Scaffeine(Caffeine.newBuilder().asInstanceOf[Caffeine[Any, Any]])

  /**
   * Constructs a new `Scaffeine` instance with the settings specified in `spec`.
   *
   * @param spec an instance of [[com.github.benmanes.caffeine.cache.CaffeineSpec]]
   * @return a new instance with the specification's settings
   */
  def apply(spec: CaffeineSpec): Scaffeine[Any, Any] =
    Scaffeine(Caffeine.from(spec).asInstanceOf[Caffeine[Any, Any]])

  /**
   * Constructs a new `Scaffeine` instance with the settings specified in `spec`.
   *
   * @param spec a [[java.lang.String]] in the format specified by
   *             [[com.github.benmanes.caffeine.cache.CaffeineSpec]]
   * @return a new instance with the specification's settings
   */
  def apply(spec: String): Scaffeine[Any, Any] =
    Scaffeine(Caffeine.from(spec).asInstanceOf[Caffeine[Any, Any]])
}

case class Scaffeine[K, V](underlying: Caffeine[K, V]) {
  /**
   * Sets the minimum total size for the internal hash tables.
   *
   * @param initialCapacity minimum total size for the internal hash tables
   * @return this builder instance
   * @throws java.lang.IllegalArgumentException if initialCapacity
   * @throws java.lang.IllegalStateException if an initial capacity was already set
   */
  def initialCapacity(initialCapacity: Int): Scaffeine[K, V] =
    Scaffeine(underlying.initialCapacity(initialCapacity))

  /**
   * Specifies the executor to use when running asynchronous tasks.
   *
   * @param executor the executor to use for asynchronous execution
   * @return this builder instance
   */
  def executor(executor: Executor): Scaffeine[K, V] =
    Scaffeine(underlying.executor(executor))

  /**
   * Specifies the maximum number of entries the cache may contain.
   *
   * @param maximumSize the maximum size of the cache
   * @return this builder instance
   * @throws java.lang.IllegalArgumentException `size` is negative
   * @throws java.lang.IllegalStateException if a maximum size or weight was already set
   */
  def maximumSize(maximumSize: Long): Scaffeine[K, V] =
    Scaffeine(underlying.maximumSize(maximumSize))

  /**
   * Specifies the maximum weight of entries the cache may contain.
   * <p>
   * This feature cannot be used in conjunction with [[Scaffeine.maximumSize]].
   *
   * @param maximumWeight the maximum total weight of entries the cache may contain
   * @return this builder instance
   * @throws java.lang.IllegalArgumentException if `maximumWeight` is negative
   * @throws java.lang.IllegalStateException if a maximum weight or size was already set
   */
  def maximumWeight(maximumWeight: Long): Scaffeine[K, V] =
    Scaffeine(underlying.maximumWeight(maximumWeight))

  /**
   * Specifies the weigher to use in determining the weight of entries.
   *
   * @param weigher the weigher to use in calculating the weight of cache entries
   * @tparam K1 key type of the weigher
   * @tparam V1 value type of the weigher
   * @return this builder instance
   * @throws java.lang.IllegalArgumentException if `size` is negative
   * @throws java.lang.IllegalStateException if a maximum size was already set
   */
  def weigher[K1 <: K, V1 <: V](weigher: (K1, V1) => Int) =
    Scaffeine(underlying.weigher(new Weigher[K1, V1] {
      override def weigh(key: K1, value: V1): Int = weigher(key, value)
    }))

  /**
   * Specifies that each key (not value) stored in the cache should be wrapped in a
   * [[java.lang.ref.WeakReference]] (by default, strong references are used).
   * <p>
   * This feature cannot be used in conjunction with [[Scaffeine.writer]].
   *
   * @return this builder instance
   * @throws java.lang.IllegalStateException if the key strength was already set or the writer was set
   */
  def weakKeys(): Scaffeine[K, V] =
    Scaffeine(underlying.weakKeys())

  /**
   * Specifies that each value (not key) stored in the cache should be wrapped in a
   * [[java.lang.ref.WeakReference]] (by default, strong references are used).
   * <p>
   * This feature cannot be used in conjunction with [[Scaffeine.buildAsync]].
   *
   * @return this builder instance
   * @throws java.lang.IllegalStateException if the value strength was already set
   */
  def weakValues(): Scaffeine[K, V] =
    Scaffeine(underlying.weakValues())

  /**
   * Specifies that each value (not key) stored in the cache should be wrapped in a
   * [[java.lang.ref.SoftReference]] (by default, strong references are used).
   * <p>
   * This feature cannot be used in conjunction with [[Scaffeine.buildAsync]].
   *
   * @return this builder instance
   * @throws java.lang.IllegalStateException if the value strength was already set
   */
  def softValues(): Scaffeine[K, V] =
    Scaffeine(underlying.softValues())

  /**
   * Specifies that each entry should be automatically removed from the cache once a fixed duration
   * has elapsed after the entry's creation, or the most recent replacement of its value.
   *
   * @param duration the length of time after an entry is created that it should be automatically
   *                 removed
   * @return this builder instance
   * @throws java.lang.IllegalArgumentException if `duration` is negative
   * @throws java.lang.IllegalStateException    if the time to live or time to idle was already set
   */
  def expireAfterWrite(duration: Duration): Scaffeine[K, V] =
    Scaffeine(underlying.expireAfterWrite(duration.toNanos, TimeUnit.NANOSECONDS))

  /**
   * Specifies that each entry should be automatically removed from the cache once a fixed duration
   * has elapsed after the entry's creation, the most recent replacement of its value, or its last
   * read.
   *
   * @param duration the length of time after an entry is last accessed that it should be
   *                 automatically removed
   * @return this builder instance
   * @throws java.lang.IllegalArgumentException if `duration` is negative
   * @throws java.lang.IllegalStateException if the time to idle or time to live was already set
   */
  def expireAfterAccess(duration: Duration): Scaffeine[K, V] =
    Scaffeine(underlying.expireAfterAccess(duration.toNanos, TimeUnit.NANOSECONDS))

  /**
   * Specifies that active entries are eligible for automatic refresh once a fixed duration has
   * elapsed after the entry's creation, or the most recent replacement of its value.
   *
   * @param duration the length of time after an entry is created that it should be considered
   *                 stale, and thus eligible for refresh
   * @return this builder instance
   * @throws java.lang.IllegalArgumentException if `duration` is negative
   * @throws java.lang.IllegalStateException if the refresh interval was already set
   */
  def refreshAfterWrite(duration: Duration): Scaffeine[K, V] =
    Scaffeine(underlying.refreshAfterWrite(duration.toNanos, TimeUnit.NANOSECONDS))

  /**
   * Specifies a nanosecond-precision time source for use in determining when entries should be
   * expired or refreshed. By default, `java.lang.System.nanoTime` is used.
   *
   * @param ticker a nanosecond-precision time source
   * @return this builder instance
   * @throws java.lang.IllegalStateException if a ticker was already set
   */
  def ticker(ticker: Ticker): Scaffeine[K, V] =
    Scaffeine(underlying.ticker(ticker))

  /**
   * Specifies a listener instance that caches should notify each time an entry is removed for any
   * [[com.github.benmanes.caffeine.cache.RemovalCause]].
   *
   * @param removalListener a listener that caches should notify each time an entry is
   *                        removed
   * @tparam K1 the key type of the listener
   * @tparam V1 the value type of the listener
   * @return this builder instance
   * @throws java.lang.IllegalStateException if a removal listener was already set
   */
  def removalListener[K1 <: K, V1 <: V](removalListener: (K1, V1, RemovalCause) => Unit): Scaffeine[K1, V1] =
    Scaffeine(underlying.removalListener(new RemovalListener[K1, V1] {
      override def onRemoval(key: K1, value: V1, cause: RemovalCause): Unit = removalListener(key, value, cause)
    }))

  /**
   * Specifies a writer instance that caches should notify each time an entry is explicitly created
   * or modified, or removed for any [[com.github.benmanes.caffeine.cache.RemovalCause]].
   * <p>
   * This feature cannot be used in conjunction with [[Scaffeine.weakKeys]] or [[Scaffeine.buildAsync]].
   *
   * @param writer a writer instance that caches should notify each time an entry is explicitly
   *               created or modified, or removed for any reason
   * @tparam K1 the key type of the writer
   * @tparam V1 the value type of the writer
   * @return this builder instance
   * @throws java.lang.IllegalStateException if a writer was already set or if the key strength is weak
   */
  def writer[K1 <: K, V1 <: V](writer: CacheWriter[K1, V1]): Scaffeine[K1, V1] =
    Scaffeine(underlying.writer(writer))

  /**
   * Enables the accumulation of [[com.github.benmanes.caffeine.cache.stats.CacheStats]]
   * during the operation of the cache.
   *
   * @return this builder instance
   */
  def recordStats(): Scaffeine[K, V] =
    Scaffeine(underlying.recordStats())

  /**
   * Enables the accumulation of [[com.github.benmanes.caffeine.cache.stats.CacheStats]] during
   * the operation of the cache.
   *
   * @param statsCounterSupplier a supplier that returns a new
   *                             [[com.github.benmanes.caffeine.cache.stats.StatsCounter]]
   * @return this builder instance
   */
  def recordStats[C <: StatsCounter](statsCounterSupplier: () => C) =
    Scaffeine(underlying.recordStats(asJavaSupplier(statsCounterSupplier)))

  /**
   * Builds a cache which does not automatically load values when keys are requested.
   *
   * @tparam K1 the key type of the cache
   * @tparam V1 the value type of the cache
   * @return a cache having the requested features
   */
  def build[K1 <: K, V1 <: V](): Cache[K1, V1] =
    Cache(underlying.build())

  /**
   * Builds a cache, which either returns an already-loaded value for a given key or atomically
   * computes or retrieves it using the supplied `loader`. If another thread is currently
   * loading the value for this key, simply waits for that thread to finish and returns its loaded
   * value. Note that multiple threads can concurrently load values for distinct keys.
   *
   * @param loader the loader used to obtain new values
   * @param allLoader the loader used to obtain new values in bulk, called by [[LoadingCache.getAll]]
   * @param reloadLoader the loader used to obtain already-cached values
   * @tparam K1 the key type of the loader
   * @tparam V1 the value type of the loader
   * @return a cache having the requested features
   */
  def build[K1 <: K, V1 <: V](
    loader: K1 => V1,
    allLoader: Option[Iterable[K1] => Map[K1, V1]] = None,
    reloadLoader: Option[(K1, V1) => V1] = None
  ): LoadingCache[K1, V1] =
    LoadingCache(underlying.build(
      toCacheLoader(
        loader,
        allLoader,
        reloadLoader
      )
    ))

  /**
   * Builds a cache, which either returns a [[scala.concurrent.Future]] already loaded or currently
   * computing the value for a given key, or atomically computes the value asynchronously through a
   * supplied mapping function or the supplied `loader`. If the asynchronous computation
   * fails then the entry will be automatically removed. Note that multiple threads can
   * concurrently load values for distinct keys.
   *
   * @param loader the loader used to obtain new values
   * @param allLoader the loader used to obtain new values in bulk, called by [[AsyncLoadingCache.getAll]]
   * @param reloadLoader the loader used to obtain already-cached values
   * @tparam K1 the key type of the loader
   * @tparam V1 the value type of the loader
   * @return a cache having the requested features
   * @throws java.lang.IllegalStateException if the value strength is weak or soft
   */
  def buildAsync[K1 <: K, V1 <: V](
    loader: K1 => V1,
    allLoader: Option[Iterable[K1] => Map[K1, V1]] = None,
    reloadLoader: Option[(K1, V1) => V1] = None
  ): AsyncLoadingCache[K1, V1] =
    AsyncLoadingCache(underlying.buildAsync[K1, V1](
      toCacheLoader(
        loader,
        allLoader,
        reloadLoader
      )
    ))

  /**
   * Builds a cache, which either returns a [[scala.concurrent.Future]] already loaded or currently
   * computing the value for a given key, or atomically computes the value asynchronously through a
   * supplied mapping function or the supplied async `loader`. If the asynchronous
   * computation fails then the entry will be automatically removed.
   * Note that multiple threads can concurrently load values for distinct keys.
   *
   * @param loader the loader used to obtain new values
   * @param allLoader the loader used to obtain new values in bulk, called by [[AsyncLoadingCache.getAll]]
   * @param reloadLoader the loader used to obtain already-cached values
   * @tparam K1 the key type of the loader
   * @tparam V1 the value type of the loader
   * @throws java.lang.IllegalStateException if the value strength is weak or soft
   */
  def buildAsyncFuture[K1 <: K, V1 <: V](
    loader: K1 => Future[V1],
    allLoader: Option[Iterable[K1] => Future[Map[K1, V1]]] = None,
    reloadLoader: Option[(K1, V1) => Future[V1]] = None
  )(
    implicit
    ec: ExecutionContext
  ): AsyncLoadingCache[K1, V1] =
    AsyncLoadingCache(underlying.buildAsync[K1, V1](
      toAsyncCacheLoader(
        loader,
        allLoader,
        reloadLoader
      )
    ))

  private[this] def toCacheLoader[K1 <: K, V1 <: V](
    loader: K1 => V1,
    allLoader: Option[Iterable[K1] => Map[K1, V1]] = None,
    reloadLoader: Option[(K1, V1) => V1] = None
  ): CacheLoader[K1, V1] = allLoader match {
    case Some(l) =>
      new CacheLoaderAdapter[K1, V1](loader, reloadLoader) {
        override def loadAll(keys: lang.Iterable[_ <: K1]): util.Map[K1, V1] =
          l(keys.asScala).asJava
      }
    case None =>
      new CacheLoaderAdapter[K1, V1](loader, reloadLoader)
  }

  private[this] class CacheLoaderAdapter[K1 <: K, V1 <: V](
      loader: K1 => V1,
      reloadLoader: Option[(K1, V1) => V1] = None
  ) extends CacheLoader[K1, V1] {
    override def load(key: K1): V1 = loader(key)

    override def reload(key: K1, oldValue: V1): V1 =
      reloadLoader match {
        case Some(l) => l(key, oldValue)
        case _ => super.reload(key, oldValue)
      }
  }

  private[this] def toAsyncCacheLoader[K1 <: K, V1 <: V](
    loader: K1 => Future[V1],
    allLoader: Option[Iterable[K1] => Future[Map[K1, V1]]] = None,
    reloadLoader: Option[(K1, V1) => Future[V1]] = None
  )(
    implicit
    ec: ExecutionContext
  ): AsyncCacheLoader[K1, V1] = allLoader match {
    case Some(l) =>
      new AsyncLoaderAdapter[K1, V1](loader, reloadLoader) {
        override def asyncLoadAll(keys: lang.Iterable[_ <: K1], executor: Executor): CompletableFuture[util.Map[K1, V1]] =
          l(keys.asScala).map(_.asJava).toJava
      }
    case None =>
      new AsyncLoaderAdapter[K1, V1](loader, reloadLoader)
  }

  private[this] class AsyncLoaderAdapter[K1 <: K, V1 <: V](
      loader: K1 => Future[V1],
      reloadLoader: Option[(K1, V1) => Future[V1]] = None
  )(
      implicit
      ec: ExecutionContext
  ) extends AsyncCacheLoader[K1, V1] {
    override def asyncLoad(key: K1, executor: Executor): CompletableFuture[V1] =
      loader(key).toJava

    override def asyncReload(key: K1, oldValue: V1, executor: Executor): CompletableFuture[V1] =
      reloadLoader match {
        case Some(l) => l(key, oldValue).toJava
        case _ => super.asyncReload(key, oldValue, executor)
      }
  }
}
