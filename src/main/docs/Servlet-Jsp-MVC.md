## 서블릿, JSP, MVC패턴

### 1. 회원 관리 웹 애플리케이션을 서블릿으로 만들어보기

회원정보는 이름: username, 나이: age로 지정하고

기능 요구사항으로는 회원 저장, 회원 목록 조회가 있다.

```java
//회원 도메인 모델

package hello.servlet.domain.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {
  
  private Long id;
  private String username;
  private int age;
  
  public Member() {
    
  }
  
  public Member(String username, int age) {
    this.username = username;
    this.age = age; 
  }
}
```

```java
//회원 저장소
package hello.servlet.domain.member;

/**
 * 동시성 문제가 고려되어 있지 않음, 실무에서는 ConcurrentHashMap, AtomicLong 고려 사용
 */

public class MemberRepository {

  private static Map<Long, Member> store = new HashMap<>(); // static 사용
  private static long sequence = 0L; // static 사용

  private static final MemberRepository instance = new MemberRepository();

  public static MemberRepository getInstance() {
    return instance;
  }

  private MemberRepository() {

  }

  public Member save(Member member) {
    member.setId(++sequence); // Id값 +1L
    store.put(member.getId(), member); // Map에 key와 value 등록 
    return member;
  }

  public Member findById(Long id) {
    return store.get(id); // key값을 가지고 value값 리턴 
  }

  public List<Member> findAll() {
    return new ArrayList<>(store.values()); // value값 모아서 저장
  }

  public void clearStore() {
    store.clear();
  }

}  

```

회원 저장소는 싱글톤 패턴을 적용했다. 스프링을 사용하면 스프링 빈으로 등록하면 되지만

지금은 최대한 스프링 없이 순수 서블릿만으로 구현하는 것이 목적이다.

싱글톤 패턴은 객체를 단 하나만 생성해서 공유해야 하므로 private 접근자로 막아둔다. 

이 코드가 정상적으로 돌아가는지 확인을 해보기 위해 테스트 코드를 작성해보자. 

```java
package hello.servlet.domain.member;

import static org.assertj.core.api.Assertions.*;
//원래 static이 안붙으면 클래스명.메서드이름.. 이런식으로 작성해야함.
//static이 붙으면 클래스명조차 필요 없음.
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class MemberRepositoryTest {

  MemberRepository memberRepository = MemberRepository.getInstance();

  @AfterEach // 테스트 끝날 때 마다 초기화, 테스트는 순서가 정해져 있지 않아서 클리어를 꼭 시켜줘야한다.
  void afterEach() {
    memberRepository.clearStore();
  }

  @Test
  void save() {
    //given
    Member member = new Member("hello", 20);

    //when
    Member savedMember = memberRepository.save(member); // 멤버 등록 

    //then
    Member findMember = memberRepository.findById(savedMember.getId()); // id(key)를 가지고 value(member객체) 반환
    assertThat(findMember).isEqualTo(savedMember); // 같은 객체를 가지고 동일한지 판별
  }

  @Test
  void findAll() {
    //given
    Member member1 = new Member("member1", 20);
    Member member2 = new Member("member2", 30);

    memberRepository.save(member1);
    memberRepository.save(member2);

    //when
    List<Member> result = memberRepository.findAll();

    //then
    assertThat(result.size()).isEqualTo(2);
    assertThat(result).contains(member1, member2);
  }

}
```

이제 본격적으로 서블릿으로 회원 관리 웹 애플리케이션을 만들어보자.

가장 먼저 서블릿으로 회원 등록 HTML 폼을 제공해보자.

```java
package hello.servlet.web.servlet;

import hello.servlet.domain.member.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "memberFormServlet", urlPatterns = "/servlet/members/new-form")
// 브라우저에서 url을 localhost:8080/servlet/members/new-form 입력해보자. 
public class MemberFormServlet extends HttpServlet {

  private MemberRepository memberRepository = MemberRepository.getInstance(); // 싱글톤 패턴을 이용해서 유일한 객체 

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html"); // 클라이언트에서 서버로 응답 메시지 보낼때 html 파일 형식임을 지정. 
    response.setCharacterEncoding("utf-8"); // 클라이언트에서 메시지 바디를 해석할때 utf-8로 하라는것 

    PrintWriter w = response.getWriter(); // 메시지 바디 작성 
    w.write("<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "   <meta charset=\"UTF-8\">\n" +
            "   <title>Title</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<form action=\"/servlet/members/save\" method=\"post\">\n" + 
            "   username: <input type=\"text\" name=\"username\" />\n" +
            "        age: <input type=\"text\" name=\"age\" />\n" +
            "        <button type=\"submit\">전송</button>\n" +
            "</form>\n" +
            "</body>\n" +
            "</html>\n");

    
    //실행하고 이름과 나이를 입력하면 whitelabel 페이지가 뜸.
    // /servlet/members/save 경로가 없어서그럼.
    //페이지 소스 보기에서 payload에 있는 form-data 보기
    //이름,나이를 입력하고나서 전송 버튼으로 누르면 localhost:8080/servlet/members/save 경로로 url에 입력됨. 
  }
}

```
이제 ServletApplication에서 Run 버튼을 누르고 url에 localhost:8080/servlet/members/new-form을 입력 하면 

username 입력과 age 입력, 전송 버튼이 보인다. (이것은 서버에서 클라이언트로 응답 메시지에 메시지 바디로 html 파일을 보낸 것이다.)

이제 전송 버튼을 누르면 localhost:8080/servlet/members/save 경로가 url에 입력된다. 그럼 이 경로에 맞게 이동하는데 서버에는 저 경로에 맞는 서블릿이 아직 없다.

만들어보자. 

```java
package hello.servlet.web.servlet;

import hello.servlet.domain.member.Member;
import hello.servlet.domain.member.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "memberSaveServlet", urlPatterns = "/servlet/members/save")
public class MemberSaveServlet extends HttpServlet {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("MemberSaveServlet.service"); 
    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age")); // getParameter는 String으로 반환하기때문에 int로 바꿈. 

    Member member = new Member(username, age);
    memberRepository.save(member); // 멤버 등록 

    response.setContentType("text/html");
    response.setCharacterEncoding("utf-8");
    PrintWriter w = response.getWriter();
    w.write("<html>\n" +
        "<head>\n" +
        " <meta charset=\"UTF-8\">\n" +
        "</head>\n" +
        "<body>\n" +
        "성공\n" +
        "<ul>\n" +
        " <li>id="+member.getId()+"</li>\n" +
        " <li>username="+member.getUsername()+"</li>\n" +
        " <li>age="+member.getAge()+"</li>\n" +
        "</ul>\n" +
        "<a href=\"/index.html\">메인</a>\n" +
        "</body>\n" +
        "</html>");
  }
}
```
파라미터를 조회하고나서 Member 객체를 만든다. 

이러한 객체를 MemberRepository를 통해서 저장한다.

Member 객체를 사용해서 결과 화면용 HTML을 동적으로 만들어서 응답함. 

이번에는 모든 회원 목록을 조회하는 기능을 만들어보자. 

```java
package hello.servlet.web.servlet;

import hello.servlet.domain.member.Member;
import hello.servlet.domain.member.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


@WebServlet(name = "memberListServlet", urlPatterns = "/servlet/members")
public class MemberListServlet extends HttpServlet {

  MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    List<Member> members = memberRepository.findAll(); // 모든 멤버를 갖고옴.

    response.setContentType("text/html");
    response.setCharacterEncoding("utf-8");

    PrintWriter w = response.getWriter();

    w.write("<html>\n");
    w.write("<head>\n");
    w.write(" <meta charset=\"UTF-8\">\n");
    w.write(" <title>Title</title>\n");
    w.write("</head>\n");
    w.write("<body>\n");
    w.write("<a href=\"/index.html\">메인</a>\n");
    w.write("<table>\n");
    w.write(" <thead>\n");
    w.write(" <th>id</th>\n");
    w.write(" <th>username</th>\n");
    w.write(" <th>age</th>\n");
    w.write(" </thead>\n");
    w.write(" <tbody>\n");

    for (Member member : members) { // members에는 여러개의 member 객체들이 있다. 그 객체들의 속성값들을 꺼내보는 로직 
      w.write(" <tr>");
      w.write(" <td>" + member.getId() + "</td>"); // 동적으로 데이터가 할당된다.
      w.write(" <td>" + member.getUsername() + "</td>");
      w.write(" <td>" + member.getAge() + "</td>");
      w.write(" </tr>");
    }

    w.write(" </tbody>\n");
    w.write("</table>\n");
    w.write("</body>\n");
    w.write("</html>\n");

  }
}
```

먼저 짠 코드들의 전반적인 흐름을 보자. 

1. localhost:8080/servlet/members/new-form url을 입력하면 url에 맞는 서블릿이 호출되고 

이름과 나이를 입력하고 전송 버튼을 누르면 클라이언트에서 localhost:8080/servlet/members/save url을 입력한다.

2. 전송 버튼을 눌렀을 때, 입력한 데이터를 바탕으로 MemberRepository에 store라는 맵에 id와 객체를 저장함.

이 객체로 객체 안에 있는 속성 값들을 웹 브라우저가 화면에 렌더링 해서 보여줌.

3. 1,2과정을 반복하면 store라는 맵에 id 및 객체가 계속 저장된다. for문을 이용해 이 객체들의 속성 값들을 html 파일에

동적으로 할당한다. 

지금까지 서블릿과 자바 코드만으로 HTML을 만들어봤다. 

서블릿 덕분에 동적으로 원하는 HTML을 마음껏 만들 수 있다. 정적인 HTML 문서라면 화면이 계속 달라지는 회원의 저장 결과라던가,

회원 목록 같은 동적인 HTML을 만드는 일은 불가능 할 것이다.

그런데, 코드에서 보듯이 이것은 매우 복잡하고 비효율 적이다.

자바 코드로 HTML을 만들어 내는 것 보다 차라리 HTML 문서에 동적으로 변경해야 하는 부분만 자바 코드를 넣을 수 있다면

더 편리할 것이다. 

이것이 바로 템플릿 엔진이 나온 이유이다. 템플릿 엔진을 사용하면 HTML 문서에서 필요한 곳만 코드를 적용해서 동적으로

변경할 수 있다. 템플릿 엔진에는 JSP, Thymeleaf, Freemarker, Velocity 등이 있다. 








