package hello.servlet.basic.request;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;

/**
 * 1. 파라미터 전송 기능
 * http://localhost:8080/request-param?username=hello&age=20
 */

@WebServlet(name = "requestParamServlet" , urlPatterns = "/request-param")
public class RequestParamServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("[전체 파라미터 조회] - start");
    request.getParameterNames().asIterator().forEachRemaining
        (paramName -> System.out.println(paramName + " = " + request.getParameter(paramName))); // key값 = value값

    // parameter가 중복이면 첫번째 값을 반환함.
    System.out.println("[전체 파라미터 조회] - end");
    System.out.println();

    System.out.println("[단일 파라미터 조회]");

    String username = request.getParameter("username"); // value값 parameter가 중복이면 첫번째 값을 반환함.
    String age = request.getParameter("age"); // value값

    System.out.println("username = " + username); // key = value
    System.out.println("age = " + age); // key = value
    System.out.println();

    System.out.println("[이름이 같은 복수 파라미터 조회]");
    String[] usernames = request.getParameterValues("username");
    for (String name : usernames) {
      System.out.println("username = " + name);
    }

    response.getWriter().write("ok"); // 응답 메시지에 있는 메시지 바디에 데이터로 들어감.
  }

}

/*
package hello.servlet.basic.request;

1. 파라미터 전송 가능
2. http://localhost:8080/request-param?username=hello&age=20

3. 동일한 파라미터 전송 가능
4. http://localhost:8080/request-param?username=hello&username=kim&age=20

@WebServlet(name = "requestParamServlet", urlPatterns = "/request-param")
public class RequestParamServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOExcpetion {

    System.out.println("[전체 파라미터 조회] - start");

    request.getParameterNames().asIterator()
           .forEachRemaining(paramName -> System.out.println(paramName + " = " + request.getParameter(paramName));

    System.out.println("[전체 파라미터 조회] - end");
    System.out.println();

    System.out.println("[단일 파라미터 조회]");
    String username = request.getParameter("username");
    System.out.println("request.getParameter(username) = " + username);

    String age = request.getParameter("age");
    System.out.println("request.getParameter(age) = " + age);
    System.out.println();

    System.out.println("[이름이 같은 복수 파라미터 조회]");
    System.out.println("request.getParameterValues(username)");
    String[] usernames = request.getParameterValues("username");

    for(String name : usernames) {
      System.out.println("username = " + name);
    }


  }

  response.getWriter().write("ok");

 }
}

@WebServlet(name = "requestParamServlet", urlPatterns = "/request-param")
public class RequestParamServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) {

    System.out.println("[전체 파라미터 조회] - start");

    request.getParameterNames().asIterator().forEachRemaining(paramName -> System.out.println
    (paramName + "=" + request.getParameter(paramName)); // key = value 구조

    System.out.println("[전체 파라미터 조회] - end");
    System.out.println();

    System.out.println("[단일 파라미터 조회]");
    String username = request.getParameter("username");
    System.out.println("request.getParameter(username) = " + username);

    String age = request.getParameter("age");
    System.out.println("request.getParameter(age) = " + age);
    System.out.println();

    System.out.println("[이름이 같은 복수 파라미터 조회]");
    System.out.println("request.getParameterValues(username)");
    String[] usernames = request.getParameterValues("username");
    for(String name : usernames) {
      System.out.println("username = " + name);
    }

    response.getWriter().write("ok");

  }

}

content-type은 HTTP 메시지 바디의 데이터 형식을 지정한다.

GET URL 쿼리 파라미터 형식으로 클라이언트에서 서버로 데이터를 전달할 때는 HTTP 메시지 바디를 사용하지
않기 때문에 content-type이 없다.

POST HTML Form 형식으로 데이터를 전달하면 HTTP 메시지 바디에 해당 데이터를 보내기 때문에
바디에 포함된 데이터가 어떤 형식인지 content-type을 꼭 지정해야 한다.
이렇게 폼으로 데이터를 전송하는 형식을 application/x-www-form-urlencoded 라 한다.









 */
