package scala.compat.java8

import org.junit.Test
import org.junit.Assert._

import collectionImpl._

class IncStepperA(private val size0: Long) extends NextStepper[Int] {
  if (size0 < 0) throw new IllegalArgumentException("Size must be >= 0L")
  private var i = 0L
  def characteristics = 0
  def knownSize = math.max(0L, size0 - i)
  def hasStep = i < knownSize
  def nextStep() = { i += 1; (i - 1).toInt }
  def substep() = if ((knownSize - i) <= 1) null else {
    val sub = new IncStepperA(size0 - (size0 - i)/2)
    sub.i = i
    i = sub.size0
    sub
  }
  def typedPrecisely = this
  def spliterator = ???
}

class IncStepperB(private val size0: Long) extends TryStepper[Int] {
  if (size0 < 0) throw new IllegalArgumentException("Size must be >= 0L")
  private var i = 0L
  def characteristics = 0
  def knownSize = math.max(0L, size0 - i)
  def tryStep(f: Int => Unit): Boolean = if (i >= size0) false else { f(i.toInt); i += 1; true }
  def substep() = if ((knownSize - i) <= 1) null else {
    val sub = new IncStepperB(size0 - (size0 - i)/2)
    sub.i = i
    i = sub.size0
    sub
  }
  def typedPrecisely = this
  def spliterator = ???
}

class StepperTest {
  def subs[Z, A, CC <: Stepper[A]](zero: Z)(s: Stepper[A])(f: Stepper[A] => Z, op: (Z, Z) => Z): Z = {
    val ss = s.substep()
    if (ss == null) op(zero, f(s))
    else {
      val left = subs(zero)(ss)(f, op)
      subs(left)(s)(f, op)
    }
  }

  val sizes = Vector(0, 1, 2, 4, 15, 17, 2512)
  def sources: Vector[(Int, Stepper[Int])] = sizes.flatMap(i => Vector(i -> new IncStepperA(i), i -> new IncStepperB(i)))

  @Test
  def count_only() {
    sources.foreach{ case (i, s) => assertEquals(i, s.count) }
    sources.foreach{ case (i, s) => assertEquals(i, subs(0)(s)(_.count.toInt, _ + _)) }
  }
}
