package r8if;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.infinispan.commons.api.AsyncCache;

import java.util.Map;
import java.util.Objects;

public final class RxMap<K, V> {

   private static final Log log = LogFactory.getLog(RxMap.class);

   private final RemoteCache<K, V> cache;
   private final RxClient client;

   public RxMap(RemoteCache<K, V> cache, RxClient client) {
      this.cache = cache;
      this.client = client;
   }

   RxMap<K, V> withScheduler(Scheduler s) {
      // TODO
      return null;
   }

   public Completable put(K key, V value) {
      return Futures
         .toCompletable(key, value, cache,
            (k, v, rc) -> {
               log.debugf("put(%s, %s)", key, value);
               return rc.putAsync(key, value);
            })
         .doOnComplete(() -> log.debugf("put(%s, %s) complete", key, value))
         .observeOn(Schedulers.io());
   }

   public Single<Boolean> putIfAbsent(K key, V value) {
      return Futures
         .toSingle(key, value, cache,
            (k, v, rc) -> {
               log.debugf("putIfAbsent(%s, %s)", key, value);
               return rc
                  // TODO: Sucks that previous value is needed when API only returns Boolean (too much work)
                  .withFlags(Flag.FORCE_RETURN_VALUE)
                  .putIfAbsentAsync(key, value)
                  .thenApply(Objects::isNull);
            })
         .doOnSuccess(isAbsent -> log.debugf("putIfAbsent returns: %b", isAbsent))
         .observeOn(Schedulers.io());
   }

   public Maybe<V> get(K key) {
      return Futures
         .toMaybe(key, cache,
            k -> String.format("get(%s)", k),
            (k, rc) -> rc.getAsync(k)
         )
         .observeOn(Schedulers.io());
   }

   public Single<Boolean> remove(K key) {
      return Futures
         .toSingle(key, cache,
            (k, rc) -> {
               log.debugf("remove(%s)", key);
               return rc
                  // TODO Sucks that previous value is needed when API only returns Boolean (too much work)
                  .withFlags(Flag.FORCE_RETURN_VALUE)
                  .removeAsync(key)
                  .thenApply(Objects::nonNull);
            })
         .doOnSuccess(removed -> log.debugf("remove(%s) returns: %b", key, removed))
         .observeOn(Schedulers.io());
   }

   public Completable clear() {
      return Futures
         .toCompletable(cache,
            "clear()",
            AsyncCache::clearAsync
         )
         .observeOn(Schedulers.io());
   }

   public Completable putAll(Flowable<Map.Entry<K, V>> f) {
      return f
         .toMap(Map.Entry::getKey, Map.Entry::getValue)
         .flatMapCompletable(map ->
            Futures
               .toCompletable(map, cache
               , m -> String.format("putAll(%s)", m)
               , (m, rc) -> rc.putAllAsync(m))
         );

   }

   public RxClient client() {
      return client;
   }

   public static <K, V> Single<RxMap<K, V>> from(String cacheName, ConfigurationBuilder cfg) {
      return RxClient
         .from(cfg)
         .flatMap(client -> client.rxMap(cacheName));
   }

   // getAll can be Flowable
   // addClientListener return a Flowable
   // getAll -> Flowable.toMap

}
