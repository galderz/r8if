package r8if;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;

public class APIs {

   /**
    * Reactive remote put/get with:
    * - create/destroy lifecycle
    * - without caching variables
    * - without deep nesting
    * - client/map retrieval as separate steps
    */
   @Test
   public void testPutGetV1() {
      Maybe<String> value = RxClients
         .rxClient("client", new ConfigurationBuilder())
         .flatMap(remote -> remote.<String, String>rxMap("default"))
         .flatMap(named -> named.put("pokemon", "mudkip").andThen(Single.just(named)))
         .flatMapMaybe(named -> named.get("pokemon"))
         .doAfterTerminate(() -> RxClients.stop("client"));

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertComplete();
      observer.assertNoErrors();
      observer.assertValue("mudkip");
   }

   /**
    * Reactive remote put/get with:
    * - create/destroy lifecycle
    * - without caching variables
    * - without deep nesting
    * - map retrieved in a single step
    */
   @Test
   public void testPutGetV2() {
      Maybe<String> value = RxClients
         .<String, String>rxMap("client", new ConfigurationBuilder(), "default")
         .flatMap(named -> named.put("pokemon", "mudkip").andThen(Single.just(named)))
         .flatMapMaybe(named -> named.get("pokemon"))
         .doAfterTerminate(() -> RxClients.stop("client"));

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertComplete();
      observer.assertNoErrors();
      observer.assertValue("mudkip");
   }

}
