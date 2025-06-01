package io.github.petty.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEditDTO {
    private String name;         // 사용자 이름
    private String displayName;  // 닉네임
    private String phone;        // 전화번호
}
