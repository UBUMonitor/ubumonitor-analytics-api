package es.ubu.lsi.moodle.model.ajax;

import lombok.Data;

/** Request envelope for a Moodle AJAX function call. */
@Data
public class AjaxRequest<T> {
  private Integer index;
  private String methodname;
  private T args;

  /**
   * @param index request index used by Moodle's AJAX protocol
   * @param methodname Moodle function name
   * @param args function arguments
   */
  public AjaxRequest(Integer index, String methodname, T args) {
    this.index = index;
    this.methodname = methodname;
    this.args = args;
  }

  /**
   * Creates a request without an explicit protocol index.
   *
   * @param methodname Moodle function name
   * @param args function arguments
   */
  public AjaxRequest(String methodname, T args) {
    this(null, methodname, args);
  }
}
