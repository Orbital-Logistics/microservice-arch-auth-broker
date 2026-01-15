package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoCategoryRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoCategoryResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CargoCategoryRestMapper {
    
    private final CargoCategoryRepository cargoCategoryRepository;
    
    public CargoCategoryResponse toResponse(CargoCategory category) {
        if (category == null) {
            return null;
        }
        
        String parentName = null;
        if (category.getParentCategoryId() != null) {
            parentName = cargoCategoryRepository.findById(category.getParentCategoryId())
                    .map(CargoCategory::getName)
                    .orElse(null);
        }
        
        return CargoCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentCategoryId(category.getParentCategoryId())
                .parentCategoryName(parentName)
                .description(category.getDescription())
                .children(new ArrayList<>())
                .level(0)
                .build();
    }
    
    public CargoCategoryResponse toResponseWithChildren(CargoCategory category) {
        return toResponseWithChildren(category, 0);
    }
    
    private CargoCategoryResponse toResponseWithChildren(CargoCategory category, int level) {
        if (category == null) {
            return null;
        }
        
        String parentName = null;
        if (category.getParentCategoryId() != null) {
            parentName = cargoCategoryRepository.findById(category.getParentCategoryId())
                    .map(CargoCategory::getName)
                    .orElse(null);
        }
        
        List<CargoCategory> childCategories = cargoCategoryRepository.findByParentCategoryId(category.getId());
        List<CargoCategoryResponse> children = childCategories.stream()
                .map(child -> toResponseWithChildren(child, level + 1))
                .collect(Collectors.toList());
        
        return CargoCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentCategoryId(category.getParentCategoryId())
                .parentCategoryName(parentName)
                .description(category.getDescription())
                .children(children)
                .level(level)
                .build();
    }
    
    public CargoCategory toDomain(CreateCargoCategoryRequest request) {
        if (request == null) {
            return null;
        }
        
        return CargoCategory.builder()
                .name(request.getName())
                .parentCategoryId(request.getParentCategoryId())
                .description(request.getDescription())
                .build();
    }
}
