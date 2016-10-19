package com.github.blemale.scaffeine

import java.util.concurrent.Executor

import com.github.benmanes.caffeine.cache.{ AsyncLoadingCache => CaffeineAsyncLoadingCache }
import com.github.blemale.scaffeine.FunctionConverters._
import com.github.blemale.scaffeine.FutureConverters._

import scala.collection.JavaConverters._
import scala.concurrent.Future

object AsyncLoadingCache {
  def apply[K, V](asyncLoadingCache: CaffeineAsyncLoadingCache[K, V]): AsyncLoadingCache[K, V] =
    new AsyncLoadingCache(asyncLoadingCache)
}

class AsyncLoadingCache[K, V](val underlying: CaffeineAsyncLoadingCache[K, V]) {
  private[this] implicit val ec = DirectExecutionContext

  /**
   * Returns the future associated with `key` in this cache, or `None` if there is no
   * cached future for `key`.
   *
   * @param key key whose associated value is to be returned
   * @return an option containing the current (existing or computed) future value to which the
   *         specified key is mapped, or `None` if this map contains no mapping for the key
   */
  def getIfPresent(key: K): Option[Future[V]] =
    Option(underlying.getIfPresent(key)).map(_.toScala)

  /**
   * Returns the future associated with `key` in this cache, obtaining that value from
   * `mappingFunction` if necessary. This method provides a simple substitute for the
   * conventional "if cached, return; otherwise create, cache and return" pattern.
   *
   * @param key             key with which the specified value is to be associated
   * @param mappingFunction the function to asynchronously compute a value
   * @return the current (existing or computed) future value associated with the specified key
   */
  def get(key: K, mappingFunction: K => V): Future[V] =
    underlying.get(key, asJavaFunction(mappingFunction)).toScala

  /**
   * Returns the future associated with `key` in this cache, obtaining that value from
   * `mappingFunction` if necessary. This method provides a simple substitute for the
   * conventional "if cached, return; otherwise create, cache and return" pattern.
   *
   * @param key             key with which the specified value is to be associated
   * @param mappingFunction the function to asynchronously compute a value
   * @return the current (existing or computed) future value associated with the specified key
   * @throws java.lang.RuntimeException     or Error if the mappingFunction does when constructing the future,
   *                              in which case the mapping is left unestablished
   */
  def getFuture(key: K, mappingFunction: K => Future[V]): Future[V] =
    underlying.get(
      key,
      asJavaBiFunction((k: K, _: Executor) => mappingFunction(k).toJava)
    ).toScala

  /**
   * Returns the future associated with `key` in this cache, obtaining that value from
   * `loader` if necessary. If the asynchronous computation fails, the entry
   * will be automatically removed.
   *
   * @param key key with which the specified value is to be associated
   * @return the current (existing or computed) future value associated with the specified key
   * @throws java.lang.RuntimeException     or Error if the `loader` does when constructing the future,
   *                                                      in which case the mapping is left unestablished
   */
  def get(key: K): Future[V] =
    underlying.get(key).toScala

  /**
   * Returns the future of a map of the values associated with `keys`, creating or retrieving
   * those values if necessary. The returned map contains entries that were already cached, combined
   * with newly loaded entries. If the any of the asynchronous computations fail, those entries will
   * be automatically removed.
   *
   * @param keys the keys whose associated values are to be returned
   * @return the future containing an mapping of keys to values for the specified keys in this cache
   * @throws java.lang.RuntimeException     or Error if the `loader` does so
   */
  def getAll(keys: Iterable[K]): Future[Map[K, V]] =
    underlying.getAll(keys.asJava).toScala.map(_.asScala.toMap)

  /**
   * Associates `value` with `key` in this cache. If the cache previously contained a
   * value associated with `key`, the old value is replaced by `value`. If the
   * asynchronous computation fails, the entry will be automatically removed.
   *
   * @param key         key with which the specified value is to be associated
   * @param valueFuture value to be associated with the specified key
   */
  def put(key: K, valueFuture: Future[V]): Unit =
    underlying.put(key, valueFuture.toJava)

  /**
   * Returns a view of the entries stored in this cache as a synchronous [[LoadingCache]]. A
   * mapping is not present if the value is currently being loaded. Modifications made to the
   * synchronous cache directly affect the asynchronous cache. If a modification is made to a
   * mapping that is currently loading, the operation blocks until the computation completes.
   *
   * @return a thread-safe synchronous view of this cache
   */
  def synchronous(): LoadingCache[K, V] =
    LoadingCache(underlying.synchronous())

  override def toString = s"AsyncLoadingCache($underlying)"
}
