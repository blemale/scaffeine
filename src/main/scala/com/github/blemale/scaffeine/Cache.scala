package com.github.blemale.scaffeine

import com.github.benmanes.caffeine.cache.stats.CacheStats
import com.github.benmanes.caffeine.cache.{ Policy, Cache => CaffeineCache }
import com.github.blemale.scaffeine.FunctionConverters._

import scala.collection.JavaConverters._

object Cache {
  def apply[K, V](cache: CaffeineCache[K, V]) =
    new Cache(cache)
}

class Cache[K, V](val underlying: CaffeineCache[K, V]) {
  /**
   * Returns the value associated with `key` in this cache, or `None` if there is no
   * cached value for `key`.
   *
   * @param key key whose associated value is to be returned
   * @return an option value containing the value to which the specified key is mapped,
   *         or `None` if this map contains no mapping for the key
   */
  def getIfPresent(key: K): Option[V] =
    Option(underlying.getIfPresent(key))

  /**
   * Returns the value associated with `key` in this cache, obtaining that value from
   * `mappingFunction` if necessary. This method provides a simple substitute for the
   * conventional "if cached, return; otherwise create, cache and return" pattern.
   *
   * @param key             key with which the specified value is to be associated
   * @param mappingFunction the function to compute a value
   * @return the current (existing or computed) value associated with the specified key
   * @throws java.lang.IllegalStateException if the computation detectably attempts a recursive update to this
   *                               cache that would otherwise never complete
   * @throws java.lang.RuntimeException      or Error if the mappingFunction does so, in which case the mapping is
   *                               left unestablished
   */
  def get(key: K, mappingFunction: K => V): V =
    underlying.get(key, asJavaFunction(mappingFunction))

  /**
   * Returns a map of the values associated with `keys` in this cache. The returned map will
   * only contain entries which are already present in the cache.
   *
   * @param keys the keys whose associated values are to be returned
   * @return the mapping of keys to values for the specified keys found in this cache
   */
  def getAllPresent(keys: Iterable[K]): Map[K, V] =
    underlying.getAllPresent(keys.asJava).asScala.toMap

  /**
   * Associates `value` with `key` in this cache. If the cache previously contained a
   * value associated with `key`, the old value is replaced by `value`.
   *
   * @param key   key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   */
  def put(key: K, value: V): Unit =
    underlying.put(key, value)

  /**
   * Copies all of the mappings from the specified map to the cache.
   *
   * @param map mappings to be stored in this cache
   */
  def putAll(map: Map[K, V]): Unit =
    underlying.putAll(map.asJava)

  /**
   * Discards any cached value for key `key`.
   *
   * @param key key whose mapping is to be removed from the cache
   */
  def invalidate(key: K): Unit =
    underlying.invalidate(key)

  /**
   * Discards any cached values for keys `keys`.
   *
   * @param keys the keys whose associated values are to be removed
   */
  def invalidateAll(keys: Iterable[K]): Unit =
    underlying.invalidateAll(keys.asJava)

  /**
   * Discards all entries in the cache.
   */
  def invalidateAll(): Unit =
    underlying.invalidateAll()

  /**
   * Returns the approximate number of entries in this cache.
   *
   * @return the estimated number of mappings
   */
  def estimatedSize(): Long =
    underlying.estimatedSize()

  /**
   * Returns a current snapshot of this cache's cumulative statistics. All statistics are
   * initialized to zero, and are monotonically increasing over the lifetime of the cache.
   *
   * @return the current snapshot of the statistics of this cache
   */
  def stats(): CacheStats =
    underlying.stats()

  /**
   * Returns a view of the entries stored in this cache as a thread-safe map. Modifications made to
   * the map directly affect the cache.
   *
   * @return a thread-safe view of this cache
   */
  def asMap(): collection.concurrent.Map[K, V] =
    underlying.asMap().asScala

  /**
   * Performs any pending maintenance operations needed by the cache. Exactly which activities are
   * performed -- if any -- is implementation-dependent.
   */
  def cleanUp(): Unit =
    underlying.cleanUp()

  /**
   * Returns access to inspect and perform low-level operations on this cache based on its runtime
   * characteristics. These operations are optional and dependent on how the cache was constructed
   * and what abilities the implementation exposes.
   *
   * @return access to inspect and perform advanced operations based on the cache's characteristics
   */
  def policy(): Policy[K, V] =
    underlying.policy()

  override def toString = s"Cache($underlying)"
}
