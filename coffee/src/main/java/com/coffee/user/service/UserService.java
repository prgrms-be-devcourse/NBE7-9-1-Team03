package com.coffee.user.service;

import com.coffee.global.exception.ServiceException;
import com.coffee.user.entity.User;
import com.coffee.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(long id){
        return userRepository.findById(id);
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public User join(String email, String username, String address, Long postalCode) {
        User user = new User(email, username, address, postalCode);

        findByEmail(email).ifPresent(u -> {
            throw new ServiceException("401", "이미 사용중인 이메일입니다.");
        });

        return userRepository.save(user);
    }
}
