package com.fbmanager.di

import com.fbmanager.data.firebase.DeviceRepositoryImpl
import com.fbmanager.domain.repository.DeviceRepository
import com.fbmanager.data.firebase.UserRepositoryImpl
import com.fbmanager.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
