package blinky

/**
 * You have these two options to import ChiselStage:
 *   import circt.stage.ChiselStage
 *   import _root_.circt.stage.ChiselStage
 *
 * The latter differentiates circt from chisel3.util.circt if chisel3.util._
 * is imported, but does not create a verification directory like the former.
 *
 * I'm unsure if this is intentional, but it's something to note.
 **/

import chisel3._
import chisel3.util.Counter
import circt.stage.ChiselStage

class Blinky(freq: Int, width: Int) extends Module {
  val io = IO(new Bundle {
    val sel = Input(Bool())
    val led = Output(UInt(width.W))
  })

  // Counter that constantly counts up to freq and clears
  val (_, sec_passed) = Counter(true.B, freq)

  // Register, zeroed at reset
  val secs = RegInit(0.U(width.W))

  // Making a counter by incrementing secs whenever a second passes
  when (sec_passed) {
    secs := secs + 1.U
  }

  // Counter that constantly counts up to freq/8 and clears
  val (_, eighth_passed) = Counter(true.B, freq/8)

  // Register initialized with 1 sized to width bits
  val bounce = RegInit(1.U(width.W))

  // A single FF, zeroed at reset
  val dir = RegInit(false.B)

  when (eighth_passed) {
    // Storing the direction of the bits to be shifted
    when (bounce(width - 2)) {
      dir := true.B
    }.elsewhen (bounce(1)) {
      dir := false.B
    }

    // Choose which direction to shift
    bounce := Mux(dir, bounce >> 1, bounce << 1)
  }

  // Tying the led output to either bounce or secs
  io.led := Mux(io.sel, bounce, secs)
}
