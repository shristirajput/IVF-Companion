package com.ivf.companion.config;

import com.ivf.companion.model.Role;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class UserSession {
    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private String token;

    public void cleanUserSession() {
        this.id = null;
        this.username = null;
        this.fullName = null;
        this.role = null;
        this.token = null;
    }

    public boolean isLoggedIn() {
        return this.username != null;
    }
}
