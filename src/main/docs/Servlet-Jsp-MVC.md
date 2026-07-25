# 서블릿, JSP, MVC 패턴 복습

> 회원 등록과 목록 조회 기능을 서블릿부터 JSP, MVC 구조까지 단계적으로 바꾸면서 각 방식의 책임과 한계를 정리했다.

## 1. 예제에서 사용할 회원 기능

학습 예제는 단순한 회원 관리 기능으로 구성한다.

```text
회원 정보
- id
- username
- age

기능
- 회원 저장
- 회원 목록 조회
```

저장소는 메모리 기반으로 만들 수 있다.

```java
public class MemberRepository {

    private final Map<Long, Member> store = new HashMap<>();
    private long sequence;

    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }

    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }
}
```

이 구현은 학습용으로는 충분하지만 여러 요청 스레드가 동시에 접근하는 실제 서버에서는 안전하지 않다.

- `HashMap`은 동시 수정에 안전하지 않다.
- `sequence++`도 원자적이지 않다.
- 애플리케이션을 재시작하면 데이터가 사라진다.

실제 환경에서는 데이터베이스나 동시성 자료구조, 원자적 ID 생성 전략이 필요하다.

---

## 2. 서블릿으로 HTML 만들기

서블릿에서 요청 파라미터를 읽고 회원을 저장한 뒤 HTML 문자열을 직접 출력할 수 있다.

```java
@WebServlet("/servlet/members/save")
public class MemberSaveServlet extends HttpServlet {

    @Override
    protected void service(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        repository.save(member);

        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        PrintWriter writer = response.getWriter();
        writer.println("<h1>회원 저장 완료</h1>");
        writer.println("<p>" + member.getUsername() + "</p>");
    }
}
```

### 장점

- HTTP 요청과 응답 흐름을 직접 확인하기 쉽다.
- 별도의 화면 기술 없이 동작을 빠르게 실험할 수 있다.

### 한계

Java 코드 안에 HTML 문자열이 섞이면서 읽기 어렵고 수정도 불편하다.

```text
요청 처리
+ 데이터 저장
+ 화면 생성
```

한 클래스가 여러 책임을 동시에 갖게 된다.

화면 구조가 커질수록 문자열 연결과 태그 관리가 복잡해지고, 디자이너나 프론트엔드 작업과 협업하기도 어렵다.

---

## 3. JSP로 화면 만들기

JSP는 HTML 문서 안에 서버 측 데이터를 출력할 수 있게 한다.

```jsp
<ul>
    <li>id=${member.id}</li>
    <li>name=${member.username}</li>
    <li>age=${member.age}</li>
</ul>
```

서블릿이 HTML을 직접 조립하는 것보다 화면을 읽고 수정하기 쉽다.

초기의 JSP에서는 스크립틀릿을 사용해 Java 코드를 넣을 수도 있다.

```jsp
<%
    String username = request.getParameter("username");
%>
```

하지만 JSP에서 요청 처리와 저장소 접근까지 맡기면 다시 여러 책임이 한 파일에 섞인다.

```text
JSP
├─ 요청 파라미터 처리
├─ 비즈니스 로직 호출
├─ 데이터 조회
└─ HTML 렌더링
```

JSP는 화면 표현에 집중시키고 Java 업무 로직은 별도 객체로 분리하는 편이 유지보수에 유리하다.

---

## 4. MVC 패턴

MVC는 웹 요청 처리와 화면 생성을 역할별로 나눈다.

### Controller

- HTTP 요청을 받는다.
- 파라미터를 검증하고 변환한다.
- 서비스나 저장소를 호출한다.
- 뷰에 필요한 데이터를 모델에 담는다.
- 사용할 뷰를 선택한다.

### Model

뷰가 출력할 데이터를 전달한다.

서블릿 환경에서는 요청 속성을 모델처럼 사용할 수 있다.

```java
request.setAttribute("member", member);
```

### View

모델 데이터를 읽어 HTML을 만든다.

```jsp
<p>${member.username}</p>
```

뷰는 회원 저장 방식이나 데이터베이스 구조를 몰라도 된다.

---

## 5. 컨트롤러에서 JSP로 전달하기

컨트롤러는 처리 결과를 모델에 담고 JSP로 포워드한다.

```java
request.setAttribute("member", member);

RequestDispatcher dispatcher =
        request.getRequestDispatcher(
                "/WEB-INF/views/save-result.jsp"
        );

dispatcher.forward(request, response);
```

`/WEB-INF` 아래에 둔 JSP는 브라우저가 직접 접근하기 어렵다. 사용자는 컨트롤러를 거쳐 화면에 접근하게 된다.

이 구조는 화면이 필요한 데이터 준비 과정을 우회하지 못하게 한다.

---

## 6. 포워드와 리다이렉트

### 포워드

```text
브라우저 → 컨트롤러
컨트롤러 → 서버 내부에서 JSP 호출
JSP → 브라우저에 응답
```

- 서버 내부 이동이다.
- 브라우저 주소가 바뀌지 않는다.
- 같은 요청과 응답 객체를 이어서 사용한다.
- 모델 데이터를 요청 속성으로 전달하기 편하다.

### 리다이렉트

```text
브라우저 → 서버
서버 → 다른 주소로 다시 요청하라고 응답
브라우저 → 새 URL로 요청
```

- 브라우저가 새 요청을 보낸다.
- 주소창의 URL이 변경된다.
- 이전 요청 객체는 그대로 이어지지 않는다.
- POST 이후 새로고침 중복을 줄이는 PRG 흐름에 사용할 수 있다.

둘은 화면 이동처럼 보일 수 있지만 요청 횟수와 데이터 전달 방식이 다르다.

---

## 7. MVC를 직접 적용한 뒤 남는 중복

역할은 나뉘었지만 각 컨트롤러에 다음 코드가 반복된다.

```java
String viewPath = "/WEB-INF/views/members.jsp";
RequestDispatcher dispatcher =
        request.getRequestDispatcher(viewPath);
dispatcher.forward(request, response);
```

반복되는 부분:

- 뷰 전체 경로 조립
- `RequestDispatcher` 조회
- `forward()` 호출
- 요청 파라미터 처리
- 공통 인코딩과 로깅
- 예외 처리

컨트롤러마다 공통 메서드를 호출하도록 만들 수도 있지만 개발자가 호출을 빠뜨릴 수 있다.

요청이 각 컨트롤러에 바로 들어가기 전에 하나의 공통 진입점이 먼저 처리하면 이 문제를 줄일 수 있다.

이 역할이 프론트 컨트롤러다.

---

## 8. 단계별 발전 흐름

```text
서블릿
→ Java 코드에서 요청 처리와 HTML 생성

JSP
→ HTML 작성은 쉬워졌지만 업무 코드가 섞일 수 있음

MVC
→ Controller, Model, View로 역할 분리

Front Controller
→ 공통 처리와 컨트롤러 호출을 중앙화
```

각 단계는 이전 기술을 완전히 버리는 것이 아니라 책임을 더 분명하게 분리하는 과정이다.

Spring MVC도 이 구조를 더 일반화하고 편리하게 만든 프레임워크로 이해할 수 있다.

---

## 핵심 정리

- 서블릿에서 HTML을 직접 만들면 Java 코드와 화면 코드가 섞인다.
- JSP는 화면 작성에는 편하지만 업무 로직까지 넣으면 유지보수가 어려워진다.
- MVC는 요청 처리, 전달 데이터, 화면 생성을 분리한다.
- 컨트롤러는 모델을 준비하고 뷰는 렌더링에 집중한다.
- 포워드는 서버 내부 이동이고 리다이렉트는 브라우저의 새 요청이다.
- 직접 만든 MVC 컨트롤러에는 뷰 이동과 공통 처리 중복이 남는다.
- 이런 중복을 중앙에서 처리하기 위해 프론트 컨트롤러가 필요하다.
