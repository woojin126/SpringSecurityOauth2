package spring.demo.security.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Collection;

/**
 * AuthenticationProvider 인터페이스는 화면에서 입력한 로그인 정보와 DB에서 가져온 사용자의 정보를 비교해주는 인터페이스이다.
 * 해당 인터페이스에 오버라이드되는 authenticate() 메서드는 화면에서 사용자가 입력한 로그인 정보를 담고 있는 Authentication 객체를 가지고 있다.
 * 그리고 DB에서 사용자의 정보를 가져오는 건 UserDetailsService 인터페이스에서 loadUserByUsername() 메서드로 구현했다.
 * 따라서 authenticate() 메서드에서 loadUserByUsernmae() 메서드를 이용해 DB에서 사용자 정보를 가져와서 Authentication 객체에서 화면에서 가져온 로그인 정보와 비교하면 된다.
 * AuthenticationProvider 인터페이스는 인증에 성공하면 인증된 Authentication 객체를 생성하여 리턴하기 때문에 비밀번호,
 * 계정 활성화, 잠금 모든 부분에서 확인이 되었다면 리턴해주도록 하자.
 *
 */

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final PrincipalDetailsService userDetailsService;
    private final BCryptPasswordEncoder encodePwd;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails user = userDetailsService.loadUserByUsername(username);

        if (user == null || !username.equals(user.getUsername()) || !encodePwd.matches(password, user.getPassword())) {
            throw new BadCredentialsException("아이디 비밀번호가 일치하지 않습니다");
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());

        return authenticationToken;
        //return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());

    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 스프링 Security가 요구하는 UsernamePasswordAuthenticationToken 타입이 맞는지 확인
        return authentication.equals(UsernamePasswordAuthenticationToken.class);


    }

}