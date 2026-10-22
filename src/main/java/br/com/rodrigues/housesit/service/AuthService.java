package br.com.rodrigues.housesit.service;

import br.com.rodrigues.housesit.domain.model.Household;
import br.com.rodrigues.housesit.domain.model.User;
import br.com.rodrigues.housesit.domain.repository.HouseholdRepository;
import br.com.rodrigues.housesit.domain.repository.UserRepository;
import br.com.rodrigues.housesit.dto.*;
import br.com.rodrigues.housesit.security.GoogleTokenVerifierService;
import br.com.rodrigues.housesit.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       HouseholdRepository householdRepository,
                       GoogleTokenVerifierService googleTokenVerifierService,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse authenticateWithGoogle(GoogleAuthRequest request) {
        GoogleTokenVerifierService.GoogleUserInfo googleUserInfo = googleTokenVerifierService.verifyToken(request.idToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID Token"));

        User user = userRepository.findByGoogleSub(googleUserInfo.sub())
                .or(() -> userRepository.findByEmail(googleUserInfo.email()))
                .orElseGet(() -> {
                    User newUser = new User(
                            googleUserInfo.email(),
                            googleUserInfo.name(),
                            googleUserInfo.picture(),
                            googleUserInfo.sub()
                    );
                    return newUser;
                });

        user.setName(googleUserInfo.name());
        if (googleUserInfo.picture() != null) {
            user.setAvatarUrl(googleUserInfo.picture());
        }
        user.setGoogleSub(googleUserInfo.sub());
        user = userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserDto.fromEntity(user), HouseholdDto.fromEntity(user.getHousehold()));
    }

    @Transactional
    public AuthResponse devLogin(DevLoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    User newUser = new User(
                            request.email(),
                            request.name(),
                            request.avatarUrl() != null ? request.avatarUrl() : "https://api.dicebear.com/7.x/bottts/svg?seed=" + request.email(),
                            "dev-sub-" + request.email()
                    );
                    return newUser;
                });

        user.setName(request.name());
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        user = userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserDto.fromEntity(user), HouseholdDto.fromEntity(user.getHousehold()));
    }
}
