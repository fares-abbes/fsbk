package com.mss.backOffice.services;



import com.mss.backOffice.request.UserPrincipal;
import com.mss.unified.entities.User;
import com.mss.unified.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
    	
        User user = userRepository.findByUserName(username)
                	.orElseThrow(() -> 
                        new UsernameNotFoundException("User Not Found with -> username or email : " + username)
        );

        return UserPrincipal.create( user);
    }
}
