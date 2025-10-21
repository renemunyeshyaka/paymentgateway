// DebugController.java
package com.kcoders.controller;

import com.kcoders.entity.User;
import com.kcoders.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @GetMapping("/health")
    public String healthCheck() {
        long userCount = userRepository.count();
        return "Database is working! Total users: " + userCount;
    }
}