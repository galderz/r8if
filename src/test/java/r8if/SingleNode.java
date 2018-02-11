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
import r8if.util.Servers;

import java.io.Closeable;
import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static r8if.util.Tests.await;
import static r8if.util.Tests.cleanup;

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

      await(value);
   }

   @Test
   public void testPutThenGet() {
      final String k = "11";
      final String v = "metapod";

      final Maybe<String> value =
         map.put(k, v)
            .andThen(map.get(k));

      await(v, value);
      cleanup(k, map);
   }

   @Test
   public void testGetThenPut() {
      final String k = "12";
      final String v = "butterfree";

      final Maybe<String> value =
         map.get(k)
            // put only happens if get returns nothing
            .switchIfEmpty(map.put(k, v).toMaybe())
            // get only happens if put happens
            // if first get returned something, put and 2nd get would not happen
            .switchIfEmpty(map.get(k));

      await(v, value);
      cleanup(k, map);
   }

   @Test
   public void testPutIfAbsent() {
      final String k = "13";

      final Single<Boolean> isAbsent =
         map.putIfAbsent(k, "weedle");

      await(true, isAbsent);
      cleanup(k, map);
   }

   @Test
   public void testPutIfAbsentNot() {
      final String k = "14";

      final Single<Boolean> isAbsent =
         map.put(k, "snorlax")
            .andThen(map.putIfAbsent(k, "kakuna"));

      await(false, isAbsent);
      cleanup(k, map);
   }

   @Test
   public void testPutThenPut() {
      final String k = "15";
      final String v = "beedrill";

      final Maybe<String> value =
         map.put(k, "blissey")
            .andThen(map.put(k, v))
            .andThen(map.get(k));

      await(v, value);
      cleanup(k, map);
   }

   @Test
   public void testClear() {
      final String k = "46";

      final Maybe<String> value =
         map.put(k, "paras")
            .andThen(map.clear())
            .andThen(map.get(k));

      await(value);
   }

   @Test
   public void testGetThenNoPut() {
      final String k = "47";
      final String v = "parasect";

      final Maybe<String> value =
         map.put(k, v)
            .andThen(map.get(k)) // get then...
            .switchIfEmpty(map.put(k, "mewtwo").toMaybe()); // no put

      await(v, value);
      cleanup(k, map);
   }

   @Test
   public void testNoRemove() {
      Single<Boolean> removed = map.remove("48");

      await(false, removed);
   }

}
