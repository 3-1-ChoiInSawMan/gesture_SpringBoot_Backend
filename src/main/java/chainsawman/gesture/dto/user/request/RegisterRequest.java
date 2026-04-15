package chainsawman.gesture.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterRequest {
    private String id;
    private String email;
    private String password;
    private String nickname;
}
