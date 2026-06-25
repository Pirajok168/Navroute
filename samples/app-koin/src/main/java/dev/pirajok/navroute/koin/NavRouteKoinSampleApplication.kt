package dev.pirajok.navroute.koin

import android.app.Application
import dev.pirajok.navroute.sample.koin.detail.impl.generated.generatedKoinDetailEntryBuilderKoinModule
import dev.pirajok.navroute.sample.koin.main.impl.generated.generatedKoinMainDeepLinkPatternProviderKoinModule
import dev.pirajok.navroute.sample.koin.main.impl.generated.generatedKoinMainEntryBuilderKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

public class NavRouteKoinSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NavRouteKoinSampleApplication)
            modules(
                generatedKoinMainEntryBuilderKoinModule,
                generatedKoinMainDeepLinkPatternProviderKoinModule,
                generatedKoinDetailEntryBuilderKoinModule,
            )
        }
    }
}
