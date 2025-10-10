package hello.servlet.web.frontcontroller.v5.adapter;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.v3.ControllerV3;
import hello.servlet.web.frontcontroller.v5.MyHandlerAdapter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ControllerV3HandlerAdapter implements MyHandlerAdapter {

  @Override
  public boolean supports(Object handler) {
    return (handler instanceof ControllerV3); // 컨트롤러가 ControllerV3로 형변환 하는지를 본다.

    /*
    instanceof 메서드는 형 변환이 가능한지를 보는데..
    이때 형변환을 할때 담는 참조 변수를 봐야 할 것이 아니라 참조 변수 안에 있는 실제 객체를 바탕으로 형 변환이 가능한지를 본다.
     */
  }

  @Override
  public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException {
    ControllerV3 controller = (ControllerV3) handler; // 핸들러에 담겨있는 실제 객체를 바탕으로 캐스팅을 함. 왜냐하면 handler는 Object형이라 process 메서드를 직접적으로 이용 못함. (메서드 오버라이딩도 불가능함)

    Map<String, String> paramMap = createParamMap(request);

    ModelView mv = controller.process(paramMap);

    return mv;
  }

  private Map<String, String> createParamMap(HttpServletRequest request) {
    Map<String, String> paramMap = new HashMap<>();
    request.getParameterNames().asIterator()
        .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));

    return paramMap;
  }

}

/*
package hello.servlet.web.frontcontroller.v5.adapter;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.v3.ControllerV3;
import hello.servlet.web.frontcontroller.v5.MyHandlerAdapter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ControllerV3HandlerAdapter implements MyHandlerAdapter {

  @Override
  public boolean supports(Object handler) {
    return (handler instanceof ControllerV3);
  }

  @Override
  public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    ControllerV3 controller = (ControllerV3) handler;

    Map<String, String> paramMap = createParamMap(request);

    ModelView mv = controller.process(paramMap);

    return mv;
  }

  private Map<String, String> createParamMap(HttpServletRequest request) {
    Map<String, String> paramMap = new HashMap<>();

    request.getParameterNames().asIterator()
           .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName));

    return parmaMap;
  }

}
 */
