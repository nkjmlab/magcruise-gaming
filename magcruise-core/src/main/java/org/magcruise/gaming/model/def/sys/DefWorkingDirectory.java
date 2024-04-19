package org.magcruise.gaming.model.def.sys;

import java.nio.file.Path;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefWorkingDirectory implements DefGameSystemProperty {

  private Path path;

  public DefWorkingDirectory(Path path) {
    this.path = path;
  }

  @Override
  public SConstructor<? extends DefWorkingDirectory> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), path);
  }

  public Path getPath() {
    return path;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
