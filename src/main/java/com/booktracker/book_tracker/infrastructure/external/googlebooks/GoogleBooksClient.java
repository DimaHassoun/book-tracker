package com.booktracker.book_tracker.infrastructure.external.googlebooks;

import com.booktracker.book_tracker.domain.model.Book;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Component
public class GoogleBooksClient {

    private final WebClient webClient;
    private final String apiKey;

    public GoogleBooksClient(
            WebClient.Builder builder,
            @Value("${app.google-books.api-key}") String apiKey
    ) {
        this.webClient = builder
                .baseUrl("https://www.googleapis.com/books/v1")
                .build();

        this.apiKey = apiKey;
    }

    public List<Book> search(String query, int maxResults) {


    JsonNode response;
        try {
            response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
							.path("/volumes")
                            .queryParam("q", query)
                            .queryParam("maxResults", maxResults)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            throw new com.booktracker.book_tracker.domain.exception.ExternalServiceException(
                "Google Books API failed: " + e.getStatusCode()
            );
        }

        List<Book> results = new ArrayList<>();
        if (response == null || !response.has("items")) {
            return results;
        }

        for (JsonNode item : response.get("items")) {
            JsonNode info = item.get("volumeInfo");
            if (info == null) continue;

            Book book = new Book();
            book.setExternalSource(ExternalSource.GOOGLE_BOOKS);
            book.setExternalId(item.path("id").asText());
            book.setTitle(info.path("title").asText(null));
            book.setSubtitle(info.path("subtitle").asText(null));
            book.setDescription(info.path("description").asText(null));
            book.setPublisher(info.path("publisher").asText(null));

            List<String> authors = new ArrayList<>();
            if (info.has("authors")) {
                info.get("authors").forEach(a -> authors.add(a.asText()));
            }
            book.setAuthors(authors);

            List<String> genres = new ArrayList<>();
            if (info.has("categories")) {
                info.get("categories").forEach(c -> genres.add(c.asText()));
            }
            book.setGenres(genres);

            if (info.has("pageCount")) {
                book.setPageCount(info.get("pageCount").asInt());
            }
            if (info.has("publishedDate")) {
                String raw = info.get("publishedDate").asText();
                book.setPublishedDate(parsePublishedDate(raw));
            }
            if (info.has("imageLinks") && info.get("imageLinks").has("thumbnail")) {
                book.setCoverImageUrl(info.get("imageLinks").get("thumbnail").asText());
            }
            book.setLanguage(info.path("language").asText("en"));

            if (info.has("industryIdentifiers")) {
                for (JsonNode idNode : info.get("industryIdentifiers")) {
                    String type = idNode.path("type").asText();
                    String identifier = idNode.path("identifier").asText();
                    if ("ISBN_13".equals(type)) book.setIsbn13(identifier);
                    if ("ISBN_10".equals(type)) book.setIsbn10(identifier);
                }
            }

            results.add(book);
        }
        return results;
    }

    private LocalDate parsePublishedDate(String raw) {
        try {
            if (raw.length() == 4) return LocalDate.of(Integer.parseInt(raw), 1, 1);
            if (raw.length() == 7) return LocalDate.parse(raw + "-01");
            return LocalDate.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }
}