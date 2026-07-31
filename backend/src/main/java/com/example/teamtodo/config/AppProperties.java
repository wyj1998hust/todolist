package com.example.teamtodo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private boolean migrateOnly;
  private final Auth auth = new Auth();
  private final Bootstrap bootstrap = new Bootstrap();

  public boolean isMigrateOnly() {
    return migrateOnly;
  }

  public void setMigrateOnly(boolean migrateOnly) {
    this.migrateOnly = migrateOnly;
  }

  public Auth getAuth() {
    return auth;
  }

  public Bootstrap getBootstrap() {
    return bootstrap;
  }

  public static class Auth {
    private String jwtSecret;
    private String cookieName = "TODO_SESSION";
    private long sessionHours = 12;
    private boolean secureCookie;

    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
    public String getCookieName() { return cookieName; }
    public void setCookieName(String cookieName) { this.cookieName = cookieName; }
    public long getSessionHours() { return sessionHours; }
    public void setSessionHours(long sessionHours) { this.sessionHours = sessionHours; }
    public boolean isSecureCookie() { return secureCookie; }
    public void setSecureCookie(boolean secureCookie) { this.secureCookie = secureCookie; }
  }

  public static class Bootstrap {
    private boolean enabled = true;
    private String adminUsername;
    private String adminDisplayName;
    private String adminPassword;
    private String memberUsernames;
    private String memberPassword;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getAdminDisplayName() { return adminDisplayName; }
    public void setAdminDisplayName(String adminDisplayName) { this.adminDisplayName = adminDisplayName; }
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
    public String getMemberUsernames() { return memberUsernames; }
    public void setMemberUsernames(String memberUsernames) { this.memberUsernames = memberUsernames; }
    public String getMemberPassword() { return memberPassword; }
    public void setMemberPassword(String memberPassword) { this.memberPassword = memberPassword; }
  }
}
