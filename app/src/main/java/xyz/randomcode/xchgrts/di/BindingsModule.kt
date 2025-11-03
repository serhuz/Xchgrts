package xyz.randomcode.xchgrts.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.randomcode.xchgrts.domain.util.FlagResourceProvider
import xyz.randomcode.xchgrts.util.DrawableResProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    abstract fun bindsFlagResourceProvider(impl: DrawableResProvider): FlagResourceProvider
}
