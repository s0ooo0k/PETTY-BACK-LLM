package io.github.petty.pipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Log
public class PromptGeneratorServiceImpl implements PromptGeneratorService {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public String generatePrompt(String extractedPetInfoJson, String location, String info) throws JsonProcessingException {
        // 필수 정보 예외
        if (extractedPetInfoJson.isEmpty()) {
            throw new IllegalArgumentException("에러! 필수 정보가 없습니다.");
        } else if (location.isEmpty()) {
            throw new IllegalArgumentException("에러! 사용자 위치 정보가 없습니다.");
        }

        // 추가 정보 : 비어도 상관 없음
        if (info.isEmpty()) {
            info = "입력된 추가 요청 사항 없음";
        }

        // 기본 문자열
        String petInfoString = """
                {
                %s
                }
                """.formatted(extractedPetInfoJson);
        // JSON 으로 변환
        Map<String, Object> petInfoMap = mapper.readValue(petInfoString, new TypeReference<>() {});
        // location 항목 추가
        petInfoMap.put("location", "%s".formatted(location));
        // info 항목 추가
        petInfoMap.put("info", "%s".formatted(info));
        // JSON 문자열로 변환
        String finalString = mapper.writeValueAsString(petInfoMap);
        log.info(finalString);

        return finalString;
    }
}
