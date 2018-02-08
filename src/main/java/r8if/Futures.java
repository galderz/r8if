package r8if;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.Single;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.MaybeSubject;
import io.reactivex.subjects.SingleSubject;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

final class Futures {

   private Futures() {
   }

   static Completable toCompletable(CompletionStage<?> future) {
      CompletableSubject cs = CompletableSubject.create();

      future.whenComplete(
         (v, t) -> {
            if (t != null)
               cs.onError(t);
            else
               cs.onComplete();
         }
      );

      return cs;
   }

   static <A, B, T> Maybe<T> toMaybe(A a, B b, BiFunction<A, B, CompletionStage<T>> f) {
      return new Maybe<T>() {
         @Override
         protected void subscribeActual(MaybeObserver<? super T> observer) {
            f.apply(a, b).whenComplete(
               (x, t) -> {
                  if (t != null)
                     observer.onError(t);
                  else if (x != null)
                     observer.onSuccess(x);
                  else
                     observer.onComplete();
               }
            );
         }
      };
   }

   static <V> Function<CompletableFuture<V>, Maybe<V>> futureToMaybe() {
      return cf ->
         new Maybe<V>() {
            @Override
            protected void subscribeActual(MaybeObserver<? super V> observer) {
               cf.whenComplete(
                  (v, t) -> {
                     if (t != null)
                        observer.onError(t);
                     else if (v != null)
                        observer.onSuccess(v);
                     else
                        observer.onComplete();
                  }
               );
            }
         };
   }

   static <T> Maybe<T> toMaybe(CompletionStage<T> future) {
      MaybeSubject<T> ms = MaybeSubject.create();

      future.whenComplete(
         (v, t) -> {
            if (t != null)
               ms.onError(t);
            else if (v != null)
               ms.onSuccess(v);
            else
               ms.onComplete();
         }
      );

      return ms;
   }

   public static <T> Single<T> toSingle(CompletionStage<T> future) {
      SingleSubject<T> cs = SingleSubject.create();

      future.whenComplete((v, e) -> {
         if (e != null)
            cs.onError(e);
         else if (v != null)
            cs.onSuccess(v);
         else
            cs.onError(new NoSuchElementException());
      });

      return cs;
   }

}
