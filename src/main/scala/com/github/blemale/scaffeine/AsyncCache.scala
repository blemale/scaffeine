package com.github.blemale.scaffeine

import java.util.concurrent.Executor

import com.github.benmanes.caffeine.cache.{AsyncCache => CaffeineAsyncCache}

import scala.jdk.CollectionConverters._
import scala.compat.java8.FunctionConverters._
import scala.compat.java8.FutureConverters._
import scala.concurrent.{ExecutionContext, Future}

object AsyncCache {

  def apply[K, V](asyncCache: CaffeineAsyncCache[K, V]): AsyncCache[K, V] =
    new AsyncCache(asyncCache)
}

class AsyncCache[K, V](val underlying: CaffeineAsyncCache[K, V]) {
  implicit private[this] val ec: ExecutionContext = DirectExecutionContext

  /** Returns the future associated with `key` in this cache, or `None` if there is no
    * cached future for `key`.
    *
    * @param key key whose associated value is to be returned
    * @return an option containing the current (existing or computed) future value to which the
    *         specified key is mapped, or `None` if this map contains no mapping for the key
    */
  def getIfPresent(key: K): Option[Future[V]] =
    Option(underlying.getIfPresent(key)).map(_.toScala)

  /** Returns the future associated with `key` in this cache, obtaining that value from
    * `mappingFunction` if necessary. This method provides a simple substitute for the
    * conventional "if cached, return; otherwise create, cache and return" pattern.
    *
    * @param key             key with which the specified value is to be associated
    * @param mappingFunction the function to asynchronously compute a value
    * @return the current (existing or computed) future value associated with the specified key
    */
  def get(key: K, mappingFunction: K => V): Future[V] =
    underlying.get(key, asJavaFunction(mappingFunction)).toScala

  /** Returns the future associated with `key` in this cache, obtaining that value from
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
    underlying
      .get(
        key,
        asJavaBiFunction((k: K, _: Executor) =>
          mappingFunction(k).toJava.toCompletableFuture
        )
      )
      .toScala

  /** Returns the future of a map of the values associated with `keys`, creating or retrieving
    * those values if necessary. The returned map contains entries that were already cached, combined
    * with newly loaded entries. If the any of the asynchronous computations fail, those entries will
    * be automatically removed from this cache.
    *
    * A single request to the `mappingFunction` is performed for all keys which are not already
    * present in the cache.
    *
    * @param keys the keys whose associated values are to be returned
    * @param mappingFunction the function to asynchronously compute the values
    * @return the future containing an unmodifiable mapping of keys to values for the specified keys
    *         in this cache
    * @throws java.lang.RuntimeException     or Error if the mappingFunction does when constructing the future,
    *                              in which case the mapping is left unestablished
    */
  def getAll(
      keys: Iterable[K],
      mappingFunction: Iterable[K] => Map[K, V]
  ): Future[Map[K, V]] =
    underlying
      .getAll(
        keys.asJava,
        asJavaFunction((ks: java.lang.Iterable[_ <: K]) =>
          mappingFunction(ks.asScala).asJava
        )
      )
      .toScala
      .map(_.asScala.toMap)

  /** Returns the future of a map of the values associated with `keys`, creating or retrieving
    * those values if necessary. The returned map contains entries that were already cached, combined
    * with newly loaded entries. If the any of the asynchronous computations fail, those entries will
    * be automatically removed from this cache.
    *
    * A single request to the `mappingFunction` is performed for all keys which are not already
    * present in the cache.
    *
    * @param keys the keys whose associated values are to be returned
    * @param mappingFunction the function to asynchronously compute the values
    * @return the future containing an unmodifiable mapping of keys to values for the specified keys
    *         in this cache
    * @throws java.lang.RuntimeException     or Error if the mappingFunction does when constructing the future,
    *                              in which case the mapping is left unestablished
    */
  def getAllFuture(
      keys: Iterable[K],
      mappingFunction: Iterable[K] => Future[Map[K, V]]
  ): Future[Map[K, V]] =
    underlying
      .getAll(
        keys.asJava,
        asJavaBiFunction((ks: java.lang.Iterable[_ <: K], _: Executor) =>
          mappingFunction(ks.asScala).map(_.asJava).toJava.toCompletableFuture
        )
      )
      .toScala
      .map(_.asScala.toMap)

  /** Associates `value` with `key` in this cache. If the cache previously contained a
    * value associated with `key`, the old value is replaced by `value`. If the
    * asynchronous computation fails, the entry will be automatically removed.
    *
    * @param key         key with which the specified value is to be associated
    * @param valueFuture value to be associated with the specified key
    */
  def put(key: K, valueFuture: Future[V]): Unit =
    underlying.put(key, valueFuture.toJava.toCompletableFuture)

  /** Returns a view of the entries stored in this cache as a synchronous [[Cache]]. A
    * mapping is not present if the value is currently being loaded. Modifications made to the
    * synchronous cache directly affect the asynchronous cache. If a modification is made to a
    * mapping that is currently loading, the operation blocks until the computation completes.
    *
    * @return a thread-safe synchronous view of this cache
    */
  def synchronous(): Cache[K, V] =
    Cache(underlying.synchronous())

}
