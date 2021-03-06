package rx.observables.creating;

import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;


/**
 * @author Pablo Perez
 * Normally when we create an observable this one is consume by a observer(Subscription).
 * which is created by subscribe method providing an Action function for onNext, onError and onComplete.
 * Those functions are just like a Java 8 consumer functions
 * Once that we subscribe and we create an observer the main thread is block unitl the observer is unsubscribe from the observable.
 * This happens once the last item of the observable has been process through the pipeline.
 */
public class ObservableSubscription {

    private String foo="empty";

    int total = 0;

    /**
     * In this test we prove how when we subscribe a observable, this one block the thread until emit all items
     */
    @Test
    public void testObservableSubscriptionBlockMainThread() {
        Integer[] numbers = {0, 1, 2, 3, 4};

        Observable.from(numbers)
                  .flatMap(Observable::just)
                  .doOnNext(number->{
                      sleep();
                  })
                  .subscribe(number -> total+=number);
        System.out.println("I finish after all items are emitted:"+total);
    }

    /**
     * Here since we use delay, that makes the pipeline asynchronous,
     * we can check how only when the observable has emit all items the observer is unsubscribed
     */
    @Test
    public void testObservableWaitForUnsubscribed() {
        Subscription subscription = Observable.just(1)
                                              .delay(5, TimeUnit.MILLISECONDS)
                                              .subscribe(number -> foo = "Subscription finish");
        while (!subscription.isUnsubscribed()) {
            System.out.println("wait for subscription to finish");
        }
        System.out.println(foo);
    }


    /**
     * In this example we create another observable through the subscription, and we subscribe to be informed when the previous observer was unsubscribed
     * Since we dont want to block our program, we will run in another thread,
     * then and once the pipeline continue we return the result event to the main thread(immediate)
     *
     */
    @Test
    public void testObservableWaitForUnsubscribedListener() {
        Subscription subscription = Observable.just(1)
                                              .delay(1, TimeUnit.SECONDS)
                                              .subscribe(number -> foo = "Subscription finish");
        Scheduler mainThread = Schedulers.immediate();
        Observable.just(subscription)
                  .subscribeOn(Schedulers.newThread())
                  .doOnNext(s ->{
                      while(!s.isUnsubscribed()){
                          sleep();
                      }
                  }).observeOn(mainThread)
                  .subscribe(u-> System.out.println("Observer unsubscribed:"+u.toString()));

        new TestSubscriber((Observer) subscription)
                .awaitTerminalEvent(2, TimeUnit.SECONDS);
    }


    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
