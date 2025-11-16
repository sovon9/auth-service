package com.sovon9.authentication_service.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sovon9.authentication_service.entities.User;

public class MyUserDetailsService/* implements UserDetailsService*/ {

//	private UserRepository userRepository;
//	
//	@Autowired
//	public MyUserDetailsService(UserRepository userRepository)
//	{
//	   this.userRepository=userRepository;	
//	}
//	
//	@Override
//	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//		Optional<User> optionalUser = userRepository.findById(username);
//		if(!optionalUser.isPresent())
//		{
//			throw new UsernameNotFoundException("username "+username+" not found");
//		}
//		return new org.springframework.security.core.userdetails.User(username, optionalUser.get().getPassword(), null);
//	}

}
