package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Tüm kategorileri getir
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ID'ye göre kategori getir
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // Ana kategorileri getir (parent_id null olanlar)
    public List<Category> getMainCategories() {
        return categoryRepository.findByParentIsNull();
    }

    // Alt kategorileri getir
    public List<Category> getSubCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    // İsme göre kategori bul
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name);
    }

    // Yeni kategori ekle (admin)
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Kategori güncelle (admin)
    public Category updateCategory(Long id, Category updatedCategory) {
        updatedCategory.setId(id);
        return categoryRepository.save(updatedCategory);
    }

    // Kategori sil (admin)
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    
}