package hello.servlet.web.servletmvc;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "mvcMemberFormServlet" , urlPatterns ="/servlet-mvc/members/new-form")
public class MvcMemberFormServlet extends HttpServlet { // 컨트롤러

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String viewPath = "/WEB-INF/views/new-form.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);// 컨트롤러에서 뷰로 이동할 때 사용하는 것. View로 가는 경로를 지정함.
    dispatcher.forward(request, response); // 서블릿에서 jsp를 호출
    /*
    dispatcher.forward() -> 다른 서블릿이나 JSP로 이동할 수 있는 기능이다.

    서버 내부에서 다시 호출이 발생함.

    1. 클라이언트에서 서버로 호출을 함.

    2. 서버 안에서 자기들끼리 서블릿을 호출했다가 jsp를 호출하고

    이러한 jsp에서 응답을 만들어서 고객한테 보낸것.

    WEB-INF는 컨트롤러를 항상 거쳐서 불러주기를 원하는 것.

    외부에서 직접적으로 부르지 않길 바라는 것임(룰임) -> 외부에서 호출 X

    항상 컨트롤러를 거쳐서 서블릿을 거치고 내부에서 포워드 해야 호출
     */
  }
}

/*
package hello.servlet.web.servletmvc;

@WebServlet(name = "mvcMemberFormServlet", urlPatterns = "/servlet-mvc/members/new-form")
public class MvcMemberFormServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String viewPath = "/WEB-INF/views/new-form.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);
  }

  dispatcher.forward() -> 다른 서블릿이나 JSP로 이동할 수 있는 기능이다.

  서버 내부에서 다시 호출이 발생함.

  1. 클라이언트에서 서버로 호출을 함.

  2. 서버 안에서 자기들끼리 서블릿을 호출했다가 jsp를 호출하고

  이러한 jsp에서 응답을 만들어서 고객한테 보낸것.

  WEB-INF는 컨트롤러를 항상 거쳐서 불러주기를 원하는 것.

  외부에서 직접적으로 부르지 않길 바라는 것임(룰임) -> 외부에서 호출 X

  항상 컨트롤러를 거쳐서 서블릿을 거치고 내부에서 포워드 해야 호출

}

package hello.servlet.web.servletmvc;

@WebServlet(name = "mvcMemberFormServlet" , urlPatterns = "/servlet-mvc/members/new-form")
public class MvcMemberFormServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String viewPath = "/WEB-INF/views/new-form.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);
  }

}
 */
