package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.PaymentMethodRepository
import com.example.expensetracker.data.seed.DefaultSeedData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
) : ViewModel() {
    fun bootstrap() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (categoryRepository.countAll() == 0) {
                categoryRepository.insertAll(DefaultSeedData.categories(now))
            }
            if (paymentMethodRepository.countAll() == 0) {
                paymentMethodRepository.insertAll(DefaultSeedData.paymentMethods(now))
            }
        }
    }
}

