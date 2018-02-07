package r8if;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.infinispan.client.hotrod.RemoteCache;

public final class RxNamed<K, V> {

   private final RemoteCache<K, V> cache;

   public RxNamed(RemoteCache<K, V> cache) {
      this.cache = cache;
   }

   RxNamed<K, V> withScheduler(Scheduler s) {
      // TODO
      return null;
   }

   Completable put(K key, V value) {
      return Futures
         .toCompletable(cache.putAsync(key, value))
         .observeOn(Schedulers.io());
   }

   Maybe<V> get(K key) {
      return Futures
         .toMaybe(cache.getAsync(key))
         .observeOn(Schedulers.io());
   }

   // get will be Maybe
   // getAll can be Flowable
   // addClientListener return a Flowable
   // getAll -> Flowable.toMap

}
