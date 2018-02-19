package r8if;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.infinispan.client.hotrod.logging.Log;
import org.infinispan.client.hotrod.logging.LogFactory;
import r8if.Fn.TriFunction;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

final class Futures {

   private static final Log log = LogFactory.getLog(Futures.class);

   private Futures() {
   }

   static <A, B, C, T> Completable toCompletable(A a, B b, C c, TriFunction<A, B, C, CompletionStage<T>> f) {
      return Completable.create(source -> {
         f.apply(a, b, c).whenComplete(
            (v, t) -> {
               if (t != null)
                  source.onError(t);
               else
                  source.onComplete();
            }
         );
      });
   }

   static <A, T> Completable toCompletable(A a, String l, Function<A, CompletionStage<T>> f) {
      return Completable.create(source -> {
         log.debugf(l);
         f.apply(a).whenComplete(
            (v, t) -> {
               if (t != null) {
                  log.debugf(t, "%s failed", l);
                  source.onError(t);
               } else {
                  log.debugf("%s completed", l);
                  source.onComplete();
               }
            }
         );
      });
   }

   static <A, B, T> Completable toCompletable(A a, B b, Function<A, String> l, BiFunction<A, B, CompletionStage<T>> f) {
      return Completable.create(source -> {
         log.debugf(l.apply(a));
         f.apply(a, b).whenComplete(
            (v, t) -> {
               if (t != null) {
                  log.debugf(t, "%s failed", l.apply(a));
                  source.onError(t);
               } else {
                  log.debugf("%s completed", l.apply(a));
                  source.onComplete();
               }
            }
         );
      });
   }

   static <A, B, T> Maybe<T> toMaybe(A a, B b, Function<A, String> l, BiFunction<A, B, CompletionStage<T>> f) {
      return Maybe.create(source -> {
         log.debugf(l.apply(a));
         f.apply(a, b).whenComplete(
            (x, t) -> {
               if (t != null) {
                  log.debugf(t, "%s failed", l.apply(a));
                  source.onError(t);
               } else if (x != null) {
                  log.debugf("%s returned %s", l.apply(a), x);
                  source.onSuccess(x);
               } else {
                  log.debugf("%s not found", l.apply(a));
                  source.onComplete();
               }
            }
         );
      });
   }

   static <A, B, T> Single<T> toSingle(A a, B b, BiFunction<A, B, CompletionStage<T>> f) {
      return Single.create(source -> {
         f.apply(a, b)
            .whenComplete(
               (x, t) -> {
                  if (t != null)
                     source.onError(t);
                  else
                     source.onSuccess(x);
               }
            );
      });
   }

   static <A, B, C, T> Single<T> toSingle(A a, B b, C c, TriFunction<A, B, C, CompletionStage<T>> f) {
      return Single.create(source -> {
         f.apply(a, b, c)
            .whenComplete(
               (x, t) -> {
                  if (t != null)
                     source.onError(t);
                  else
                     source.onSuccess(x);
               }
            );
      });
   }

}
