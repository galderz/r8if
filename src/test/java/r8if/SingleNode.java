package r8if;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.fail;

public class SingleNode {

   private static final Log log = LogFactory.getLog(SingleNode.class);

   @Test
   public void testGetOnly() {
      Maybe<String> value = RxClients
         .<String, String>rxMap("client", new ConfigurationBuilder(), "default")
         .flatMapMaybe(named -> named.get("10"))
         .doAfterTerminate(() -> RxClients.stop("client"));

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValueCount(0);
   }

   @Test
   public void testPutThenGet() {
      Maybe<String> value = RxMap
         .<String, String>from("default", new ConfigurationBuilder())
         .flatMapMaybe(map ->
            map.put("11", "metapod")
               .andThen(map.get("11"))
               .doAfterTerminate(() -> map.client().stop())
         );

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue("metapod");
   }

   @Test
   public void testGetThenPut() {
      Maybe<String> value = RxMap
         .<String, String>from("default", new ConfigurationBuilder())
         .flatMapMaybe(map ->
            map.get("12")
               .isEmpty()
               .flatMapCompletable(notFound ->
                  notFound
                     ? map.put("12", "butterfree")
                     : Completable.error(new AssertionError("Expected no results out of get()"))
               )
               .andThen(map.get("12"))
               .doAfterTerminate(() -> map.client().stop())
         );

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue("butterfree");
   }

   @Test
   public void testPutIfAbsent() {
      // TODO Can it work when values are sent back? (see client config)
      Single<Boolean> isAbsent = RxMap
         .<String, String>from("default", new ConfigurationBuilder())
         .flatMap(map ->
            map.putIfAbsent("13", "weedle")
               .doAfterTerminate(() -> map.client().stop())
         );

      TestObserver<Boolean> observer = new TestObserver<>();
      isAbsent.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue(true);
   }

   @Test
   public void testPutIfAbsentNot() {
      // TODO Can it work when values are sent back? (see client config)
      Single<Boolean> isAbsent = RxMap
         .<String, String>from("default", new ConfigurationBuilder())
         .flatMap(map ->
            map.put("14", "snorlax")
               .andThen(map.putIfAbsent("14", "kakuna"))
               .doAfterTerminate(() -> map.client().stop())
         );

      TestObserver<Boolean> observer = new TestObserver<>();
      isAbsent.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue(false);
   }

}
