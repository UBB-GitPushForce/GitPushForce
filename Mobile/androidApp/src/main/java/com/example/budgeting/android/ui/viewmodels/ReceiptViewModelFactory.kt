import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.budgeting.android.ui.viewmodels.ReceiptViewModel

class ReceiptViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReceiptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReceiptViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}