package com.example.reactivedemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.core.query.N1qlPrimaryIndexed;
import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

@SpringBootApplication
@EnableReactiveCouchbaseRepositories
public class ReactivedemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactivedemoApplication.class, args);
	}


}

@org.springframework.web.bind.annotation.RestController
class RestController {

	@Autowired
	private UserReactiveRepository repository;

	@Autowired
	private UserEventsReactiveRepository evtRep;

	private String newId() {
		return UUID.randomUUID().toString();
	}


	private Mono<User> save(User s){
		System.out.println("salvandoooooo userrrr "+s.getId());
		return repository.save(s);
	}

	private Mono<UserEvents> saveEvt(UserEvents s){
		System.out.println("salvandoooooo eventooo "+s.getId());
		return evtRep.save(s);
	}


	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/test")
	Publisher<User> test() {
		List<User> users = Arrays.asList( new User(newId(), "denis", "pass"),
					new User(newId(), "john", "pass"),
					new User(newId(), "matthew", "pass"),
				new User(newId(), "john", "pass"),
				new User(newId(), "matthew", "pass"),
				new User(newId(), "john", "pass"),
				new User(newId(), "matthew", "pass")
				);

		 repository.deleteAll().block();

		 //List<User> result = new ArrayList<>();
		return Flux.fromIterable(users)
							.flatMap(this::save)
							.map(e-> new UserEvents(newId(), e.getId(), Arrays.asList("USER_CREATED")))
							.flatMap(evt->saveEvt(evt))
							.flatMap(e->repository.findById(e.getUserId()));

	}

//	@RequestMapping(value = "/")
//	Publisher<String> test() {
//
//		List<User> users = Arrays.asList( new User(null, "denis", "pass"),
//				new User(null, "john", "pass"),
//				new User(null, "matthew", "pass"),
//				new User(null,"benjamin", "pass")
//		);
//
//		repository.deleteAll();
//
//		Flux<User> fluxUsers = Flux.fromIterable(users);
//		Flux<User> savedUsers = fluxUsers.flatMap(repository::save);
//		Flux<UserEvents> events = savedUsers.map(e-> new UserEvents(null, e.getId(), Arrays.asList("USER_CREATED")));
//		return events.flatMap(evtRep::save).map(e->e.getUserId());
//	}

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
class UserEvents {

	@Id
	private String id;
	private String userId;
	private List<String> events;
}

@N1qlPrimaryIndexed
interface UserReactiveRepository extends ReactiveCouchbaseRepository<User,String> {

}

@N1qlPrimaryIndexed
interface UserEventsReactiveRepository extends ReactiveCouchbaseRepository<UserEvents,String> {

}



//##########1
//	@RequestMapping("/")
//	Publisher<User> test() {
//		return Mono.just(new User("denis", "password"));
//	}

//##########2

//	@RequestMapping("/")
//	Publisher<User> test() {
//		Flux<User> users = Flux.<User>generate(e-> e.next(new User("denis", ""))).take(20);
//		return users;
//	}

//##########3
//	@RequestMapping(value = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//	Publisher<User> test() {
//		Flux<User> users = Flux.<User>generate(e-> e.next(new User("denis", "")))
//				.delayElements(Duration.ofSeconds(1));
//		return users;
//	}


//##########4
//@N1qlPrimaryIndexed
//interface UserReactiveRepository extends ReactiveCouchbaseRepository<User,String> {
//
//}

//	@Autowired
//	private UserReactiveRepository repository;

//@N1qlPrimaryIndexed
//interface UserEventsReactiveRepository extends ReactiveCouchbaseRepository<UserEvents,String> {
//
//}

//	private String newId() {
//		return UUID.randomUUID().toString();
//	}
//
//	@RequestMapping(value = "/")
//	Publisher<User> test() {
//
//		List<User> users = Arrays.asList( new User(newId(), "denis", "pass"),
//					new User(newId(), "john", "pass"),
//					new User(newId(), "matthew", "pass"),
//					new User(newId(),"benjamin", "pass")
//				);
//
//		repository.deleteAll();
//
//		return Flux.fromIterable(users)
//				.flatMap(e -> save(e))
//				.map(e-> new UserEvents(newId(), e.getId(), Arrays.asList("USER_CREATED")))
//				.flatMap(e->saveEvt(e))
//				.flatMap(e->repository.findById(e.getUserId()));
//	}

//##########5
//	private Mono<User> save(User s){
//		System.out.println("salvandoooooo userrrr "+s.getId());
//		return repository.save(s);
//	}
//
//	private Mono<UserEvents> saveEvt(UserEvents s){
//		System.out.println("salvandoooooo eventooo "+s.getId());
//		return evtRep.save(s);
//	}

//##########5


// ##########6

