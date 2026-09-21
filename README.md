# Asset-Control

Comprehensive IT Asset Management (ITAM) web system. Centralizes the control of computer hardware, mobile devices, user assignments, and responsibility document generation.

Asset-Control is designed to modernize and centralize technology inventory administration. It allows for the management of the complete hardware lifecycle, maintaining exact traceability of locations, maintenance records, and assignments within a fast and responsive interface.

## Key Features

* **Computer Hardware Management:** Detailed registry of laptops and desktop computers (hostname, asset tag, serial number, operating system, etc.) and their maintenance history.
* **Mobile Device Inventory:** Mobile phone management by IMEI and model. It allows for independent management of phone lines and carriers to rotate them among physical devices.
* **Assignments and Loans Control:** Equipment lifecycle tracking. It identifies which user has which device, whether it is a permanent assignment or a temporary loan with a due date, and maintains a historical log of returns.
* **People Views and Bulk Import:** Inventory can be viewed by equipment or by assigned person. Computer inventory can be imported from a validated CSV that creates or updates its reference records.
* **Responsibility Document Generation:** Automated creation of responsibility documents in Word format (.docx). It uses an editable base template within the project, allowing users to adapt legal terms and formatting without needing to modify the source code.

## Technologies

The project is built on the Java ecosystem and does not rely on external CSS frameworks, keeping the interface lightweight.

* **Backend:** Java 21, Spring Boot
* **Database:** SQLite (Current phase)
* **Frontend:** Thymeleaf, HTML5, native CSS3
* **Dependency Management:** Maven

## Installation and Usage

The project is designed to start quickly without the need for complex environment variable configurations. The local SQLite database is generated automatically upon application startup.

1. Clone this repository:
   ```bash
   git clone [https://github.com/AngelRicardoIA/Asset-Control.git](https://github.com/AngelRicardoIA/Asset-Control.git)
   ```
2. Navigate to the project directory:
   ```bash
   cd Asset-Control
   ```
3. Run the application using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Open your web browser and access `http://localhost:8080`.

## Roadmap

Asset-Control is under active development. The following features are planned for future releases:

* Implementation of a more robust database engine for production environments.
* Authentication and authorization module (login and role management).
* Accessories management module (monitors, peripherals, etc.).

## License

This project is distributed under the MIT License. See the `LICENSE` file for more information.
