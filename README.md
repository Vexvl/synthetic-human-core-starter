
# Synthetic Human Core Starter

Synthetic Human Core Starter - это Spring Boot стартер, который предоставляет ядро для обработки команд синтетиков. Он реализует аудит действий, очередь задач с приоритетами, сбор метрик и централизованную обработку ошибок. Стартер подключается как зависимость в конечные приложения, например, bishop-prototype.

## Описание

Возможности стартера:

- аннотация `@WeylandWatchingYou` для аудита вызовов методов;
- аспект `AuditAspect`, логирующий вызовы в консоль или отправляющий сообщения в Kafka;
- DTO модель `CommandDto` и маппер `CommandMapper` для преобразования данных;
- модель `Command` с enum `Priority` (COMMON, CRITICAL);
- обработка CRITICAL команд сразу, COMMON - через очередь ThreadPoolExecutor;
- ограничение очереди на 100 задач, выброс `CommandQueueOverflowException` при переполнении;
- сбор метрик с помощью Micrometer;

## Подключение

1. Добавить зависимость в `pom.xml` проекта:

```xml
<dependency>
  <groupId>svs</groupId>
  <artifactId>synthetic-human-core-starter</artifactId>
  <version>0.0.2-SNAPSHOT</version>
</dependency>
```

2. Включить свойства в `application.properties`:

```xml
audit.kafka-enabled=true
audit.kafka-topic=audit-topic
spring.kafka.bootstrap-servers=localhost:9092
```

3. Если Kafka не используется, оставить:
```xml
audit.kafka-enabled=false
```
## Как работает

Контроллер приложения получает POST-запрос с JSON телом, которое валидируется аннотациями (`@NotBlank`, `@Size`) в `CommandDto`. Далее с помощью `CommandMapper` DTO преобразуется в доменную модель `Command`. Сервис `CommandServiceImpl` (помеченный аспектом `@WeylandWatchingYou`) вызывает `CommandProcessor`, который:

- для CRITICAL команд выполняет задачу сразу;
- для COMMON команд ставит задачу в очередь (ограничение 100 задач).

При переполнении очереди выбрасывается `CommandQueueOverflowException`. Все успешные и ошибочные вызовы сопровождаются записью аудита в лог и, если включён Kafka, отправкой сообщения в топик.

Сервис `MetricsService` обновляет метрики:
- `command.queue.size` - текущий размер очереди;
- `commands.by.author` - количество выполненных задач по авторам.

## Метрики

Для включения Spring Boot Actuator:

management.endpoints.web.exposure.include=*

Метрики:
- `command.queue.size` - размер очереди;
- `commands.by.author` - количество команд по каждому автору.

Доступны по адресу:

/actuator/metrics

## Исключения

- `IllegalArgumentException`- если priority не COMMON или CRITICAL, либо время в неправильном формате;
- `CommandQueueOverflowException` - если очередь задач переполнена;
- остальные ошибки могут быть обработаны на стороне приложения через `@RestControllerAdvice`.

## Основные классы

- `AuditAspect`- аспект аудита вызовов;
- `AuditProperties` - конфигурация аудита;
- `WeylandWatchingYou` - аннотация для методов, которые нужно аудировать;
- `CommandDto` - DTO объекта команды;
- `CommandMapper` - преобразование DTO → Command;
- `Command` - модель команды;
- `Priority` - enum приоритета;
- `CommandProcessor` - исполнение команд с учётом приоритета;
- `CommandService` / `CommandServiceImpl` - сервисный слой;
- `SyntheticHumanCoreStarterConfiguration` - конфигурация Spring Boot;
- `CommandQueueOverflowException` - исключение переполнения очереди;
- `MetricsService` - управление метриками.

## Unit-тесты

- `CommandMapperTest` - проверка маппинга и выброса ошибок при некорректных данных;
- `CommandProcessorTest` - проверка логики CRITICAL/COMMON и вызова метрик;
- `CommandServiceImplTest` - проверка вызова процессора из сервиса.

## Сборка и запуск

Сборка проекта:

mvn clean package

Запуск тестов:

mvn test

Java версия:
21

Основные зависимости:
- spring-boot-starter-web;
- spring-boot-starter-validation;
- spring-boot-starter-actuator;
- spring-boot-starter-test;
- lombok.

## Заключение

Synthetic Human Core Starter - это инфраструктурный модуль, который упрощает разработку систем управления командами для синтетиков и других распределённых систем. Он даёт готовые механизмы аудита, обработки задач с приоритетами, мониторинга и обработки ошибок, что ускоряет разработку и повышает надёжность конечных приложений.
