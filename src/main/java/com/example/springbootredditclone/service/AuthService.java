package com.example.springbootredditclone.service;




import com.example.springbootredditclone.dto.AuthenticationResponse;
import com.example.springbootredditclone.dto.LoginRequest;
import com.example.springbootredditclone.dto.RegisterRequest;
import com.example.springbootredditclone.exception.SpringRedditException;
import com.example.springbootredditclone.model.NotificationEmail;
import com.example.springbootredditclone.model.User;
import com.example.springbootredditclone.model.VerificationToken;
import com.example.springbootredditclone.repository.UserRepository;
import com.example.springbootredditclone.repository.VerificationTokenRepository;
import com.example.springbootredditclone.security.JWTProvider;
import lombok.AllArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;


import java.time.Instant;



@Service
@AllArgsConstructor
//@Transactional
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JWTProvider jwtProvider;
	private final MailContentBuilder mailContentBuilder;
	private final MailService mailService;
	private final VerificationTokenRepository verificationTokenRepository;
//    private final RefreshTokenService refreshTokenService;
	@Transactional
	public void signup(RegisterRequest registerRequest) {
		User user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(encodePassword(registerRequest.getPassword()));
		user.setCreated(Instant.now());
		user.setEnabled(false);

		userRepository.save(user);

		String token = generateVerificationToken(user);

		mailService.sendMail(new NotificationEmail("Please Activate your Account", user.getEmail(),
				"Thank you for signing up to Spring Reddit, "
						+ "please click on the below url to activate your account : "
						+ "http://localhost:8080/api/auth/accountVerification/" + token));
	}

	public String generateVerificationToken(User user) {
		String token = UUID.randomUUID().toString();
		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setUser(user);

		verificationTokenRepository.save(verificationToken);
		return token;
	}

	private String encodePassword(String password) {
		return passwordEncoder.encode(password);
	}

	public AuthenticationResponse login(LoginRequest loginRequest) {
		Authentication authenticate = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authenticate);
		String authenticationToken = jwtProvider.generateToken(authenticate);
		return new AuthenticationResponse(authenticationToken, loginRequest.getUsername());
	}

	public void verifyAccount(String token) {
		Optional<VerificationToken> verificationTokenOptional = verificationTokenRepository.findByToken(token);
		fetchUserAndEnable(verificationTokenOptional.orElseThrow(() -> new SpringRedditException("Invalid Token")));
	}

	@Transactional
	private void fetchUserAndEnable(VerificationToken verificationToken) {
		String username = verificationToken.getUser().getUsername();
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new SpringRedditException("User Not Found with id - " + username));
		user.setEnabled(true);
		userRepository.save(user);
	}
}
