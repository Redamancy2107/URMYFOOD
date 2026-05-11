package com.urmyfood.user.domain.usecase

import com.urmyfood.user.domain.model.Result
import com.urmyfood.user.domain.repository.GuestRepository

class LoginAsGuestUseCase(private val guestRepository: GuestRepository) {
    operator fun invoke(): Result<Unit> {
        guestRepository.setGuest()
        return Result.Success(Unit)
    }
}
