package com.turnzero.di;

import com.turnzero.core.tools.api.Game;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class GamesModule_ProvideYuGiOhGameFactory implements Factory<Game> {
  @Override
  public Game get() {
    return provideYuGiOhGame();
  }

  public static GamesModule_ProvideYuGiOhGameFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static Game provideYuGiOhGame() {
    return Preconditions.checkNotNullFromProvides(GamesModule.INSTANCE.provideYuGiOhGame());
  }

  private static final class InstanceHolder {
    private static final GamesModule_ProvideYuGiOhGameFactory INSTANCE = new GamesModule_ProvideYuGiOhGameFactory();
  }
}
