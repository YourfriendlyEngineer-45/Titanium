# 🔷 TITANIUM
### The 4-Atom Backend Framework for the Sovereign Solopreneur
**Element 22 OPC • Oracle Cloud Infrastructure • JDK 21**

## ⚛️ WHAT IS TITANIUM?
Titanium is a **1-file, 4-atom backend framework** built for developers who refuse to pay the AWS Tax, drown in Spring Boot annotations, or surrender their sovereignty to bloated cloud platforms. The 4 Atoms are: **Router** → `Titanium.get()`, `Titanium.post()` (Raw HTTP), **Input** → `req.jsonField()`, `req.header()` (Request parsing), **Output** → `Response.json()`, `Response.error()` (Response formatting), **Database** → `DB.query()`, `DB.insert()` (Raw SQL). That's it. No magic. No proxies. No 500MB dependencies. Just physics.

## 🎯 WHY TITANIUM?
Spring Boot has 500MB RAM idle, 4,000ms boot time, 47+ concepts (Beans, AOP, Annotations), $200/month AWS bill, and requires Kubernetes + Docker + CI/CD. Titanium has **40MB RAM idle**, **300ms boot time**, **4 Atoms (Router, Input, Output, DB)**, **$0/month OCI Free Tier**, and runs as **1 Fat JAR (`java -jar`)**. Titanium runs on Oracle Cloud Infrastructure's Always Free ARM instances (24GB RAM, 4 cores). You can host 50+ SaaS apps on a single $0/month server.

## 🚀 QUICK START
Prerequisites: JDK 21 or higher, Maven 3.6+, MySQL 8.0+ (or any JDBC-compatible database). Clone the repository with `git clone https://github.com/element22opc/titanium.git` and `cd titanium`. Configure your database by editing `src/main/java/titanium/Main.java` and setting `DB.init("jdbc:mysql://localhost:3306/copper_app", "root", "your_password");`. Build the reactor with `mvn clean package`. Ignite the core with `java -jar target/titanium-app-1.0.0.jar`. Expected output: `[TITANIUM] Reactor Core Online. Port 8080 | Virtual Threads: ACTIVE`.

## 💻 USAGE EXAMPLES
Basic routes: Health check with `Titanium.get("/health", req -> Response.ok("alive"));`. Get all users with `Titanium.get("/api/users", req -> { List<Map<String, Object>> users = DB.query("SELECT * FROM users LIMIT 50"); return Response.obj(users); });`. Create user with `Titanium.post("/api/users", req -> { String email = req.jsonField("email"); if (email == null || email.isEmpty()) { return Response.error("email required", 400); } long id = DB.insert("INSERT INTO users (email) VALUES (?)", email); return Response.json("{\"id\":" + id + ",\"email\":\"" + email + "\"}", 201); });`. Update user with `Titanium.put("/api/users/{id}", req -> { String id = req.param("id"); String email = req.jsonField("email"); DB.update("UPDATE users SET email = ? WHERE id = ?", email, id); return Response.ok("updated"); });`. Delete user with `Titanium.delete("/api/users/{id}", req -> { String id = req.param("id"); DB.update("DELETE FROM users WHERE id = ?", id); return Response.ok("deleted"); });`.

Request parsing: `Titanium.post("/api/orders", req -> { String email = req.jsonField("email"); JsonNode json = req.json(); String page = req.param("page"); String limit = req.param("limit"); String auth = req.header("Authorization"); String tenant = req.header("X-Tenant-ID"); String session = req.cookie("session_id"); String rawBody = req.body(); return Response.ok("parsed"); });`.

Database operations: SELECT with `List<Map<String, Object>> users = DB.query("SELECT id, email FROM users WHERE status = ?", "active");`. INSERT with `long userId = DB.insert("INSERT INTO users (email, created_at) VALUES (?, NOW())", "user@example.com");`. UPDATE with `int rowsAffected = DB.update("UPDATE users SET status = ? WHERE id = ?", "inactive", 123);`.

## 🏗️ ARCHITECTURE
The 4 Atoms Explained: **Router (Titanium.java)** is built on `com.sun.net.httpserver.HttpServer` (JDK native), uses JDK 21 Virtual Threads for infinite concurrency, has zero external dependencies, and handles GET, POST, PUT, DELETE. **Input (Request.java)** parses HTTP request body as JSON (via Jackson), extracts query parameters, headers, cookies, with no magic, no reflection, just raw byte parsing. **Output (Response.java)** formats Java objects as JSON, sets HTTP status codes and headers, with zero abstraction layers. **Database (DB.java)** uses raw JDBC (`java.sql.*`), no ORM, no Hibernate, no generated SQL—you write the SQL, you control the queries.

## 🌍 DEPLOYMENT TO ORACLE CLOUD
Provision OCI Free Tier Instance by going to Oracle Cloud, creating an Always Free ARM Ampere A1 instance (24GB RAM, 4 cores), and choosing Oracle Linux 8 or Ubuntu 22.04. Install JDK 21 with `sudo dnf install java-21-openjdk`. Install MySQL 8 with `sudo dnf install mysql-server`, `sudo systemctl start mysqld`, and `sudo systemctl enable mysqld`. Create database with `mysql -u root -p`, then `CREATE DATABASE copper_app;` and `CREATE TABLE users (id BIGINT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);`. Deploy the JAR by uploading with `scp target/titanium-app-1.0.0.jar opc@your-server-ip:~/`, SSH with `ssh opc@your-server-ip`, and run with `nohup java -jar titanium-app-1.0.0.jar > app.log 2>&1 &`. Open firewall ports with `sudo firewall-cmd --permanent --add-port=8080/tcp` and `sudo firewall-cmd --reload`. Your Titanium app is now live on a $0/month server with 24GB RAM.

## 🔒 SECURITY
Auth (Do It Yourself): `Titanium.get("/api/protected", req -> { String token = req.header("Authorization"); if (!MyJWT.verify(token)) { return Response.error("Unauthorized", 401); } return Response.ok("secret data"); });`. Rate Limiting (Do It Yourself): `Titanium.post("/api/login", req -> { String ip = req.header("X-Forwarded-For"); if (MyRateLimiter.isBlocked(ip)) { return Response.error("Too Many Requests", 429); } });`. Multi-Tenancy (Do It Yourself): `Titanium.get("/api/invoices", req -> { String tenantId = req.header("X-Tenant-ID"); List<Map<String, Object>> invoices = DB.query("SELECT * FROM invoices WHERE tenant_id = ?", tenantId); return Response.obj(invoices); });`. Titanium gives you the raw atoms. You build the features.

## 📊 PERFORMANCE
Benchmarks (Single OCI Free Tier ARM Instance): Idle RAM is 40MB, boot time is 300ms, requests/second is 15,000+ (simple JSON response), concurrent connections is 100,000+ (JDK 21 Virtual Threads). Compare to Spring Boot: idle RAM 500MB, boot time 4,000ms, requests/second 2,000, concurrent connections 1,000.

## 🛡️ LICENSE
TITANIUM FRAMEWORK LICENSE Version 3.0 — Element 22 OPC. This software is proprietary and closed-source. It is NOT governed by MIT, GPL, Apache, or any open-source license. You May: use Titanium to build and deploy your own applications, modify the source code for your own private, internal use, deploy compiled JARs to servers you own or control. You May Not: redistribute, resell, or sublicense the Software, publish to Maven Central, npm, PyPI, or any package registry, use Titanium to build a competing backend framework or hosting platform, remove or alter this license file. Full License: See `LICENSE` file in the repository.

## 💰 PRICING
Titanium Sole Operator Retainer: **$99/month or $999/year**. Includes: full access to the Titanium source code, private Discord with the Architect and 0.1% hackers, automated `landlord.sh` deployment scripts, OCI architecture blueprints, priority support from the Syndicate. Enterprise Sovereign License: **$30,000/year** for Fortune 500 companies requiring legal indemnification and SLA guarantees.

## 🤝 SUPPORT
For Subscribers: Discord at Element 22 Private Server, email at support@element22.com, response time < 24 hours. For Enterprise Clients: dedicated Slack channel, direct Architect access, SLA guarantee 99.9% uptime.

## 🏴‍☠️ THE MANIFESTO
We believe: the backend should be dangerous but visible, not hidden behind 47 layers of magic; developers should be treated as engineers, not consumers; a 1-file framework can outperform a 50,000-line enterprise suite; $0/month is the only acceptable cloud bill for a solopreneur; sovereignty is worth more than convenience. We do not believe in: built-in ORMs that generate terrible SQL, microservices that multiply your AWS bill, Kubernetes clusters that require a DevOps team, frameworks that update every 6 months and break your code. Titanium is not a framework. It is a declaration of independence.

## 📜 COPYRIGHT
Copyright (c) 2024-2026 Element 22 OPC. All rights reserved. Element 22 OPC is a One Person Corporation registered under the laws of the Republic of the Philippines. Architect: [Aeron Josh O. Lebrilla]. Jurisdiction: Metro Manila, Philippines.

## 🔥 THE REACTOR IS ONLINE
You have the 4 Atoms. You have the OCI Free Tier. You have the physics of HTTP and SQL. Now go build your empire. Run `java -jar titanium-app-1.0.0.jar`. Titanium. Frozen. Sovereign. Unkillable. 🏴‍☠️⚛️🔥💸
