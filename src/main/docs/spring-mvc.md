# Spring MVC 구조 복습

> 직접 만든 프론트 컨트롤러 구조가 Spring MVC의 DispatcherServlet, HandlerMapping, HandlerAdapter, ViewResolver로 어떻게 연결되는지 정리했다.

## 1. DispatcherServlet

Spring MVC의 중심에는 `DispatcherServlet`이 있다.

`DispatcherServlet`은 `HttpServlet` 계층을 기반으로 동작하며 모든 MVC 요청을 먼저 받는 프론트 컨트롤러 역할을 한다.

스프링 부트는 일반적으로 이 서블릿을 기본 경로에 자동 등록한다.

```text
브라우저 요청
→ DispatcherServlet
→ 요청 처리 구성 요소 탐색
→ 컨트롤러 실행
→ 응답 또는 뷰 렌더링
```

개별 컨트롤러는 URL 매핑 탐색, 어댑터 선택, 뷰 해석 같은 공통 절차를 직접 구현하지 않는다.

---

## 2. 전체 요청 흐름

Spring MVC의 전통적인 뷰 렌더링 흐름은 다음과 같이 볼 수 있다.

```text
1. DispatcherServlet이 요청 수신
2. HandlerMapping에서 Handler 조회
3. HandlerAdapter 선택
4. HandlerAdapter가 Handler 실행
5. ModelAndView 반환
6. ViewResolver가 View 조회
7. View가 Model을 이용해 렌더링
8. HTTP 응답
```

이 구조는 직접 만든 MVC 프레임워크의 V5 흐름과 연결된다.

```text
직접 만든 구조          Spring MVC
------------------------------------------
FrontController       DispatcherServlet
handlerMappingMap     HandlerMapping
MyHandlerAdapter      HandlerAdapter
ModelView             ModelAndView
viewResolver()        ViewResolver
MyView                View
```

---

## 3. HandlerMapping

HandlerMapping은 요청에 맞는 실행 대상을 찾는다.

현재 주로 사용하는 애노테이션 컨트롤러는 다음 구현으로 탐색된다.

```text
RequestMappingHandlerMapping
```

예전 방식이나 다른 핸들러 타입에는 별도의 매핑 구현이 사용될 수 있다.

핵심은 DispatcherServlet이 컨트롤러 클래스를 직접 하드코딩하지 않고 HandlerMapping에 탐색을 위임한다는 점이다.

---

## 4. HandlerAdapter

찾은 핸들러를 실제로 호출할 방법을 결정한다.

```text
RequestMappingHandlerAdapter
```

애노테이션 기반 컨트롤러 메서드를 실행하고 메서드 파라미터를 준비하며 반환값을 해석한다.

HandlerMapping이 실행 대상을 찾는 역할이라면 HandlerAdapter는 그 대상을 어떤 규칙으로 호출할지를 담당한다.

```text
HandlerMapping → 누구를 실행할지
HandlerAdapter → 어떻게 실행할지
```

어댑터 구조 덕분에 서로 다른 형태의 핸들러도 DispatcherServlet의 공통 흐름에 참여할 수 있다.

---

## 5. 과거 Controller 인터페이스 방식

```java
@Component("/springmvc/old-controller")
public class OldController implements Controller {

    @Override
    public ModelAndView handleRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return new ModelAndView("new-form");
    }
}
```

이 방식에서는 빈 이름으로 URL을 찾는 HandlerMapping과 `Controller` 인터페이스를 실행하는 HandlerAdapter가 필요하다.

요즘 주로 쓰는 방식은 아니지만 Spring MVC가 단일 컨트롤러 형태에 묶이지 않고 매핑과 어댑터로 확장된다는 점을 이해하기 좋다.

---

## 6. 애노테이션 기반 컨트롤러 시작

```java
@Controller
public class MemberFormController {

    @RequestMapping(
        "/springmvc/members/new-form"
    )
    public ModelAndView newForm() {
        return new ModelAndView("new-form");
    }
}
```

### `@Controller`

- 스프링 빈 등록 대상이 된다.
- Spring MVC가 요청 처리 클래스로 인식한다.

### `@RequestMapping`

요청 경로와 컨트롤러 메서드를 연결한다.

애노테이션 기반 매핑은 `RequestMappingHandlerMapping`이 찾고 `RequestMappingHandlerAdapter`가 실행한다.

---

## 7. 여러 컨트롤러를 한 클래스로 통합

회원 등록 폼, 저장, 목록을 각각 별도 클래스에 둘 수도 있지만 공통 경로를 기준으로 하나의 컨트롤러에 모을 수 있다.

```java
@Controller
@RequestMapping("/springmvc/members")
public class MemberController {

    @RequestMapping("/new-form")
    public ModelAndView newForm() {
        return new ModelAndView("new-form");
    }

    @RequestMapping("/save")
    public ModelAndView save(
            HttpServletRequest request
    ) {
        // 저장 처리
        return new ModelAndView("save-result");
    }

    @RequestMapping
    public ModelAndView members() {
        return new ModelAndView("members");
    }
}
```

클래스 수준과 메서드 수준의 경로를 조합할 수 있다.

```text
클래스: /springmvc/members
메서드: /new-form
결과:   /springmvc/members/new-form
```

---

## 8. 더 실용적인 컨트롤러 형태

Spring MVC는 서블릿 요청 객체를 직접 사용하지 않아도 필요한 값을 메서드 파라미터에 넣어 준다.

```java
@PostMapping("/save")
public String save(
        @RequestParam String username,
        @RequestParam int age,
        Model model
) {
    Member member = new Member(username, age);
    repository.save(member);

    model.addAttribute("member", member);
    return "save-result";
}
```

### 개선된 점

- 요청 파라미터를 메서드 인자로 바로 받는다.
- 문자열을 숫자로 변환하는 작업을 프레임워크가 처리할 수 있다.
- `ModelAndView`를 직접 만들지 않아도 된다.
- 논리적 뷰 이름만 문자열로 반환한다.
- HTTP 메서드를 애노테이션으로 구분한다.

```java
@GetMapping("/new-form")
@PostMapping("/save")
@GetMapping
```

같은 URL이라도 GET과 POST를 다른 메서드에 매핑할 수 있다.

---

## 9. 반환값 해석

문자열 반환은 컨트롤러 종류와 애노테이션에 따라 의미가 달라진다.

```java
@Controller
public class PageController {

    @GetMapping("/page")
    public String page() {
        return "page";
    }
}
```

`@Controller`에서는 보통 논리적 뷰 이름으로 처리한다.

```java
@RestController
public class ApiController {

    @GetMapping("/message")
    public String message() {
        return "hello";
    }
}
```

`@RestController`에서는 문자열 자체가 응답 본문이 된다.

화면 컨트롤러와 API 컨트롤러의 반환 의미를 혼동하지 않아야 한다.

---

## 10. ViewResolver

컨트롤러가 `"members"`를 반환하면 ViewResolver가 실제 뷰 경로를 찾는다.

예를 들어 다음 설정을 사용한다고 생각할 수 있다.

```text
prefix: /WEB-INF/views/
suffix: .jsp
```

```text
논리 이름: members
실제 경로: /WEB-INF/views/members.jsp
```

컨트롤러는 파일 시스템의 구체적인 위치를 몰라도 된다.

---

## 11. Spring MVC가 추가로 처리하는 일

직접 만든 프레임워크보다 훨씬 많은 기능이 HandlerAdapter와 주변 구성 요소에 포함된다.

- 메서드 파라미터 해석
- 타입 변환
- 데이터 바인딩
- Bean Validation
- JSON 메시지 변환
- 세션과 쿠키
- 파일 업로드
- 예외 처리
- 비동기 요청
- 인터셉터
- 국제화

개발자는 요청 처리 메서드의 입력과 출력에 집중하고 공통 규칙은 프레임워크에 맡긴다.

---

## 12. 구조를 이해했을 때의 장점

Spring MVC의 내부 흐름을 알면 다음 문제를 추적하기 쉽다.

```text
컨트롤러를 찾지 못함
→ HandlerMapping 확인

컨트롤러 타입을 실행하지 못함
→ HandlerAdapter 확인

메서드 파라미터 변환 실패
→ ArgumentResolver 또는 변환 과정 확인

뷰를 찾지 못함
→ ViewResolver와 논리 이름 확인

JSON 응답이 이상함
→ HttpMessageConverter 확인
```

애노테이션을 외우는 것보다 요청이 어떤 구성 요소를 거치는지 이해하는 것이 디버깅에 도움이 된다.

---

## 핵심 정리

- DispatcherServlet은 Spring MVC의 프론트 컨트롤러다.
- HandlerMapping은 실행할 핸들러를 찾는다.
- HandlerAdapter는 핸들러 호출 방식을 통일한다.
- ModelAndView는 뷰 이름과 모델을 담는다.
- ViewResolver는 논리적 이름을 실제 View로 바꾼다.
- 애노테이션 컨트롤러는 RequestMapping 기반 매핑과 어댑터가 처리한다.
- 실용적인 방식에서는 요청 값을 메서드 인자로 받고 논리적 뷰 이름을 반환한다.
- Spring MVC는 직접 만든 MVC 프레임워크를 확장한 구조로 이해할 수 있다.
