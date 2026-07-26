package com.sovon9.authentication_service.service;

import com.sovon9.authentication_service.entities.User;
import com.sovon9.authentication_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Autowired
	public MyUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		// Map the stored role string (e.g. "ROLE_USER") to a GrantedAuthority.
		// If the role column is null or empty, fall back to a default role.
		String role = (user.getRole() != null && !user.getRole().isBlank())
				? user.getRole()
				: "ROLE_USER";

		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPassword(),
				List.of(new SimpleGrantedAuthority(role))
		);
	}
}
