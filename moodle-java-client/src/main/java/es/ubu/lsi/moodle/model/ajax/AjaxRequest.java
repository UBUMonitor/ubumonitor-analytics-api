package es.ubu.lsi.moodle.model.ajax;

import lombok.Data;

@Data
public class AjaxRequest<T> {
  private Integer index;
  private String methodname;
  private T args;

  public AjaxRequest(Integer index, String methodname, T args) {
    this.index = index;
    this.methodname = methodname;
    this.args = args;
  }

  public AjaxRequest(String methodname, T args) {
    this(null, methodname, args);
  }
}
