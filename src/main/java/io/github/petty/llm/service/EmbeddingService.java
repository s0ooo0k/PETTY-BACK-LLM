package io.github.petty.llm.service;

import io.github.petty.tour.entity.Content;
import io.github.petty.tour.entity.PetTourInfo;
import io.github.petty.tour.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmbeddingService {
    private final ContentRepository contentRepository;
    // OpenAI 임베딩 모델
    private final EmbeddingModel embeddingModel;

    // 임베딩 결과
    public record EmbeddingResult(String id, String text, List<Float> vector) {}


    // 텍스트 정제 -> 임베딩
    public EmbeddingResult embedContent(Content content) {
        String text = prepareContentText(content);
        // 임베딩 벡터 생성
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        float[] output = response.getResults().get(0).getOutput();
         // 수동 변환
        List<Float> vector = new ArrayList<>();
        for (float f : output) {
            vector.add(f);
        }

        // content id + 원문 텍스트 + 임베딩 벡터 반환
        return new EmbeddingResult(content.getContentId().toString(), text, vector);
    }

    // 텍스트 정제 : 장소 설명들 다 모아서 문장으로 만들기
    public String prepareContentText(Content content) {
        StringBuilder sb = new StringBuilder();

        //[관광지 이름]-[주소]-[설명]-[반려동물 동반 정보 | 동반 유형, 추가 정보] 등
        sb.append("[관광지 이름] ").append(content.getTitle()).append("\n");
        sb.append("[주소] ").append(content.getAddr1());
        if(content.getAddr2() != null){
            sb.append(" ").append(content.getAddr2());
        }
        sb.append("\n");

        if(content.getAddr1() != null){
            sb.append("[설명] ").append(content.getOverview()).append("\n");
        }

        PetTourInfo pet = content.getPetTourInfo();
        if (pet != null) {
            sb.append("[반려동물 동반 정보]").append("\n");
            if (pet.getAcmpyTypeCd() != null)
                sb.append("- 동반 유형: ").append(pet.getAcmpyTypeCd()).append("\n");
            if (pet.getEtcAcmpyInfo() != null)
                sb.append("- 동반 가능 반려 동물 정보 : ").append(pet.getEtcAcmpyInfo()).append("\n");
            if (pet.getAcmpyPsblCpam() != null)
                sb.append("- 추가 정보 : ").append(pet.getAcmpyPsblCpam()).append("\n");
            if (pet.getAcmpyNeedMtr() != null)
                sb.append("- 준비물(목줄, 기타) : ").append(pet.getAcmpyNeedMtr()).append("\n");
        }

        return sb.toString();
    }

}
