package r8if.suites;

import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import r8if.SingleNode;

@RunWith(Suite.class)
@Suite.SuiteClasses(SingleNode.class)
public class SmokeSuite {

   static Log log = LogFactory.getLog(SmokeSuite.class);
   private static HotRodServer server;

   @BeforeClass
   public static void startServers() {
      log.info("Start servers");
      server = new HotRodServer();
      server.start(
         new HotRodServerConfigurationBuilder().build(),

         // TODO: Why is default getCache() called?
         // org.infinispan.commons. CacheConfigurationException: ISPN000433: A default cache has been requested, but no cache has been set as default for this container
         // new DefaultCacheManager()
         new DefaultCacheManager(
            new GlobalConfigurationBuilder().nonClusteredDefault().defaultCacheName("default").build(),
            new ConfigurationBuilder().build())
      );
   }

   @AfterClass
   public static void stopServers() {
      log.info("Stop servers");
      server.stop();
   }

}
