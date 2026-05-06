# GraphQL

## 1. Що таке GraphQL?

GraphQL — це мова запитів для API та середовище виконання цих запитів, розроблена Facebook у 2012 році та опублікована як open-source у 2015. GraphQL дозволяє клієнту точно вказати, які дані йому потрібні, і отримати їх в одному запиті.

### Основні переваги

- **Точне отримання даних** — клієнт запитує лише ті поля, які йому потрібні (без over-fetching та under-fetching)
- **Один endpoint** — всі запити надсилаються на один URL (зазвичай `/graphql`)
- **Строга типізація** — схема визначає всі можливі типи та операції
- **Інтроспекція** — API самодокументується, клієнт може дізнатися структуру схеми
- **Еволюція без версій** — нові поля додаються без порушення існуючих клієнтів

### Порівняння з REST

| Характеристика | REST | GraphQL |
|---------------|------|---------|
| Endpoints | Багато (`/users`, `/users/1/posts`) | Один (`/graphql`) |
| Отримання даних | Фіксована структура відповіді | Клієнт визначає структуру |
| Over-fetching | Часто | Відсутній |
| Under-fetching | Часто (N+1 запитів) | Відсутній |
| Версіонування | `/api/v1`, `/api/v2` | Еволюція схеми |
| Кешування | Просте (HTTP cache) | Складніше (потребує спеціальних рішень) |

## 2. Основні концепції

### Schema Definition Language (SDL)

GraphQL використовує SDL для опису типів та структури API:

```graphql
type Student {
    id: ID!
    firstName: String!
    lastName: String!
    email: String!
    group: Group!
    grades: [Grade!]!
}

type Group {
    id: ID!
    name: String!
    students: [Student!]!
}

type Grade {
    id: ID!
    subject: String!
    value: Int!
    student: Student!
}
```

### Скалярні типи

| Тип | Опис |
|-----|------|
| `Int` | 32-бітне ціле число |
| `Float` | Число з плаваючою точкою |
| `String` | Рядок UTF-8 |
| `Boolean` | `true` або `false` |
| `ID` | Унікальний ідентифікатор (серіалізується як String) |

### Модифікатори типів

- `String` — nullable поле (може бути `null`)
- `String!` — non-null поле (обов'язкове)
- `[String]` — nullable список nullable рядків
- `[String!]!` — обов'язковий список обов'язкових рядків

## 3. Операції

### Query (Запити)

Запити використовуються для читання даних:

```graphql
query {
    students {
        id
        firstName
        lastName
        group {
            name
        }
    }
}
```

Запит з аргументами:

```graphql
query {
    student(id: "1") {
        firstName
        lastName
        grades {
            subject
            value
        }
    }
}
```

### Mutation (Мутації)

Мутації використовуються для створення, оновлення та видалення даних:

```graphql
mutation {
    createStudent(input: {
        firstName: "Іван"
        lastName: "Петренко"
        email: "petrenk@example.com"
        groupId: "1"
    }) {
        id
        firstName
        lastName
    }
}
```

### Subscription (Підписки)

Підписки дозволяють отримувати дані в реальному часі через WebSocket:

```graphql
subscription {
    gradeAdded(studentId: "1") {
        subject
        value
    }
}
```

## 4. Додаткові можливості SDL

### Input типи

Використовуються для передачі складних аргументів у мутаціях:

```graphql
input CreateStudentInput {
    firstName: String!
    lastName: String!
    email: String!
    groupId: ID!
}

type Mutation {
    createStudent(input: CreateStudentInput!): Student!
}
```

### Enum

```graphql
enum Semester {
    FIRST
    SECOND
}

type Grade {
    id: ID!
    subject: String!
    value: Int!
    semester: Semester!
}
```

### Interface

```graphql
interface Person {
    id: ID!
    firstName: String!
    lastName: String!
    email: String!
}

type Student implements Person {
    id: ID!
    firstName: String!
    lastName: String!
    email: String!
    group: Group!
}

type Teacher implements Person {
    id: ID!
    firstName: String!
    lastName: String!
    email: String!
    department: String!
}
```

### Union

```graphql
union SearchResult = Student | Teacher | Group

type Query {
    search(query: String!): [SearchResult!]!
}
```

## 5. Змінні та фрагменти

### Змінні

```graphql
query GetStudent($id: ID!) {
    student(id: $id) {
        firstName
        lastName
    }
}
```

Змінні передаються окремо як JSON:

```json
{
    "id": "1"
}
```

### Фрагменти

Фрагменти дозволяють перевикористовувати набори полів:

```graphql
fragment StudentInfo on Student {
    id
    firstName
    lastName
    email
}

query {
    student(id: "1") {
        ...StudentInfo
        group {
            name
        }
    }
}
```

## 6. Spring for GraphQL

Spring for GraphQL — офіційна інтеграція GraphQL у Spring-екосистему (замінює застарілий graphql-java-kickstart).

### Залежності (Maven)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### Структура проєкту

```
src/
├── main/
│   ├── java/
│   │   └── com/example/
│   │       ├── controller/
│   │       │   └── StudentController.java
│   │       ├── model/
│   │       │   └── Student.java
│   │       ├── repository/
│   │       │   └── StudentRepository.java
│   │       └── service/
│   │           └── StudentService.java
│   └── resources/
│       └── graphql/
│           └── schema.graphqls
```

### Визначення схеми

Файл `src/main/resources/graphql/schema.graphqls`:

```graphql
type Query {
    students: [Student!]!
    student(id: ID!): Student
}

type Mutation {
    createStudent(input: CreateStudentInput!): Student!
    updateStudent(id: ID!, input: UpdateStudentInput!): Student!
    deleteStudent(id: ID!): Boolean!
}

type Student {
    id: ID!
    firstName: String!
    lastName: String!
    email: String!
    group: Group!
    grades: [Grade!]!
}

type Group {
    id: ID!
    name: String!
    students: [Student!]!
}

type Grade {
    id: ID!
    subject: String!
    value: Int!
}

input CreateStudentInput {
    firstName: String!
    lastName: String!
    email: String!
    groupId: ID!
}

input UpdateStudentInput {
    firstName: String
    lastName: String
    email: String
    groupId: ID
}
```

### Controller (анотаційний підхід)

```java
@Controller
public class StudentController {

    private final StudentService studentService;

    @QueryMapping
    public Flux<Student> students() {
        return studentService.findAll();
    }

    @QueryMapping
    public Mono<Student> student(@Argument String id) {
        return studentService.findById(id);
    }

    @MutationMapping
    public Mono<Student> createStudent(@Argument CreateStudentInput input) {
        return studentService.create(input);
    }

    @MutationMapping
    public Mono<Student> updateStudent(@Argument String id, @Argument UpdateStudentInput input) {
        return studentService.update(id, input);
    }

    @MutationMapping
    public Mono<Boolean> deleteStudent(@Argument String id) {
        return studentService.delete(id);
    }

    @SchemaMapping(typeName = "Student", field = "group")
    public Mono<Group> group(Student student) {
        return groupService.findById(student.getGroupId());
    }

    @SchemaMapping(typeName = "Student", field = "grades")
    public Flux<Grade> grades(Student student) {
        return gradeService.findByStudentId(student.getId());
    }
}
```

### Основні анотації

| Анотація | Опис |
|----------|------|
| `@QueryMapping` | Маппінг на поле типу Query |
| `@MutationMapping` | Маппінг на поле типу Mutation |
| `@SubscriptionMapping` | Маппінг на поле типу Subscription |
| `@SchemaMapping` | Маппінг на поле будь-якого типу |
| `@Argument` | Прив'язка аргументу GraphQL до параметра методу |
| `@BatchMapping` | Batch-завантаження для вирішення проблеми N+1 |

## 7. Проблема N+1 та DataLoader

### Проблема

При запиті списку студентів з групами, для кожного студента виконується окремий запит до БД для отримання групи — це проблема N+1.

### Рішення: @BatchMapping

```java
@Controller
public class StudentController {

    @BatchMapping
    public Flux<Group> group(List<Student> students) {
        List<String> groupIds = students.stream()
            .map(Student::getGroupId)
            .distinct()
            .toList();
        return groupService.findByIds(groupIds);
    }
}
```

### Рішення: DataLoader

```java
@Configuration
public class DataLoaderConfig {

    @Bean
    public BatchLoaderRegistry batchLoaderRegistry(GroupService groupService) {
        return registry -> registry.forTypePair(String.class, Group.class)
            .registerMappedBatchLoader((groupIds, env) ->
                groupService.findByIds(groupIds)
                    .collectMap(Group::getId));
    }
}
```

## 8. Пагінація

### Offset-based

```graphql
type Query {
    students(page: Int = 0, size: Int = 10): StudentPage!
}

type StudentPage {
    content: [Student!]!
    totalElements: Int!
    totalPages: Int!
}
```

### Cursor-based (Connection pattern)

```graphql
type Query {
    students(first: Int, after: String): StudentConnection!
}

type StudentConnection {
    edges: [StudentEdge!]!
    pageInfo: PageInfo!
}

type StudentEdge {
    node: Student!
    cursor: String!
}

type PageInfo {
    hasNextPage: Boolean!
    hasPreviousPage: Boolean!
    startCursor: String
    endCursor: String
}
```

## 9. Обробка помилок

GraphQL повертає помилки у стандартному форматі:

```json
{
    "errors": [
        {
            "message": "Студента не знайдено",
            "locations": [{"line": 2, "column": 3}],
            "path": ["student"],
            "extensions": {
                "classification": "NOT_FOUND"
            }
        }
    ],
    "data": {
        "student": null
    }
}
```

### Кастомна обробка помилок

```java
@Component
public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof NotFoundException) {
            return GraphqlErrorBuilder.newError(env)
                .message(ex.getMessage())
                .errorType(ErrorType.NOT_FOUND)
                .build();
        }
        return null;
    }
}
```

## 10. Тестування

### GraphQlTester

```java
@GraphQlTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private StudentService studentService;

    @Test
    void shouldReturnAllStudents() {
        when(studentService.findAll()).thenReturn(Flux.just(
            new Student("1", "Іван", "Петренко", "ivan@example.com")
        ));

        graphQlTester.document("""
                query {
                    students {
                        id
                        firstName
                        lastName
                    }
                }
            """)
            .execute()
            .path("students")
            .entityList(Student.class)
            .hasSize(1);
    }

    @Test
    void shouldCreateStudent() {
        graphQlTester.document("""
                mutation {
                    createStudent(input: {
                        firstName: "Іван"
                        lastName: "Петренко"
                        email: "ivan@example.com"
                        groupId: "1"
                    }) {
                        id
                        firstName
                    }
                }
            """)
            .execute()
            .path("createStudent.firstName")
            .entity(String.class)
            .isEqualTo("Іван");
    }
}
```

## 11. Конфігурація

`application.yml`:

```yaml
spring:
  graphql:
    graphiql:
      enabled: true
      path: /graphiql
    schema:
      printer:
        enabled: true
    path: /graphql
```

- **GraphiQL** — інтерактивна IDE для тестування запитів (доступна за `/graphiql`)
- **Schema printer** — дозволяє отримати повну схему за запитом

## 12. Безпека

```java
@Controller
public class StudentController {

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<Student> students() {
        return studentService.findAll();
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Student> createStudent(@Argument CreateStudentInput input) {
        return studentService.create(input);
    }
}
```

## 13. Найкращі практики

1. **Використовуйте Input типи** для мутацій замість окремих аргументів
2. **Реалізуйте пагінацію** для списків, що можуть бути великими
3. **Використовуйте @BatchMapping** для вирішення проблеми N+1
4. **Додавайте валідацію** на рівні input типів та сервісів
5. **Обмежуйте глибину запитів** для захисту від зловмисних запитів
6. **Використовуйте фрагменти** для перевикористання полів на клієнті
7. **Документуйте схему** за допомогою описів у SDL:

```graphql
"""
Студент навчального закладу
"""
type Student {
    "Унікальний ідентифікатор студента"
    id: ID!
    "Ім'я студента"
    firstName: String!
}
```
