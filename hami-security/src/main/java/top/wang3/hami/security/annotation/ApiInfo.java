package top.wang3.hami.security.annotation;


import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
public class ApiInfo {

    AccessControl accessControl;
    HttpMethod httpMethod;
    String[] patterns;
    String[] roles;
    String[] authorities;

}
