package r8if;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import org.infinispan.query.dsl.Query;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import r8if.model.Pokemon;
import r8if.util.Servers;

import java.io.Closeable;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SingleNodeQuery {

   private static final Log log = LogFactory.getLog(SingleNodeQuery.class);

   static RxMap<String, Pokemon> map;
   private static Closeable local;

   @BeforeClass
   public static void before() {
      local = Servers.startIfNotRunning(Servers::local);

      Single<RxMap<String, Pokemon>> value = RxClient
         .from(RxConfigs.client())
         .flatMap(c ->
            // TODO Cache name should not be needed (ISPN-8851 or use workaround with StAx)
            c.createRxMap("single-node-query"
               , RxConfigs.map(() -> "<local-cache name=\"configuration\"/>"))
         );

      TestObserver<RxMap<String, Pokemon>> observer = new TestObserver<>();
      value.subscribe(observer);
      boolean terminated = observer.awaitTerminalEvent(10, SECONDS);
      if (!terminated)
         throw new AssertionError("Unable to create RxMap instance");
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValueCount(1);

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
   public void testQuery() {
      final String k1 = "197", k2 = "198", k3 = "143";
      final Pokemon mon1 = new Pokemon("umbreon", "dark");
      final Pokemon mon2 = new Pokemon("murkrow", "dark");
      final Pokemon mon3 = new Pokemon("snorlax", "normal");
      final List<Pokemon> vs = Arrays.asList(mon1, mon2);

      Flowable<Map.Entry<String, Pokemon>> mons = Flowable
         .fromArray(
            new AbstractMap.SimpleEntry<>(k1, mon1)
            , new AbstractMap.SimpleEntry<>(k2, mon2)
            , new AbstractMap.SimpleEntry<>(k3, mon3)
         );

      String queryString =
         "select p from r8if.model.Pokemon where type = :monType";
      Query query = map.query(queryString);
      query.setParameter("monType", "dark");

      final Flowable<Pokemon> results = map
         .putAll(mons)
         .andThen(map.execute(query));

      TestSubscriber<Pokemon> observer = new TestSubscriber<>();
      results.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValueSet(vs);
   }

}
