package ru.vortex.geocoder.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vortex.geocoder.dto.RegisterRequestDto;
import ru.vortex.geocoder.model.User;
import ru.vortex.geocoder.repository.UserRepository;

@Service
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Transactional
    public String register(RegisterRequestDto request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Пользователь уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_" + request.getRole().toUpperCase());

        if (userRepository.count() == 0) {
            user.setRole("ROLE_ADMIN");
        }

        userRepository.save(user);
        return jwtService.generateToken(user);
    }

    public String login(String username, String password) {
        User user = (User) loadUserByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Неверный пароль");
        }
        return jwtService.generateToken(user);
    }
}