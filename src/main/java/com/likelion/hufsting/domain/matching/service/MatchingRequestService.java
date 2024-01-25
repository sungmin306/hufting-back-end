package com.likelion.hufsting.domain.matching.service;

import com.likelion.hufsting.domain.matching.domain.*;
import com.likelion.hufsting.domain.matching.dto.matchingrequest.*;
import com.likelion.hufsting.domain.matching.repository.MatchingPostRepository;
import com.likelion.hufsting.domain.matching.repository.MatchingRequestRepository;
import com.likelion.hufsting.domain.matching.repository.query.MatchingRequestQueryRepository;
import com.likelion.hufsting.domain.profile.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingRequestService {
    private final MatchingRequestRepository matchingRequestRepository;
    private final MatchingPostRepository matchingPostRepository;
    private final MatchingRequestQueryRepository matchingRequestQueryRepository;

    // 매칭 신청 생성
    @Transactional
    public CreateMatchingReqResponse createMatchingRequests(CreateMatchingReqData dto){
        Member representative = new Member(); // 임시 대표 신청자
        MatchingPost matchingPost = matchingPostRepository.findById(dto.getMatchingPostId())
                .orElseThrow(() -> new IllegalArgumentException("Not Found: " + dto.getMatchingPostId()));
        MatchingRequest newMatchingRequest = MatchingRequest.builder()
                .matchingPost(matchingPost)
                .representative(representative)
                .matchingAcceptance(MatchingAcceptance.WAITING)
                .build();
        // Member 리스트 가져오기
        List<MatchingParticipant> matchingParticipants = createMatchingParticipantsById(newMatchingRequest, dto.getParticipantIds());
        newMatchingRequest.addParticipant(matchingParticipants);
        matchingRequestRepository.save(newMatchingRequest);

        // return value generation
        Long createdMatchingRequestId = newMatchingRequest.getId();
        List<Long> createdMatchingRequestParticipants = dto.getParticipantIds();
        return new CreateMatchingReqResponse(createdMatchingRequestId, createdMatchingRequestParticipants);
    }

    // 매칭 신청 취소
    @Transactional
    public void removeMatchingRequest(Long matchingRequestId){
        MatchingRequest matchingRequest = matchingRequestRepository.findById(matchingRequestId)
                        .orElseThrow(() -> new IllegalArgumentException("Not Found: " + matchingRequestId));
        matchingRequestRepository.delete(matchingRequest);
    }

    // 매칭 신청 수정
    @Transactional
    public UpdateMatchingReqResponse updateMatchingRequest(Long matchingRequestId, UpdateMatchingReqData dto){
        // matchingRequest 조회
        MatchingRequest matchingRequest = matchingRequestRepository.findById(matchingRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Not Found: " + matchingRequestId));
        // 매칭 신청 수정
        matchingRequest.updateParticipant(
                createMatchingParticipantsById(matchingRequest, dto.getParticipantIds())
        );

        return new UpdateMatchingReqResponse(matchingRequestId, dto.getParticipantIds());
    }

    public FindMyMatchingReqResponse getMyMatchingRequest(){
        Member participant = new Member(); // 임시 인증 유저
        List<MatchingRequest> findMyMatchingRequests = matchingRequestQueryRepository.findByParticipant(participant);
        List<FindMyMatchingReqData> convertedMyMatchingRequests = findMyMatchingRequests.stream().map(
                findMyMatchingRequest ->
        ).toList();
    }

    // 사용자 정의 메서드
    // create MatchingRequests List By MemberId
    private List<MatchingParticipant> createMatchingParticipantsById(MatchingRequest matchingRequest,List<Long> participantIds){
        List<MatchingParticipant> matchingParticipants = new ArrayList<>();
        for(Long participantId : participantIds){
            Member findParticipant = new Member(); // 임시 사용자 생성
            matchingParticipants.add(new MatchingParticipant(matchingRequest, findParticipant));
        }
        return matchingParticipants;
    }
}
