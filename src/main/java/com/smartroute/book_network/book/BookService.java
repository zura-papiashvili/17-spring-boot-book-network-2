package com.smartroute.book_network.book;

import org.springframework.data.domain.Pageable;

import static com.smartroute.book_network.book.BookSpecification.withOwnerId;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartroute.book_network.aws.StorageService;
import com.smartroute.book_network.common.PageResponse;
import com.smartroute.book_network.exception.OperationNotPermittedException;
import com.smartroute.book_network.file.FileStorageService;
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
    private final StorageService storageService;

    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Book existingBook = request.id() != null ? bookRepository.findById(request.id()).orElse(null) : null;
        Book book = bookMapper.toBook(request);
        book.setOwner(user);
        if (existingBook != null && book.getBookCover() == null) {
            book.setBookCover(existingBook.getBookCover());

        }

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

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        User user = ((User) connectedUser.getPrincipal());

        if (!book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You are not the owner of this book");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return book.getId();
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        User user = ((User) connectedUser.getPrincipal());

        if (!book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You are not the owner of this book");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return book.getId();
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("This book is not available for borrowing");
        }
        User user = ((User) connectedUser.getPrincipal());

        if (book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You are the owner of this book");
        }
        final boolean isBorrowed = bookTransactionHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());
        if (isBorrowed) {
            throw new OperationNotPermittedException("You have already borrowed this book");
        }
        BookTransactionHistory history = BookTransactionHistory.builder()
                .book(book)
                .user(user)
                .returnApproved(false)
                .returned(false)
                .build();

        return bookTransactionHistoryRepository.save(history).getId();
    }

    public Integer returnBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("This book is not available for borrowing");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You are the owner of this book");
        }
        BookTransactionHistory history = bookTransactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You have not borrowed this book"));
        history.setReturned(true);

        return bookTransactionHistoryRepository.save(history).getId();

    }

    public Integer approveReturn(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        User user = ((User) connectedUser.getPrincipal());
        if (!book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You are the owner of this book");
        }
        BookTransactionHistory history = bookTransactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet"));
        history.setReturnApproved(true);

        return bookTransactionHistoryRepository.save(history).getId();
    }

    public void uploadBookCover(MultipartFile file, Authentication connectedUser, Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        User user = ((User) connectedUser.getPrincipal());
        if (!book.getOwner().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You are not the owner of this book");
        }
        String bookCover = storageService.uploadFile(file);
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }

}
