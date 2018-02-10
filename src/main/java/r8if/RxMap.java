package r8if;

import io.reactivex.Completable;
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
                  .putIfAbsentAsync(key, value);
            })
         .map(Objects::isNull)
         .doOnSuccess(isAbsent -> log.debugf("putIfAbsent returns: %s", isAbsent))
         .observeOn(Schedulers.io());
   }

   public Maybe<V> get(K key) {
      return Futures
         .toMaybe(key, cache,
            (k, rc) -> {
               log.debugf("get(%s)", key);
               return rc.getAsync(key);
            })
         .observeOn(Schedulers.io());
   }

   public Completable clear() {
      return Futures
         .toCompletable(cache, "clear()", AsyncCache::clearAsync)
         .observeOn(Schedulers.io());
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
