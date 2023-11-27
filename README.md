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

## My Submission: 
1. **Seyfal Sultanov**
2. **NetID: ssulta24**
3. **UIC Email: ssulta24@uic.edu**
4. **UIN: 678686497**

---

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

---

## Introduction

---

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

---

## Quick Start

### Prerequisites
- Scala 2.13.x
- sbt 1.x

### Setup and Execution
1. Clone the repository for each microservice.
2. Follow the instructions in each repository to compile and run the services.
3. Ensure the services are correctly configured to communicate with each other.

## Video Walkthru

A video walkthru explaining the entire system, including microservices interactions and deployment - [YouTube Link](https://youtu.be/wrUE1sUKhKw)
---

## System Architecture

The system architecture diagram highlights the interactions between the microservices, the distributed nature of the application, and how Apache Spark integrates with the system.

![System Architecture Diagram](#) _[Image placeholder for system architecture visual representation]_

---

## Code Logic and Flow

### Initialization
The initialization process sets up the required environment for each microservice, including configuring Apache Spark and establishing communication channels between services.

### Data Loading and Pre-processing
Each microservice is responsible for loading and processing its relevant data, ensuring smooth interaction and data flow between services.

### Distributed Matching
The system collectively works to match nodes, compute statistics, and analyze the efficacy of simulated MitM attacks.

### Post-processing and Result Compilation
Results from the simulations are compiled, including success and failure rates of attacks, and insights into the effectiveness of the security mechanisms.

---

## Generated Statistics

The statistics offer insights into the success rates of attacks, the efficacy of the defense mechanisms, and the overall performance of the simulation.

---

##
