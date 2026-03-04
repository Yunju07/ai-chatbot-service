package com.sionic.ai.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app.openai", name = ["enabled"], havingValue = "false", matchIfMissing = true)
class MockAiClient : AiClient {
    override fun generateAnswer(model: String, question: String, history: List<Pair<String, String>>): String {
        return "[mock:$model] $question"
    }

    override fun generateAnswerStream(model: String, question: String, history: List<Pair<String, String>>): Sequence<String> {
        val full = generateAnswer(model, question, history)
        return full.chunked(10).asSequence()
    }
}
