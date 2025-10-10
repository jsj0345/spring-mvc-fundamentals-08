package hello.servlet.web.frontcontroller.v5;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v3.controller.MemberFormControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberListControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberSaveControllerV3;
import hello.servlet.web.frontcontroller.v4.controller.MemberFormControllerV4;
import hello.servlet.web.frontcontroller.v4.controller.MemberListControllerV4;
import hello.servlet.web.frontcontroller.v4.controller.MemberSaveControllerV4;
import hello.servlet.web.frontcontroller.v5.adapter.ControllerV3HandlerAdapter;
import hello.servlet.web.frontcontroller.v5.adapter.ControllerV4HandlerAdapter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "frontControllerServletV5" , urlPatterns = "/front-controller/v5/*")
public class FrontControllerServletV5 extends HttpServlet {

  private final Map<String, Object> handlerMappingMap = new HashMap<>(); // 다른 Controller를 받아야해서 Object로 둠.
  private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>(); // 컨트롤러가 호환 가능한지를 보기위한 어댑터를 둠.

  public FrontControllerServletV5() {
    initHandlerMappingMap();
    initHandlerAdapters();
  }

  private void initHandlerMappingMap() {
    handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3()); // 웹 브라우저(클라이언트)에서 입력한 url에 맞게 컨트롤러를 value값으로 둠.
    handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
    handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());

    //v4 추가
    handlerMappingMap.put("/front-controller/v5/v4/members/new-form", new MemberFormControllerV4());
    handlerMappingMap.put("/front-controller/v5/v4/members/save", new MemberSaveControllerV4());
    handlerMappingMap.put("/front-controller/v5/v4/members", new MemberListControllerV4());

  }

  private void initHandlerAdapters() {
    handlerAdapters.add(new ControllerV3HandlerAdapter()); // 어댑터를 리스트에 초기화.
    handlerAdapters.add(new ControllerV4HandlerAdapter()); // 어댑터를 리스트에 초기화.
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Object handler = getHandler(request); // 입력한 URI를 바탕으로 컨트롤러를 받아옴.

    if(handler == null) { // 받아온 컨트롤러가 null이면 -> 404 Error (즉, 클라이언트 측에 문제가 있는것)
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    MyHandlerAdapter adapter = getHandler(handler); // getHandler(handler) 메서드를 호출해서 호환 가능한 어댑터가 있는지 파악하고 있으면 그런 어댑터를 리턴 함.

    ModelView mv = adapter.handle(request, response, handler); // 반환받은 어댑터로 모델에 값을 담고 논리적인 뷰 이름 초기화 및 모델뷰 반환.

    String viewName = mv.getViewName(); // 논리적인 뷰 이름 갖고오기
    MyView view = viewResolver(viewName); // 논리적인 뷰 이름과 JSP파일 확장자 및 앞부분을 이어 붙여서 완전한 경로를 만든다.

    view.render(mv.getModel(), request, response); // 화면에 렌더링을 해야 하므로 렌더 메서드 호출. 이때 Model은 단순히 Map에 값이 담겨져 있는거기때문에 setAttribute 메서드를 호출해서 서블릿에서 실제 모델에 값을 담아야함.

  }

  private MyHandlerAdapter getHandler(Object handler) {
     for(MyHandlerAdapter adapter : handlerAdapters) { // 적합한 어댑터가 있는지 순회함.
       if(adapter.supports(handler)) { // 만약에 어댑터가 있다 true
         return adapter;
       }
     }

     throw new IllegalArgumentException("handler adapter를 찾을 수 없습니다. handler = " + handler); // 없으면 예외 터트리기
  }

  private Object getHandler(HttpServletRequest request) {
    String requestURI = request.getRequestURI();
    return handlerMappingMap.get(requestURI);
  }

  private MyView viewResolver(String viewName) {
    return new MyView("/WEB-INF/views/" + viewName + ".jsp");
  }

}

/*
package hello.servlet.web.frontcontroller.v5;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v3.controller.MemberFormControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberListControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberSaveControllerV3;
import hello.servlet.web.frontcontroller.v4.controller.MemberFormControllerV4;
import hello.servlet.web.frontcontroller.v4.controller.MemberListControllerV4;
import hello.servlet.web.frontcontroller.v4.controller.MemberSaveControllerV4;
import hello.servlet.web.frontcontroller.v5.adapter.ControllerV3HandlerAdapter;
import hello.servlet.web.frontcontroller.v5.adapter.ControllerV4HandlerAdapter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "frontControllerServletV5", urlPatterns = "/front-controller/v5/*")
public class FrontControllerServletV5 extends HttpServlet {

  private final Map<String, Object> handlerMappingMap = new HashMap<>();
  private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>();

  public FrontControllerServletV5() {
    initHandlerMappingMap();
    initHandlerAdapters();
  }

  private void initHandlerMappingMap() { // 컨트롤러 매핑
    handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
    handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
    handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());
  }

  private void initHandlerAdapters() { // 어댑터 초기화
    handlerAdapters.add(new ControllerV3HandlerAdapter());
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    Object handler = getHandler(request);

    if(handler == null) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    MyHandlerAdapter adapter = getHandlerAdapter(handler); // getHandlerAdapter 메서드를 호출해서 handler를 처리할수 있는지를 봐야함.
    ModelView mv = adapter.handle(request, response, handler);

    MyView view = viewResolver(mv.getViewName());
    view.render(mv.getModel(), request, response);
  }

  private Object getHandler(HttpServletRequest request) {
    String requestURI = request.getRequestURI(); // 입력한 URI 문자열로
    return handlerMappingMap.get(requestURI); // 문자열로된 URI를 바탕으로 value값 리턴
  }

  private MyHandlerAdapter getHandlerAdapter(Object handler) {
    for(MyHandlerAdapter adapter : handlerAdapters) {
      if(adapter.supports(handler)) {
        return adapter;
      }
    }
    throw new IllegalArgumentException("handler adapter를 찾을 수 없습니다. handler = " + handler);
  }

  private MyView viewResolver(String viewName) {
    return new MyView("/WEB-INF/views/" + viewName + ".jsp");
  }

}

*/
