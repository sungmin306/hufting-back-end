package com.likelion.hufsting.domain.matching.dto;

import com.likelion.hufsting.domain.matching.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MatchingPostData {
    private String title;
    private Gender gender;
    private int desiredNumPeople;
    private String authorName;
    private LocalDateTime createdAt;
}
