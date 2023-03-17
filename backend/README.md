## Introduction

This is the repository of the capstone project of the [Java Development Bootcamp](https://www.neuefische.de/bootcamp/java-development) by [neuefische](https://www.neuefische.de/) created by [Gleb Abramov](https://github.com/gadmel).<br>

The project is a web application built with [Vite](https://vitejs.dev/), [Typescript](https://www.typescriptlang.org/) and [React](https://reactjs.org/) on the frontend, and with [Maven](https://maven.apache.org/) [Java](https://www.java.com/en/) and [Spring Boot](https://spring.io/projects/spring-boot) on the backend.

The capstone project as itself is intended to be an extended Developer Homepage with a secured CV access.

## Availability

The application is CI/CD enabled and is automatically deployed to https://gleb-abramov.com using [Firebase](https://firebase.google.com/) once the main branch is updated.<br>
This is achieved by configuring the project for deployment using [Google Cloud Build](https://cloud.google.com/build) and [Google Container Registry](https://cloud.google.com/container-registry).

## Installation

Before using this project, you need to install the following dependencies:
### Backend Dependencies
- JDK 17
- Maven
- Homebrew
- Docker

### Frontend Dependencies 
- Node
- React
- Vite
- Typescript

### Install Homebrew

If you don't have Homebrew installed, please follow these steps:
1. Open Terminal.
2. Type the following command and press Enter:
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ``` 
3. Follow the on-screen instructions to complete the installation.

### Install Backend Dependencies

To install JDK 17, Maven, and Docker, you can use Homebrew. Open Terminal and run the following commands:
```bash
brew install openjdk@17
brew install maven
brew install docker
```
Once Docker is installed, you'll need to start it by running the following command:
```bash
open /Applications/Docker.app
```

### Install Frontend Dependencies

To install Node, React, Vite, and Typescript, open Terminal and run the following commands:
```bash
brew install node
npm install --global create-react-app
npm install --global vite
npm install --global typescript
```
## Usage

This project includes a bash script located in the .github/scripts directory that can be used to build a Docker container of the project locally.<br>
To run this script, open Terminal, navigate to the root directory of the project, and run the following command:
```bash
cd ./.github/scripts && ./docker.sh && cd ../..
```
If it doesn't work, try to run the following command:
```bash
chmod +x ./.github/scripts/docker.sh
```
THe `docker.sh` script will build a Docker container of the project and tag it with the name `gleb-abramov`.

Once the container is built, you can access the application by navigating to [localhost:3000](localhost:3000) in your web browser.

Please note that this project is configured for deployment using Google Cloud Build and Google Container Registry, and it's not recommended to use the deploy.sh script directly.

Instead, we recommend following the instructions in the cloudbuild.yml file to set up a Cloud Build trigger that will build a container image of the project and push it to Google Container Registry. The `firebase.json` file can be used to deploy the application to Firebase Hosting. 

Please see the comments in the cloudbuild.yml file for more information and links to helpful resources on containerizing and deploying Java applications with Google Cloud Build and Firebase Hosting.
## Conclusion

I hope this readme has provided you with all the necessary information to install the dependencies and use the bash script to build a Docker container of the project locally. If you have any questions or issues, please don't hesitate to contact me. Thank you for checking out my capstone project!