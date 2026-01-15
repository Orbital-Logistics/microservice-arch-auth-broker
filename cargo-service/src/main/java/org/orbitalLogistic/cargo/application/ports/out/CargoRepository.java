package org.orbitalLogistic.cargo.application.ports.out;

import org.orbitalLogistic.cargo.domain.model.Cargo;

import java.util.List;
import java.util.Optional;

public interface CargoRepository {
    Cargo save(Cargo cargo);
    Optional<Cargo> findById(Long id);
    List<Cargo> findAll();
    List<Cargo> findByName(String name);
    boolean existsByName(String name);
    boolean existsById(Long id);
    void deleteById(Long id);
    List<Cargo> findWithFilters(String name, String cargoType, String hazardLevel, int limit, int offset);
    long countWithFilters(String name, String cargoType, String hazardLevel);
}
