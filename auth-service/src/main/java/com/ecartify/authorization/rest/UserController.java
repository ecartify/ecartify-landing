package com.ecartify.authorization.rest;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ecartify.authorization.domain.User;
import com.ecartify.authorization.domain.UserCreateRequest;
import com.ecartify.authorization.domain.UserUpdateRequest;
import com.ecartify.authorization.exceptions.UserServiceException;
import com.ecartify.authorization.service.UserService;

@RestController()
@RequestMapping("users")
public class UserController
{
    @Autowired
    private UserService userService;

    @RequestMapping( method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public User createUser(Principal principal,@RequestBody UserCreateRequest userCreateRequest)
    {
        return userService.createUser(userCreateRequest);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @PreAuthorize("principal == #userId")
    public User updateUser(@PathVariable("userId") String userId, @RequestBody UserUpdateRequest userUpdateRequest)
            throws UserServiceException
    {
        return userService.updateUser(userId, userUpdateRequest);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @PreAuthorize("principal == #userId")
    public User getUser(@PathVariable("userId") String userId)
    {
        return userService.getUser(userId);
    }
}
