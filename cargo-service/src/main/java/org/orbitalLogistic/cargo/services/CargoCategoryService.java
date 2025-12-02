package org.orbitalLogistic.cargo.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.cargo.entities.CargoCategory;
import org.orbitalLogistic.cargo.exceptions.CargoCategoryNotFoundException;
import org.orbitalLogistic.cargo.mappers.CargoCategoryMapper;
import org.orbitalLogistic.cargo.repositories.CargoCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CargoCategoryService {

    private final CargoCategoryRepository cargoCategoryRepository;
    private final CargoCategoryMapper cargoCategoryMapper;

    public List<CargoCategoryResponseDTO> getAllCategories() {
        List<CargoCategory> categories = (List<CargoCategory>) cargoCategoryRepository.findAll();
        return categories.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public CargoCategoryResponseDTO getCategoryById(Long id) {
        CargoCategory category = getEntityById(id);
        return toResponseDTO(category);
    }

    public CargoCategoryResponseDTO createCategory(CargoCategoryRequestDTO request) {
        CargoCategory category = cargoCategoryMapper.toEntity(request);
        CargoCategory saved = cargoCategoryRepository.save(category);
        return toResponseDTO(saved);
    }

    public List<CargoCategoryResponseDTO> getCategoryTree() {
        List<CargoCategory> rootCategories = cargoCategoryRepository.findByParentCategoryIdIsNull();
        return rootCategories.stream()
                .map(this::toTreeResponseDTO)
                .toList();
    }

    public CargoCategory getEntityById(Long id) {
        return cargoCategoryRepository.findById(id)
                .orElseThrow(() -> new CargoCategoryNotFoundException("Cargo category not found with id: " + id));
    }

    private CargoCategoryResponseDTO toResponseDTO(CargoCategory category) {
        String parentCategoryName = null;
        if (category.getParentCategoryId() != null) {
            CargoCategory parent = cargoCategoryRepository.findById(category.getParentCategoryId()).orElse(null);
            if (parent != null) {
                parentCategoryName = parent.getName();
            }
        }
        return cargoCategoryMapper.toResponseDTO(category, parentCategoryName, new ArrayList<>(), 0);
    }

    private CargoCategoryResponseDTO toTreeResponseDTO(CargoCategory category) {
        List<CargoCategory> children = cargoCategoryRepository.findByParentCategoryId(category.getId());
        List<CargoCategoryResponseDTO> childrenDTOs = children.stream()
                .map(this::toTreeResponseDTO)
                .toList();

        String parentCategoryName = null;
        if (category.getParentCategoryId() != null) {
            CargoCategory parent = cargoCategoryRepository.findById(category.getParentCategoryId()).orElse(null);
            if (parent != null) {
                parentCategoryName = parent.getName();
            }
        }

        return cargoCategoryMapper.toResponseDTO(category, parentCategoryName, childrenDTOs, 0);
    }
}

