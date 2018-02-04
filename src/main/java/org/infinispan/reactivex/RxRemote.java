package org.infinispan.reactivex;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;

import java.util.concurrent.Callable;

public final class RxRemote {

   private static final Log log = LogFactory.getLog(RxRemote.class);

   private final RemoteCacheManager remote;

   public RxRemote(RemoteCacheManager remote) {
      this.remote = remote;
   }

   static Single<RxRemote> from(ConfigurationBuilder cfg) {
      return Single
         .fromCallable(remote(cfg))
         .subscribeOn(Schedulers.io());
   }

   private static Callable<RxRemote> remote(ConfigurationBuilder cfg) {
      return () -> {
         log.info("Create cache manager");
         return new RxRemote(new RemoteCacheManager(cfg.build()));
      };
   }

}
