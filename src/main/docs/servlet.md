## 서블릿(Servlet)

### 1. 서블릿 등록 및 사용 

먼저 스프링 부트 서블릿 환경을 구성하기 위해 SevletApplication 클래스에 어노테이션을 추가하자.

@ServletComponentScan // 지금 위치한 패키지 및 하단 패키지에 있는 서블릿들을 다 찾아서 서블릿으로 등록함. 
// 마치 스프링 핵심 원리에서 배웠던 @ComponentScan이랑 비슷한 느낌임. 
@SpringBootApplication
public class ServletApplication {
  
  public static void main(String[] args) {
    SpringApplication.run(ServletApplication.class, args);
  }
  
}

실제 동작하는 서블릿 코드를 작성해보자. 

@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {
  
  @Override // helloServlet이 호출 되면 service 메서드 실행
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     
    System.out.println("HelloServlet.service");
    System.out.println("request = " + request); // 객체 정보
    System.out.println("response = " + response); // 객체 정보 

    String username = request.getParameter("username"); // 요청을 보낼때 쿼리 파라미터에 있는 username에 관한 정보를 담기.
    System.out.println("username = " + username); 
    
    response.setContentType("text/plain"); // 응답 메시지를 보낼때 지정할 Content-Type (F12 눌러서 확인)
    response.setCharacterEncoding("utf-8"); // 한글도 보이게끔 설정. 
    response.getWriter().write("hello " + username); // HTTP Response Message에 있는 메시지 바디에 띄움. 

    // url에 ?username=kim 쿼리 파라미터를 써보자.
    // f12 눌러서 Content-Type 확인해보기(Response 메시지)
   }
}

코드의 전체적인 흐름은 다음과 같다.

1. 코드를 실행하고 클라이언트(웹 브라우저)에 url을 입력한다. (localhost:8080/hello)

2. urlPatterns = "/hello"를 보면 저 url에 맞게 입력을 하면 HelloServlet이 실행 된다. 

실행 전 상황을 잠깐 살펴보면 서블릿 컨테이너가 전/후처리 과정을 다 해줌. 이제 비즈니스 로직을 실행하는 곳이 서블릿 아래에 있는 메서드인데

request 객체는 클라이언트에서 HTTP Request Message에 대한 정보를 담고 있는 객체다. 

response 객체는 비즈니스 로직을 수행한 후, 결과를 담는 객체다.

3. 그럼 서블릿 컨테이너는 response 객체를 보고 HTTP Response Message를 만들고 클라이언트에 전송. 

### 2. HttpServletRequest 개요

1. HttpServletRequest 역할

- HTTP 요청 메시지를 개발자가 직접 파싱해서 사용해도 되지만, 매우 불편할 것이다. 서블릿은 개발자가 HTTP 요청 메시지를 편리하게 
사용할 수 있도록 개발자 대신에 HTTP 요청 메시지를 파싱한다. 그리고 그 결과를 HttpServletRequest 객체에 담아서 제공한다.

- HttpServletRequest를 사용하면 다음과 같은 HTTP 요청 메시지를 편리하게 조회할 수 있다.

HTTP 요청 메시지

POST /save HTTP/1.1 (start-line)
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded

username=kim&age=20

START LINE - HTTP 메소드, URL, 쿼리 스트링, 스키마, 프로토콜

헤더 - 헤더 조회

바디 - form 파라미터 형식 조회, message body 데이터 직접 조회

임시 저장소 기능
- 해당 HTTP 요청이 시작부터 끝날 때 까지 유지되는 임시 저장소 기능
- 저장: request.setAttribute(name, value)
- 조회: request.getAttribute(name)

해당 기능을 사용하려면 HelloServlet 클래스에 대해 올린 것을 참고하자. 

2. HttpServletRequest - 기본 사용법

@WebServlet(name = "requestHeaderServlet", urlPatterns = "/request-header")
public class RequestHeaderServlet extends HttpServlet {
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        printStartLine(request);
        printHeaders(request);
        printHeaderUtils(request);
        printEtc(request);
    }
    
    //start line 정보
    private void printStartLine(HttpServletRequest request) {
        System.out.println("--- REQUEST-LINE - start ---");
        System.out.println("request.getMethod() = " + request.getMethod()); //GET
        System.out.println("request.getProtocol() = " + request.getProtocol()); // HTTP/1.1
        
        System.out.println("request.getScheme() = " + request.getScheme()); //http
        // http://localhost:8080/request-header
        System.out.println("request.getRequestURL() = " + request.getRequestURL());
        // /request-header
        System.out.println("request.getRequestURI() = " + request.getRequestURI());
        //username=hi
        System.out.println("request.getQueryString() = " +
            request.getQueryString());
        System.out.println("request.isSecure() = " + request.isSecure()); //https 사용 유무
        System.out.println("--- REQUEST-LINE - end ---");
        System.out.println();
    }
    
    //Header 모든 정보
    private void printHeaders(HttpServletRequest request) {
    System.out.println("--- Headers - start ---");
    
        /*
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
          String headerName = headerNames.nextElement();
          System.out.println(headerName + ": " + headerName);
        }
         */
    
        // 요즘 방식
        request.getHeaderNames().asIterator().
        forEachRemaining(headerName -> System.out.println(headerName + ": " + headerName));
    
    
        System.out.println("--- Headers - end ---");
        System.out.println();
    }
    
    //Header 편리한 조회
    private void printHeaderUtils(HttpServletRequest request) {
        System.out.println("--- Header 편의 조회 start ---");
        System.out.println("[Host 편의 조회]");
        System.out.println("request.getServerName() = " +
        request.getServerName()); //Host 헤더
        System.out.println("request.getServerPort() = " +
        request.getServerPort()); //Host 헤더
        System.out.println();
        System.out.println("[Accept-Language 편의 조회]");
        request.getLocales().asIterator()
        .forEachRemaining(locale -> System.out.println("locale = " +
        locale)); // Request Header에 Accept-Language 정보 있음.
    
        System.out.println("request.getLocale() = " + request.getLocale()); // 우선순위가 가장 높은거 꺼내오기
        System.out.println();
        System.out.println("[cookie 편의 조회]");
        if (request.getCookies() != null) {
          for (Cookie cookie : request.getCookies()) {
            System.out.println(cookie.getName() + ": " + cookie.getValue());
          }
        }
        System.out.println();

        System.out.println("[Content 편의 조회]"); // 포스트맨 이용해보기
        System.out.println("request.getContentType() = " +
            request.getContentType());
        System.out.println("request.getContentLength() = " +
            request.getContentLength());
        System.out.println("request.getCharacterEncoding() = " +
            request.getCharacterEncoding());
        System.out.println("--- Header 편의 조회 end ---");

        System.out.println();
    }
    
    private void printEtc(HttpServletRequest request) {
        System.out.println("--- 기타 조회 start ---");
        System.out.println("[Remote 정보]"); // 내부에서 네트워크 커넥션이 맺어진 정보들을 보여줌.
        System.out.println("request.getRemoteHost() = " +
        request.getRemoteHost()); //
        System.out.println("request.getRemoteAddr() = " +
        request.getRemoteAddr()); //
        System.out.println("request.getRemotePort() = " +
        request.getRemotePort()); //
        System.out.println();
        System.out.println("[Local 정보]"); // 내 서버 정보를 보여줌.
        System.out.println("request.getLocalName() = " + request.getLocalName());
        System.out.println("request.getLocalAddr() = " + request.getLocalAddr());
        System.out.println("request.getLocalPort() = " + request.getLocalPort());
        System.out.println("--- 기타 조회 end ---");
        System.out.println();
    }
}

위 코드를 실행하면 http 요청 메시지에 대한 Header, start-line에 대해 볼 수 있다. 

### 3. HTTP 요청 데이터 - 개요

HTTP 요청 메시지를 통해 클라이언트에서 서버로 데이터를 전달하는 방법을 알아보자.

1. GET - 쿼리 파라미터

- /url?username=hello&age=20

- 메시지 바디 없이, URL의 쿼리 파라미터에 데이터를 포함해서 전달(단순 조회) 

- ex) 검색, 필터, 페이징등에서 많이 사용하는 방식

2. POST - HTML Form

- content-type: application/x-www-form-urlencoded(HTML form 기본 전송 형식, 단순 key=value 나열만 가능)

- 메시지 바디에 쿼리 파라미터 형식으로 전달 username=hello&age=20

- ex)회원 가입, 상품 주문, HTML Form 사용

3. HTTP message body에 데이터를 직접 담아서 요청

- HTTP API에서 주로 사용, JSON, XML, TEXT

- 데이터 형식은 주로 JSON 사용
  (POST, PUT, PATCH)

### 4. HTTP 요청 데이터 - GET 쿼리 파라미터

다음 데이터를 클라이언트에서 서버로 전송해보자.

username=hello
age=20

메시지 바디 없이, URL의 쿼리 파라미터를 사용해서 데이터를 전달하자.

ex) 검색,필터,페이징등에서 많이 사용하는 방식

코드를 보자.

@WebServlet(name = "requestParamServlet", urlPatterns = "/request-param")
public class RequestParamServlet extends HttpServlet {
  
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("[전체 파라미터 조회] - start");
    
    request.getParameterNames().asIterator().forEachRemaining(paramName -> System.out.println(paramName 
     + "=" + request.getParameter(paramName)); // url에 입력된 여러가지 parameter들을 꺼내옴, key = value 형태로 만듬. 
    
    System.out.println("[전체 파라미터 조회] - end");
    System.out.println();
    
    System.out.println("[단일 파라미터 조회]");
    String username = request.getParameter("username"); // username이라는 key를 이용해가지고 value(username)을 갖고옴. 
    System.out.println("request.getParameter(username) = " + username);

    String age = request.getParameter("age"); // age라는 key를 이용해가지고 value(age)를 갖고옴. 
    System.out.println("request.getParameter(age) = " + age);
    System.out.println();

    System.out.pritnln("[이름이 같은 복수 파라미터 조회]");
    System.out.println("request.getParameterValues(username)");
    String[] usernames = request.getParameterValues("username");
    for(String name : usernames) {
      System.out.println("username = " + name);
    }

    response.getWriter().write("ok");
 }
}

localhost:8080/request-param을 url에 입력하면 화면에 ok라는 메시지를 띄우고

파라미터에 대한 value값이 나오는걸 콘솔창에 확인할 수 있다.

그럼 여기서 username=lee&username=jeon&age=28

이런식으로 url에 입력하면 어떻게 될까?

제일 먼저 입력한 파라미터가 먼저 나온다. 

### 5. HTTP 요청 데이터 - POST HTML Form 

이번에는 html form을 활용해서 post 방식으로 데이터를 메시지 바디에 넣고 서버에 보내보자.

src/main/webapp/basic에서 html 파일 생성. 

<!DOCTYPE html>
<html>
<head>
   <meta charset="UTF-8">
   <title>Title</title>
</head>
<body>
<form action="/request-param" method="post">
   username: <input type="text" name="username" />
   age: <input type="text" name="age" />
   <button type="submit">전송</button>
</form>
</body>
</html>

이렇게 html 파일을 만들고 스프링 부트를 실행한 다음에 

localhost:8080/basic/hello-form.html을 입력해보자.

참고로, webapp까진 경로에 있어서 basic부터 url에 입력하면 된다. 

이제 username과 age에 각각 값을 입력하면 

콘솔창에 값들이 정상적으로 출력된다.

저 html파일을 보면 전송을 하는 순간 request-param 경로로 이동된다. 

그래서 request-param 경로를 거쳤을때의 서블릿을 호출하는것이다.

저 html 파일에서 값들을 입력했을때 요청 메시지에선 post 방식임을 알고 있어서

메시지 바디에 username=lee&age=28 이런식으로 띄운다. 

그리고 post방식이니까 content-type: application/x-www-form-urlencoded이다.

그런데 이런 간단한 작업을 하는데 html 파일까지 작성하면서 입력값을 보내는건 귀찮은 일이다.

postman을 활용해보자. 

postman을 활용하면 먼저 post방식이니까 Body에 x-www-form-urlencoded를 체크 해줘야 한다.

content-type: application/x-www-form-urlencoded이기 때문이다.

그리고 key와 value에 각각 값을 넣어보고 send를 해보자.

url옆에 Post로 체크. 

### 6. HTTP 요청 데이터 - API 메시지 바디 - 단순 텍스트

- HTTP message body에 데이터를 직접 담아서 요청

-> HTTP API에서 주로 사용, JSON, XML, TEXT

-> 데이터 형식은 주로 JSON 사용

-> POST,PUT,PATCH 

가장 단순한 텍스트 메시지를 HTTP 메시지 바디에 담아서 전송하고, 읽어보자. 

### 7. HTTP 요청 데이터 - API 메시지 바디 - JSON 

JSON 방식으로 파싱을 해야 하니까 객체를 하나 생성해보자.

package hello.servlet.basic;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class HelloData {
  private String username;
  private int age;
}

-> lombok이 있어서 @Getter, @Setter 어노테이션만 달아줘도 눈에는 안보이지만 생성됨.

@WebServlet(name = "requestBodyJsonServlet", urlPatterns = "/request-body-json")
public class RequestBodyJsonServlet extends HttpServlet {
  
  private ObjectMapper objectMapper = new ObjectMapper();
  
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    ServletInputStream inputStream = request.getInputStream();
    String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8); 

    System.out.println("messageBody = " + messageBody); 

    HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);
    
    System.out.println("helloData.username = " + helloData.getUsername());

    System.out.println("helloData.age = " + helloData.getAge());
   
    response.getWriter().write("ok");
  }

}

여기서 postman을 이용해서 post 방식으로 메시지 바디를 보낸다고 가정하자.

System.out.println("messageBody = " + messageBody); -> 여기서 메시지 바디는 맨 처음에 문자열이다.

아직 파싱을 하지 않았다. 파싱을 하는 문장은 HelloData helloData = objectMapper.readValue(messageBody, HelloData.class); 

이제 객체를 얻어냈으니 참조값을 통해 멤버변수에 접근하자. 

    



    















  


  
