package io.github.vylegzhaninn.authservice.service;

import io.github.vylegzhaninn.authservice.exception.UserAlreadyExistsException;
import io.github.vylegzhaninn.authservice.model.AuthenticationRequest;
import io.github.vylegzhaninn.authservice.model.AuthenticationResponse;
import io.github.vylegzhaninn.authservice.model.Role;
import io.github.vylegzhaninn.authservice.model.User;
import io.github.vylegzhaninn.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationResponse login(AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.name(), request.password()));
        return new AuthenticationResponse(jwtService.generateToken(authentication));
    }

    public AuthenticationResponse registration(AuthenticationRequest request) {
        if (userRepository.findByName(request.name()).isPresent()) {
            throw new UserAlreadyExistsException(request.name());
        }
        User user = User.builder()
            .name(request.name())
            .password(passwordEncoder.encode(request.password()))
            .role(Role.USER)
            .build();
        userRepository.save(user.name(), user.password(), user.role().name());
        return login(request);
    }
}
