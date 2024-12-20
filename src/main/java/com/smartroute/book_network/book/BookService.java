package com.smartroute.book_network.book;

import org.springframework.data.domain.Pageable;

import static com.smartroute.book_network.book.BookSpecification.withOwnerId;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.smartroute.book_network.common.PageResponse;
import com.smartroute.book_network.history.BookTransactionHistory;
import com.smartroute.book_network.history.BookTransactionHistoryRepository;
import com.smartroute.book_network.user.User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;

    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request);

        book.setOwner(user);

        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
    }

    public PageResponse<BookResponse> findAllBooks(Integer page, Integer size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, user.getId());
        List<BookResponse> bookResponses = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast());
    }

    public PageResponse<BookResponse> findAllBooksByOwner(Integer page, Integer size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<Book> books = bookRepository.findAll(withOwnerId(user.getId()), pageable);
        List<BookResponse> bookResponses = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponses,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast());
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(Integer page, Integer size,
            Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllBorrowedBooks(pageable,
                user.getId());
        List<BorrowedBookResponse> borrowedBookResponses = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                borrowedBookResponses,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast());

    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(Integer page, Integer size,
            Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<BookTransactionHistory> allReturnedBooks = bookTransactionHistoryRepository.findAllReturnedBooks(pageable,
                user.getId());
        List<BorrowedBookResponse> returnedBookResponses = allReturnedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                returnedBookResponses,
                allReturnedBooks.getNumber(),
                allReturnedBooks.getSize(),
                allReturnedBooks.getTotalElements(),
                allReturnedBooks.getTotalPages(),
                allReturnedBooks.isFirst(),
                allReturnedBooks.isLast());

    }
}
