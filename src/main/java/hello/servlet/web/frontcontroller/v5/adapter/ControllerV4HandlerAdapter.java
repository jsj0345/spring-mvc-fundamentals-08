package hello.servlet.web.frontcontroller.v5.adapter;

import hello.servlet.domain.member.Member;
import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.v4.ControllerV4;
import hello.servlet.web.frontcontroller.v5.MyHandlerAdapter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

public class ControllerV4HandlerAdapter implements MyHandlerAdapter {

  @Override
  public boolean supports(Object handler) {
    return (handler instanceof ControllerV4);
  }

  @Override
  public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    ControllerV4 controller = (ControllerV4) handler; // 캐스팅을 하는 이유는 handler는 런타임 시점에 결정되기 때문임.

    Map<String, String> paramMap = createParamMap(request);
    Map<String, Object> model = new HashMap<>();

    String viewName = controller.process(paramMap, model);

    ModelView mv = new ModelView(viewName);
    mv.setModel(model); // 원래 ModelView에는 model이라는 변수가 있는데 V3에서는 ModelView를 생성해서 getModel()을 호출해서 ModelView에 있는 model에 직접적으로 초기화 했음.
    //그런데 V4는 그러한 과정이 없으므로 직접 setModel()을 호출해서 model을 넣어줘야함.

    return mv;
  }

  private static Map<String, String> createParamMap(HttpServletRequest request) {
    Map<String, String> paramMap = new HashMap<>();
    request.getParameterNames().asIterator().
        forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
    return paramMap;
  }


}
