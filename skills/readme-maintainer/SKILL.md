---
name: readme-maintainer
description: Update this repository's README so it matches the current implemented scope. Use when the user asks to create, rewrite, or refresh README content, document new features, update setup or deployment instructions, or keep the README in sync with current code, configuration, Docker deployment, Raspberry Pi behavior, or project limitations.
---

# README Maintainer

Use this skill when editing `README.md` for this repository.

## Core Rules

1. Keep the README in Korean.
2. Document only what is already implemented or explicitly decided.
3. Do not write speculative roadmap content as if it already exists.
4. Treat the README as a living document that will be updated whenever features change.

## Workflow

1. Read the current implementation before editing the README.
2. Prefer the codebase over memory.
3. Update only the sections affected by the current change unless the user asks for a broader rewrite.
4. Keep the wording concrete and operational.

## What To Reflect

When relevant, check and update:

- project purpose
- current implemented scope
- major directory structure
- configuration and required environment variables
- local run commands
- proto generation commands
- Raspberry Pi deployment flow
- current limitations or assumptions

## Repository-Specific Rules

1. Keep examples neutral.
   - Do not leave personal usernames or machine-specific values in examples unless the user explicitly wants them.
   - Prefer values like `pi`, `192.168.0.10`, or placeholder-style examples when documenting deploy settings.
2. Keep Raspberry Pi deployment aligned with the real repository flow.
   - The project builds the Docker image on the developer machine and runs it on the Pi.
   - Do not describe Pi-local `docker build` as the default flow unless the code or scripts change.
3. Keep sensor details aligned with the current code.
   - If `BME680` config, I2C path, or address handling changes, update the README sections that describe them.
4. Respect the current stage of the project.
   - If the project is incomplete, say so explicitly instead of making the README sound finished.

## Editing Notes

1. Prefer short sections over long explanations.
2. Keep command examples executable.
3. When showing config examples, match `application.yaml`, `.env`, and deploy scripts as they exist now.
4. If terminal output looks garbled because of encoding, do not assume the README must be rewritten in English. Keep the README itself in Korean unless the user says otherwise.
