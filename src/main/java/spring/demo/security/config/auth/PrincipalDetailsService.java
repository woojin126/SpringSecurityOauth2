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

/**
 UserDetailsService 인터페이스는 DB에서 유저 정보를 가져오는 역할을 한다. 해당 인터페이스의 메소드에서 DB의 유저 정보를 가져와서 AuthenticationProvider
 인터페이스로 유저 정보를 리턴하면, 그 곳에서 사용자가 입력한 정보와 DB에 있는 유저 정보를 비교한다. 지금 우리가 할 것은 유저 정보를 가져오는 인터페이스를 구현하는 것이다.
 사용자가 입력한 정보와 비교하는 작업은 이 글에서는 없다. DB에서 유저 정보를 가져오는 작업만 하기 때문에,
 여기에서 필요한 인터페이스는 UserDetails 인터페이스와 UserDetailsService 인터페이스이다. 그럼 시작한다!
 */
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
