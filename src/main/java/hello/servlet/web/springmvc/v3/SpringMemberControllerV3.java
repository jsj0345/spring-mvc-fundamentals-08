package hello.servlet.web.springmvc.v3;

import hello.servlet.domain.member.Member;
import hello.servlet.domain.member.MemberRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Controller
@RequestMapping("/springmvc/v3/members")
public class SpringMemberControllerV3 {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  //@RequestMapping("/new-form")
  @RequestMapping(value = "/new-form", method = RequestMethod.GET) // 경로가 중복된다. 해결해보자. RequestMapping 활용해보자.
  //여태까지 짠 코드를 보면 입력을 어떠한 url로 넣던지간에 get, post, put과 관계없이 모든 메서드를 다 활용할 수 있다.
  //이러면 문제가 있다.
  //@GetMapping("/new-form")
  public String newForm() {
    return "new-form";
  }

  @RequestMapping(value = "/save" , method = RequestMethod.POST)
  //@PostMapping("/save")
  public String save(@RequestParam("username") String username, @RequestParam("age") int age, Model model) {
    Member member = new Member(username, age);
    memberRepository.save(member);

    model.addAttribute("member",member);

    return "save-result";
  }

  @RequestMapping(method = RequestMethod.GET)
  //@GetMapping("")
  public String member(Model model) {
    List<Member> members = memberRepository.findAll();

    model.addAttribute("members",members);

    return "members";
  }
}
