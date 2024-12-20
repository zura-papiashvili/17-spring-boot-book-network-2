package com.smartroute.book_network.feedback;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;

import com.smartroute.book_network.book.Book;
import com.smartroute.book_network.book.BookRepository;

import com.smartroute.book_network.common.PageResponse;
import com.smartroute.book_network.user.User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final BookRepository bookRepository;
    private final FeedbackMapper feedbackMapper;
    private final FeedbackRepository feedbackRepository;

    public Integer save(FeedbackRequest request, Authentication connectedUser) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        if (book.isArchived() || !book.isShareable()) {
            throw new IllegalArgumentException("Book is not available for feedback");
        }

        User user = ((User) connectedUser.getPrincipal());

        if (book.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can't give feedback to your own book");
        }

        Feedback feedback = feedbackMapper.toFeedBack(request);

        return feedbackRepository.save(feedback).getId();

    }

    public PageResponse<FeedbackResponse> findAllFeedbacksByBook(Integer bookId, Integer page, Integer size,
            Authentication connectedUser) {
        Pageable pageable = PageRequest.of(page, size);
        User user = ((User) connectedUser.getPrincipal());

        Page<Feedback> feedbacks = feedbackRepository.findAllByBookId(bookId, pageable);

        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(feedback -> feedbackMapper.toFeedbackResponse(feedback, user.getId()))
                .toList();

        return new PageResponse<>(
                feedbackResponses,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast());
    }

}
