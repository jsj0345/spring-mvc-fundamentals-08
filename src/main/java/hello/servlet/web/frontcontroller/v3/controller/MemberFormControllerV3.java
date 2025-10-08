package hello.servlet.web.frontcontroller.v3.controller;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.v3.ControllerV3;

import java.util.Map;

public class MemberFormControllerV3 implements ControllerV3 {

  @Override
  public ModelView process(Map<String, String> paramMap) {
    return new ModelView("new-form"); // 논리적인 이름에 대한 기준은 정해야한다.

  }
}

/*
package hello.servlet.web.frontcontroller.v3.controller;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.v3.ControllerV3;

import java.util.Map;

public class MemberFormControllerV3 implements ControllerV3 {

  @Override
  public ModelView process(Map<String, String> paramMap) {
    return new ModelView("new-form");
  }

}
 */
