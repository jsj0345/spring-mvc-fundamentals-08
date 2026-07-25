# MVC 프레임워크 직접 만들어보기

> 반복되는 서블릿 컨트롤러 코드를 프론트 컨트롤러, 뷰 추상화, 모델, 어댑터 구조로 발전시키는 과정을 정리했다.

## 1. 프론트 컨트롤러의 목적

기존 MVC에서는 요청 URL마다 서블릿을 만들고 각 서블릿에서 공통 작업을 반복했다.

```text
요청 A → Controller A
요청 B → Controller B
요청 C → Controller C
```

프론트 컨트롤러를 두면 모든 요청이 먼저 하나의 입구를 통과한다.

```text
요청
→ FrontController
→ URL에 맞는 Controller
→ View
```

중앙에서 처리하기 좋은 기능:

- URL 매핑
- 공통 인코딩
- 인증과 권한 확인
- 로깅
- 예외 처리
- 뷰 선택
- 컨트롤러 호출 방식 통일

Spring MVC의 `DispatcherServlet`도 이 패턴을 기반으로 동작한다.

---

## 2. V1: 요청 URL과 컨트롤러 매핑

컨트롤러가 공통 인터페이스를 구현하게 한다.

```java
public interface ControllerV1 {

    void process(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception;
}
```

프론트 컨트롤러는 URI를 키로 컨트롤러를 찾는다.

```java
private final Map<String, ControllerV1> mappings =
        new HashMap<>();

ControllerV1 controller =
        mappings.get(request.getRequestURI());

if (controller == null) {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    return;
}

controller.process(request, response);
```

### 얻은 점

- 요청 입구가 하나로 모인다.
- URL과 처리 객체의 관계를 중앙에서 관리한다.
- 컨트롤러를 공통 타입으로 호출한다.

### 남은 문제

각 컨트롤러가 여전히 JSP 경로와 포워드 코드를 직접 작성한다.

---

## 3. V2: 뷰 렌더링 분리

반복되는 포워드 코드를 `MyView`로 옮긴다.

```java
public class MyView {

    private final String viewPath;

    public MyView(String viewPath) {
        this.viewPath = viewPath;
    }

    public void render(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        request.getRequestDispatcher(viewPath)
               .forward(request, response);
    }
}
```

컨트롤러는 화면 객체만 반환한다.

```java
public interface ControllerV2 {

    MyView process(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception;
}
```

프론트 컨트롤러가 공통으로 렌더링한다.

```java
MyView view = controller.process(request, response);
view.render(request, response);
```

뷰 이동 코드가 컨트롤러에서 사라지고 한 곳으로 모인다.

---

## 4. V3: 서블릿 의존성 줄이기

컨트롤러가 `HttpServletRequest`와 `HttpServletResponse`를 직접 사용하지 않도록 바꾼다.

```java
public interface ControllerV3 {

    ModelView process(Map<String, String> parameters);
}
```

프론트 컨트롤러가 요청 파라미터를 일반 Map으로 변환한다.

```java
Map<String, String> parameters = new HashMap<>();

request.getParameterNames()
       .asIterator()
       .forEachRemaining(
           name -> parameters.put(
               name,
               request.getParameter(name)
           )
       );
```

컨트롤러는 웹 기술보다 업무 처리에 가까운 형태가 된다.

### ModelView

```java
public class ModelView {

    private final String viewName;
    private final Map<String, Object> model =
            new HashMap<>();

    public ModelView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, Object> getModel() {
        return model;
    }
}
```

컨트롤러는 논리적 뷰 이름과 모델 데이터를 반환한다.

```java
ModelView modelView = new ModelView("save-result");
modelView.getModel().put("member", member);
return modelView;
```

---

## 5. 뷰 리졸버

컨트롤러가 전체 JSP 경로를 반환하면 물리 경로가 곳곳에 반복된다.

```text
/WEB-INF/views/new-form.jsp
/WEB-INF/views/members.jsp
```

논리 이름만 반환하고 프론트 컨트롤러가 실제 경로로 변환한다.

```java
private MyView resolveView(String viewName) {
    return new MyView(
            "/WEB-INF/views/" + viewName + ".jsp"
    );
}
```

```text
논리 이름: members
물리 경로: /WEB-INF/views/members.jsp
```

뷰 파일 위치나 확장자가 바뀌어도 변환 규칙 한 곳만 수정하면 된다.

---

## 6. 모델 데이터를 요청에 옮기기

`ModelView`의 데이터는 JSP가 읽을 수 있도록 요청 속성에 복사한다.

```java
modelView.getModel().forEach(
        request::setAttribute
);
```

렌더링 흐름은 다음과 같다.

```text
Controller
→ ModelView 반환
→ ViewResolver가 MyView 생성
→ Model을 request attribute에 저장
→ JSP forward
```

---

## 7. V4: 컨트롤러 사용성 개선

V3는 구조가 분명하지만 모든 컨트롤러가 `ModelView`를 직접 만들어야 한다.

V4에서는 프레임워크가 모델 Map을 넘겨주고 컨트롤러는 뷰 이름만 반환한다.

```java
public interface ControllerV4 {

    String process(
            Map<String, String> parameters,
            Map<String, Object> model
    );
}
```

```java
public String process(
        Map<String, String> parameters,
        Map<String, Object> model
) {
    Member member = save(parameters);
    model.put("member", member);
    return "save-result";
}
```

프레임워크 내부에서 `ModelView`를 구성할 수 있어 컨트롤러 작성량이 줄어든다.

### 장점

- 논리적 뷰 이름만 반환한다.
- 모델 객체를 따로 생성하지 않는다.
- 개발자가 프레임워크 내부 표현을 덜 알아도 된다.

### 한계

V3와 V4는 메서드 모양이 달라 하나의 타입으로 직접 호출할 수 없다.

---

## 8. V5: 핸들러 어댑터

서로 다른 컨트롤러 타입을 하나의 프론트 컨트롤러에서 실행하려면 중간 변환 계층이 필요하다.

```java
public interface HandlerAdapter {

    boolean supports(Object handler);

    ModelView handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception;
}
```

### `supports()`

어댑터가 특정 핸들러 타입을 실행할 수 있는지 판단한다.

```java
@Override
public boolean supports(Object handler) {
    return handler instanceof ControllerV4;
}
```

### `handle()`

실제 컨트롤러를 호출하고 결과를 프레임워크 표준 형식인 `ModelView`로 맞춘다.

```java
@Override
public ModelView handle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
) {
    ControllerV4 controller = (ControllerV4) handler;

    Map<String, String> parameters =
            createParameterMap(request);
    Map<String, Object> model = new HashMap<>();

    String viewName =
            controller.process(parameters, model);

    ModelView modelView = new ModelView(viewName);
    modelView.getModel().putAll(model);
    return modelView;
}
```

프론트 컨트롤러는 핸들러의 실제 타입을 몰라도 된다.

```text
요청 URI
→ Handler 조회
→ 지원 가능한 Adapter 검색
→ Adapter가 Handler 호출
→ ModelView로 통일
→ ViewResolver
→ View 렌더링
```

---

## 9. 컨트롤러보다 넓은 핸들러라는 이름

어댑터가 있으면 프론트 컨트롤러는 반드시 특정 `Controller` 인터페이스만 받을 필요가 없다.

실행 가능한 대상과 그 대상을 처리할 어댑터가 있으면 된다.

그래서 프레임워크 내부에서는 더 넓은 의미의 `handler`라는 용어를 사용한다.

이 구조는 Spring MVC의 다음 구성과 연결된다.

```text
HandlerMapping
HandlerAdapter
ModelAndView
ViewResolver
View
DispatcherServlet
```

---

## 10. 직접 만든 프레임워크의 한계

학습용 구조는 핵심 원리를 보여 주지만 실제 프레임워크에는 더 많은 기능이 필요하다.

- HTTP 메서드별 매핑
- 파라미터 타입 변환
- 입력 검증
- JSON 요청과 응답
- 예외 처리
- 인터셉터
- 국제화
- 파일 업로드
- 비동기 처리
- 캐시와 보안
- 다양한 뷰 기술

직접 구현 과정의 목적은 Spring MVC를 대체하는 것이 아니라 내부 구성 요소가 왜 필요한지 이해하는 데 있다.

---

## 핵심 정리

- 프론트 컨트롤러는 모든 요청의 공통 입구다.
- V1은 URL 매핑과 공통 컨트롤러 호출을 도입한다.
- V2는 반복되는 뷰 렌더링을 별도 객체로 분리한다.
- V3는 서블릿 의존성을 줄이고 ModelView와 ViewResolver를 도입한다.
- V4는 컨트롤러 개발자가 모델과 뷰 이름을 더 간단히 다루게 한다.
- V5는 어댑터를 통해 서로 다른 컨트롤러 타입을 함께 처리한다.
- Spring MVC의 DispatcherServlet 구조도 같은 문제를 더 일반적으로 해결한다.
