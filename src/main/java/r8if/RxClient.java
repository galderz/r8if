package r8if;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;

import java.util.concurrent.Callable;

public final class RxClient {

   private static final Log log = LogFactory.getLog(RxClient.class);

   private final RemoteCacheManager remote;

   private RxClient(RemoteCacheManager remote) {
      this.remote = remote;
   }

   public <K, V> Single<RxMap<K, V>> named(String name) {
      return Single
         .fromCallable(this.<K, V>cache(name))
         .subscribeOn(Schedulers.io());
   }

   public static Single<RxClient> from(ConfigurationBuilder cfg) {
      return Single
         .fromCallable(remote(cfg))
         .subscribeOn(Schedulers.io());
   }

   private <K, V> Callable<RxMap<K, V>> cache(String name) {
      return () -> {
         log.info("Get cache");
         return new RxMap<>(remote.getCache(name));
      };
   }

   private static Callable<RxClient> remote(ConfigurationBuilder cfg) {
      return () -> {
         log.info("Create cache manager");
         return new RxClient(new RemoteCacheManager(cfg.build()));
      };
   }

}
