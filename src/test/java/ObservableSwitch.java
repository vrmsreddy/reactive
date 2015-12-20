import org.junit.Test;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;


public class ObservableSwitch<T> {

    /**
     * We switch from alternative observable if the origin observable is empty
     */
    @Test
    public void testSwitchPerson() {
        Observable.just(new ArrayList<>())
                  .flatMap(persons -> Observable.from(persons)
                                                .switchIfEmpty(Observable.just(new Person("new", 0, "no_sex"))))
                  .subscribe(person -> {
                      System.out.println(person.toString());
                  });

    }

    /**
     * We dont switch from alternative observable because the origin observable is not empty
     */
    @Test
    public void testNoSwitchPerson() {
        List<Person> people = new ArrayList<>();
        people.add(new Person("Pablo", 34, "male"));
        Observable.just(people)
                  .flatMap(persons -> Observable.from(persons)
                                                .switchIfEmpty(Observable.just(new Person("new", 0, "no_sex"))))
                  .subscribe(person -> {
                      System.out.println(person.toString());
                  });

    }

}