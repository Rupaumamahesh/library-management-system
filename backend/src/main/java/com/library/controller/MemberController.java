package com.library.controller;

import com.library.dto.MemberDTO;
import com.library.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for member management.
 * Endpoint base: /api/members
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberDTO>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @PostMapping
    public ResponseEntity<MemberDTO> registerMember(@Valid @RequestBody MemberDTO memberDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.registerMember(memberDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDTO> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberDTO> updateMember(@PathVariable Long id, @Valid @RequestBody MemberDTO memberDTO) {
        return ResponseEntity.ok(memberService.updateMember(id, memberDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<?>> getMemberHistory(@PathVariable Long id) {
        // Will be implemented after TransactionController is set up
        return ResponseEntity.ok(List.of());
    }
}
