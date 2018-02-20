package r8if;

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.configuration.BasicConfiguration;
import org.infinispan.commons.util.Either;

public final class RxConfigs {

   public static ConfigurationBuilder client() {
      return new ConfigurationBuilder();
   }

   public static Either<String, BasicConfiguration> map(BasicConfiguration cfg) {
      return Either.newRight(() ->
         "<infinispan><cache-container>"
            + cfg.toXMLString()
            + "</cache-container></infinispan>");
   }

}
