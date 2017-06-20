package org.zella.neuralstyle

import java.nio.file.Path

/**
  * @author zella.
  */
trait IComputeJob {

  type Reason = String

  def runSync(): Either[Reason,Path]
}
