package io.github.petty.users.service;

import io.github.petty.users.dto.CustomUserDetails;
import io.github.petty.users.dto.UserProfileEditDTO;
import io.github.petty.users.entity.Users;
import io.github.petty.users.oauth2.CustomOAuth2User;
import io.github.petty.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;

    public UUID getCurrentUserId(Object principal) {
        if (principal == null) {
            return null;
        }

        // CustomUserDetails 처리 (일반 로그인)
        if (principal instanceof CustomUserDetails) {
            // CustomUserDetails에서 username 얻기
            String username = ((CustomUserDetails) principal).getUsername();
            // username으로 사용자 찾기
            Users user = usersRepository.findByUsername(username);
            return user != null ? user.getId() : null;
        }

        // CustomOAuth2User 처리 (소셜 로그인)
        if (principal instanceof CustomOAuth2User) {
            // OAuth2에서 username 얻기
            String username = ((CustomOAuth2User) principal).getName();
            // username으로 사용자 찾기
            Users user = usersRepository.findByUsername(username);
            return user != null ? user.getId() : null;
        }

        // Users 객체 직접 처리
        if (principal instanceof Users) {
            return ((Users) principal).getId();
        }

        return null;
    }

    // 사용자 정보 가져오기
    public UserProfileEditDTO getUserById(UUID userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new UserProfileEditDTO(user.getName(), user.getDisplayName(), user.getPhone());

    }

    // 사용자 정보 수정
    public Users updateUserProfile(UUID userId, UserProfileEditDTO userProfileEditDTO) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 닉네임 중복 검사
        String newDisplayName = userProfileEditDTO.getDisplayName();
        if (!user.getDisplayName().equals(newDisplayName)) {
            if (usersRepository.existsByDisplayName(newDisplayName)) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }
        }

        user.setName(userProfileEditDTO.getName());
        user.setDisplayName(userProfileEditDTO.getDisplayName());
        user.setPhone(userProfileEditDTO.getPhone());

        return usersRepository.save(user);
    }

    // 닉네임 유효성 검사
    public Map<String, Object> checkDisplayName(UUID currentUserId, String displayName) {
        boolean isCurrentUserDisplayName = isCurrentUserDisplayName(currentUserId, displayName);
        boolean isAvailable = !usersRepository.existsByDisplayName(displayName) || isCurrentUserDisplayName;

        String message;
        if (isCurrentUserDisplayName) {
            message = "현재 사용 중인 닉네임입니다.";
        } else if (isAvailable) {
            message = "사용 가능한 닉네임입니다.";
        } else {
            message = "이미 사용 중인 닉네임입니다.";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("available", isAvailable);
        result.put("message", message);
        return result;
    }

    // 현재 사용자의 닉네임인지 확인
    public boolean isCurrentUserDisplayName(UUID userId, String displayName) {
        Users user = usersRepository.findById(userId).orElse(null);
        return user != null && displayName.equals(user.getDisplayName());
    }
}
