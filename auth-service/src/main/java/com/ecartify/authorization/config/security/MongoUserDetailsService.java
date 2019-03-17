package com.ecartify.authorization.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecartify.authorization.domain.User;
import com.ecartify.authorization.repository.UserRepository;

@Service
public class MongoUserDetailsService implements UserDetailsService
{
    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException
    {
        return userRepository.findById(userName).orElseThrow(()->new UsernameNotFoundException(userName));
    }
}
