package com.kiet.library.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiet.library.model.Book;

@RestController
@RequestMapping("/books")
public class BookController {

    private final List<Book> books = new ArrayList<>();

    public BookController() {
        books.add(new Book(1, "Clean Code", "Robert C. Martin",
                "Programming", 5));
        books.add(new Book(2, "Effective Java", "Joshua Bloch",
                "Programming", 3));
        books.add(new Book(3, "The Hobbit", "J.R.R. Tolkien",
                "Fiction", 4));
    }

    private int findBookIndex(int id) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return books;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable int id) {
        int index = findBookIndex(id);

        if (index == -1) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Book not found"));
        }

        return ResponseEntity.ok(books.get(index));
    }

    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        if (findBookIndex(book.getId()) != -1) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", "Book ID already exists"));
        }

        books.add(book);
        return ResponseEntity.status(201).body(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(
            @PathVariable int id,
            @RequestBody Book updatedBook) {

        int index = findBookIndex(id);

        if (index == -1) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Book not found"));
        }

        updatedBook.setId(id);
        books.set(index, updatedBook);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable int id) {
        int index = findBookIndex(id);

        if (index == -1) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Book not found"));
        }

        books.remove(index);
        return ResponseEntity.ok(
                Map.of("message", "Book deleted successfully"));
    }
}