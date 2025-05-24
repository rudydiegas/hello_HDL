package chipinterface

import chisel3._
import circt.stage.ChiselStage
import blinky.Blinky       // top module
import scala.sys.process._ // synth commands

/**
 * Generate SV files by running:
 *  ./mill Top.runMain chipinterface.GenerateSV
 *
 * Synthesize design by running:
 *  ./mill Top.runMain chipinterface.Synth
 **/

// Bundle for ULX3S IO
class ULX3S_IO extends Bundle {
  val clk_25mhz = Input(Clock())
  val btn       = Input(UInt(7.W))
  val led       = Output(UInt(8.W))
}

// RawModule to exclude the default clock and reset
class ChipInterface(freq: Int) extends RawModule {
  // FlatIO to remove the _io ending of ports
  val io = FlatIO(new ULX3S_IO)

  // Using an explicit clock and reset domain to connect up the top module
  val blinky = withClockAndReset(io.clk_25mhz, io.btn(3)) {
    Module(new Blinky(freq, io.led.getWidth))
  }

  // Connect it up!
  blinky.io.sel := io.btn(2)
  io.led := blinky.io.led
}

// TODO: add error handling for the shell commands, plus there's
// probably a better way to do this by modifying build.mill instead
// of choosing which main function to run explicitly
object gv {
  def generateSV(): Unit = {
    // delete old build dir
    var output = "rm -rf build || true".!!
    output = "mkdir build".!!

    // generate sv files
    ChiselStage.emitSystemVerilogFile(
      gen = new ChipInterface(25_000_000),
      args = Array("--target-dir", "build/sv/"),
      firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
    )
  }
}

// Execute by running: ./mill Top.runMain chipinterface.GenerateSV
object GenerateSV extends App {
  gv.generateSV()
}

// Execute by running: ./mill Top.runMain chipinterface.Synth
object Synth extends App {
  gv.generateSV()

  // synthesis flow for ULX3S board using Yosys
  var output = "yosys -m slang -p 'read_slang build/sv/*.sv; synth_ecp5 -json build/synthesis.json -top ChipInterface -noflatten'".!!
  output = "nextpnr-ecp5 --12k --json build/synthesis.json --lpf util/ulx3s_v20.lpf --textcfg build/pnr_out.config --freq 25".!!
  output = "ecppack --compress build/pnr_out.config build/bitstream.bit".!!
  output = "fujprog build/bitstream.bit".!!
}
