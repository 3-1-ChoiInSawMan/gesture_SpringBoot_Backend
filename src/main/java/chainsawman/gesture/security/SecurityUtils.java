package chainsawman.gesture.security;

import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.exceptions.auth.NotAuthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotAuthenticatedException();
        }

        if (authentication.getPrincipal() instanceof User user) {
            return user;
        }

        throw new NotAuthenticatedException();
    }
}
