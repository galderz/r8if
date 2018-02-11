package r8if;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import r8if.server.Servers;

import java.io.Closeable;
import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SingleNode {

   private static final Log log = LogFactory.getLog(SingleNode.class);

   static RxMap<String, String> map;
   private static Closeable local;

   @BeforeClass
   public static void before() {
      local = Servers.startIfNotRunning(Servers::local);

      Single<RxMap<String, String>> value =
         RxMap.from("default", new ConfigurationBuilder());

      TestObserver<RxMap<String, String>> observer = new TestObserver<>();
      value.subscribe(observer);
      boolean terminated = observer.awaitTerminalEvent(10, SECONDS);
      if (!terminated)
         throw new AssertionError("Unable to create RxMap instance");

      map = observer.values().get(0);
   }

   @AfterClass
   public static void after() throws IOException {
      Completable stop = map.client().stop();
      TestObserver<Void> observer = new TestObserver<>();
      stop.subscribe(observer);
      boolean terminated = observer.awaitTerminalEvent(5, SECONDS);
      if (!terminated)
         log.debugf("Unable to complete stopping client");

      local.close();
   }

   @Test
   public void testGetOnly() {
      Maybe<String> value = map.get("10");

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValueCount(0);
   }

   @Test
   public void testPutThenGet() {
      Maybe<String> value =
         map.put("11", "metapod")
            .andThen(map.get("11"));

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue("metapod");
   }

   @Test
   public void testGetThenPut() {
      Maybe<String> value =
         map.get("12")
            // put only happens if get returns nothing
            .switchIfEmpty(map.put("12", "butterfree").toMaybe())
            // get only happens if put happens
            // if first get returned something, put and 2nd get would not happen
            .switchIfEmpty(map.get("12"));

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue("butterfree");
   }

   @Test
   public void testPutIfAbsent() {
      Single<Boolean> isAbsent =
         map.putIfAbsent("13", "weedle");

      TestObserver<Boolean> observer = new TestObserver<>();
      isAbsent.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue(true);
   }

   @Test
   public void testPutIfAbsentNot() {
      Single<Boolean> isAbsent =
         map.put("14", "snorlax")
            .andThen(map.putIfAbsent("14", "kakuna"));

      TestObserver<Boolean> observer = new TestObserver<>();
      isAbsent.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue(false);
   }

   @Test
   public void testPutThenPut() {
      Maybe<String> value =
         map.put("15", "blissey")
            .andThen(map.put("15", "beedrill"))
            .andThen(map.get("15"));

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue("beedrill");
   }

   @Test
   public void testClear() {
      Maybe<String> value =
         map.put("46", "paras")
            .andThen(map.clear())
            .andThen(map.get("46"));

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValueCount(0);
   }

   @Test
   public void testGetThenNoPut() {
      Maybe<String> value =
         map.put("47", "parasect")
            .andThen(map.get("47")) // get then...
            .switchIfEmpty(map.put("47", "mewtwo").toMaybe()); // no put

      TestObserver<String> observer = new TestObserver<>();
      value.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue("parasect");
   }

}
