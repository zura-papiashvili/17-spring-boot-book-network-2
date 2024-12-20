package com.smartroute.book_network.book;

import java.beans.Transient;
import java.util.List;

import com.smartroute.book_network.common.BaseEntity;
import com.smartroute.book_network.feedback.Feedback;
import com.smartroute.book_network.history.BookTransactionHistory;
import com.smartroute.book_network.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Book extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String authorName;

    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(length = 2000)
    private String synopsis;

    private String bookCover;

    private boolean archived;

    private boolean shareable;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "book")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "book")
    private List<BookTransactionHistory> bookTransactionHistories;

    @Transient
    public double getRate() {
        if (feedbacks == null || feedbacks.isEmpty()) {
            return 0;
        }
        var rate = this.feedbacks.stream().mapToDouble(Feedback::getNote).average().orElse(0.0);

        double rateRounded = Math.round(rate * 10.0) / 10.0;

        return rateRounded;
    }

}
