package hello.servlet.basic;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// ctrl + / -> 주석
// ctrl + alt + L -> 자동 들여쓰기

/*
파싱(Parsing)의 정확한 의미

- 문자열 형태로 된 텍스트를 구조적으로 해석해서, 의미 있는 단위로 나누는 과정

- 쉽게 말해, 문장을 단어별로 쪼개고, 문법 규칙에 맞게 구조화하는것.

예를 들면, 택배 송장에 "수취인: 홍길동, 주소: 서울시 강남구, 연락처: 010-..."
이렇게 있으면 수취인=홍길동, 주소=서울시 강남구.. 이렇게 나누는게 파싱
 */


@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

  @Override // helloServlet이 호출 되면 service 메서드 실행.
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    System.out.println("HelloServlet.service"); // url 입력을 해보고 콘솔창을 보자.
    //빈 화면이 나올 것이다. 응답을 한 게 없으니까!
    System.out.println("request = " + request);
    System.out.println("response = " + response);
    /*
    request = org.apache.catalina.connector.RequestFacade@5b99ad8e
    response = org.apache.catalina.connector.ResponseFacade@727a61e8

    Tomcat, 제티, 언더토 등등 여러가지 와스 서버가 많음.

    WAS 서버들이 이 서블릿 표준 스펙을 구현함.
    구현체가 위처럼 org~로 찍히는 것.

    org.apache.catalina.connector <- 톰켓쪽 라이브러리

    http://localhost:8080/hello?username=kim

    ?username=kim -> 쿼리 파라미터

    쿼리 파라미터를 서블릿은 편하게 읽는 방법이 있음.
     */

    String username = request.getParameter("username");
    System.out.println("username = " + username); // 콘솔창에 username = kim이 나옴.

    request.setAttribute("username",username);

    System.out.println("request.getAttribute : " + request.getAttribute("username"));
    // request.getAttribute("username");
    // 만약에 username으로 놓으면 어떻게될까?
    // 위에서 String username을 썼으므로 value에 대한 값을 리턴하는데
    // 메서드의 의도는 key값에 대한 value값을 리턴 하는 것이므로 null이 출력.


    response.setContentType("text/plain");
    response.setCharacterEncoding("utf-8");
    response.getWriter().write("hello " + username);  // HTTP 응답 메시지 바디에 데이터가 들어감.

    // hello kim이 보인다. ?username=kim 쿼리 파라미터 썼음.
    // f12 눌러서 Content-Type 확인해보기

    // HTTP 요청 메시지를 보고 싶으면 ? -> resource에서 application.properties 확인
  }
}

/*
package hello.servlet.basic;

@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     System.out.println("HelloServlet.servlet"); // url 입력을 해보고 콘솔창을 보자. 콘솔창엔 정상 출력.
     //아무것도 안 보일 것이다. 왜냐면 응답 받은 것이 없기 때문 응답을 받았으면 렌더링하고 웹에 띄움.
     System.out.println("request = " + request);
     System.out.println("response = " + response);

    request = org.apache.catalina.connector.RequestFacade@5b99ad8e
    response = org.apache.catalina.connector.ResponseFacade@727a61e8

    Tomcat, 제티, 언더토 등등 여러가지 was가 많음.

    1. 서블릿 표준 스펙(Servlet Specification)

    - 자바 진영에서 정의한 웹 애플리케이션을 만들기 위한 표준 인터페이스

    - 대표적으로 HttpServlet, ServletRequest, ServletResponse 같은 인터페이스/추상클래스가 있음.

    - 이 스펙 문서에는 service() 메서드는 이런 역할을 한다, 개발자가 doget()을 오버라이딩하면 GET 요청을 처리한다.

    이런 규칙들이 있음.

    구현체가 Tomcat, Jetty, Undertow, GlassFish 등이 서블릿 스펙을 구현한 구현체임

    url에 ?username=이름(쿼리 파라미터) 을 입력하면 이번에도 마찬가지로 아무것도 안나옴.

    서블릿은 쿼리 파라미터를 편하게 읽는 방법이 있음.

    String username = request.getParameter("username");
    System.out.println("username = " + username); -> 콘솔창에 이름 나옴.

    response.setContentType("text/plain");
    response.getWriter().write("hello " + username); // HTTP 응답 메시지 바디에 들어감.

   }

}

package hello.servlet.basic;

@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
      System.out.println("HelloServlet.service");
      System.out.println("request = " + request);
      System.out.println("response = " + response);

     request = org.apache.catalina.connector.RequestFacade@5b99ad8e
     response = org.apache.catalina.connector.ResponseFacade@727a61e8

     Tomcat, 제티, 언더토 등등 여러가지 WAS가 많음.

     1. 서블릿 표준 스펙(Servlet Specification)

     - 자바 진영에서 정의한 웹 애플리케이션을 만들기 위한 표준 인터페이스

     - 대표적으로 HttpServlet, ServletRequest, ServletResponse 같은 인터페이스/추상클래스가 있음.

     - 이 스펙 문서에는 service 메서드는 이런 역할을 한다, 개발자가 doget()을 오버라이딩 하면 GET 요청을 처리한다.

     이런 규칙들이 있음.

     구현체가 Tomcat, Jetty, undertow, GlassFish 등이 서블릿 스펙을 구현한 구현체임

     url에 ?username=이름(쿼리 파라미터) 을 입력하면 이번에도 마찬가지로 아무것도 안나옴.

     서블릿은 쿼리 파라미터를 편하게 읽는 방법이 있음.

     Tomcat, 제티, 언더토 등등 여러가지 was가 많음.

    String username = request.getParameter("username");
    System.out.println("username = " + username); -> 콘솔창에 이름 나옴.

    String username = request.getParamter("username");
    System.out.println("username = " + username);

    response.setContentType("text/plain");
    response.setCharacterEncoding("utf-8");
    response.getWriter().write("hello " + username); // HTTP 응답 메시지 바디에 들어감.
 }
}

@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    System.out.println("HelloServlet.service");
    System.out.println("request = " + request);
    System.out.println("response = " + response);

    String username = request.getParameter("username");
    System.out.println("username = " + username);

    request.setAttribute("username", username);

    System.out.println("request.getAttribute : " + request.getAttribute("username"));

    만약에 username으로 놓으면 어떻게될까?
    위에서 String username을 썼으므로 value에 대한 값을 리턴하는데
    메서드의 의도는 key값에 대한 value값을 리턴 하는 것이므로 null이 출력


    response.setContentType("text/plain");
    response.setCharacterEncoding("utf-8");
    response.getWriter().write("hello " + username); // HTTP 응답 메시지 바디에 데이터가 들어감.


  }

}


request = org.apache.catalina.connector.RequestFacade@5b99ad8e
response = org.apache.catalina.connector.ResponseFacade@727a61e8

Tomcat, 제티, 언더토 등등 여러가지 와스 서버가 많음.

WAS 서버들이 이 서블릿 표준 스펙을 구현함.

구현체가 위처럼 org~로 찍히는 것.

org.apache.catalina.connector <- 톰켓쪽 라이브러리.

http://localhost.8080/hello?username=kim

?username=kim -> 쿼리 파라미터

쿼리 파라미터를 서블릿은 편하게 읽는 방법이 있음.

파싱(Parsing)의 정확한 의미

- 문자열 형태로 된 텍스트를 구조적으로 해석해서, 의미 있는 단위로 나누는 과정

- 쉽게 말해, 문장을 단어별로 쪼개고, 문법 규칙에 맞게 구조화하는것.

예를 들면, 택배 송장에 "수취인: 홍길동, 주소: 서울시 강남구, 연락처: 010-..."
이렇게 있으면 수취인=홍길동, 주소=서울시 강남구.. 이렇게 나누는게 파싱

파싱(Parsing)의 정확한 의미

- 문자열 형태로 된 텍스트를 구조적으로 해석해서, 의미 있는 단위로 나누는 과정

- 쉽게 말해, 문장을 단어별로 쪼개고, 문법 규칙에 맞게 구조화하는것.

예를 들면, 택배 송장에 "수취인: 홍길동, 주소: 서울시 강남구, 연락처: 010-..."
이렇게 있으면 수취인=홍길동, 주소=서울시 강남구.. 이렇게 나누는게 파싱











 */
