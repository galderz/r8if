package r8if;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import org.infinispan.client.hotrod.Flag;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import r8if.Fn.TriFunction;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
      log.debugf("put(%s, %s)", key, value);
      // TODO put should be lazy (verify with a Completable -> Completable)
      return Futures
         .toCompletable(cache.putAsync(key, value))
         .doOnComplete(() -> log.debug("put complete"))
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

//      // TODO putAsync should be lazy (verify with a Completable -> Single)
//      return toSingle(cache.putIfAbsentAsync(key, value))
//         .observeOn(Schedulers.io());
   }

   public static <T> Single<Boolean> toSingle(CompletionStage<T> future) {
      SingleSubject<Boolean> cs = SingleSubject.create();

      future.whenComplete((v, e) -> {
         if (e != null)
            cs.onError(e);
         else if (v != null)
            cs.onSuccess(false);
         else
            cs.onSuccess(true);
      });

      return cs;
   }

   public Maybe<V> get(K key) {
      return Futures
         .toMaybe(key, cache,
            (k, rc) -> {
               log.debugf("get(%s)", key);
               return rc.getAsync(key);
            })
         .observeOn(Schedulers.io());

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
