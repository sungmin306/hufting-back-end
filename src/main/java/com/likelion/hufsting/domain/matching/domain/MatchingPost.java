package com.likelion.hufsting.domain.matching.domain;

import com.likelion.hufsting.domain.matching.dto.matchingpost.UpdateMatchingPostData;
import com.likelion.hufsting.domain.profile.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "HUFSTING_POSTS")
public class MatchingPost {
    @Id @GeneratedValue
    @Column(name = "POST_ID")
    private Long id; // PK

    private String title; // 제목
    private String content; // 내용

    @Enumerated(EnumType.STRING)
    private Gender gender; // 성별, MALE, FEMALE

    private int desiredNumPeople; // 희망 매칭 인원

    private String openTalkLink; // 오픈톡 링크

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY) // 개발 완료 후 cascade 삭제
    @JoinColumn(name = "AUTHOR_ID")
    private Member author; // 작성자

    @Enumerated(EnumType.STRING)
    private MatchingStatus matchingStatus; // 매칭 상태, WAITING, COMPLETED

    @OneToMany(mappedBy = "matchingPost", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<MatchingHost> matchingHosts = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchingPost that = (MatchingPost) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    protected MatchingPost(){}

    public MatchingPost(String title, String content, Gender gender,
                        int desiredNumPeople, String openTalkLink,
                        Member author, MatchingStatus matchingStatus
    ) {
        this.title = title;
        this.content = content;
        this.gender = gender;
        this.desiredNumPeople = desiredNumPeople;
        this.openTalkLink = openTalkLink;
        this.author = author;
        this.matchingStatus = matchingStatus;
    }

    public void updateMatchingPost(UpdateMatchingPostData dto){
         this.title = dto.getTitle();
         this.content = dto.getContent();
         this.gender = dto.getGender();
         this.desiredNumPeople = dto.getDesiredNumPeople();
         this.openTalkLink = dto.getOpenTalkLink();
    }

    // MatchingHost Add Function
    public void addHost(List<MatchingHost> hosts){
        this.matchingHosts.addAll(hosts);
    }

    // MatchingHost Update Function
    public void updateHost(List<MatchingHost> hosts){
        // add new host
        for(MatchingHost host : hosts){
            if(!matchingHosts.contains(host)){
                matchingHosts.add(host);
            }
        }
        // remove not in hosts
        matchingHosts.removeIf(matchingHost -> !hosts.contains(matchingHost));
    }
}
