# Dev-Utils

**A central request orchestrator for microservices environments.** Avoid changing your main application's code by instantly switching between real services and simulated responses (Mocks).

## 🚀 What does it do?

Dev-Utils acts as a **smart Proxy/Mock** that intercepts calls and decides the destination based on priorities:

1. **Mock (Active):** If a Mock is registered for the endpoint, it returns the static JSON immediately.
2. **Proxy (Active):** If no Mock exists, it redirects the request to the configured `host` for that route.
3. **Fallback (Global):** If neither matches, the request is sent to a default global URL.

## 🛠️ How to use

1. **Create a Collection:** Group your endpoints by project (e.g., "Finance", "Sales").
2. **Configure Mocks:** Register the Path, HTTP Method, and JSON response for rapid testing.
3. **Define Routes:** Point URL prefixes to real services running locally or in staging.
4. **Toggle in seconds:** Enable or disable any rule via the interface without restarting the app.

## 📦 Import and Export

* **Standardization:** Export your configurations to JSON and share them with your team so everyone can test the same error or success scenarios.

## 🏃 Quick Start

* **Requirements:** Java 17 and Maven.
* **Build:** `mvn clean install`
* **Run:** `mvn spring-boot:run`
* **Access:** `http://localhost:8080`
