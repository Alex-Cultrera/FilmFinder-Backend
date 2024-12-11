package com.codercultrera.FilmFinder_Backend.web;

import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.UserInformation;
import com.codercultrera.FilmFinder_Backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

//    @PostMapping("/name")
//    public String getFirstName(@RequestBody UserInformation userInfo) {
//        System.out.println(userInfo.getEmail());
//        User user = userService.findByEmail(userInfo.getEmail());
//        return user.getFirstName();
//    }




}
