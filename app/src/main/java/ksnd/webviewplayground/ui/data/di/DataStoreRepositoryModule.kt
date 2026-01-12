package ksnd.webviewplayground.ui.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ksnd.webviewplayground.ui.data.DataStoreRepository
import ksnd.webviewplayground.ui.data.DataStoreRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreRepositoryModule {
    @Singleton
    @Binds
    abstract fun bindDataStoreRepository(impl: DataStoreRepositoryImpl): DataStoreRepository
}
