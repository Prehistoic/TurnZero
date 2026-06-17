package com.turnzero.tools.dice;

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
public final class DiceTool_Factory implements Factory<DiceTool> {
  @Override
  public DiceTool get() {
    return newInstance();
  }

  public static DiceTool_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DiceTool newInstance() {
    return new DiceTool();
  }

  private static final class InstanceHolder {
    private static final DiceTool_Factory INSTANCE = new DiceTool_Factory();
  }
}
