# Horizon Finance

Horizon Finance is a local app built for Windows that allows you to track and analyze your income and spending as simply as it needs to be!

## Project Overview
* **Role:** Head Developer
* **Context:** Summer 2026 Freshman Capstone Project
* **Languages, Tools, and Libraries:** Java, SQLite, JavaFX, CSS, Scene Builder, Git, IntelliJ, Maven
* **Status:** Complete / Production Build (v1.0.0)
* **Support:** Built & Optimized for Windows 10/11

## About the App
* **Create Accounts:** Users can make multiple different accounts and easily switch between them to keep financial data separate.
* **Add Transactions:** View, add, and remove recent transactions to update your income and expense data.
* **Analyze Your Spending:** Make key insights from your finances over any period of time with clean charts and graphs.
* **Entirely Secure:** Horizon keeps all your data right on your device and nowhere else, without the need for logins or a network connection!

## Key Implementations
* **MVC Architecture:** Architected the application following the Model-View-Controller (MVC) pattern, separating data models (Account, Transaction), JavaFX FXML views managed via Scene Builder, and Controller classes handling user interaction and business logic delegation to DatabaseManager.
* **SQLite & JDBC:** Engineered a DatabaseManager class that uses Java's Database Connectivity API to create and interface with a local SQLite database for persistent data storage.
* **JavaFX & CSS:** Built the application's front end using JavaFX styled with CSS, with Controller classes receiving and sending information via data models to create accounts and transactions along with transactional analytics.
