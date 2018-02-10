package r8if;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static r8if.Futures.toMaybe;

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
      // TODO putAsync should be lazy (verify when putIfAsbent -> put)
      return Futures
         .toCompletable(cache.putAsync(key, value))
         .doOnComplete(() -> log.debugf("Put complete"))
         .observeOn(Schedulers.io());
   }

//   public Single<Boolean> putIfAbsent(K key, V value) {
//      return Futures
//         .toSingle(cache.putIfAbsentAsync(key, value))
//         .map(Objects::isNull)
//         .observeOn(Schedulers.io());
//   }

   public Maybe<V> get(K key) {
      return Futures.toMaybe(key, cache,
         (k, rc) -> rc.getAsync(key)
      );

//      return getAsync
//         .andThen(cf -> Futures.<V>futureToMaybe().apply(cf))
//         .apply(key, cache);
   }

//   private BiFunction<K, RemoteCache<K, V>, CompletableFuture<V>> keyCacheToFuture() {
//      return (k, rc) ->
//   }

   private Maybe<V> toMaybe(K key, BiFunction<K, RemoteCache<K, V>, CompletableFuture<V>> f) {
      return new Maybe<V>() {
         @Override
         protected void subscribeActual(MaybeObserver<? super V> observer) {
            f.apply(key, cache).whenComplete(
               (v, t) -> {
                  if (t != null)
                     observer.onError(t);
                  else if (v != null)
                     observer.onSuccess(v);
                  else
                     observer.onComplete();
               }
            );
         }
      };
   }

   private MaybeSource<V> toMaybeSource(K key, BiFunction<K, RemoteCache<K, V>, CompletableFuture<V>> f) {
      return (MaybeSource<V>) observer -> {
         f.apply(key, cache).whenComplete(
            (v, t) -> {
               if (t != null)
                  observer.onError(t);
               else if (v != null)
                  observer.onSuccess(v);
               else
                  observer.onComplete();
            }
         );
      };
   }
   

   public RxClient client() {
      return client;
   }

   public static <K, V> Single<RxMap<K, V>> from(String cacheName, ConfigurationBuilder cfg) {
      return RxClient
         .from(cfg)
         .flatMap(client -> client.rxMap(cacheName));
   }

   // get will be Maybe
   // getAll can be Flowable
   // addClientListener return a Flowable
   // getAll -> Flowable.toMap

}
