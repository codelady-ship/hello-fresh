# HelloFresh Landing Page Clone: full-stack practice project

[![Live demo](https://img.shields.io/badge/live-demo-067a46?style=for-the-badge)](https://codelady-ship.github.io/hello-fresh/hellofresh.html)
[![Backend CI](https://github.com/codelady-ship/hello-fresh/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/codelady-ship/hello-fresh/actions/workflows/backend-ci.yml)

A responsive clone of the HelloFresh meal-kit landing page (HTML, CSS, vanilla JavaScript) with a **Java 21 / Spring Boot 3 REST API** behind it. The page loads its menu cards and plan prices from the API and sends the "Flexible Plans" form to it. If the API is not configured, asleep or down, the page keeps its built-in content, so the GitHub Pages site never looks broken. (Until the API is deployed, `DEPLOYED_API_URL` in `hellofresh.js` is `null` and the live page runs on static content only.)

> **Practice clone.** This is a learning project. It is not affiliated with, endorsed by or connected to HelloFresh. The HelloFresh name, logo, icons and recipe photos belong to HelloFresh. The hero and community photos are free stock photos from Unsplash (see [Photo credits](#photo-credits)); the clone shows no photos of real customers or celebrities.
>
> 🇦🇿 Təlim layihəsi: HelloFresh açılış səhifəsinin HTML, CSS və JavaScript ilə hazırlanmış responsiv klonu, arxasında Java Spring Boot REST API.

![Screenshot of the landing page](docs/screenshot.png)

## Portfolio overview

**What it is.** A static landing page turned into a small full-stack app:

- **Meal kits**: the four menu cards in the "Over 30+ fresh recipes every week" carousel (title, description, image, cooking time, calories, tags, price per serving) come from `GET /api/meal-kits`.
- **Plans**: "Flexible Plans" are people x meals per week. The price per serving and weekly total come from `GET /api/plans`.
- **Plan requests**: the plan form validates in the browser and on the server, prices the plan and stores the request with `POST /api/plan-requests`.

**Stack.**

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3 (Grid, Flexbox), vanilla JavaScript (`fetch` with `AbortController` timeouts), GitHub Pages |
| Backend | Java 21, Spring Boot 3.5 (Web, Validation, Data JPA, Actuator), Maven wrapper |
| Database | PostgreSQL (Neon) in production, H2 in PostgreSQL mode for local dev and tests, Flyway migrations |
| API docs | springdoc-openapi, Swagger UI at `/swagger-ui` |
| Errors | RFC 9457 `application/problem+json` from one global `@RestControllerAdvice` |
| Tests | JUnit 5, MockMvc (`@WebMvcTest`), Mockito service tests, `@DataJpaTest`, `@SpringBootTest` |
| Delivery | Multi-stage Dockerfile (Temurin 21 JRE), Render Blueprint (`render.yaml`), GitHub Actions CI |

### Architecture

```mermaid
flowchart LR
    subgraph Browser
        P["hellofresh.html + hellofresh.js<br/>(GitHub Pages)"]
    end
    subgraph Render["Render free web service (Frankfurt, Docker)"]
        C["Controllers<br/>/api/meal-kits · /api/plans · /api/plan-requests"]
        S["Services<br/>pricing, validation"]
        R["Spring Data JPA repositories"]
        H["/actuator/health"]
        C --> S --> R
    end
    DB[("Neon PostgreSQL<br/>Flyway V1 schema, V2 seed")]
    P -- "fetch JSON, 8 s timeout<br/>CORS: codelady-ship.github.io" --> C
    P -. "API down or slow:<br/>keep static cards and prices" .-> P
    R --> DB
```

Code layout (`backend/src/main/java/io/github/codeladyship/hellofresh/`):

```
controller/   REST endpoints (thin, validated input)
service/      business logic: plan pricing, request handling
repository/   Spring Data JPA interfaces
entity/       JPA entities: MealKit, Plan, PlanRequest
dto/          request/response records, PageResponse<T>
exception/    NotFoundException, InvalidRequestException, GlobalExceptionHandler (problem+json)
config/       CORS, OpenAPI, DATABASE_URL -> JDBC conversion
resources/db/migration/   Flyway: V1 schema, V2 seed data from the page
```

### Run it locally

Requirements: JDK 21. Nothing else: the `dev` profile uses an in-memory H2 database that Flyway creates and seeds on start.

```bash
git clone https://github.com/codelady-ship/hello-fresh.git
cd hello-fresh/backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
# API:        http://localhost:8080/api/meal-kits
# Swagger UI: http://localhost:8080/swagger-ui
# Health:     http://localhost:8080/actuator/health
```

In a second terminal, serve the site (any static server works) and open it on `localhost`; `hellofresh.js` uses `http://localhost:8080` automatically when the page runs on `localhost` or `127.0.0.1`:

```bash
cd hello-fresh
npx serve -l 5500 .             # then open http://localhost:5500/hellofresh.html
```

Run all tests: `./mvnw verify` (in `backend/`).

### API

All list endpoints are paginated with `page` (0-based), `size` (max 50) and `sort`, and return `{content, page, size, totalElements, totalPages, last}`. Errors are `application/problem+json`; validation errors add an `errors: [{field, message}]` array.

| Method | Path | Description | Success | Errors |
|---|---|---|---|---|
| GET | `/api/meal-kits` | List meal kits, optional `?tag=vegetarian` | 200 | 400 bad sort/params |
| GET | `/api/meal-kits/{slug}` | One meal kit, e.g. `veggie` | 200 | 400, 404 |
| GET | `/api/plans` | All plans with servings, price per serving, shipping, weekly total | 200 | 400 |
| GET | `/api/plans/quote?people=2&mealsPerWeek=3` | Price for one combination | 200 | 400 out of range, 404 not offered |
| POST | `/api/plan-requests` | Body `{name, email, people, mealsPerWeek, mealKitSlug?}`; stores the request with a price snapshot | 201 | 400 validation, 422 plan/meal kit not available, 429 more than 5 per minute from one IP |
| GET | `/actuator/health` | Health check (Render uses it) | 200 | |
| GET | `/swagger-ui`, `/v3/api-docs` | Interactive docs and OpenAPI JSON | 200 | |

Example:

```bash
curl -X POST http://localhost:8080/api/plan-requests \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ana","email":"ana@example.com","people":2,"mealsPerWeek":3,"mealKitSlug":"veggie"}'
# 201 {"id":1,"status":"RECEIVED","name":"Ana","people":2,"mealsPerWeek":3,"mealKitSlug":"veggie","weeklyTotal":70.93,...}
```

CORS allows only `https://codelady-ship.github.io` and `http://localhost:*` / `http://127.0.0.1:*` (override with the `CORS_ALLOWED_ORIGINS` env var).

**Abuse limits and data retention.** The plan form is a public write endpoint that stores a name and an email, so:

- `POST /api/plan-requests` is rate limited per client IP (in memory, default 5 per minute, `PLAN_REQUESTS_PER_MINUTE`). Over the limit the API answers `429 Too Many Requests` as problem+json with a `Retry-After` header. It is a basic spam throttle for a single free instance, not a security boundary.
- A scheduled job (`MaintenanceJobs`) deletes plan requests older than **30 days** (`PLAN_REQUEST_RETENTION_DAYS`). It runs one minute after start-up and then every 6 hours, so it also runs after a sleeping free instance wakes up.
- The form says on the page that this is a practice project: nothing is ordered and the data only goes to this demo's database.

**About the seed data.** The menu card titles, subtitles and images, and the "from $7.49 per meal" price, are taken from the page. Cooking times, calories, the other prices and the $10.99 shipping fee are sample values for this practice project.

### Deploy (Render + Neon)

The API runs as a free Docker web service on Render; the database is a free Neon Postgres. No secret is stored in git: the connection string is entered only in the Render dashboard.

1. **Neon**: create a project (region: AWS Europe / Frankfurt) and a database named `hellofresh`. Copy the connection string, e.g. `postgresql://USER:PASSWORD@ep-xxxx.eu-central-1.aws.neon.tech/hellofresh?sslmode=require`.
2. **Render**: New > Blueprint > pick this repository. Render reads `render.yaml` and creates the `hellofresh-api` web service (Docker, free plan, Frankfurt, health check `/actuator/health`).
3. When Render asks for `DATABASE_URL`, paste the Neon connection string. The app converts it to JDBC settings at startup. (Alternatively set `JDBC_DATABASE_URL=jdbc:postgresql://HOST/hellofresh?sslmode=require`, `JDBC_DATABASE_USERNAME` and `JDBC_DATABASE_PASSWORD` instead.)
4. Wait for the first deploy. Flyway creates the tables and seeds the data. Check `https://<service>.onrender.com/actuator/health` returns `{"status":"UP"}`.
5. Connect the page: in `hellofresh.js`, change `const DEPLOYED_API_URL = null;` to the exact URL Render shows for the service (e.g. `'https://hellofresh-api-xxxx.onrender.com'`) and push to `main` (GitHub Pages redeploys the page). Until then the live page makes no API calls: it shows its static content and the form says the demo backend is not connected yet. Do not guess the URL: an unclaimed `*.onrender.com` name could be registered by someone else.

Free Render instances sleep after 15 minutes without traffic and need about 30 to 60 seconds to wake up. The page waits 8 seconds for data and otherwise keeps its static content; the form waits up to 45 seconds and tells the user it may be waking the server.

### Screenshots

| Menu cards loaded from the API | Plan form, request saved |
|---|---|
| ![Menu carousel rendered from GET /api/meal-kits](docs/screenshots/api-menus-1440.jpg) | ![Flexible plans form after a successful POST /api/plan-requests](docs/screenshots/api-plans-1440.jpg) |

| No API configured (live page before deploy, 390 px) | Swagger UI |
|---|---|
| ![Plan form with the practice-project note and the "demo backend is not connected yet" message](docs/screenshots/no-api-plans-390.jpg) | ![Swagger UI listing the API endpoints](docs/screenshots/swagger.jpg) |

## Features

- Hero, "Why HelloFresh?", "What's inside each box?", recipe plans, community photos, flexible plans with a plan request form, FAQ and footer sections
- Sticky navigation with in-page links and a working hamburger menu on small screens
- Recipe carousel with previous/next arrow buttons (scroll-snap, swipe on touch), filled from the API with a static fallback
- Live plan price (people x meals per week) and a plan request form with success and error states
- FAQ built with native `<details>` / `<summary>` (no JavaScript needed)
- Responsive layout with CSS Grid and Flexbox, tested at 390, 768, 1440 and 1920 px
- Self-hosted Open Sans font via `@font-face`
- Large images converted to WebP (every photo is under 200 KB)
- SEO basics: meta description, canonical URL, Open Graph/Twitter tags, favicon, `robots.txt`, `sitemap.xml`

## Photo credits

Free photos from [Unsplash](https://unsplash.com), used under the [Unsplash License](https://unsplash.com/license). They were cropped and converted to WebP.

| File | Photo | Photographer |
|---|---|---|
| `images/page1.webp` (hero, left bowl) | [Bowl of food on a white table](https://unsplash.com/photos/a-bowl-of-food-on-a-white-table-KwGoCJGwbCs) | [Xavier Chng](https://unsplash.com/@cohesionsingapore) |
| `images/page1.webp` (hero, right bowl) | [Plate of food with meat, vegetables and sauce](https://unsplash.com/photos/a-plate-of-food-with-meat-vegetables-and-sauce-o3Og5MYceTI) | [Xavier Chng](https://unsplash.com/@cohesionsingapore) |
| `images/page5-1.webp` | [Person slicing vegetable](https://unsplash.com/photos/person-slicing-vegetable-uB7q7aipU2o) | Jonathan Borba |
| `images/page5-2.webp` | [Person holding black frying pan with cooked food](https://unsplash.com/photos/person-holding-black-frying-pan-with-cooked-food-UjIkvPvfsFY) | Carlos Fernández |
| `images/page5-3.webp` | [People mixing items on bowl](https://unsplash.com/photos/people-mixing-items-on-bowl-XiWtCLWK5K8) | Annie Spratt |
| `images/page5-4.webp` | [Hands arranging fresh cherry tomatoes on a wooden board](https://unsplash.com/photos/hands-arranging-fresh-cherry-tomatoes-on-a-wooden-board-Yy8FETHf8lo) | [Ahmet Koç](https://unsplash.com/@ahmetkoc) |

## Project structure

```
hellofresh.html   main page
index.html        redirects the root URL to hellofresh.html
hellofresh.css    base styles
responsive.css    media queries
hellofresh.js     hamburger menu, carousel, API calls with static fallback, plan form
images/           page images (WebP for photos)
font-family/      Open Sans font files
backend/          Spring Boot API (Maven wrapper, Dockerfile, Flyway migrations, tests)
render.yaml       Render Blueprint for the API
.github/workflows/backend-ci.yml   CI: ./mvnw verify, PostgreSQL smoke test, Docker build
```

## Deployment of the page

GitHub Pages serves the `main` branch from the repository root, so every push to `main` updates the live site:
https://codelady-ship.github.io/hello-fresh/

## What I learned

- Why fixed `margin-left` values break a layout on other screen sizes, and how Grid/Flexbox with `max-width` fixes that
- `calc()` needs spaces around `+` and `-`; `clamp()` keeps fluid font sizes readable
- How `@font-face` needs a `font-family` name and a correct path before it can be used
- Building an accessible hamburger menu (`aria-expanded`, closing on link click and Escape)
- Compressing images to WebP and why image size matters for page speed
- Layering a Spring Boot API (controller, service, repository, DTO, entity) and returning consistent problem+json errors
- Writing Flyway migrations that run on both H2 and PostgreSQL, and testing each layer with the right Spring test slice
- Calling an API from a static page with timeouts and a fallback, and locking it down with CORS
