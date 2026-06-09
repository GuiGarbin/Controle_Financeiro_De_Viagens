package org.example.controller;

import org.example.dto.request.response.ApiResponse;
import org.example.model.Category;
import org.example.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController { // Controlador para gerenciar categorias de despesas, com endpoints para criar, listar e remover categorias

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // GET /api/categories
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.findAll()));
    }

    // GET /api/categories/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.findById(id)));
    }

    // POST /api/categories
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> create(@RequestBody Map<String, String> body) {
        Category category = categoryService.create(
            body.get("name"),
            body.get("icon"),
            body.get("color")
        );
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    // DELETE /api/categories/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}