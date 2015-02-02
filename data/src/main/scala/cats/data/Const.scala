package cats.data

import algebra._
import cats.{Applicative, Apply, Lazy, Show, Traverse}

/**
 * [[Const]] is a phantom type, it does not contain a value of its second type parameter `B`
 * [[Const]] can be seen as a type level version of `Function.const[A, B]: A => B => A`
 */
final case class Const[A, B](getConst: A) {
  def retag[C]: Const[A, C] =
    this.asInstanceOf[Const[A, C]]

  def combine(that: Const[A, B])(implicit A: Semigroup[A]): Const[A, B] =
    Const(A.combine(getConst, that.getConst))

  def traverse[F[_], C](f: B => F[C])(implicit F: Applicative[F]): F[Const[A, C]] =
    F.pure(retag[C])

  def ===(that: Const[A, B])(implicit A: Eq[A]): Boolean =
    A.eqv(getConst, that.getConst)

  def show(implicit A: Show[A]): String =
    s"Const(${A.show(getConst)}})"
}

object Const extends ConstInstances

sealed abstract class ConstInstances extends ConstInstances0 {
  implicit def constOrder[A: Order, B]: Order[Const[A, B]] =
    Order.by[Const[A, B], A](_.getConst)

  implicit def constShow[A: Show, B]: Show[Const[A, B]] =
    Show.show[Const[A, B]](_.show)

  implicit def constTraverse[C]: Traverse[Const[C, ?]] = new Traverse[Const[C, ?]] {
    def traverse[G[_]: Applicative, A, B](fa: Const[C, A])(f: A => G[B]): G[Const[C, B]] =
      fa.traverse(f)

    def foldLeft[A, B](fa: Const[C, A], b: B)(f: (B, A) => B): B = b

    def foldRight[A, B](fa: Const[C, A], b: B)(f: (A, B) => B): B = b

    def foldRight[A, B](fa: Const[C, A], b: Lazy[B])(f: (A, Lazy[B]) => B): Lazy[B] = b
  }

  implicit def monoidConst[A, B](implicit A: Monoid[A]): Monoid[Const[A, B]] = new Monoid[Const[A, B]]{
    def empty: Const[A, B] = Const(A.empty)

    def combine(x: Const[A, B], y: Const[A, B]): Const[A, B] =
      x combine y
  }
}

sealed abstract class ConstInstances0 extends ConstInstances1 {
  implicit def constPartialOrder[A: PartialOrder, B]: PartialOrder[Const[A, B]] =
    PartialOrder.by[Const[A, B], A](_.getConst)

  implicit def constApplicative[C](implicit C: Monoid[C]): Applicative[Const[C, ?]] = new Applicative[Const[C, ?]] {
    def pure[A](x: A): Const[C, A] =
      Const(C.empty)

    def apply[A, B](fa: Const[C, A])(f: Const[C, A => B]): Const[C, B] =
      fa.retag[B] combine f.retag[B]
  }
}

sealed abstract class ConstInstances1 {
  implicit def constEq[A: Eq, B]: Eq[Const[A, B]] =
    Eq.by[Const[A, B], A](_.getConst)

  implicit def constApply[C](implicit C: Semigroup[C]): Apply[Const[C, ?]] = new Apply[Const[C, ?]] {
    def apply[A, B](fa: Const[C, A])(f: Const[C, A => B]): Const[C, B] =
      fa.retag[B] combine f.retag[B]

    def map[A, B](fa: Const[C, A])(f: A => B): Const[C, B] =
      fa.retag[B]
  }
}
