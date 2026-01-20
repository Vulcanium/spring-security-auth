package com.vulcanium.springsecurityauth.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@RestController
@AllArgsConstructor
public class LoginController {

    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @GetMapping("/user")
    public String getUser() {
        return "Welcome User!";
    }

    @GetMapping("/admin")
    public String getAdmin() {
        return "Welcome Admin!";
    }

    /**
     * Retrieve the relevant information from the Form or via OAuth2 login
     *
     * @param user The Principal converted into a token to retrieve its information
     * @return A formatted string containing all relevant information about the authenticated user
     */
    @GetMapping("/")
    public String getUserInfo(Principal user, @AuthenticationPrincipal OidcUser oidcUser) {
        StringBuilder userInfo = new StringBuilder();
        userInfo.append("Welcome ");

        switch (user) {
            case UsernamePasswordAuthenticationToken token -> userInfo.append(getUsernamePasswordLoginInfo(user));
            case OAuth2AuthenticationToken token -> userInfo.append(getOAuth2LoginInfo(user, oidcUser));
            default -> throw new IllegalStateException("Unexpected value: " + user);
        }

        return userInfo.toString();
    }

    // Returns the user details from the Form login
    private StringBuilder getUsernamePasswordLoginInfo(Principal user) {
        StringBuilder usernameInfo = new StringBuilder();
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) user;

        if (token.isAuthenticated()) {
            User userDetails = (User) Objects.requireNonNull(token.getPrincipal());

            usernameInfo.append(userDetails.getUsername());
        } else {
            usernameInfo.append("NA");
        }

        return usernameInfo;
    }

    // Returns the user details from the OAuth2
    private StringBuilder getOAuth2LoginInfo(Principal user, OidcUser oidcUser) {
        StringBuilder protectedInfo = new StringBuilder();
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) user;
        OAuth2AuthorizedClient authClient = oAuth2AuthorizedClientService.loadAuthorizedClient(
                token.getAuthorizedClientRegistrationId(), token.getName());

        if (token.isAuthenticated()) {
            Map<String, Object> userAttributes = Objects.requireNonNull(token.getPrincipal()).getAttributes();
            String userToken = authClient.getAccessToken().getTokenValue();

            protectedInfo.append(userAttributes.get("name")).append("<br><br>");
            protectedInfo.append("Email: ").append(userAttributes.get("email")).append("<br><br>");
            protectedInfo.append("Access token: ").append(userToken).append("<br><br>");

            // Displays additional information when OpenID Connect is enabled
            if (oidcUser != null && oidcUser.getIdToken() != null) {
                OidcIdToken idToken = oidcUser.getIdToken();

                protectedInfo.append("ID token: ").append(idToken.getTokenValue()).append("<br>");
                protectedInfo.append("Token mapped values: <br>");

                Map<String, Object> claims = idToken.getClaims();
                for (String key : claims.keySet()) {
                    protectedInfo.append(key).append(": ").append(claims.get(key)).append("<br>");
                }
            }
        } else {
            protectedInfo.append("NA");
        }

        return protectedInfo;
    }
}
