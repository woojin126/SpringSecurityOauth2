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