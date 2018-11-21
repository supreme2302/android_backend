package notes.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task {
    private String author;
    private String title;
    private String body;

    @JsonCreator
    public Task(
            @JsonProperty("author") String author,
            @JsonProperty("title") String title,
            @JsonProperty("body") String body
    ) {
        this.author = author;
        this.title = title;
        this.body = body;
    }

    public Task() {}

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
