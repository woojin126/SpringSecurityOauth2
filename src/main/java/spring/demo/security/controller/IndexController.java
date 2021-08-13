package spring.demo.security.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import spring.demo.security.config.auth.PrincipalDetails;
import spring.demo.security.model.User;
import spring.demo.security.repository.UserRepository;

@Controller
@Slf4j
@RequiredArgsConstructor
public class IndexController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encodePwd;

    @GetMapping("/test/login")
    public @ResponseBody String testLogin(Authentication authentication//DI(의존성주입) authentication 안에 principal이  Object로 들어가있어서 다운캐스팅후 사용하면된다. , 여기에 PrincipalDetails객체가 들어오는것
    , @AuthenticationPrincipal PrincipalDetails userDetails){  //세션정보에 접근가능 @AuthenticationPrincipal
        log.info("authentication.getPrincipal={}", authentication.getPrincipal());
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        log.info("authentication={}", principalDetails.getUser());

        log.info("userDetails={}", userDetails.getUser());
        return "세션 정보 확인하기";
    }

    /**
     * 아래는 Oauth2User로 캐스팅해야함, //loginForm에서 구글로 로그인할시에는
     * */
    @GetMapping("/test/oauth/login")
    public @ResponseBody String testOauthLogin(Authentication authentication//DI(의존성주입) authentication 안에 principal이  Object로 들어가있어서 다운캐스팅후 사용하면된다.
       , @AuthenticationPrincipal OAuth2AuthenticatedPrincipal oAuth2AuthenticatedPrincipal){
        log.info("authentication.getPrincipal={}", authentication.getPrincipal());
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.info("oAuth2User.getAuthorities={}", oAuth2User.getAttributes());
        log.info("OAuth2AuthenticatedPrincipal={}", oAuth2AuthenticatedPrincipal.getAttributes());


        return "Oauth2 세션 정보 확인하기";
    }


    @GetMapping("/")
    public @ResponseBody String index(){
        // 머스태치 기본폴더 serc/main/resources/
        // 뷰리졸버 설정:templates(prefix).mustache(suffix) yml에 설정되있음
        return "index"; //기본적으로 src/main/resources/templates/index.mustache를 찾음
    }

    //오우스, 일반로그인 통합버전
    //Oauth 로그인 ,일반 로그인 둘다 PrincipalDetails로 받을수있다.
    //@AuthenticationPrincipal
    @GetMapping("/user") //일반유저로그인을 받으려면
    public @ResponseBody String user(@AuthenticationPrincipal PrincipalDetails userDetails){
        System.out.println("principalDetails = " +userDetails.getUser());
        return "user";
    }

    @GetMapping("/admin")
    public @ResponseBody String admin(){
        return "admin";
    }

    @GetMapping("/manager")
    public @ResponseBody String manager(){
        return "manager";
    }
    // 스프링 시큐리티가 로그인은 해당주소를 낚아채버림 -> SecurityConfig 파일생성후 로그인창 자동으로 뜨떤게 작동안함
    @RequestMapping(value = "/login",method = {RequestMethod.GET,RequestMethod.POST})
    public String login(){
        return "loginForm";
    }

    @GetMapping("/loginForm")
    public String loginForm(){
        return "loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm(){
        return "joinForm";
    }

    @PostMapping("/join")
    public String join(User user){
        log.info("userInfo={}", user);
        user.setRole("ROLE_USER");
        String rawPassword = user.getPassword();
        String encPassword = encodePwd.encode(rawPassword);
        user.setPassword(encPassword);
        userRepository.save(user); //비밀번호가 1234 => 시큐리티로 로그인을 할 수가 없다. 패스워드가 암호화가 안되었기 때문에
        return "redirect:/loginForm";
    }

    @Secured("ROLE_ADMIN") //특정메서드에 간단히걸고싶을떄 시큐얼 컨피그에 설정후 사용가능
    @GetMapping("/info")
    public @ResponseBody String info(){ //누구나 다가지는 경로
        return "개인정보";
    }

    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')") //data() 메서드가 실행되기 전에 실행된다.
    @GetMapping("/data")
    public @ResponseBody String data(){ //누구나 다가지는 경로
        return "데이터 정보";
    }

}
