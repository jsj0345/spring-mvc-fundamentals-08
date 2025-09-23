package hello.servlet.basic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // 스프링 핵심 원리 - 기본편에서 lombok을 다뤘음. 어노테이션이 있으면 Setter가 눈에 안보여도 이미 만들어진 상태로 있음.
public class HelloData {

  private String username;
  private int age;


}

/*
package hello.servlet.basic;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // 어노테이션이 있으면 Setter가 눈에 안보여도 이미 생성되어있음.
public class HelloData {

  private String username;
  private int age;

}
 */
