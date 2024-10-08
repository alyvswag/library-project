package com.example.libraryproject.controller.v1.author;


import com.example.libraryproject.model.dto.response.payload.BookResponse;
import com.example.libraryproject.model.dto.response.base.BaseResponse;
import com.example.libraryproject.service.author.AuthorManagementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/author-management")
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorManagementController {
    final AuthorManagementService authorManagementService;

    @DeleteMapping("/remove-book-from-author")
    public BaseResponse<Void> removeBookFromAuthor(@RequestParam("bookId") Long bookId) {
        authorManagementService.removeBookFromAuthor(bookId);
        return BaseResponse.success();
    }

    @GetMapping("/get-books-by-author")
    public BaseResponse<List<BookResponse>> getBooksByAuthor(@RequestParam("authorId") Long authorId) {
        return BaseResponse.success(authorManagementService.getBooksByAuthorId(authorId));
    }
}
