package com.github.blemale.scaffeine

import com.github.benmanes.caffeine
import com.github.benmanes.caffeine.cache.Scheduler
import com.github.benmanes.caffeine.cache.stats.StatsCounter

import java.util.concurrent.{CompletableFuture, Executor}
import java.{lang, util}
import scala.jdk.CollectionConverters._
import scala.compat.java8.DurationConverters._
import scala.compat.java8.FunctionConverters._
import scala.compat.java8.FutureConverters._
import scala.concurrent.Future
import scala.concurrent.duration._

object Scaffeine {

  /** Constructs a new `Scaffeine` instance with default settings, including strong keys, strong
    * values, and no automatic eviction of any kind.
    *
    * @return a new instance with default settings
    */
  def apply(): Scaffeine[Any, Any] =
    Scaffeine(
      caffeine.cache.Caffeine
        .newBuilder()
        .asInstanceOf[caffeine.cache.Caffeine[Any, Any]]
    )

  /** Constructs a new `Scaffeine` instance with the settings specified in `spec`.
    *
    * @param spec an instance of [[com.github.benmanes.caffeine.cache.CaffeineSpec]]
    * @return a new instance with the specification's settings
    */
  def apply(spec: caffeine.cache.CaffeineSpec): Scaffeine[Any, Any] =
    Scaffeine(
      caffeine.cache.Caffeine
        .from(spec)
        .asInstanceOf[caffeine.cache.Caffeine[Any, Any]]
    )

  /** Constructs a new `Scaffeine` instance with the settings specified in `spec`.
    *
    * @param spec a [[java.lang.String]] in the format specified by
    *             [[com.github.benmanes.caffeine.cache.CaffeineSpec]]
    * @return a new instance with the specification's settings
    */
  def apply(spec: String): Scaffeine[Any, Any] =
    Scaffeine(
      caffeine.cache.Caffeine
        .from(spec)
        .asInstanceOf[caffeine.cache.Caffeine[Any, Any]]
    )
}

case class Scaffeine[K, V](underlying: caffeine.cache.Caffeine[K, V]) {

  /** Sets the minimum total size for the internal hash tables.
    *
    * @param initialCapacity minimum total size for the internal hash tables
    * @return this builder instance
    * @throws java.lang.IllegalArgumentException if initialCapacity
    * @throws java.lang.IllegalStateException if an initial capacity was already set
    */
  def initialCapacity(initialCapacity: Int): Scaffeine[K, V] =
    Scaffeine(underlying.initialCapacity(initialCapacity))

  /** Specifies the executor to use when running asynchronous tasks.
    *
    * @param executor the executor to use for asynchronous execution
    * @return this builder instance
    */
  def executor(executor: Executor): Scaffeine[K, V] =
    Scaffeine(underlying.executor(executor))

  /** Specifies the maximum number of entries the cache may contain.
    *
    * @param maximumSize the maximum size of the cache
    * @return this builder instance
    * @throws java.lang.IllegalArgumentException `size` is negative
    * @throws java.lang.IllegalStateException if a maximum size or weight was already set
    */
  def maximumSize(maximumSize: Long): Scaffeine[K, V] =
    Scaffeine(underlying.maximumSize(maximumSize))

  /** Specifies the maximum weight of entries the cache may contain.
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

  /** Specifies the weigher to use in determining the weight of entries.
    *
    * @param weigher the weigher to use in calculating the weight of cache entries
    * @tparam K1 key type of the weigher
    * @tparam V1 value type of the weigher
    * @return this builder instance
    * @throws java.lang.IllegalArgumentException if `size` is negative
    * @throws java.lang.IllegalStateException if a maximum size was already set
    */
  def weigher[K1 <: K, V1 <: V](weigher: (K1, V1) => Int): Scaffeine[K1, V1] =
    Scaffeine(underlying.weigher(new caffeine.cache.Weigher[K1, V1] {
      override def weigh(key: K1, value: V1): Int = weigher(key, value)
    }))

  /** Specifies that each key (not value) stored in the cache should be wrapped in a
    * [[java.lang.ref.WeakReference]] (by default, strong references are used).
    * <p>
    * This feature cannot be used in conjunction with [[Scaffeine.writer]].
    *
    * @return this builder instance
    * @throws java.lang.IllegalStateException if the key strength was already set or the writer was set
    */
  def weakKeys(): Scaffeine[K, V] =
    Scaffeine(underlying.weakKeys())

  /** Specifies that each value (not key) stored in the cache should be wrapped in a
    * [[java.lang.ref.WeakReference]] (by default, strong references are used).
    * <p>
    * This feature cannot be used in conjunction with [[Scaffeine.buildAsync[K1<:K,V1<:V]()*]].
    *
    * @return this builder instance
    * @throws java.lang.IllegalStateException if the value strength was already set
    */
  def weakValues(): Scaffeine[K, V] =
    Scaffeine(underlying.weakValues())

  /** Specifies that each value (not key) stored in the cache should be wrapped in a
    * [[java.lang.ref.SoftReference]] (by default, strong references are used).
    * <p>
    * This feature cannot be used in conjunction with [[Scaffeine.buildAsync[K1<:K,V1<:V]()*]].
    *
    * @return this builder instance
    * @throws java.lang.IllegalStateException if the value strength was already set
    */
  def softValues(): Scaffeine[K, V] =
    Scaffeine(underlying.softValues())

  /** Specifies that each entry should be automatically removed from the cache once a fixed duration
    * has elapsed after the entry's creation, or the most recent replacement of its value.
    *
    * @param duration the length of time after an entry is created that it should be automatically
    *                 removed
    * @return this builder instance
    * @throws java.lang.IllegalArgumentException if `duration` is negative
    * @throws java.lang.IllegalStateException    if the time to live or time to idle was already set
    */
  def expireAfterWrite(duration: FiniteDuration): Scaffeine[K, V] =
    Scaffeine(underlying.expireAfterWrite(duration.toJava))

  /** Specifies that each entry should be automatically removed from the cache once a fixed duration
    * has elapsed after the entry's creation, the most recent replacement of its value, or its last
    * read.
    *
    * @param duration the length of time after an entry is last accessed that it should be
    *                 automatically removed
    * @return this builder instance
    * @throws java.lang.IllegalArgumentException if `duration` is negative
    * @throws java.lang.IllegalStateException if the time to idle or time to live was already set
    */
  def expireAfterAccess(duration: FiniteDuration): Scaffeine[K, V] =
    Scaffeine(underlying.expireAfterAccess(duration.toJava))

  /** Specifies that each entry should be automatically removed from the cache once a duration has
    * elapsed after the entry's creation, the most recent replacement of its value, or its last
    * read.
    *
    * @param create the length of time an entry should be automatically removed from the cache after the entry's creation.
    * @param update the length of time an entry should be automatically removed from the cache after the replacement of it's value.
    * @param read   the length of time an entry should be automatically removed from the cache after the entry's last read.
    * @tparam K1 the key type of the expiry.
    * @tparam V1 the value type of the expiry.
    * @return this builder instance
    * @throws java.lang.IllegalStateException if expiration was already set or used with expiresAfterAccess or expiresAfterWrite.
    */
  def expireAfter[K1 <: K, V1 <: V](
      create: (K1, V1) => FiniteDuration,
      update: (K1, V1, FiniteDuration) => FiniteDuration,
      read: (K1, V1, FiniteDuration) => FiniteDuration
  ): Scaffeine[K1, V1] =
    Scaffeine(
      underlying
        .expireAfter(new caffeine.cache.Expiry[K1, V1] {

          override def expireAfterCreate(key: K1, value: V1, currentTime: Long)
              : Long =
            create(key, value).toNanos

          override def expireAfterUpdate(
              key: K1,
              value: V1,
              currentTime: Long,
              currentDuration: Long
          ): Long =
            update(key, value, currentDuration.nanos).toNanos

          override def expireAfterRead(
              key: K1,
              value: V1,
              currentTime: Long,
              currentDuration: Long
          ): Long =
            read(key, value, currentDuration.nanos).toNanos
        })
        .asInstanceOf[caffeine.cache.Caffeine[K1, V1]]
    )

  /** Specifies that active entries are eligible for automatic refresh once a fixed duration has
    * elapsed after the entry's creation, or the most recent replacement of its value.
    *
    * @param duration the length of time after an entry is created that it should be considered
    *                 stale, and thus eligible for refresh
    * @return this builder instance
    * @throws java.lang.IllegalArgumentException if `duration` is negative
    * @throws java.lang.IllegalStateException if the refresh interval was already set
    */
  def refreshAfterWrite(duration: FiniteDuration): Scaffeine[K, V] =
    Scaffeine(underlying.refreshAfterWrite(duration.toJava))

  /** Specifies a nanosecond-precision time source for use in determining when entries should be
    * expired or refreshed. By default, `java.lang.System.nanoTime` is used.
    *
    * @param ticker a nanosecond-precision time source
    * @return this builder instance
    * @throws java.lang.IllegalStateException if a ticker was already set
    */
  def ticker(ticker: caffeine.cache.Ticker): Scaffeine[K, V] =
    Scaffeine(underlying.ticker(ticker))

  /** Specifies a listener instance that caches should notify each time an entry is removed for any
    * [[com.github.benmanes.caffeine.cache.RemovalCause]].
    *
    * @param removalListener a listener that caches should notify each time an entry is
    *                        removed
    * @tparam K1 the key type of the listener
    * @tparam V1 the value type of the listener
    * @return this builder instance
    * @throws java.lang.IllegalStateException if a removal listener was already set
    */
  def removalListener[K1 <: K, V1 <: V](
      removalListener: (K1, V1, caffeine.cache.RemovalCause) => Unit
  ): Scaffeine[K1, V1] =
    Scaffeine(
      underlying.removalListener(new caffeine.cache.RemovalListener[K1, V1] {

        override def onRemoval(
            key: K1,
            value: V1,
            cause: caffeine.cache.RemovalCause
        ): Unit = removalListener(key, value, cause)
      })
    )

  /** Specifies a listener instance that caches should notify each time an entry is evicted.
    *
    * @param evictionListener a listener that caches should notify each time an entry is
    *                        being automatically removed due to eviction
    * @tparam K1 the key type of the listener
    * @tparam V1 the value type of the listener
    * @return this builder instance
    * @throws java.lang.IllegalStateException if a removal listener was already set
    */
  def evictionListener[K1 <: K, V1 <: V](
      evictionListener: (K1, V1, caffeine.cache.RemovalCause) => Unit
  ): Scaffeine[K1, V1] =
    Scaffeine(
      underlying.evictionListener(new caffeine.cache.RemovalListener[K1, V1] {

        override def onRemoval(
            key: K1,
            value: V1,
            cause: caffeine.cache.RemovalCause
        ): Unit = evictionListener(key, value, cause)
      })
    )

  /** Specifies a writer instance that caches should notify each time an entry is explicitly created
    * or modified, or removed for any [[com.github.benmanes.caffeine.cache.RemovalCause]].
    * <p>
    * This feature cannot be used in conjunction with [[Scaffeine.weakKeys]] or [[Scaffeine.buildAsync[K1<:K,V1<:V]()*]].
    *
    * @param writer a writer instance that caches should notify each time an entry is explicitly
    *               created or modified, or removed for any reason
    * @tparam K1 the key type of the writer
    * @tparam V1 the value type of the writer
    * @return this builder instance
    * @throws java.lang.IllegalStateException if a writer was already set or if the key strength is weak
    */
  @deprecated("Scheduled for removal", "scaffeine 4.1.0")
  def writer[K1 <: K, V1 <: V](
      writer: caffeine.cache.CacheWriter[K1, V1]
  ): Scaffeine[K1, V1] =
    Scaffeine(underlying.writer(writer))

  /** Enables the accumulation of [[com.github.benmanes.caffeine.cache.stats.CacheStats]]
    * during the operation of the cache.
    *
    * @return this builder instance
    */
  def recordStats(): Scaffeine[K, V] =
    Scaffeine(underlying.recordStats())

  /** Enables the accumulation of [[com.github.benmanes.caffeine.cache.stats.CacheStats]] during
    * the operation of the cache.
    *
    * @param statsCounterSupplier a supplier that returns a new
    *                             [[com.github.benmanes.caffeine.cache.stats.StatsCounter]]
    * @return this builder instance
    */
  def recordStats[C <: StatsCounter](
      statsCounterSupplier: () => C
  ): Scaffeine[K, V] =
    Scaffeine(underlying.recordStats(asJavaSupplier(statsCounterSupplier)))

  /** Specifies the scheduler to use when scheduling routine maintenance based on an expiration
    * event. This augments the periodic maintenance that occurs during normal cache operations to
    * allow for the prompt removal of expired entries regardless of whether any cache activity is
    * occurring at that time. By default the scheduler is disabled.
    *
    * @param scheduler the scheduler that submits a task to the [[Scaffeine.executor*]] after a given delay
    * @return this builder instance
    */
  def scheduler(scheduler: Scheduler): Scaffeine[K, V] =
    Scaffeine(underlying.scheduler(scheduler))

  /** Builds a cache which does not automatically load values when keys are requested.
    *
    * @tparam K1 the key type of the cache
    * @tparam V1 the value type of the cache
    * @return a cache having the requested features
    */
  def build[K1 <: K, V1 <: V](): Cache[K1, V1] =
    Cache(underlying.build())

  /** Builds a cache, which either returns an already-loaded value for a given key or atomically
    * computes or retrieves it using the supplied `loader`. If another thread is currently
    * loading the value for this key, simply waits for that thread to finish and returns its loaded
    * value. Note that multiple threads can concurrently load values for distinct keys.
    *
    * @param loader the loader used to obtain new values
    * @param allLoader the loader used to obtain new values in bulk, called by [[LoadingCache.getAll(keys:Iterable[K])*]]
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
    LoadingCache(
      underlying.build(
        toCacheLoader(
          loader,
          allLoader,
          reloadLoader
        )
      )
    )

  /** Builds a cache which does not automatically load values when keys are requested unless a
    * mapping function is provided. The returned [[scala.concurrent.Future]] may be already loaded or
    * currently computing the value for a given key. If the asynchronous computation fails
    * value then the entry will be automatically removed. Note that multiple
    * threads can concurrently load values for distinct keys.
    *
    * @tparam K1 the key type of the cache
    * @tparam V1 the value type of the cache
    * @return a cache having the requested features
    */
  def buildAsync[K1 <: K, V1 <: V](): AsyncCache[K1, V1] =
    AsyncCache(underlying.buildAsync[K1, V1]())

  /** Builds a cache, which either returns a [[scala.concurrent.Future]] already loaded or currently
    * computing the value for a given key, or atomically computes the value asynchronously through a
    * supplied mapping function or the supplied `loader`. If the asynchronous computation
    * fails then the entry will be automatically removed. Note that multiple threads can
    * concurrently load values for distinct keys.
    *
    * @param loader the loader used to obtain new values
    * @param allLoader the loader used to obtain new values in bulk, called by [[AsyncLoadingCache.getAll(keys:Iterable[K])*]]
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
    AsyncLoadingCache(
      underlying.buildAsync[K1, V1](
        toCacheLoader(
          loader,
          allLoader,
          reloadLoader
        )
      )
    )

  /** Builds a cache, which either returns a [[scala.concurrent.Future]] already loaded or currently
    * computing the value for a given key, or atomically computes the value asynchronously through a
    * supplied mapping function or the supplied async `loader`. If the asynchronous
    * computation fails then the entry will be automatically removed.
    * Note that multiple threads can concurrently load values for distinct keys.
    *
    * @param loader the loader used to obtain new values
    * @param allLoader the loader used to obtain new values in bulk, called by [[AsyncLoadingCache.getAll(keys:Iterable[K])*]]
    * @param reloadLoader the loader used to obtain already-cached values
    * @tparam K1 the key type of the loader
    * @tparam V1 the value type of the loader
    * @throws java.lang.IllegalStateException if the value strength is weak or soft
    */
  def buildAsyncFuture[K1 <: K, V1 <: V](
      loader: K1 => Future[V1],
      allLoader: Option[Iterable[K1] => Future[Map[K1, V1]]] = None,
      reloadLoader: Option[(K1, V1) => Future[V1]] = None
  ): AsyncLoadingCache[K1, V1] =
    AsyncLoadingCache(
      underlying.buildAsync[K1, V1](
        toAsyncCacheLoader(
          loader,
          allLoader,
          reloadLoader
        )
      )
    )

  private[this] def toCacheLoader[K1 <: K, V1 <: V](
      loader: K1 => V1,
      allLoader: Option[Iterable[K1] => Map[K1, V1]],
      reloadLoader: Option[(K1, V1) => V1]
  ): caffeine.cache.CacheLoader[K1, V1] =
    allLoader match {
      case Some(l) =>
        new CacheLoaderAdapter[K1, V1](loader, reloadLoader) {

          override def loadAll(keys: lang.Iterable[_ <: K1]): util.Map[K1, V1] =
            l(keys.asScala).asJava
        }
      case None =>
        new CacheLoaderAdapter[K1, V1](loader, reloadLoader)
    }

  private[this] def toAsyncCacheLoader[K1 <: K, V1 <: V](
      loader: K1 => Future[V1],
      allLoader: Option[Iterable[K1] => Future[Map[K1, V1]]],
      reloadLoader: Option[(K1, V1) => Future[V1]]
  ): caffeine.cache.AsyncCacheLoader[K1, V1] =
    allLoader match {
      case Some(l) =>
        new AsyncCacheLoaderAdapter[K1, V1](loader, reloadLoader) {

          override def asyncLoadAll(
              keys: lang.Iterable[_ <: K1],
              executor: Executor
          ): CompletableFuture[util.Map[K1, V1]] =
            l(keys.asScala)
              .map(_.asJava)(DirectExecutionContext)
              .toJava
              .toCompletableFuture
        }
      case None =>
        new AsyncCacheLoaderAdapter[K1, V1](loader, reloadLoader)
    }
}
