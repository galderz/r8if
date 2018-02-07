package r8if;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// TODO Change name
public final class RxClients {

   private static final ConcurrentMap<String, RxClient> CLIENTS = new ConcurrentHashMap<>();

   public static Single<RxClient> rxClient(String clientName, ConfigurationBuilder cfg) {
      return RxClient.from(cfg)
         .doOnSuccess(rxClient -> CLIENTS.put(clientName, rxClient))
         .subscribeOn(Schedulers.io());
   }

   public static <K, V> Single<RxMap<K, V>> rxMap(String clientName, ConfigurationBuilder cfg, String cacheName) {
      return RxClient.from(cfg)
         .doOnSuccess(client -> CLIENTS.put(clientName, client))
         .flatMap(client -> client.<K, V>rxMap(cacheName))
         .subscribeOn(Schedulers.io());
   }

   public static Completable stop(String clientName) {
      RxClient client = CLIENTS.get(clientName);

      return client == null
         ? Completable.complete()
         : client.stop();
   }

}
