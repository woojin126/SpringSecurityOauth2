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
//https://velog.io/@sa833591/Spring-Security-2 시큐리티
//userDetails 부분 https://programmer93.tistory.com/68
@Data
public class PrincipalDetails implements UserDetails, OAuth2User {//Spring Security에서 사용자의 정보를 담는 인터페이스이다.
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
