package io.github.petty.llm.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentType {
    TOUR(12, "관광지"),
    CULTURE(14, "문화시설"),
    FESTIVAL(15, "축제, 공연, 행사"),
    SPORTS(28, "레포츠, 레저"),
    STAY(32, "숙박, 숙소, 호텔, 모텔, 펜션"),
    SHOPPING(38, "쇼핑"),
    FOOD(39, "음식점, 식당"),
    ETC(0, "기타");

    private final int code;
    private final String name;

    public static ContentType fromCode(Integer code) {
        if (code == null)
            return ETC;
        for (ContentType t : values()) {
            if (t.code == code)
                return t;
        }
        return ETC;
    }
}