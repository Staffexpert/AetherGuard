package com.aetherguard.api;

import com.aetherguard.core.AetherGuard;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * REST API for AetherGuard - Premium integration API
 * Provides endpoints for external integrations, monitoring, and management
 */
public class RestAPI {
    private final AetherGuard plugin;
    private final Gson gson = new Gson();
    private HttpServer server;
    private final int port;

    public RestAPI(AetherGuard plugin, int port) {
        this.plugin = plugin;
        this.port = port;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newCachedThreadPool());

            // Register endpoints
            server.createContext("/api/v1/status", new StatusHandler());
            server.createContext("/api/v1/players", new PlayersHandler());
            server.createContext("/api/v1/violations", new ViolationsHandler());
            server.createContext("/api/v1/checks", new ChecksHandler());

            server.start();
            plugin.getLogger().info("§aREST API started on port " + port);

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start REST API: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("§cREST API stopped");
        }
    }

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, Object> status = new HashMap<>();
                status.put("version", plugin.getDescription().getVersion());
                status.put("online", true);
                status.put("tps", plugin.getLastTPS());
                status.put("memory", plugin.getMemoryUsagePercentage());
                status.put("players", Bukkit.getOnlinePlayers().size());
                status.put("checks", plugin.getCheckManager().getTotalChecks());

                sendJSONResponse(exchange, status, 200);
            } else {
                sendJSONResponse(exchange, Map.of("error", "Method not allowed"), 405);
            }
        }
    }

    private class PlayersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                // Simplified player list
                Map<String, Object> response = new HashMap<>();
                response.put("count", Bukkit.getOnlinePlayers().size());
                List<Map<String, String>> players = new java.util.ArrayList<>();
                for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                    Map<String, String> playerData = new HashMap<>();
                    playerData.put("name", player.getName());
                    playerData.put("uuid", player.getUniqueId().toString());
                    players.add(playerData);
                }
                response.put("players", players);

                sendJSONResponse(exchange, response, 200);
            } else {
                sendJSONResponse(exchange, Map.of("error", "Method not allowed"), 405);
            }
        }
    }

    private class ViolationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, Object> response = new HashMap<>();
                response.put("total", 0); // Simplified
                response.put("recent", new java.util.ArrayList<>()); // Simplified

                sendJSONResponse(exchange, response, 200);
            } else {
                sendJSONResponse(exchange, Map.of("error", "Method not allowed"), 405);
            }
        }
    }

    private class ChecksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, Object> response = new HashMap<>();
                response.put("total", plugin.getCheckManager().getTotalChecks());
                response.put("enabled", plugin.getCheckManager().getEnabledChecksCount());
                response.put("categories", plugin.getCheckManager().getAllCategories());

                sendJSONResponse(exchange, response, 200);
            } else {
                sendJSONResponse(exchange, Map.of("error", "Method not allowed"), 405);
            }
        }
    }

    private void sendJSONResponse(HttpExchange exchange, Object data, int statusCode) throws IOException {
        String json = gson.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }
}