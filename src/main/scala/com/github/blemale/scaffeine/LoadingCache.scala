package com.github.blemale.scaffeine

import com.github.benmanes.caffeine.cache.{LoadingCache => CaffeineLoadingCache}

import scala.jdk.CollectionConverters._

object LoadingCache {

  def apply[K, V](
      loadingCache: CaffeineLoadingCache[K, V]
  ): LoadingCache[K, V] =
    new LoadingCache(loadingCache)
}

class LoadingCache[K, V](override val underlying: CaffeineLoadingCache[K, V])
    extends Cache(underlying) {

  /** Returns the value associated with `key` in this cache, obtaining that value from
    * `loader` if necessary.
    * <p>
    * If another call to this method is currently loading the value for `key`, this thread
    * simply waits for that thread to finish and returns its loaded value. Note that multiple threads
    * can concurrently load values for distinct keys.
    *
    * @param key key with which the specified value is to be associated
    * @return the current (existing or computed) value associated with the specified key
    * @throws java.lang.IllegalArgumentException if the computation detectably attempts a recursive update to this
    *                               cache that would otherwise never complete
    * @throws java.util.concurrent.CompletionException   if a checked exception was thrown while loading the value
    * @throws java.lang.RuntimeException      or Error if the `CacheLoader` does so, in which case the mapping
    *                                                       is left unestablished
    */
  def get(key: K): V =
    underlying.get(key)

  /** Returns a map of the values associated with `keys`, creating or retrieving those values
    * if necessary. The returned map contains entries that were already cached, combined with newly
    * loaded entries.
    *
    * @param keys the keys whose associated values are to be returned
    * @return the mapping of keys to values for the specified keys in this cache
    * @throws java.util.concurrent.CompletionException  if a checked exception was thrown while loading the value
    * @throws java.lang.RuntimeException     or Error if the `loader` does so
    */
  def getAll(keys: Iterable[K]): Map[K, V] =
    underlying.getAll(keys.asJava).asScala.toMap

  /** Loads a new value for the `key`, asynchronously. While the new value is loading the
    * previous value (if any) will continue to be returned by `get(key)` unless it is evicted.
    *
    * @param key key with which a value may be associated
    */
  def refresh(key: K): Unit =
    underlying.refresh(key)

  override def toString = s"LoadingCache($underlying)"
}
