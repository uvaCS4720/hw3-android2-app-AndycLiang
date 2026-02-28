package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.nd.pmcburne.hwapp.one.ui.ScoresScreen
import edu.nd.pmcburne.hwapp.one.ui.ScoresViewModel
import edu.nd.pmcburne.hwapp.one.ui.ScoresViewModelFactory
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DatabaseModule.create(this)
        val api = NetworkModule.createService()
        val repository = ScoresRepository(api = api, db = database)
        val factory = ScoresViewModelFactory(application = application, repository = repository)

        setContent {
            HWStarterRepoTheme {
                val viewModel: ScoresViewModel = viewModel(factory = factory)
                ScoresScreen(viewModel = viewModel)
            }
        }
    }
}
