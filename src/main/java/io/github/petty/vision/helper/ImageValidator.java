package io.github.petty.vision.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageValidator {

    private final RekognitionClient rekognitionClient;

    private static final Set<String> VALID_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "bmp")
    );
    private static final long MIN_FILE_SIZE = 10 * 1024;        // 10KB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MIN_WIDTH = 200;
    private static final int MIN_HEIGHT = 200;
    private static final float MIN_ANIMAL_CONFIDENCE = 70.0f;
    private static final Set<String> ANIMAL_LABELS = new HashSet<>(
            Arrays.asList("Animal", "Pet", "Dog", "Cat", "Mammal", "Canine", "Feline")
    );

    public ValidationResult validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.invalid("이미지 파일이 없습니다.");
        }
        if (file.getSize() < MIN_FILE_SIZE) {
            return ValidationResult.invalid("이미지 파일이 너무 작습니다. 최소 10KB 이상이어야 합니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ValidationResult.invalid("이미지 파일이 너무 큽니다. 최대 5MB 이하여야 합니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ValidationResult.invalid("파일 형식을 확인할 수 없습니다.");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!VALID_EXTENSIONS.contains(extension)) {
            return ValidationResult.invalid("지원하지 않는 이미지 형식입니다. JPG, PNG, BMP 형식만 지원합니다.");
        }

        try {
            byte[] bytes = file.getBytes();
            if (!hasValidSignature(bytes, extension)) {
                return ValidationResult.invalid("파일 시그니처가 유효하지 않습니다.");
            }
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return ValidationResult.invalid("유효한 이미지 파일이 아닙니다.");
            }
            if (image.getWidth() < MIN_WIDTH || image.getHeight() < MIN_HEIGHT) {
                return ValidationResult.invalid("이미지 해상도가 너무 낮습니다. 최소 200×200 이상이어야 합니다.");
            }
            return validateAnimalContent(bytes);
        } catch (IOException e) {
            log.error("이미지 처리 중 오류 발생", e);
            return ValidationResult.invalid("이미지 파일을 처리하는 중 오류가 발생했습니다.");
        }
    }

    private ValidationResult validateAnimalContent(byte[] bytes) {
        try {
            DetectLabelsRequest request = DetectLabelsRequest.builder()
                    .image(Image.builder().bytes(SdkBytes.fromByteArray(bytes)).build())
                    .maxLabels(10)
                    .minConfidence(50.0f)
                    .build();
            DetectLabelsResponse response = rekognitionClient.detectLabels(request);

            for (Label label : response.labels()) {
                if (ANIMAL_LABELS.contains(label.name()) && label.confidence() >= MIN_ANIMAL_CONFIDENCE) {
                    log.info("동물 감지됨: {}, 신뢰도: {}", label.name(), label.confidence());
                    return ValidationResult.valid();
                }
            }
            return ValidationResult.invalid("반려동물이 감지되지 않았습니다.");
        } catch (Exception e) {
            log.error("Rekognition 오류", e);
            return ValidationResult.invalid("이미지 분석 중 오류가 발생했습니다.");
        }
    }

    // Magic Number Signatures
    private static final byte[] JPG_SIG = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIG = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] BMP_SIG = new byte[]{0x42, 0x4D};

    private boolean hasValidSignature(byte[] data, String ext) {
        if (data.length < 8) return false;
        return switch (ext) {
            case "jpg", "jpeg" -> startsWith(data, JPG_SIG);
            case "png"       -> startsWith(data, PNG_SIG);
            case "bmp"       -> startsWith(data, BMP_SIG);
            default            -> false;
        };
    }

    private boolean startsWith(byte[] data, byte[] sig) {
        for (int i = 0; i < sig.length; i++) {
            if (data[i] != sig[i]) return false;
        }
        return true;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public static ValidationResult valid() {
            return new ValidationResult(true, "유효한 이미지입니다.");
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }
}
