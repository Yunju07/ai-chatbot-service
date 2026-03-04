package com.sionic.ai.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import com.sionic.ai.config.OpenAiProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import org.slf4j.LoggerFactory

@Component
@ConditionalOnProperty(prefix = "app.openai", name = ["enabled"], havingValue = "true")
class OpenAiClient(
    private val openAiProperties: OpenAiProperties,
    private val objectMapper: ObjectMapper,
) : AiClient {

    private val httpClient: HttpClient = HttpClient.newHttpClient()
    private val logger = LoggerFactory.getLogger(OpenAiClient::class.java)
    private val completionsUrl = openAiProperties.baseUrl.trimEnd('/') + "/v1/chat/completions"

    override fun generateAnswer(model: String, question: String, history: List<Pair<String, String>>): String {
        return runCatching {
            if (openAiProperties.apiKey.isBlank()) {
                error("OPENAI_API_KEY is empty")
            }

            val requestJson = buildRequestJson(model, question, history, stream = false)
            val request = HttpRequest.newBuilder(URI.create(completionsUrl))
                .header("Authorization", "Bearer ${openAiProperties.apiKey}")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            if (response.statusCode() !in 200..299) {
                error("OpenAI HTTP ${response.statusCode()}: ${response.body().take(300)}")
            }

            parseNonStreamingContent(response.body())
        }.getOrElse { ex ->
            logger.warn("OpenAI non-stream call failed; fallback to mock response: {}", ex.message)
            mockAnswer(model, question)
        }
    }

    override fun generateAnswerStream(model: String, question: String, history: List<Pair<String, String>>): Sequence<String> {
        return sequence {
            try {
                if (openAiProperties.apiKey.isBlank()) {
                    error("OPENAI_API_KEY is empty")
                }

                val requestJson = buildRequestJson(model, question, history, stream = true)
                val request = HttpRequest.newBuilder(URI.create(completionsUrl))
                    .header("Authorization", "Bearer ${openAiProperties.apiKey}")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build()

                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
                if (response.statusCode() !in 200..299) {
                    val err = response.body().bufferedReader(StandardCharsets.UTF_8).readText()
                    error("OpenAI HTTP ${response.statusCode()}: ${err.take(300)}")
                }

                var emitted = false
                response.body().bufferedReader(StandardCharsets.UTF_8).use { reader ->
                    for (rawLine in reader.lineSequence()) {
                        val line = rawLine.trim()
                        if (!line.startsWith("data:")) continue

                        val data = line.removePrefix("data:").trim()
                        if (data == "[DONE]" || data.isBlank()) continue

                        val json = objectMapper.readTree(data)
                        val delta = json.path("choices").path(0).path("delta").path("content").asText("")
                        if (delta.isNotBlank()) {
                            emitted = true
                            yield(delta)
                        }
                    }
                }

                if (!emitted) {
                    error("OpenAI stream returned no content chunks")
                }
            } catch (ex: Exception) {
                logger.warn("OpenAI stream call failed; fallback to mock stream: {}", ex.message)
                yieldAll(mockStream(model, question))
            }
        }
    }

    private fun buildRequestJson(
        model: String,
        question: String,
        history: List<Pair<String, String>>,
        stream: Boolean,
    ): String {
        val messages = buildList {
            add(mapOf("role" to "system", "content" to "You are a concise and helpful assistant."))
            history.forEach { (q, a) ->
                add(mapOf("role" to "user", "content" to q))
                add(mapOf("role" to "assistant", "content" to a))
            }
            add(mapOf("role" to "user", "content" to question))
        }

        val body = mutableMapOf<String, Any>(
            "model" to model,
            "messages" to messages,
            "temperature" to 0.3,
        )
        if (stream) {
            body["stream"] = true
        }
        return objectMapper.writeValueAsString(body)
    }

    private fun parseNonStreamingContent(raw: String): String {
        val response: JsonNode = objectMapper.readTree(raw)
        val content = response.path("choices")
            .path(0)
            .path("message")
            .path("content")
            .asText("")
            .trim()

        if (content.isBlank()) {
            throw IllegalStateException("OpenAI response content is empty")
        }
        return content
    }

    private fun mockAnswer(model: String, question: String): String {
        return "[mock:$model] $question"
    }

    private fun mockStream(model: String, question: String): Sequence<String> {
        return mockAnswer(model, question).chunked(10).asSequence()
    }
}
