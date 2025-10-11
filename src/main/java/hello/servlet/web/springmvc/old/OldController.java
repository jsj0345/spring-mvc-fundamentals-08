package hello.servlet.web.springmvc.old;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

@Component("/springmvc/old-controller") // 빈 이름 지정
public class OldController implements Controller { // OldController가 어떻게 호출 된 걸까?

  DispatcherServlet dispatcherServlet = new DispatcherServlet();

  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    System.out.println("OldController.handleRequest");
    return new ModelAndView("new-form"); // 여기서 따로 viewResolver에 완전한 경로를 만들면서 view를 리턴하는게 아니면 당연히 화이트라벨이 뜸.
  }
}
