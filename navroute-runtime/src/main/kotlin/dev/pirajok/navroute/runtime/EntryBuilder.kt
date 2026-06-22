package dev.pirajok.navroute.runtime

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

public typealias EntryBuilder = EntryProviderScope<NavKey>.() -> Unit
