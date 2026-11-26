package com.reboot.identity.repository;

import com.reboot.identity.model.RefreshSession;
import com.reboot.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {

    Optional<RefreshSession> findByTokenHash(String tokenHash);

    List<RefreshSession> findByUser(User user);

    void deleteByTokenHash(String tokenHash);
}
