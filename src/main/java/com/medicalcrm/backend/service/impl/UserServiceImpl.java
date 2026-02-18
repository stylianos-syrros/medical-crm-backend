package com.medicalcrm.backend.service.impl;

import com.medicalcrm.backend.dto.request.CreateUserRequest;
import com.medicalcrm.backend.dto.request.UpdateUserRequest;
import com.medicalcrm.backend.dto.response.UserResponse;
import com.medicalcrm.backend.mapper.UserMapper;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request){

        if (userRepository.existsByUsername(request.getUsername())){
            throw new BusinessException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())){
            throw new BusinessException("Email already exists");
        }

        User user = UserMapper.toEntity(request);

        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(user);

        log.info("User created: {}", saved.getUsername());

        return UserMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id){
        User user = getUserEntity(id);
        checkOwnership(user);

        return UserMapper.toResponse(user);
    }


    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {

        User user = getUserEntity(userId);
        checkOwnership(user);

        UserMapper.updateEntity(user, request);

        log.info("User {} updated", userId);

        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Override
    public void enableUser(Long userId) {

        User user = getUserEntity(userId);

        user.setEnabled(true);

        log.info("User {} enabled", userId);
    }

    @Override
    public void disableUser(Long userId) {

        User user = getUserEntity(userId);

        user.setEnabled(false);

        log.info("User {} disabled", userId);
    }

    @Override
    public void changePassword(Long userId,
                               String oldPassword,
                               String newPassword){

        User user = getUserEntity(userId);
        checkOwnership(user);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())){
            throw new BusinessException("Wrong old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        log.info("User {} changed password", userId);
    }

    @Override
    public void changeRole(Long userId, Role role){
        User user = getUserEntity(userId);

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

    // HELPERS

    private User getUserEntity(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User not found"));
    }

    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new BusinessException("No authentication context available");
        }
        return auth;
    }

    private String getCurrentUsername() {
        return getAuthentication().getName();
    }

    private boolean isAdmin() {
        return getAuthentication().getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private void checkOwnership(User user) {

        if (!isAdmin() && !user.getUsername().equals(getCurrentUsername())) {
            throw new AccessDeniedException("Access denied");
        }
    }


}