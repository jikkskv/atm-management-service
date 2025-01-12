# atm-management-service

Test project for atm management service

## Requirements

For building and running the application you need:

- [JDK 17](https://www.azul.com/downloads/?version=java-17-lts&os=linux&package=jdk#zulu)
- [Maven 3](https://maven.apache.org)
- [Spring Boot 3.4.1](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes)

## Building a fat jar

```shell
cd ./atm-management-service
mvn clean package
```

## Testing the application

```shell
cd ./atm-management-service
mvn clean test
```

## How to Run

```shell
java -jar ./atm-management-service/atm-management-service.jar
```

## Running the application locally

```shell
mvn spring-boot:run
```

## Implementation details

#### Initializer used:

[Spring Initializer](https://start.spring.io/)

#### Application Properties:

This section describes the configurable properties for the application. These properties can be set in
the `application.yml` file or as environment variables.

##### General Configuration

| Property Name                                          | Default Value | Description             |
|--------------------------------------------------------|---------------|-------------------------|
| `logging.level.com.xyzbank.atm.atm_management_service` | `INFO`        | The root logging level. |

