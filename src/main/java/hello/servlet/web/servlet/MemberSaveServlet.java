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
    int age = Integer.parseInt(request.getParameter("age")); // 문자열을 int형으로

    Member member = new Member(username, age);
    memberRepository.save(member); // 멤버 등록

    response.setContentType("text/html"); // html파일 타입이 text/html
    response.setCharacterEncoding("utf-8"); // 클라이언트에서 인코딩할때 utf-8로
    PrintWriter w = response.getWriter(); // 메시지 바디 작성
    w.write("<html>\n" +
        "<head>\n" +
        " <meta charset=\"UTF-8\">\n" +
        "</head>\n" +
        "<body>\n" +
        "성공\n" +
        "<ul>\n" +
        " <li>id="+member.getId()+"</li>\n" + // 동적으로 데이터가 바뀜.
        " <li>username="+member.getUsername()+"</li>\n" +
        " <li>age="+member.getAge()+"</li>\n" +
        "</ul>\n" +
        "<a href=\"/index.html\">메인</a>\n" +
        "</body>\n" +
        "</html>");
  }
}

/*
package hello.servlet.web.servlet;

import hello.servlet.domain.member.Member;
import hello.servlet.domain.member.MemberRepository;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "memberSaveServlet", urlPatterns = "/servlet/members/save")
public class MemberSaveServlet extends HttpServlet {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("MemberSaveServlet.service");
    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age")); // 파라미터 반환값이 string이여서 int로 바꿔줘야함.

    Member member = new Member(username, age);
    System.out.println("member = " + member);

    memberRepository.save(member);

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



 */


