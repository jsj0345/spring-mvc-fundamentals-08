package hello.servlet.basic.request;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "requestBodyStringServlet", urlPatterns = "/request-body-string")
public class RequestBodyStringServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    ServletInputStream inputStream = request.getInputStream(); // 메시지 바디 내용을 바이트 코드로 얻을 수 있음
    String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8); // 인코딩 정보가 어떤건지 알려줘야함.

    System.out.println("messageBody = " + messageBody);

    response.getWriter().write("ok");
    /*
    inputStream은 byte 코드를 반환한다. byte 코드를 우리가 읽을 수 있는 문자(String)로 보려면
    문자표(Charset)를 지정해주어야 한다. 여기서는 UTF_8 Charset을 지정해주었다.

     */
  }

}

/*
@WebServlet(name = "requestBodyStringServlet", urlPatterns = "/request-body-string")
public class RequestBodyStringServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    ServletInputStream inputStream = request.getInputStream(); // 메시지 바디를 바이트 코드로 바꿈.
    String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8); // 인코딩 정보를 알려줘야한다.

    System.out.println("messageBody = " + messageBody);

    response.getWriter().write("ok");
  }

}
 */