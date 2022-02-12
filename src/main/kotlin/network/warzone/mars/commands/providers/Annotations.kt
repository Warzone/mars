package network.warzone.mars.commands.providers

import app.ashcon.intake.parametric.annotation.Classifier

/**
 * String provider that enables suggestions for punishment types. Provides a greedy string.
 */
@Classifier
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class PunishmentTypes

/**
 * String provider that suggest online player names. Used for commands that support online and offline players.
 */
@Classifier
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class PlayerName