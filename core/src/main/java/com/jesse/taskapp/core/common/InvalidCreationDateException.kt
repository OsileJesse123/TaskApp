package com.jesse.taskapp.core.common

/**
 * Raised when optional creation-date strings fail format or range validation.
 *
 * @param message plain-language reason the dates cannot be used for filtering.
 */
class InvalidCreationDateException(message: String) : IllegalArgumentException(message)
