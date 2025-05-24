package io.github.petty.vision.service;

import io.github.petty.vision.helper.PromptFactory;
import io.github.petty.vision.helper.SpeciesDetector;
import io.github.petty.vision.port.in.VisionUseCase;
import io.github.petty.vision.port.out.GeminiPort;
import io.github.petty.vision.port.out.TogetherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisionServiceImpl implements VisionUseCase {
    private final SpeciesDetector detector;
    private final PromptFactory prompt;
    private final GeminiPort gemini;
    private final TogetherPort together;

    /**
     * 중간 결과 캐시: 이미지 바이트와 petName 조합으로 키 생성
     */
    @Override
    @Cacheable(value = "speciesResults",
            key = "#petName + '_' + T(io.github.petty.vision.service.VisionServiceImpl).generateCacheKey(#image)")
    public String interim(byte[] image, String petName) {
        String species = detector.detect(image);
        return prompt.interimMsg(petName, species);
    }

    /**
     * 상세 분석 캐시: MultipartFile과 petName 조합으로 키 생성
     */
    @Override
    @Cacheable(value = "visionResults",
            key = "#petName + '_' + T(io.github.petty.vision.service.VisionServiceImpl).generateCacheKey(#file)")
    public String analyze(MultipartFile file, String petName) {
        byte[] img;
        try {
            img = file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 읽기 실패", e);
        }

        String species = detector.detect(img);
        String interim = prompt.interimMsg(petName, species);

        try {
            return gemini.generate(
                    prompt.toGeminiReq(img, petName, species)
            ).plainText();
        } catch (Exception gex) {
            log.warn("Gemini 실패 → Together fallback", gex);
        }

        try {
            return together.generate(
                    prompt.toTogetherReq(img, petName)
            ).plainText();
        } catch (Exception tex) {
            log.error("Together 실패", tex);
            return interim + "\n\n최종 분석 실패";
        }
    }

    /**
     * SHA-256 해시(Base64 URL-safe)로 캐시 키 생성
     * @param file MultipartFile
     */
    @SuppressWarnings("unused")
    public static String generateCacheKey(MultipartFile file) {
        try {
            byte[] data = file.getBytes();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            return Base64.getUrlEncoder().encodeToString(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            return file.getOriginalFilename() + "_" + file.getSize();
        }
    }

    /**
     * byte[]용 캐시 키 오버로드
     */
    @SuppressWarnings("unused")
    public static String generateCacheKey(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            return Base64.getUrlEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(java.util.Arrays.hashCode(data));
        }
    }
}
