package hello.servlet.web.springmvc.v1;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
// 스프링 MVC에서 애노테이션 기반 컨트롤러로 인식한다.
// 이 얘기는 핸들러 정보로 인식한다. 라고 생각하면 편함.

//@Component
//@RequestMapping  // 클래스 레벨에 있어야 RequestMappingHandlerMapping, RequestMappingAdapterHandler 찾아냄.
public class SpringMemberFormControllerV1 {

  @RequestMapping("/springmvc/v1/members/new-form")
  public ModelAndView process() {
    return new ModelAndView("new-form");
  }

}
