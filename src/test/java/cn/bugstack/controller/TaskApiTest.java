package cn.bugstack.controller;

import cn.bugstack.TaskApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TaskApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class TaskApiTest {
    private static final String BASE = "/api/tasks";
    private static final String FUTURE_DUE_DATE = LocalDateTime.now().plusDays(30).withNano(0)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final String UPDATED_DUE_DATE = LocalDateTime.now().plusDays(60).withNano(0)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    private static final String CREATE_JSON = """
            {"title":"First","description":"Keep this","projectId":"00000000-0000-0000-0000-000000000001",
             "priority":"HIGH","dueDate":"%s"}
            """.formatted(FUTURE_DUE_DATE);

    @LocalServerPort
    private int port;

    private final JsonMapper json = new JsonMapper();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final List<String> createdPaths = new ArrayList<>();

    @AfterEach
    void cleanup() throws Exception {
        try {
            for (String path : createdPaths) send("DELETE", path, null);
        } finally {
            client.close();
        }
    }

    @Test
    void createsReadsListsPatchesAndDeletesOverHttp() throws Exception {
        Result created = createTask();
        assertEquals(201, created.status());
        String path = created.location();
        assertEquals(BASE + "/" + created.body().get("id").asText(), path);
        assertEquals("TODO", created.body().get("status").asText());
        assertTrue(created.body().get("assigneeId").isNull());
        assertEquals(created.body(), expect("GET", path, null, 200).body());

        JsonNode list = expect("GET", BASE, null, 200).body();
        assertTrue(list.isArray());
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).get("id").equals(created.body().get("id"))) found = true;
        }
        assertTrue(found, "Created task must appear in the list");

        Result patched = expect("PATCH", path, "{\"title\":\"Second\"}", 200);
        assertEquals("Second", patched.body().get("title").asText());
        for (String field : List.of("description", "priority", "projectId", "dueDate", "createdAt")) {
            assertEquals(created.body().get(field), patched.body().get(field), field);
        }
        Result edited = expect("PATCH", path,
                "{\"description\":\"Changed\",\"priority\":\"URGENT\",\"dueDate\":\"%s\"}".formatted(UPDATED_DUE_DATE), 200);
        assertEquals("Changed", edited.body().get("description").asText());
        assertEquals("URGENT", edited.body().get("priority").asText());
        assertEquals(UPDATED_DUE_DATE, edited.body().get("dueDate").asText());
        assertEquals(edited.body(), expect("GET", path, null, 200).body());

        assertEquals("", expect("DELETE", path, null, 204).rawBody());
        expect("GET", path, null, 404);
        JsonNode remaining = expect("GET", BASE, null, 200).body();
        for (int i = 0; i < remaining.size(); i++) {
            assertNotEquals(created.body().get("id"), remaining.get(i).get("id"));
        }
    }

    @Test
    void returnsNotFoundForMissingTasks() throws Exception {
        String path = BASE + "/" + UUID.randomUUID();
        assertEquals(404, expect("GET", path, null, 404).body().get("status").asInt());
        expect("PATCH", path, "{\"title\":\"Missing\"}", 404);
        expect("DELETE", path, null, 404);
    }

    @Test
    void rejectsInvalidRequestsWithoutCreatingTasks() throws Exception {
        int before = expect("GET", BASE, null, 200).body().size();
        for (String payload : List.of(
                "{}", "null", "{invalid", "",
                CREATE_JSON.replace("First", "   "),
                CREATE_JSON.replace("HIGH", "UNKNOWN"),
                CREATE_JSON.replace(FUTURE_DUE_DATE, "not-a-date"),
                CREATE_JSON.replace("00000000-0000-0000-0000-000000000001", "not-a-uuid"))) {
            assertEquals(400, expect("POST", BASE, payload, 400).body().get("status").asInt());
        }
        assertEquals(before, expect("GET", BASE, null, 200).body().size());
        expect("GET", BASE + "/not-a-uuid", null, 400);
    }

    @Test
    void invalidPatchIsAtomicAndNullOrOmittedFieldsAreUnchanged() throws Exception {
        Result created = createTask();
        String path = created.location();
        for (String payload : List.of(
                "{\"title\":\"   \",\"description\":\"Must not persist\"}",
                "{\"title\":\"Must not persist\",\"priority\":\"UNKNOWN\"}",
                "{\"dueDate\":\"invalid\"}", "null")) {
            expect("PATCH", path, payload, 400);
            assertEquals(created.body(), expect("GET", path, null, 200).body());
        }
        assertEquals(created.body(), expect("PATCH", path, "{}", 200).body());
        assertEquals(created.body(), expect("PATCH", path,
                "{\"title\":null,\"description\":null,\"priority\":null,\"dueDate\":null}", 200).body());
    }

    @Test
    void existingStateActionsStillUseTheTaskRepository() throws Exception {
        String path = createTask().location();
        expect("POST", path + "/complete", null, 400);
        assertEquals("IN_PROGRESS", expect("POST", path + "/start", null, 200).body().get("status").asText());
        assertEquals("COMPLETED", expect("POST", path + "/complete", null, 200).body().get("status").asText());
        expect("POST", path + "/cancel", null, 400);
        assertTrue(expect("GET", path, null, 200).body().get("completedAt").isTextual());
        String cancellable = createTask().location();
        assertEquals("CANCELLED", expect("POST", cancellable + "/cancel", null, 200).body().get("status").asText());
    }

    @Test
    void validatesLengthsBlankTitlesAndPastDatesBeforeAnyMutation() throws Exception {
        Result created = createTask();
        int count = expect("GET", BASE, null, 200).body().size();
        String past = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        for (InvalidField invalid : List.of(
                new InvalidField("title", "", "title must not be blank"),
                new InvalidField("title", " \t\n", "title must not be blank"),
                new InvalidField("title", "\u3000", "title must not be blank"),
                new InvalidField("title", "x".repeat(101), "title must be at most 100 characters"),
                new InvalidField("description", "x".repeat(2001), "description must be at most 2000 characters"),
                new InvalidField("dueDate", past, "dueDate must be in the future"))) {
            Result rejectedCreate = expect("POST", BASE, createWith(invalid.field(), invalid.value()), 400);
            assertTrue(rejectedCreate.body().get("detail").asText().contains(invalid.message()));
            // A valid title plus another invalid field must not partially update the task.
            String patch = json.createObjectNode().put("title", "Must not persist")
                    .put(invalid.field(), invalid.value()).toString();
            Result rejectedPatch = expect("PATCH", created.location(), patch, 400);
            assertTrue(rejectedPatch.body().get("detail").asText().contains(invalid.message()));
            assertEquals(created.body(), expect("GET", created.location(), null, 200).body());
        }
        assertEquals(count, expect("GET", BASE, null, 200).body().size());
        expect("POST", BASE, createWith("title", null), 400);
        expect("POST", BASE, createWith("priority", null), 400);
    }

    @Test
    void rejectsInvalidPriorityAndDateRepresentationsOnBothEndpoints() throws Exception {
        Result created = createTask();
        int count = expect("GET", BASE, null, 200).body().size();
        String impossibleDate = (LocalDateTime.now().getYear() + 1) + "-02-30T12:00:00";
        for (String patch : List.of(
                "{\"priority\":\"UNKNOWN\"}", "{\"priority\":\"high\"}",
                "{\"priority\":1}", "{\"priority\":\"1\"}", "{\"priority\":\"\"}",
                "{\"dueDate\":\"not-a-date\"}", "{\"dueDate\":\"\"}",
                "{\"dueDate\":\"" + impossibleDate + "\"}")) {
            // Replace the matching field rather than submitting duplicate JSON keys.
            var createBody = json.createObjectNode()
                    .put("title", "First").put("projectId", "00000000-0000-0000-0000-000000000001")
                    .put("priority", "HIGH").put("dueDate", FUTURE_DUE_DATE);
            var invalid = json.readTree(patch);
            if (invalid.has("priority")) createBody.set("priority", invalid.get("priority"));
            if (invalid.has("dueDate")) createBody.set("dueDate", invalid.get("dueDate"));
            expect("POST", BASE, createBody.toString(), 400);
            expect("PATCH", created.location(), patch, 400);
            assertEquals(created.body(), expect("GET", created.location(), null, 200).body());
        }
        assertEquals(count, expect("GET", BASE, null, 200).body().size());
    }

    @Test
    void acceptsMaximumLengthsOptionalValuesAndAllNamedPriorities() throws Exception {
        String body = json.createObjectNode().put("title", "x".repeat(100))
                .put("description", "x".repeat(2000))
                .put("projectId", "00000000-0000-0000-0000-000000000001")
                .put("priority", "LOW").putNull("dueDate").toString();
        Result created = expect("POST", BASE, body, 201);
        createdPaths.add(created.location());
        assertEquals(100, created.body().get("title").asText().length());
        assertEquals(2000, created.body().get("description").asText().length());
        assertTrue(created.body().get("dueDate").isNull());
        expect("PATCH", created.location(), json.createObjectNode().put("title", "y".repeat(100))
                .put("description", "y".repeat(2000)).toString(), 200);
        for (String priority : List.of("LOW", "MEDIUM", "HIGH", "URGENT")) {
            assertEquals(priority, expect("PATCH", created.location(),
                    "{\"priority\":\"" + priority + "\"}", 200).body().get("priority").asText());
        }
        assertEquals("", expect("PATCH", created.location(), "{\"description\":\"\"}", 200)
                .body().get("description").asText());
    }

    private String createWith(String field, String value) {
        return json.createObjectNode().put("title", "First").put("description", "Keep this")
                .put("projectId", "00000000-0000-0000-0000-000000000001")
                .put("priority", "HIGH").put("dueDate", FUTURE_DUE_DATE)
                .put(field, value).toString();
    }

    private record InvalidField(String field, String value, String message) {}

    private Result createTask() throws Exception {
        Result result = expect("POST", BASE, CREATE_JSON, 201);
        createdPaths.add(result.location());
        return result;
    }

    private Result expect(String method, String path, String body, int status) throws Exception {
        Result result = send(method, path, body);
        assertEquals(status, result.status(), method + " " + path + " request=" + body + ": " + result.rawBody());
        if (status == 400) {
            assertTrue(result.contentType().startsWith("application/problem+json"));
            assertEquals(400, result.body().get("status").asInt());
            assertEquals("Bad Request", result.body().get("title").asText());
            assertFalse(result.body().get("detail").asText().isBlank());
        }
        return result;
    }

    private Result send(String method, String path, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10));
        if (body != null) request.header("Content-Type", "application/json");
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        return new Result(response.statusCode(), response.body().isBlank() ? null : json.readTree(response.body()),
                response.headers().firstValue("Location").orElse(""), response.body(),
                response.headers().firstValue("Content-Type").orElse(""));
    }

    private record Result(int status, JsonNode body, String location, String rawBody, String contentType) {}
}
