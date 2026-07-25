# Spring MVC Fundamentals 08

Spring MVC와 Servlet 기반 웹 애플리케이션의 기본 흐름을 학습하고 예제 코드로 정리한 저장소입니다.

Servlet, HTTP 요청/응답, Controller, Request Mapping, Model, View Template 등 Spring MVC가 웹 요청을 처리하는 기본 구조를 학습했습니다.

## 학습 목적

Spring MVC에서 클라이언트 요청이 들어온 뒤 Controller를 거쳐 View 또는 응답 데이터로 반환되는 흐름을 이해하기 위해 정리했습니다.

Servlet 기반 웹 처리 흐름부터 Spring MVC 구조까지 단계적으로 확인하면서, 백엔드 웹 애플리케이션의 요청 처리 방식을 익히는 데 중점을 두었습니다.

## 학습 내용

- Servlet 기본 구조
- HTTP 요청과 응답 처리
- Request Mapping
- Controller 역할
- Model을 활용한 데이터 전달
- View Template 연동
- MVC 패턴의 기본 흐름
- 웹 애플리케이션 요청 처리 구조
- HTML 화면 응답 기초

## 디렉터리 구조

```text
spring-mvc-fundamentals-08
├── gradle
├── src
│   ├── main
│   └── test
│       └── java
│           └── hello
│               └── servlet
├── build.gradle
├── gradlew
├── gradlew.bat
└── settings.gradle
```

## 학습 포인트

- Servlet이 HTTP 요청과 응답을 처리하는 기본 흐름을 학습했습니다.
- Spring MVC에서 Controller가 요청을 받고 응답을 반환하는 구조를 이해했습니다.
- Request Mapping을 통해 URL과 Controller 메서드를 연결하는 방식을 익혔습니다.
- Model을 사용해 Controller에서 View로 데이터를 전달하는 흐름을 확인했습니다.
- MVC 패턴을 통해 요청 처리, 비즈니스 로직, 화면 렌더링 역할을 분리하는 방식을 학습했습니다.

## 실행 환경

- Java
- Spring Boot
- Spring MVC
- Gradle
- IntelliJ IDEA
- HTML

## 참고
- 코드 출처 : 스프링 MVC 1편 - 백엔드 웹 개발 핵심 기술
