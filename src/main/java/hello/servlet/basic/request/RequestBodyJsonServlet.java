package hello.servlet.basic.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.servlet.basic.HelloData;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// 포스트맨 raw - 그냥 있는 그대로의 text를 보냄

/**
 * http:localhost:8080/request-body-json
 *
 * JSON 형식 전송
 * content-type: application/json
 * message body: {"username": "hello", "age": 20}
 *
 */

@WebServlet(name = "requestBodyJsonServlet", urlPatterns = "/request-body-json")
public class RequestBodyJsonServlet extends HttpServlet {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    ServletInputStream inputStream = request.getInputStream(); // 메시지 바디를 바이트 코드로 바꿈.
    String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8); // 인코딩 방식 작성

    System.out.println("messageBody = " + messageBody); // 여기서는 문자열 그대로 읽음. (raw)

    HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
    /*
    JSON 결과를 파싱해서 사용할 수 있는 자바 객체로 변환하려면 Jackson, Gson 같은
    JSON 라이브러리를 추가해서 사용해야 한다.

    스프링 부트로 spring MVC를 선택하면 기본적으로 Jackson 라이브러리(ObjectMapper)를 함께 제공한다.
     */
    System.out.println("helloData.username = " + helloData.getUsername());
    System.out.println("helloData.age = " + helloData.getAge());

    response.getWriter().write("ok");
  }
}

/*
@WebServlet(name = "requestBodyJsonServlet", urlPatterns = "/request-body-json")
public class RequestBodyJsonServlet extends HttpServlet {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     ServletInputStream inputStream = request.getInputStream(); // 메시지 바디를 바이트 코드로 해석
     String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8); // 인코딩 방식 적어야함.

     System.out.println("messageBody = " + messageBody);

     HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);

     System.out.println("helloData.username = " + helloData.getUsername());
     System.out.println("helloData.age = " + helloData.getAge());

     response.getWriter().write("OK");
  }

}




 */

