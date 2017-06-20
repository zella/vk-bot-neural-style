package org.zella.conf.impl

import java.nio.file.{Files, Path, Paths}

import org.zella.conf.IConfig

/**
  * @author zella.
  */
class BotConfig(conf: com.typesafe.config.Config) extends IConfig {

  //TODO rework config, potential paths issue
  private val modelsPath = Paths.get(conf.getString("path.models"))

  override val vkGroupId: Int = conf.getInt("vk.groupId")

  override val vkToken: String = conf.getString("vk.token")

  override val vkConfirmationCode: String = conf.getString("vk.confirmationCode")

  override val netPort: Int = conf.getInt("net.port")

  override val pathFastStyle: Path = Paths.get(conf.getString("path.fastStyle"))

  override val pathUploads: Path = Paths.get(conf.getString("path.uploads"))

  override def modelPathById(modelId: String): Option[Path] = {
    val modelPath = modelsPath.resolve(modelId + ".t7")
    if (Files.exists(modelPath)) Some(modelPath) else None
  }

  override val pathTorchExe: Path = Paths.get(conf.getString("path.torchExe"))


  override val textInstruction: String = conf.getString("text.instruction")

  override val textInternalError: String = conf.getString("text.internalError")

  override val textSuccess: String = conf.getString("text.success")
}
