# REFERENCES — Références obligatoires

Ce fichier liste les sources à consulter lorsque les documents internes ne suffisent pas.

## Scrum / Agile / Delivery

- Scrum Guide : https://scrumguides.org/
- Scrum.org Resources : https://www.scrum.org/resources
- Atlassian Agile Estimation : https://www.atlassian.com/agile/project-management/estimation
- Kanban Guide : https://kanbanguides.org/
- Evidence-Based Management Guide : https://www.scrum.org/resources/evidence-based-management-guide

## Architecture, cloud-native et production

- 12-Factor App : https://12factor.net
- CNCF Cloud Native Definition : https://github.com/cncf/toc/blob/main/DEFINITION.md
- Google SRE Book : https://sre.google/books/
- Martin Fowler — Microservices : https://martinfowler.com/articles/microservices.html
- Martin Fowler — Branch by Abstraction : https://martinfowler.com/bliki/BranchByAbstraction.html

## Sécurité

- OWASP Top 10 : https://owasp.org/Top10/
- OWASP ASVS : https://owasp.org/www-project-application-security-verification-standard/
- OWASP MASVS : https://mas.owasp.org/MASVS/
- OWASP API Security Top 10 : https://owasp.org/www-project-api-security/
- OWASP Cheat Sheet Series : https://cheatsheetseries.owasp.org/
- NIST Secure Software Development Framework : https://csrc.nist.gov/Projects/ssdf

## Backend — Spring Boot / Java

- Spring Boot Documentation : https://docs.spring.io/spring-boot/
- Spring Boot — versions supportées : https://spring.io/projects/spring-boot#support
- Project Lombok : https://projectlombok.org/features/
- Configuration Lombok (`lombok.config`) : https://projectlombok.org/features/configuration
- SpringDoc OpenAPI : https://springdoc.org/
- Spring Boot Externalized Configuration : https://docs.spring.io/spring-boot/reference/features/external-config.html
- Spring Security Documentation : https://docs.spring.io/spring-security/reference/
- Spring Data JPA Documentation : https://docs.spring.io/spring-data/jpa/reference/
- Java Documentation : https://docs.oracle.com/en/java/
- Apache Maven Documentation : https://maven.apache.org/guides/
- Testcontainers : https://testcontainers.com/
- OpenTelemetry Java : https://opentelemetry.io/docs/languages/java/

## Frontend — Angular / TypeScript

- Angular Documentation : https://angular.dev
- Angular 20 (documentation de la version) : https://v20.angular.dev
- Angular — versions et support : https://angular.dev/reference/releases
- Tailwind CSS v4 : https://tailwindcss.com/docs
- Angular Security Guide : https://angular.dev/best-practices/security
- Tailwind CSS Documentation : https://tailwindcss.com/docs
- TypeScript Handbook : https://www.typescriptlang.org/docs/
- RxJS Documentation : https://rxjs.dev
- Web.dev Performance : https://web.dev/learn/performance/
- WCAG 2.2 : https://www.w3.org/TR/WCAG22/

## Mobile — Flutter / Dart

- Flutter Documentation : https://docs.flutter.dev/
- Dart Documentation : https://dart.dev/guides
- Flutter Security Best Practices : https://docs.flutter.dev/security
- Android App Security Best Practices : https://developer.android.com/privacy-and-security/security-best-practices
- Apple Platform Security : https://support.apple.com/guide/security/welcome/web

## API, documentation et contrats

- OpenAPI Specification : https://spec.openapis.org/oas/latest.html
- JSON:API : https://jsonapi.org/
- Google API Design Guide : https://cloud.google.com/apis/design

## Git, versions et changelog

- Conventional Commits : https://www.conventionalcommits.org/
- Semantic Versioning : https://semver.org/
- Keep a Changelog : https://keepachangelog.com/
- Git Tagging : https://git-scm.com/book/en/v2/Git-Basics-Tagging
- Architecture Decision Records : https://adr.github.io/

## CI/CD et supply chain

- SLSA Framework : https://slsa.dev/
- CycloneDX SBOM : https://cyclonedx.org/
- Trivy : https://trivy.dev/
- Dependabot : https://docs.github.com/en/code-security/dependabot
- GitHub Actions Security Hardening : https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions

## Règle d'utilisation

Quand une IA s'appuie sur une référence externe pour décider, elle doit indiquer dans son résumé final :

- la référence consultée ;
- la règle retenue ;
- l'impact sur le code, l'architecture ou le planning ;
- ce qui reste à vérifier.

## Frontend/Mobile UI, i18n et design system

- Tailwind CSS Documentation : https://tailwindcss.com/docs
- ngx-translate : https://github.com/ngx-translate/core
- Flutter Internationalization : https://docs.flutter.dev/ui/accessibility-and-internationalization/internationalization
- Flutter Theming : https://docs.flutter.dev/cookbook/design/themes
- WCAG 2.2 : https://www.w3.org/TR/WCAG22/

## Design system et UI agents

- Google Labs DESIGN.md : https://github.com/google-labs-code/design.md
- Google Blog — Stitch DESIGN.md : https://blog.google/innovation-and-ai/models-and-research/google-labs/stitch-design-md/
- Tailwind CSS v4 : https://tailwindcss.com/blog/tailwindcss-v4
- Tailwind CSS Theme Variables : https://tailwindcss.com/docs/theme
- Tailwind CSS Dark Mode : https://tailwindcss.com/docs/dark-mode

Règle d’utilisation :

- `DESIGN.md` est la source de vérité design pour Angular et Flutter.
- Pour Angular, utiliser Tailwind CSS v4 avec `@theme` et CSS-first.
- Ne pas utiliser Tailwind CSS v3 ni Angular Material sauf ADR.
