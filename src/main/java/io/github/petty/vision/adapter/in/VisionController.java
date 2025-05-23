package io.github.petty.vision.adapter.in;

import io.github.petty.vision.helper.ImageValidator;
import io.github.petty.vision.helper.ImageValidator.ValidationResult;
import io.github.petty.vision.port.in.VisionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;

@Controller
@RequestMapping("/vision")
@RequiredArgsConstructor
public class VisionController {
    private final VisionUseCase vision;
    private final ImageValidator imageValidator;

    @GetMapping("/upload")
    public String page() {
        return "visionUpload";
    }

    @PostMapping("/species")
    @ResponseBody
    public String getSpeciesInterim(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName
    ) throws IOException {
        // 파일 유효성 검사
        ValidationResult vr = imageValidator.validate(file);
        if (!vr.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, vr.getMessage());
        }

        // 기존 서비스 호출
        return vision.interim(file.getBytes(), petName);
    }

    @PostMapping("/analyze")
    @ResponseBody
    public String analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName
    ) {
        // 파일 유효성 검사
        ValidationResult vr = imageValidator.validate(file);
        if (!vr.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, vr.getMessage());
        }

        // 기존 서비스 호출
        return vision.analyze(file, petName);
    }
}
