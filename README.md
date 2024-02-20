# Policeman Thief Graph Game

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.1.0-blue)
![Scala Version](https://img.shields.io/badge/Scala-2.13.10-red)
![Akka HTTP Version](https://img.shields.io/badge/Akka-10.5.0-blueviolet)
![Akka Version](https://img.shields.io/badge/Akka-2.8.0-blueviolet)
![ScalaTest Version](https://img.shields.io/badge/ScalaTest-3.2.x-orange)
![Typesafe Config Version](https://img.shields.io/badge/Typesafe_Config-1.4.1-brightgreen)
![Logback Version](https://img.shields.io/badge/Logback-1.2.3-yellow)
![SLF4J Version](https://img.shields.io/badge/SLF4J-1.7.30-lightgrey)
![License](https://img.shields.io/badge/license-Apache_2.0-green)

## Table of Contents
1. [Introduction](#introduction)
2. [Microservices Architecture](#microservices-architecture)
3. [Interactions Between Microservices](#interactions-between-microservices)
4. [Quick Start](#quick-start)
5. [Video Walkthru](#video-walkthru)
6. [System Architecture](#system-architecture)
7. [Code Logic and Flow](#code-logic-and-flow)
8. [Generated Statistics](#generated-statistics)
9. [Areas of Improvement](#areas-of-improvement)
10. [Known Bugs and Issues](#known-bugs-and-issues)
11. [References and Citations](#references-and-citations)

## Video Walkthru

A video walkthru explaining the entire system, including microservices interactions and deployment https://youtu.be/wrUE1sUKhKw
---

## Introduction

This project focuses on a distributed graph-based simulation system, designed to model and analyze interactions in a networked environment. The system is based on three interconnected microservices, each serving a distinct role within the broader architecture. The simulation's primary objective is to understand dynamic interactions within a graph structure, particularly focusing on strategic movements and decision-making processes.

## Microservices Architecture

The simulation uses three interconnected microservices:
- **Game Logic Service (GLS)**: Manages game logic, including starting games and processing moves.
- **Graph Query Service (GQS)**: Handles queries related to game state and move possibilities.
- **Player Management Service (PMS)**: Manages player sessions and interactions.

Each service is designed to function independently while seamlessly communicating with others.

---

## Interactions Between Microservices

- **GLS** is the core service where the game logic resides. It communicates with **GQS** to fetch game states and calculate move possibilities.
- **PMS** handles player interactions, starting new games via **GLS**, and fetching game states and possible moves from **GQS**.
- **GQS** interacts with **GLS** to fetch the current game state and then computes the potential moves and their confidence scores based on this state.

## Setup and Execution

### Prerequisites
- Java 8
- Scala 2.13.x
- sbt 1.9.x

### Running the Services
1. Clone the repository for each service.
2. Modify the Conf file based on your paths and other needs
3. Run using ```sbt clean compile run```

---

## Technologies Used

- **Scala**: Programming language for service development.
- **Akka HTTP**: Toolkit for building HTTP-based services.
- **Docker**: Containerization platform.
- **AWS**: For cloud deployment and scalability.

---

## Potential Improvements

- **Load Balancing**: Implementing a load balancer to distribute requests evenly across service instances.
- **Fault Tolerance**: Enhancing system robustness through advanced error handling and recovery mechanisms.
- **UI/UX**: Developing a user interface for easier interaction and monitoring of the simulation.

## License

This project is licensed under the Apache 2.0 License. See [LICENSE](#) for more details.

_The badges and other specific details can be adjusted as per the actual project specifications._

##
