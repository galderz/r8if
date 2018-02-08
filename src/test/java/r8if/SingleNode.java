package r8if;

import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SingleNode {

   private static final Log log = LogFactory.getLog(SingleNode.class);

//   @Test
//   public void testGetOnly() {
//      Maybe<String> value = RxClients
//         .<String, String>rxMap("client", new ConfigurationBuilder(), "default")
//         .flatMapMaybe(named -> named.get("snorlax"))
//         .doAfterTerminate(() -> RxClients.stop("client"));
//
//      TestObserver<String> observer = new TestObserver<>();
//      value.subscribe(observer);
//
//      observer.awaitTerminalEvent(5, SECONDS);
//      observer.assertNoErrors();
//      observer.assertComplete();
//      observer.assertValueCount(0);
//   }

   @Test
   public void testPutThenGet() {
      Maybe<String> value = RxMap
         .<String, String>from("default", new ConfigurationBuilder())
         .flatMapMaybe(map ->
            map.put("pokemon", "mudkip")
               .andThen(map.get("pokemon"))
               .doAfterTerminate(() -> map.client().stop())
         );

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue("mudkip");
   }

//   @Test
//   public void testNoPutIfAbsent() {
//
//   }

}
