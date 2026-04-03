# ticket-booking-system
Project Objective: Build the core engine for envents ticket booking system, focusing on resolving concurrency issues (Race Conditions) when multiple users attempt to book the same seat simultaneously, and managing the automated order lifecycle using State Machine and Timeout mechanisms.
**I'll cover the following:**

* [System Requirements](#system-requirements)
* [Use Case Diagram](#use-case-diagram)
* [Class Diagram](#class-diagram)
* [Activity Diagrams](#activity-diagrams)
* [Code](#code)
* [Concurrency](#concurrency)

### System Requirements 

## 1. Functional Requirements

### 1.1. Guest
* **Browse & Search:** View and search for available events.
* **Authentication:** Requires user login or account creation to proceed with ticket purchases.

### 1.2. Customer
* **Search & Discover:** Search for events by name, location, time, and category.
* **Booking:**
  * **Real-time Seat Map:** View seat availability updated in real-time.
  * **Seat Locking:** Select seats and apply a temporary hold (lock) for 5-10 minutes during the checkout process.
* **Payment Integration:** Process payments via integrated gateways (VNPAY/MoMo). Automatically handle refunds for failed transactions.
* **E-Ticket:** Automatically generate a unique QR code for each ticket upon successful payment.
* **History & Notifications:** Manage purchased tickets on the web portal and receive order confirmation notifications via Email.

### 1.3. Event Organizer
* **Event Management:** Create and edit event details, and submit them to the system admin for approval.
* **Seat Map & Pricing Configuration:** Set up ticket tiers (VIP, Standard, Early Bird) and configure flexible seating layouts.
* **Staff Management:** Create accounts and assign roles for Staff (Ticket Inspectors) to assist at events.
* **Analytics:** View dashboards for revenue, seat occupancy rates, and ticket sales velocity for their managed events.

### 1.4. Staff (Ticket Inspector)
* **Check-in:** Use a mobile device to scan customer E-Ticket QR codes.
* **Ticket Lookup:** Verify ticket status (Used/Unused) and owner information to handle gate issues. Access scanning history to prevent duplicate entry errors.

### 1.5. System Admin
* **Event Approval:** Review and approve events created by Organizers before publishing them for public sale.
* **Partner (Organizer) Management:** Approve, temporarily suspend, or delete Organizer accounts.
* **System Configuration:** Configure platform commission fees, manage categories, and set security parameters (e.g., Anti-bot thresholds).
* **Comprehensive Reporting:** Monitor total cash flow, overall traffic, and system-wide performance.

### 1.6. System Internals (Microservices & Kafka)
* **Message Queuing:** Offload heavy tasks (e.g., sending emails with QR codes, ticket generation, bot behavior logging) to Kafka queues for asynchronous processing, ensuring the customer payment flow is never blocked.
* **Real-time Synchronization (WebSockets):** When a seat is locked or successfully purchased, the state change must be pushed immediately to all clients currently viewing that specific seat map, without requiring a page refresh.

### 1.7. Reporting & Monitoring (Admin/Developer)
* **Monitoring Dashboard:** Display real-time metrics using Prometheus/Grafana, including requests/second, Microservices node status, and resource consumption (CPU/RAM).
* **Distributed Tracing:** Allow Admins/Developers to trace the journey of a ticket booking request across multiple services for debugging purposes.

---

## 2. Non-Functional Requirements

### 2.1. Performance & Scalability
* **High Concurrency:** The system must handle concurrent seat holding requests for at least 2,000 - 5,000 users (at localhost scale) without double-booking (overselling).
* **Response Time:** Critical APIs (e.g., View Seat Map, Lock Seat) must have a response time of `< 200ms`.

### 2.2. Security & Anti-Bot
* **Authentication & Authorization:** Utilize JWT combined with Spring Security to enforce Role-Based Access Control (RBAC).
* **Multi-layer Anti-bot:**
  * **Rate Limiting:** Block IP addresses that exceed a predefined request threshold.
  * **Honeypot:** Implement hidden honeypot fields on forms to detect and block automated scripts.
  * **Captcha:** Apply reCAPTCHA v3 for critical transactions (e.g., payment, login).
* **Bypass Mechanism:** Provide a bypass mechanism via custom Headers strictly to facilitate Load Testing.

### 2.3. Data Integrity
* **Distributed Locking:** Utilize Redis to ensure seat state consistency across a distributed environment.
* **Idempotency:** Guarantee that every payment transaction is processed exactly once, even if the user clicks the submit button multiple times.

### 2.4. Observability & DevOps
* **Monitoring:** The system must expose a `/metrics` endpoint (via Spring Boot Actuator) for Prometheus data scraping.
* **Logging:** Centralize all logs to track and analyze automated attack attempts (e.g., Honeypot triggers).
* **Containerization:** All services (Backend, Frontend, Redis, Kafka, Database) must be containerized using Docker to ensure consistency between development, testing, and production environments.


### Use Case Diagram

<img width="1262" height="2361" alt="usecase-v5-eng" src="https://github.com/user-attachments/assets/94812f26-5695-4d9f-b354-e727a2d8737d" />


