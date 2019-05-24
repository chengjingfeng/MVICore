package com.badoo.mvicore

class ModelWatcher<T> private constructor(
    private val watchers: List<Watcher<T, Any?>>
) {
    private var model: T? = null

    operator fun invoke(value: T) {
        val state = this.state
        watchers.forEach { element ->
            val getter = element.accessor
            val new = getter(value)
            if (state == null || !element.diffStrategy(getter(state), new)) {
                element.callback(new)
            }
        }

        this.state = value
    }

    private class Watcher<T, R>(
        val accessor: (T) -> R,
        val callback: (R) -> Unit,
        val diffStrategy: (R?, R) -> Boolean
    )

    class Builder<T> @PublishedApi internal constructor() {
        private val watchers = mutableListOf<Watcher<T, Any?>>()

        fun <R> watch(
            accessor: (T) -> R,
            diff: DiffStrategy<R> = byValue(),
            callback: (R) -> Unit
        ) {
            watchers += Watcher(
                accessor,
                callback,
                diff
            ) as Watcher<T, Any?>
        }

        @PublishedApi
        internal fun build(): ModelWatcher<T> =
            ModelWatcher(watchers)
    }
}


inline fun <T> modelWatcher(init: ModelWatcher.Builder<T>.() -> Unit): ModelWatcher<T> =
    ModelWatcher.Builder<T>()
        .apply(init)
        .build()
