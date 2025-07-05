package space.banterbox.core.common.concurrent

import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val aiaDispatcher: AiaDispatchers)

enum class AiaDispatchers {
    Default, Io, Main
}