# 서블릿 요청과 응답 처리 복습

> 서블릿 등록부터 요청 데이터 조회, JSON 변환, HTTP 응답 작성까지 실제 흐름을 중심으로 정리했다.

## 1. 서블릿 등록

스프링 부트 환경에서 애노테이션 기반 서블릿을 찾도록 설정할 수 있다.

```java
@ServletComponentScan
@SpringBootApplication
public class ServletApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                ServletApplication.class,
                args
        );
    }
}
```

서블릿 클래스는 URL과 연결한다.

```java
@WebServlet(
    name = "helloServlet",
    urlPatterns = "/hello"
)
public class HelloServlet extends HttpServlet {

    @Override
    protected void service(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String name = request.getParameter("name");

        response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write("hello " + name);
    }
}
```

브라우저가 `/hello?name=kim`을 요청하면 서블릿 컨테이너가 URL에 맞는 객체를 찾아 `service()`를 실행한다.

---

## 2. 서블릿 컨테이너가 대신하는 일

개발자가 원시 HTTP 문자열을 직접 분해하지 않아도 되도록 컨테이너가 요청과 응답 객체를 만든다.

```text
HTTP 요청 수신
→ 요청 메시지 파싱
→ HttpServletRequest 생성
→ HttpServletResponse 생성
→ 대상 서블릿 호출
→ 응답 객체를 HTTP 메시지로 변환
→ 클라이언트에 전송
```

서블릿 코드는 네트워크 연결이나 헤더 구분보다 애플리케이션 처리에 집중할 수 있다.

---

## 3. HttpServletRequest

요청 객체에서 확인할 수 있는 대표 정보:

- HTTP 메서드
- 요청 URI와 URL
- 쿼리 문자열
- 프로토콜
- 헤더
- 쿠키
- 원격 주소
- 요청 본문
- 요청 속성

```java
request.getMethod();
request.getRequestURI();
request.getQueryString();
request.getHeader("User-Agent");
```

### 요청 범위 저장소

```java
request.setAttribute("member", member);

Member value =
        (Member) request.getAttribute("member");
```

하나의 요청이 처리되는 동안 컨트롤러와 뷰 사이에서 데이터를 전달하는 용도로 사용할 수 있다.

---

## 4. 요청 데이터 전달 방식

### GET 쿼리 파라미터

```http
GET /search?keyword=spring&page=2
```

검색, 필터, 정렬, 페이지 번호처럼 조회 조건에 적합하다.

### HTML Form

```http
POST /members
Content-Type: application/x-www-form-urlencoded

username=kim&age=20
```

브라우저 Form의 기본 전송 형식이다.

### 메시지 본문

```http
POST /members
Content-Type: application/json

{
  "username": "kim",
  "age": 20
}
```

HTTP API에서는 JSON 본문을 자주 사용한다.

---

## 5. 쿼리 파라미터 조회

```java
String username =
        request.getParameter("username");

String age =
        request.getParameter("age");
```

동일한 이름으로 값이 여러 개 전달되면 배열로 조회할 수 있다.

```java
String[] values =
        request.getParameterValues("tag");
```

`getParameter()`는 쿼리 문자열과 `application/x-www-form-urlencoded` 형식의 Form 데이터를 공통된 방식으로 다룰 수 있다.

하지만 JSON 본문은 이 메서드로 자동 변환되지 않는다.

---

## 6. Form 요청

```html
<form action="/request-param" method="post">
    <input name="username">
    <input name="age">
    <button type="submit">전송</button>
</form>
```

Form의 입력 이름이 파라미터 키가 된다.

```java
String username =
        request.getParameter("username");
```

텍스트로 전달되므로 숫자 변환과 유효성 검증은 서버가 처리해야 한다.

```java
int age = Integer.parseInt(
        request.getParameter("age")
);
```

값이 없거나 숫자가 아니면 예외가 발생할 수 있으므로 실제 서비스에서는 검증 로직이 필요하다.

---

## 7. 일반 텍스트 본문 읽기

본문을 직접 읽을 때는 입력 스트림을 사용한다.

```java
String body = request.getReader()
                     .lines()
                     .collect(
                         Collectors.joining(
                             System.lineSeparator()
                         )
                     );
```

본문은 한 번 소비하면 다시 읽기 어려울 수 있다. 필터나 로깅 코드에서 먼저 읽어 버리면 뒤의 컨트롤러가 사용할 수 없으므로 래퍼나 캐싱 전략이 필요하다.

---

## 8. JSON 요청 처리

DTO를 준비한다.

```java
public class MemberRequest {

    private String username;
    private int age;

    // getter, setter
}
```

Jackson의 `ObjectMapper`로 JSON을 객체로 변환한다.

```java
private final ObjectMapper objectMapper =
        new ObjectMapper();

MemberRequest data =
        objectMapper.readValue(
                request.getInputStream(),
                MemberRequest.class
        );
```

`Content-Type: application/json`을 사용했다는 전제 아래 JSON 형식과 Java 필드 구조가 맞아야 한다.

직접 `ObjectMapper`를 호출할 수 있지만 Spring MVC에서는 메시지 변환기가 이 과정을 자동화한다.

---

## 9. HttpServletResponse

응답 객체를 통해 다음 정보를 설정한다.

- 상태 코드
- 응답 헤더
- 콘텐츠 타입
- 문자 인코딩
- 쿠키
- 리다이렉션
- 본문

```java
response.setStatus(
        HttpServletResponse.SC_OK
);
response.setContentType("text/plain");
response.setCharacterEncoding("utf-8");
response.getWriter().write("ok");
```

헤더는 다음처럼 추가할 수 있다.

```java
response.setHeader(
        "Cache-Control",
        "no-store"
);
```

편의 메서드도 제공된다.

```java
response.sendRedirect("/members");
response.addCookie(cookie);
```

---

## 10. HTML 응답

```java
response.setContentType("text/html");
response.setCharacterEncoding("utf-8");

PrintWriter writer = response.getWriter();
writer.println("<html>");
writer.println("<body>");
writer.println("<h1>Hello</h1>");
writer.println("</body>");
writer.println("</html>");
```

동작은 가능하지만 화면이 커질수록 Java 문자열로 HTML을 관리하기 어렵다. 뷰 템플릿이나 JSP로 화면 책임을 분리하는 이유다.

---

## 11. JSON 응답

```java
MemberResponse result =
        new MemberResponse("kim", 20);

String json =
        objectMapper.writeValueAsString(result);

response.setContentType("application/json");
response.setCharacterEncoding("utf-8");
response.getWriter().write(json);
```

객체를 문자열로 직접 조립하지 않고 JSON 라이브러리를 사용해야 escaping, 타입 변환, 중첩 구조를 안전하게 처리할 수 있다.

단, 엔티티나 내부 객체를 그대로 응답하면 노출하면 안 되는 필드까지 포함될 수 있으므로 응답 DTO를 분리하는 편이 좋다.

---

## 12. 직접 서블릿 처리의 한계

서블릿 API만으로도 모든 기능을 구현할 수 있지만 다음 코드가 반복된다.

- 파라미터 읽기와 타입 변환
- JSON 직렬화와 역직렬화
- 상태 코드와 헤더 설정
- URL 매핑
- 뷰 경로 선택
- 유효성 검증
- 예외 처리

Spring MVC는 이러한 반복을 애노테이션, 데이터 바인딩, 메시지 변환기, 예외 처리 기능으로 추상화한다.

---

## 핵심 정리

- 서블릿 컨테이너는 HTTP 메시지를 요청과 응답 객체로 변환한다.
- 쿼리와 Form 데이터는 `getParameter()`로 조회할 수 있다.
- JSON 본문은 입력 스트림을 읽어 별도로 역직렬화해야 한다.
- 응답 객체로 상태 코드, 헤더, 콘텐츠 타입과 본문을 설정한다.
- HTML을 문자열로 직접 만들면 화면 코드가 복잡해진다.
- Spring MVC는 서블릿 기반의 반복 작업을 더 높은 수준으로 추상화한다.
