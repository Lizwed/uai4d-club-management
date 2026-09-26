### UAI4D Club Management System

A Java-based web application for managing club activities, members, events, projects, and resources. Built with Jakarta EE (Servlets & JSP), MySQL, and deployed on Apache Tomcat. 

### ✨ Features

* **User Management:** Registration, login, profile updates, password reset.
* **Role-Based Access:** Separate dashboards and permissions for members and admins.
* **Event Management:** Admins can create and delete events; members can register.
* **Project Management:** Admins can create and delete projects; members can join.
* **Resource Management:** Admins can add and delete resources.
* **Reporting:** Export reports in PDF, Excel, and CSV formats.
* **Security:** BCrypt password hashing, HttpSession management, and a custom SecurityFilter.
* **AJAX Support:** For features like username availability checks and dashboard stats.

### 🛠️ Tech Stack

LayerTechnology
****Language****
Java 17+ (JDK 24 for local development)
****Web Framework****
Jakarta EE 11 (Servlets 6.0, JSP 3.1, JSTL 3.0)
****Database****
MySQL 8.0+ with JDBC (MySQL Connector/J 9.2.0)
****Security****
BCrypt, HttpSession, Custom SecurityFilter
****JSON****
Jackson Databind 2.18.1
****Export Libraries****
Apache POI 5.2.5 (Excel), iText 7 7.2.5 (PDF), Apache Commons CSV 1.10.0
****Build Tool****
Maven
<img width="464" height="735" alt="Screenshot 2026-09-26 210007" src="https://github.com/user-attachments/assets/bd69155e-dc93-4508-928c-7a102fc0c8b5" />
<img width="405" height="889" alt="Screenshot 2026-09-26 210103" src="https://github.com/user-attachments/assets/1e02b509-40bc-45cc-8568-14626f8e1304" />
<img width="674" height="904" alt="Screenshot 2026-09-26 210438" src="https://github.com/user-attachments/assets/7956899b-3812-4ec6-9a5c-645bad14e2a0" />
<img width="823" height="918" alt="Screenshot 2026-09-26 203043" src="https://github.com/user-attachments/assets/46c7dfe2-21cb-4d40-8138-cf438feee11c" />
<img width="963" height="897" alt="Screenshot 2026-09-26 210520" src="https://github.com/user-attachments/assets/76dc22b3-928c-4098-8431-705b80402342" />

### 🚀 Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites

* **JDK 17 or higher**
* **Apache Maven**
* **MySQL Server** (version 8.0 or higher)
* **Apache Tomcat** (version 9.0 or 10.1)

### Installation & Setup

1. **Clone the repository:** 

bash

git clone https://github.com/Lizwed/uai4d-club-management
cd UAI4DClub

Use code with caution.
2. **Set up the database:** 

  * Create a new MySQL database (e.g., uai4d_club).
  * Run the SQL script located in src/main/resources/db/schema.sql (if available) to create the necessary tables.
  * Update the database connection details in your configuration file (e.g., src/main/resources/db.properties) or set them as environment variables: 

properties

DB_URL=jdbc:mysql://localhost:3306/uai4d_club
DB_USER=your_db_user
DB_PASS=your_db_password

Use code with caution.
3. **Build the application:** 

bash

mvn clean package

Use code with caution.

This will create a .war file in the target/ directory.
4. **Deploy to Tomcat:** 

  * Copy the generated .war file (e.g., target/UAI4DClub.war) to the webapps directory of your Tomcat installation.
  * Start Tomcat.
5. **Access the application:** 

  * Open your browser and go to http://localhost:8080/UAI4DClub.

### 📁 Project Structure

A quick overview of the main directories: 

text

.
├── .github/
│   └── workflows/
│       └── build.yml      # CI/CD Workflow
├── src/
│   ├── main/
│   │   ├── java/         # Java source code (Servlets, DAOs, Models)
│   │   ├── resources/    # Configuration files (DB properties, SQL scripts)
│   │   └── webapp/       # Web resources (JSP, CSS, JS, WEB-INF)
│   └── test/             # Unit tests
├── pom.xml               # Maven project configuration
└── README.md             # This document

Use code with caution.

### ⚙️ CI/CD Automation (GitHub Actions)

This project includes a GitHub Actions workflow to automatically build the application on every push or pull request to the main branch. 

The build pipeline generates a downloadable .war artifact ready for testing or production deployment. 

### Workflow Configuration

The file .github/workflows/build.yml contains: 

yaml

name: Build WAR

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: 'maven'

    - name: Build with Maven
      run: mvn clean package -DskipTests

    - name: Upload WAR artifact
      uses: actions/upload-artifact@v4
      with:
        name: uai4d-club-war
        path: target/*.war

Use code with caution.

### Java Compatibility Note

If you develop locally using a different version (like JDK 24), make sure your pom.xml explicitly restricts compiler settings to **Java 17** matching the workflow environment to prevent build runtime failures: 

xml

<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>

Use code with caution.

### 🤝 Contributing

Contributions are welcome. Please fork the repository and create a pull request with your changes. 

