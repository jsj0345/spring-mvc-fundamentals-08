package hello.servlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan // spring이 자동으로 내 패키지 포함 하위패키지 다 뒤져서 Sevlet을 찾아 가지고 자동으로 등록해서 실행 할 수 있게 도와줌
@SpringBootApplication
public class ServletApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServletApplication.class, args);
	}

}

/*
@ServletComponentScan // spring이 자동으로 내 패키지 포함 하위패키지 다 뒤져서 Servlet을 찾아 가지고 자동으로 등록해서 실행 할 수 있게 도와줌.
@SpringBootApplication
public class ServletApplication {

  public static void main(String[] args) {
    SpringApplication.run(ServletApplication.class, args);
  }

}

@ServletComponentScan // spring이 자동으로 내 패키지 포함 하위패키지 다 뒤져서 servlet을 찾아 가지고 자동으로 등록해서 실행 할 수 있게 도와줌.
@SpringBootApplication
public class ServletApplication {

  public static void main(String[] args) {
    SpringApplication.run(ServletApplication.class, args):
  }

}

@ServletComponentScan // spring이 자동으로 내 패키지 포함 하위패키지 다 뒤져서 servlet을 찾아 가지고 자동으로 등록해서 실행 할 수 있게 도와준다.
@SpringBootApplication
public class ServletApplication {

  public static void main(String[] args) {
    SpringApplication.run(ServletApplication.run);
  }

}
 */
