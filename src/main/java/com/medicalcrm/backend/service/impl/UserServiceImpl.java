package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.service.UserService;

import com.medicalcrm.backend.exception.BusinessException;
import com.medicalcrm.backend.exception.NotFoundException;

import com.medicalcrm.backend.repository.UserRepository;

import com.medicalcrm.backend.model.User;
import com.medicalcrm.backend.model.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(String username,
                          String password,
                          String email,
                          Role role){
        if (userRepository.existsByUsername(username)){
            throw new BusinessException("Username already exists");
        }

        if (userRepository.existsByEmail(email)){
            throw new BusinessException("Email already exists");
        }

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(password));

        User saved = userRepository.save(user);

        log.info("User created: {}", username);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id){
        return userRepository.findById(id)
                .orElseThrow(()->
                        new NotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public User getByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(()->
                        new NotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @Override
    public void enableUser(Long userId){
        User user = getById(userId);
        user.setEnabled(true);

        log.info("User {} enabled", userId);
    }

    @Override
    public void disableUser(Long userId){
        User user = getById(userId);
        user.setEnabled(false);

        log.info("User {} disabled", userId);
    }

    @Override
    public void changePassword(Long userId,
                               String oldPassword,
                               String newPassword){

        User user = getById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())){
            throw new BusinessException("Wrong old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        log.info("User {} changed password", userId);
    }

    @Override
    public void changeRole(Long userId, Role role){
        User user = getById(userId);

        user.setRole(role);

        log.info("User {} role changed to {}", userId, role);
    }


    @Override
    public void deleteUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        userRepository.deleteById(userId);

        log.warn("User {} deleted", userId);
    }

    @Override
    public void updateEmail(String username, String newEmail) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessException("Email already in use");
        }

        user.setEmail(newEmail);

        log.info("User {} updated email", username);
    }


}
