# About
A Simple Trivia game that's made with Java 21 spring boot and Angular.

Try the game out by visting the following link: https://trivia-frontend-production.up.railway.app/

# Technology stack
## Backend
* Java 21
* Spring boot 3.x
* Maven for dependency injection
* RESTful API architecture

## Frontend
* Angular 20+

# Getting started 
## prerequisites
#### Make sure the follwoing software is installed on your system:

* Java Development Kit (JDK): Version 21 or higher.
* Maven: for building the Java backend.
* Node.js & npm: Version 18 or higher.
* Angular CLI: Global installation (npm install -g @angular/cli)

## 1. The Backend (Java/Spring Boot)
The backend is responsible for API endpoints, logic, and fetching categories and questions.

### Setup, Build, and Run
Clone the Repository:
```
git clone https://github.com/afedjah/Trivia_opdracht.git
cd Trivia_backend/demo
```
Run the Application (Development Mode): This command builds and runs the application directly, starting the API on http://localhost:8080.
```
.\mvnw spring-boot:run
```
Testing
To run the unit and integration tests, use the following command in the backend folder:
```
./mvnw test
```

## 2. The Frontend (Angular)
The frontend is the web application that communicates with the backend.

Local Build and Run
Navigate to the Frontend Folder:

### Assuming you are in the 'Trivia_backend/demo' folder
```
cd ../../trivia-frontend
```
Install Dependencies: Install all necessary Node modules:
```
npm install
```
Start the Development Server: Start the Angular application. It will default to http://localhost:4200 and is configured to proxy API requests to the running backend on http://localhost:8080.
```
ng serve
```
Important: API URL
The frontend is configured to call the backend API at http://localhost:8080. Ensure the backend (Section 1) is running before opening the frontend application.
