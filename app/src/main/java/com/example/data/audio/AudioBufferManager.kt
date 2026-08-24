package com.example.data.audio

import java.util.concurrent.ConcurrentLinkedQueue

class AudioBufferManager(private val maxCapacity: Int = 100) {

    private val queue = ConcurrentLinkedQueue<ByteArray>()

    fun enqueue(chunk: ByteArray) {
        if (queue.size >= maxCapacity) {
            queue.poll() // Drop oldest on backpressure to maintain low latency
        }
        queue.offer(chunk)
    }

    fun dequeue(): ByteArray? {
        return queue.poll()
    }

    fun clear() {
        queue.clear()
    }

    fun isEmpty(): Boolean = queue.isEmpty()

    fun size(): Int = queue.size
}
