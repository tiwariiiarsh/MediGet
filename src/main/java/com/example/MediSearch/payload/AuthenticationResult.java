package com.example.MediSearch.payload;

//import com.Ecommerce.project.security.response.UserInfofResponse;
import com.example.MediSearch.security.response.UserInfofResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
@AllArgsConstructor
public class AuthenticationResult {

    private final UserInfofResponse response;
    private final ResponseCookie jwtCookie;

}
