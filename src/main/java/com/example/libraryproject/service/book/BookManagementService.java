package com.example.libraryproject.service.book;

import com.example.libraryproject.mapper.book.BookMapper;
import com.example.libraryproject.mapper.book.QuantityBookMapper;
import com.example.libraryproject.model.dao.Book;
import com.example.libraryproject.model.dto.request.filter.BookRequestFilter;
import com.example.libraryproject.model.dto.response.admin.QuantityBookResponseAdmin;
import com.example.libraryproject.model.dto.response.user.BookResponseUser;
import com.example.libraryproject.model.enums.book.Status;
import com.example.libraryproject.repository.book.BookRepository;
import com.example.libraryproject.repository.book.QuantityBookRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

import static com.example.libraryproject.model.enums.book.Status.ACTIVE;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BookManagementService {
    final BookMapper bookMapper;
    final BookRepository bookRepository;
    final EntityManager em;
    final QuantityBookRepository quantityBookRepository;
    final QuantityBookMapper quantityBookMapper;

    public List<BookResponseUser> searchBooks(String searchWord) {
        return bookMapper.toResponseUser(bookRepository.searchBook(searchWord));
    }

    public List<BookResponseUser> filterBooks(BookRequestFilter bookRequest) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> query = cb.createQuery(Book.class);
        List<Predicate> predicates = new ArrayList<>();
        Root<Book> root = query.from(Book.class);

        predicates.add(cb.like(root.get("status"),"%" + ACTIVE + "%"));

        if (bookRequest.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), bookRequest.getMinPrice()));
        }
        if (bookRequest.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), bookRequest.getMaxPrice()));
        }
        if (bookRequest.getGenre() != null) {
            predicates.add(cb.like(root.get("genre"), "%" + bookRequest.getGenre() + "%"));
        }
        if (bookRequest.getLanguage() != null) {
            predicates.add(cb.like(root.get("language"), "%" + bookRequest.getLanguage() + "%"));
        }
        if (bookRequest.getMinPages() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("pages"), bookRequest.getMinPages()));
        }
        if (bookRequest.getMaxPages() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("pages"), bookRequest.getMaxPages()));
        }


        query.where(
                cb.and(predicates.toArray(new Predicate[0]))
        );
        TypedQuery<Book> typedQuery = em.createQuery(query);
        return bookMapper.toResponseUser(typedQuery.getResultList());
    }

    public List<QuantityBookResponseAdmin> getBookInventory() {
        return quantityBookMapper.toResponse(quantityBookRepository.findAll());
    }


}
