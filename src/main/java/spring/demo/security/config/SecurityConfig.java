package spring.demo.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import spring.demo.security.config.auth.CustomAuthenticationProvider;
import spring.demo.security.config.auth.PrincipalDetailsService;
import spring.demo.security.config.exception.AuthFailureHandler;
import spring.demo.security.config.oauth.PrincipalOauth2UserService;


/**
 * 구글 로그인이 된후 처리 다음
 * 1.코드받기(인증이되었다는거 = 구글에 로그인된 정상적인사람이란것)
 * 2.엑세스토큰을 코들르통해받으면 (사용자 정보에 접근할 권한이 생김)
 * 3.권한을통해서 사용자 프로필정보를 가져오고
 * 4.그 정보를 토대로 회원가입을 자동으로 진행시키기도하고,
 *  4-1.정보를 통해서 회원가입정보를가져올수도 있음
 *  4-2.(이메일,전화번호,이름,아이디) 쇼핑몰을 한다하면 -> 추가적인정보 (집주소도필요),백화점몰 -> 추가적인 정보(vip등급, 일반등급) 한마디로 추가적인 구성이 필요하다면
 *   추가적인 회원가입을 만들어 회원가입을 해야한다.
 */

@Configuration
@EnableWebSecurity //활성화가 되기위한 어노테이션, 스프링 시큐리티 필터(SecurityConfig)가 스프링 필터체인에 등록이 된다.
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@RequiredArgsConstructor
//첫번쨰인자 sucured 어노테이션 활성화, 두번째 인자 메서드 시작전,후 에 처리할 권한
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    private final PrincipalOauth2UserService principalOauth2UserService;
    private final PrincipalDetailsService principalDetailsService;

    //해당 메서드 리턴되는 오브젝트를 Ioc 등록해줌
    @Bean
    public BCryptPasswordEncoder encodePwd(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationFailureHandler failureHandler(){
        return new AuthFailureHandler();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable(); //csrf 비활성화
        http.authorizeRequests()
                //아래 접근권한을 부여한곳으로 들어가면 403 에러가 나올것( 접근권한이 없다는 뜻)
                .antMatchers("/user/**").authenticated() //인증만되면 들어갈 수있는 주소
                .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")//매니저 경로로 오는것은 접근권한이 어드민, 매니저여야하한다
                .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")//어드민 권한만 접근가능
                .anyRequest().permitAll() //다른요청은 모두 허용!!
                .and()
                .formLogin()
                .loginPage("/loginForm")  //이설정을하면  위에 접근권한을 받은 접근권한들이 모두 loginForm 페이지로 가게된다
                .loginProcessingUrl("/login")// /login이 호출이되면 시큐리티가 낚아채서 대신 로그인을 진행해준다
                .defaultSuccessUrl("/") //컨트롤러에 /login을 안만들어도 대신해준다
                .failureHandler(failureHandler())
                .and()
                .oauth2Login() //oauth2Login 설정 시작
                .loginPage("/loginForm")//google login을 할 기본 페이지, 구글로그인후의 후처리가 필요함
                .userInfoEndpoint()// oauth2Login 성공 이후의 설정을 시작 tip) 코드X, (엑세스토큰+사용자프로필정보(o))한번에 받아줌 => Oauth-Client라는 라이브러리의 엄청난 기능
                .userService(principalOauth2UserService);//customOAuth2UserService(PrincipalOauth2UserService) => 구글로그인이 완료된 뒤의 후처리가 필요함, TIP) 코드x (액세스토큰 + 사용자프로필정보 O 바로받아줌) ouath2 라이브러리 특징

        //.failureForwardUrl("/fail_login")
        //.failureUrl("/login?error=true")
    }
}
