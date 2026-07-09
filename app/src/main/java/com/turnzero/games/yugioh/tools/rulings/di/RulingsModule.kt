package com.turnzero.games.yugioh.tools.rulings.di

import android.content.Context
import androidx.room.Room
import com.turnzero.games.yugioh.tools.rulings.data.RulingsDao
import com.turnzero.games.yugioh.tools.rulings.data.RulingsDatabase
import com.turnzero.games.yugioh.tools.rulings.data.RulingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RulingsModule {

    @Provides
    @Singleton
    fun provideRulingsDatabase(@ApplicationContext context: Context): RulingsDatabase {
        return Room.databaseBuilder(
            context,
            RulingsDatabase::class.java,
            "rulings.db",
        )
            .createFromAsset("databases/rulings.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    fun provideRulingsDao(db: RulingsDatabase): RulingsDao = db.rulingsDao()

    @Provides
    @Singleton
    fun provideRulingsRepository(dao: RulingsDao): RulingsRepository = RulingsRepository(dao)
}
