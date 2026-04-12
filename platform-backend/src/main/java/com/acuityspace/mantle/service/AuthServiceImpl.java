package com.acuityspace.mantle.service;

import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.enums.UserRole;
import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.OrgMembership;
import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.model.SubOrgMembership;
import com.acuityspace.mantle.domain.model.User;
import com.acuityspace.mantle.domain.repository.OrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.SubOrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import com.acuityspace.mantle.domain.repository.UserRepository;
import com.acuityspace.mantle.exception.EmailAlreadyExistsException;
import com.acuityspace.mantle.exception.InvalidCredentialsException;
import com.acuityspace.mantle.exception.ResourceNotFoundException;
import com.acuityspace.mantle.security.JwtService;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.LoginRequest;
import com.acuityspace.mantle.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OrgRepository orgRepository;
    private final OrgMembershipRepository orgMembershipRepository;
    private final SubOrgRepository subOrgRepository;
    private final SubOrgMembershipRepository subOrgMembershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           OrgRepository orgRepository,
                           OrgMembershipRepository orgMembershipRepository,
                           SubOrgRepository subOrgRepository,
                           SubOrgMembershipRepository subOrgMembershipRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.orgRepository = orgRepository;
        this.orgMembershipRepository = orgMembershipRepository;
        this.subOrgRepository = subOrgRepository;
        this.subOrgMembershipRepository = subOrgMembershipRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.email());
        }

        String encoded = passwordEncoder.encode(request.password());
        User user = userRepository.save(new User(request.email(), encoded, request.name()));

        Org org = orgRepository.save(new Org(request.orgName(), request.orgType()));

        orgMembershipRepository.save(new OrgMembership(user, org));

        SubOrg defaultSubOrg;
        SubOrgMembership defaultMembership;

        if (request.orgType() == OrgType.INDIVIDUAL) {
            defaultSubOrg = subOrgRepository.save(new SubOrg(org, "Default", true, false));
            defaultMembership = subOrgMembershipRepository.save(
                    new SubOrgMembership(user, defaultSubOrg, UserRole.SUPER_ADMIN));
        } else {
            SubOrg globalSubOrg = subOrgRepository.save(new SubOrg(org, "Global", false, true));
            defaultSubOrg = subOrgRepository.save(new SubOrg(org, "Default", true, false));

            subOrgMembershipRepository.save(new SubOrgMembership(user, globalSubOrg, UserRole.SUPER_ADMIN));
            defaultMembership = subOrgMembershipRepository.save(
                    new SubOrgMembership(user, defaultSubOrg, UserRole.SUPER_ADMIN));
        }

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                org.getId(),
                org.getName(),
                org.getType(),
                defaultSubOrg.getId(),
                defaultMembership.getRole()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        OrgMembership orgMembership = orgMembershipRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User has no org membership"));

        Org org = orgMembership.getOrg();

        SubOrg defaultSubOrg = subOrgRepository.findByOrgAndIsDefaultTrue(org)
                .orElseThrow(() -> new ResourceNotFoundException("Default SubOrg not found"));

        SubOrgMembership subOrgMembership = subOrgMembershipRepository.findByUserAndSubOrg(user, defaultSubOrg)
                .orElseThrow(() -> new ResourceNotFoundException("SubOrg membership not found"));

        String token = jwtService.generateToken(user.getEmail(), user.getId(), org.getId());

        ResponseCookie cookie = ResponseCookie.from("mantle-token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(86400)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                org.getId(),
                org.getName(),
                org.getType(),
                defaultSubOrg.getId(),
                subOrgMembership.getRole()
        );
    }

    @Override
    public void logout(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from("mantle-token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
    }
}
