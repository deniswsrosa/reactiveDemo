package com.example.reactivedemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.query.N1qlPrimaryIndexed;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.core.query.ViewIndexed;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@SpringBootApplication
public class ReactiveDemo {


    public static void main(String[] args) {
        SpringApplication.run(ReactiveDemo.class, args);
    }

}

@Slf4j
@RestController
class RestControl {


    @Autowired
    private UserReactiveRepository userReactiveRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEventReactiveRepository userEventReactiveRepository;


    @RequestMapping(value = "/stream/users", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> streamUsers() {
        return userReactiveRepository.findAll();
    }

    @RequestMapping("/")
    public Publisher<User> test() {
        userEventReactiveRepository.deleteAll().block();
        userReactiveRepository.deleteAll().block();

        return Flux.fromIterable(Arrays.asList( new User(newId(), "denis", "pass"),
                new User(newId(), "alex", "pass"),
                new User(newId(), "alex", "pass"),
                new User(newId(), "venkar", "pass"),
                new User(newId(), "alex", "pass"),
                new User(newId(), "venkar", "pass"),
                new User(newId(), "alex", "pass"),
                new User(newId(), "venkar", "pass"),
                new User(newId(), "alex", "pass"),
                new User(newId(), "venkar", "pass"),
                new User(newId(), "alex", "pass"),
                new User(newId(), "venkar", "pass"),
                new User(newId(), "alex", "pass"),
                new User(newId(), "venkar", "pass"),
                new User(newId(), "alex", "pass"),
                new User(newId(), "venkar", "pass"),
                    new User(newId(), "venkar", "pass")))
                .flatMap(this::saveUser)
                .map(e-> new UserEvent(newId(), e.getId(), Arrays.asList("USER_CREATED")))
                .flatMap(this::saveEvent)
                .thenMany(userReactiveRepository.findAll().limitRequest(100));
    }


    public Mono<User> saveUser(User user) {
        log.warn("------- saving user");
        if(user.getUsername() == null) {
            throw new IllegalArgumentException("Hey, username can't be null");
        }
        return userReactiveRepository.save(user);
    }

    public Mono<UserEvent> saveEvent(UserEvent userEvent) {
        log.warn("*********** saving event");
        return userEventReactiveRepository.save(userEvent);
    }


    @RequestMapping("/findByUsername")
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @RequestMapping("/findByUsernameLike")
    public List<User> findByUsernameLike(String username) {
        return userRepository.findByUsernameLike(username+"%");
    }

    @RequestMapping("/save")
    public Mono<ResponseEntity<User>> save(@RequestBody User user) {
        return Mono.just(user)
                .map(e-> {e.setId(newId());return e;})
                .flatMap(this::saveUser)
                .onErrorResume(e-> Mono.error( new RuntimeException("Could not save the user")))
                .map( e-> ResponseEntity.ok(e));
    }


    @RequestMapping("/delete")
    public Mono<ResponseEntity<Void>> delete(String userId) {
        return userReactiveRepository.findById(userId)
                .flatMap(e-> userReactiveRepository.delete(e)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }




    private String newId() {
        return UUID.randomUUID().toString();
    }

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class User {

    @Id
    private String id;
    private String username;
    private String password;

}


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
class UserEvent {

    @Id
    private String id;
    private String userId;
    private List<String> events;

}

@N1qlPrimaryIndexed
@ViewIndexed(designDoc = "userEvent")
interface UserEventReactiveRepository extends ReactiveCrudRepository<UserEvent, String> {

}


@N1qlPrimaryIndexed
@ViewIndexed(designDoc = "user")
interface UserReactiveRepository extends ReactiveCrudRepository<User, String> {

    Mono<User> findByUsername(String username);

    Flux<User> findByUsernameLike(String username);

}


@N1qlPrimaryIndexed
@ViewIndexed(designDoc = "user2")
interface UserRepository extends CouchbaseRepository<User, String> {

    User findByUsername(String username);

    List<User> findByUsernameLike(String username);

}
