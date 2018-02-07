package r8if;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SingleNode {

   private static final Log log = LogFactory.getLog(SingleNode.class);

   @Test
   public void testPutGet() {
      log.info("First test");

      TestObserver<String> observer = new TestObserver<>();
      Maybe<String> maybe = RxClient
         .from(new ConfigurationBuilder())
         .flatMap(remote -> remote.<String, String>named("default"))
         .flatMap(named -> named.put("pokemon", "mudkip").andThen(Single.just(named)))
         .flatMapMaybe(named -> named.get("pokemon"));

      maybe.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertComplete();
      observer.assertNoErrors();
      observer.assertValue("mudkip");
   }

}
