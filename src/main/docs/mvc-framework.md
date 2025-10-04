## MVC 프레임워크 만들기

### 1. 프론트 컨트롤러

**FrontController 패턴 특징**

- 프론트 컨트롤러는 서블릿 하나로 클라이언트의 요청을 받음

- 프론트 컨트롤러가 요청에 맞는 컨트롤러를 찾아서 호출

- 입구를 하나로!

- 공통 처리 기능

- 프론트 컨트롤러를 제외한 나머지 컨트롤러는 서블릿을 사용하지 않아도 됨

**스프링 웹 MVC와 프론트 컨트롤러**

- 스프링 웹 MVC의 핵심도 바로 FrontController!

- 스프링 웹 MVC의 DispatcherServlet이 FrontController의 패턴으로 구현되어 있음.

### 2. 프론트 컨트롤러 도입 - v1

v1의 구조를 잠깐 생각해보자.

1. 클라이언트에서 HTTP 요청을 보낸다.

2. FrontController에서 URL 매핑 정보에서 컨트롤러를 조회한다.

3. 이에 맞는 컨트롤러를 호출한다.

4. 컨트롤러에서 JSP forward

5. JSP에서 HTML 응답.

이것을 코드로 구현해보자.

```java
package hello.servlet.web.frontcontroller.v1;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ControllerV1 {
  void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException; 
}
```

서블릿과 비슷한 모양의 컨트롤러 인터페이스를 도입한다. 

각 컨트롤러들은 이 인터페이스를 구현하면 컨트롤러는 이 인터페이스를 호출해서 구현과 관계없이 로직의 일관성을 가져갈 수 있다.

이제 이 인터페이스를 구현한 컨트롤러를 만들어보자. 

**MemberFormControllerV1 - 회원 등록 컨트롤러**

```java
package hello.servlet.web.frontcontroller.v1.controller;

import hello.servlet.web.frontcontroller.v1.ControllerV1;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class MemberFormControllerV1 implements ControllerV1 {
  @Override
  public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String viewPath = "/WEB-INF/views/new-form.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);
  }
}
```

이 코드를 한번 더 정리해보자. 

요청에 대한 정보는 request에 담겨져있고 응답을 담아야하는 response를 서블릿 컨테이너에서 줌. 

이제 가야할 경로를 문자열로 만들고 dispatcher가 문자열로 된 경로를 보고 어디로 가야할지 파악한후에, 

그 경로에 맞는 JSP 파일에 요청정보를 주고 응답 정보를 담아달라고 지시한다.

**MemberSaveControllerV1 - 회원 저장 컨트롤러**

```java
package hello.servlet.web.frontcontroller.v1.controller;

import hello.servlet.domain.member.Member;
import hello.servlet.domain.member.MemberRepository;
import hello.servlet.web.frontcontroller.v1.ControllerV1;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class MemberSaveControllerV1 implements ControllerV1 {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age"));

    Member member = new Member(username, age);
    memberRepository.save(member);

    //Model에 데이터를 보관함.
    request.setAttribute("member", member);

    String viewPath = "/WEB-INF/views/save-result.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);

  }
}
```

저번에 Servlet-Jsp-MVC 마크다운 파일을 보면 new-form.jsp 파일에서 form action="save"로 있었다.

이거는 경로중에 맨 마지막 세그먼트를 빼고 save로 url이 입력된다.

그리고 new-form.jsp 파일에서 username과 age를 입력 해야 파라미터를 읽을 수 있기 때문에 등록 컨트롤러로 둔 것. 

**MemberListControllerV1 - 회원 목록 컨트롤러**

```java
package hello.servlet.web.frontcontroller.v1.controller;

import hello.servlet.domain.member.Member;
import hello.servlet.domain.member.MemberRepository;
import hello.servlet.web.frontcontroller.v1.ControllerV1;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class MemberListControllerV1 implements ControllerV1 {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    List<Member> members = memberRepository.findAll();

    request.setAttribute("members", members);

    String viewPath = "/WEB-INF/views/members.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);


  }

}
```

내부 로직은 기존 서블릿과 거의 같다.

이제 프론트 컨트롤러를 만들어보자.

**FrontControllerServletV1 - 프론트 컨트롤러**

```java
package hello.servlet.web.frontcontroller.v1;

import hello.servlet.web.frontcontroller.v1.controller.MemberFormControllerV1;
import hello.servlet.web.frontcontroller.v1.controller.MemberListControllerV1;
import hello.servlet.web.frontcontroller.v1.controller.MemberSaveControllerV1;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// v1/* 어떠한 url이 들어와도 일단 이 서블릿이 무조건 호출이 된다.
@WebServlet(name = "frontControllerServletV1", urlPatterns = "/front-controller/v1/*")
public class FrontControllerServletV1 extends HttpServlet {

  private Map<String, ControllerV1> controllerMap = new HashMap<>();

  public FrontControllerServletV1() {
    controllerMap.put("/front-controller/v1/members/new-form", new MemberFormControllerV1());
    controllerMap.put("/front-controller/v1/members/save", new MemberSaveControllerV1());
    controllerMap.put("/front-controller/v1/members", new MemberListControllerV1());
  }

  @Override
  protected void service(HttpServletRequest request , HttpServletResponse response) throws ServletException, IOException {
     System.out.println("FrontControllerServletV1.service");

     String requestURI = request.getRequestURI();

     ControllerV1 controller = controllerMap.get(requestURI);

     if(controller == null) {
       response.setStatus(HttpServletResponse.SC_NOT_FOUND);
       return;
     }

     controller.process(request, response);
  }


}
```

코드의 로직을 한번 보자.

먼저 여태까지 짠 컨트롤러들을 담을 controllerMap을 만들었다.

그리고 이 Map에 문자열로 된 경로를 key로 저장, 그 경로에 맞는 컨트롤러들을 생성함과 동시에 참조값을 value에 저장.

앞에서 만들었던 서블릿(Servlet-Jsp-MVC 마크다운 파일 참고)과는 다르게 "/front-controller/v1/*"

저런 urlPattern이 있는데 저것은 저 경로에 맞는 모든 url 패턴을 웹 브라우저에서 입력해도(*에 어떠한 것이 들어갔던지간에)

위에서 만든 서블릿한테 우선권이 있다. 즉 url을 입력하면 위에 만든 서블릿부터 호출됨. 

이제 URI를 받고 맵에서 get메서드를 활용하여 URI를 매개변수에 넣고 value값으로 참조값을 받음. (다형성 활용)

받은 참조값을 기반으로 @Override 한 프로세스 메서드를 호출한다. 

이 코드의 전체적인 분석은 이런데 코드에서 아쉬운점은 view에서 담당하는 화면 렌더링 작업을 컨트롤러에서 하고 있다.

View를 따로 만들어서 코드를 짜보도록 하자. 

### 2. 프론트 컨트롤러 도입 - v2

먼저 V2 구조를 잠깐 파악해보자.

1. 클라이언트에서 HTTP 요청을 보낸다.

2. FrontController에서 URL 매핑 정보에서 컨트롤러를 조회한다.

3. FrontController에서 URL에 맞는 다른 컨트롤러를 호출한다.

4. 호출된 컨트롤러가 MyView를 반환함.

5. MyView를 받아서 render 메서드를 호출함.

6. JSP forward

7. HTML 응답

**MyView 코드**

```java
package hello.servlet.web.frontcontroller;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyView {

  private String viewPath;

  public MyView(String viewPath) {
    this.viewPath = viewPath;
  }

  public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);
  }

}
```

원래 기존 코드에서는 viewPath를 문자열로 초기화 한 다음에 getRequestDispatcher(viewPath); 이런식으로 작성했다.

여기선 생성자에서 viewPath를 초기화함.

일단 코드를 보면 view에 관한 코드를 아예 따로 분리했다는걸 볼 수 있다. 

이제 다음 버전에 컨트롤러를 만들어보겠다. 

```java
package hello.servlet.web.frontcontroller.v2;

import hello.servlet.web.frontcontroller.MyView;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ControllerV2 {
  MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
```

앞서 만들었던 ControllerV1 process 메서드와 다르게 MyView를 반환한다. 

**MemberFormControllerV2 - 회원 등록 폼**

```java
package hello.servlet.web.frontcontroller.v2.controller;

import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v2.ControllerV2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class MemberFormControllerV2 implements ControllerV2 {

  @Override
  public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    return new MyView("/WEB-INF/views/new-form.jsp");
  }
}
```

V1과는 다르게 view와 관련된 dispatcher.forward() 메서드를 호출 하지 않아도 된다. 좀 더 코드가 간결해졌다.

**MemberSaveControllerV2 - 회원 저장**

```java
package hello.servlet.web.frontcontroller.v2.controller;

import hello.servlet.domain.member.Member;
import hello.servlet.domain.member.MemberRepository;
import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v2.ControllerV2;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class MemberSaveControllerV2 implements ControllerV2 {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  // "/front-controller/v2/members/save" 바로 검색하면 안되는 이유 파라미터로 받는게 없어서 500뜸
  public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age"));

    Member member = new Member(username, age);
    memberRepository.save(member);

    //Model에 데이터를 보관
    request.setAttribute("member",member);

    return new MyView("/WEB-INF/views/save-result.jsp");
  }
}
```

localhost:8080/front-controller/v2/members/save를 url 입력하면 500 에러가 뜬다. (서버 오류)

이거는 new-form.jsp에서 데이터를 입력 받은걸 getParameter로 갖고 와야하는데 갖고 올 데이터가 없으니 

오류가 뜬다. 아무튼 코드에서 Model에 데이터를 보관하고 MyView에 경로를 매개변수 생성자로 리턴해서 MyView에서 받은 경로를 가지고 

view에 렌더링 하는 작업을 하게끔 해주는 코드이다. 

**MemberListControllerV2 - 회원 목록**

```java
package hello.servlet.web.frontcontroller.v2.controller;

import hello.servlet.domain.member.Member;
import hello.servlet.domain.member.MemberRepository;
import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v2.ControllerV2;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class MemberListControllerV2 implements ControllerV2 {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    List<Member> members = memberRepository.findAll();

    request.setAttribute("members",members);

    return new MyView("/WEB-INF/views/members.jsp");
  }

}
```

이 코드도 마찬가지로 원래 RequestDispatcher를 활용하여 view화면에 띄우는 작업을 했었는데 MyView로 인해 작업량이 줄어듬을 볼 수 있다. 

이제 프론트 컨트롤러를 작성해보자. 

```java
package hello.servlet.web.frontcontroller.v2;

import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v2.controller.MemberFormControllerV2;
import hello.servlet.web.frontcontroller.v2.controller.MemberListControllerV2;
import hello.servlet.web.frontcontroller.v2.controller.MemberSaveControllerV2;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "frontControllerServletV2" , urlPatterns = "/front-controller/v2/*")
public class FrontControllerServletV2 extends HttpServlet {

  private Map<String, ControllerV2> controllerMap = new HashMap<>();

  public FrontControllerServletV2() {
    controllerMap.put("/front-controller/v2/members/new-form", new MemberFormControllerV2());
    controllerMap.put("/front-controller/v2/members/save", new MemberSaveControllerV2());
    controllerMap.put("/front-controller/v2/members", new MemberListControllerV2());
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("FrontControllerServletV2.service");

    String requestURI = request.getRequestURI();

    ControllerV2 controller = controllerMap.get(requestURI);

    if(controller == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    MyView view = controller.process(request, response);
    view.render(request, response);


  }
}
```

이 코드에서는 URI를 받고 받은 URI로 get 메서드를 써서 value에 있는 참조값을 controller 참조변수에 넣는다.

이러한 참조변수를 가지고 @Override한 메서드를 호출하고 MyView에서 렌더링을 한다.












