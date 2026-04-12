# Photos Assignment

Milestone 1 sets up the JavaFX 21 project skeleton and a minimal FXML login screen.

## Project Layout

- `src/` - Java source, controllers, and FXML views
- `data/` - serialized app data and bundled stock photo folder
- `docs/` - assignment documentation output later in the project

## Requirements

- JDK 21
- Maven 3.9+ for local build and run

## Run The App

From the project root:

```bash
./scripts/run.sh
```

## Compile Without Launching

```bash
./scripts/compile.sh
```

These scripts force Maven to use JDK 21 and keep downloaded dependencies inside the project-local `.m2/` folder.

## Current Scope

This repository currently contains only the milestone 1 bootstrap:

- `Photos` JavaFX entry point
- module configuration
- `LoginView.fxml`
- `LoginController`

Real login routing, persistence, models, and user features will be added in later milestones.
