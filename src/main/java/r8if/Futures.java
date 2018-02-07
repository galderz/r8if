package r8if;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.subjects.CompletableSubject;
import io.reactivex.subjects.MaybeSubject;

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

}
