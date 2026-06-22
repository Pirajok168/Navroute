package dev.pirajok.navroute.deeplink

import android.net.Uri

public class DeepLinkRequest(
    public val uri: Uri,
) {
    public val pathSegments: List<String> = uri.pathSegments

    public val queries: Map<String, String> = buildMap {
        uri.queryParameterNames.forEach { argName ->
            uri.getQueryParameter(argName)?.let { value ->
                this[argName] = value
            }
        }
    }
}
