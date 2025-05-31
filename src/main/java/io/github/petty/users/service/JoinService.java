package io.github.petty.users.service;

import io.github.petty.users.Role;
import io.github.petty.users.dto.JoinDTO;
import io.github.petty.users.entity.Users;
import io.github.petty.users.repository.UsersRepository;
import io.github.petty.users.util.DisplayNameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final UsersRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final DisplayNameGenerator displayNameGenerator;

    public boolean joinProcess(JoinDTO joinDTO) {
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        String name = joinDTO.getName();
        String phone = joinDTO.getPhone();
        String encodedPassword = bCryptPasswordEncoder.encode(password);

        Boolean exists = userRepository.existsByUsername(username);
        if (exists) {
            return false;
        }

        Users users = new Users();
        users.setUsername(username);
        users.setPassword(encodedPassword);
        users.setName(name);
        users.setPhone(phone);
        users.setRole(Role.ROLE_USER.name());
        users.setProvider("local");

        String uniqueDisplayName = displayNameGenerator.generateUniqueDisplayName();
        users.setDisplayName(uniqueDisplayName);

        userRepository.save(users);

        return true;
    }
}
