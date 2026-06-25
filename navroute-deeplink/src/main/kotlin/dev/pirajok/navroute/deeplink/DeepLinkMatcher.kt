package dev.pirajok.navroute.deeplink

import dev.pirajok.navroute.runtime.NavRouteDestination
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException

public class DeepLinkMatcher<T : NavRouteDestination>(
    private val request: DeepLinkRequest,
    private val deepLinkPattern: DeepLinkPattern<T>,
) {

    public fun match(): DeepLinkMatchResult<T>? {
        if (request.uri.scheme != deepLinkPattern.uriPattern.scheme) return null
        if (!request.uri.authority.equals(deepLinkPattern.uriPattern.authority, ignoreCase = true)) return null
        if (request.pathSegments.size != deepLinkPattern.pathSegments.size) return null

        if (request.uri == deepLinkPattern.uriPattern) {
            return DeepLinkMatchResult(deepLinkPattern.serializer, emptyMap())
        }

        val arguments = mutableMapOf<String, Any>()

        request.pathSegments
            .asSequence()
            .zip(deepLinkPattern.pathSegments.asSequence())
            .forEach { (requestedSegment, candidateSegment) ->
                if (candidateSegment.isArgument) {
                    val parsedValue = try {
                        candidateSegment.typeParser.invoke(requestedSegment)
                    } catch (_: IllegalArgumentException) {
                        return null
                    }
                    arguments[candidateSegment.stringValue] = parsedValue
                } else if (requestedSegment != candidateSegment.stringValue) {
                    return null
                }
            }

        request.queries.forEach { (name, value) ->
            val queryStringParser = deepLinkPattern.queryValueParsers[name] ?: return@forEach
            val queryParsedValue = try {
                queryStringParser.invoke(value)
            } catch (_: IllegalArgumentException) {
                return null
            }
            arguments[name] = queryParsedValue
        }

        return DeepLinkMatchResult(deepLinkPattern.serializer, arguments)
    }
}

public data class DeepLinkMatchResult<T : NavRouteDestination>(
    public val serializer: KSerializer<T>,
    public val arguments: Map<String, Any>,
) {
    public fun toRoute(): T =
        serializer.deserialize(KeyDecoder(arguments))

    public fun toRouteOrNull(): T? =
        try {
            toRoute()
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: IllegalStateException) {
            null
        } catch (_: SerializationException) {
            null
        }
}
