package es.ubu.lsi.ubumonitoranalytics.shared.domain.model;

/** Identifies the authentication flow used to create a session. */
public enum LoginMode {
  CREDENTIALS,
  SSO,
  OFFLINE
}
