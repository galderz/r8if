package org.infinispan.reactivex;

import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SingleTest {

   private static final Log log = LogFactory.getLog(SingleTest.class);

   @Test
   public void testPutGet() {
      log.info("First test");
      assertEquals(1, 1);
   }

}
