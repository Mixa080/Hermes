package com.example.aiapp.model;

public enum ChatMode {
    CASUAL("Розважливий", "Світла, яскрава тема", "О, круто придумав! Зараз замутимо таку штуку - сусіди обзавидуються!"),
    PROFESSIONAL("Професійний", "Нейтральна, ділова тема", "Добре. Завдання прийнято. Приступаю до виконання."),
    ACADEMIC("Науковий", "Холодні відтінки", "Згідно з останніми дослідженнями, оптимальна реалізація вимагає адаптивної логіки на рівні інтерфейсу і моделі.");

    private final String title;
    private final String themeDescription;
    private final String exampleResponse;

    ChatMode(String title, String themeDescription, String exampleResponse) {
        this.title = title;
        this.themeDescription = themeDescription;
        this.exampleResponse = exampleResponse;
    }

    public String getTitle() {
        return title;
    }

    public String getThemeDescription() {
        return themeDescription;
    }

    public String getExampleResponse() {
        return exampleResponse;
    }
} 