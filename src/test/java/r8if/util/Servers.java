package r8if.util;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public final class Servers {

   private static final AtomicBoolean isLocalRunning = new AtomicBoolean(false);

   private Servers() {
   }

   public static Closeable local() {
      HotRodServer local = new HotRodServer();
      local.start(
         new HotRodServerConfigurationBuilder().build(),

         // TODO Why is default getCache() called?
         // org.infinispan.commons. CacheConfigurationException: ISPN000433: A default cache has been requested, but no cache has been set as default for this container
         // new DefaultCacheManager()
         new DefaultCacheManager(
            new GlobalConfigurationBuilder().nonClusteredDefault().defaultCacheName("default").build(),
            new ConfigurationBuilder().build())

         // TODO Fix WARN messages appearing in log as a result of code above
      );

      isLocalRunning.set(true);
      return local::stop;
   }

   public static Closeable startIfNotRunning(Supplier<Closeable> server) {
      boolean isRunning = isLocalRunning.get();
      return isRunning
         ? () -> {}
         : server.get();
   }

}
