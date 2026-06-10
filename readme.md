<h1>NearPharma — Location-Based Pharmacy Finder API</h1>

<p>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
  <img src="https://img.shields.io/badge/RapidAPI-Integrated-0055DA?style=for-the-badge&logo=rapid&logoColor=white"/>
</p>

A **Spring Boot REST API** that locates nearby pharmacies based on user coordinates, provides
turn-by-turn directions, filters by pharmacy chain, and searches healthcare places —
powered by RapidAPI's TrueWay services and backed by PostgreSQL.

---

## ✨ Features

- 📍 **Proximity search** — find pharmacies sorted by distance from user's lat/lng
- 🗺️ **Directions** — get driving/walking/cycling/transit routes to any pharmacy
- 🏥 **Chain filtering** — filter results by pharmacy chain (Apollo, MedPlus, etc.)
- 🔍 **Healthcare place search** — search for hospitals, dentists, clinics nearby
- 🐳 **Docker Compose** — one command to run the full stack locally
- 📬 **Postman collection** — included for easy API testing

---

## 🏗️ Architecture

                 Client Request
                       │
                       ▼
            ┌─────────────────────┐
            │   REST Controller   │  ← Receives lat/lng, mode, filters
            └──────────┬──────────┘
                       │
            ┌──────────▼──────────┐
            │    Service Layer    │  ← Distance calc, business logic
            └──────────┬──────────┘
                       │
                 ┌─────┴──────┐
                 │            │
            ┌────▼────┐  ┌────▼──────────┐
            │ JPA Repo│  │  RapidAPI     │
            │(PostgreSQL││ (TrueWay      │
            │ pharmacy │ │  Directions + │
            │  data)   │ │  Places)      │
            └──────────┘ └───────────────┘

---

---

## 🛠️ Tech Stack

- **Java 17** + **Spring Boot 3.x**
- **Spring Data JPA** + **PostgreSQL** — pharmacy CRUD and persistence
- **RapidAPI** — TrueWay Directions + TrueWay Places for routing and search
- **Docker + Docker Compose** — containerized full-stack setup
- **Maven** — build and dependency management

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Docker + Docker Compose
- RapidAPI account (free tier works) — get your key at [rapidapi.com](https://rapidapi.com)

### 1. Clone the repo
```bash
git clone https://github.com/warisamir/NearPharmaViaSpringboot.git
cd NearPharmaViaSpringboot
```

### 2. Create environment file
Create `.env.properties` in the root directory:
```properties
rapidApiKey.key=YOUR_RAPIDAPI_KEY
DB_DATABASE=pharma_db
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
```

### 3. Run with Docker Compose
```bash
docker compose up --build
```

API will be available at `http://localhost:8080`

---

## 📡 API Endpoints

### Pharmacy CRUD

POST   /api/pharmacies/createpharmacy     — Add a pharmacy
GET    /api/pharmacies/getAll             — List all pharmacies
GET    /api/pharmacies/{id}               — Get pharmacy by ID
PUT    /api/pharmacies/{id}               — Update pharmacy
DELETE /api/pharmacies/{id}               — Delete pharmacy     

### Location & Discovery

GET /api/pharmacies/distances
?lat=12.9716&lng=77.5946&mode=driving
— Get all pharmacies sorted by distance from coordinates
GET /api/pharmacies/{id}/directions
?fromLat=12.9716&fromLng=77.5946&mode=walking
— Get turn-by-turn directions to a specific pharmacy
GET /api/pharmacies/{id}/nearby
?radius=3000&chains=Apollo&chains=MedPlus
— Find nearby pharmacies, optionally filtered by chain name
GET /api/pharmacies/places/search
?query=pharmacy&types=pharmacy&lat=12.9716&lng=77.5946
— Search healthcare places near coordinates

### Travel modes
`driving` · `walking` · `bicycling` · `transit` (default: `driving`)

---

## 📬 Testing with Postman

A full Postman collection is included — `PharmacyApi.postman_collection.json`

1. Open Postman → Import → select the file
2. Set `baseUrl` variable to `http://localhost:8080`
3. All endpoints are pre-configured with example parameters

---

## 📁 Project Structure

NearPharmaViaSpringboot/
├── src/
│   └── main/java/
│       ├── controller/     ← REST endpoints
│       ├── service/        ← Business logic + RapidAPI calls
│       ├── repository/     ← Spring Data JPA
│       └── model/          ← Pharmacy entity
├── Dockerfile
├── compose.yaml            ← Docker Compose (app + PostgreSQL)
├── PharmacyApi.postman_collection.json
└── pom.xml

---

## 🔮 Planned Enhancements

- [ ] Real-time open/close status from Google Places API
- [ ] Map UI (React frontend) for visual route display
- [ ] Filter by pharmacy rating and operating hours
- [ ] Redis caching for frequent location queries

---

## 👤 Author

**Waris Amir** — Java Backend Engineer, Bangalore
[LinkedIn](https://linkedin.com/in/waris-amir-0387461b3) · [Portfolio](https://portfolio-git-main-warisamirs-projects.vercel.app/) · [GitHub](https://github.com/warisamir)

