package com.siroha.core.common.di

import javax.inject.Qualifier

/**
 * Qualifiers so Hilt can inject the right CoroutineDispatcher without the
 * caller needing to know which one — and so tests can swap in a
 * TestDispatcher by binding the same qualifier.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher
