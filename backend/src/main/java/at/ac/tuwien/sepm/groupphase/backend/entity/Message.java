package at.ac.tuwien.sepm.groupphase.backend.entity;

import com.github.javafaker.Faker;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(nullable = false, length = 10000)
    private String text;

    @Column(nullable = false, length = 2000000000)
    private String image;

    @OneToMany(mappedBy = "news", cascade = CascadeType.REMOVE)
    private List<SeenNews> seenNews;

    public Message(String title, String summary, String text) {
        this.title = title;
        this.summary = summary;
        this.text = text;
    }

    public static Message generateRandomMessage() {
        Faker faker = new Faker();
        Message message = new Message();
        String title = faker.lorem().sentence();
        String summary = faker.lorem().sentence();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 19; i++) {
            text.append(faker.lorem().paragraph());
        }
        LocalDateTime publishedAt = faker.date().past(100, java.util.concurrent.TimeUnit.DAYS).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

        message.setTitle(title);
        message.setSummary(summary);
        message.setText(text.toString());
        message.setPublishedAt(publishedAt);

        return message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<SeenNews> getSeenNews() {
        return seenNews;
    }

    public void setSeenNews(List<SeenNews> seenNews) {
        this.seenNews = seenNews;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message message)) {
            return false;
        }
        return Objects.equals(id, message.id)
            && Objects.equals(publishedAt, message.publishedAt)
            && Objects.equals(title, message.title)
            && Objects.equals(summary, message.summary)
            && Objects.equals(text, message.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publishedAt, title, summary, text);
    }

    @Override
    public String toString() {
        return "Message{"
            + "id=" + id
            + ", publishedAt=" + publishedAt
            + ", title='" + title + '\''
            + ", summary='" + summary + '\''
            + ", text='" + text + '\''
            + '}';
    }

    public static final class MessageBuilder {
        private Long id;
        private LocalDateTime publishedAt;
        private String title;
        private String summary;
        private String text;
        private String image;

        private MessageBuilder() {
        }

        public static MessageBuilder aMessage() {
            return new MessageBuilder();
        }

        public MessageBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public MessageBuilder withPublishedAt(LocalDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public MessageBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public MessageBuilder withSummary(String summary) {
            this.summary = summary;
            return this;
        }

        public MessageBuilder withText(String text) {
            this.text = text;
            return this;
        }

        public MessageBuilder withImage(String image) {
            this.image = image;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.setId(id);
            message.setPublishedAt(publishedAt);
            message.setTitle(title);
            message.setSummary(summary);
            message.setText(text);
            message.setImage(image);
            return message;
        }

    }
}