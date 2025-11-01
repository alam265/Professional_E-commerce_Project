package com.springproject.profEcomWebApp.controller;

import com.springproject.profEcomWebApp.model.AppRole;
import com.springproject.profEcomWebApp.model.Role;
import com.springproject.profEcomWebApp.model.User;
import com.springproject.profEcomWebApp.repository.RoleRepository;
import com.springproject.profEcomWebApp.repository.UserRepository;
import com.springproject.profEcomWebApp.security.jwt.JwtUtils;
import com.springproject.profEcomWebApp.security.request.LoginRequest;
import com.springproject.profEcomWebApp.security.request.SignupRequest;
import com.springproject.profEcomWebApp.security.response.MessageResponse;
import com.springproject.profEcomWebApp.security.response.UserInfoResponse;
import com.springproject.profEcomWebApp.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RoleRepository roleRepository;


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();


        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(),
                userDetails.getUsername(), roles, jwtToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid  SignupRequest signupRequest){
       if( userRepository.existsByUserName(signupRequest.getUsername())){
           return ResponseEntity.badRequest().body(new MessageResponse("Error: username is already taken"));
       }
        if( userRepository.existsByUserEmail(signupRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already taken"));
        }
        User user = new User(signupRequest.getUsername(), signupRequest.getEmail(),passwordEncoder.encode(signupRequest.getPassword()));

        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null){
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow(()-> new RuntimeException("Error: role is not listed"));
            roles.add(userRole);

        }
        else {
            strRoles.forEach(r-> {
                switch (r) {
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN).orElseThrow(()-> new RuntimeException("Error"));
                        roles.add(adminRole);
                        break;
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER).orElseThrow(()-> new RuntimeException("Error"));
                        roles.add(sellerRole);
                        break;

                    default:
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow(()-> new RuntimeException("Error: role is not listed"));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);




        return ResponseEntity.ok().body(new MessageResponse("User Registered Successfully"));
    }
}
