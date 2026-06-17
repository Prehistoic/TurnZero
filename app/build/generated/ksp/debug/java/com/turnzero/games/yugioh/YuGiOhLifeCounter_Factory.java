package com.turnzero.games.yugioh;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class YuGiOhLifeCounter_Factory implements Factory<YuGiOhLifeCounter> {
  @Override
  public YuGiOhLifeCounter get() {
    return newInstance();
  }

  public static YuGiOhLifeCounter_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static YuGiOhLifeCounter newInstance() {
    return new YuGiOhLifeCounter();
  }

  private static final class InstanceHolder {
    private static final YuGiOhLifeCounter_Factory INSTANCE = new YuGiOhLifeCounter_Factory();
  }
}
