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

### 2. 회원 관리 웹 애플리케이션을 JSP로 만들어보기 

**회원 등록 폼 JSP**

```html
<%@ page contentType="text/html;charset=UTF=8" language="java">
<html>
<head>
    <title>Title</title>
</head>
<body>

<form action="/jsp/members/save.jsp" method="post">
    username: <input type="text" name="username" />
    age:      <input type="text" name="age" />
    <button type="submit">전송</button>
</form>

</body>
</html>
```

<%@ page contentType="text/html;charset=UTF=8" language="java">

-> JSP문서라는 뜻이다. JSP 문서는 이렇게 시작해야 한다. 

JSP는 서버 내부에서 서블릿으로 변환되는데 우리가 만들었던 MemberFormServlet과 거의 비슷한 모습으로 변환된다.

여기서도 JSP 파일을 보면 이름과 나이를 입력하고 전송 버튼을 누르면 

다른 jsp 파일로 가는 것을 알 수 있다. 

**회원 저장 JSP**

```html
<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%@ page import="hello.servlet.domain.member.Member %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%

   // request, response 사용 가능
   
   MemberRepository memberRepository = MemberRepository.getInstance();

   System.out.println("save.jsp");
   String username = request.getParameter("username");
   int age = Integer.parseInt(request.getParameter("age"));

   Member member = new Member(username, age);
   System.out.println("member = " + member);
   memberRepository.save(member);
%>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
성공
<ul>
    <li>id=<%=member.getId()%></li>
    <li>username=<%=member.getUsername()%></li>
    <li>age=<%=member.getAge()%></li>
</ul>
<a href="/index.html">메인</a>
</body>
</html>

```

<%@ page import="hello.servlet.domain.Member.MemberRepository" %>

-> 자바의 import 문과 같다.

<% ~~ %>

-> 이 부분에는 자바 코드를 입력할 수 있다. 

<%= ~~ %>

-> 이 부분에는 자바 코드를 출력할 수 있다. 

서블릿이랑은 다르게 파일 안에다가 코드를 넣을 수 있다.

**회원 목록 JSP**

```html
<%@ page import="java.util.List" %>
<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%@ page import="hello.servlet.domain.member.Member" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
   MemberRepository memberRepository = MemberRepository.getInstance();
   List<Member> members = memberRepository.findAll();
%>

<html>
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<a href="index.html">메인</a>
<table>
    <thead>
    <th>id</th>
    <th>username</th>
    <th>age</th>
    </thead>
    </thead>
    <tbody>
    <%
       for(Member member : members) {
          out.write("     <tr>");
          out.write("       <td>" + member.getId() + "</td>");
          out.write("       <td>" + member.getUsername() + "</td>");
          out.write("       <td>" + member.getAge() + "</td>");
        out.write("       </tr>");
    %>
    </tbody>
</table>
</body>
</html>
```

앞서 작성한 두개의 jsp 파일에서 username과 age를 가지고 객체에 데이터를 저장한다.

위에 있는 파일은 정보를 담은 객체들에 있는 Getter 메서드를 활용해서 정보값을 받는다. 

_______________________________________________________________

서블릿과 JSP의 한계

서블릿으로 개발할 때는 뷰(View)화면을 위한 HTML을 만드는 작업이 자바 코드에 섞여서 지저분하고 복잡하다.

JSP를 사용한 덕분에 뷰를 생성하는 HTML 작업을 깔끔하게 가져가고, 중간중간 동적으로 변경이 필요한 부분에만 자바 코드를 적용했다.

그런데 이렇게 해도 해결되지 않는 몇가지 고민이 남는다.

회원 저장 JSP를 보자. 코드의 상위 절반은 회원을 저장하기 위한 비즈니스 로직이고,

나머지 하위 절반만 결과를 HTML로 보여주기 위한 뷰 영역이다.

회원 목록의 경우에도 마찬가지다.

코드를 잘 보면, JAVA 코드, 데이터를 조회하는 리포지토리 등등 다양한 코드가 모두 JSP에 노출되어 있다. 

JSP가 너무 많은 역할을 한다. 

### 3. MVC 패턴 - 개요

하나의 서블릿이나 JSP만으로 비즈니스 로직과 뷰 렌더링까지 모두 처리하게 되면, 너무 많은 역할을 하게되고, 결과적으로 유지보수가 어려워진다.

비즈니스 로직을 호출하는 부분에 변경이 발생해도 해당 코드를 손대야 하고, 할 일이 있어도 비즈니스 로직이 함께 있는

해당 파일을 수정해야 한다. HTML 코드 하나 수정해야 하는데 수백줄 자바 코드가 함께 있다고 상상하면 끔찍하다.

**Model, View, Controller**

MVC 패턴은 지금까지 학습한 것 처럼 하나의 서블릿이나. JSP로 처리하던 것을 컨트롤러와 뷰라는 영역으로 서로 역할을 나눈 것을 말한다. 

웹 애플리케이션은 보통 이 MVC 패턴을 사용한다.

- 컨트롤러: HTTP 요청을 받아서 파라미터를 검증하고, 비즈니스 로직을 실행한다. 그리고 뷰에 전달할 결과 데이터를 조회해서 모델에 담는다.

- 모델: 뷰에 출력할 데이터를 담아둔다. 뷰가 필요한 데이터를 모두 모델에 담아서 전달해주는 덕분에 뷰는 비즈니스 로직이나 데이터 접근을 몰라도 되고,

화면에 렌더링 하는 일에 집중할 수 있다. 

- 뷰 : 모델에 담겨있는 데이터를 사용해서 화면에 그리는 일에 집중한다. 여기서는 HTML을 생성하는 부분을 말한다. 


### 4. MVC 패턴 - 적용

**회원 등록 폼 - 컨트롤러**

```java
package hello.servlet.web.servletmvc;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(name = "mvcMemberFormServlet", urlPatterns = "/servlet-mvc/members/new-form")
public class MvcMemberFormServlet extends HttpServlet {
   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     String viewPath = "/WEB-INF/views/new-form.jsp";
     RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
     dispatcher.forward(request, response);
   }
}
```

Dispatcher라는 용어는 서블릿에서 요청 흐름을 다른 자원(컨트롤러, 뷰 등)으로 전달하는 역할을 하는 객체를 말함.

구체적으로는 RequestDispatcher라는 인터페이스를 사용해서 요청을 다른 서블릿이나 JSP(View)로 넘길 수 있음.

요청 전달(forward) -> 클라이언트가 보낸 요청을 다른 서블릿이나 JSP로 넘겨서 그쪽에서 응답을 완성하게 함.

이때 클라이언트는 내부적으로 이동한 사실을 모름(URL 변화 없음). -> 리다이렉트랑 다른 개념 

1. 컨트롤러(Servlet)에서 최종적으로 보여줄 화면(JSP)의 위치를 문자열로 담음.

2. RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);

-> request 객체한테 viewPath 경로로 요청을 넘길 준비를 하는 것.  뷰로 이동하는 경로를 넘겨주는 것. 

3. dispatcher.forward(request, response);

-> 실제로 요청과 응답을 해당 JSP로 넘겨서, JSP가 응답을 만들도록 함. 

/WEB-INF

-> 이 경로안에 JSP가 있으면 외부에서 직접 JSP를 호출할 수 없다.

우리가 기대하는 것은 항상 컨트롤러를 통해서 JSP를 호출하는 것이다.

redirect vs forward

리다이렉트는 실제 클라이언트(웹 브라우저)에 응답이 나갔다가, 클라이언트가 redirect 경로로 다시 요청한다.

따라서 클라이언트가 인지할 수 있고, URL 경로도 실제로 변경된다. 반면에 포워드는 서버 내부에서 일어나는 호출이기 때문에

클라이언트가 전혀 인지하지 못한다. 

**회원 저장 - 뷰**

**회원 저장 - 컨트롤러**
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
 <meta charset="UTF-8">
 <title>Title</title>
</head>
<body>
<!-- 상대경로 사용, [현재 URL이 속한 계층 경로 + /save] -->
<form action="save" method="post">
 username: <input type="text" name="username" />
 age: <input type="text" name="age" />
 <button type="submit">전송</button>
</form>
</body>
</html>
```

위 jsp 파일을 보면 form action="save"을 보자. 만약에 /save였으면 localhost:8080/save로 절대경로로 지정된다.

그런데 파일에서는 "save"이므로 localhost:8080/servlet-mvc/members/new-form에서 

new-form을 지우고 save로 바꾼 것이 경로다. 

따라서, localhost:8080/servlet-mvc/members/save로 요청하게 된다. 

```java
package hello.servlet.web.servletmvc;

@WebServlet(name = "mvcMemberSaveServlet", urlPatterns = "/servlet-mvc/members/save")
public class MvcMemberSaveServlet extends HttpServlet {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age"));

    Member member = new Member(username, age);
    memberRepository.save(member);

    //Model에 데이터를 보관한다.
    request.setAttribute("member", member);
    //"member"를 key 값으로 두고 value값을 member 객체로 둔다. 

    String viewPath = "/WEB-INF/views/save-result.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response);
  }

}
```

그러면 아까 전에 작성한 jsp 파일에서 전송 버튼을 누르면 localhost:8080/servlet-mvc/members/new-form에서

new-form을 지우고 save로 바꾼 것이 경로다. 이래서 mvcMemberSaveServlet이 호출 되고 쓰레드가 실행한다.

입력한 이름과 나이를 Member 객체에 있는 멤버변수에 초기화를 하고 member객체를 MemberRepository에 보관한다.

setAttribute는 이렇게 생각하면 된다.

클라이언트에서 url을 입력해서 서버로 요청을 해야한다. 요청을 할 때, url에 맞는 서블릿을 서블릿 컨테이너에서 찾고 

그 서블릿을 찾은 다음에 호출하고 쓰레드가 실행한다.

이제 실행하는 과정중에 view로 이동하기 전에 데이터를 Model에 담는다. 

그럼 이러한 데이터를 view에선 Model에 있는 데이터를 보고 화면에 띄우는 역할을 한다. 

```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
성공
<ul>
    <li>id=${member.id}</li>  
    <li>username=${member.username}</li>
    <li>age=${member.age}</li>
</ul>
<a href="/index.html">메인</a>
</body>
</html> 
```

저기서 member.id, member.username, member.age를 꺼내려면 getAttribute 메서드를 쓰고 그 메서드의 리턴값은

Object형이기 때문에 다운캐스팅도 해야하고 좀 머리가 많이 아프다.

JSP에서는 ${}를 써서 캐스팅없이 편하게 속성값을 출력하게 해줌. 

이번에는 회원 목록 조회 - 컨트롤러를 만들어보자.

**회원 목록 조회 - 컨트롤러 코드**

```java
package hello.servlet.web.servletmvc;

import hello.servlet.domain.member.MemberRepository;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "mvcMemberListServlet", urlPatterns = "/servlet-mvc/members")
public class MvcMemberListServlet extends HttpServlet {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("MvcMemberListServlet.service");
    List<Member> members = memberRepository.findAll();

    request.setAttribute("members", members);

    String viewPath = "/WEB-INF/views/members.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
    dispatcher.forward(request, response); 

  }
}
```

**회원 목록 조회 - 뷰**
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<a href="/index.html">메인</a>
<table>
    <thead>
    <th>id</th>
    <th>username</th>
    <th>age</th>
    </thead>
    <tbody>
    <c:forEach var="item" items="${members}">
        <tr>
            <td>${item.id}</td>
            <td>${item.username}</td>
            <td>${item.age}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
```

앞서 회원 등록과 저장을 하고나서 이러한 객체를 List에 담고 List에 저장된 객체를 가지고 회원의 속성값을 알아내야한다. 

원래는 getAttribute 메서드를 쓰면 Object형이므로 다운 캐스팅을 해줘야 하지만 JSP는 $를 써서 캐스팅 불편함 없게 해준다. 

지금까지 MVC 패턴을 적용해봤는데 마지막으로 정리를 해보자. 

1. 먼저 mvcMemberFormServlet을 호출 하기 위해서 localhost:8080/servlet-mvc/members/new-form을 url에 입력한다.

2. 이제 url을 입력했으므로 요청 메시지가 서버로 전달된다. 서버에서는 컨트롤러에서 뷰로 가야하는데 

가기 전에 viewPath를 지정하고 dispatcher라는 객체를 이용하여 뷰로 간다.

그리고 이 객체를 forward라는 메서드를 써서 해당 jsp 파일에 request, response를 넘긴다.

3. 넘기고 나서 이제 클라이언트는 응답을 받아서 화면에 렌더링을 한다. 

렌더링 화면은 username과 age를 입력하는 화면.

이제 이 두개의 값을 입력하자.

4. 그런데 new-form.jsp 파일을 보면 form action="save"가 있다. 이것은 상대경로를 사용한 것이다.

상대경로를 사용하면 현재 경로에서 마지막 세그먼트를 빼고 /save로 대체한다. 

5. 그러면 클라이언트에서는 서버로 url을 localhost:8080/servlet-mvc/members/save로 입력하고

이제 이 url에 맞는 서블릿이 실행된다.

mvcMemberSaveServlet에서는 jsp 파일에서 입력받은 이름과 나이값을 받고 이 두 변수를 바탕으로

Member객체(엔티티)를 생성함. 

그리고 이 객체를 가지고 저장소에 member 객체를 저장함. (HashMap<>()을 이용)

6. 저장을 한 후에, model에 member 객체를 담는다. (value값으로)

7. dispatcher가 viewPath를 받은 후 경로에 맞게 view로 감.

8. jsp파일에서 request와 response 객체를 받아서 html 파일 내용 처리 

### 5. MVC 패턴 - 한계

MVC 패턴을 적용한 덕분에 컨트롤러의 역할과 뷰를 렌더링 하는 역할을 명확하게 구분할 수 있다. 

특히 뷰는 화면을 그리는 역할에 충실한 덕분에, 코드가 깔끔하고 직관적이다. 단순하게 모델에서 필요한 데이터를 꺼내고,

화면을 만들면 된다.

그런데 컨트롤러는 딱 봐도 중복이 많고, 필요하지 않는 코드들도 많이 보인다. 

**MVC 컨트롤러의 단점**

포워드 중복

View로 이동하는 코드가 항상 중복 호출되어야 한다. 물론 이 부분을 메서드로 공통화해도 되지만, 해당 메서드도 항상 직접 호출해야 한다.

```java
import jakarta.servlet.RequestDispatcher;

RequestDispatcher dispatcher = request.getRequestDisPatcher(viwePath);

dispatcher.forward(request, response);
```

ViewPath에 중복

```java
String viewPath = "/WEB-INF/views/new-form.jsp"; 
```

공통 처리가 어렵다. 

기능이 복잡해질수록 컨트롤러에서 공통으로 처리해야 하는 부분이 점점 더 많이 증가할 것이다.

단순히 공통 기능을 메서드로 뽑으면 될 것 같지만, 결과적으로 해당 메서드를 항상 호출해야 되고, 실수로 호출하지 않으면 문제가 될 것이다.

정리하면 공통 처리가 어렵다는 문제가 있다.

이 문제를 해결하려면 컨트롤러 호출 전에 먼저 공통 기능을 처리해야 한다. 

소위 수문장 역할을 하는 기능이 필요하다.

프론트 컨트롤러(Front Controller) 패턴을 도입하면 이런 문제를 깔끔하게 해결할 수 있다.

스프링 MVC의 핵심도 바로 이 프론트 컨트롤러에 있다.

















