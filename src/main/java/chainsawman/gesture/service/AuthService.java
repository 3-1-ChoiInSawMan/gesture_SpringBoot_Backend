package chainsawman.gesture.service;

import chainsawman.gesture.dto.user.request.LoginRequest;
import chainsawman.gesture.dto.user.response.LoginResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    // 로그인
    public LoginResponse login(LoginRequest loginRequest) {

    }
}
