package io.github.petty.llm.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AreaCode {
    SEOUL(1, "서울특별시"),
    INCHEON(2, "인천광역시"),
    DAEJEON(3, "대전광역시"),
    DAEGU(4, "대구광역시"),
    GWANGJU(5, "광주광역시"),
    BUSAN(6, "부산광역시"),
    ULSAN(7, "울산광역시"),
    SEJONG(8, "세종특별자치시"),
    GYEONGGI(31, "경기도"),
    GANGWON(32, "강원특별자치도"),
    CHUNGBUK(33, "충청북도"),
    CHUNGNAM(34, "충청남도"),
    GYEONGBUK(35, "경상북도"),
    GYEONGNAM(36, "경상남도"),
    JEONBUK(37, "전북특별자치도"),
    JEONNAM(38, "전라남도"),
    JEJU(39, "제주특별자치도"),
    ETC(0, "기타");

    private final int code;
    private final String name;

    public static AreaCode fromCode(Integer code) {
        if (code == null)
            return ETC;
        for (AreaCode a : values()) {
            if (a.code == code)
                return a;
        }
        return ETC;
    }

    public static AreaCode fromName(String name) {
        for (AreaCode a : values()) {
            if (name != null && name.contains(a.name))
                return a;
        }
        return ETC;
    }
}
