package r8if;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.infinispan.commons.configuration.BasicConfiguration;
import org.infinispan.commons.util.Either;

import java.util.concurrent.Callable;

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

   public <K, V> Single<RxMap<K, V>> createRxMap(String name, Either<String, BasicConfiguration> from) {
      return Single
         .<RemoteCache<K, V>>fromCallable(createRemoteCacheAsync(name, from))
         .map(rc -> new RxMap<>(rc, this))
         .subscribeOn(Schedulers.io());
   }

   public Completable stop() {
      return Completable.fromFuture(cacheManager.stopAsync());
   }

   public static Single<RxClient> from(ConfigurationBuilder cfg) {
      return Single.fromCallable(
         () -> new RxClient(new RemoteCacheManager(cfg.build()))
      );
   }

   private <K, V> Callable<RemoteCache<K, V>> createRemoteCacheAsync(
         String name, Either<String, BasicConfiguration> from) {
      return () -> {
         switch (from.type()) {
            case LEFT:
               return cacheManager.administration().getOrCreateCache(name, from.left());
            case RIGHT:
               // TODO Remove casting when updating to 9.2.0.CR3 or higher
               return (RemoteCache<K, V>)
                  cacheManager.administration().getOrCreateCache(name, from.right());
            default:
               throw new IllegalStateException();
         }
      };
   }

}
