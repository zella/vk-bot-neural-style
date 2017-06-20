package org.zella.neuralstyle.impl

import java.nio.file.{Files, Path}

import com.typesafe.scalalogging.Logger
import org.zella.neuralstyle.IComputeJob

/**
  * @author zella.
  */
case class ComputeFastStyleJob(inputImage: Path, model: Path, fastStylePath: Path, torchExe: Path) extends IComputeJob {

  val log = Logger[ComputeFastStyleJob]

  override def runSync(): Either[Reason, Path] = {

    import sys.process._

    val outImagePath = inputImage.getParent.resolve("out_" + inputImage.getFileName)

    //TODO naming insertions, like victorina phrases bot. Additional params should be in conf

    val cmd = Process(s"$torchExe fast_neural_style.lua " +
      s"-model $model " +
      s"-input_image $inputImage " +
      s"-output_image $outImagePath",
      fastStylePath.toFile)

    //    val cmd = s"cd ${fastStylePath.toAbsolutePath} && ${torchExe.toAbsolutePath.toFile} " +
    //      s"fast_neural_style.lua " +
    //      s"-model $model " +
    //      s"-input_image ${inputImage.toAbsolutePath} " +
    //      s"-output_image ${inputImage.getParent.resolve("out_" + inputImage.getFileName).toAbsolutePath}"
    log.debug(cmd.toString)

    //TODO https://stackoverflow.com/questions/5563439/grabbing-process-stderr-in-scala

    //. ~/torch/install/bin/torch-activate if th not found

    //FIXME cd
    val cmdResponse = cmd.!!

    log.debug(cmdResponse)

    if (!Files.exists(outImagePath) || Files.size(outImagePath) == 0) {
      log.error("File not exist")
      Left("File not exist")
    } else
      Right(outImagePath)
  }
}
