This documentation was prepared for the `README.md` file of your repository, focusing on clarity for Junior developers and highlighting the logical operation of the **Dev-Utils** solution.

---

# Dev-Utils

**Dev-Utils** is a tool developed to facilitate the local orchestration of systems distributed in microservices. It acts as a central point (Proxy/Mock) that decides how requests should be handled, allowing you to switch between real services and simulated responses without having to change your main application's code.

## 🚀 How the system works

The core of the project is based on a decision hierarchy for each received request. When a call arrives at Dev-Utils, it follows this order of priority:

1. **Mock Configs (Active):** The system checks if there is a static response registered for the requested path and HTTP method. If there is an **active** Mock, it returns the configured JSON immediately.
2. **Proxy Routes (Active):** If no active mock exists, the system looks for a Proxy route. If the start of the URL matches a registered prefix and the route is **active**, the request is redirected to the specific server (host) of that service.
3. **Fallback (Last Resort):** If no mock or route is found, the system uses the **Fallback URL**. It is the last destination consulted to ensure that the request is not "lost".

### ⚙️ The Nature of the Fallback URL

Unlike specific routes, the **Fallback URL is Global**. Although it can be viewed within a Collection's interface, it does not belong exclusively to any of them. Changing the Fallback URL affects the entire Dev-Utils ecosystem globally, meaning for all Collections.

## 🛠️ Management Features

* **Activate/Inactivate Mocks and Proxies:** You can "turn off" a mock or a route without having to delete them. This allows you to test the behavior of a real service and switch to a mock in seconds just by changing the status.
* **Complete Deletion:** The system allows the removal of mocks, proxies, and entire collections. When a Collection is deleted, all items linked to it are automatically removed.
* **Separation by Collections:** Collections work as organizing folders. You can create a collection for the "Financial Project" and another for the "Sales Project," keeping the endpoints of each context isolated and organized.

### 📦 Import and Export

Dev-Utils allows you to export your configurations to a JSON file and import them into another instance.

* **Use Case:** A developer can configure an entire error scenario for a complex API and export that Collection so that the rest of the front-end team can simulate the same error on their local machines just by importing the file, ensuring standardization in tests.

## 🛠️ Requirements and Technologies

* **Java:** 17
* **Framework:** Spring Boot 3.3.6
* **Dependency Manager:** Maven
* **Database:** SQLite (Simplified local storage)

## 🏃 How to Build and Run

To run the project on your machine, use the Maven commands:

1. **Build the project:**
```bash
mvn clean install
```

2. **Run the application:**
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.
