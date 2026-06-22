package dev.pirajok.navroute.koin

import android.app.Application
import dev.pirajok.navroute.sample.generation.generated.generatedSampleGenerationDeepLinkPatternProviderKoinModule
import dev.pirajok.navroute.sample.generation.generated.generatedSampleGenerationEntryBuilderKoinModule
import dev.pirajok.navroute.sample.koin.impl.generated.generatedSampleKoinDeepLinkPatternProviderKoinModule
import dev.pirajok.navroute.sample.koin.impl.generated.generatedSampleKoinEntryBuilderKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

public class NavRouteKoinSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NavRouteKoinSampleApplication)
            modules(
                generatedSampleGenerationEntryBuilderKoinModule,
                generatedSampleGenerationDeepLinkPatternProviderKoinModule,
                generatedSampleKoinEntryBuilderKoinModule,
                generatedSampleKoinDeepLinkPatternProviderKoinModule,
            )
        }
    }
}
