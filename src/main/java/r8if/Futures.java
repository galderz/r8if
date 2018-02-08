package r8if;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.MaybeSubject;
import io.reactivex.subjects.SingleSubject;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletionStage;

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
