package com.acuityspace.mantle.service;

import com.acuityspace.mantle.domain.enums.InviteStatus;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.enums.UserRole;
import com.acuityspace.mantle.domain.model.Invite;
import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.OrgMembership;
import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.model.SubOrgMembership;
import com.acuityspace.mantle.domain.model.User;
import com.acuityspace.mantle.domain.repository.InviteRepository;
import com.acuityspace.mantle.domain.repository.OrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.SubOrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import com.acuityspace.mantle.domain.repository.UserRepository;
import com.acuityspace.mantle.exception.AccessDeniedException;
import com.acuityspace.mantle.exception.EmailAlreadyExistsException;
import com.acuityspace.mantle.exception.IllegalOperationException;
import com.acuityspace.mantle.exception.InvalidInviteException;
import com.acuityspace.mantle.exception.MemberLimitExceededException;
import com.acuityspace.mantle.exception.ResourceNotFoundException;
import com.acuityspace.mantle.security.JwtService;
import com.acuityspace.mantle.web.dto.AcceptInviteRequest;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.InviteDetailResponse;
import com.acuityspace.mantle.web.dto.InviteRequest;
import com.acuityspace.mantle.web.dto.InviteResponse;
import com.acuityspace.mantle.web.dto.MemberResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InviteServiceImpl implements InviteService {

    private final InviteRepository inviteRepository;
    private final OrgRepository orgRepository;
    private final SubOrgRepository subOrgRepository;
    private final SubOrgMembershipRepository subOrgMembershipRepository;
    private final OrgMembershipRepository orgMembershipRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleResolutionService roleResolutionService;

    public InviteServiceImpl(InviteRepository inviteRepository,
                              OrgRepository orgRepository,
                              SubOrgRepository subOrgRepository,
                              SubOrgMembershipRepository subOrgMembershipRepository,
                              OrgMembershipRepository orgMembershipRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              JwtService jwtService,
                              RoleResolutionService roleResolutionService) {
        this.inviteRepository = inviteRepository;
        this.orgRepository = orgRepository;
        this.subOrgRepository = subOrgRepository;
        this.subOrgMembershipRepository = subOrgMembershipRepository;
        this.orgMembershipRepository = orgMembershipRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleResolutionService = roleResolutionService;
    }

    @Override
    @Transactional
    public InviteResponse sendInvite(UUID orgId, InviteRequest request, UUID requestingUserId) {
        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Org not found: " + orgId));

        SubOrg defaultSubOrg = subOrgRepository.findByOrgAndIsDefaultTrue(org)
                .orElseThrow(() -> new ResourceNotFoundException("Default SubOrg not found"));

        roleResolutionService.requireAdminOrAbove(requestingUserId, defaultSubOrg.getId());

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email is already registered: " + request.email());
        }

        inviteRepository.findByOrgAndEmail(org, request.email())
                .filter(i -> i.getStatus() == InviteStatus.PENDING)
                .ifPresent(i -> {
                    throw new IllegalOperationException("A pending invite already exists for: " + request.email());
                });

        if (org.getType() == OrgType.INDIVIDUAL) {
            long memberCount = orgMembershipRepository.countByOrg(org);
            long pendingCount = inviteRepository.countByOrgAndStatus(org, InviteStatus.PENDING);
            if (memberCount + pendingCount >= 4) {
                throw new MemberLimitExceededException(
                        "INDIVIDUAL org has reached the maximum of 4 members (existing + pending)");
            }
        }

        User inviter = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requestingUserId));

        Invite invite = inviteRepository.save(new Invite(org, inviter, request.email()));

        return toInviteResponse(invite);
    }

    @Override
    @Transactional
    public AuthResponse acceptInvite(UUID inviteId, AcceptInviteRequest request, HttpServletResponse response) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found: " + inviteId));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new InvalidInviteException("Invite is not in PENDING state: " + invite.getStatus());
        }

        if (!invite.getEmail().equalsIgnoreCase(request.email())) {
            throw new InvalidInviteException("Email does not match the invite");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email is already registered: " + request.email());
        }

        Org org = invite.getOrg();

        String encodedPassword = passwordEncoder.encode(request.password());
        User newUser = userRepository.save(new User(request.email(), encodedPassword, request.name()));

        orgMembershipRepository.save(new OrgMembership(newUser, org));

        SubOrg defaultSubOrg = subOrgRepository.findByOrgAndIsDefaultTrue(org)
                .orElseThrow(() -> new ResourceNotFoundException("Default SubOrg not found"));

        subOrgMembershipRepository.save(new SubOrgMembership(newUser, defaultSubOrg, UserRole.STANDARD));

        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);

        String token = jwtService.generateToken(newUser.getEmail(), newUser.getId(), org.getId());
        ResponseCookie cookie = ResponseCookie.from("mantle-token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(86400)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new AuthResponse(
                newUser.getId(),
                newUser.getEmail(),
                newUser.getName(),
                org.getId(),
                org.getName(),
                org.getType(),
                defaultSubOrg.getId(),
                UserRole.STANDARD);
    }

    @Override
    @Transactional
    public void revokeInvite(UUID inviteId, UUID requestingUserId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found: " + inviteId));

        Org org = invite.getOrg();

        SubOrg defaultSubOrg = subOrgRepository.findByOrgAndIsDefaultTrue(org)
                .orElseThrow(() -> new ResourceNotFoundException("Default SubOrg not found"));

        UserRole requesterRole = roleResolutionService
                .getUserRoleOnSubOrg(requestingUserId, defaultSubOrg.getId())
                .orElseThrow(() -> new AccessDeniedException("User has no membership in the default SubOrg"));

        if (requesterRole != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Only SUPER_ADMIN can revoke an invite");
        }

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new InvalidInviteException("Only PENDING invites can be revoked");
        }

        invite.setStatus(InviteStatus.REVOKED);
        inviteRepository.save(invite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InviteResponse> getInvites(UUID orgId, UUID requestingUserId) {
        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Org not found: " + orgId));

        SubOrg defaultSubOrg = subOrgRepository.findByOrgAndIsDefaultTrue(org)
                .orElseThrow(() -> new ResourceNotFoundException("Default SubOrg not found"));

        roleResolutionService.requireAdminOrAbove(requestingUserId, defaultSubOrg.getId());

        return inviteRepository.findByOrg(org).stream()
                .map(this::toInviteResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(UUID orgId, UUID requestingUserId) {
        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Org not found: " + orgId));

        SubOrg defaultSubOrg = subOrgRepository.findByOrgAndIsDefaultTrue(org)
                .orElseThrow(() -> new ResourceNotFoundException("Default SubOrg not found"));

        roleResolutionService.requireMembership(requestingUserId, defaultSubOrg.getId());

        return orgMembershipRepository.findByOrg(org).stream()
                .map(membership -> {
                    User member = membership.getUser();
                    UserRole role = subOrgMembershipRepository
                            .findByUserAndSubOrg(member, defaultSubOrg)
                            .map(SubOrgMembership::getRole)
                            .orElse(UserRole.STANDARD);
                    return new MemberResponse(
                            member.getId(),
                            member.getName(),
                            member.getEmail(),
                            role,
                            membership.getCreatedAt());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InviteDetailResponse getInviteDetail(UUID inviteId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found: " + inviteId));
        Org org = invite.getOrg();
        return new InviteDetailResponse(
                invite.getId(),
                org.getId(),
                org.getName(),
                invite.getEmail(),
                invite.getStatus());
    }

    private InviteResponse toInviteResponse(Invite invite) {
        return new InviteResponse(
                invite.getId(),
                invite.getOrg().getId(),
                invite.getEmail(),
                invite.getStatus(),
                invite.getInvitedBy().getName(),
                invite.getCreatedAt());
    }
}
