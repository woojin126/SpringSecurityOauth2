package spring.demo.security.config.exception;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String DEFAULT_FAILURE_URL= "/login?error=true";
    //private final String DEFAULT_FAILURE_URL= "/login";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = null;

        if (exception instanceof BadCredentialsException || exception instanceof InternalAuthenticationServiceException) {
            errorMessage = " 아이디나 비밀번호가 맞지 않습니다. 다시 확인해 주세요.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = " 회원가입이 안되있는 계정입니다. ";
        } else if (exception instanceof DisabledException) {
            errorMessage = "계정이 비활성화 되었습니다. 관리자에 문의하세요";
        } else if (exception instanceof CredentialsExpiredException) {
            errorMessage = "비밀번호 유효기간이 만료되었습니다. 관리자에게 문의하세요";
        }

        request.setAttribute("errorMessage", errorMessage);

        request.getRequestDispatcher(DEFAULT_FAILURE_URL).forward(request, response);
    }
}
