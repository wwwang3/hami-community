package top.wang3.hami.security.annotation;


import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

@Setter
@Getter
public class ApiInfo {

    AccessControl accessControl;
    HttpMethod httpMethod;
    String[] patterns;
    String[] roles;
    String[] authorities;


}
