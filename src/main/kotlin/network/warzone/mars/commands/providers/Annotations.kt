package network.warzone.mars.commands.providers

import app.ashcon.intake.parametric.annotation.Classifier

/**
 * String provider that enables suggestions for punishment types. Provides a greedy string.
 */
@Classifier
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class PunishmentTypes