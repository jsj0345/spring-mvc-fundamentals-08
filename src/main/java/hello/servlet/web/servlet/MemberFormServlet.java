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
public class MemberFormServlet extends HttpServlet {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("utf-8");

    PrintWriter w = response.getWriter();
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
  }
}

/*
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
public class MemberFormServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    response.setCharacterEncoding("utf-8");

    PrintWriter w = response.getWriter();
    w.write("<!DOCTYPE html>\n" +
           "<html>\n" +
           "<head>\n" +
           " <meta charset=\"UTF-8\">\n" +
           " <title>Title</title>\n" +
           "</head>\n" +
           "<body>\n" +
           "<form action=\"/servlet/members/save\" method=\"post\">\n" +
           " username: <input type=\"text\" name=\"username\" />\n" +
           " age: <input type=\"text\" name=\"age\" />\n" +
           " <button type=\"submit\">전송</button>\n" +
           "</form>\n" +
           "</body>\n" +
           "</html>\n");
  }

}
 */