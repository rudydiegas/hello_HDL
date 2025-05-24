package blinky

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

/**
 * Run all tests by executing:
 *   ./mill Top.test
 *
 * Run this specific test by executing:
 *   ./mill Top.test.testOnly blinky.BlinkySpec
 **/

class BlinkySpec extends AnyFlatSpec with ChiselSim {

  "Blinky" should "count and wrap" in {
    // setting freq to 1 so output increments every clock tick
    simulate(new Blinky(1, 4)) { dut =>
      // selecting seconds counter as output
      dut.io.sel.poke(0)

      // stepping through cycles
      for (n <- 0 to 16) {
        // overflow case
        if (n == 16)
          dut.io.led.expect(0)
        else
          dut.io.led.expect(n)

        dut.clock.step()
      }
    }
  }

  it should "bounce back and forth" in {
    // setting freq to 8 so that a bounce occurs every clock tick
    simulate(new Blinky(8, 4)) { dut =>
      // selecting bouncing led as output
      dut.io.sel.poke(1)

      // left shifting
      for (n <- 0 until 4) {
        dut.io.led.expect(1 << n)
        dut.clock.step()
      }

      // right shifting
      for (n <- 0 until 3) {
        dut.io.led.expect(4 >> n)
        dut.clock.step()
      }

      // check that dir changed again
      dut.io.led.expect(2)
    }
  }
}
