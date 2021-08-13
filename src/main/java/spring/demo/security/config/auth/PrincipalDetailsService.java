package spring.demo.security.config.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import spring.demo.security.model.User;
import spring.demo.security.repository.UserRepository;

import java.util.Optional;

// 시큐리티 설정에서 loginProcessingUrl("/login")
// login 요청이 오면 자동으로 UserDetailsService 타입으로 IoC 되어있는 loadUserByUsername 함수가 실행
@Service //메모리에 띄우겠다.
@Slf4j
public class PrincipalDetailsService implements UserDetailsService {//Spring Security에서 유저의 정보를 가져오는 인터페이스이다.
    private final UserRepository userRepository;

    @Autowired
    public PrincipalDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     *1.아래 변수 username은 loginform.html 에있는 input type name="username" 과 이름이 완전같아야함
     *2.아래 UserDetails는 Authentication 내부로 들어가게됨(인증을위해)
     *3.그리고 이 Authentication 은  session 내부로 쏙들어감
     *  session(Authentication(Userdetails)) 이런형태라고 생각
     *
     *  tip)이함수가 종료되는시점에 @AuthenticationPrincipal 어노테이션이 만들어진다
     */
    //loadUserByUsername이 반환될때 값이  Authtentication 내부로 들어가게됨
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("usernames={}", username);

        User user = userRepository.findByUsername(username);
        Optional.ofNullable(user).orElseThrow(() -> new UsernameNotFoundException("아이디가 없습니다"));

        return new PrincipalDetails(user);

    }
}
