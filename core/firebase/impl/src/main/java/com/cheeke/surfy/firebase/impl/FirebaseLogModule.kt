package com.cheeke.surfy.firebase.impl

import com.cheeke.surfy.firebase.api.LogHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(value = [SingletonComponent::class])
abstract class FirebaseLogModule {
    @Binds
    abstract fun bindFirebaseLogHelper(
        surfyFirebaseLogHelper: SurfyFirebaseLogHelper,
    ): LogHelper
}