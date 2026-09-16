package kz.iitu.springlab.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Value("${app.owner:unknown}")
    private String owner;

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(defaultValue = "world") String name) {
        return new Greeting("Hello, " + name + "!", owner, LocalDateTime.now());
    }

    @GetMapping("/info")
    public Info info() {
        return new Info(owner,
                System.getProperty("java.version"),
                Runtime.getRuntime().availableProcessors());
    }

    public record Greeting(String message, String owner, LocalDateTime timestamp) { }

    public record Info(String owner, String javaVersion, int cpuCores) { }
    @GetMapping("/sum")
    public CalculationResult sum(@RequestParam int a, @RequestParam int b) {
        return new CalculationResult(a, b, a + b, a - b, a * b);
    }

    public record CalculationResult(int a, int b, int sum, int difference, int product) { }
    @GetMapping("/wordcount")
    public WordCountResult wordCount(@RequestParam(defaultValue = "Hello Spring Boot") String text) {
        String trimmed = text.trim();
        int charCount = trimmed.length();

        if (trimmed.isEmpty()) {
            return new WordCountResult(text, 0, 0, "");
        }

        String[] words = trimmed.split("\\s+");
        int wordCount = words.length;

        String longestWord = "";
        for (String word : words) {
            // Очищаем от возможных знаков препинания для честной длины
            String cleanWord = word.replaceAll("[^a-zA-ZА-Яа-я0-9]", "");
            if (cleanWord.length() > longestWord.length()) {
                longestWord = cleanWord;
            }
        }

        return new WordCountResult(text, wordCount, charCount, longestWord);
    }

    public record WordCountResult(String originalText, int wordCount, int charCount, String longestWord) { }
}