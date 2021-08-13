package spring.demo.security.config.oauth.provider;

public interface OAuth2UserInfo {
    String getProviderId(); //facebook primaryKey Id
    String getProvider(); //facebook
    String getEmail();
    String getName();
}
