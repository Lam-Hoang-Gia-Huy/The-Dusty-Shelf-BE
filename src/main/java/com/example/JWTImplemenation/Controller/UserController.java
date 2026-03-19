package com.example.JWTImplemenation.Controller;

import com.example.JWTImplemenation.DTO.UserDTO;
import com.example.JWTImplemenation.Service.IService.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable("id") Integer id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable("id") Integer id,
            @RequestPart("user") UserDTO userDTO,
            @RequestPart("avatar") MultipartFile avatarFile) {
        return userService.update(id, userDTO, avatarFile);
    }

    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteById(@PathVariable("id") Integer id) {
    // return userService.deleteById(id);
    // }
    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable("id") Integer id) {
        return userService.deleteById(id);
    }

    @PutMapping("/activate/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable Integer id) {
        return userService.activateUser(id);
    }

    @GetMapping("/staff")
    public ResponseEntity<List<UserDTO>> findAllStaff() {
        return userService.findAllStaff();
    }
}
