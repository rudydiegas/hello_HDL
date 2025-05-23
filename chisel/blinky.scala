
// Spicy Macros necessary to make scala-cli work :)

//> using scala "2.13.12"
//> using dep "org.chipsalliance::chisel::6.7.0"
//> using plugin "org.chipsalliance:::chisel-plugin::6.7.0"
//> using options "-unchecked", "-deprecation" "-language:reflectiveCalls" "-feature" "-Xcheckinit" "-Xfatal-warnings" "-Ywarn-dead-code" "-Ywarn-unused" "-Ymacro-annotations"

// Importing the Chisel Library and other modules. Remember,
// we are writing Scala code here :)

import chisel3._
import _root_.circt.stage.ChiselStage
// import chisel3.experimental.BundleLiterals._
// import chisel3.simulator.EphemeralSimulator._
import chisel3.util._


class Blinky_Generator (speed: Int) extends Module {
    val io = IO( new Bundle {
        val LED = Output(UInt(8.W))
    })

    val (_, sec_passed) = Counter(true.B, 25000000)
    val (_, speed_reached) = Counter(sec_passed, speed)
    val (cntr, cntr_inc) = Counter(speed_reached, 256)
    io.LED := cntr
}

object Main extends App {
    println(
        ChiselStage.emitSystemVerilog(
            gen = new Blinky_Generator(1),
            firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
        )
    )
}
