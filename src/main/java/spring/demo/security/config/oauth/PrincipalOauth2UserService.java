package spring.demo.security.config.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import spring.demo.security.config.auth.PrincipalDetails;
import spring.demo.security.config.oauth.provider.FacebookUserInfo;
import spring.demo.security.config.oauth.provider.GoogleUserInfo;
import spring.demo.security.config.oauth.provider.NaverUserInfo;
import spring.demo.security.config.oauth.provider.OAuth2UserInfo;
import spring.demo.security.model.User;
import spring.demo.security.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

/**
 * 1.SecurityConfig 에적었던 후처리를 여기서함 (loadUser)
 * 2.구글로 부터 받은 userRequest 데이터에 대한 후처리함수
 * 여기서 액세스토큰 받고 , 사용자프로필 받은 정보가 userRequest에 리턴됨
 */
@Component
@Slf4j
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Autowired @Lazy
    public PrincipalOauth2UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * ouath2-Client 는 액세스토큰 + 사용자프로필을 한번에 끌고온다했죠?
     * 그정보가 아래 userRequest에 담겨있습니다.
     * 
     * 구글 로그인 버튼 클릭시 -> 구글로그인 창 -> 로그인 완료 -> code 리턴받은(oauth2-Client 라이브러리가 받아준다) -> Accesstoken 요청 ==>여기까지가 userRequest정보
     * userRequest정보로 -> loadUser함수로 회원 프로필을 받을수 있다. -> google로부터 회원프로필을 받게됨
     * 결론 loadUser의 역할은 구글로부터 회원 프로필정보를 받아준다
     *
     * tip)이함수가 종료되는시점에 @AuthenticationPrincipal 어노테이션이 만들어진다
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("getClientRegistration={}", userRequest.getClientRegistration().getRegistrationId());//어떤 oauth로 로그인했는지 확인 가능 == google
        log.info("getAccessToken={}", userRequest.getAccessToken().getTokenValue());//
        log.info("getCilentname={}" , userRequest.getClientRegistration().getClientName());


        OAuth2User oAuth2User = super.loadUser(userRequest); //회원 프로필 받는시점




        log.info("googleInfo={}", oAuth2User.getAttributes());
        OAuth2UserInfo oAuth2UserInfo = null; //각각의 프로필이 제공하는 key value가 다를수있어서 인터페이스를 이용해서 통합
        if(userRequest.getClientRegistration().getRegistrationId().equals("google")){
            log.info("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        }else if(userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            log.info("페이스북 로그인 요청");
            oAuth2UserInfo = new FacebookUserInfo(oAuth2User.getAttributes());
        }else if(userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            log.info("네이버 로그인 요청");
            oAuth2UserInfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
        }
        else {
            log.info("구글, 페이스북,네이버만 지원");
        }


        String provider = oAuth2UserInfo.getProvider(); //google
        String providerId = oAuth2UserInfo.getProviderId(); //Sub 구글 primary key값 , 페이스북은 Null이 나올것 sub라는 값이 없기때문, 페이스북 프라이머리키는 Id라고 나와있음
        String username = provider+"_"+providerId; //google_sub  이렇게하면 어우스회원과. 일반회원 정보가 충돌날일이없다
        String password = bCryptPasswordEncoder.encode("1234"); //의미없다
        String providerEmail = oAuth2UserInfo.getEmail();


        String role = "ROLE_USER";

        User userEntity = userRepository.findByUsername(username);

        if(userEntity != null) {
            System.out.println("이미 로그인한적 있습니다. 당신은 자동회원가입이 되어 있습니다.");
        }
            User user = Optional.ofNullable(userEntity).orElseGet(() -> {
                System.out.println("Oauth 로그인 최초 입니다");
                return userRepository.save(User.builder()
                        .username(username)
                        .password(password)
                        .email(providerEmail)
                        .role(role)
                        .provider(provider)
                        .providerId(providerId)
                        .build()
                );
            });

        System.out.println("유저 = " + user);
        System.out.println("oAuth2User.getAttributes() = " + oAuth2User.getAttributes());
        //googleInfo 정보로 강제로 회원가입을 해볼것
        return new PrincipalDetails(user,oAuth2User.getAttributes()); //이게이제 Authentication 객체안에 들어감

/**
 * 회원가입 할정보들
 * "googleInfo={}" ->  sub=115757493514499791335 name=복자, given_name=자, family_name=복, picture=https://lh3.googleusercontent.com/a/AATXAJzUyzNrq5u5LkEqlnuvMUcpqVpxpDqZGfE2qj86=s96-c, email=woojin126789@gmail.com, email_verified=true, locale=ko}
 * username : 115757493514499791335
                * password = "암호화(겟인데어)"
                * email= "woojin126789@gmail.com"
                * role = "ROLE_USER"
                */

    }

}
