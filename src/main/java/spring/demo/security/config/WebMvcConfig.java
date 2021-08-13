package spring.demo.security.config;

import org.springframework.boot.web.servlet.view.MustacheViewResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//머스태치 설정
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override //머스태치를 재설정가능하게해주는 오버라이드 메서드
    public void configureViewResolvers(ViewResolverRegistry registry) {
        MustacheViewResolver resolver = new MustacheViewResolver();
        resolver.setCharset("UTF-8");
        resolver.setContentType("text/html; charset=UTF-8");
        resolver.setPrefix("classpath:/templates/"); //내 프로젝트경로
        resolver.setSuffix(".html"); //이렇게 html로바꾸면 index.mustache가 index.html로 인식할수있다.

        registry.viewResolver(resolver);
    }
}
