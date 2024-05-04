package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    // wire in the user repository (~ 1 line)

    @Autowired
    private UserRepository userRepository;

    /**
     * Create an user entity with information given in the payload, store it in the
     * database
     * 
     * @param payload: Data about the user to be saved
     * @return id of the created user in 200 OK response if successful, bad request
     *         if thing go wrong
     */
    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        User user = new User();
        user.setEmail(payload.getEmail());
        user.setName(payload.getName());
        User savedUser = userRepository.save(user);
        if (savedUser != null) {
            return ResponseEntity.ok(savedUser.getId()); // Return only the user ID
        }
        return ResponseEntity.internalServerError().build();
    }

    /***
     * @param userId: Id of the user to be deleted
     * @return 200 OK if a user with the given ID exists, and the deletion is
     *         successful
     *         404 Not Found if a user with the ID does not exist
     */

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRepository.delete(user);
            // Return 200 OK if a user with the given ID exists, and the deletion is
            // successful
            return ResponseEntity.ok("User deleted successfully");

        } else {
            // Return 404 Not Found if a user with the ID does not exist
            return ResponseEntity.notFound().build();
        }

    }
}
