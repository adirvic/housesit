package br.com.rodrigues.housesit.controller;

import br.com.rodrigues.housesit.domain.model.ExpenseCategory;
import br.com.rodrigues.housesit.dto.CategoryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories() {
        List<CategoryDto> list = Arrays.stream(ExpenseCategory.values())
                .map(CategoryDto::fromEnum)
                .toList();
        return ResponseEntity.ok(list);
    }
}
