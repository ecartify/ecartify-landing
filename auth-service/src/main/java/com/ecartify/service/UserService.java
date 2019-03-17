package com.ecartify.service;

import java.util.Arrays;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecartify.domain.User;
import com.ecartify.domain.UserCreateRequest;
import com.ecartify.domain.UserUpdateRequest;
import com.ecartify.exceptions.UserServiceException;
import com.ecartify.repository.UserRepository;

@Service
public class UserService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    public User createUser(UserCreateRequest userCreateRequest)
    {

        User user = userRepository.findById(userCreateRequest.getUsername()).orElse(null);
        if(user != null)
            throw new UserServiceException("User Already Exists");
        user = new User();
        user.setUsername(userCreateRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userCreateRequest.getPassword()));
        user.setEnabled(true);
        //FIXME below needs to be fixed
        user.setAuthorities(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        userRepository.save(user);
        return user;
    }

    public User updateUser(String userID, UserUpdateRequest userUpdateRequest) throws UserServiceException
    {
        User user = userRepository.findById(userID).orElseThrow(() -> new UserServiceException("User Not Found"));
        if (passwordEncoder.matches(userUpdateRequest.getOldPassword(), user.getPassword()))
        {
          user.setPassword(passwordEncoder.encode(userUpdateRequest.getNewPassword()));
          userRepository.save(user);
        }else{
            throw new UserServiceException("Old password didn't match");
        }
        return user;
    }

    public User getUser(String userId)
    {
     return userRepository.findById(userId).orElseThrow(()->new UserServiceException("User Not Found"));
    }
}
