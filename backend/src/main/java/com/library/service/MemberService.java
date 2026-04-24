package com.library.service;

import com.library.dto.MemberDTO;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Member;
import com.library.repository.MemberRepository;
import com.library.repository.TransactionRepository;
import com.library.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for member management.
 * Handles all business logic for members.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Registers a new member.
     *
     * @param memberDTO Member data
     * @return Saved member
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public MemberDTO registerMember(MemberDTO memberDTO) {
        if (memberRepository.existsByEmail(memberDTO.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + memberDTO.getEmail());
        }

        if (memberRepository.existsByMemberId(memberDTO.getMemberId())) {
            throw new IllegalArgumentException("Member ID already exists: " + memberDTO.getMemberId());
        }

        Member member = new Member();
        member.setMemberId(memberDTO.getMemberId());
        member.setName(memberDTO.getName());
        member.setEmail(memberDTO.getEmail());
        member.setPhone(memberDTO.getPhone());
        member.setBorrowedCount(0);

        Member saved = memberRepository.save(member);
        log.info("Member registered: {}", saved.getMemberId());
        return mapToDTO(saved);
    }

    /**
     * Gets all members.
     */
    public List<MemberDTO> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets a single member by ID.
     *
     * @throws ResourceNotFoundException if member not found
     */
    public MemberDTO getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + id));
        return mapToDTO(member);
    }

    /**
     * Updates a member.
     */
    @Transactional
    public MemberDTO updateMember(Long id, MemberDTO memberDTO) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + id));

        member.setName(memberDTO.getName());
        member.setEmail(memberDTO.getEmail());
        member.setPhone(memberDTO.getPhone());

        Member updated = memberRepository.save(member);
        log.info("Member updated: {}", updated.getMemberId());
        return mapToDTO(updated);
    }

    /**
     * Deletes a member (only if no active borrows).
     */
    @Transactional
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + id));

        long activeBorrows = transactionRepository.countByMemberAndStatus(member);
        if (activeBorrows > 0) {
            throw new IllegalArgumentException("Cannot delete member with active borrows");
        }

        memberRepository.deleteById(id);
        log.info("Member deleted: {}", id);
    }

    /**
     * Gets a member by business identifier.
     * Used internally by other services.
     */
    public Member getMemberByIdInternal(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
    }

    /**
     * Updates borrowed count for a member.
     */
    @Transactional
    public void updateBorrowedCount(Member member) {
        long activeBorrows = transactionRepository.countByMemberAndStatus(member);
        member.setBorrowedCount((int) activeBorrows);
        memberRepository.save(member);
    }

    /**
     * DTO mapper.
     */
    private MemberDTO mapToDTO(Member member) {
        return new MemberDTO(
                member.getId(),
                member.getMemberId(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getBorrowedCount()
        );
    }
}
