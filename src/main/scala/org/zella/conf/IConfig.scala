package org.zella.conf

import java.nio.file.Path

/**
  * @author zella.
  */
trait IConfig {

  def vkGroupId: Int

  def vkToken: String

  def vkConfirmationCode:String

  def textInstruction:String

  def textInternalError:String

  def textSuccess: String

  def netPort: Int

  def pathFastStyle: Path

  def pathUploads:Path

  def pathTorchExe: Path

  def modelPathById(modelId: String): Option[Path]

}
