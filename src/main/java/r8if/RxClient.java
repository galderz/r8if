package r8if;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;

public final class RxClient {

   private static final Log log = LogFactory.getLog(RxClient.class);

   private final RemoteCacheManager cacheManager;

   private RxClient(RemoteCacheManager client) {
      this.cacheManager = client;
   }

   public <K, V> Single<RxMap<K, V>> rxMap(String name) {
      return Single
         .fromCallable(
            () -> new RxMap<>(cacheManager.<K, V>getCache(name), this)
         ).subscribeOn(Schedulers.io());
   }

   public Completable stop() {
      return Completable.fromFuture(cacheManager.stopAsync());
   }

   public static Single<RxClient> from(ConfigurationBuilder cfg) {
      return Single.fromCallable(
         () -> new RxClient(new RemoteCacheManager(cfg.build()))
      );
   }

}
