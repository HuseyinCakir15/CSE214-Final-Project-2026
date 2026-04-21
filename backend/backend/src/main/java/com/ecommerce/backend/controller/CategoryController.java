package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CategoryController {

    private final CategoryService categoryService;

    // Tüm kategorileri getir (herkes)
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    // ID'ye göre kategori getir
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Ana kategorileri getir (herkes)
    @GetMapping("/main")
    public List<Category> getMainCategories() {
        return categoryService.getMainCategories();
    }

    // Alt kategorileri getir (herkes)
    @GetMapping("/{parentId}/sub")
    public List<Category> getSubCategories(@PathVariable Long parentId) {
        return categoryService.getSubCategories(parentId);
    }

    // Yeni kategori ekle (sadece admin)
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Category createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

    // Kategori güncelle (sadece admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.updateCategory(id, category);
    }

    // Kategori sil (sadece admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}