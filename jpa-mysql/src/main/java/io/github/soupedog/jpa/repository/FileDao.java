package io.github.soupedog.jpa.repository;


import io.github.soupedog.jpa.domain.po.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Xavier
 * @date 2024/8/25
 * @since 1.0
 */
@Repository
public interface FileDao extends JpaRepository<FileEntity, Integer> {
}
