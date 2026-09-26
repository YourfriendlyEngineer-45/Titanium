package titanium;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

// ============================================================
// THE 4 ATOMS - ALL IN ONE FILE
// ============================================================

// ATOM 1: THE ROUTER
class Titanium {
    private static final Map<String, Function<Request, Response>> routes = new ConcurrentHashMap<>();

    public static void get(String path, Function<Request, Response> handler) {
        routes.put("GET " + path, handler);
    }

    public static void post(String path, Function<Request, Response> handler) {
        routes.put("POST " + path, handler);
    }

    public static void put(String path, Function<Request, Response> handler) {
        routes.put("PUT " + path, handler);
    }

    public static void delete(String path, Function<Request, Response> handler) {
        routes.put("DELETE " + path, handler);
    }

    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/", exchange -> {
            try {
                handleRequest(exchange);
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.start();
        System.out.println("[TITANIUM] Reactor Core Online. Port " + port + " | Virtual Threads: ACTIVE");
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String key = method + " " + path;

        Function<Request, Response> handler = routes.get(key);
        if (handler == null) {
            sendResponse(exchange, 404, "{\"error\":\"Not Found\"}");
            return;
        }

        Request req = new Request(exchange);
        Response res = handler.apply(req);
        sendResponse(exchange, res.status, res.body);
    }

    private static void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = body.getBytes("UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}

// ATOM 2: THE INPUT
class Request {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final HttpExchange exchange;
    private final String body;
    private final Map<String, String> headers;
    private JsonNode json;

    public Request(HttpExchange exchange) throws IOException {
        this.exchange = exchange;
        this.body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        this.headers = new HashMap<>();
        exchange.getRequestHeaders().forEach((k, v) -> headers.put(k.toLowerCase(), v.get(0)));

        if (!body.isEmpty()) {
            try { this.json = mapper.readTree(body); }
            catch (Exception e) { this.json = null; }
        }
    }

    public String body() { return body; }

    public JsonNode json() { return json; }

    public String jsonField(String field) {
        if (json == null || !json.has(field)) return null;
        return json.get(field).asText();
    }

    public String header(String name) { return headers.get(name.toLowerCase()); }

    public String param(String name) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(name)) return kv[1];
        }
        return null;
    }

    public String cookie(String name) {
        String cookieHeader = header("cookie");
        if (cookieHeader == null) return null;
        for (String cookie : cookieHeader.split(";")) {
            String[] kv = cookie.trim().split("=");
            if (kv.length == 2 && kv[0].equals(name)) return kv[1];
        }
        return null;
    }
}

// ATOM 3: THE OUTPUT
class Response {
    private static final ObjectMapper mapper = new ObjectMapper();
    int status;
    String body;

    private Response(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public static Response json(String rawJson) {
        return new Response(200, rawJson);
    }

    public static Response json(String rawJson, int status) {
        return new Response(status, rawJson);
    }

    public static Response obj(Object obj) {
        try {
            return new Response(200, mapper.writeValueAsString(obj));
        } catch (Exception e) {
            return new Response(500, "{\"error\":\"Serialization failed\"}");
        }
    }

    public static Response ok(String message) {
        return new Response(200, "{\"status\":\"ok\",\"message\":\"" + message + "\"}");
    }

    public static Response error(String message, int status) {
        return new Response(status, "{\"error\":\"" + message + "\"}");
    }

    public static Response error(String message) {
        return error(message, 400);
    }
}

// ATOM 4: THE DATABASE
class DB {
    private static String url;
    private static String user;
    private static String pass;

    public static void init(String jdbcUrl, String username, String password) {
        url = jdbcUrl;
        user = username;
        pass = password;
    }

    public static List<Map<String, Object>> query(String sql, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= cols; i++) row.put(meta.getColumnName(i), rs.getObject(i));
                results.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB Query Failed: " + e.getMessage());
        }
        return results;
    }

    public static int update(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB Update Failed: " + e.getMessage());
        }
    }

    public static long insert(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            return rs.next() ? rs.getLong(1) : -1;
        } catch (SQLException e) {
            throw new RuntimeException("DB Insert Failed: " + e.getMessage());
        }
    }
}

// ============================================================
// YOUR APP - THE PUBLIC CLASS (Must match filename: Main.java)
// ============================================================

public class Main {
    public static void main(String[] args) throws Exception {

        DB.init(
            System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/copper_app"),
            System.getenv().getOrDefault("DB_USER", "root"),
            System.getenv().getOrDefault("DB_PASS", "")
        );

        Titanium.get("/health", req -> Response.ok("alive"));

        Titanium.get("/api/users", req -> {
            List<Map<String, Object>> users = DB.query("SELECT * FROM users LIMIT 50");
            return Response.obj(users);
        });

        Titanium.post("/api/users", req -> {
            String email = req.jsonField("email");
            if (email == null || email.isEmpty()) return Response.error("email required");
            long id = DB.insert("INSERT INTO users (email) VALUES (?)", email);
            return Response.json("{\"id\":" + id + ",\"email\":\"" + email + "\"}", 201);
        });

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Titanium.start(port);
    }
}
