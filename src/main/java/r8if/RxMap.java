package r8if;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;

public final class RxMap<K, V> {

   private static final Log log = LogFactory.getLog(RxMap.class);

   private final RemoteCache<K, V> cache;

   public RxMap(RemoteCache<K, V> cache) {
      this.cache = cache;
   }

   RxMap<K, V> withScheduler(Scheduler s) {
      // TODO
      return null;
   }

   public Completable put(K key, V value) {
      // TODO: Add flat to avoid return
      return Futures
         .toCompletable(cache.putAsync(key, value))
         .observeOn(Schedulers.io());
   }

   public Maybe<V> get(K key) {
      return Futures
         .toMaybe(cache.getAsync(key))
         .doOnSuccess(v -> log.debugf("get(%s)=%s", key, v))
         .observeOn(Schedulers.io());
   }

   public RxClient client() {
      return client();
   }

   // get will be Maybe
   // getAll can be Flowable
   // addClientListener return a Flowable
   // getAll -> Flowable.toMap

}
