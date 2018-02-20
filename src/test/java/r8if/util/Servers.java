package r8if.util;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.server.core.admin.embeddedserver.EmbeddedServerAdminOperationHandler;
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
      // TODO There should be no need to fiddle with default cache: ISPN-8826
      final DefaultCacheManager cacheManager = new DefaultCacheManager();
      cacheManager.defineConfiguration("SHOULD_NOT_BE_NEEDED",
         new ConfigurationBuilder().build());

      final HotRodServerConfigurationBuilder serverCfg =
         new HotRodServerConfigurationBuilder();
      serverCfg.defaultCacheName("SHOULD_NOT_BE_NEEDED");

      serverCfg.adminOperationsHandler(new EmbeddedServerAdminOperationHandler());

      final HotRodServer server = new HotRodServer();
      server.start(serverCfg.build(), cacheManager);

      isLocalRunning.set(true);

      return () -> {
         server.stop();
         cacheManager.close();
      };
   }

   public static Closeable startIfNotRunning(Supplier<Closeable> server) {
      boolean isRunning = isLocalRunning.get();
      return isRunning
         ? () -> {}
         : server.get();
   }

}
