# NearPharma API Documentation

## 📄 Project Overview

**Project Name**: NearPharma
**Description**: A Spring Boot-based service to locate pharmacies nearby, get directions, search healthcare-related places, and filter chains using RapidAPI services.

---

## ⚙️ Setup Instructions

### 1. Requirements

* Java 17+
* Spring Boot 3+
* PostgreSQL
* Maven
* RapidAPI Account

### 2. Configure `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/pharma_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

rapidapi.key=YOUR_RAPID_API_KEY
```

### 3. Dependencies (`pom.xml`)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
</dependencies>
```

---

## 📂 API Endpoints

### 1. Add Sample Pharmacies

**POST** `/api/pharmacies/createpharmacy`

### 2. Get All Pharmacies

**GET** `/api/pharmacies/getAll`

### 3. Get Pharmacy by ID

**GET** `/api/pharmacies/{id}`

### 4. Update Pharmacy by ID

**PUT** `/api/pharmacies/{id}`

### 5. Delete Pharmacy by ID

**Delete** `/api/pharmacies/{id}`

---

### 6. Get Nearby Pharmacies with Distance

**GET** `/api/pharmacies/distances`

#### Query Parameters:

| Param  | Type   | Required | Description                                                       |
| ------ | ------ | -------- | ----------------------------------------------------------------- |
| `lat`  | double | Yes      | Latitude                                                          |
| `lng`  | double | Yes      | Longitude                                                         |
| `mode` | string | No       | `driving`, `walking`, `bicycling`, `transit` (default: `driving`) |

#### Example:

```
GET /api/pharmacies/distances?lat=23.0225&lng=72.5714&mode=driving
```
---

### 4. Get Directions to Pharmacy

**GET** `/api/pharmacies/{id}/directions`

#### Query Parameters:

| Param     | Type   | Required | Description      |
| --------- | ------ | -------- | ---------------- |
| `fromLat` | double | Yes      | Source Latitude  |
| `fromLng` | double | Yes      | Source Longitude |
| `mode`    | string | No       | Mode of travel   |

#### Example:

```
GET /api/pharmacies/3/directions?fromLat=23.0225&fromLng=72.5714&mode=driving
```

---

### 5. Find Nearby Pharmacy Chains

**GET** `/api/pharmacies/{id}/nearby`

#### Query Parameters:

| Param    | Type            | Required | Description                     |
| -------- | --------------- | -------- | ------------------------------- |
| `radius` | int (500-10000) | No       | Radius in meters (default 2000) |
| `chains` | List of String  | No       | Filter by pharmacy chain names  |

#### Example:

```
GET /api/pharmacies/3/nearby?radius=3000&chains=Apollo&chains=MedPlus
```

---

### 6. Search Healthcare Places

**GET** `/api/pharmacies/places/search`

#### Query Parameters:

| Param   | Type           | Required | Description                                                  |
| ------- | -------------- | -------- | ------------------------------------------------------------ |
| `query` | string         | Yes      | Search text (e.g., pharmacy)                                 |
| `types` | List of String | No       | `pharmacy`, `hospital`, `doctor`, `dentist`, `establishment` |
| `lat`   | double         | Yes      | Latitude                                                     |
| `lng`   | double         | Yes      | Longitude                                                    |

#### Example:

```
GET /api/pharmacies/places/search?query=pharmacy&types=pharmacy&lat=23.0225&lng=72.5714
```

---

## 🔢 Sample cURL Requests

### 1. Get Route

```bash
curl -X GET "https://trueway-directions2.p.rapidapi.com/FindDrivingRoute?stops=23.0225,72.5714;23.0256,72.5718" \
  -H "x-rapidapi-host: trueway-directions2.p.rapidapi.com" \
  -H "x-rapidapi-key: YOUR_RAPID_API_KEY"
```

### 2. Place Search

```bash
curl -X GET "https://trueway-places.p.rapidapi.com/FindPlaceByText?input=pharmacy&location=23.0225,72.5714&types=pharmacy" \
  -H "x-rapidapi-host: trueway-places.p.rapidapi.com" \
  -H "x-rapidapi-key: YOUR_RAPID_API_KEY"
```

---

## 🚀 Future Enhancements

* Real-time open/close status from Google API
* Map UI integration for displaying routes
* Support for filtering by rating or hours

---

## 🚀 Author

**Waris Amir**
Backend & Full-Stack Developer
