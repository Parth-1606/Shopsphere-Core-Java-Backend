package com.parth.shopsphere.auth.repository;

import com.parth.shopsphere.auth.entity.RefreshToken;
import com.parth.shopsphere.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUserAndIsRevokedFalse(User user);
}
