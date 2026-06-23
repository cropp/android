package io.homeassistant.companion.android.common.phoneautomation

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PhoneAutomationModule {

    @Binds
    @Singleton
    abstract fun bindPhoneAutomationRepository(
        impl: PhoneAutomationRepositoryImpl,
    ): PhoneAutomationRepository
}
