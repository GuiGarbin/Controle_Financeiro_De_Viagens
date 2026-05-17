package org.example.service;

import org.example.exception.DuplicateResourceException; // exceção personalizada para indicar que um recurso já existe, usada ao tentar criar uma categoria com um nome já existente
import org.example.exception.ResourceNotFoundException; // exceção personalizada para indicar que um recurso não foi encontrado, usada ao buscar ou deletar uma categoria que não existe
import org.example.model.Category; // modelo de categoria de despesa, com campos como id, nome, ícone, cor e timestamps, usado para representar as categorias no sistema
import org.example.repository.CategoryRepository; // repositório para acessar os dados de categorias, com métodos como findAll(), findById(), save() e deleteById(), usado para persistir as categorias no banco de dados
import org.example.util.IdGenerator; // utilitário para gerar ids únicos para as categorias, usado ao criar uma nova categoria
import org.springframework.stereotype.Service; // anotação para indicar que esta classe é um serviço do Spring, responsável pela lógica de negócios relacionada às categorias de despesas

import java.time.Instant; // importa a classe Instant para usar na criação e atualização de timestamps das categorias
import java.util.List; // importa a classe List para usar no retorno do método findAll() que retorna todas as categorias cadastradas

@Service
public class CategoryService { // Serviço para gerenciar categorias de despesas, permitindo criar, listar e remover categorias

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Retorna todas as categorias
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    // Busca uma categoria pelo id
    public Category findById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + id));
    }

    // Cria uma nova categoria
    public Category create(String name, String icon, String color) {
        // Verifica se já existe uma categoria com esse nome
        boolean exists = categoryRepository.findAll().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));

        if (exists) {
            throw new DuplicateResourceException("Já existe uma categoria com o nome: " + name);
        }

        Category category = new Category();
        category.setId(IdGenerator.forCategory());
        category.setName(name);
        category.setIcon(icon);
        category.setColor(color);
        category.setCreatedAt(Instant.now().toString());
        category.setUpdatedAt(Instant.now().toString());

        return categoryRepository.save(category);
    }

    // Remove uma categoria
    public void delete(String id) {
        findById(id); // lança exceção se não existir
        categoryRepository.deleteById(id);
    }
}