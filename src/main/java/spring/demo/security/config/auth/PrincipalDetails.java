package spring.demo.security.config.auth;

import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import spring.demo.security.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 시큐리티가 /login 주소 요청이 오면 낚아채서 로그인을 진행시킨다
 * 로그인 진행이 완료가 되면 시큐리티 session을 만들어준다. -> 같은 세션공간인데 시큐리티만의 세션공간을 가진다 (Security ContextHolder) <- 여기에 세션정보를 저장시킴
 * 위의 security session에 들어갈수 있는 오브젝트 => Authentication 타입 객체여야만한다.
 * Authentication 안에에는 User정보가 있어야 됨
 * 이것도 클래스가 정해져있다 User오브젝트타입 => UserDetails 타입 객체 여야만함
 *
 * 정리: Security Session 안에들어갈수있는 인증정보 => Authentication 객체여야만함함 =>
 * 이안에 필요한정보는 UserDetails(PrincipalDetails를 이제 Authentication 객체안에 넣을수 있다.) 이어야만함
 * */

/**
 * Spring Security에서 사용자의 정보를 담는 인터페이스는 UserDetails 인터페이스이다.
 * 우리가 이 인터페이스를 구현하게 되면 Spring Security에서 구현한 클래스를 사용자 정보로 인식하고 인증 작업을 한다.
 * 쉽게 말하면 UserDetails 인터페이스는 VO 역할을 한다고 보면 된다. 그래서 우리는 사용자의 정보를 모두 담아두는 클래스를 구현할 것이다.
 * UserDetails 인터페이스를 구현하게 되면 오버라이드되는 메소드들이 있다. 이 메소드들에 대해 파악을 해야 된다.
 * 그리고 회원 정보에 관한 다른 정보(이름, 나이, 생년월일, ...)도 추가해도 된다. 오버라이드되는 메소드들만 Spring Security에서 알아서 이용하기 때문에
 * 따로 클래스를 만들지 않고 멤버변수를 추가해서 같이 사용해도 무방하다. 만든 멤버변 수들은 getter, setter를 만들어서 사용하면 된다.
 */
@Data
public class PrincipalDetails implements UserDetails, OAuth2User {//DB에서 유저정보를 가져와 사용자의 정보를 담는 인터페이스이다.
                                         //일반로그인정보 , Oauth2로 로그인한정보
    private User user;//컴포지션 유저정보
    private Map<String,Object> attributes;//auth정보담을곳

    //일반 로그인전용
    public PrincipalDetails(User user){
        this.user = user;
    }
    //생성자 오버로딩 (Oauth 로그인 전용)
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return null;
    }

    //해당 User의 권한을 리턴하는 곳!!
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //user.getRole();//타입이 스트링이라 바로 반환은 불가능 아래처럼하면됨
        Collection<GrantedAuthority> collect = new ArrayList<>();
        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole();
            }
        });
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override //이계정 만료됬니? 아니오
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override //계정잠겼니? 아니오
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override //계정 비밀번호 기간이 지났니? 아니오 = true
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {

        // 우리 사이트에서 1년동안 회원이 로그인을 안하면 휴먼 계정으로 하기로 했다면
        // user 엔티티에 TimeStamp loginDate; 를추가해주고
        // user.getLoginDate(); 를해주고
        // 현재시간 - 로그인시간 을해서 => 이게 1년을 초과하면 return을 false 해주면된다
        return true;
    }


}
