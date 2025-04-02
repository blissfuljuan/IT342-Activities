package Delima.com.example.OAuth2demo.UserController;
import Delima.com.example.OAuth2demo.DTO.LoginRequest;
import Delima.com.example.OAuth2demo.DTO.RegisterRequest;
import Delima.com.example.OAuth2demo.Service.UserService;
import Delima.com.example.OAuth2demo.Repository.UserRepository;
import  Delima.com.example.OAuth2demo.Entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController2 {

    private final UserService userService;

    public UserController2(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request.getUsername(), request.getPassword());
            return ResponseEntity.ok("User registered successfully: " + user.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        try {
            User user = userService.authenticateUser(request.getUsername(), request.getPassword());
            return ResponseEntity.ok("User logged in successfully: " + user.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}