package com.odos.odos_server_v2.domain.banner.repository;

import com.odos.odos_server_v2.domain.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {}
