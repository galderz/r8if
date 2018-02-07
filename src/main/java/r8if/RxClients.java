package r8if;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RxClients {

   private static final ConcurrentMap<String, RxClient> CLIENTS = new ConcurrentHashMap<>();

   public static Single<RxClient> rxClient(String clientName, ConfigurationBuilder cfg) {
      return RxClient.from(cfg)
         .doOnSuccess(rxClient -> CLIENTS.put(clientName, rxClient))
         .subscribeOn(Schedulers.io());
   }

   public static Completable stop(String clientName) {
      RxClient client = CLIENTS.get(clientName);

      return client == null
         ? Completable.complete()
         : client.stop();
   }

}
