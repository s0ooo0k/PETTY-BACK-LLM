package io.github.petty.users.util;

import io.github.petty.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class DisplayNameGenerator {

    private final UsersRepository usersRepository;
    private final Random random = new Random();

    // 귀여운 형용사들
    private static final String[] ADJECTIVES = {
            "귀여운", "활발한", "똑똑한", "용감한", "착한", "재미있는", "친근한", "사랑스러운",
            "깜찍한", "멋진", "예쁜", "훌륭한", "행복한", "밝은", "따뜻한", "부드러운"
    };

    // 반려동물들
    private static final String[] ANIMALS = {
            "강아지", "고양이", "햄스터", "토끼", "앵무새", "거북이", "금붕어", "페럿",
            "친칠라", "기니피그", "고슴도치", "이구아나", "카나리아", "문조", "코카티엘", "잉꼬"
    };

    // 유니크 displayName 생성
    public String generateUniqueDisplayName() {
        String candidate;
        int attempts = 0;
        final int MAX_ATTEMPTS = 50;

        do {
            candidate = generateCuteDisplayName();
            attempts++;

            // 50번 실패하면 UUID 폴백
            if (attempts >= MAX_ATTEMPTS) {
                candidate = generateUuidFallback();
                break;
            }
        } while (usersRepository.existsByDisplayName(candidate));

        return candidate;
    }

    // 형용사 + 동물 + 3자리 숫자
    private String generateCuteDisplayName() {
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String animal = ANIMALS[random.nextInt(ANIMALS.length)];
        String number = String.format("%03d", random.nextInt(1000));

        return adjective + animal + number;
    }

    // UUID 기반 폴백 닉네임 (만약의 경우)
    private String generateUuidFallback() {
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "petty_" + uuid;
    }
}