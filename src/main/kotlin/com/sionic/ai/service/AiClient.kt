package com.sionic.ai.service

interface AiClient {
    fun generateAnswer(model: String, question: String, history: List<Pair<String, String>>): String
    fun generateAnswerStream(model: String, question: String, history: List<Pair<String, String>>): Sequence<String>
}
