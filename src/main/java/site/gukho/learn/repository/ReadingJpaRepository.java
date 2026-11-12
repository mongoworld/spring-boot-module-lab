package site.gukho.learn.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import site.gukho.learn.model.ReadingEntity;

public interface ReadingJpaRepository extends JpaRepository<ReadingEntity, Long> {

    List<ReadingEntity> findByHumidityGreaterThanEqualOrderById(double humidity);
}
