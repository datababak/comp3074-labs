package ca.gbc.comp3074.lab_ex4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import ca.gbc.comp3074.lab_ex4.data.AppDatabase
import ca.gbc.comp3074.lab_ex4.ui.CustomerScreen
import ca.gbc.comp3074.lab_ex4.ui.CustomerViewModel
import ca.gbc.comp3074.lab_ex4.ui.CustomerViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)

        setContent {
            MaterialTheme {
                val viewModel: CustomerViewModel = viewModel(
                    factory = CustomerViewModelFactory(
                        database.customerDao()
                    )
                )

                CustomerApp(viewModel)
            }
        }
    }
}

@Composable
fun CustomerApp(
    viewModel: CustomerViewModel
) {
    val customers by viewModel.customers
        .collectAsStateWithLifecycle(initialValue = emptyList())

    CustomerScreen(
        customers = customers,
        onAddCustomer = viewModel::addCustomer,
        onDeleteCustomer = viewModel::deleteCustomer
    )
}