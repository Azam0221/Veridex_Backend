package com.example.veridex.veridex.service;



import com.example.veridex.veridex.UserPrincipal;
import com.example.veridex.veridex.model.User;
import com.example.veridex.veridex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username);
        System.out.println(user);
        if(user == null){
            System.out.println("User not found");
            throw  new UsernameNotFoundException("User not found");
        }
        return new UserPrincipal(user);
    }
}
