package mcchat.server.helpers

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class Position(val position: Int)

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class OpCode(val opcode: Byte)
