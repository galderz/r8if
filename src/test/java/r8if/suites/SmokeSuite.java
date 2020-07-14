package r8if.suites;

import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import r8if.SingleNode;
import r8if.SingleNodeQuery;
import r8if.util.Servers;

import java.io.Closeable;
import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   SingleNode.class
   , SingleNodeQuery.class
   /*, APIs.class*/
})
public class SmokeSuite {

   private static Log log = LogFactory.getLog(SmokeSuite.class);
   private static Closeable local;

   @BeforeClass
   public static void startServers() {
      log.info("Start servers");
      local = Servers.local();
   }

   @AfterClass
   public static void stopServers() throws IOException {
      log.info("Stop servers");
      local.close();
   }

}
