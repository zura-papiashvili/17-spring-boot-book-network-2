package com.smartroute.book_network.feedback;

import org.springframework.stereotype.Service;

import com.smartroute.book_network.book.Book;

@Service
public class FeedbackMapper {

    public Feedback toFeedBack(FeedbackRequest request) {
        return Feedback.builder()
                .note(request.note())
                .comment(request.comment())
                .book(Book.builder()
                        .id(request.bookId())
                        .build())
                .build();

    }

    public FeedbackResponse toFeedbackResponse(Feedback feedback, Integer id) {
        return FeedbackResponse.builder()
                .note(feedback.getNote())
                .comment(feedback.getComment())
                .ownFeedback(feedback.getBook().getOwner().getId().equals(id))
                .build();
    }
}
