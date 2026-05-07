package com.dropp.userinfo.service;

import com.dropp.userinfo.dto.UserDto;
import com.dropp.userinfo.entity.User;
import com.dropp.userinfo.exception.UserNotFoundException;
import com.dropp.userinfo.mapper.UserMapper;
import com.dropp.userinfo.repo.UserRepo;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepo userRepo;

    public UserService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    public UserDto addUser(UserDto userDto){
        User savedUser = userRepo.save(UserMapper.INSTANCE.mapUserDtoToUser(userDto));
        return UserMapper.INSTANCE.mapUserToUserDto(savedUser);
    }
    public UserDto getUserById(Long userId){
        User userFetched = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return UserMapper.INSTANCE.mapUserToUserDto(userFetched);
    }
}
