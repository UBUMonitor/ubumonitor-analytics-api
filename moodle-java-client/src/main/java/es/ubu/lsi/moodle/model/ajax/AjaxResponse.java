package es.ubu.lsi.moodle.model.ajax;

public class AjaxResponse<T> {
  private Boolean error;
  private T data;

  public AjaxResponse() {}

  public Boolean getError() {
    return error;
  }

  public void setError(Boolean error) {
    this.error = error;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }
}
