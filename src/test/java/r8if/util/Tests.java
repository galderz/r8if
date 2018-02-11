package r8if.util;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import r8if.RxMap;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class Tests {

   private Tests() {
   }

   public static <T> void await(Maybe<T> m) {
      TestObserver<T> observer = new TestObserver<>();
      m.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValueCount(0);
   }

   public static <T> void await(T expected, Maybe<T> m) {
      TestObserver<T> observer = new TestObserver<>();
      m.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue(expected);
   }

   public static <T> void await(T expected, Single<T> s) {
      TestObserver<T> observer = new TestObserver<>();
      s.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue(expected);
   }

   public static <K, V> void cleanup(K key, RxMap<K, V> map) {
      Single<Boolean> removed = map.remove(key);
      TestObserver<Boolean> observer = new TestObserver<>();
      removed.subscribe(observer);

      observer.awaitTerminalEvent(5, SECONDS);
      observer.assertNoErrors();
      observer.assertComplete();
      observer.assertValue(true);
   }

}
