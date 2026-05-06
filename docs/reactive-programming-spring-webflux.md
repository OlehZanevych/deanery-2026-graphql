# Реактивне програмування та Spring WebFlux

## 1. Що таке реактивне програмування?

Реактивне програмування — це парадигма програмування, орієнтована на потоки даних та поширення змін. Замість того, щоб явно запитувати дані, програма реагує на їх появу асинхронно.

### Основні принципи

- **Асинхронність** — операції виконуються без блокування потоку виконання
- **Потоки даних (Streams)** — дані розглядаються як послідовність подій у часі
- **Зворотний тиск (Backpressure)** — механізм контролю швидкості передачі даних між виробником і споживачем
- **Неблокуючий ввід/вивід (Non-blocking I/O)** — потік не блокується під час очікування відповіді

### Reactive Streams Specification

Специфікація Reactive Streams визначає 4 основні інтерфейси:

- `Publisher<T>` — виробник даних, публікує елементи для підписників
- `Subscriber<T>` — споживач даних, підписується на Publisher
- `Subscription` — зв'язок між Publisher і Subscriber, дозволяє керувати потоком
- `Processor<T, R>` — одночасно є і Publisher, і Subscriber

## 2. Project Reactor

Project Reactor — це реалізація Reactive Streams для JVM, яка є основою Spring WebFlux.

### Основні типи

#### Mono\<T\>

Реактивний тип, що представляє 0 або 1 елемент:

```java
Mono<String> mono = Mono.just("Hello");
Mono<String> empty = Mono.empty();
Mono<String> error = Mono.error(new RuntimeException("Помилка"));
```

#### Flux\<T\>

Реактивний тип, що представляє 0..N елементів:

```java
Flux<String> flux = Flux.just("A", "B", "C");
Flux<Integer> range = Flux.range(1, 10);
Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
```

### Основні оператори

| Оператор | Опис |
|----------|------|
| `map()` | Трансформація кожного елемента |
| `flatMap()` | Асинхронна трансформація з поверненням Publisher |
| `filter()` | Фільтрація елементів за умовою |
| `zip()` | Об'єднання кількох потоків |
| `merge()` | Злиття потоків в один |
| `switchIfEmpty()` | Альтернативний потік, якщо основний порожній |
| `onErrorResume()` | Обробка помилок з альтернативним потоком |
| `retry()` | Повторна спроба при помилці |

### Приклад ланцюжка операторів

```java
Flux.range(1, 100)
    .filter(n -> n % 2 == 0)
    .map(n -> n * n)
    .take(5)
    .subscribe(System.out::println);
```

## 3. Spring WebFlux

Spring WebFlux — це реактивний веб-фреймворк у складі Spring Framework 5+, альтернатива Spring MVC для побудови неблокуючих веб-додатків.

### Порівняння Spring MVC та Spring WebFlux

| Характеристика | Spring MVC | Spring WebFlux |
|---------------|------------|----------------|
| Модель виконання | Блокуюча (thread-per-request) | Неблокуюча (event loop) |
| Сервер | Tomcat, Jetty | Netty, Tomcat, Jetty |
| API | Servlet API | Reactive Streams |
| Типи повернення | Об'єкти, ResponseEntity | Mono, Flux |
| Масштабованість | Обмежена кількістю потоків | Висока при великій кількості з'єднань |

### Анотаційна модель (Annotation-based)

```java
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public Flux<Student> getAll() {
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Student> getById(@PathVariable String id) {
        return studentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Student> create(@RequestBody Student student) {
        return studentService.save(student);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return studentService.deleteById(id);
    }
}
```

### Функціональна модель (Functional Endpoints)

```java
@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(StudentHandler handler) {
        return RouterFunctions.route()
            .path("/api/students", builder -> builder
                .GET("", handler::getAll)
                .GET("/{id}", handler::getById)
                .POST("", handler::create)
                .DELETE("/{id}", handler::delete)
            )
            .build();
    }
}
```

```java
@Component
public class StudentHandler {

    private final StudentService studentService;

    public Mono<ServerResponse> getAll(ServerRequest request) {
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(studentService.findAll(), Student.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        String id = request.pathVariable("id");
        return studentService.findById(id)
            .flatMap(student -> ServerResponse.ok().bodyValue(student))
            .switchIfEmpty(ServerResponse.notFound().build());
    }
}
```

## 4. Реактивний доступ до даних

### Spring Data Reactive

Spring Data надає реактивні репозиторії для різних баз даних:

- **Spring Data R2DBC** — для реляційних БД (PostgreSQL, MySQL)
- **Spring Data Reactive MongoDB** — для MongoDB
- **Spring Data Reactive Redis** — для Redis

```java
public interface StudentRepository extends ReactiveCrudRepository<Student, String> {

    Flux<Student> findByGroupName(String groupName);

    Mono<Student> findByEmail(String email);
}
```

### R2DBC (Reactive Relational Database Connectivity)

```java
@Configuration
@EnableR2dbcRepositories
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(
            ConnectionFactoryOptions.builder()
                .option(DRIVER, "postgresql")
                .option(HOST, "localhost")
                .option(PORT, 5432)
                .option(DATABASE, "deanery")
                .option(USER, "user")
                .option(PASSWORD, "password")
                .build()
        );
    }
}
```

## 5. WebClient — реактивний HTTP-клієнт

WebClient — це неблокуюча альтернатива RestTemplate:

```java
WebClient webClient = WebClient.builder()
    .baseUrl("http://localhost:8080")
    .build();

Mono<Student> student = webClient.get()
    .uri("/api/students/{id}", id)
    .retrieve()
    .bodyToMono(Student.class);

Flux<Student> students = webClient.get()
    .uri("/api/students")
    .retrieve()
    .bodyToFlux(Student.class);
```

## 6. Обробка помилок

```java
@GetMapping("/{id}")
public Mono<Student> getById(@PathVariable String id) {
    return studentService.findById(id)
        .switchIfEmpty(Mono.error(new NotFoundException("Студента не знайдено")))
        .onErrorResume(e -> Mono.error(new ResponseStatusException(
            HttpStatus.NOT_FOUND, e.getMessage())));
}
```

### Глобальна обробка помилок

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(NotFoundException ex) {
        return Mono.just(ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage())));
    }
}
```

## 7. Тестування реактивного коду

### StepVerifier

```java
@Test
void shouldReturnAllStudents() {
    Flux<Student> students = studentService.findAll();

    StepVerifier.create(students)
        .expectNextCount(3)
        .verifyComplete();
}

@Test
void shouldReturnStudentById() {
    Mono<Student> student = studentService.findById("1");

    StepVerifier.create(student)
        .assertNext(s -> assertEquals("Іванов", s.getLastName()))
        .verifyComplete();
}
```

### WebTestClient

```java
@WebFluxTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldGetAllStudents() {
        webTestClient.get()
            .uri("/api/students")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Student.class)
            .hasSize(3);
    }
}
```

## 8. Коли використовувати Spring WebFlux?

**Підходить для:**
- Додатків з великою кількістю одночасних з'єднань
- Мікросервісів з інтенсивним I/O
- Стрімінгу даних у реальному часі
- Систем з високими вимогами до масштабованості

**Не підходить для:**
- Додатків з переважно CPU-інтенсивними операціями
- Проєктів з блокуючими залежностями (JDBC без R2DBC)
- Простих CRUD-додатків з невеликим навантаженням
- Команд без досвіду реактивного програмування

## 9. Корисні залежності (Maven)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
```
