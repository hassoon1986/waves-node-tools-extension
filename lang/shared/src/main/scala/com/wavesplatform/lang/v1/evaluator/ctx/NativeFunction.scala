package com.wavesplatform.lang.v1.evaluator.ctx

import cats.data.EitherT
import com.wavesplatform.lang.TrampolinedExecResult
import com.wavesplatform.lang.v1.FunctionHeader
import com.wavesplatform.lang.v1.compiler.Terms.EXPR
import com.wavesplatform.lang.v1.compiler.Types._
import monix.eval.Coeval

sealed trait BaseFunction {
  def signature: FunctionTypeSignature
  def header: FunctionHeader = signature.header
  def cost: Long
  def name: String
}

case class FunctionTypeSignature(result: TYPEPLACEHOLDER, args: Seq[TYPEPLACEHOLDER], header: FunctionHeader)

case class NativeFunction private (name: String, cost: Long, signature: FunctionTypeSignature, ev: List[Any] => Either[String, Any])
    extends BaseFunction {
  def eval(args: List[Any]): TrampolinedExecResult[Any] = EitherT.fromEither[Coeval](ev(args))
}

object NativeFunction {

  def apply(name: String, cost: Long, internalName: Short, resultType: TYPEPLACEHOLDER, args: (String, TYPEPLACEHOLDER)*)(
      ev: List[Any] => Either[String, Any]) =
    new NativeFunction(name, cost, FunctionTypeSignature(???, args.map(_._2), FunctionHeader.Native(internalName)), ev)

}

case class UserFunction private (name: String, cost: Long, signature: FunctionTypeSignature, ev: List[EXPR] => EXPR) extends BaseFunction

object UserFunction {

  def apply(name: String, cost: Long, resultType: TYPEPLACEHOLDER, args: (String, TYPEPLACEHOLDER)*)(ev: List[EXPR] => EXPR) =
    new UserFunction(name, cost, FunctionTypeSignature(???, args.map(_._2), FunctionHeader.User(name)), ev)

}
