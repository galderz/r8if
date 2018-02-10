package r8if;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

final class Fn {

   @FunctionalInterface
   interface TriFunction<T, U, V, R> {

      R apply(T t, U u, V v);

   }

}
