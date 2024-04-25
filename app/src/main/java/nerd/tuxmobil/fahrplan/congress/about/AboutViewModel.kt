package nerd.tuxmobil.fahrplan.congress.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.about.AboutViewEvent.OnPostalAddressClick
import nerd.tuxmobil.fahrplan.congress.commons.ExternalNavigation
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.ExecutionContext

class AboutViewModel(
    private val repository: AppRepository = AppRepository,
    private val executionContext: ExecutionContext,
    private val externalNavigation: ExternalNavigation,
    private val aboutParameterFactory: AboutParameterFactory,
) : ViewModel() {

    private val mutableAboutParameter = MutableStateFlow(AboutParameter())
    val aboutParameter = mutableAboutParameter.asStateFlow()

    init {
        launch {
            repository.meta.collect { meta ->
                mutableAboutParameter.value = aboutParameterFactory.createAboutParameter(meta)
            }
        }
    }

    fun onViewEvent(viewEvent: AboutViewEvent) {
        when (viewEvent) {
            is OnPostalAddressClick -> externalNavigation.openMap(viewEvent.textualAddress)
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(executionContext.database, block = block)
    }

}
