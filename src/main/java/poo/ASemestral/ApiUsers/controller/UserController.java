package poo.ASemestral.ApiUsers.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poo.ASemestral.ApiUsers.entity.User;
import poo.ASemestral.ApiUsers.service.UserService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserDto createUserDto) {
        var userId = userService.createUser(createUserDto);
        return ResponseEntity.created(URI.create("/api/users" + userId.toString())).build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable("userId") String userId) {
       var user = userService.getUserById(userId);
       if (user.isPresent()){
           return ResponseEntity.ok(user.get());
       } else {
           return ResponseEntity.notFound().build();
       }

    }
    @GetMapping
    public ResponseEntity<List<User>> listUsers() {
        var users =  userService.listUsers();
        return ResponseEntity.ok(users);

    }

    @PutMapping
    public ResponseEntity<Void> updateUserById(@PathVariable("userId") String userId,
                                               @RequestBody UpdateUserDto updateUserDto) {
        userService.updateUserById(userId, updateUserDto);
        return ResponseEntity.noContent().build();

    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteById(@PathVariable("userId") String userId) {
        userService.deleteById(userId);
        // var users = userService.listUsers();

        return ResponseEntity.noContent().build();

    }
}