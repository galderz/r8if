package org.infinispan.reactivex;

import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SingleTest {

   private static final Log log = LogFactory.getLog(SingleTest.class);

   @Test
   public void testPutGet() {
      log.info("First test");
      assertEquals(1, 1);
   }

}
